package edu.illinois.cs.cogcomp.wikirelation.wikipedia;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.wiki.parsing.MLWikiDumpFilter;
import edu.illinois.cs.cogcomp.wiki.parsing.processors.PageMeta;
import edu.illinois.cs.cogcomp.wikirelation.datastructure.MemoryCooccuranceMap;
import info.bliki.wiki.dump.WikiArticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by schen149 on 6/24/17.
 */
public class RelationMapImporter {
    private static Logger logger = LoggerFactory.getLogger(RelationMapImporter.class);

    private String dumpdir;
    private String date;
    private String language;
    private String dumpfile, pagefile, langfile, redirectfile;

    private MemoryCooccuranceMap cache;
    private RelationMapLinker relation;
    private PageIDLinker idLinker;

    public RelationMapImporter(String dumpDir, String date, String language) {
        this.dumpdir = dumpDir;
        this.date = date;
        this.language = language;
        this.cache = new MemoryCooccuranceMap();
        this.idLinker = new PageIDLinker(true);
        this.relation = new RelationMapLinker(false);
        setPath();
    }

    private void setPath() {
        if (!(new File(dumpdir).exists()))
            new File(dumpdir).mkdir();
        dumpfile = dumpdir + File.separator + language + "wiki-" + date + "-pages-articles.xml.bz2";
    }

    public void parseWikiDump() throws IOException, SAXException {
        logger.info("Parsing wikidump " + dumpfile);

        MLWikiDumpFilter filter = new MLWikiDumpFilter(56) {
            @Override
            public void processAnnotation(WikiArticle page, PageMeta meta, TextAnnotation ta) throws Exception {
                if (page.getTitle().contains("/"))
                    return;

                if (ta == null || !ta.hasView(ViewNames.WIKIFIER)
                        || meta.isRedirect() || meta.isDisambiguationPage())
                    return;

                SpanLabelView wikiView = (SpanLabelView) ta.getView(ViewNames.WIKIFIER);

                Integer pageId = Integer.parseInt(page.getId());
                for (Constituent c : wikiView.getConstituents()) {
                    Integer hrefPageId = idLinker.getIDFromTitle(c.getLabel());
                    cache.count(pageId, hrefPageId);
                }
            }
        };

        filter.setLang(language);
        MLWikiDumpFilter.parseDump(dumpfile, filter);
    }

    public void populateDB() {
        logger.info("Populating mapdb...");

        this.cache.keySet().parallelStream()
                .forEach(id -> relation.put(id, sortKeyByValue(this.cache.get(id))));

        logger.info("Finished!");
    }



    public void closeDB() {
        this.relation.closeDB();
    }

    private static int[] sortKeyByValue(Map<Integer, Integer> map) {
        List<Map.Entry<Integer, Integer>> candsCounts = new ArrayList<>(map.entrySet());

        candsCounts.sort((c1, c2) -> c2.getValue() - c1.getValue()); // Descending order

        return candsCounts.stream()
                .mapToInt(Map.Entry::getKey)
                .toArray();
    }

    public static void main(String args[]) {
        RelationMapImporter rmg = new RelationMapImporter("/media/evo/data/wiki/enwiki-20170601/", "20170601", "en");

        try{
            rmg.parseWikiDump();
            rmg.populateDB();
        }
        catch (Exception e) {
            logger.info("Error reading wikipedia dump at /media/evo/data/wiki/enwiki-20170601/");
            e.printStackTrace();
            System.exit(1);
        }
    }

}
