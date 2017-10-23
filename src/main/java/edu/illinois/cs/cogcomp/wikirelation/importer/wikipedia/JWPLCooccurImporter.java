package edu.illinois.cs.cogcomp.wikirelation.importer.wikipedia;

import com.sun.prism.PixelFormat;
import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import edu.illinois.cs.cogcomp.wikirelation.core.CooccuranceMapLinker;
import edu.illinois.cs.cogcomp.wikirelation.core.PageIDLinker;
import edu.illinois.cs.cogcomp.wikirelation.util.DataTypeUtil;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Import wikipedia Page title to ID map from JWPL
 */
public class JWPLCooccurImporter {

    public static Wikipedia wiki = null;
    public CooccuranceMapLinker cooccur;

    private int processed, dumped;
    private String configFile;

    private Map<Long, Integer> memCount;

    public JWPLCooccurImporter(String configFile) {

        memCount = new HashMap<>();

        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setHost("localhost");
        dbConfig.setDatabase("jwpl");
        dbConfig.setUser("schen149");
        dbConfig.setPassword("Sihao0217");
        dbConfig.setLanguage(WikiConstants.Language.english);
        try {
            wiki = new Wikipedia(dbConfig);
        } catch (WikiInitializationException e) {
            e.printStackTrace();
        }


        this.processed = 0;
        this.dumped = 0;
        this.configFile = configFile;
        this.cooccur = new CooccuranceMapLinker(false, configFile);
    }

    public void parse() {
        Iterable<Integer> it = wiki.getPageIds();
        it.forEach(this::updateMap);

        memCount.forEach((key, value) -> {
            cooccur.put(key, value);
            dumped++;

            if (dumped % 500 == 0) {
                System.out.println("Dumped: " + dumped);
            }
        });
    }

    /* TODO: not the best idea to catch exception here */
    public void updateMap(Integer pageId) {

        if (pageId == null) return;

        try {
            Page page = wiki.getPage(pageId);

            if (page.isDisambiguation() || page.isRedirect()) return;

            for (Integer id2 : page.getInlinkIDs()) {
                if (id2 == null) continue;

                memPut(pageId, id2);
            }
        }
        catch (WikiApiException e) {
            System.out.println("Can't find page with pageId " + pageId);
        }

        processed++;

        if (processed % 500 == 0) {
            System.out.println("Processed: " + processed);
        }

    }

    private void memPut(int id1, int id2) {
        Long key = DataTypeUtil.concatTwoIntToLong(id1, id2);
        if (memCount.containsKey(key)) {
            memCount.put(key, memCount.get(key) + 1);
        }
        else
            memCount.put(key, 1);
    }
    public void closeDB() {
        if (this.cooccur != null) {
            cooccur.closeDB();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java JWPLCooccurImporter [config-file-path]");
            System.exit(1);
        }

        JWPLCooccurImporter importer = new JWPLCooccurImporter(args[0]);
        importer.parse();
        importer.closeDB();
    }
}
