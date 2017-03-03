package org.linccy.graffiti;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * @author linchenxi
 *         画图操作，由{@link GraffitiView}传递触摸事件
 */
@SuppressLint("ClickableViewAccessibility")
public class LineView extends View {

    private float mCurrentLineWidth = MarkPath.NORMAL_LINE_WIDTH;
    private boolean mIsDoubleTouch = false;
    private int mPathCount = 0;
    private ArrayList<MarkPath> mFinishedPaths;
    private MarkPath mCurrentPath = null;

    private Bitmap mBitmap = null;
    private Canvas mTempCanvas = null;
    private Paint mPaint = null;
    private float mScale = 1;
    private PointF mOffset = new PointF(0, 0);

    /**
     * 保存down下的坐标点
     */
    private PointF mCurrentPoint;

    /**
     * 当前的标记类型
     */
    private MarkPath.MarkType mCurrentType = MarkPath.MarkType.PEN_1;
    private int width;
    private int height;

    public LineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mFinishedPaths = new ArrayList<MarkPath>();
        setBackgroundColor(Color.TRANSPARENT);
    }

    public LineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineView(Context context) {
        this(context, null, 0);
    }

    public void setScaleAndOffset(float scale, float dx, float dy) {
        mScale = scale;
        mCurrentLineWidth = MarkPath.NORMAL_LINE_WIDTH / mScale;
        mOffset.x = dx;
        mOffset.y = dy;
    }

    public void setPenType_1() {
        mCurrentType = MarkPath.MarkType.PEN_1;
    }

    public void setPenType_2() {
        mCurrentType = MarkPath.MarkType.PEN_2;
    }

    public void setPenType_3() {
        mCurrentType = MarkPath.MarkType.PEN_3;
    }

    public void setPenType_4() {
        mCurrentType = MarkPath.MarkType.PEN_4;
    }

    public void setPenType_5() {
        mCurrentType = MarkPath.MarkType.PEN_5;
    }

    public void setPenType_6() {
        mCurrentType = MarkPath.MarkType.PEN_6;
    }

    public void setPenType_7() {
        mCurrentType = MarkPath.MarkType.PEN_7;
    }

    public void setPenType_8() {
        mCurrentType = MarkPath.MarkType.PEN_8;
    }

    public void setEraserType() {
        mCurrentType = MarkPath.MarkType.ERASER;
    }


    /**
     * 撤销 上一个MarkPath 对象画的线
     */
    public void undo() {
        if (mPathCount > 0) {
            mPathCount--;
        }
        invalidate();
    }

    public void redo() {
        if (mFinishedPaths != null && mFinishedPaths.size() > 0) {
            if (mPathCount < mFinishedPaths.size()) {
                mPathCount++;
            }
        }
        invalidate();
    }

    public void clear() {
        if (mPathCount != 0) {
            mPathCount = 0;
            mFinishedPaths.clear();
            invalidate();
        }
    }

    public Bitmap getBCResutlImage(RectF clipRect, PointF srcSize) {
        Bitmap drawBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(drawBitmap);
        canvas.drawColor(Color.TRANSPARENT);
        for (int i = 0; i < mPathCount; i++) {
            mFinishedPaths.get(i).drawBCResultPath(canvas);
        }

        Bitmap clipBitmap = Bitmap.createBitmap(drawBitmap, (int) clipRect.left, (int) clipRect.top, (int) clipRect.right, (int) clipRect.bottom, null, false);
        Bitmap resultBitmap = Bitmap.createScaledBitmap(clipBitmap, (int) srcSize.x, (int) srcSize.y, true);
        drawBitmap.recycle();
        clipBitmap.recycle();
        return resultBitmap;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mCurrentPoint = new PointF((event.getX() - mOffset.x) / mScale, (event.getY() - mOffset.y) / mScale);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsDoubleTouch = false;
                mCurrentPath = MarkPath.newMarkPath(mCurrentPoint);
                mCurrentPath.setCurrentMarkType(mCurrentType);
                mCurrentPath.setWidth(mCurrentLineWidth);
                invalidate();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mIsDoubleTouch = true;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mCurrentPath == null || mIsDoubleTouch == true) break;
                mCurrentPath.addMarkPointToPath(mCurrentPoint);
                postInvalidateDelayed(40);
