package org.linccy.graffiti;

import android.content.Context;
import android.util.DisplayMetrics;

import java.io.File;

/**
 * @author linccy on 2017/3/3 13:32
 * 绘图板配置类
 */
public class GraffitiConfig {
    public static String APP_TEMP_FILE_PATH;
    public static int screenHeight;
    public static int screenWidth;

    public static void init(Context context) {
        //设置屏幕宽高
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        //创建存储临时文件的目录
        APP_TEMP_FILE_PATH = context.getFilesDir().getAbsolutePath() + "/temp/";
        File dirFile = new File(APP_TEMP_FILE_PATH);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
    }
}
