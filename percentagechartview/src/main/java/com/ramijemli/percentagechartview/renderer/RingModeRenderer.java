/*
 * Copyright 2018 Rami Jemli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ramijemli.percentagechartview.renderer;

import android.animation.ArgbEvaluator;
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


    // BACKGROUND BAR
    private static final float DEFAULT_BG_BAR_DP_WIDTH = 16;
    private Paint mBackgroundBarPaint;
    private boolean mDrawBackgroundBar;
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
        mDrawBackgroundBar = attrs.getBoolean(R.styleable.PercentageChartView_pcv_drawBackgroundBar, true);

        //BACKGROUND WIDTH
        mBackgroundBarThickness = attrs.getDimensionPixelSize(com.ramijemli.percentagechartview.R.styleable.PercentageChartView_pcv_backgroundBarThickness,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BG_BAR_DP_WIDTH, mView.getViewContext().getResources().getDisplayMetrics()));

        //BACKGROUND BAR COLOR
        mBackgroundBarColor = attrs.getColor(R.styleable.PercentageChartView_pcv_backgroundBarColor, Color.BLACK);

        //PROGRESS WIDTH
        mProgressBarThickness = attrs.getDimensionPixelSize(R.styleable.PercentageChartView_pcv_progressBarThickness,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PROGRESS_BAR_DP_WIDTH, mView.getViewContext().getResources().getDisplayMetrics()));

        //PROGRESS BAR STROKE STYLE
        int cap = attrs.getInt(com.ramijemli.percentagechartview.R.styleable.PercentageChartView_pcv_progressBarStyle, CAP_ROUND);
        mProgressBarStyle = (cap == CAP_ROUND) ? Paint.Cap.ROUND : Paint.Cap.BUTT;

        //ADAPTIVE BACKGROUND BAR COLOR
        mAdaptBackgroundBar = false;
        mAdaptBackgroundBar = attrs.getBoolean(R.styleable.PercentageChartView_pcv_adaptiveBackgroundBar, false);
        mAdaptiveBackgroundBarRatio = attrs.getInt(R.styleable.PercentageChartView_pcv_adaptiveBackgroundBarRatio, -1);
        mAdaptiveBackgroundBarMode = attrs.getInt(R.styleable.PercentageChartView_pcv_adaptiveBackgroundBarMode, -1);

        setup();
    }

    private void init() {
        //DRAW BACKGROUND BAR
        mDrawBackgroundBar = true;

        //BACKGROUND WIDTH
        mBackgroundBarThickness = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_BG_BAR_DP_WIDTH, mView.getViewContext().getResources().getDisplayMetrics());

        //BACKGROUND BAR COLOR
        mBackgroundBarColor = Color.BLACK;

        //PROGRESS BAR WIDTH
        mProgressBarThickness = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_PROGRESS_BAR_DP_WIDTH, mView.getViewContext().getResources().getDisplayMetrics());

        //PROGRESS BAR STROKE STYLE
        mProgressBarStyle = Paint.Cap.ROUND;

        //ADAPTIVE BACKGROUND BAR COLOR
        mAdaptBackgroundBar = false;
        mAdaptiveBackgroundBarRatio = mAdaptiveBackgroundBarMode = -1;

        setup();
    }

    private void setup() {
        mCircleBounds = new RectF();
        mBackgroundBounds = new RectF();
        mTextBounds = new Rect();
        mSweepAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ?
                -(this.mProgress / DEFAULT_MAX * 360) :
                this.mProgress / DEFAULT_MAX * 360;

        //BACKGROUND
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(mBackgroundColor);

        //BACKGROUND BAR
        mBackgroundBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundBarPaint.setStyle(Paint.Style.STROKE);
        mBackgroundBarPaint.setColor(mBackgroundBarColor);
        mBackgroundBarPaint.setStrokeWidth(mBackgroundBarThickness);
        mBackgroundBarPaint.setStrokeCap(mProgressBarStyle);

        //PROGRESS
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressBarThickness);
        mProgressPaint.setStrokeCap(mProgressBarStyle);
        mProgressPaint.setColor(mProgressColor);

        //TEXT
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        if (mTypeface != null) {
            mTextPaint.setTypeface(mTypeface);
        }
        if (mTextShadowColor != Color.TRANSPARENT) {
            mTextPaint.setShadowLayer(mTextShadowRadius, mTextShadowDistX, mTextShadowDistY, mTextShadowColor);
        }
        updateText();

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

                mSweepAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ?
                        -(mProgress / DEFAULT_MAX * 360) :
                        mProgress / DEFAULT_MAX * 360;

                updateText();

                mView.onProgressUpdated(mProgress);
                mView.invalidate();
            }
        });
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
        if (mDrawBackground) {
            canvas.drawArc(mBackgroundBounds, 0, 360, false, mBackgroundPaint);
        }

        //BACKGROUND BAR
        if (mDrawBackgroundBar) {
            if (mBackgroundBarThickness <= mProgressBarThickness) {
                canvas.drawArc(mCircleBounds, mStartAngle + mSweepAngle, 360 - mSweepAngle, false, mBackgroundBarPaint);
            } else {
                canvas.drawArc(mCircleBounds, 0, 360, false, mBackgroundBarPaint);
            }
        }

        //FOREGROUND
        if (mProgress != 0) {
            canvas.drawArc(mCircleBounds, mStartAngle, mSweepAngle, false, mProgressPaint);
        }

        //TEXT
        canvas.drawText(textValue, mCircleBounds.centerX(), mCircleBounds.centerY() + (textHeight / 2f), mTextPaint);
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
        mCircleBounds = mBackgroundBounds = null;
        mTextBounds = null;
        mBackgroundPaint = mProgressPaint = mTextPaint = null;

        if (mAdaptiveColorProvider != null) {
            mAdaptiveColorProvider = null;
        }
    }

    @Override
    public void setAdaptiveColorProvider(@Nullable PercentageChartView.AdaptiveColorProvider adaptiveColorProvider) {
        if (adaptiveColorProvider == null) {
            mColorAnimator = null;
            this.mAdaptiveColorProvider = null;
            mAdaptBackground = mAdaptText = mAdaptBackgroundBar = false;
            mTextPaint.setColor(mTextColor);
            mBackgroundBarPaint.setColor(mBackgroundBarColor);
            mBackgroundPaint.setColor(mBackgroundColor);
            mProgressPaint.setColor(mProgressColor);
            mView.invalidate();
            return;
        }

        this.mAdaptiveColorProvider = adaptiveColorProvider;

        if (mColorAnimator == null) {
            mColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mProgressColor, mAdaptiveColor);
            mColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    updateAdaptiveColors((int) animation.getAnimatedValue());
                }
            });
            mColorAnimator.setDuration(mAnimDuration);
        }

        updateAdaptiveColors(adaptiveColorProvider.getColor(mProgress));
        mView.invalidate();
    }

    @Override
    public void setProgress(float progress, boolean animate) {
        if (this.mProgress == progress) return;

        if (mProgressAnimator.isRunning()) {
            mProgressAnimator.cancel();
        }


        if (mColorAnimator != null && mColorAnimator.isRunning()) {
            mColorAnimator.cancel();
        }

        if (!animate) {
            if (mAdaptiveColorProvider != null) {
                updateAdaptiveColors(mAdaptiveColorProvider.getColor(progress));
            }
            this.mProgress = progress;
            this.mTextProgress = (int) progress;

            mSweepAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ?
                    -(this.mProgress / DEFAULT_MAX * 360) :
                    this.mProgress / DEFAULT_MAX * 360;

            updateText();

            mView.onProgressUpdated(mProgress);
            mView.invalidate();
            return;
        }

        mProgressAnimator.setFloatValues(mProgress, progress);
        mProgressAnimator.start();

        if (mAdaptiveColorProvider != null) {
            int startColor = mAdaptiveColor != -1 ? mAdaptiveColor : mProgressColor;
            int endColor = mAdaptiveColorProvider.getColor(progress);
            mColorAnimator.setIntValues(startColor, endColor);
            mColorAnimator.start();
        }
    }

    private void updateAdaptiveColors(int targetColor) {
        mAdaptiveColor = targetColor;
        mProgressPaint.setColor(mAdaptiveColor);

        if (mDrawBackgroundBar && mAdaptBackgroundBar) {
            mAdaptiveBackgroundBarColor = (mAdaptiveBackgroundBarMode != -1 && mAdaptiveBackgroundBarRatio != -1) ?
                    ColorUtils.blendARGB(targetColor,
                            (mAdaptiveBackgroundBarMode == DARKER_MODE) ? Color.BLACK : Color.WHITE,
                            mAdaptiveBackgroundBarRatio / 100) :
                    ColorUtils.blendARGB(targetColor,
                            Color.BLACK,
                            .5f);

            mBackgroundBarPaint.setColor(mAdaptiveBackgroundBarColor);
        }

        if (mDrawBackground && mAdaptBackground) {
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
    public void setOrientation(int orientation) {
        mSweepAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ?
                -(mProgress / DEFAULT_MAX * 360) :
                mProgress / DEFAULT_MAX * 360;
    }

    @Override
    public void setStartAngle(float startAngle) {
        this.mStartAngle = startAngle;
        mView.invalidate();
    }

    @Override
    void updateText() {
        textValue = String.valueOf(mTextProgress) + "%";
        mTextPaint.getTextBounds(textValue, 0, textValue.length(), mTextBounds);
        textHeight = mTextBounds.height();
    }

    // DRAW BACKGROUND BAR STATE
    public boolean isDrawBackgroundBarEnabled() {
        return mDrawBackgroundBar;
    }

    public void setDrawBackgroundBarEnabled(boolean drawBackgroundBar) {
        if(mDrawBackgroundBar == drawBackgroundBar) return;
        this.mDrawBackgroundBar = drawBackgroundBar;
        mView.invalidate();
    }

    //ADAPTIVE BACKGROUND BAR
    public boolean isAdaptiveBackgroundBarEnabled() {
        return mAdaptBackgroundBar;
    }

    public float getAdaptiveBackgroundBarRatio() {
        return mAdaptiveBackgroundBarRatio;
    }

    public int getAdaptiveBackgroundBarMode() {
        return mAdaptiveBackgroundBarMode;
    }

    public void setAdaptiveBgBarEnabled(boolean enable) {
        if (mAdaptiveColorProvider == null || !mDrawBackgroundBar) return;
        mAdaptBackgroundBar = enable;
        if (mAdaptBackgroundBar) {
            updateAdaptiveColors(mAdaptiveColorProvider.getColor(mProgress));
        } else {
            mAdaptiveBackgroundBarRatio = mAdaptiveBackgroundBarMode = -1;
            mBackgroundBarPaint.setColor(mBackgroundBarColor);
        }
        mView.invalidate();
    }

    public void setAdaptiveBackgroundBar(float ratio, int adaptiveMode) {
        if (mAdaptiveColorProvider == null || !mDrawBackgroundBar) return;
        mAdaptBackgroundBar = true;
        mAdaptiveBackgroundBarRatio = ratio;
        mAdaptiveBackgroundBarMode = adaptiveMode;
        updateAdaptiveColors(mAdaptiveColorProvider.getColor(mProgress));
        mView.invalidate();
    }

    //ADAPTIVE BACKGROUND
    @Override
    public void setAdaptiveBgEnabled(boolean enable) {
        if (mAdaptiveColorProvider == null || !mDrawBackground || mAdaptBackground == enable)
            return;
        mAdaptBackground = enable;
        if (mAdaptBackground) {
            updateAdaptiveColors(mAdaptiveColorProvider.getColor(mProgress));
        } else {
            mAdaptiveBackgroundRatio = mAdaptiveBackgroundMode = -1;
            mBackgroundPaint.setColor(mBackgroundColor);
        }
        mView.invalidate();
    }

    @Override
    public void setAdaptiveBackground(float ratio, int adaptiveMode) {
        if (mAdaptiveColorProvider == null || !mDrawBackground) return;
        mAdaptBackground = true;
        mAdaptiveBackgroundRatio = ratio;
        mAdaptiveBackgroundMode = adaptiveMode;
        updateAdaptiveColors(mAdaptiveColorProvider.getColor(mProgress));
        mView.invalidate();
    }

    //ADAPTIVE TEXT
    @Override
    public void setAdaptiveTextEnabled(boolean enable) {
        if (mAdaptiveColorProvider == null || mAdaptText == enable) return;
        mAdaptText = enable;
        if (mAdaptText) {
            updateAdaptiveColors(mAdaptiveColorProvider.getColor(mProgress));
        } else {
            mAdaptiveTextRatio = mAdaptiveTextMode = -1;
            mTextPaint.setColor(mTextColor);
        }
        mView.invalidate();
    }

    @Override
    public void setAdaptiveText(float ratio, int adaptiveMode) {
        if (mAdaptiveColorProvider == null) return;
        mAdaptText = true;
        mAdaptiveTextRatio = ratio;
        mAdaptiveTextMode = adaptiveMode;
        updateAdaptiveColors(mAdaptiveColorProvider.getColor(mProgress));
        mView.invalidate();
    }

    //BACKGROUND BAR COLOR
    public int getBackgroundBarColor() {
        if (!mDrawBackgroundBar) return -1;
        return (!mAdaptBackgroundBar) ? mBackgroundBarColor : mAdaptiveBackgroundBarColor;
    }

    public void setBackgroundBarColor(int backgroundBarColor) {
        if (!mDrawBackgroundBar || (mAdaptiveColorProvider != null && mAdaptBackgroundBar) || this.mBackgroundBarColor == backgroundBarColor)
            return;
        this.mBackgroundBarColor = backgroundBarColor;
        mBackgroundBarPaint.setColor(mBackgroundBarColor);
        mView.invalidate();
    }

    //BACKGROUND BAR THICKNESS
    public float getBackgroundBarThickness() {
        return mBackgroundBarThickness;
    }

    public void setBackgroundBarThickness(float backgroundBarThickness) {
        if (this.mBackgroundBarThickness == backgroundBarThickness) return;
        this.mBackgroundBarThickness = backgroundBarThickness;
        mBackgroundBarPaint.setStrokeWidth(backgroundBarThickness);
        mView.invalidate();
        mView.requestLayout();
    }

    //PROGRESS BAR THICKNESS
    public float getProgressBarThickness() {
        return mProgressBarThickness;
    }

    public void setProgressBarThickness(float progressBarThickness) {
        if (this.mProgressBarThickness == progressBarThickness) return;
        this.mProgressBarThickness = progressBarThickness;
        mProgressPaint.setStrokeWidth(progressBarThickness);
        mView.invalidate();
        mView.requestLayout();
    }

    //PROGRESS BAR STYLE
    public int getProgressBarStyle() {
        return (mProgressBarStyle == Paint.Cap.ROUND) ? CAP_ROUND : CAP_SQUARE;
    }

    public void setProgressBarStyle(int progressBarStyle) {
        mProgressBarStyle = (progressBarStyle == CAP_ROUND) ? Paint.Cap.ROUND : Paint.Cap.BUTT;
        mProgressPaint.setStrokeCap(mProgressBarStyle);
        mView.invalidate();
    }
}
