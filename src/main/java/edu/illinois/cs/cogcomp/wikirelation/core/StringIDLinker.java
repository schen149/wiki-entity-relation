package edu.illinois.cs.cogcomp.wikirelation.core;

import edu.illinois.cs.cogcomp.wikirelation.util.DataTypeUtil;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Mapping from String (and alternative forms) to a uniform id
 */
public class StringIDLinker {

    private static Logger logger = LoggerFactory.getLogger(StringIDLinker.class);
    private DB db;
    private boolean bReadOnly;
    public boolean bDBopen = false;

    private HTreeMap<String, int[]> string2id;

    public StringIDLinker(boolean bReadOnly, String mapdbDir) {
        this.bReadOnly = bReadOnly;
        loadDB(mapdbDir);
    }

    private void loadDB(String mapdbDir) {

        String dbfile = mapdbDir + File.separator + "string2id";

        if (bReadOnly) {
            db = DBMaker.fileDB(dbfile)
                    .fileChannelEnable()
                    .closeOnJvmShutdown()
                    .readOnly()
                    .make();
            string2id = db.hashMap("string2id")
                    .layout(8,64,4)
                    .keySerializer(Serializer.STRING)
                    .valueSerializer(Serializer.INT_ARRAY)
                    .open();
        }
        else {
            db = DBMaker.fileDB(dbfile)
                    .closeOnJvmShutdown()
                    .make();
            string2id = db.hashMap("string2id")
                    .layout(8,64,4)
                    .keySerializer(Serializer.STRING)
                    .valueSerializer(Serializer.INT_ARRAY)
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

    public void put(String title, Integer id) {
        if ((id != null) && (title != null)) {
            if (string2id.containsKey(title)) {
                int[] idList = string2id.get(title);
                idList = DataTypeUtil.appendElement(idList, id);
                string2id.put(title, idList);
            }
            else
            {
                string2id.put(title, new int[]{id});
            }
        }
    }

    public int[] getIDsFromString(String title) {
        if (title == null)
            return null;
        
        // TODO: Maybe remove this?
        title = DataTypeUtil.normalizeString(title);
        return this.string2id.get(title);
    }

}
