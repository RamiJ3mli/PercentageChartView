package com.ramijemli.percentagechartview.renderer;

import android.animation.ValueAnimator;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.TypedValue;

import com.ramijemli.percentagechartview.IPercentageChartView;
import com.ramijemli.percentagechartview.PercentageChartView;
import com.ramijemli.percentagechartview.R;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

public class RingModeRenderer extends BaseModeRenderer {

    // BACKGROUND
    private RectF mBackgroundBounds;


    // BACKGROUND BAR
    private static final float DEFAULT_BG_BAR_DP_WIDTH = 16;
    private Paint mBackgroundBarPaint;
    private boolean drawBackgroundBar;
    private float mBackgroundBarThickness;
    private int mBackgroundBarColor;

    private int mAdaptiveBackgroundBarMode;
    private float mAdaptiveBackgroundBarRatio;
    private int mAdaptiveBackgroundBarColor;
    private boolean mAdaptBackgroundBar;

    //PROGRESS BAR
    private static final float DEFAULT_PROGRESS_BAR_DP_WIDTH = 16;
    public static final int CAP_ROUND = 0;
    public static final int CAP_SQUARE = 1;
    private Paint.Cap mProgressBarStyle;
    private float mProgressBarThickness;


    public RingModeRenderer(IPercentageChartView view) {
        super(view);
        init();
    }

    public RingModeRenderer(IPercentageChartView view, TypedArray attrs) {
        super(view, attrs);
        init(attrs);
    }

    private void init(TypedArray attrs) {
        //BACKGROUND BAR DRAW STATE
        drawBackgroundBar = attrs.getBoolean(R.styleable.PercentageChartView_pcv_drawBackgroundBar, true);

        //BACKGROUND WIDTH
        mBackgroundBarThickness = attrs.getDimensionPixelSize(com.ramijemli.percentagechartview.R.styleable.PercentageChartView_pcv_backgroundBarThickness,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BG_BAR_DP_WIDTH, mView.getViewContext().getResources().getDisplayMetrics()));

        //BACKGROUND BAR COLOR
        mBackgroundBarColor = attrs.getColor(R.styleable.PercentageChartView_pcv_backgroundBarColor, ColorUtils.blendARGB(mProgressColor, Color.BLACK, 0.8f));

