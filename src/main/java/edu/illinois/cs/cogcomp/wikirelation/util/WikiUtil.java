package edu.illinois.cs.cogcomp.wikirelation.util;

import edu.illinois.cs.cogcomp.xlwikifier.ConfigParameters;
import edu.illinois.cs.cogcomp.xlwikifier.freebase.FreeBaseQuery;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiUtil {

    public static final Set<String> FREEBASE_NE_TYPES = new HashSet<>(Arrays.asList(
            "people.person", "location.location", "organization.organization"
    ));

    public static String str2wikilink(String str){
        if(StringUtils.isEmpty(str))
            return str;
        if(Character.isLowerCase(str.codePointAt(0)))
            str = str.substring(0, 1).toUpperCase() + (str.length() > 1 ? str.substring(1) : "");
        return StringUtils.replaceChars(str, ' ', '_');
    }

    public static String url2wikilink(String url) {
        if(StringUtils.isEmpty(url))
            return url;
        url = url.trim();

        Pattern p = Pattern.compile(".*wiki/(.*)$");
        Matcher m = p.matcher(url);
        if (m.matches()) {
            return m.group(1);
        }
        else
            return null;
    }

    public static boolean isTitleNEType(String title, String lang){
        try {
            if (!ConfigParameters.is_set)
                ConfigParameters.setPropValues();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        if (!FreeBaseQuery.isloaded())
            FreeBaseQuery.loadDB(true);

        Set<String> types = new HashSet<>(FreeBaseQuery.getTypesFromTitle(title, lang));
        types.retainAll(FREEBASE_NE_TYPES);

        return !types.isEmpty();
    }
}
