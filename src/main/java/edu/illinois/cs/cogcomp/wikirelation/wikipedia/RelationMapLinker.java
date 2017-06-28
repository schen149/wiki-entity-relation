package edu.illinois.cs.cogcomp.wikirelation.wikipedia;

import edu.illinois.cs.cogcomp.wikirelation.util.DataTypeUtil;
import edu.illinois.cs.cogcomp.wikirelation.config.Configurator;
import org.mapdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Use Co-occurance metric to measure & cache related wikipedia titles
 */
public class RelationMapLinker {

    private static Logger logger = LoggerFactory.getLogger(RelationMapLinker.class);

    private DB db;
    private PageIDLinker idLinker;
    private BTreeMap<Long, Short> coocuranceCount;

    private boolean bReadOnly;
    private static String defaultConfigFile = "config/cogcomp-english-170601.properties";

    public RelationMapLinker(boolean bReadOnly) {
        this(bReadOnly, defaultConfigFile);
    }
    public RelationMapLinker(boolean bReadOnly, String configFile) {
        this.bReadOnly = bReadOnly;
        this.idLinker = new PageIDLinker(true);

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

        String dbfile = Configurator.MAPDB_PATH + File.separator + "coocurancemap";

        if (bReadOnly) {
            db = DBMaker.fileDB(dbfile)
                    .fileChannelEnable()
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

    public int[] getRelatedCandidateIds(Integer pageId) {
        if (pageId == null)
            return new int[]{};

        long upperBound = DataTypeUtil.concatTwoIntToLong(pageId+1, 0);
        long lowerBount = DataTypeUtil.concatTwoIntToLong(pageId, 0);

        // Co-occurance count of the given pageId
        Map<Long, Short> count = this.coocuranceCount.subMap(lowerBount, upperBound);
        List<Map.Entry<Long, Short>> sortedCount = new ArrayList<>(count.entrySet());
        sortedCount.sort((e1,e2) -> e2.getValue() - e1.getValue());

        int[] candIds = sortedCount.stream()
                .mapToLong(Map.Entry::getKey)
                .mapToInt(DataTypeUtil::getLower32bitFromLong)
                .toArray();

        return candIds;
    }

    public int[] getRelatedCandidateIds(String title) {
        return getRelatedCandidateIds(this.idLinker.getIDFromTitle(title));
    }

    public String[] getRelatedCandidateTitles(Integer pageId) {
        int[] candIds = getRelatedCandidateIds(pageId);
        return Arrays.stream(candIds)
                .mapToObj(c -> idLinker.getTitleFromID(c))
                .toArray(String[]::new);
    }

    public String[] getRelatedCandidateTitles(String title) {
        return getRelatedCandidateTitles(this.idLinker.getIDFromTitle(title));
    }

}
