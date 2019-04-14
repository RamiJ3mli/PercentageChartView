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
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;

import com.ramijemli.percentagechartview.IPercentageChartView;
import com.ramijemli.percentagechartview.callback.AdaptiveColorProvider;
import com.ramijemli.percentagechartview.callback.ProgressTextFormatter;

import androidx.annotation.Nullable;

public class PieModeRenderer extends BaseModeRenderer implements OrientationBasedMode, OffsetEnabledMode {

    private float mBgStartAngle;
    private float mBgSweepAngle;

    public PieModeRenderer(IPercentageChartView view) {
        super(view);
        setup();
    }

    public PieModeRenderer(IPercentageChartView view, TypedArray attrs) {
        super(view, attrs);
        setup();
    }

    private void setup() {
        mCircleBounds = new RectF();
        mBackgroundBounds = new RectF();
        mTextBounds = new Rect();
        mProvidedProgressColor = mProvidedBackgroundColor = mProvidedTextColor = -1;
        updateDrawingAngles();

        //BACKGROUND
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.FILL);

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

            if (mProgress > 0 && mProgress <= 100) {
                mTextProgress = (int) mProgress;
            } else if (mProgress > 100) {
                mProgress = mTextProgress = 100;
            } else {
                mProgress = mTextProgress = 0;
            }

            updateDrawingAngles();
            updateText();

