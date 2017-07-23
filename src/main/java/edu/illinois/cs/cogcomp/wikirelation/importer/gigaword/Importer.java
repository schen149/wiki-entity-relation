package edu.illinois.cs.cogcomp.wikirelation.importer.gigaword;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.thrift.base.Span;
import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.wikirelation.core.CooccuranceMapLinker;
import edu.illinois.cs.cogcomp.wikirelation.core.PageIDLinker;
import edu.illinois.cs.cogcomp.wikirelation.util.CommonUtil;
import edu.illinois.cs.cogcomp.wikirelation.util.WikiUtil;
import org.apache.commons.io.IOUtils;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Importer {

    private static Logger logger = LoggerFactory.getLogger(Importer.class);

    private String recordDirPath;
    private File recordDir;
    private String configFile, indexFile;
    private PageIDLinker idLinker;
    private final CooccuranceMapLinker coourranceMap;
    private List<String> processed;

    public Importer(String recordDirPath, String configFile, String indexFile) throws FileNotFoundException {
        this.recordDirPath = recordDirPath;
        this.recordDir = new File(recordDirPath);
        this.processed = new ArrayList<>();

        if (!recordDir.exists() || !recordDir.isDirectory())
            throw new FileNotFoundException("The record directory doesn't exist or is not a directory.");

        this.configFile = configFile;
        this.indexFile = indexFile;
        this.idLinker = new PageIDLinker(true, configFile);
        this.coourranceMap = new CooccuranceMapLinker(false, configFile);

        /* Attach shutdown hook */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(indexFile));
                    for (String file: processed) {
                        bw.write(file);
                        bw.newLine();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void populateDB() {

        int count = 0;

        File[] recordFiles = recordDir.listFiles();
        if (recordFiles == null) {
            logger.info("The target record dir is empty.");
            return;
        }
        logger.info("Detected Files:\t" + recordFiles.length);

        for (File f: recordFiles) {
            if (f.isFile()){
                try {
                    FileInputStream in = new FileInputStream(f);
                    Record rec = deserializeRecordFromBytes(IOUtils.toByteArray(in));
                    Map<Pair<Integer, Integer>, Integer> cache = new HashMap<>();
                    if (rec.getLabelViews().containsKey("wikifier")) {

                        List<Span> spans = rec.getLabelViews().get("wikifier").getLabels();

                        /* Pre-filter out non NE-type links */
                        List<String> links = spans.stream()
                                .map(Span::getLabel)
                                .map(WikiUtil::url2wikilink)
                                .filter(Objects::nonNull)
                                .filter(l -> WikiUtil.isTitleNEType(l, "en"))
                                .collect(Collectors.toList());

                        for (int i = 0; i < links.size(); ++i) {
                            for (int j = i + 1; j < links.size(); ++j) {
                                String link1 = links.get(i);
                                String link2 = links.get(j);

                                Integer curId1 = idLinker.getIDFromTitle(link1);
                                Integer curId2 = idLinker.getIDFromTitle(link2);

                                if ((curId1 != null) && (curId2 != null) && !(curId1.equals(curId2))) {
                                    Pair<Integer, Integer> pair = new Pair<>(curId1, curId2);
                                    if (cache.containsKey(pair))
                                        cache.put(pair, cache.get(pair) + 1);
                                    else
                                        cache.put(pair, 1);
                                }
                            }
                        }

                        cache.entrySet().forEach(e ->
                                this.coourranceMap.put(e.getKey().getFirst(), e.getKey().getSecond(), e.getValue()));
                    }
                } catch (Exception e) {
                    continue;
                }
                processed.add(f.getName());
                count++;
                if (count % 100 == 0)
                    logger.info("Processed:\t" + count);
            }
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
            System.err.println("Usage: java Importer [record-dir] [config-file] [file-index]");
            System.exit(1);
        }
        String recordDir = args[0];
        String configFile = args[1];
        String index = args[2];
        try {
            Importer importer = new Importer(recordDir, configFile, index);
            importer.populateDB();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
