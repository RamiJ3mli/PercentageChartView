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
import com.ramijemli.percentagechartview.R;
import com.ramijemli.percentagechartview.callback.AdaptiveColorProvider;

import androidx.annotation.Nullable;

public class RingModeRenderer extends BaseModeRenderer {


    // BACKGROUND BAR
    private static final float DEFAULT_BG_BAR_DP_WIDTH = 16;
    private Paint mBackgroundBarPaint;
    private boolean mDrawBackgroundBar;
    private float mBackgroundBarThickness;
    private int mBackgroundBarColor;

    private int mProvidedBgBarColor;

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

        setup();
    }

    private void setup() {
        mCircleBounds = new RectF();
        mBackgroundBounds = new RectF();
        mTextBounds = new Rect();
        mSweepAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ?
                -(this.mProgress / DEFAULT_MAX * 360) :
                this.mProgress / DEFAULT_MAX * 360;
        mProvidedProgressColor = mProvidedBackgroundColor = mProvidedTextColor = mProvidedBgBarColor = -1;

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
        mProgressAnimator.addUpdateListener(valueAnimator -> {
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

        if (mProgressColorAnimator != null) {
            if (mProgressColorAnimator.isRunning()) {
                mProgressColorAnimator.cancel();
            }
            mProgressColorAnimator.removeAllUpdateListeners();
        }

        if (mBackgroundColorAnimator != null) {
            if (mBackgroundColorAnimator.isRunning()) {
                mBackgroundColorAnimator.cancel();
            }
            mBackgroundColorAnimator.removeAllUpdateListeners();
        }

        if (mTextColorAnimator != null) {
            if (mTextColorAnimator.isRunning()) {
                mTextColorAnimator.cancel();
            }
            mTextColorAnimator.removeAllUpdateListeners();
        }

        if (mBgBarColorAnimator != null) {
            if (mBgBarColorAnimator.isRunning()) {
                mBgBarColorAnimator.cancel();
            }
            mBgBarColorAnimator.removeAllUpdateListeners();
        }

        mProgressAnimator = mProgressColorAnimator = mBackgroundColorAnimator = mTextColorAnimator = mBgBarColorAnimator = null;
        mCircleBounds = mBackgroundBounds = null;
        mTextBounds = null;
        mBackgroundPaint = mProgressPaint = mTextPaint = null;

        if (mAdaptiveColorProvider != null) {
            mAdaptiveColorProvider = null;
        }
    }

    @Override
    public void setAdaptiveColorProvider(@Nullable AdaptiveColorProvider adaptiveColorProvider) {
        if (adaptiveColorProvider == null) {
            mProgressColorAnimator = mBackgroundColorAnimator = mTextColorAnimator = mBgBarColorAnimator = null;
            this.mAdaptiveColorProvider = null;
            mTextPaint.setColor(mTextColor);
            mBackgroundBarPaint.setColor(mBackgroundBarColor);
            mBackgroundPaint.setColor(mBackgroundColor);
            mProgressPaint.setColor(mProgressColor);
            mView.invalidate();
            return;
        }

        this.mAdaptiveColorProvider = adaptiveColorProvider;

        setupColorAnimations();
        updateProvidedColors(mProgress);
        mView.invalidate();
    }

    @Override
    public void setProgress(float progress, boolean animate) {
        if (this.mProgress == progress) return;

        cancelAnimations();

        if (!animate) {
            updateProvidedColors(progress);
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

        updateAnimations(progress);
    }

    private void setupColorAnimations() {
        if (mProgressColorAnimator == null) {
            mProgressColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mProgressColor, mProvidedProgressColor);
            mProgressColorAnimator.addUpdateListener(animation -> {
                mProvidedProgressColor = (int) animation.getAnimatedValue();
                mProgressPaint.setColor(mProvidedProgressColor);
            });
            mProgressColorAnimator.setDuration(mAnimDuration);
        }

        if (mBackgroundColorAnimator == null) {
            mBackgroundColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mBackgroundColor, mProvidedBackgroundColor);
            mBackgroundColorAnimator.addUpdateListener(animation -> {
                mProvidedBackgroundColor = (int) animation.getAnimatedValue();
                mBackgroundPaint.setColor(mProvidedBackgroundColor);
            });
            mBackgroundColorAnimator.setDuration(mAnimDuration);
        }

        if (mTextColorAnimator == null) {
            mTextColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mTextColor, mProvidedTextColor);
            mTextColorAnimator.addUpdateListener(animation -> {
                mProvidedTextColor = (int) animation.getAnimatedValue();
                mTextPaint.setColor(mProvidedTextColor);
            });
            mTextColorAnimator.setDuration(mAnimDuration);
        }

        if (mBgBarColorAnimator == null) {
            mBgBarColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mBackgroundBarColor, mProvidedBgBarColor);
            mBgBarColorAnimator.addUpdateListener(animation -> {
                mProvidedBgBarColor = (int) animation.getAnimatedValue();
                mBackgroundBarPaint.setColor(mProvidedBgBarColor);
            });
            mBgBarColorAnimator.setDuration(mAnimDuration);
        }
    }

    private void cancelAnimations() {
        if (mProgressAnimator.isRunning()) {
            mProgressAnimator.cancel();
        }

        if (mProgressColorAnimator != null && mProgressColorAnimator.isRunning()) {
            mProgressColorAnimator.cancel();
        }

        if (mBackgroundColorAnimator != null && mBackgroundColorAnimator.isRunning()) {
            mBackgroundColorAnimator.cancel();
        }

        if (mTextColorAnimator != null && mTextColorAnimator.isRunning()) {
            mTextColorAnimator.cancel();
        }

        if (mBgBarColorAnimator != null && mBgBarColorAnimator.isRunning()) {
            mBgBarColorAnimator.cancel();
        }
    }

    private void updateAnimations(float progress) {
        mProgressAnimator.setFloatValues(mProgress, progress);
        mProgressAnimator.start();

        if (mAdaptiveColorProvider == null) return;


        int providedProgressColor = mAdaptiveColorProvider.provideProgressColor(progress);

        if (providedProgressColor != -1 && providedProgressColor != mProvidedProgressColor) {
            int startColor = mProvidedProgressColor != -1 ? mProvidedProgressColor : mProgressColor;
            mProgressColorAnimator.setIntValues(startColor, providedProgressColor);
            mProgressColorAnimator.start();
        }


        int providedBackgroundColor = mAdaptiveColorProvider.provideBackgroundColor(progress);

        if (providedBackgroundColor != -1 && providedBackgroundColor != mProvidedBackgroundColor) {
            int startColor = mProvidedBackgroundColor != -1 ? mProvidedBackgroundColor : mBackgroundColor;
            mBackgroundColorAnimator.setIntValues(startColor, providedBackgroundColor);
            mBackgroundColorAnimator.start();
        }


        int providedTextColor = mAdaptiveColorProvider.provideTextColor(progress);

        if (providedTextColor != -1 && providedTextColor != mProvidedTextColor) {
            int startColor = mProvidedTextColor != -1 ? mProvidedTextColor : mTextColor;
            mTextColorAnimator.setIntValues(startColor, providedTextColor);
            mTextColorAnimator.start();
        }


        int providedBgBarColor = mAdaptiveColorProvider.provideBackgroundBarColor(progress);

        if (providedBgBarColor != -1 && providedBgBarColor != mProvidedBgBarColor) {
            int startColor = mProvidedBgBarColor != -1 ? mProvidedBgBarColor : mBackgroundBarColor;
            mBgBarColorAnimator.setIntValues(startColor, providedBgBarColor);
            mBgBarColorAnimator.start();
        }
    }

    private void updateProvidedColors(float progress) {
        if (mAdaptiveColorProvider == null) return;


        int providedProgressColor = mAdaptiveColorProvider.provideProgressColor(progress);

        if (providedProgressColor != -1 && providedProgressColor != mProvidedProgressColor) {
            mProvidedProgressColor = providedProgressColor;
            mProgressPaint.setColor(mProvidedProgressColor);
        }


        int providedBackgroundColor = mAdaptiveColorProvider.provideBackgroundColor(progress);

        if (providedBackgroundColor != -1 && providedBackgroundColor != mProvidedBackgroundColor) {
            mProvidedBackgroundColor = providedBackgroundColor;
            mBackgroundPaint.setColor(mProvidedBackgroundColor);
        }


        int providedTextColor = mAdaptiveColorProvider.provideTextColor(progress);

        if (providedTextColor != -1 && providedTextColor != mProvidedTextColor) {
            mProvidedTextColor = providedTextColor;
            mTextPaint.setColor(mProvidedTextColor);
        }


        int providedBgBarColor = mAdaptiveColorProvider.provideBackgroundBarColor(progress);

        if (providedBgBarColor != -1 && providedBgBarColor != mProvidedBgBarColor) {
            mProvidedBgBarColor = providedBgBarColor;
            mBackgroundBarPaint.setColor(mProvidedBgBarColor);
        }
    }

    @Override
    public void setOrientation(int orientation) {
        if (this.orientation == orientation) return;
        this.orientation = orientation;
        this.mSweepAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ?
                -(mProgress / DEFAULT_MAX * 360) :
                mProgress / DEFAULT_MAX * 360;
    }

    @Override
    public void setStartAngle(float startAngle) {
        if (this.mStartAngle == startAngle) return;
        this.mStartAngle = startAngle;
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
        if (mDrawBackgroundBar == drawBackgroundBar) return;
        this.mDrawBackgroundBar = drawBackgroundBar;
    }

    //BACKGROUND BAR COLOR
    public int getBackgroundBarColor() {
        if (!mDrawBackgroundBar) return -1;
        return mBackgroundBarColor;
    }

    public void setBackgroundBarColor(int backgroundBarColor) {
        if (!mDrawBackgroundBar || (mAdaptiveColorProvider != null && mAdaptiveColorProvider.provideBackgroundBarColor(mProgress) != -1) || this.mBackgroundBarColor == backgroundBarColor)
            return;
        this.mBackgroundBarColor = backgroundBarColor;
        mBackgroundBarPaint.setColor(mBackgroundBarColor);
    }

    //BACKGROUND BAR THICKNESS
    public float getBackgroundBarThickness() {
        return mBackgroundBarThickness;
    }

    public void setBackgroundBarThickness(float backgroundBarThickness) {
        if (this.mBackgroundBarThickness == backgroundBarThickness) return;
        this.mBackgroundBarThickness = backgroundBarThickness;
        mBackgroundBarPaint.setStrokeWidth(backgroundBarThickness);
        mesure(mView.getWidth(), mView.getHeight(), 0, 0, 0, 0);
    }

    //PROGRESS BAR THICKNESS
    public float getProgressBarThickness() {
        return mProgressBarThickness;
    }

    public void setProgressBarThickness(float progressBarThickness) {
        if (this.mProgressBarThickness == progressBarThickness) return;
        this.mProgressBarThickness = progressBarThickness;
        mProgressPaint.setStrokeWidth(progressBarThickness);
        mesure(mView.getWidth(), mView.getHeight(), 0, 0, 0, 0);
    }

    //PROGRESS BAR STYLE
    public int getProgressBarStyle() {
        return (mProgressBarStyle == Paint.Cap.ROUND) ? CAP_ROUND : CAP_SQUARE;
    }

    public void setProgressBarStyle(int progressBarStyle) {
        if (progressBarStyle < 0 || progressBarStyle > 1) {
            throw new IllegalArgumentException("Text style must be a valid TextStyle constant.");
        }
        mProgressBarStyle = (progressBarStyle == CAP_ROUND) ? Paint.Cap.ROUND : Paint.Cap.BUTT;
        mProgressPaint.setStrokeCap(mProgressBarStyle);
    }
}
