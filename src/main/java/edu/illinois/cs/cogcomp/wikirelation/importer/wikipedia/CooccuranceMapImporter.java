package edu.illinois.cs.cogcomp.wikirelation.importer.wikipedia;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.wiki.parsing.MLWikiDumpFilter;
import edu.illinois.cs.cogcomp.wiki.parsing.processors.PageMeta;
import edu.illinois.cs.cogcomp.wikirelation.core.CooccuranceMapLinker;
import edu.illinois.cs.cogcomp.wikirelation.core.PageIDLinker;
import edu.illinois.cs.cogcomp.wikirelation.util.WikiUtil;
import info.bliki.wiki.dump.WikiArticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;


/**
 * Some calculation
 */
public class CooccuranceMapImporter {
    private static Logger logger = LoggerFactory.getLogger(CooccuranceMapImporter.class);

    private String dumpdir;
    private String date;
    private String language;
    private String dumpfile, cooccurfile, configfile;

    private CooccuranceMapLinker relation;
    private PageIDLinker idLinker;

    public CooccuranceMapImporter(String dumpDir, String date, String language, String configfile) {
        this.dumpdir = dumpDir;
        this.date = date;
        this.language = language;
        this.configfile = configfile;
        this.idLinker = new PageIDLinker(true, configfile);
        setPath();
    }

    private void setPath() {
        if (!(new File(dumpdir).exists()))
            new File(dumpdir).mkdir();
        dumpfile = dumpdir + File.separator + language + "wiki-" + date + "-pages-articles.xml.bz2";
        cooccurfile = dumpdir + "/cooccurance/cooccurance.txt";
    }

    private void parseWikiDump() throws IOException, SAXException {
        logger.info("Parsing wikidump " + dumpfile);
        StringBuilder cand_pair = new StringBuilder();

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

                for (Constituent c : wikiView.getConstituents()) {
                    Integer hrefPageId = idLinker.getIDFromTitle(c.getLabel());

                    if (hrefPageId == null) continue;
                    synchronized (cand_pair) {
                        cand_pair.append(pageId+"\t"+hrefPageId+"\n");

                        if (cand_pair.length() > 100000) {
                            BufferedWriter bw1 = new BufferedWriter(new FileWriter(cooccurfile, true));
                            bw1.write(cand_pair.toString());
                            bw1.close();
                            cand_pair.delete(0, cand_pair.length());
                        }
                     }
                }
            }
        };

        filter.setLang(language);
        MLWikiDumpFilter.parseDump(dumpfile, filter);
    }

    private void populateDB() throws IOException{
        logger.info("Populating mapdb...");

        this.relation = new CooccuranceMapLinker(false, this.configfile);

        BufferedReader br = new BufferedReader(new FileReader(cooccurfile));
        String line;
        int count = 0;
        while ((line = br.readLine()) != null) {
            String[] ids = line.split("\\t");
            if (ids.length != 2) continue;
            Integer pageId1 = Integer.parseInt(ids[0]);
            Integer pageId2 = Integer.parseInt(ids[1]);

            // only record NE titles
            String title1 = idLinker.getTitleFromID(pageId1);
            String title2 = idLinker.getTitleFromID(pageId2);

            if (title1 != null && title2 != null
                    && WikiUtil.isTitleNEType(title1, "en") && WikiUtil.isTitleNEType(title2, "en")) {
                this.relation.put(pageId1, pageId2);
            }

            count++;

            if (count % 10000 == 0)
                logger.info("Lines processed: " + count);
        }

        this.relation.closeDB();
        logger.info("Finished!");
    }

    private static int[] sortKeyByValue(Map<Integer, Integer> map) {
        List<Map.Entry<Integer, Integer>> candsCounts = new ArrayList<>(map.entrySet());

        candsCounts.sort((c1, c2) -> c2.getValue() - c1.getValue()); // Descending order

        return candsCounts.stream()
                .mapToInt(Map.Entry::getKey)
                .toArray();
    }

    public static void main(String args[]) {
        CooccuranceMapImporter rmg = new CooccuranceMapImporter("/media/evo/data/wiki/enwiki-20170601/",
                "20170601", "en", "/home/squirrel/project/wiki-entity-relation/config/sihaopc-english-170601.properties");

        try{
            rmg.populateDB();
//            rmg.parseWikiDump();
        }
        catch (Exception e) {
            logger.info("Error reading wikipedia dump at /media/evo/data/wiki/enwiki-20170601/");
            e.printStackTrace();
            System.exit(1);
        }
    }

}