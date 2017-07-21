package edu.illinois.cs.cogcomp.wikirelation.core;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.Normalizer;

/**
 * Mapping from String (and alternative forms) to a uniform id
 */
public class StringIDLinker {

    private static Logger logger = LoggerFactory.getLogger(StringIDLinker.class);
    private DB db;
    private boolean bReadOnly;
    public boolean bDBopen = false;

    private HTreeMap<String, Integer> string2id;

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
                    .valueSerializer(Serializer.INTEGER)
                    .open();
        }
        else {
            db = DBMaker.fileDB(dbfile)
                    .closeOnJvmShutdown()
                    .make();
            string2id = db.hashMap("string2id")
                    .layout(8,64,4)
                    .keySerializer(Serializer.STRING)
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

    public void putWithoutNormalization(String title, Integer id) {
        if ((id != null) && (title != null))
            this.string2id.put(title, id);
    }

    public void put(String title, Integer id) {
        if ((id != null) && (title != null))
            this.string2id.put(normalizeString(title), id);
    }

    public Integer getIDFromString(String title) {
        if (title == null)
            return null;
        title = title.trim();
        return this.string2id.get(title);
    }

    /**
     * Normalize utf-8 encoded latin-based characters to canonical forms in ascii
     * See https://en.wikipedia.org/wiki/Unicode_equivalence for explanation on 'NFKD' form
     */
    public static String normalizeString(String str) {
        return Normalizer.normalize(str.trim().toLowerCase(), Normalizer.Form.NFKD).replaceAll("\\p{M}","");
    }
}
