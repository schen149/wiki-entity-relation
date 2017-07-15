package edu.illinois.cs.cogcomp.wikirelation.wikipedia;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiInitializationException;
import edu.illinois.cs.cogcomp.wikirelation.core.PageIDLinker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import wikipedia Page title to ID map from JWPL
 *
 * TODO: JWPL is overkill.. I'm using it only because it's faster (given that JWPL is already set up).
 * TODO: A class with similar functionality exists already. See xlwikifier.wikipedia.DumpReader
 */
public class PageIDImporter {

    private static Logger logger = LoggerFactory.getLogger(PageIDImporter.class);

    public static Wikipedia wiki = null;
    public PageIDLinker titleMap;

    private String configFile;

    public PageIDImporter(String configFile) {

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

        this.configFile = configFile;
        this.titleMap = new PageIDLinker(false, configFile);
    }

    public void parse() {
        logger.info("Start Parsing page id...");
        Iterable<Integer> it = wiki.getPageIds();

        it.forEach(this::updateMap);
        logger.info("Finish Parsing page id.");
    }

    /* TODO: not the best idea to catch exception here */
    public void updateMap(int pageId) {
        try{
            String title = wiki.getTitle(pageId).getWikiStyleTitle();
            titleMap.put(pageId, title);
        }
        catch (WikiApiException e) {
            logger.info("Can't find page with pageId " + pageId);
        }
    }

    public void closeDB() {
        if (this.titleMap != null) {
            titleMap.closeDB();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java DemoPageIDLinker [config-file-path]");
            System.exit(1);
        }

        PageIDImporter importer = new PageIDImporter(args[0]);
        importer.parse();
        importer.closeDB();
    }
}
