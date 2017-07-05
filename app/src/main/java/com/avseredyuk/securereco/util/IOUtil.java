package com.avseredyuk.securereco.util;

import android.util.Log;

import com.avseredyuk.securereco.callback.FileCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;

/**
 * Created by lenfer on 2/20/17.
 */
public class IOUtil {
    /** Filter which accepts every file */
    public static final String FILTER_ALLOW_ALL = "*.*";
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private IOUtil() {
    }

    public static void processFiles(String directory, FileCallback callback) {
        File folder = new File(directory);
        if (folder.listFiles() != null) {
            for (File fileEntry : folder.listFiles()) {
                callback.execute(fileEntry);
            }
        }
    }

    public static boolean isSameFile(String filePath1, String filePath2) {
        try {
            return new File(filePath1).getCanonicalPath().equals(new File(filePath2).getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * This method checks that the file is accepted by the filter
     *
     * @param file
     *            - file that will be checked if there is a specific type
     * @param filter
     *            - criterion - the file type(for example ".jpg")
     * @return true - if file meets the criterion - false otherwise.
     */
    public static boolean accept(final File file, final String filter) {
        if (filter.compareTo(FILTER_ALLOW_ALL) == 0) {
            return true;
        }
        if (file.isDirectory()) {
            return true;
        }
        int lastIndexOfPoint = file.getName().lastIndexOf('.');
        if (lastIndexOfPoint == -1) {
            return false;
        }
        String fileType = file.getName().substring(lastIndexOfPoint).toLowerCase();
        return fileType.compareTo(filter) == 0;
    }

    public static boolean writeFile(String filePath, byte[] fileData) throws IOException {
        File file = new File(filePath);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            out.write(fileData);
            return true;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public static boolean copyFile(File src, File dst) {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();
            long fileSize = inChannel.size();
            return fileSize == inChannel.transferTo(0, fileSize, outChannel);
        } catch (IOException e) {
            //todo
        } finally {
            if (outChannel != null) {
                try {outChannel.close();} catch (IOException e) {}
            }
            if (inChannel != null) {
                try {inChannel.close();} catch (IOException e) {}
            }
        }
        return false;
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
