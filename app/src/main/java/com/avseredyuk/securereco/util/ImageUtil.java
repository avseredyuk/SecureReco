package com.avseredyuk.securereco.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

/**
 * Created by Anton_Serediuk on 6/20/2017.
 */

public class ImageUtil {
    private ImageUtil() {
    }

    public static Bitmap getCircleCroppedBitmap(Bitmap fromBitmap) {
        Bitmap output = Bitmap.createBitmap(fromBitmap.getWidth(),
                fromBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, fromBitmap.getWidth(),
                fromBitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(fromBitmap.getWidth() / 2,
                fromBitmap.getHeight() / 2, fromBitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(fromBitmap, rect, rect, paint);
        return output;
    }
}
