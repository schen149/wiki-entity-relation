package edu.illinois.cs.cogcomp.wikirelation.datastructure;

import edu.illinois.cs.cogcomp.wikirelation.util.DataTypeUtil;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe map wrapper that act as cache during Relation map import
 */
@Deprecated
public class MemoryCooccuranceMap {

    public ConcurrentHashMap<Long, Short> memCache;

    /* ConcurrentMap parameters */
    private static int TOTAL_CAPACITY = 5000000;
    private static int INITIAL_CAPACITY;
    private static float LOAD_FACTOR = 0.75f;
    private static int CONCURRENCY_LEVEL = 32;

    private int totalBatches;
    private int currentBatch;

    public MemoryCooccuranceMap(int totalBatches, int currentBatch){
        this.totalBatches = totalBatches;
        this.currentBatch = currentBatch;
        INITIAL_CAPACITY = TOTAL_CAPACITY / totalBatches;
        this.memCache = new ConcurrentHashMap<>(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL);
    }

    /**
     * record co-occurance of two page titles
     * @param pageId1
     * @param pageId2
     * */
    public void count(Integer pageId1, Integer pageId2) {
        if (pageId1 == null || pageId2 == null) return;

        if (pageId1 != pageId2) {
            __count(pageId1, pageId2);
            __count(pageId2, pageId1);
        }

    }

    private void __count(Integer pageId1, Integer pageId2) {
        if (pageId1 % totalBatches == currentBatch) {
            long key = DataTypeUtil.concatTwoIntToLong(pageId1, pageId2);

            if (this.memCache.containsKey(key))
                memCache.put(key, (short) (memCache.get(key) + 1));
            else
                memCache.put(key, (short) 1);
        }
    }


}
