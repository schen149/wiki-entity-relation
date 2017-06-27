package edu.illinois.cs.cogcomp.wikirelation.config;

import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Configurator {
    private static Logger logger = LoggerFactory.getLogger(Configurator.class);
    private static boolean bIsLoaded = false;
    private static String loadedConfigFile = "";

    /* global config */
    public static String WIKI_DUMP_DATE;

    /* title map config */
    public static String MAPDB_PATH;

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
        if (rm.containsKey("mapDBPath"))
            MAPDB_PATH = rm.getString("mapDBPath").trim();
        if (rm.containsKey("dumpDate"))
            WIKI_DUMP_DATE = rm.getString("dumpDate").trim();
    }
}
