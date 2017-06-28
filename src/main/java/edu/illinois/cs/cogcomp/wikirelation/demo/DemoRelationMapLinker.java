package edu.illinois.cs.cogcomp.wikirelation.demo;

import edu.illinois.cs.cogcomp.wikirelation.wikipedia.RelationMapLinker;

import java.util.Arrays;

/**
 * Created by squirrel on 6/27/17.
 */
public class DemoRelationMapLinker {
    public static void main(String[] args) {
        RelationMapLinker rml = new RelationMapLinker(true);
        System.out.println("Getting Related Page ids to \"Autism\" (curID = 25)\n");
        System.out.println(Arrays.toString(rml.getRelatedCandidateIds(25)) + "\n");
        System.out.println("Getting Related Page titles to \"Autism\" (curID = 25)\n");
        System.out.println(Arrays.toString(rml.getRelatedCandidateTitles(25)));
        System.out.println("Getting Related Page ids to \"Champaign,_Illinois\"" + "\n");
        System.out.println(Arrays.toString(rml.getRelatedCandidateIds("Champaign,_Illinois")) + "\n");
        System.out.println("Getting Related Page titles to \"Champaign,_Illinois\"\n");
        System.out.println(Arrays.toString(rml.getRelatedCandidateTitles("Champaign,_Illinois")) + "\n");
    }
}
