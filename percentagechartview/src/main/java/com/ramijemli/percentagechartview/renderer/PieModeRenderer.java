package com.ramijemli.percentagechartview.renderer;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.ramijemli.percentagechartview.IPercentageChartView;

import androidx.core.graphics.ColorUtils;

public class PieModeRenderer extends BaseModeRenderer {

    //BACKGROUND
    private Paint mBackgroundPaint;
    private int mBackgroundColor;

    public PieModeRenderer(IPercentageChartView view) {
        super(view);
        init();
    }

    public PieModeRenderer(IPercentageChartView view, TypedArray attrs) {
        super(view, attrs);
        init(attrs);
    }

    private void init(TypedArray attrs) {
        //BACKGROUND COLOR
        mBackgroundColor = attrs.getColor(com.ramijemli.percentagechartview.R.styleable.PercentageChartView_pcv_backgroundColor, ColorUtils.blendARGB(getThemeAccentColor(), Color.BLACK, 0.8f));

        setup();
    }

    private void init() {
        mBackgroundColor = ColorUtils.blendARGB(getThemeAccentColor(), Color.BLACK, 0.8f);

        setup();
    }

    private void setup() {
        mCircleBounds = new RectF();
        mTextBounds = new Rect();
        mArcAngle = mProgress / DEFAULT_MAX * 360;

        //BACKGROUND
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(mAdaptBackground ? mAdaptiveBackgroundColor : mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

        //PROGRESS
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor((mAdaptiveColors != null) ? mAdaptiveColor : mProgressColor);
        mProgressPaint.setStyle(Paint.Style.FILL);

        //TEXT
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mAdaptText ? mAdaptiveTextColor : mTextColor);
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
        if (mAdaptiveColors != null) {
            mColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mAdaptiveColor, getAdaptiveColor(mProgress));
            mColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    updateAdaptiveColors((int) animation.getAnimatedValue());
                }
            });
            mColorAnimator.setDuration(mAnimDuration);
        }
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
        if (orientation == ORIENTATION_COUNTERCLOCKWISE) {
            mArcAngle = -(this.mProgress / DEFAULT_MAX * 360);
        } else {
            mArcAngle = this.mProgress / DEFAULT_MAX * 360;
        }

        //FOREGROUND
        if (mProgress != 0) {
            canvas.drawArc(mCircleBounds, mStartAngle, mArcAngle, true, mProgressPaint);
        }

        //BACKGROUND
        if (drawBackground) {
            canvas.drawArc(mCircleBounds, mStartAngle + mArcAngle, 360 - mArcAngle, true, mBackgroundPaint);
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
            if (mValueAnimator.isRunning()) {
                mValueAnimator.cancel();
            }
            mValueAnimator.removeAllUpdateListeners();
        }

        if (mColorAnimator != null) {
            if (mColorAnimator.isRunning()) {
                mColorAnimator.cancel();
            }
            mColorAnimator.removeAllUpdateListeners();
        }

        mValueAnimator = mColorAnimator = null;
        mCircleBounds = null;
        mTextBounds = null;
        mBackgroundPaint = mProgressPaint = mTextPaint = null;
    }

    private void updateAdaptiveColors(int targetColor) {
        mAdaptiveColor = targetColor;
        mProgressPaint.setColor(mAdaptiveColor);

        if (drawBackground && mAdaptBackground) {
            if (mAdaptiveBackgroundMode != -1 && mAdaptiveBackgroundRatio != -1) {
                mAdaptiveBackgroundColor = ColorUtils.blendARGB(targetColor,
                        (mAdaptiveBackgroundMode == DARKER_COLOR) ? Color.BLACK : Color.WHITE,
                        mAdaptiveBackgroundRatio / 100);
            } else {
                mAdaptiveBackgroundColor = ColorUtils.blendARGB(targetColor,
                        Color.BLACK,
                        .5f);
            }
            mBackgroundPaint.setColor(mAdaptiveBackgroundColor);
        }

        if (mAdaptText) {
            if (mAdaptiveTextMode != -1 && mAdaptiveTextRatio != -1) {
                mAdaptiveTextColor = ColorUtils.blendARGB(targetColor,
                        (mAdaptiveTextMode == DARKER_COLOR) ? Color.BLACK : Color.WHITE,
                        mAdaptiveTextRatio / 100);
            } else {
                mAdaptiveTextColor = ColorUtils.blendARGB(targetColor,
                        Color.WHITE,
                        .5f);
            }
            mTextPaint.setColor(mAdaptiveTextColor);
        }
    }

    @Override
    public void setProgress(float progress, boolean animate) {
        if (this.mProgress == progress) return;

        if (mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }

        if (mAdaptiveColors != null && mColorAnimator.isRunning()) {
            mColorAnimator.cancel();
        }

        if (!animate) {
            if (mAdaptiveColors != null) {
                updateAdaptiveColors(getAdaptiveColor(progress));
            }
            this.mProgress = progress;
            this.mTextProgress = (int) progress;
            mView.onProgressUpdated(mProgress);
            mView.requestInvalidate();
            return;
        }

        mValueAnimator.setFloatValues(mProgress, progress);
        mValueAnimator.start();

        if (mAdaptiveColors != null) {
            updateAdaptiveColors(mAdaptiveColor);
            mColorAnimator.setIntValues(mAdaptiveColor, getAdaptiveColor(progress));
            mColorAnimator.start();
        }
    }
}