        //PROGRESS WIDTH
        mProgressBarThickness = attrs.getDimensionPixelSize(R.styleable.PercentageChartView_pcv_progressBarThickness,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PROGRESS_BAR_DP_WIDTH, mView.getViewContext().getResources().getDisplayMetrics()));

        //PROGRESS BAR STROKE STYLE
        int cap = attrs.getInt(com.ramijemli.percentagechartview.R.styleable.PercentageChartView_pcv_progressBarStyle, CAP_ROUND);
        mProgressBarStyle = (cap == CAP_ROUND) ? Paint.Cap.ROUND : Paint.Cap.BUTT;

        //ADAPTIVE BACKGROUND BAR COLOR
        mAdaptBackgroundBar = false;
        mAdaptBackgroundBar = attrs.getBoolean(R.styleable.PercentageChartView_pcv_adaptiveBackgroundBar, false);
        if (drawBackgroundBar && mAdaptBackgroundBar) {
            mAdaptiveBackgroundBarRatio = attrs.getInt(R.styleable.PercentageChartView_pcv_adaptiveBackgroundBarRatio, -1);
            mAdaptiveBackgroundBarMode = attrs.getInt(R.styleable.PercentageChartView_pcv_adaptiveBackgroundBarMode, -1);
        }


        setup();
    }

    private void init() {
        //DRAW BACKGROUND BAR
        drawBackgroundBar = true;

        //BACKGROUND WIDTH
        mBackgroundBarThickness = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BG_BAR_DP_WIDTH, mView.getViewContext().getResources().getDisplayMetrics());

        //BACKGROUND BAR COLOR
        mBackgroundBarColor = ColorUtils.blendARGB(mProgressColor, Color.BLACK, 0.8f);

        //PROGRESS BAR WIDTH
        mProgressBarThickness = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PROGRESS_BAR_DP_WIDTH, mView.getViewContext().getResources().getDisplayMetrics());

        //PROGRESS BAR STROKE STYLE
        mProgressBarStyle = Paint.Cap.ROUND;

        setup();
    }

    private void setup() {
        mCircleBounds = new RectF();
        mBackgroundBounds = new RectF();
        mTextBounds = new Rect();
        mArcAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ?
                -(this.mProgress / DEFAULT_MAX * 360) :
                this.mProgress / DEFAULT_MAX * 360;

        //BACKGROUND
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(mAdaptBackground ? mAdaptiveBackgroundColor : mBackgroundColor);

        //BACKGROUND BAR
        mBackgroundBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundBarPaint.setStyle(Paint.Style.STROKE);
        mBackgroundBarPaint.setColor(mAdaptBackgroundBar ? mAdaptiveBackgroundBarColor : mBackgroundBarColor);
        mBackgroundBarPaint.setStrokeWidth(mBackgroundBarThickness);
        mBackgroundBarPaint.setStrokeCap(mProgressBarStyle);

        //PROGRESS
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.STROKE);
//        mProgressPaint.setColor((mAdaptiveColors != null || mView.getProvidedColor(mProgress) != -1) ? mAdaptiveColor : mProgressColor);
        mProgressPaint.setStrokeWidth(mProgressBarThickness);
        mProgressPaint.setStrokeCap(mProgressBarStyle);

        //TEXT
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mAdaptText ? mAdaptiveTextColor : mTextColor);
        if (mTypeface != null) {
            mTextPaint.setTypeface(mTypeface);
        }
        if (mTextShadowColor != Color.TRANSPARENT) {
            mTextPaint.setShadowLayer(mTextShadowRadius, mTextShadowDistX, mTextShadowDistY, mTextShadowColor);
        }

        //ANIMATIONS
        mProgressAnimator = ValueAnimator.ofFloat(0, mProgress);
        mProgressAnimator.setDuration(mAnimDuration);
        mProgressAnimator.setInterpolator(mAnimInterpolator);
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mProgress = (float) valueAnimator.getAnimatedValue();

                if (mProgress > 0 && mProgress <= 100)
                    mTextProgress = (int) mProgress;
                else if (mProgress > 100)
                    mTextProgress = 100;
                else mTextProgress = 0;

                mArcAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ?
                        -(mProgress / DEFAULT_MAX * 360) :
                        mProgress / DEFAULT_MAX * 360;

                mView.onProgressUpdated(mProgress);
                mView.requestInvalidate();
            }
        });

