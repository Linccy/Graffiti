package org.linccy.graffiti;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableBitmap;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;


/**
 * @author linchenxi
 *         画图和缩放、移动手势的判断，画图由{@link LineView}完成
 */
public class GraffitiView extends RelativeLayout {

    private static final float MAX_SCALE = 10.0F;
    private static final float MIN_SCALE = 1.0f;
    private static final float BORDER = 10f;
    private static final long TO_CANVAS_TIME = 30;//触发绘图板onTouch的触摸时间
    private float[] mMatrixValues = new float[9];
    private float mGraffitiX, mGraffitiY;
    private float mOldDistance;
    private boolean mIsDrag = false;
    private boolean mIsClick = false;
    private RelativeLayout mShowView;
    private ImageView mImageView;
    private LineView mLineView;
    private Bitmap mCutoutImage = null;
    private PointF mOldPointer = null;
    private float initImageWidth;
    private float initImageHeight;
    private int lastHashcode = 0;
    private OnGraffitiViewOnClickListener mOnGraffitiViewOnClick;
    private OnLoadFinishListener mLoadFinishListener;
    private volatile Object lock = new Object();
    //    private boolean startScale = false;

    @SuppressLint("ClickableViewAccessibility")
    public GraffitiView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public GraffitiView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GraffitiView(Context context) {
        this(context, null, 0);
    }

    public void setOnGraffitiViewOnClick(OnGraffitiViewOnClickListener onGraffitiViewOnClick) {
        this.mOnGraffitiViewOnClick = onGraffitiViewOnClick;
    }

    public void setOnLoadFinishListener(OnLoadFinishListener mLoadFinishListener) {
        this.mLoadFinishListener = mLoadFinishListener;
    }


    public interface OnGraffitiViewOnClickListener {
        void onGraffitiClick();
    }

    public interface OnLoadFinishListener {
        void OnLoadFinish();
    }

    public void setImageUri(String uri) {
        if (mImageView == null) {
            mImageView = new ImageView(getContext());
        }
        init(uri);
    }

    protected void setCutoutImage(Bitmap bitmap) {
        mCutoutImage = bitmap;
    }

    protected void init(String url) {

        Bitmap bitmap;
        Drawable drawable;
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .setResizeOptions(new ResizeOptions(GraffitiConfig.screenWidth, GraffitiConfig.screenHeight))
                .build();
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>>
                dataSource = imagePipeline.fetchDecodedImage(imageRequest, getContext());
        CloseableReference<CloseableImage> closeableImageRef = dataSource.getResult();

        if (closeableImageRef != null && closeableImageRef.get() instanceof CloseableBitmap) {
            try {
                bitmap = ((CloseableBitmap) closeableImageRef.get()).getUnderlyingBitmap();
                synchronized (lock) {
                    setCutoutImage(bitmap.copy(bitmap.getConfig(), false));
                }
                mLoadFinishListener.OnLoadFinish();
            } catch (Exception e) {

            } finally {
                closeableImageRef.close();
            }
        } else {
            dataSource.subscribe(new BaseBitmapDataSubscriber() {
                @Override
                protected void onNewResultImpl(Bitmap bitmap) {
                    //FIXME run in workerThread
                    synchronized (lock) {
                        setCutoutImage(bitmap.copy(bitmap.getConfig(), false));
                    }
                    mLoadFinishListener.OnLoadFinish();
                }

                @Override
                protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {

                }
            }, CallerThreadExecutor.getInstance());
        }
    }

    public void setPenType_1() {
        mLineView.setPenType_1();
    }

    public void setPenType_2() {
        mLineView.setPenType_2();
    }

    public void setPenType_3() {
        mLineView.setPenType_3();
    }

    public void setPenType_4() {
        mLineView.setPenType_4();
    }

    public void setPenType_5() {
        mLineView.setPenType_5();
    }

    public void setPenType_6() {
        mLineView.setPenType_6();
    }

    public void setPenType_7() {
        mLineView.setPenType_7();
    }

    public void setPenType_8() {
        mLineView.setPenType_8();
    }

    public void setEraserType() {
        mLineView.setEraserType();
    }


    public void undo() {
        mLineView.undo();
    }

    public void redo() {
        mLineView.redo();
    }

    public void clear() {
        mLineView.clear();
    }

    public void release() {
        if (null != mCutoutImage) {
            mCutoutImage.recycle();
        }
    }

