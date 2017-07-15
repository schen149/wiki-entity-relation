package edu.illinois.cs.cogcomp.wikirelation.config;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Configurator {
    private static Logger logger = LoggerFactory.getLogger(Configurator.class);
    private static boolean bIsLoaded = false;
    private static String loadedConfigFile = "";

    /* Choose which source of co-occurance map to use */
    public static String SOURCE;

    /* mapdb path */
    public static String PAGE_ID_MAPDB_PATH;
    public static String COOCCURANCE_MAPDB_PATH;
    public static String ENTITY_COUNT_MAPDB_PATH;

    public static void setPropValues(String configFile) throws IOException {
        ResourceManager rm = new ResourceManager(configFile);

        if (!bIsLoaded) {
            setPropValues(rm);
            bIsLoaded = true;
            loadedConfigFile = configFile;
        }
        else
            logger.info("Config is already set to "+ loadedConfigFile);
    }

    private static void setPropValues(ResourceManager rm) throws IOException {
        if (rm.containsKey("source")) {
            SOURCE = rm.getString("source").trim();
            switch (SOURCE) {
                case "enwiki-170601":
                    PAGE_ID_MAPDB_PATH = rm.getString("enwiki_20170601_pageid_mapdb");
                    COOCCURANCE_MAPDB_PATH = rm.getString("enwiki_20170601_cooccur_mapdb");
                    ENTITY_COUNT_MAPDB_PATH = rm.getString("enwiki_20170601_entity_count_mapdb");
                    break;
                case "gigaword":
                    PAGE_ID_MAPDB_PATH = rm.getString("enwiki_20170601_pageid_mapdb");
                    COOCCURANCE_MAPDB_PATH = rm.getString("enwiki_20170601_cooccur_mapdb");
                    ENTITY_COUNT_MAPDB_PATH = rm.getString("enwiki_20170601_entity_count_mapdb");
                    break;
                default:
                    PAGE_ID_MAPDB_PATH = rm.getString("enwiki_20170601_pageid_mapdb");
                    COOCCURANCE_MAPDB_PATH = rm.getString("enwiki_20170601_cooccur_mapdb");
                    ENTITY_COUNT_MAPDB_PATH = rm.getString("enwiki_20170601_entity_count_mapdb");
                    break;
            }
        }
    }
}
