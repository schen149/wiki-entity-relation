package edu.illinois.cs.cogcomp.wikirelation.core;

import edu.illinois.cs.cogcomp.core.datastructures.Pair;
import edu.illinois.cs.cogcomp.wikirelation.util.DataTypeUtil;
import edu.illinois.cs.cogcomp.wikirelation.config.Configurator;
import org.mapdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Use Co-occurance metric to measure & cache related wikipedia titles
 *
 * Because there are ~78M co-occurances for 5M pages in current enwiki dump,
 * we need to choose the right datastructure to store the frequency map for max performance.
 * The way I did it is building a treemap by connecting the two (integer) page ids into a single
 * long as key and their co-occurance frequency as value. By using primitive types as key and values
 * we can save at least 4B for each entry (Object head length).
 *
 * Theoretical space needed: ~70M * (8B + 4B) * 2 / 0.75 = 2.24 GB (Assume perfect primitive map)
 * Actual Space of output: ~2.7GB
 *
 */
public class CooccuranceMapLinker {

    private static Logger logger = LoggerFactory.getLogger(CooccuranceMapLinker.class);

    private DB db;
    private PageIDLinker idLinker;
    private BTreeMap<Long, Short> coocuranceCount;

    private boolean bReadOnly;
    private static String defaultConfigFile = "config/cogcomp-english-170601.properties";

    public CooccuranceMapLinker(boolean bReadOnly) {
        this(bReadOnly, defaultConfigFile);
    }
    public CooccuranceMapLinker(boolean bReadOnly, String configFile) {
        this.bReadOnly = bReadOnly;
        this.idLinker = new PageIDLinker(true, configFile);

        // TODO: call this in top level instead of here
        try {
            Configurator.setPropValues(configFile);
        }
        catch (IOException e) {
            logger.error("Failed to load config file: " + configFile);
            System.exit(1);
        }
        loadDB();
    }

    private void loadDB(){

        String dbfile = Configurator.COOCCURANCE_MAPDB_PATH;

        if (bReadOnly) {
            db = DBMaker.fileDB(dbfile)
                    .fileMmapEnable()
                    .closeOnJvmShutdown()
                    .readOnly()
                    .make();
            coocuranceCount = db.treeMap("coocurance-treemap")
                    .keySerializer(Serializer.LONG)
                    .valueSerializer(Serializer.SHORT)
                    .open();
        }
        else {
            db = DBMaker.fileDB(dbfile)
                    .closeOnJvmShutdown()
                    .make();
            coocuranceCount = db.treeMap("coocurance-treemap")
                    .keySerializer(Serializer.LONG)
                    .valueSerializer(Serializer.SHORT)
                    .create();
        }
    }

    public void put(Integer pageId1, Integer pageId2) {
        if (pageId1 == null || pageId2 == null) return;
        __put(pageId1, pageId2);
        __put(pageId2, pageId1);
    }


    private void __put(int pageId1, int pageId2) {
        long key = DataTypeUtil.concatTwoIntToLong(pageId1, pageId2);
        if (coocuranceCount.containsKey(key))
            coocuranceCount.put(key, (short) (coocuranceCount.get(key) + 1));
        else
            coocuranceCount.put(key, (short) 1);
    }

    public void closeDB() {
        if (db != null && !db.isClosed()) {
            db.commit();
            db.close();
        }
    }

    /**
     * Get all related wikipedia pages' id
     * @param pageId page id (curId) of the wikipedia page link
     * @return array of all page ids related to the input wikipedia page
     * */
    private int[] getAllRelatedCandidateIds(Integer pageId) {
        if (pageId == null)
            return new int[]{};

        long upperBound = DataTypeUtil.concatTwoIntToLong(pageId+1, 0);
        long lowerBound = DataTypeUtil.concatTwoIntToLong(pageId, 0);

        // Co-occurance count of the given pageId
        Map<Long, Short> count = this.coocuranceCount.subMap(lowerBound, upperBound);
        List<Map.Entry<Long, Short>> sortedCount = new ArrayList<>(count.entrySet());
        sortedCount.sort((e1,e2) -> e2.getValue() - e1.getValue());

        int[] candIds = sortedCount.stream()
                .mapToLong(Map.Entry::getKey)
                .mapToInt(DataTypeUtil::getLower32bitFromLong)
                .toArray();

        return candIds;
    }
    
    /**
     * Get all related wikipedia page NE titles/links
     * @param pageId page id (curId) of the wikipedia page link
     * @return array of all page titles/links related to the input wikipedia page
     * */
    public String[] getAllRelatedNETitles(Integer pageId) {
        if (pageId == null)
            return new String[]{};

        int[] candIds = getAllRelatedCandidateIds(pageId);
        return Arrays.stream(candIds)
                .mapToObj(c -> idLinker.getTitleFromID(c))
                .filter(Objects::nonNull)
                .toArray(String[]::new);
    }

