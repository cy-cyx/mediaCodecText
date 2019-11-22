package android.com.mediacodectext.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * create by cy
 * time : 2019/11/20
 * version : 1.0
 * Features :
 */
public class FileUtils {

    private static String sdPath;

    public static String getSDPath() {
        if (sdPath == null) {
            sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
        }
        return sdPath;
    }

    public static String getNewMp4Path() {
        return getSDPath() + "MediaCodecText" + File.separator + System.currentTimeMillis() + ".mp4";
    }

    /**
     * 将Asset写到SD卡
     */
    public static boolean assets2SD(Context context, String path, String sdPath, boolean deleteOld) {
        InputStream is = getAssetsStream(context, path);
        return write2SD(is, sdPath, deleteOld, true);
    }

    /**
     * 打开Assets流
     */
    public static InputStream getAssetsStream(Context context, String resName) {
        if (resName == null || resName.trim().equals("")) {
            return null;
        }
        AssetManager asset = context.getAssets();
        InputStream is = null;
        try {
            is = asset.open(resName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }

    /**
     * 将流写到SD卡
     */
    public static boolean write2SD(InputStream is, String path, boolean deleteOld, boolean close) {
        if (is == null || path.isEmpty()) {
            return false;
        }
        File file = new File(path);
        if (file.isDirectory()) {
            return false;
        }
        if (file.exists()) {
            if (deleteOld) {
                file.delete();
            } else {
                return true;
            }
        } else {
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    return false;
                }
            }
        }
        try {
            if (!file.createNewFile()) {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        OutputStream os = null;
        try {
            os = new FileOutputStream(path);
            byte[] buf = new byte[4096];
            int len = 0;
            while ((len = is.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                os = null;
            }
            if (close) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                is = null;
            }
        }
        return true;
    }
}
