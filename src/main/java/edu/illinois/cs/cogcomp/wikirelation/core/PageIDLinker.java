package edu.illinois.cs.cogcomp.wikirelation.core;

import edu.illinois.cs.cogcomp.wikirelation.config.Configurator;
import org.mapdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * A bimap from wikipedia page title to pageid (curID)
 */
public class PageIDLinker {

    private static Logger logger = LoggerFactory.getLogger(PageIDLinker.class);
    private DB db;
    private boolean bReadOnly;
    public boolean bDBopen = false;

    private HTreeMap<String, Integer> title2id;
    private HTreeMap<Integer, String> id2title;

    private static String defaultConfigFile = "config/cogcomp-english-170601.properties";

    public PageIDLinker(boolean bReadOnly) {
        this(bReadOnly, defaultConfigFile);
    }

    public PageIDLinker(boolean bReadOnly, String configFile) {
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

        String dbfile = Configurator.PAGE_ID_MAPDB_PATH;

        if (bReadOnly) {
            db = DBMaker.fileDB(dbfile)
                    .fileMmapEnable()
                    .closeOnJvmShutdown()
                    .readOnly()
                    .make();
            title2id = db.hashMap("title2id")
                    .keySerializer(Serializer.STRING)
                    .valueSerializer(Serializer.INTEGER)
                    .open();
            id2title = db.hashMap("id2title")
                    .keySerializer(Serializer.INTEGER)
                    .valueSerializer(Serializer.STRING)
                    .open();
        }
        else {
            db = DBMaker.fileDB(dbfile)
                    .closeOnJvmShutdown()
                    .make();
            title2id = db.hashMap("title2id")
                    .keySerializer(Serializer.STRING)
                    .valueSerializer(Serializer.INTEGER)
                    .create();
            id2title = db.hashMap("id2title")
                    .keySerializer(Serializer.INTEGER)
                    .valueSerializer(Serializer.STRING)
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

    public void put(Integer pageID, String title) {
        if (pageID != null) {
            this.title2id.put(title,pageID);
            this.id2title.put(pageID, title);
        }
    }

    public String getTitleFromID(Integer pageID) {
        if (pageID == null)
            return null;
        return this.id2title.get(pageID);
    }

    public Integer getIDFromTitle(String title) {
        if (title == null)
            return null;
        title = title.trim();
        return this.title2id.get(title);
    }

    public int getIDCount() {
        return this.id2title.getSize();
    }
}
