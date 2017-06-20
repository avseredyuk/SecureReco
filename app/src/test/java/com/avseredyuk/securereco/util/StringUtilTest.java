package com.avseredyuk.securereco.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Anton_Serediuk on 6/20/2017.
 */
public class StringUtilTest {
    public static final String AMR_EXTENSION = ".amr";
    public static final String FILE_DOT_AMR = "file.amr";
    public static final String FILE_DOT_BIN = "file.bin";
    public static final String FILE = "file";

    @Test
    public void addOrChangeFileExtensionNoExtension() throws Exception {
        assertEquals(FILE_DOT_AMR,
                StringUtil.addOrChangeFileExtension(FILE, AMR_EXTENSION));
    }

    @Test
    public void addOrChangeFileExtensionHasExtension() throws Exception {
        assertEquals(FILE_DOT_AMR,
                StringUtil.addOrChangeFileExtension(FILE_DOT_BIN, AMR_EXTENSION));
    }

}