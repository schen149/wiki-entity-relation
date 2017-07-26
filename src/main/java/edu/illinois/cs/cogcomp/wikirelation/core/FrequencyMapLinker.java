package edu.illinois.cs.cogcomp.wikirelation.core;

import org.mapdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Record Frequency of strings
 */
public class FrequencyMapLinker {

    private static Logger logger = LoggerFactory.getLogger(FrequencyMapLinker.class);

    private DB db;
    private StringIDLinker idLinker;
    private HTreeMap<Integer, Integer> freqCount;

    private boolean bReadOnly;

    public FrequencyMapLinker(boolean bReadOnly, String mapdbDir) {
        this.bReadOnly = bReadOnly;

        if (bReadOnly)
            this.idLinker = new StringIDLinker(true, mapdbDir);
        else
            this.idLinker = null;

        loadDB(mapdbDir);
    }

    private void loadDB(String mapdbDir){

        String dbfile = mapdbDir + File.separator + "geoname-frequency";

        if (bReadOnly) {
            db = DBMaker.fileDB(dbfile)
                    .fileChannelEnable()
                    .closeOnJvmShutdown()
                    .readOnly()
                    .make();
            freqCount = db.hashMap("frequencymap")
                    .layout(16,32,4)
                    .keySerializer(Serializer.INTEGER)
                    .valueSerializer(Serializer.INTEGER)
                    .open();
        }
        else {
            db = DBMaker.fileDB(dbfile)
                    .closeOnJvmShutdown()
                    .make();
            freqCount = db.hashMap("frequencymap")
                    .layout(16,32,4)
                    .keySerializer(Serializer.INTEGER)
                    .valueSerializer(Serializer.INTEGER)
                    .create();
        }
    }

    public void put(Integer id, Integer count) {
        if (id == null || count == null) return;
        if (freqCount.containsKey(id)) {
            Integer c =  freqCount.get(id);
            if (c == null)
                freqCount.put(id, count);
            else
                freqCount.put(id, c + count);
        }
        else
            freqCount.put(id, count);
    }

    public void closeDB() {
        if (db != null && !db.isClosed()) {
            db.commit();
            db.close();
        }
    }

    /**
     * Get the number of appearences of a string
     * @param name name of the string
     */
    public int getFrequency(String name) {
        Integer id = idLinker.getIDFromString(name);
        return this.getFrequency(id);
    }

    /**
     * Get the number of appearences of a string
     * @param id id of the string
     */
    public int getFrequency(Integer id) {
        if (id == null) return 0;
        if (freqCount.containsKey(id)) {
            Integer count = freqCount.get(id);
            if (count == null)
                return 0;
            else
                return count;
        }
        else
            return 0;
    }
}
