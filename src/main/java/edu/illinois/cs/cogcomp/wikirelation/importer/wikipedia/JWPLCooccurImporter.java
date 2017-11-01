package edu.illinois.cs.cogcomp.wikirelation.importer.wikipedia;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import edu.illinois.cs.cogcomp.wikirelation.core.CooccurrenceMapLinker;
import edu.illinois.cs.cogcomp.wikirelation.util.DataTypeUtil;
import org.eclipse.collections.api.map.primitive.MutableLongIntMap;
import org.eclipse.collections.impl.map.mutable.primitive.LongIntHashMap;

/**
 * Import wikipedia Page title to ID map from JWPL
 */
public class JWPLCooccurImporter {

    public static Wikipedia wiki = null;
    public CooccurrenceMapLinker cooccur;

    private int processed, dumped;
    private String configFile;

    private MutableLongIntMap memCount;

    public JWPLCooccurImporter(String configFile) {

        memCount = new LongIntHashMap();

        DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        dbConfig.setHost("localhost");
        dbConfig.setDatabase("jwpl");
        dbConfig.setUser("schen149");
        dbConfig.setPassword("Sihao0217");
        dbConfig.setLanguage(WikiConstants.Language.english);
        try {
            wiki = new Wikipedia(dbConfig);
//            System.out.println(wiki.getPage("Alex_Smith_(tight_end)").getInlinkIDs().size());
//            System.out.println(wiki.getPage("Alex_Smith_(tight_end)").getOutlinkIDs().size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.processed = 0;
        this.dumped = 0;
        this.configFile = configFile;
        this.cooccur = new CooccurrenceMapLinker(false, configFile);
    }

    public void parse() {
        Iterable<Integer> it = wiki.getPageIds();
        it.forEach(this::updateMap);

        memCount.forEachKeyValue((key, value) -> {
            cooccur.put(key, value);
            dumped++;

            if (dumped % 2048 == 0) {
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

            for (Integer id2 : page.getOutlinkIDs()) {
                if (id2 == null) continue;

                memPut(pageId, id2);
            }
        }
        catch (WikiApiException e) {
            System.out.println("Can't find page with pageId " + pageId);
        }

        processed++;

        if (processed % 1024 == 0) {
            System.out.println("Processed: " + processed);

            if (processed % 8192 == 0) {
                System.out.println("MemCount size: " + memCount.size());

                if (memCount.size() > 100000000) {
                    memCount.forEachKeyValue((key, value) -> {
                        cooccur.put(key, value);
                        dumped++;

                        if (dumped % 2048 == 0) {
                            System.out.println("Dumped: " + dumped);
                        }
                    });

                    memCount.clear();
                }
            }

        }

    }

    private void memPut(int id1, int id2) {
        Long key1 = DataTypeUtil.concatTwoIntToLong(id1, id2);
        Long key2 = DataTypeUtil.concatTwoIntToLong(id2, id1);
        memCount.addToValue(key1, 1);
        memCount.addToValue(key2, 1);
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
