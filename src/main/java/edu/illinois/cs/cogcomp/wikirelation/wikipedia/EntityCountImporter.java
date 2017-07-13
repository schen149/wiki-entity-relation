package edu.illinois.cs.cogcomp.wikirelation.wikipedia;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.wiki.parsing.MLWikiDumpFilter;
import edu.illinois.cs.cogcomp.wiki.parsing.processors.PageMeta;
import info.bliki.wiki.dump.WikiArticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.*;


/**
 * Some calculation
 */
public class EntityCountImporter {
    private static Logger logger = LoggerFactory.getLogger(EntityCountImporter.class);

    private String dumpdir;
    private String date;
    private String language;
    private String dumpfile;

    private EntityCountLinker entityCountLinker;


    public EntityCountImporter(String dumpDir, String date, String language, String configFile) {
        this.dumpdir = dumpDir;
        this.date = date;
        this.language = language;
        this.entityCountLinker = new EntityCountLinker(false, configFile);
        setPath();
    }

    private void setPath() {
        if (!(new File(dumpdir).exists()))
            new File(dumpdir).mkdir();
        dumpfile = dumpdir + File.separator + language + "wiki-" + date + "-pages-articles.xml.bz2";
    }

    private void parseWikiDump() throws IOException, SAXException {
        logger.info("Parsing wikidump " + dumpfile);

        MLWikiDumpFilter filter = new MLWikiDumpFilter(56) {
            @Override
            public void processAnnotation(WikiArticle page, PageMeta meta, TextAnnotation ta) throws Exception {
                if (ta == null || !ta.hasView(ViewNames.WIKIFIER)
                        || meta.isRedirect() || meta.isDisambiguationPage())
                    return;

                if (page.getTitle().contains("/"))
                    return;

                Integer pageId = Integer.parseInt(page.getId());

                SpanLabelView wikiView = (SpanLabelView) ta.getView(ViewNames.WIKIFIER);

                entityCountLinker.put(pageId, wikiView.count());
            }
        };

        filter.setLang(language);
        MLWikiDumpFilter.parseDump(dumpfile, filter);
    }


    public static void main(String args[]) {
        EntityCountImporter importer = new EntityCountImporter("/media/evo/data/wiki/enwiki-20170601/",
                "20170601", "en","config/sihaopc-english-170601.properties");

        try{
            importer.parseWikiDump();
        }
        catch (Exception e) {
            logger.info("Error reading wikipedia dump at /media/evo/data/wiki/enwiki-20170601/");
            e.printStackTrace();
            System.exit(1);
        }
    }

}
