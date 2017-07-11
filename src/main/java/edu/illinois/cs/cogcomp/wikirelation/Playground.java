package edu.illinois.cs.cogcomp.wikirelation;

import edu.illinois.cs.cogcomp.wikirelation.wikipedia.RelationMapLinker;
import org.mapdb.*;

import java.util.*;

/**
 * Created by squirrel on 6/24/17.
 */
public class Playground {
    public static void main(String[] args){
        long startTime = System.currentTimeMillis();

        RelationMapLinker linker = new RelationMapLinker(true, args[0]);
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("Overhead: " + endTime);


        startTime = System.currentTimeMillis();
        String[] set1 = new String[]{"Champaign,_Illinois", "Urbana,_Illinois"};
        linker.getTopKRelatedCandidateTitles(set1,10);
        endTime = System.currentTimeMillis() - startTime;
        System.out.println("Time Elasped: " + endTime);


        String[] set2 = new String[]{"Angela_Merkel","Barack_Obama","Donald_Trump"};
        startTime = System.currentTimeMillis();
        linker.getTopKRelatedCandidateTitles(set2,10);
        endTime = System.currentTimeMillis() - startTime;
        System.out.println("Time Elasped: " + endTime);

        String[] set3 = new String[]{"Angela_Merkel","Barack_Obama","Donald_Trump"};
        startTime = System.currentTimeMillis();
        linker.getTopKRelatedCandidateTitles(set3,10);
        endTime = System.currentTimeMillis() - startTime;
        System.out.println("Time Elasped: " + endTime);

        String[] set4 = new String[]{"Autism","A","B"};
        startTime = System.currentTimeMillis();
        linker.getTopKRelatedCandidateTitles(set4,10);
        endTime = System.currentTimeMillis() - startTime;
        System.out.println("Time Elasped: " + endTime);


//        testMapdb();
    }
//
//    private static int[] sortKeyByValue(Map<Integer, Integer> map) {
//        List<Map.Entry<Integer, Integer>> candsCounts = new ArrayList<>(map.entrySet());
//
//        candsCounts.sort((c1, c2) -> c2.getValue() - c1.getValue());
//
//        return candsCounts.stream()
//                .mapToInt(Map.Entry::getKey)
//                .toArray();
//    }
//
//    private static void testMapdb() {
//        DB db = DBMaker.fileDB("/media/evo/bkup/tmp/test")
//                .closeOnJvmShutdown()
//                .make();
//        HTreeMap<Integer, Integer> map = db.hashMap("map")
//                .keySerializer(Serializer.INTEGER)
//                .valueSerializer(Serializer.INTEGER)
//                .create();
//
//        long startTime = System.currentTimeMillis();
//        List<Integer> someInt = Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17);
//
//        someInt.stream()
//                .forEach(i -> map.put(i, 1));
//
//        long elasped = System.currentTimeMillis() - startTime;
//        System.out.println(elasped);
//        System.out.println(map.get(2));
//
//        db.commit();
//        db.close();
//    }
//
//    private static int putItem(HTreeMap<Integer, Integer> map, int n1, int n2) {
//        map.put(n1, n2);
//        return 0;
//    }

}
