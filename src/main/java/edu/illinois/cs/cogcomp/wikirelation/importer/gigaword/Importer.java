package edu.illinois.cs.cogcomp.wikirelation.importer.gigaword;

import edu.illinois.cs.cogcomp.thrift.curator.Record;
import edu.illinois.cs.cogcomp.wikirelation.core.PageIDLinker;
import org.apache.commons.io.IOUtils;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class Importer {

    private static Logger logger = LoggerFactory.getLogger(Importer.class);

    private String recordDirPath;
    private String configFile;
    private PageIDLinker idLinker;

    public Importer(String recordDirPath, String configFile) throws FileNotFoundException {
        this.recordDirPath = recordDirPath;
        File recordDir = new File(recordDirPath);
        if (!recordDir.exists() || !recordDir.isDirectory())
            throw new FileNotFoundException("The record directory doesn't exist or is not a directory.");

        this.configFile = configFile;
        this.idLinker = new PageIDLinker(true, configFile);
    }

    public void populateDB(String fileIndex) {

        int allDocCount = 0;
        int processedCount = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileIndex));
            String line;
            while ((line = br.readLine()) != null) {

                if (!line.endsWith("_ner")) continue;

                line = line.trim();
                String recPath = recordDirPath + File.separator + line;
                try {
                    FileInputStream in = new FileInputStream(recPath);
                    Record rec = deserializeRecordFromBytes(IOUtils.toByteArray(in));
                    if (rec.getLabelViews().containsKey("wikifier")) {
                        // testing
                        logger.info("Jackpot!");
                        return;
                    }
                }
                catch (Exception e) {
                    continue;
                }

                allDocCount++;
                if (allDocCount % 2000 == 0)
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
