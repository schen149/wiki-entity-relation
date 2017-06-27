package edu.illinois.cs.cogcomp.wikirelation.Util;

/**
 * Created by squirrel on 6/27/17.
 */
public class CacheUtil {
    public static long concatTwoIntToLong(int higher, int lower) {
        return ((long) higher) << 32 | (lower & 0xffffffffL);
    }

    public static int getHigher32bitFromLong(long l) {
        return (int) (l >> 32);
    }

    public static int getLower32bitFromLong(long l) {
        return (int) l;
    }
}
