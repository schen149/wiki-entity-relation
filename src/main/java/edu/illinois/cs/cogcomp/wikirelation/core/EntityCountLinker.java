package edu.illinois.cs.cogcomp.wikirelation.core;

import edu.illinois.cs.cogcomp.wikirelation.config.Configurator;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * A bimap from wikipedia page title to pageid (curID)
 */
public class EntityCountLinker {

    private static Logger logger = LoggerFactory.getLogger(EntityCountLinker.class);
    private DB db;
    private boolean bReadOnly;
    public boolean bDBopen = false;

    private HTreeMap<Integer, Integer> entityCount;

    private static String defaultConfigFile = "config/cogcomp-english-170601.properties";

    public EntityCountLinker(boolean bReadOnly) {
        this(bReadOnly, defaultConfigFile);
    }

    public EntityCountLinker(boolean bReadOnly, String configFile) {
        this.bReadOnly = bReadOnly;

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

    private void loadDB() {

        String dbfile = Configurator.MAPDB_PATH + File.separator + "wordcount";

        if (bReadOnly) {
            db = DBMaker.fileDB(dbfile)
                    .fileChannelEnable()
                    .closeOnJvmShutdown()
                    .readOnly()
                    .make();
            entityCount = db.hashMap("wordCount")
                    .keySerializer(Serializer.INTEGER)
                    .valueSerializer(Serializer.INTEGER)
                    .open();

        }
        else {
            db = DBMaker.fileDB(dbfile)
                    .closeOnJvmShutdown()
                    .make();
            entityCount = db.hashMap("wordCount")
                    .keySerializer(Serializer.INTEGER)
                    .valueSerializer(Serializer.INTEGER)
                    .create();
        }
        this.bDBopen = true;
    }

    public void closeDB() {
        if (db != null && !db.isClosed()) {
            db.commit();
            db.close();
        }
        this.bDBopen = false;
    }

    public void put(Integer pageID, int entityCount) {
        if (pageID != null)
            this.entityCount.put(pageID, entityCount);
    }

    public int getEntityCount(int pageID) {
        return this.entityCount.get(pageID);
    }
}
