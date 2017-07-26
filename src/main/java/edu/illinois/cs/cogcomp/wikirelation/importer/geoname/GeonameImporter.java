package edu.illinois.cs.cogcomp.wikirelation.importer.geoname;

import edu.illinois.cs.cogcomp.wikirelation.core.FrequencyMapLinker;
import edu.illinois.cs.cogcomp.wikirelation.core.StringIDLinker;
import edu.illinois.cs.cogcomp.wikirelation.util.DataTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    public void populateDB(Charset charset) {

        if (!charset.equals(StandardCharsets.UTF_8) && !charset.equals(StandardCharsets.US_ASCII)) {
            logger.error("Charset Not Supported. Use either utf-8 or ascii.");
            return;
        }
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

                if (charset.equals(StandardCharsets.US_ASCII)) {
                    String form = DataTypeUtil.utf8ToAscii(parts[1]);
                    addToSet(forms, form);
                    form = DataTypeUtil.utf8ToAscii(parts[2]);
                    addToSet(forms, form);
                    if (!parts[3].isEmpty()) {
                        for (String f : parts[3].split(",")) {
                            form = DataTypeUtil.utf8ToAscii(f);
                            addToSet(forms, form);
                        }
                    }
                }
                else {
                    String form = DataTypeUtil.normalizeString(parts[1]);
                    addToSet(forms, form);
                    form = DataTypeUtil.normalizeString(parts[2]);
                    addToSet(forms, form);
                    if (!parts[3].isEmpty()) {
                        for (String f : parts[3].split(",")) {
                            form = DataTypeUtil.normalizeString(f);
                            addToSet(forms, form);
                        }
                    }
                }

                /* Update id linker one by one */
                for (String f: forms)
                    idLinker.put(f, id);

                /* Update frequency map */
                freqMap.put(id, forms.size());

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

    private void addToSet(Set<String> set, String str) {
        if (str != null)
            set.add(str);
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java GeonameImporter [input-file] [mapdb-dir]");
            System.exit(1);
        }

        GeonameImporter gi = new GeonameImporter(args[0], args[1]);
        gi.populateDB(StandardCharsets.UTF_8);
    }

}
