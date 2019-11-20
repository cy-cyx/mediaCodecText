package android.com.mediacodectext.utils;

import android.com.mediacodectext.Application;

import java.io.File;
import java.util.ArrayList;

/**
 * create by cy
 * time : 2019/11/20
 * version : 1.0
 * Features : 测试数据的管理类
 */
public class TextDataManager {

    private static ArrayList<DataInfo> dataInfos = new ArrayList<>();

    public static void initAsyn(final ICallBack callBack) {
        DataInfo dataInfo = new DataInfo();
        dataInfo.id = 0;
        dataInfo.AssetPath = "textVideo.mp4";
        dataInfo.LocalPath = FileUtils.getSDPath() + "MediaCodecText" + File.separator + dataInfo.AssetPath;
        dataInfos.add(dataInfo);

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (DataInfo info : dataInfos) {
                    if (!new File(info.LocalPath).exists()) {
                        FileUtils.assets2SD(Application.getInstance(), info.AssetPath, info.LocalPath, true);
                    }
                }
                callBack.onFinish();
            }
        }).start();
    }

    public interface ICallBack {
        public void onFinish();
    }

    private static class DataInfo {
        int id;
        String AssetPath;
        String LocalPath;
    }
}
