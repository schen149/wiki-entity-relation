package edu.illinois.cs.cogcomp.wikirelation.datastructure;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe map wrapper that act as cache during Relation map import
 */
public class MemoryCooccuranceMap {

    public ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, Integer>> cache;

    /* ConcurrentMap parameters */
    private static int LEVEL1_INITIAL_CAPACITY = 5500000;  // Roughly the number of enwiki pages
    private static float LEVEL1_LOAD_FACTOR = 0.75f;
    private static int LEVEL1_CONCURRENCY_LEVEL = 32;
    private static int LEVEL2_INITIAL_CAPACITY = 500;     // Roughly the number of occurance peers of each page
    private static float LEVEL2_LOAD_FACTOR = 0.75f;
    private static int LEVEL2_CONCURRENCY_LEVEL = 2;      // Upper bound of number of threads accessing same map

    public MemoryCooccuranceMap(){
        this.cache = new ConcurrentHashMap<>(LEVEL1_INITIAL_CAPACITY, LEVEL1_LOAD_FACTOR, LEVEL1_CONCURRENCY_LEVEL);
    }

    /**
     * record co-occurance of two page titles
     * @param pageId1
     * @param pageId2
     * */
    public void count(Integer pageId1, Integer pageId2) {
        if (pageId1 == null || pageId2 == null) return;
        __count(pageId1, pageId2);
        __count(pageId2, pageId1);
    }

    private void __count(Integer pageId1, Integer pageId2) {
        if (!cache.containsKey(pageId1)){
            cache.put(pageId1, new ConcurrentHashMap<>(LEVEL2_INITIAL_CAPACITY, LEVEL2_LOAD_FACTOR, LEVEL2_CONCURRENCY_LEVEL));
        }
        ConcurrentHashMap<Integer, Integer> page1map = cache.get(pageId1);
        page1map.put(pageId2, page1map.get(pageId2) + 1);
    }

    public Set<Integer> keySet(){
        return this.cache.keySet();
    }

    public ConcurrentHashMap<Integer, Integer> get(Integer id){
        if (id == null) return null;
        return this.cache.get(id);
    }

}