//        if (mAdaptiveColors != null || mView.getProvidedColor(mProgress) != -1) {
//            mColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mAdaptiveColor, getAdaptiveColor(mProgress));
//            mColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    updateAdaptiveColors((int) animation.getAnimatedValue());
//                }
//            });
//            mColorAnimator.setDuration(mAnimDuration);
//        }
    }

    @Override
    public void mesure(int w, int h, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        int diameter = Math.min(w, h);
        float maxOffset = Math.max(mProgressBarThickness, mBackgroundBarThickness);

        int centerX = w / 2;
        int centerY = h / 2;
        float radius = (diameter - maxOffset) / 2;

        mCircleBounds.left = centerX - radius;
        mCircleBounds.top = centerY - radius;
        mCircleBounds.right = centerX + radius;
        mCircleBounds.bottom = centerY + radius;

        float backgroundRadius = radius - (mBackgroundBarThickness / 2) + 1;
        mBackgroundBounds.left = centerX - backgroundRadius;
        mBackgroundBounds.top = centerY - backgroundRadius;
        mBackgroundBounds.right = centerX + backgroundRadius;
        mBackgroundBounds.bottom = centerY + backgroundRadius;
    }

    @Override
    public void draw(Canvas canvas) {
        //BACKGROUND
        if (drawBackground) {
            canvas.drawArc(mBackgroundBounds, 0, 360, false, mBackgroundPaint);
        }

        //BACKGROUND BAR
        if (drawBackgroundBar) {
            if (mBackgroundBarThickness <= mProgressBarThickness) {
                canvas.drawArc(mCircleBounds, mStartAngle + mArcAngle, 360 - mArcAngle, false, mBackgroundBarPaint);
            } else {
                canvas.drawArc(mCircleBounds, 0, 360, false, mBackgroundBarPaint);
            }
        }

        //FOREGROUND
        if (mProgress != 0) {
            canvas.drawArc(mCircleBounds, mStartAngle, mArcAngle, false, mProgressPaint);
        }

        //TEXT
        String text = String.valueOf(mTextProgress) + "%";
        mTextPaint.getTextBounds(text, 0, text.length(), mTextBounds);
        int textHeight = mTextBounds.height();
        canvas.drawText(text, mCircleBounds.centerX(), mCircleBounds.centerY() + (textHeight / 2f), mTextPaint);
    }

    @Override
    public void destroy() {
        if (mProgressAnimator != null) {
            if (mProgressAnimator.isRunning()) {
                mProgressAnimator.cancel();
            }
            mProgressAnimator.removeAllUpdateListeners();
        }

        if (mColorAnimator != null) {
            if (mColorAnimator.isRunning()) {
                mColorAnimator.cancel();
            }
            mColorAnimator.removeAllUpdateListeners();
        }

        mProgressAnimator = mColorAnimator = null;
        mCircleBounds = null;
        mTextBounds = null;
        mBackgroundPaint = mProgressPaint = mTextPaint = null;

        if (adaptiveColorProvider != null) {
            adaptiveColorProvider = null;
        }
    }

    @Override
    public void setAdaptiveColorProvider(@Nullable PercentageChartView.AdaptiveColorProvider adaptiveColorProvider) {

    }

    private void updateAdaptiveColors(int targetColor) {
        mAdaptiveColor = targetColor;
        mProgressPaint.setColor(mAdaptiveColor);

        if (drawBackgroundBar && mAdaptBackgroundBar) {
            mAdaptiveBackgroundBarColor = (mAdaptiveBackgroundBarMode != -1 && mAdaptiveBackgroundBarRatio != -1) ?
                    ColorUtils.blendARGB(targetColor,
                            (mAdaptiveBackgroundBarMode == DARKER_MODE) ? Color.BLACK : Color.WHITE,
                            mAdaptiveBackgroundBarRatio / 100) :
                    ColorUtils.blendARGB(targetColor,
                            Color.BLACK,
                            .5f);

            mBackgroundBarPaint.setColor(mAdaptiveBackgroundBarColor);
        }

        if (drawBackground && mAdaptBackground) {
            mAdaptiveBackgroundColor = (mAdaptiveBackgroundMode != -1 && mAdaptiveBackgroundRatio != -1) ?
                    ColorUtils.blendARGB(targetColor,
                            (mAdaptiveBackgroundMode == DARKER_MODE) ? Color.BLACK : Color.WHITE,
                            mAdaptiveBackgroundRatio / 100) :
                    ColorUtils.blendARGB(targetColor,
                            Color.BLACK,
                            .5f);

            mBackgroundPaint.setColor(mAdaptiveBackgroundColor);
        }

        if (mAdaptText) {
            mAdaptiveTextColor = (mAdaptiveTextMode != -1 && mAdaptiveTextRatio != -1) ?
                    ColorUtils.blendARGB(targetColor,
                            (mAdaptiveTextMode == DARKER_MODE) ? Color.BLACK : Color.WHITE,
                            mAdaptiveTextRatio / 100) :
                    ColorUtils.blendARGB(targetColor,
                            Color.WHITE,
                            .5f);

            mTextPaint.setColor(mAdaptiveTextColor);
        }
    }

    @Override
    public void setProgress(float progress, boolean animate) {
        if (this.mProgress == progress) return;

        if (mProgressAnimator.isRunning()) {
            mProgressAnimator.cancel();
        }

//        if ((mAdaptiveColors != null || mView.getProvidedColor(mProgress) != -1) && mColorAnimator != null &&  mColorAnimator.isRunning()) {
//            mColorAnimator.cancel();
//        }

        if (!animate) {
//            if (mAdaptiveColors != null || mView.getProvidedColor(mProgress) != -1) {
//                updateAdaptiveColors(getAdaptiveColor(progress));
//            }
            this.mProgress = progress;
            this.mTextProgress = (int) progress;
            mArcAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ?
                    -(this.mProgress / DEFAULT_MAX * 360) :
                    this.mProgress / DEFAULT_MAX * 360;
            mView.onProgressUpdated(mProgress);
            mView.requestInvalidate();
            return;
        }

        mProgressAnimator.setFloatValues(mProgress, progress);
        mProgressAnimator.start();

//        if (mAdaptiveColors != null || mView.getProvidedColor(mProgress) != -1) {
//            updateAdaptiveColors(mAdaptiveColor);
//            mColorAnimator.setIntValues(mAdaptiveColor, getAdaptiveColor(progress));
//            mColorAnimator.start();
//        }
    }
}