    /**
     * Get all related wikipedia page NE titles/links
     * @param title page title/link of the wikipedia page link
     * @return array of all page titles/links related to the input wikipedia page
     * */
    public String[] getAllRelatedNETitles(String title) {
        return getAllRelatedNETitles(this.idLinker.getIDFromTitle(title));
    }

    /**
     * Get top k related wikipedia page titles/links
     * @param pageId page id (curId) of the wikipedia page link.
     * @param k number of related candidates
     * @return array of all page titles/links related to the input wikipedia page
     * */
    public String[] getTopKRelatedNETitles(Integer pageId, int k) {
        if (pageId == null || k <= 0)
            return new String[]{};
        else
            return Arrays.copyOfRange(getAllRelatedNETitles(pageId), 0, k);
    }

    /**
     * Get top k related wikipedia page NE titles/links
     * @param title page title/link of the wikipedia page link.
     * @param k number of related candidates
     * @return array of all page titles/links related to the input wikipedia page
     * */
    public String[] getTopKRelatedNETitles(String title, int k) {
        if (title == null || k <= 0)
            return new String[]{};
        else
            return Arrays.copyOfRange(getAllRelatedNETitles(title), 0, k);
    }

    private int[] getAllRelatedCandidateIds(int[] pageIds) {
        if (pageIds == null)
            return new int[]{};

        // Map<(id of the related page), Pair<(# pages it relates to), (freq count)>
        Map<Integer, Pair<Integer, Integer>> cands = new HashMap<>();

        for (Integer pageId : pageIds) {
            if (pageId == null) continue;

            long upperBound = DataTypeUtil.concatTwoIntToLong(pageId + 1, 0);
            long lowerBound = DataTypeUtil.concatTwoIntToLong(pageId, 0);

            Map<Long, Short> count = coocuranceCount.subMap(lowerBound, upperBound);

            for (Map.Entry<Long, Short> curCand : count.entrySet()) {
                int candId = DataTypeUtil.getLower32bitFromLong(curCand.getKey());
                if (!cands.containsKey(candId))
                    cands.put(candId, new Pair<>(1, (int) curCand.getValue()));
                else {
                    Pair<Integer, Integer> oldVal = cands.get(candId);
                    Pair<Integer, Integer> newVal = new Pair<>(oldVal.getFirst() + 1, Math.max(oldVal.getSecond(),curCand.getValue()));
                    cands.put(candId, newVal);
                }
            }
        }
        Set<Integer> inputPageIds = Arrays.stream(pageIds).boxed().collect(Collectors.toSet());
        List<Map.Entry<Integer, Pair<Integer, Integer>>> sortedCands = new ArrayList<>(cands.entrySet());

        int[] candIds = sortedCands.stream()
                .filter(c -> c.getValue().getFirst() == pageIds.length)
                .sorted((c1, c2) -> {
                    int relatedPageNum1 = c1.getValue().getFirst();
                    int relatedPageNum2 = c2.getValue().getFirst();
                    if (relatedPageNum1 != relatedPageNum2)
                        return relatedPageNum2 - relatedPageNum1;
                    else
                        return c2.getValue().getSecond() - c1.getValue().getSecond();
                })
                .mapToInt(Map.Entry::getKey)
                .filter(c -> !inputPageIds.contains(c))
                .toArray();

        return candIds;
    }

    private int[] getAllRelatedCandidateIds(String[] titles) {
        return getAllRelatedCandidateIds(Arrays.stream(titles)
                .map(t -> idLinker.getIDFromTitle(t))
                .filter(Objects::nonNull)
                .mapToInt(i->i)
                .toArray());
    }

    public String[] getTopKRelatedNETitles(int[] pageIds, int k) {
        String[] allCands = Arrays.stream(getAllRelatedCandidateIds(pageIds))
                .mapToObj(c -> idLinker.getTitleFromID(c))
                .filter(Objects::nonNull)
                .toArray(String[]::new);
        return Arrays.copyOfRange(allCands,0, k);
    }

    public String[] getTopKRelatedNETitles(String[] titles, int k) {
        String[] allCands = Arrays.stream(getAllRelatedCandidateIds(titles))
                .mapToObj(c -> idLinker.getTitleFromID(c))
                .filter(Objects::nonNull)
                .toArray(String[]::new);
        return Arrays.copyOfRange(allCands,0, k);
    }
}
