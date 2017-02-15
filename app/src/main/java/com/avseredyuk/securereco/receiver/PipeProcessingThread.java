package com.avseredyuk.securereco.receiver;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lenfer on 2/15/17.
 */
public class PipeProcessingThread extends Thread {
    InputStream in;
    FileOutputStream out;

    PipeProcessingThread(InputStream in, FileOutputStream out) {
        this.in=in;
        this.out=out;
    }

    @Override
    public void run() {
        byte[] buf=new byte[8192];
        int len;
        try {
            while ((len=in.read(buf)) >= 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.flush();
            out.getFD().sync();
            out.close();
        }
        catch (IOException e) {
            Log.e(getClass().getSimpleName(),
                    "Exception transferring file", e);
        }
    }
}
