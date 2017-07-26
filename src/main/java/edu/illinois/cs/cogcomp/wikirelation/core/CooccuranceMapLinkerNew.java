//package edu.illinois.cs.cogcomp.wikirelation.core;
//
//import edu.illinois.cs.cogcomp.core.datastructures.Pair;
//import edu.illinois.cs.cogcomp.wikirelation.config.Configurator;
//import edu.illinois.cs.cogcomp.wikirelation.util.DataTypeUtil;
//import org.mapdb.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * Use Co-occurance metric to measure & cache related wikipedia titles
// *
// * Switch to hashmap + pre-sorted map for better import performance
// */
//public class CooccuranceMapLinkerNew {
//
//    private static Logger logger = LoggerFactory.getLogger(CooccuranceMapLinkerNew.class);
//
//    private DB db;
//    private PageIDLinker idLinker;
//    private HTreeMap<Integer, long[]> coocuranceCount;
//
//    private boolean bReadOnly;
//    private static String defaultConfigFile = "config/cogcomp-english-170601.properties";
//
//    public CooccuranceMapLinkerNew(boolean bReadOnly) {
//        this(bReadOnly, defaultConfigFile);
//    }
//    public CooccuranceMapLinkerNew(boolean bReadOnly, String configFile) {
//        this.bReadOnly = bReadOnly;
//        this.idLinker = new PageIDLinker(true, configFile);
//
//        // TODO: call this in top level instead of here
//        try {
//            Configurator.setPropValues(configFile);
//        }
//        catch (IOException e) {
//            logger.error("Failed to load config file: " + configFile);
//            System.exit(1);
//        }
//        loadDB();
//    }
//
//    private void loadDB(){
//
//        String dbfile = Configurator.COOCCURANCE_MAPDB_PATH;
//
//        if (bReadOnly) {
//            db = DBMaker.fileDB(dbfile)
//                    .fileChannelEnable()
//                    .closeOnJvmShutdown()
//                    .readOnly()
//                    .make();
//            coocuranceCount = db.hashMap("coocurance-map")
//                    .layout(16, 32, 4)
//                    .keySerializer(Serializer.INTEGER)
//                    .valueSerializer(Serializer.LONG_ARRAY)
//                    .open();
//        }
//        else {
//            db = DBMaker.fileDB(dbfile)
//                    .closeOnJvmShutdown()
//                    .make();
//            coocuranceCount = db.hashMap("coocurance-map")
//                    .layout(16, 32, 4)
//                    .keySerializer(Serializer.INTEGER)
//                    .valueSerializer(Serializer.LONG_ARRAY)
//                    .create();
//        }
//    }
//
//    public void put(Integer pageId1, Integer pageId2) {
//        if (pageId1 == null || pageId2 == null) return;
//        if (pageId1.equals(pageId2)) return;
//        __put(pageId1, pageId2, 1);
//        __put(pageId2, pageId1, 1);
//    }
//
//    public void put(Integer pageId1, Integer pageId2, int count) {
//        if (pageId1 == null || pageId2 == null) return;
//        if (pageId1.equals(pageId2)) return;
//        __put(pageId1, pageId2, count);
//        __put(pageId2, pageId1, count);
//    }
//
//    private void __put(int pageId1, int pageId2, int count) {
//        long key = DataTypeUtil.concatTwoIntToLong(pageId1, pageId2);
//        if (coocuranceCount.containsKey(key))
//            coocuranceCount.put(key, coocuranceCount.get(key) + count);
//        else
//            coocuranceCount.put(key, 1);
//    }
//
//    public void closeDB() {
//        if (db != null && !db.isClosed()) {
//            db.commit();
//            db.close();
//        }
//    }
//
//    /**
//     * Get all related wikipedia pages' id
//     * @param pageId page id (curId) of the wikipedia page link
//     * @return array of all page ids related to the input wikipedia page
//     * */
//    private int[] getAllRelatedCandidateIds(Integer pageId) {
//        if (pageId == null)
//            return new int[]{};
//
//        return Arrays.stream(this.coocuranceCount.get(pageId))
//                .mapToInt(DataTypeUtil::getHigher32bitFromLong)
//                .toArray();
//    }
//
//    /**
//     * Get all related wikipedia page NE titles/links
//     * @param pageId page id (curId) of the wikipedia page link
//     * @return array of all page titles/links related to the input wikipedia page
//     * */
//    public String[] getAllRelatedNETitles(Integer pageId) {
//        if (pageId == null)
//            return new String[]{};
//
//        int[] candIds = getAllRelatedCandidateIds(pageId);
//        return Arrays.stream(candIds)
//                .mapToObj(c -> idLinker.getTitleFromID(c))
//                .filter(Objects::nonNull)
//                .toArray(String[]::new);
//    }
//
//    /**
//     * Get all related wikipedia page NE titles/links
//     * @param title page title/link of the wikipedia page link
//     * @return array of all page titles/links related to the input wikipedia page
//     * */
//    public String[] getAllRelatedNETitles(String title) {
//        return getAllRelatedNETitles(this.idLinker.getIDFromTitle(title));
//    }
//
//    /**
//     * Get top k related wikipedia page titles/links
//     * @param pageId page id (curId) of the wikipedia page link.
//     * @param k number of related candidates
//     * @return array of all page titles/links related to the input wikipedia page
//     * */
//    public String[] getTopKRelatedNETitles(Integer pageId, int k) {
//        if (pageId == null || k <= 0)
//            return new String[]{};
//
//        allCands
//        long[] cands = Arrays.copyOfRange(this.coocuranceCount.get(pageId), 0, k);
//
//        return Arrays.copyOfRange(getAllRelatedNETitles(pageId), 0, k);
//    }
//
//    /**
//     * Get top k related wikipedia page NE titles/links
//     * @param title page title/link of the wikipedia page link.
//     * @param k number of related candidates
//     * @return array of all page titles/links related to the input wikipedia page
//     * */
//    public String[] getTopKRelatedNETitles(String title, int k) {
//        if (title == null || k <= 0)
//            return new String[]{};
//        else {
//            String[] cands = getAllRelatedNETitles(title);
//            if (cands.length <= k)
//                return cands;
//            else
//                return Arrays.copyOfRange(cands, 0, k);
//        }
//    }
//
//    private int[] getAllRelatedCandidateIds(int[] pageIds) {
//        if (pageIds == null)
//            return new int[]{};
//
//        // Map<(id of the related page), Pair<(# pages it relates to), (freq count)>
//        Map<Integer, Pair<Integer, Integer>> cands = new HashMap<>();
//
//        for (Integer pageId : pageIds) {
//            if (pageId == null) continue;
//
//            long upperBound = DataTypeUtil.concatTwoIntToLong(pageId + 1, 0);
//            long lowerBound = DataTypeUtil.concatTwoIntToLong(pageId, 0);
//
//            Map<Long, Integer> count = coocuranceCount.subMap(lowerBound, upperBound);
//
//            for (Map.Entry<Long, Integer> curCand : count.entrySet()) {
//                int candId = DataTypeUtil.getLower32bitFromLong(curCand.getKey());
//                if (!cands.containsKey(candId))
//                    cands.put(candId, new Pair<>(1, curCand.getValue()));
//                else {
//                    Pair<Integer, Integer> oldVal = cands.get(candId);
//                    Pair<Integer, Integer> newVal = new Pair<>( oldVal.getFirst() + 1, Math.max(oldVal.getSecond(),curCand.getValue()));
//                    cands.put(candId, newVal);
//                }
//            }
//        }
//        Set<Integer> inputPageIds = Arrays.stream(pageIds).boxed().collect(Collectors.toSet());
//        List<Map.Entry<Integer, Pair<Integer, Integer>>> sortedCands = new ArrayList<>(cands.entrySet());
//
//        int[] candIds = sortedCands.stream()
//                .filter(c -> c.getValue().getFirst() == pageIds.length)
//                .sorted((c1, c2) -> {
//                    int relatedPageNum1 = c1.getValue().getFirst();
//                    int relatedPageNum2 = c2.getValue().getFirst();
//                    if (relatedPageNum1 != relatedPageNum2)
//                        return relatedPageNum2 - relatedPageNum1;
//                    else
//                        return c2.getValue().getSecond() - c1.getValue().getSecond();
//                })
//                .mapToInt(Map.Entry::getKey)
//                .filter(c -> !inputPageIds.contains(c))
//                .toArray();
//
//        return candIds;
//    }
//
//    private int[] getAllRelatedCandidateIds(String[] titles) {
//        return getAllRelatedCandidateIds(Arrays.stream(titles)
//                .map(t -> idLinker.getIDFromTitle(t))
//                .filter(Objects::nonNull)
//                .mapToInt(i->i)
//                .toArray());
//    }
//
//    public String[] getTopKRelatedNETitles(int[] pageIds, int k) {
//        String[] allCands = Arrays.stream(getAllRelatedCandidateIds(pageIds))
//                .mapToObj(c -> idLinker.getTitleFromID(c))
//                .filter(Objects::nonNull)
//                .toArray(String[]::new);
//
//        if (allCands.length <= k)
//            return allCands;
//        else
//            return Arrays.copyOfRange(allCands, 0, k);
//    }
//
//    public String[] getTopKRelatedNETitles(String[] titles, int k) {
//        String[] allCands = Arrays.stream(getAllRelatedCandidateIds(titles))
//                .mapToObj(c -> idLinker.getTitleFromID(c))
//                .filter(Objects::nonNull)
//                .toArray(String[]::new);
//
//        if (allCands.length <= k)
//            return allCands;
//        else
//            return Arrays.copyOfRange(allCands, 0, k);
//    }
//}
