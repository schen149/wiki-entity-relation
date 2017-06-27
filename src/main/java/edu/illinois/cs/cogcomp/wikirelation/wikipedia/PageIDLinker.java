package edu.illinois.cs.cogcomp.wikirelation.wikipedia;

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

    public PageIDLinker(boolean bReadOnly) {
        this.bReadOnly = bReadOnly;

        // TODO: call this in top level instead of here
        try {
            Configurator.setPropValues("config/sihaopc-english-170601.properties");
        }
        catch (IOException e) {
            logger.error("Failed to load config file: config/sihaopc-english-170601.properties");
        }

        loadDB();
    }

    private void loadDB() {

        String dbfile = Configurator.MAPDB_PATH + File.separator + "titlemap";

        if (bReadOnly) {
            db = DBMaker.fileDB(dbfile)
                    .fileChannelEnable()
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

    protected void put(Integer pageID, String title) {
        if (pageID != null) {
            this.title2id.put(title,pageID);
            this.id2title.put(pageID, title);
        }
    }

    public String getTitleFromID(int pageID) {
        return this.id2title.get(pageID);
    }

    public Integer getIDFromTitle(String title) {
        return this.title2id.get(title);
    }

}
