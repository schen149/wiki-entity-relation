package edu.illinois.cs.cogcomp.wikirelation.demo;

import edu.illinois.cs.cogcomp.wikirelation.core.PageIDLinker;

public class DemoPageIDLinker {
    public static void main(String[] args){
        if (args.length != 1) {
            System.err.println("Usage: java DemoPageIDLinker [config-file-path]");
            System.exit(1);
        }

        PageIDLinker linker = new PageIDLinker(true, args[0]);

        /* Get page title given the id of that page */
        String title = linker.getTitleFromID(290);
        System.out.println("Getting page title with curID = 290: \t" + title);

        /* Get page id given the title of the page */
        Integer id = linker.getIDFromTitle("Neurodevelopmental_disorder");
        System.out.println("Getting page id with title = \"Neurodevelopmental_disorder\": \t"+id);
    }
}
