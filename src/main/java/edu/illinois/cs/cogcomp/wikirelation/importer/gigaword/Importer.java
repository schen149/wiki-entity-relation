package edu.illinois.cs.cogcomp.wikirelation.importer.gigaword;

import edu.illinois.cs.cogcomp.thrift.base.Span;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.wikirelation.core.CooccuranceMapLinker;
import edu.illinois.cs.cogcomp.wikirelation.core.PageIDLinker;
import edu.illinois.cs.cogcomp.wikirelation.util.WikiUtil;
import org.apache.commons.io.IOUtils;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Importer {

    private static Logger logger = LoggerFactory.getLogger(Importer.class);

    private String recordDirPath;
    private String configFile;
    private PageIDLinker idLinker;
    private CooccuranceMapLinker coourranceMap;

    public Importer(String recordDirPath, String configFile) throws FileNotFoundException {
        this.recordDirPath = recordDirPath;
        File recordDir = new File(recordDirPath);
        if (!recordDir.exists() || !recordDir.isDirectory())
            throw new FileNotFoundException("The record directory doesn't exist or is not a directory.");

        this.configFile = configFile;
        this.idLinker = new PageIDLinker(true, configFile);
        this.coourranceMap = new CooccuranceMapLinker(false, configFile);
    }

    public void populateDB(String fileIndex) {

        int allDocCount = 0;
        int processedCount = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileIndex));
            logger.info("File index loaded:\t" + fileIndex);
            String line;
            while ((line = br.readLine()) != null) {

                //if (!line.endsWith("_ner")) continue;

                line = line.trim();
                String recPath = recordDirPath + File.separator + line;
                try {
                    File fin = new File(recPath);
                    if (!fin.isFile()) continue;
                    
                    FileInputStream in = new FileInputStream(recPath);
                    Record rec = deserializeRecordFromBytes(IOUtils.toByteArray(in));
                    if (rec.getLabelViews().containsKey("wikifier")) {
//                        // testing
//                        logger.info("Jackpot!");
//                        return;

                        List<Span> spans = rec.getLabelViews().get("wikifier").getLabels();

                        /* Pre-filter out non NE-type links */
                        List<String> links = spans.stream()
                                .map(Span::getLabel)
                                .map(WikiUtil::url2wikilink)
                                .filter(Objects::nonNull)
                                .filter(l -> WikiUtil.isTitleNEType(l, "en"))
                                .collect(Collectors.toList());

                        for (int i = 0; i < links.size(); ++i) {
                            for (int j = i + 1; j < spans.size(); j++) {
                                String link1 = links.get(i);
                                String link2 = links.get(j);

                                Integer curId1 = idLinker.getIDFromTitle(link1);
                                Integer curId2 = idLinker.getIDFromTitle(link2);

                                if ((curId1 != null) && (curId2 != null)) {
                                    coourranceMap.put(curId1, curId2);
                                }
                            }
                        }
                        processedCount++;
                    }
                }
                catch (Exception e) {
                    continue;
                }

                allDocCount++;
                if (allDocCount % 10000 == 0)
                    logger.info("Document count:\t" + allDocCount + "\tProcessed:\t" + processedCount);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static Record deserializeRecordFromBytes(byte[] bytes) throws TException {
        Record rec = new Record();
        TDeserializer td = new TDeserializer();
        td.deserialize(rec, bytes);
        return rec;
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java Importer [record-dir] [config-file] [record-file-index]");
            System.exit(1);
        }
        String recordDir = args[0];
        String configFile = args[1];
        String fileIndex = args[2];
        try {
            Importer importer = new Importer(recordDir, configFile);
            importer.populateDB(fileIndex);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
