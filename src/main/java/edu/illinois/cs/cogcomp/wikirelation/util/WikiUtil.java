package edu.illinois.cs.cogcomp.wikirelation.util;

import org.apache.commons.lang.StringUtils;

public class WikiUtil {
    public static String str2wikilink(String str){
        if(StringUtils.isEmpty(str))
            return str;
        if(Character.isLowerCase(str.codePointAt(0)))
            str = str.substring(0, 1).toUpperCase() + (str.length() > 1 ? str.substring(1) : "");
        return StringUtils.replaceChars(str, ' ', '_');
    }
}
