package edu.illinois.cs.cogcomp.wikirelation.util;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Arrays;

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

    /**
     * Normalize utf-8 encoded latin-based characters to canonical forms in ascii
     * See https://en.wikipedia.org/wiki/Unicode_equivalence for explanation on 'NFD' form
     */
    public static String normalizeString(String str) {
        return Normalizer.normalize(str.trim().toLowerCase(), Normalizer.Form.NFKD).replaceAll("\\p{M}","");
    }

    public static String utf8ToAscii(String str) {
        str = normalizeString(str);
        byte[] sequence = str.getBytes(StandardCharsets.US_ASCII);

        boolean bIsLegal = true;
        /* Look for illegal characters */
        for (byte b : sequence) {
            if (b == 63) {
                bIsLegal = false;
                break;
            }
        }

        if (bIsLegal)
            return new String(sequence);
        else
            return null;
    }

    public static int[] appendElement(int[] a, int e) {
        a = Arrays.copyOf(a, a.length + 1);
        a[a.length - 1] = e;
        return a;
    }
}
