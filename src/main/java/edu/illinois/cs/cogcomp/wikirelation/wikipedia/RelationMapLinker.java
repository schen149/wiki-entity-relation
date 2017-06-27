package edu.illinois.cs.cogcomp.wikirelation.wikipedia;

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
 * Use Co-occurance metric to measure & cache related wikipedia titles
 * Currently I'm using a unmodifiable structure to store co-occurance map.
 * Performance-wise speaking this is the way to go.
 * But I believe there should be a better solution than this.
 *
 * TODO: think of a better datastructure to store co-occurance map
 */
public class RelationMapLinker {

    private static Logger logger = LoggerFactory.getLogger(RelationMapLinker.class);

    private DB db;
    private PageIDLinker idLinker;

    private boolean bReadOnly;
    private boolean bDBopen;

    private HTreeMap<Integer, int[]> coocuranceGraph;

    public RelationMapLinker(boolean bReadOnly) {
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

    private void loadDB(){

        String dbfile = Configurator.MAPDB_PATH + File.separator + "coocuranceGraph";

        if (bReadOnly) {
            db = DBMaker.fileDB(dbfile)
                    .fileChannelEnable()
                    .closeOnJvmShutdown()
                    .readOnly()
                    .make();
            coocuranceGraph = db.hashMap("coocuranceGraph")
                    .keySerializer(Serializer.INTEGER)
                    .valueSerializer(Serializer.INT_ARRAY)
                    .open();
        }
        else {
            db = DBMaker.fileDB(dbfile)
                    .closeOnJvmShutdown()
                    .make();
            coocuranceGraph = db.hashMap("coocuranceGraph")
                    .keySerializer(Serializer.INTEGER)
                    .valueSerializer(Serializer.INT_ARRAY)
                    .create();
        }
        this.bDBopen = true;
    }

    public void put(Integer pageId, int[] candidates) {
        if (pageId == null) return;
        this.coocuranceGraph.put(pageId, candidates);
    }

    public void closeDB() {
        if (db != null && !db.isClosed()) {
            db.commit();
            db.close();
        }
        this.bDBopen = false;
    }

    public String[] getRelatedCandidateTitles(String title) {
        return getRelatedCandidateTitles(this.idLinker.getIDFromTitle(title));
    }

    public String[] getRelatedCandidateTitles(Integer pageId) {
        if (pageId == null)
            return new String[]{};

        return null;
    }

}
