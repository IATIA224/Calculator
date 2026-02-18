package com.calmahahh.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for image loading, resizing and Base64 encoding.
 */
public class ImageUtils {

    /**
     * Loads a bitmap from a URI and downsamples it so neither dimension
     * exceeds {@code maxDimension} pixels.  This keeps memory usage and
     * upload payload size reasonable.
     */
    public static Bitmap loadAndResizeBitmap(Context context, Uri uri, int maxDimension)
            throws IOException {

        // 1. Decode bounds only to figure out the original size
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;

        InputStream in = context.getContentResolver().openInputStream(uri);
        if (in == null) throw new IOException("Cannot open image stream");
        BitmapFactory.decodeStream(in, null, opts);
        in.close();

        // 2. Calculate a power-of-two sample size
        int width = opts.outWidth;
        int height = opts.outHeight;
        int inSampleSize = 1;
        while (width / inSampleSize > maxDimension || height / inSampleSize > maxDimension) {
            inSampleSize *= 2;
        }

        // 3. Decode with the sample size
        opts = new BitmapFactory.Options();
        opts.inSampleSize = inSampleSize;

        in = context.getContentResolver().openInputStream(uri);
        if (in == null) throw new IOException("Cannot open image stream");
        Bitmap bitmap = BitmapFactory.decodeStream(in, null, opts);
        in.close();

        if (bitmap == null) throw new IOException("Failed to decode image");
        return bitmap;
    }

    /**
     * Compresses a bitmap to JPEG and returns its Base64-encoded string
     * (no line wrapping).
     *
     * @param bitmap  the source bitmap
     * @param quality JPEG quality 0-100
     */
    public static String bitmapToBase64(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}
