package edu.illinois.cs.cogcomp.wikirelation;

import edu.illinois.cs.cogcomp.wikirelation.wikipedia.RelationMapLinker;

public class Timer {
    public static void main(String[] args){
        long startTime = System.currentTimeMillis();

        RelationMapLinker linker = new RelationMapLinker(true, args[0]);
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("Overhead: " + endTime);


        startTime = System.currentTimeMillis();
        String[] set1 = new String[]{"Champaign,_Illinois", "Urbana,_Illinois"};
        linker.getTopKRelatedNETitles(set1,10);
        endTime = System.currentTimeMillis() - startTime;
        System.out.println("Time Elasped: " + endTime);


        String[] set2 = new String[]{"Angela_Merkel","Barack_Obama","Donald_Trump"};
        startTime = System.currentTimeMillis();
        linker.getTopKRelatedNETitles(set2,10);
        endTime = System.currentTimeMillis() - startTime;
        System.out.println("Time Elasped: " + endTime);

        String[] set3 = new String[]{"Angela_Merkel","Barack_Obama","Donald_Trump"};
        startTime = System.currentTimeMillis();
        linker.getTopKRelatedNETitles(set3,10);
        endTime = System.currentTimeMillis() - startTime;
        System.out.println("Time Elasped: " + endTime);

        String[] set4 = new String[]{"Autism","A","B"};
        startTime = System.currentTimeMillis();
        linker.getTopKRelatedNETitles(set4,10);
        endTime = System.currentTimeMillis() - startTime;
        System.out.println("Time Elasped: " + endTime);
    }
}
