package org.linccy.graffiti_sample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.linccy.graffiti.GraffitiView;
import org.linccy.graffiti.GraffitiConfig;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by lcx on 12/21/16.
 * 画图fragment
 */

public class GraffitiFragment extends android.support.v4.app.Fragment implements GraffitiView.OnLoadFinishListener {
    public static final int REDO = 0;
    public static final int UNDO = 1;
    public static final int CLEAR = 2;
    public static final int ROTATE = 3;
    public static int ROTATE_ANGLE = 90;
    public static final int RED_PEN = 1;
    public static final int YELLOW_PEN = 2;
    public static final int BLUE_PEN = 3;

    private FrameLayout checkView;
    private GraffitiView mGraffitiView;
    private GraffitiView.OnGraffitiViewOnClickListener onGraffitiViewOnClickListener;

    private String img;
    private String local;
    private View mRootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        img = arguments.getString("url");
        local = arguments.getString("local");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(getContentLayout(), container, false);
        mGraffitiView = new GraffitiView(getContext());
        mGraffitiView.setOnLoadFinishListener(this);
        mGraffitiView.setOnGraffitiViewOnClick(onGraffitiViewOnClickListener);
        mGraffitiView.setId(R.id.graffitiview);     //setId，当页面被移除后恢复时GraffitiView调用保存状态
        mGraffitiView.setSaveEnabled(true);
        checkView = (FrameLayout) mRootView.findViewById(R.id.graffit);
        return mRootView;
    }

    public int getContentLayout() {
        return R.layout.fragment_graffiti;
    }


    public void setPenColor(int penType) {
        switch (penType) {
            case RED_PEN:
                mGraffitiView.setPenType_1();
                break;
            case YELLOW_PEN:
                mGraffitiView.setPenType_2();
                break;
            case BLUE_PEN:
                mGraffitiView.setPenType_3();
                break;
            default:
                break;
        }
    }


    public void action(int action) {
        switch (action) {
            case REDO:
                mGraffitiView.redo();
                break;
            case UNDO:
                mGraffitiView.undo();
                break;
            case CLEAR:
                mGraffitiView.clear();
                break;
            case ROTATE:
                mGraffitiView.rotate(ROTATE_ANGLE);
                break;
            default:
                break;
        }
    }

    /**
     * 设置画图View的背景图
     * 通过rxJava下载图片
     *
     * @param url 图片的地址
     */
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

    @Override
    public void onResume() {
        super.onResume();
        setImage(local, img);
    }

    public String saveAndClear() {

        if (mGraffitiView != null && mGraffitiView.isChanged()) {
            if (!TextUtils.isEmpty(local)) {
                //如果上一次有图片保存，则把上一次的图片删除
                File f = new File(local);
                if (f.exists()) f.delete();
                local = "";
            }
            local = saveEditPic(mGraffitiView.getResultBitmap());
            setChangedOver();
        }
        return local;
    }

    public boolean graffitiIsChanged() {
        return mGraffitiView != null && mGraffitiView.isChanged();
    }

    private void setChangedOver() {
        mGraffitiView.setChangedOver();
    }

    /**
     * 保存图片,图片必须为png->bitmap->png或者jpg->bitmap->jpg否则保存的图片将无法打开
     *
     * @param bitmap 要保存的bitmap
     */
    private String saveEditPic(Bitmap bitmap) {
        String path = "";
        File f = new File(GraffitiConfig.APP_TEMP_FILE_PATH + System.currentTimeMillis() + ".jpg");
        if (f.exists()) f.delete();
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
            if (null != fOut) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
            }
            path = f.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fOut.close();
                bitmap.recycle();
            } catch (Exception e) {
            }
        }
        return path;
    }

    public void setOnGraffitiViewOnClickListener(GraffitiView.OnGraffitiViewOnClickListener onGraffitiViewOnClickListener) {
        this.onGraffitiViewOnClickListener = onGraffitiViewOnClickListener;
    }

    @Override
    public void OnLoadFinish() {
        if (null == getActivity()) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkView.removeAllViews();
                checkView.addView(mGraffitiView);
            }
        });
    }

    @Override
    public void onDestroy() {
        if (null != mGraffitiView) {
            mGraffitiView.release();
            mGraffitiView.removeAllViews();
        }
        super.onDestroy();
    }
}
