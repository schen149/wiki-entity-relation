package edu.illinois.cs.cogcomp.wikirelation.demo;

import edu.illinois.cs.cogcomp.wikirelation.wikipedia.PageIDLinker;

public class DemoPageIDLinker {
    public static void main(String[] args){
        PageIDLinker linker = new PageIDLinker(true);
        System.out.println("Getting page title with curID = 290...");
        System.out.println(linker.getTitleFromID(290));
        System.out.println("Getting page id with title = \"Neurodevelopmental_disorder\"...");
        System.out.println(linker.getIDFromTitle("Neurodevelopmental_disorder"));
    }
}
