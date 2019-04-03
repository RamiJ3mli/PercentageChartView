package com.ramijemli.percentagechartview.renderer;

import android.animation.ValueAnimator;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.TypedValue;

import com.ramijemli.percentagechartview.IPercentageChartView;
import com.ramijemli.percentagechartview.R;

public class RingModeRenderer extends BaseModeRenderer {

    // BACKGROUND BAR
    private static final float DEFAULT_BG_BAR_DP_WIDTH = 16;
    private Paint mBackgroundBarPaint;
    private float mBackgroundBarWidth;
    private int mBackgroundBarColor;

    // BACKGROUND FILL
    private RectF mBackgroundFillBounds;
    private Paint mBackgroundFillPaint;
    private int mBackgroundFillColor;

    //PROGRESS BAR
    private static final float DEFAULT_PROGRESS_BAR_DP_WIDTH = 12;
    public static final int CAP_ROUND = 0;
    public static final int CAP_SQUARE = 1;
    private Paint.Cap mProgressStyle;
    private float mProgressWidth;

    public RingModeRenderer(IPercentageChartView view) {
        super(view);
        init();
    }

    public RingModeRenderer(IPercentageChartView view, TypedArray attrs) {
        super(view, attrs);
        init(attrs);
    }

    private void init(TypedArray attrs) {
        //BACKGROUND WIDTH
        mBackgroundBarWidth = attrs.getDimensionPixelSize(com.ramijemli.percentagechartview.R.styleable.PercentageChartView_pcv_backgroundBarWidth,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BG_BAR_DP_WIDTH, mView.getViewContext().getResources().getDisplayMetrics()));

        //BACKGROUND FILL COLOR
        mBackgroundFillColor = attrs.getColor(com.ramijemli.percentagechartview.R.styleable.PercentageChartView_pcv_backgroundFillColor, -1);

        //PROGRESS WIDTH
        mProgressWidth = attrs.getDimensionPixelSize(R.styleable.PercentageChartView_pcv_progressBarWidth,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PROGRESS_BAR_DP_WIDTH, mView.getViewContext().getResources().getDisplayMetrics()));

        //PROGRESS BAR STROKE STYLE
        int cap = attrs.getInt(com.ramijemli.percentagechartview.R.styleable.PercentageChartView_pcv_progressBarStyle, CAP_ROUND);
        mProgressStyle = (cap == CAP_ROUND) ? Paint.Cap.ROUND : Paint.Cap.BUTT;

        prepare();
    }

    private void init() {
        mBackgroundBarWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BG_BAR_DP_WIDTH, mView.getViewContext().getResources().getDisplayMetrics());
        mBackgroundFillColor = -1;

        mProgressWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PROGRESS_BAR_DP_WIDTH, mView.getViewContext().getResources().getDisplayMetrics());
        mProgressStyle = Paint.Cap.ROUND;

        prepare();
    }

    private void prepare() {
        mCircleBounds = new RectF();
        mTextBounds = new Rect();
        mBackgroundFillBounds = new RectF();
        mArcAngle = mProgress / DEFAULT_MAX * 360;

        //BACKGROUND BAR
        mBackgroundFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundFillPaint.setStyle(Paint.Style.STROKE);
        mBackgroundFillPaint.setColor(mBackgroundFillColor);

        //BACKGROUND FILL
        mBackgroundFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundFillPaint.setStyle(Paint.Style.STROKE);
        mBackgroundFillPaint.setColor(mBackgroundFillColor);

        //PROGRESS
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setStyle(Paint.Style.FILL);

        //TEXT
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        if (mTypeface != null) {
            mTextPaint.setTypeface(mTypeface);
        }

        //ANIMATION
        mValueAnimator = ValueAnimator.ofFloat(0, mProgress);
        mValueAnimator.setDuration(mAnimDuration);
        mValueAnimator.setInterpolator(mAnimInterpolator);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mProgress = (float) valueAnimator.getAnimatedValue();

                if (mProgress > 0 && mProgress <= 100)
                    mTextProgress = (int) mProgress;
                else if (mProgress > 100)
                    mTextProgress = 100;
                else mTextProgress = 0;

                mView.onProgressUpdated(mProgress);
                mView.requestInvalidate();
            }
        });
    }

    @Override
    public void mesure(int w, int h, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        int centerX = w / 2;
        int centerY = h / 2;
        float radius = (float) Math.min(w, h) / 2;

        mCircleBounds.left = centerX - radius;
        mCircleBounds.top = centerY - radius;
        mCircleBounds.right = centerX + radius;
        mCircleBounds.bottom = centerY + radius;
    }

    @Override
    public void draw(Canvas canvas) {

        //FOREGROUND
        if (mProgress != 0) {
            if (orientation == ORIENTATION_COUNTERCLOCKWISE)
                mArcAngle = -(this.mProgress / DEFAULT_MAX * 360);
            else
                mArcAngle = this.mProgress / DEFAULT_MAX * 360;
            canvas.drawArc(mCircleBounds, mStartAngle, mArcAngle, true, mProgressPaint);
        }

        //BACKGROUND
        if (drawBackground) {
//            canvas.drawArc(mCircleBounds, mStartAngle + mArcAngle, 360 - mArcAngle, true, mBackgroundPaint);
        }

        //TEXT
        String text = String.valueOf(mTextProgress) + "%";
        mTextPaint.getTextBounds(text, 0, text.length(), mTextBounds);
        int textHeight = mTextBounds.height();
        canvas.drawText(text, mCircleBounds.centerX(), mCircleBounds.centerY() + (textHeight / 2f), mTextPaint);
    }

    @Override
    public void destroy() {
        if (mValueAnimator != null) {
            if (mValueAnimator.isRunning())
                mValueAnimator.cancel();

            mValueAnimator.removeAllUpdateListeners();
        }

        mValueAnimator = null;
        mCircleBounds = null;
        mTextBounds = null;
//        mBackgroundPaint = mProgressPaint = mTextPaint = null;
    }

    @Override
    public void setProgress(float progress, boolean animate) {
        if (this.mProgress == progress) return;

        if (mValueAnimator.isRunning()) mValueAnimator.cancel();

        if (!animate) {
            this.mProgress = progress;
            this.mTextProgress = (int) progress;
            mView.onProgressUpdated(mProgress);
            mView.requestInvalidate();
            return;
        }

        mValueAnimator.setFloatValues(mProgress, progress);
        mValueAnimator.start();
    }
}