            mView.onProgressUpdated(mProgress);
            mView.invalidate();
        });
    }

    @Override
    public void measure(int w, int h, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        float centerX = w * 0.5f;
        float centerY = h * 0.5f;
        float radius = Math.min(w, h) * 0.5f;

        mCircleBounds.left = centerX - radius;
        mCircleBounds.top = centerY - radius;
        mCircleBounds.right = centerX + radius;
        mCircleBounds.bottom = centerY + radius;
        measureBackgroundBounds();
        setupGradientColors(mCircleBounds);
    }

    @Override
    public void draw(Canvas canvas) {

        if (mGradientType == GRADIENT_SWEEP && mView.isInEditMode()) {
            // TO GET THE RIGHT DRAWING START ANGLE FOR SWEEP GRADIENT'S COLORS IN PREVIEW MODE
            canvas.save();
            canvas.rotate(mStartAngle, mCircleBounds.centerX(), mCircleBounds.centerY());
        }

        //FOREGROUND
        canvas.drawArc(mCircleBounds, mStartAngle, mSweepAngle, true, mProgressPaint);

        //BACKGROUND
        if (mDrawBackground) {
            canvas.drawArc(mBackgroundBounds, mBgStartAngle, mBgSweepAngle, true, mBackgroundPaint);
        }

        if (mGradientType == GRADIENT_SWEEP && mView.isInEditMode()) {
            // TO GET THE RIGHT DRAWING START ANGLE FOR SWEEP GRADIENT'S COLORS IN PREVIEW MODE
            canvas.restore();
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

        mProgressAnimator = mProgressColorAnimator = mBackgroundColorAnimator = mTextColorAnimator = null;
        mCircleBounds = null;
        mTextBounds = null;
        mBackgroundPaint = mProgressPaint = mTextPaint = null;
        gradient = null;

        mAdaptiveColorProvider = null;
        defaultTextFormatter = mProvidedTextFormatter = null;
    }

    private void measureBackgroundBounds() {
        mBackgroundBounds.left = mCircleBounds.left + mBackgroundOffset;
        mBackgroundBounds.top = mCircleBounds.top + mBackgroundOffset;
        mBackgroundBounds.right = mCircleBounds.right - mBackgroundOffset;
        mBackgroundBounds.bottom = mCircleBounds.bottom - mBackgroundOffset;
    }

    @Override
    public void setAdaptiveColorProvider(@Nullable AdaptiveColorProvider adaptiveColorProvider) {
        if (adaptiveColorProvider == null) {
            mProgressColorAnimator = mBackgroundColorAnimator = mTextColorAnimator = null;
            mAdaptiveColorProvider = null;
            mTextPaint.setColor(mTextColor);
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
    public void setTextFormatter(@Nullable ProgressTextFormatter textFormatter) {
        this.mProvidedTextFormatter = textFormatter;
        updateText();
        mView.invalidate();
    }

    @Override
    public void setProgress(float progress, boolean animate) {
        if (this.mProgress == progress) return;

        cancelAnimations();

        if (!animate) {
            this.mProgress = progress;
            this.mTextProgress = (int) progress;

            updateProvidedColors(progress);
            updateDrawingAngles();
            updateText();

            mView.onProgressUpdated(mProgress);
            mView.invalidate();
            return;
        }

        updateAnimations(progress);
    }

    private void setupGradientColors(RectF bounds) {
        if (mGradientType == -1) return;

        switch (mGradientType) {
            default:
            case GRADIENT_LINEAR:
                gradient = new LinearGradient(bounds.centerX(), bounds.top, bounds.centerX(), bounds.bottom, mGradientColors, mGradientDistributions, Shader.TileMode.CLAMP);
                updateGradientAngle(mGradientAngle);
                break;

            case GRADIENT_RADIAL:
                gradient = new RadialGradient(bounds.centerX(), bounds.centerY(), bounds.bottom - bounds.centerY(), mGradientColors, mGradientDistributions, Shader.TileMode.MIRROR);
                break;

            case GRADIENT_SWEEP:
                gradient = new SweepGradient(bounds.centerX(), bounds.centerY(), mGradientColors, mGradientDistributions);

                if (!mView.isInEditMode()) {
                    // THIS BREAKS SWEEP GRADIENT'S PREVIEW MODE
                    updateGradientAngle(mStartAngle);
                }
                break;
        }

        mProgressPaint.setShader(gradient);
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
    }

    private void updateAnimations(float progress) {
        mProgressAnimator.setFloatValues(mProgress, progress);
        mProgressAnimator.start();

        if (mAdaptiveColorProvider == null) return;


        int providedProgressColor = mAdaptiveColorProvider.provideProgressColor(progress);

        if (providedProgressColor != -1 && providedProgressColor != mProvidedProgressColor && mGradientType == -1) {
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
    }

    private void updateProvidedColors(float progress) {
        if (mAdaptiveColorProvider == null) return;


        int providedProgressColor = mAdaptiveColorProvider.provideProgressColor(progress);

        if (providedProgressColor != -1 && providedProgressColor != mProvidedProgressColor && mGradientType == -1) {
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
    }

    private void updateDrawingAngles() {
        switch (orientation) {
            case ORIENTATION_COUNTERCLOCKWISE:
                mSweepAngle = -(mProgress / DEFAULT_MAX * 360);
                mBgStartAngle = mStartAngle;
                mBgSweepAngle = 360 + mSweepAngle;
                break;

            default:
            case ORIENTATION_CLOCKWISE:
                mSweepAngle = mProgress / DEFAULT_MAX * 360;
                mBgStartAngle = mStartAngle + mSweepAngle;
                mBgSweepAngle = 360 - mSweepAngle;
                break;
        }
    }

    private void updateGradientAngle(float angle) {
        if (mGradientType == -1 || mGradientType == GRADIENT_RADIAL) return;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle, mCircleBounds.centerX(), mCircleBounds.centerY());
        gradient.setLocalMatrix(matrix);
    }

    @Override
    void updateText() {
        textValue = (mProvidedTextFormatter != null) ?
                mProvidedTextFormatter.provideFormattedText(mTextProgress) :
                defaultTextFormatter.provideFormattedText(mTextProgress);

        mTextPaint.getTextBounds(textValue, 0, textValue.length(), mTextBounds);
        textHeight = mTextBounds.height();
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
        updateDrawingAngles();
        if (mGradientType == GRADIENT_SWEEP) {
            updateGradientAngle(startAngle);
        }
    }

    @Override
    public void setGradientColors(int type, int[] colors, float[] positions, float angle) {
        mGradientType = type;
        mGradientColors = colors;
        mGradientDistributions = positions;
        setupGradientColors(mCircleBounds);
        if (mGradientType == GRADIENT_LINEAR && mGradientAngle != angle) {
            mGradientAngle = angle;
            updateGradientAngle(mGradientAngle);
        }
    }

    //BACKGROUND OFFSET
    public float getBackgroundOffset() {
        return mBackgroundOffset;
    }

    public void setBackgroundOffset(int backgroundOffset) {
        if (!mDrawBackground || this.mBackgroundOffset == backgroundOffset)
            return;
        this.mBackgroundOffset = backgroundOffset;
        measureBackgroundBounds();
    }
}