//			invalidate();
                break;

            case MotionEvent.ACTION_UP:
                if (mCurrentPath != null && mIsDoubleTouch != true) {
                    mCurrentPath.addMarkPointToPath(mCurrentPoint);
                    //如果是点击了撤销后，撤销的笔画移出栈，并将新的笔画压入栈
                    if (mPathCount < mFinishedPaths.size()) {
                        int oldSize = mFinishedPaths.size();
                        for (int i = oldSize; i > mPathCount; i--) {
                            mFinishedPaths.remove(i - 1);
                        }
                    }
                    mFinishedPaths.add(mCurrentPath);

                    mPathCount++;
                }

                mIsDoubleTouch = false;
                mCurrentPath = null;
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mTempCanvas = new Canvas(mBitmap);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //清空Bitmap画布
        mTempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        width = getWidth();
        height = getHeight();
        if (mFinishedPaths.size() >= 0) {
            for (int i = 0; i < mPathCount; i++) {
                mFinishedPaths.get(i).drawMarkPath(mTempCanvas);
            }
        }

        if (mCurrentPath != null) {
            mCurrentPath.drawMarkPath(mTempCanvas);
        }

        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
    }

    public void setCurrentMarkType(MarkPath.MarkType markType) {
        setCurrentMarkType(markType);
    }

    /**
     * 用于记录绘制路径
     */
    public static class MarkPath implements Parcelable {

        public static final int[] m_penColors =
                {
                        Color.argb(128, 32, 79, 140),
                        Color.argb(156, 255, 0, 0),//红色画笔
                        Color.argb(156, 241, 221, 2),//黄色画笔
                        Color.argb(156, 0, 138, 255),//蓝色画笔
                        Color.argb(128, 40, 36, 37),
                        Color.argb(128, 226, 226, 226),
                        Color.argb(128, 219, 88, 50),
                        Color.argb(128, 129, 184, 69)
                };

        public static final float[] m_PenStrock =
                {
                        12,
                        14,
                        16,
                        18,
                        20,
                        22,
                        24,
                        26

                };

        protected MarkPath(Parcel in) {
            mCurrentWidth = in.readFloat();
            mPrevPoint = in.readParcelable(PointF.class.getClassLoader());
        }

        public static final Creator<MarkPath> CREATOR = new Creator<MarkPath>() {
            @Override
            public MarkPath createFromParcel(Parcel in) {
                return new MarkPath(in);
            }

            @Override
            public MarkPath[] newArray(int size) {
                return new MarkPath[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeFloat(mCurrentWidth);
            dest.writeParcelable(mPrevPoint, flags);
        }


        public static enum MarkType {
            PEN_1,
            PEN_2,
            PEN_3,
            PEN_4,
            PEN_5,
            PEN_6,
            PEN_7,
            PEN_8,
            ERASER
        }


        public static final float NORMAL_LINE_WIDTH = (float) 15.0f;


        private static enum LineType {
            MARK,
            BCRESULT,
        }

        private static final float ERASER_FACTOT = (float) 1.5;
        private static Paint sPaint = null;
        private static PorterDuffXfermode sClearMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

        private static final float TOUCH_TOLERANCE = 4.0f;

        private Path mPath;
        private float mCurrentWidth = NORMAL_LINE_WIDTH;
        private PointF mPrevPoint;
        private MarkType mCurrentMarkType = MarkType.PEN_1;

        private MarkPath() {
            mPath = new Path();
        }

        public static MarkPath newMarkPath(PointF point) {
            MarkPath newPath = new MarkPath();
            newPath.mPath.moveTo(point.x, point.y);
            newPath.mPrevPoint = point;

            return newPath;
        }

        /**
         * addMarkPointToPath 将坐标点添加到路径当中
         *
         * @param point， p2当前的点
         */
        public void addMarkPointToPath(PointF point) {
            float dx = Math.abs(point.x - mPrevPoint.x);
            float dy = Math.abs(point.y - mPrevPoint.y);

            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mPrevPoint.x, mPrevPoint.y, (point.x + mPrevPoint.x) / 2, (point.y + mPrevPoint.y) / 2);
            }
            mPrevPoint = point;
        }

        public void drawMarkPath(Canvas canvas) {
            resetPaint(LineType.MARK);
            canvas.drawPath(mPath, sPaint);
        }

        public void drawBCResultPath(Canvas canvas) {
            resetPaint(LineType.BCRESULT);
            canvas.drawPath(mPath, sPaint);
        }


        public MarkType getCurrentMarkType() {
            return mCurrentMarkType;
        }

        public void setCurrentMarkType(MarkType currentMarkType) {
            mCurrentMarkType = currentMarkType;
        }

        public void setWidth(float width) {
            mCurrentWidth = width;
        }

        //	ERASER


        private void resetPaint(LineType lineType) {
            if (sPaint == null) {
                sPaint = new Paint();
                sPaint.setAntiAlias(true);
                sPaint.setDither(true);
                sPaint.setStyle(Paint.Style.STROKE);
                sPaint.setStrokeJoin(Paint.Join.ROUND);
                sPaint.setStrokeCap(Paint.Cap.ROUND);
            }


            switch (mCurrentMarkType) {
                case PEN_1:
                    setNormalPaint();
                    sPaint.setColor(m_penColors[1]);
                    break;
                case PEN_2:
                    setNormalPaint();
                    sPaint.setColor(m_penColors[2]);
                    break;
                case PEN_3:
                    setNormalPaint();
                    sPaint.setColor(m_penColors[3]);
                    break;
                case PEN_4:
                    setNormalPaint();
                    sPaint.setColor(m_penColors[4]);
                    break;
                case PEN_5:
                    setNormalPaint();
                    sPaint.setColor(m_penColors[5]);
                    break;
                case PEN_6:
                    setNormalPaint();
                    sPaint.setColor(m_penColors[6]);
                    break;
                case PEN_7:
                    setNormalPaint();
                    sPaint.setColor(m_penColors[7]);
                    break;
                case PEN_8:
                    setNormalPaint();
                    sPaint.setColor(m_penColors[8]);
                    break;
                case ERASER:
                    sPaint.setAlpha(Color.TRANSPARENT);
                    sPaint.setXfermode(sClearMode);
                    sPaint.setStrokeWidth(mCurrentWidth * ERASER_FACTOT);
                    break;

                default:
                    break;
            }

        }

        private void setNormalPaint() {
            sPaint.setXfermode(null);
            sPaint.setAntiAlias(true);
            sPaint.setDither(true);
            sPaint.setStrokeWidth(mCurrentWidth);
        }
    }

    public int getmPathCount() {
        return mPathCount;
    }

    public int getLastHashCode() {
        if (mPathCount > 0) {
            return mFinishedPaths.get(mPathCount - 1).hashCode();
        }
        return 0;

    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        Parcelable superData = super.onSaveInstanceState();
        bundle.putParcelable("super_data", superData);
        bundle.putParcelableArrayList("finish_path", mFinishedPaths);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        Parcelable superData = bundle.getParcelable("super_data");
        mFinishedPaths = bundle.getParcelableArrayList("finish_path");
        super.onRestoreInstanceState(superData);
    }
}
