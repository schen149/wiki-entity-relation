package edu.illinois.cs.cogcomp.wikirelation.importer.geoname;

import edu.illinois.cs.cogcomp.wikirelation.core.StringIDLinker;
import edu.illinois.cs.cogcomp.wikirelation.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

public class IDImporter {
    private static Logger logger = LoggerFactory.getLogger(IDImporter.class);
    private static int NUM_FIELDS = 19;

    private final ThreadPoolExecutor pool; 
    private String inputFilePath;

    private StringIDLinker idLinker;

    public IDImporter(String inputFilePath, String mapdbDir) {
        this.inputFilePath = inputFilePath;
        this.pool = CommonUtil.getBoundedThreadPool(20);

        this.idLinker = new StringIDLinker(false, mapdbDir);
    }

    public void populateDB() {
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
            String line;
            int processed = 0;
            while ((line = br.readLine()) != null) {
                
                pool.execute(new IDImporterWorker(line, idLinker));
                
                processed++;

                if (processed % 10000 == 0) {
                    logger.info("Processed: " + processed);
                }
            }
            br.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        idLinker.closeDB();
    }



    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java IDImporter [input-file] [mapdb-dir]");
            System.exit(1);
        }

        IDImporter gi = new IDImporter(args[0], args[1]);
        gi.populateDB();
    }

}
