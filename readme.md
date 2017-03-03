# AutoFitTextView

Android绘图板，使用fresco、支持网络图片，支持手势缩放、undo、redo、清除、旋转、保存图片等操作。

![Example Image](/doc/sample.gif?raw=true)


## Usage

app：build.gradle
```cson
dependencies {
    compile project(':graffiti')
}
```
graffiti: build.gradle (需要依赖fresco)
```cson
dependencies {
    compile 'com.facebook.fresco:fresco:0.12.0'
    compile 'com.facebook.fresco:animated-gif:0.12.0'// 支持 GIF 动图，需要添加
    compile 'com.facebook.fresco:imagepipeline-okhttp3:0.12.0+'
}
```

初始化:

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        GraffitiConfig.init(getApplicationContext());//只需要在使用前初始化就可以了
        //Fresco初始化
        ImagePipelineConfig config = OkHttpImagePipelineConfigFactory.newBuilder(getApplicationContext(), new OkHttpClient())
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(getApplicationContext(), config);
    }
}
```
在代码中使用：

```java
 mGraffitiView = new GraffitiView(getContext());
        mGraffitiView.setOnLoadFinishListener(this);
        mGraffitiView.setOnGraffitiViewOnClick(onGraffitiViewOnClickListener);
        mGraffitiView.setId(R.id.graffitiview);     //setId，当页面被移除后恢复时GraffitiView调用保存状态
        mGraffitiView.setSaveEnabled(true);
```
添加网络图片或本地图片：

```java
    private void setImage(String path, String url) {
        //优先使用本地文件

        if (mGraffitiView == null) {
            mGraffitiView = new GraffitiView(getContext());
            mGraffitiView.setOnLoadFinishListener(this);
            mGraffitiView.setOnGraffitiViewOnClick(onGraffitiViewOnClickListener);
        }
        if (!TextUtils.isEmpty(path)) {
            mGraffitiView.setImageUri("file://" + path);
        } else {
            mGraffitiView.setImageUri(url);
        }
    }
```


## License

    Copyright 2017 Graffiti Linccy

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.