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
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.TypedValue;

import androidx.annotation.Nullable;

import com.ramijemli.percentagechartview.IPercentageChartView;
import com.ramijemli.percentagechartview.R;
import com.ramijemli.percentagechartview.callback.AdaptiveColorProvider;


public class RingModeRenderer extends BaseModeRenderer implements OrientationBasedMode {

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

    //TO PUSH PROGRESS BAR OUT OF SWEEP GRADIENT'S WAY
    private float tweakAngle;

    public RingModeRenderer(IPercentageChartView view) {
        super(view);
        init();
        setup();
    }

    public RingModeRenderer(IPercentageChartView view, TypedArray attrs) {
        super(view, attrs);
        init(attrs);
        setup();
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
    }

    @Override
    void setup() {
        super.setup();
        mProvidedBgBarColor = -1;
        tweakAngle = 0;
        updateDrawingAngles();

        //BACKGROUND BAR
        mBackgroundBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundBarPaint.setStyle(Paint.Style.STROKE);
        mBackgroundBarPaint.setColor(mBackgroundBarColor);
        mBackgroundBarPaint.setStrokeWidth(mBackgroundBarThickness);
        mBackgroundBarPaint.setStrokeCap(mProgressBarStyle);

        //PROGRESS PAINT
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressBarThickness);
        mProgressPaint.setStrokeCap(mProgressBarStyle);
    }

    @Override
    public void measure(int w, int h, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        int diameter = Math.min(w, h);
        float maxOffset = Math.max(mProgressBarThickness, mBackgroundBarThickness);

        int centerX = w / 2;
        int centerY = h / 2;
        float radius = (diameter - maxOffset) / 2;

        mCircleBounds.set(centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius);

        float backgroundRadius = radius - (mBackgroundBarThickness / 2) + 1;

        mBackgroundBounds.set(centerX - backgroundRadius,
                centerY - backgroundRadius,
                centerX + backgroundRadius,
                centerY + backgroundRadius);

        setupGradientColors(mCircleBounds);
        updateText();
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
                canvas.drawArc(mCircleBounds, mStartAngle + tweakAngle, -(360 - mSweepAngle + tweakAngle), false, mBackgroundBarPaint);
            } else {
                canvas.drawArc(mCircleBounds, 0, 360, false, mBackgroundBarPaint);
            }
        }

        //FOREGROUND
        if (mProgress != 0) {
            canvas.drawArc(mCircleBounds, mStartAngle + tweakAngle, mSweepAngle, false, mProgressPaint);
        }

        //TEXT
        drawText(canvas);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mBgBarColorAnimator != null) {
            if (mBgBarColorAnimator.isRunning()) {
                mBgBarColorAnimator.cancel();
            }
            mBgBarColorAnimator.removeAllUpdateListeners();
        }
        mBgBarColorAnimator = null;
        mBackgroundBarPaint = null;
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
            mView.postInvalidate();
            return;
        }

        this.mAdaptiveColorProvider = adaptiveColorProvider;

        setupColorAnimations();
        updateProvidedColors(mProgress);
        mView.postInvalidate();
    }

    @Override
    void setupGradientColors(RectF bounds) {
        if (mGradientType == -1) return;

        double ab = Math.pow(bounds.bottom - bounds.centerY(), 2);
        tweakAngle = (float) Math.toDegrees(Math.acos((2 * ab - Math.pow(mProgressBarThickness / 2, 2)) / (2 * ab)));

        switch (mGradientType) {
            default:
            case GRADIENT_LINEAR:
                mGradientShader = new LinearGradient(bounds.centerX(), bounds.top, bounds.centerX(), bounds.bottom, mGradientColors, mGradientDistributions, Shader.TileMode.CLAMP);
                updateGradientAngle(mStartAngle);
                break;

            case GRADIENT_RADIAL:
                mGradientShader = new RadialGradient(bounds.centerX(), bounds.centerY(), bounds.bottom - bounds.centerY(), mGradientColors, mGradientDistributions, Shader.TileMode.MIRROR);
                break;

            case GRADIENT_SWEEP:
                mGradientShader = new SweepGradient(bounds.centerX(), bounds.centerY(), mGradientColors, mGradientDistributions);

                if (!mView.isInEditMode()) {
                    // THIS BREAKS SWEEP GRADIENT'S PREVIEW MODE
                    updateGradientAngle(mStartAngle);
                }
                break;
        }

        mProgressPaint.setShader(mGradientShader);
    }

    @Override
    void setupColorAnimations() {
        super.setupColorAnimations();
        if (mBgBarColorAnimator == null) {
            mBgBarColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mBackgroundBarColor, mProvidedBgBarColor);
            mBgBarColorAnimator.addUpdateListener(animation -> {
                mProvidedBgBarColor = (int) animation.getAnimatedValue();
                mBackgroundBarPaint.setColor(mProvidedBgBarColor);
            });
            mBgBarColorAnimator.setDuration(mAnimDuration);
        }
    }

    @Override
    void cancelAnimations() {
        super.cancelAnimations();
        if (mBgBarColorAnimator != null && mBgBarColorAnimator.isRunning()) {
            mBgBarColorAnimator.cancel();
        }
    }

    @Override
    void updateAnimations(float progress) {
        super.updateAnimations(progress);

        if (mAdaptiveColorProvider == null) return;

        int providedBgBarColor = mAdaptiveColorProvider.provideBackgroundBarColor(progress);
        if (providedBgBarColor != -1 && providedBgBarColor != mProvidedBgBarColor) {
            int startColor = mProvidedBgBarColor != -1 ? mProvidedBgBarColor : mBackgroundBarColor;
            mBgBarColorAnimator.setIntValues(startColor, providedBgBarColor);
            mBgBarColorAnimator.start();
        }
    }

    @Override
    void updateProvidedColors(float progress) {
        super.updateProvidedColors(progress);
        if (mAdaptiveColorProvider == null) return;
        int providedBgBarColor = mAdaptiveColorProvider.provideBackgroundBarColor(progress);
        if (providedBgBarColor != -1 && providedBgBarColor != mProvidedBgBarColor) {
            mProvidedBgBarColor = providedBgBarColor;
            mBackgroundBarPaint.setColor(mProvidedBgBarColor);
        }
    }

    @Override
    void updateDrawingAngles() {
        switch (orientation) {
            case ORIENTATION_COUNTERCLOCKWISE:
                mSweepAngle = -(mProgress / DEFAULT_MAX * 360);
                break;

            default:
            case ORIENTATION_CLOCKWISE:
                mSweepAngle = mProgress / DEFAULT_MAX * 360;
                break;
        }
    }

    @Override
    void updateGradientAngle(float angle) {
        if (mGradientType == -1 || mGradientType == GRADIENT_RADIAL) return;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle, mCircleBounds.centerX(), mCircleBounds.centerY());
        mGradientShader.setLocalMatrix(matrix);
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        if (this.orientation == orientation) return;
        this.orientation = orientation;
        updateDrawingAngles();
    }

    @Override
    public void setStartAngle(float startAngle) {
        if (this.mStartAngle == startAngle) return;
        this.mStartAngle = startAngle;
        if (mGradientType == GRADIENT_SWEEP) {
            updateGradientAngle(startAngle);
        }
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
        measure(mView.getWidth(), mView.getHeight(), 0, 0, 0, 0);
    }

    //PROGRESS BAR THICKNESS
    public float getProgressBarThickness() {
        return mProgressBarThickness;
    }

    public void setProgressBarThickness(float progressBarThickness) {
        if (this.mProgressBarThickness == progressBarThickness) return;
        this.mProgressBarThickness = progressBarThickness;
        mProgressPaint.setStrokeWidth(progressBarThickness);
        measure(mView.getWidth(), mView.getHeight(), 0, 0, 0, 0);
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
