package android.com.mediacodectext;

import android.content.Context;

/**
 * create by cy
 * time : 2019/11/20
 * version : 1.0
 * Features :
 */
public class Application extends android.app.Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getInstance() {
        return mContext;
    }
}
