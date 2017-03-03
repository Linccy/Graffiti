package org.linccy.graffiti_sample;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

import org.linccy.graffiti.GraffitiConfig;

import okhttp3.OkHttpClient;

/**
 * @author：linchenxi on 2017/3/3 13:34
 * @description:
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        GraffitiConfig.init(getApplicationContext());
        //Fresco初始化
        ImagePipelineConfig config = OkHttpImagePipelineConfigFactory.newBuilder(getApplicationContext(), new OkHttpClient())
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(getApplicationContext(), config);
    }
}
