package com.avseredyuk.securereco.util;

import java.util.Arrays;

/**
 * Created by lenfer on 2/26/17.
 */
public class ArrayUtil {
    private ArrayUtil() {
    }

    public static byte[] combineArrays(byte[] one, byte[] two)  {
        byte[] combined = new byte[one.length + two.length];
        System.arraycopy(one, 0, combined, 0, one.length);
        System.arraycopy(two, 0, combined, one.length, two.length);
        return combined;
    }

    public static void eraseArray(byte[] array) {
        Arrays.fill(array, (byte) 0);
    }
}
