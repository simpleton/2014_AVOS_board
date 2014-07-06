package com.sim.board.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by sim on 7/6/14.
 */
public class Utils {
    public static Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
}
