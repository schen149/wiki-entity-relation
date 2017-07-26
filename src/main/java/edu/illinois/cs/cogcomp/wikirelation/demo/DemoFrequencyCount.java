package edu.illinois.cs.cogcomp.wikirelation.demo;

import edu.illinois.cs.cogcomp.wikirelation.core.FrequencyMapLinker;
import edu.illinois.cs.cogcomp.wikirelation.core.PageIDLinker;

public class DemoFrequencyCount {
    public static void main(String[] args){
        if (args.length != 1) {
            System.err.println("Usage: java DemoPageIDLinker [mapdb-file-path] [geoname]");
            System.exit(1);
        }

        FrequencyMapLinker linker = new FrequencyMapLinker(true, args[0]);

        /* Get page title given the id of that page */
        int count = linker.getFrequency(args[1]);
        System.out.println("Frequency of " + args[1] + " :\t" + count);
    }
}
