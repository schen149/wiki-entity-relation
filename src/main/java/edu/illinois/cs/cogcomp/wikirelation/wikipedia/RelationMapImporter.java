package edu.illinois.cs.cogcomp.wikirelation.wikipedia;

import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.SpanLabelView;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.wiki.parsing.MLWikiDumpFilter;
import edu.illinois.cs.cogcomp.wiki.parsing.processors.PageMeta;
import edu.illinois.cs.cogcomp.wikirelation.Util.CacheUtil;
import edu.illinois.cs.cogcomp.wikirelation.datastructure.MemoryCooccuranceMap;
import info.bliki.wiki.dump.WikiArticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


/**
 * Some calculation
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

    int totalBatches; // Number of batches it takes

    public RelationMapImporter(String dumpDir, String date, String language, int totalBatches) {
        this.dumpdir = dumpDir;
        this.date = date;
        this.language = language;
        this.totalBatches = totalBatches;
        this.idLinker = new PageIDLinker(true);
        setPath();
    }

    private void setPath() {
        if (!(new File(dumpdir).exists()))
            new File(dumpdir).mkdir();
        dumpfile = dumpdir + File.separator + language + "wiki-" + date + "-pages-articles.xml.bz2";
    }

    public void batchProcess() throws IOException, SAXException {
        for (int currentBatch = 0; currentBatch < totalBatches; currentBatch++) {
            logger.info("Start Processing Batch "+currentBatch+"/"+this.totalBatches);

            this.cache = new MemoryCooccuranceMap(totalBatches, currentBatch);
            loadCurrentBatch(currentBatch);

            populateDB();
        }
    }

    private void loadCurrentBatch(int currentBatch){

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
                            BufferedWriter bw1 = new BufferedWriter(new FileWriter(dumpdir+"/cooccurance/cooccurance.txt", true));
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

    private void populateDB() {
        logger.info("Populating mapdb...");

        this.relation = new RelationMapLinker(false);

        List<Map.Entry<Long, Short>> sortedList = new ArrayList<>(this.cache.memCache.entrySet());

        sortedList.sort(new Comparator<Map.Entry<Long, Short>>() {
            @Override
            public int compare(Map.Entry<Long, Short> e1, Map.Entry<Long, Short> e2) {
                int k1 = CacheUtil.getHigher32bitFromLong(e1.getKey());
                int k2 = CacheUtil.getHigher32bitFromLong(e2.getKey());
                if (k1 == k2)
                    return e2.getValue() - e1.getValue();
                else
                    return k1 - k2;
            }
        });

        int curPage = -1;
        List<Integer> cands = new ArrayList<>();
        for (Map.Entry<Long,Short> e: sortedList) {
            int k = CacheUtil.getHigher32bitFromLong(e.getKey());
            int cand = CacheUtil.getLower32bitFromLong(e.getKey());
            if (curPage == -1)
                curPage = k;

            if (curPage != k) {
                int[] candsArray = cands.stream().mapToInt(i -> i).toArray();
                relation.put(curPage, candsArray);
                curPage = k;
                cands = new ArrayList<>();
            }
            else
                cands.add(cand);
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
        RelationMapImporter rmg = new RelationMapImporter("/media/evo/data/wiki/enwiki-20170601/",
                "20170601", "en", 5);

        try{
//            rmg.batchProcess();
            rmg.parseWikiDump();
        }
        catch (Exception e) {
            logger.info("Error reading wikipedia dump at /media/evo/data/wiki/enwiki-20170601/");
            e.printStackTrace();
            System.exit(1);
        }
    }

}
