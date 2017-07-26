package edu.illinois.cs.cogcomp.wikirelation.importer.wikipedia;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
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
import java.util.stream.Collectors;


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
        cooccurfile = dumpdir + "/cooccurance/entities.txt";
    }

    private void parseWikiDump() throws IOException, SAXException {
        logger.info("Parsing wikidump " + dumpfile);
        StringBuilder entities = new StringBuilder();

        MLWikiDumpFilter filter = new MLWikiDumpFilter(56) {
            @Override
            public void processAnnotation(WikiArticle page, PageMeta meta, TextAnnotation ta) throws Exception {
                if (ta == null || !ta.hasView(ViewNames.WIKIFIER)
                        || meta.isRedirect() || meta.isDisambiguationPage())
                    return;

                if (page.getTitle().contains("/"))
                    return;

                SpanLabelView wikiView = (SpanLabelView) ta.getView(ViewNames.WIKIFIER);

                String entityIds = wikiView.getConstituents().stream()
                        .map(Constituent::getLabel)
                        .filter(s -> !s.isEmpty())
                        .filter(title -> WikiUtil.isTitleNEType(title, "en"))
                        .map(title -> idLinker.getIDFromTitle(title))
                        .filter(Objects::nonNull)
                        .map(i -> i.toString())
                        .collect(Collectors.joining("\t"));

                synchronized (entities) {
                    if (!entityIds.isEmpty()) {
                        entities.append(entityIds).append("\n");

                        if (entities.length() > 100000) {
                            BufferedWriter bw1 = new BufferedWriter(new FileWriter(cooccurfile, true));
                            bw1.write(entities.toString());
                            bw1.close();
                            entities.delete(0, entities.length());
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

            Map<Pair<Integer, Integer>, Integer> buffer = new HashMap<>();

            for (int i = 0; i < ids.length; ++i) {
                try {
                    Integer pageId1 = Integer.parseInt(ids[i]);
                    for (int j = i + 1; j < ids.length; ++j) {
                        Integer pageId2 = Integer.parseInt(ids[j]);

                        Pair<Integer, Integer> pair = new Pair<>(pageId1, pageId2);
                            if (buffer.containsKey(pair))
                                buffer.put(pair, buffer.get(pair) + 1);
                            else
                                buffer.put(pair, 1);
                        }
                }
                catch (NumberFormatException e) {
                }
            }

            buffer.entrySet().forEach(e ->
                    this.relation.put(e.getKey().getFirst(), e.getKey().getSecond(), e.getValue()));
            count++;
            if (count % 500 == 0)
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
                "20170601", "en", "/home/squirrel/project/wiki-entity-relation/config/sihaopc-tmp.properties");

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
