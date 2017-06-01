package com.avseredyuk.securereco.util;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Created by lenfer on 2/20/17.
 */
public class IOUtil {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private IOUtil() {
    }

    public static byte[] readFile(String filename) throws IOException {
        return readFile(new File(filename));
    }

    public static byte[] readFile(File file) throws IOException {
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

    public static String readText(final InputStream is, final String charsetName) throws IOException{
        final char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        final StringBuilder out = new StringBuilder();
        try {
            Reader in = new InputStreamReader(is, charsetName);
            for (;;) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
            in.close();
        }
        catch (UnsupportedEncodingException e) {
            Log.e(IOUtil.class.getSimpleName(),
                    "UnsupportedEncodingException stuff", e);
            throw new IOException(e);
        }
        return out.toString();
    }

    public static byte[] inputStreamToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream os = null;
        try {
            os = new ByteArrayOutputStream();
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

            for (int len; (len = is.read(buffer)) != -1;)
                os.write(buffer, 0, len);

            os.flush();

            return os.toByteArray();
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }
}