    public Bitmap getResultBitmap() {

        lastHashcode = mLineView.getLastHashCode();

        RectF clipRect = new RectF();
        clipRect.top = mImageView.getY();
        clipRect.left = mImageView.getX();
        clipRect.bottom = mImageView.getHeight();
        clipRect.right = mImageView.getWidth();

        PointF srcSize = new PointF();
        srcSize.x = mCutoutImage.getWidth();
        srcSize.y = mCutoutImage.getHeight();

        Bitmap bitmap = mLineView.getBCResutlImage(clipRect, srcSize);


        Bitmap resultBitmap = Bitmap.createBitmap(mCutoutImage.getWidth(), mCutoutImage.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(mCutoutImage, 0, 0, null);
        canvas.drawBitmap(bitmap, 0, 0, null);

        return resultBitmap;
    }

    private long mOnACTION_DOWN_TIME;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mIsClick = true;
                mOnACTION_DOWN_TIME = System.currentTimeMillis();
                return mLineView.onTouchEvent(event);

            case MotionEvent.ACTION_POINTER_DOWN:
                mIsDrag = true;
                mIsClick = false;
                mOldDistance = spacingOfTwoFinger(event);
                mOldPointer = middleOfTwoFinger(event);
                //设置放大和旋转的中心
//                mShowView.setPivotX((event.getX(0) + event.getX(1)) / 2);
//                mShowView.setPivotY((event.getY(0) + event.getY(1)) / 2);
//                startScale = false;
                break;

            case MotionEvent.ACTION_MOVE:
                mIsClick = false;
                long on_move_time = System.currentTimeMillis();
                if (on_move_time - mOnACTION_DOWN_TIME <=
                ViewConfiguration.getTapTimeout()) {
                    mIsClick = true;
                    return true;
                }
                if (!mIsDrag) return mLineView.onTouchEvent(event);
                if (event.getPointerCount() != 2) break;
                float newDistance = spacingOfTwoFinger(event);
                float scaleFactor = newDistance / mOldDistance;
                scaleFactor = checkingScale(mShowView.getScaleX(), scaleFactor);
//                if (startScale) {
                mShowView.setScaleX(mShowView.getScaleX() * scaleFactor);
                mShowView.setScaleY(mShowView.getScaleY() * scaleFactor);
//                }
                mOldDistance = newDistance;

                PointF newPointer = middleOfTwoFinger(event);
                mShowView.setX(mShowView.getX() + newPointer.x - mOldPointer.x);
                mShowView.setY(mShowView.getY() + newPointer.y - mOldPointer.y);
                mOldPointer = newPointer;
                checkingGraffiti();
//                startScale = true;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                break;

            case MotionEvent.ACTION_UP:
                if (mIsClick) {
                    if (mOnGraffitiViewOnClick != null) {
                        mOnGraffitiViewOnClick.onGraffitiClick();
                        return true;
                    }
                }
                if (!mIsDrag) return mLineView.onTouchEvent(event);
                mShowView.getMatrix().getValues(mMatrixValues);
                mLineView.setScaleAndOffset(mShowView.getScaleX(), mMatrixValues[2], mMatrixValues[5]);
                mIsDrag = false;
                break;
        }
        return true;

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float bitmapWidth = (float) mCutoutImage.getWidth();
        float bitmapHeight = (float) mCutoutImage.getHeight();
        initImageWidth = 0;
        initImageHeight = 0;

        if (bitmapWidth > bitmapHeight) {
            initImageWidth = getWidth() - 2 * BORDER;
            initImageHeight = (bitmapHeight / bitmapWidth) * initImageWidth;
        } else {
            initImageHeight = getHeight() - 2 * BORDER;
            initImageWidth = (bitmapWidth / bitmapHeight) * initImageHeight;
        }

