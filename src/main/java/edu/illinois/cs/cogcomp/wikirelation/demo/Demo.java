package edu.illinois.cs.cogcomp.wikirelation.demo;

import edu.illinois.cs.cogcomp.wikirelation.core.CooccurrenceMapLinker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Demo {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java Demo [config-file-path] [input-file] [Number-of-entities]");
            System.err.println("[config-file-path]:\tconfig file should be located in <project-dir>/config/");
            System.err.println("[input-file]:\t each line of the input file should contain a wikipedia page title/link");
            System.err.println("[Number-of-entities]:\t Top k related entities");
        }

        String configFilePath = args[0];
        String pageTitlesInputFilePath = args[1];
        int k = Integer.parseInt(args[2]);

        CooccurrenceMapLinker linker = new CooccurrenceMapLinker(true,configFilePath);

        try {
            List<String> titles = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(pageTitlesInputFilePath));
            String line;
            while ((line = br.readLine()) != null)
                titles.add(line.trim());

            String[] candIds = linker.getTopKRelatedNETitles(titles.toArray(new String[titles.size()]), k);

            System.out.println("Retrieving Top " + k + " Related NE titles...");
            for (String cand: candIds)
                System.out.println(cand);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
