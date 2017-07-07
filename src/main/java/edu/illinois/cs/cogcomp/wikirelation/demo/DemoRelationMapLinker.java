package edu.illinois.cs.cogcomp.wikirelation.demo;

import edu.illinois.cs.cogcomp.wikirelation.wikipedia.RelationMapLinker;

import java.util.Arrays;

/**
 * Created by squirrel on 6/27/17.
 */
public class DemoRelationMapLinker {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java DemoRelationMapLinker [config-file-path]");
            System.exit(1);
        }

        RelationMapLinker rml = new RelationMapLinker(true, args[0]);

        /* Get related page ids given a page id */
        System.out.println("Getting Related Page ids to \"Autism\" (curID = 25)\n");
        int[] candIds1 = rml.getRelatedCandidateIds(25);
        System.out.println(Arrays.toString(candIds1) + "\n");

        /* Get related page titles given a page id */
        System.out.println("Getting Related Page titles to \"Autism\" (curID = 25)\n");
        String[] candTitles1 = rml.getRelatedCandidateTitles(25);
        System.out.println(Arrays.toString(candTitles1) + "\n");

        /* Get related page ids given a page title */
        System.out.println("Getting Related Page ids to \"Champaign,_Illinois\"" + "\n");
        int[] candIds2 = rml.getRelatedCandidateIds("Champaign,_Illinois");
        System.out.println(Arrays.toString(candIds2) + "\n");

        /* Get related page titles given a page title */
        System.out.println("Getting Related Page titles to \"Champaign,_Illinois\"\n");
        String[] candTitles2 = rml.getRelatedCandidateTitles("Champaign,_Illinois");
        System.out.println(Arrays.toString(candTitles2) + "\n");

        System.out.println("Getting Top 10 Related Page ids to \"Champaign,_Illinois\"" + "\n");
        int[] candIds3 = rml.getTopKRelatedCandidateIds("Champaign,_Illinois", 0);
        System.out.println(Arrays.toString(candIds3) + "\n");
    }
}
