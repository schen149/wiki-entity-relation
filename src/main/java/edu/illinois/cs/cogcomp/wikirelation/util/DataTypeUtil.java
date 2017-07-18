package edu.illinois.cs.cogcomp.wikirelation.util;

public class DataTypeUtil {
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
