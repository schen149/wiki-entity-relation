package edu.illinois.cs.cogcomp.wikirelation.importer.geoname;

import edu.illinois.cs.cogcomp.wikirelation.core.FrequencyMapLinker;
import edu.illinois.cs.cogcomp.wikirelation.core.StringIDLinker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GeonameImporter {
    private static Logger logger = LoggerFactory.getLogger(GeonameImporter.class);
    private static int NUM_FIELDS = 19;

    private String inputFilePath;
    private String mapdbDir;

    private StringIDLinker idLinker;
    private FrequencyMapLinker freqMap;

    public GeonameImporter(String inputFilePath, String mapdbDir) {
        this.inputFilePath = inputFilePath;
        this.mapdbDir = mapdbDir;

        this.idLinker = new StringIDLinker(false, mapdbDir);
        this.freqMap = new FrequencyMapLinker(false, mapdbDir);
    }

    public void populateDB() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
            String line;
            int processed = 0;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\t");

                if (parts.length != NUM_FIELDS)
                    continue;

                int id = 0;
                try {
                    id = Integer.parseInt(parts[0]);
                }
                catch (NumberFormatException e){
                    continue;
                }

                Set<String> forms = new HashSet<>();
                forms.add(StringIDLinker.normalizeString(parts[1]));
                forms.add(StringIDLinker.normalizeString(parts[2]));
                int formCount = 2;
                /* No alternative forms */
                if (!parts[3].isEmpty()) {
                    for (String form : parts[3].split(",")) {
                        forms.add(StringIDLinker.normalizeString(form));
                        formCount++;
                    }
                }

                /* Update id linker one by one */
                for (String form: forms)
                    idLinker.put(form, id);

                /* Update frequency map */
                freqMap.put(id, formCount);

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
        freqMap.closeDB();
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java GeonameImporter [input-file] [mapdb-dir]");
            System.exit(1);
        }

        GeonameImporter gi = new GeonameImporter(args[0], args[1]);
        gi.populateDB();
    }

}