        mShowView = new RelativeLayout(getContext());
        LayoutParams showViewParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);

        mImageView = new ImageView(getContext());
        LayoutParams imageViewParams = new LayoutParams((int) initImageWidth, (int) initImageHeight);
        if (mCutoutImage != null) {
            mImageView.setImageBitmap(mCutoutImage);
        }
        imageViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mLineView = new LineView(getContext());
        LayoutParams lineViewParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);


        mShowView.addView(mImageView, imageViewParams);
        mShowView.addView(mLineView, lineViewParams);

        addView(mShowView, showViewParams);

        mGraffitiX = (getWidth() - initImageWidth) / 2;
        mGraffitiY = (getHeight() - initImageHeight) / 2;
        mLineView.requestLayout();
    }

    private float checkingScale(float scale, float scaleFactor) {
        if ((scale <= MAX_SCALE && scaleFactor > 1.0) || (scale >= MIN_SCALE && scaleFactor < 1.0)) {
            if (scale * scaleFactor < MIN_SCALE) {
                scaleFactor = MIN_SCALE / scale;
            }

            if (scale * scaleFactor > MAX_SCALE) {
                scaleFactor = MAX_SCALE / scale;
            }

        }

        return scaleFactor;
    }

    private void checkingGraffiti() {
        PointF offset = offsetGraffiti();
        mShowView.setX(mShowView.getX() + offset.x);
        mShowView.setY(mShowView.getY() + offset.y);
        if (mShowView.getScaleX() == 1) {
            mShowView.setX(0);
            mShowView.setY(0);
        }
    }

    private PointF offsetGraffiti() {
        PointF offset = new PointF(0, 0);
        if (mShowView.getScaleX() > 1) {
            mShowView.getMatrix().getValues(mMatrixValues);
            if (mMatrixValues[2] > -(mGraffitiX * (mShowView.getScaleX() - 1))) {
                offset.x = -(mMatrixValues[2] + mGraffitiX * (mShowView.getScaleX() - 1));
            }

            if (mMatrixValues[2] + mShowView.getWidth() * mShowView.getScaleX() - mGraffitiX * (mShowView.getScaleX() - 1) < getWidth()) {
                offset.x = getWidth() - (mMatrixValues[2] + mShowView.getWidth() * mShowView.getScaleX() - mGraffitiX * (mShowView.getScaleX() - 1));
            }

            if (mMatrixValues[5] > -(mGraffitiY * (mShowView.getScaleY() - 1))) {
                offset.y = -(mMatrixValues[5] + mGraffitiY * (mShowView.getScaleY() - 1));
            }

            if (mMatrixValues[5] + mShowView.getHeight() * mShowView.getScaleY() - mGraffitiY * (mShowView.getScaleY() - 1) < getHeight()) {
                offset.y = getHeight() - (mMatrixValues[5] + mShowView.getHeight() * mShowView.getScaleY() - mGraffitiY * (mShowView.getScaleY() - 1));
            }
        }

        return offset;
    }

    public boolean isChanged() {
        if (mLineView != null && lastHashcode != mLineView.getLastHashCode()) {
            return true;
        }
        return false;
    }

    public void setChangedOver(){
        lastHashcode = mLineView.getLastHashCode();
    }

    /**
     * 沿Z轴旋转
     * 每次加90度
     */
    public void rotate(float rotation) {
        setRotation(getRotation() + rotation);
    }

    /**
     * 计算两个触控点之间的距离
     *
     * @param event 触控事件
     * @return 触控点之间的距离
     */
    public static float spacingOfTwoFinger(MotionEvent event) {
        if (event.getPointerCount() != 2) return 0;
        double dx = event.getX(0) - event.getX(1);
        double dy = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * 计算两个触控点形成的角度
     *
     * @param event 触控事件
     * @return 角度值
     */
    public static float rotationDegreeOfTwoFinger(MotionEvent event) {
        if (event.getPointerCount() != 2) return 0;
        double dx = (event.getX(0) - event.getX(1));
        double dy = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(dy, dx);
        return (float) Math.toDegrees(radians);
    }

    /**
     * 计算两个触控点的中点
     *
     * @param event 触控事件
     * @return 中点浮点类
     */
    public static PointF middlePointFOfTwoFinger(MotionEvent event) {
        if (event.getPointerCount() != 2) return null;
        float mx = (event.getX(0) + event.getX(1)) / 2;
        float my = (event.getY(0) + event.getY(1)) / 2;
        PointF middle = new PointF(mx, my);
        return middle;
    }

    /**
     * 获得触控事件的坐标点
     *
     * @param event 触控事件
     * @return 坐标点浮点类
     */
    public static PointF getPointFFromEvent(MotionEvent event) {
        return new PointF(event.getX(), event.getY());
    }


    public static PointF middleOfTwoFinger(MotionEvent event) {
        float mx = (event.getX(0) + event.getX(1)) / 2;
        float my = (event.getY(0) + event.getY(1)) / 2;
        PointF middle = new PointF(mx, my);
        return middle;
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        super.dispatchSaveInstanceState(container);
    }
}
