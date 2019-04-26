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
import android.annotation.TargetApi;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.ramijemli.percentagechartview.IPercentageChartView;
import com.ramijemli.percentagechartview.callback.AdaptiveColorProvider;
import com.ramijemli.percentagechartview.callback.ProgressTextFormatter;

import androidx.annotation.Nullable;

public class FillModeRenderer extends BaseModeRenderer implements OffsetEnabledMode {

    private float mDirectionAngle;
    private float mBgSweepAngle;
    private float mRadius;

    public FillModeRenderer(IPercentageChartView view) {
        super(view);
        setup();
    }

    public FillModeRenderer(IPercentageChartView view, TypedArray attrs) {
        super(view, attrs);
        setup();
    }

    private void setup() {
        mCircleBounds = new RectF();
        mBackgroundBounds = new RectF();
        mTextBounds = new Rect();
        mProvidedProgressColor = mProvidedBackgroundColor = mProvidedTextColor = -1;
        this.mDirectionAngle = mStartAngle;

        //BACKGROUND
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(mBackgroundColor);

        //PROGRESS
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor(mProgressColor);

        //TEXT
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mTextLayout = DynamicLayout.Builder.obtain(mTextEditor, mTextPaint, Integer.MAX_VALUE)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0, 0)
                    .setJustificationMode(Layout.JUSTIFICATION_MODE_NONE)
                    .setBreakStrategy(Layout.HYPHENATION_FREQUENCY_NONE)
                    .setIncludePad(false)
                    .build();
        } else {
            mTextLayout = new DynamicLayout(mTextEditor,
                    mTextPaint,
                    Integer.MAX_VALUE,
                    Layout.Alignment.ALIGN_NORMAL,
                    0, 0,
                    false);
        }
    }

    @Override
    public void measure(int w, int h, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        int centerX = w / 2;
        int centerY = h / 2;
        mRadius = (float) Math.min(w, h) / 2;

        mCircleBounds.left = centerX - mRadius;
        mCircleBounds.top = centerY - mRadius;
        mCircleBounds.right = centerX + mRadius;
        mCircleBounds.bottom = centerY + mRadius;
        measureBackgroundBounds();
        updateDrawingAngles();
        setupGradientColors(mCircleBounds);
        updateText();
    }

    @Override
    public void draw(Canvas canvas) {
        //BACKGROUND
        if (mDrawBackground) {
            canvas.drawArc(mBackgroundBounds, mStartAngle, mBgSweepAngle, false, mBackgroundPaint);
        }

        //FOREGROUND
        canvas.drawArc(mCircleBounds, mStartAngle, mSweepAngle, false, mProgressPaint);

        //TEXT
        drawText(canvas);
    }

    private void drawText(Canvas canvas) {
        canvas.save();
        canvas.translate(mCircleBounds.centerX(), mCircleBounds.centerY() - (mTextLayout.getHeight() >> 1));
        mTextLayout.draw(canvas);
        canvas.restore();
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
            this.mAdaptiveColorProvider = null;
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
        if (mGradientType == -1 || mGradientType == GRADIENT_SWEEP) return;

        switch (mGradientType) {
            default:
            case GRADIENT_LINEAR:
                gradient = new LinearGradient(bounds.centerX(), bounds.top, bounds.centerX(), bounds.bottom, mGradientColors, mGradientDistributions, Shader.TileMode.CLAMP);
                updateGradientAngle(mGradientAngle);
                break;

            case GRADIENT_RADIAL:
                gradient = new RadialGradient(bounds.centerX(), bounds.centerY(), bounds.bottom - bounds.centerY(), mGradientColors, mGradientDistributions, Shader.TileMode.MIRROR);
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
        float height = mRadius - mProgress * (mRadius * 2) / DEFAULT_MAX;
        double radiusPow = Math.pow(mRadius, 2);
        double heightPow = Math.pow(height, 2);

        mSweepAngle = (height == 0) ? 180 : (float) Math.toDegrees(Math.acos((heightPow + radiusPow - Math.pow(Math.sqrt(radiusPow - heightPow), 2)) / (2 * height * mRadius))) * 2;
        mStartAngle = mDirectionAngle - (mSweepAngle / 2);
        mBgSweepAngle = (mBackgroundOffset > 0) ? 360 : mSweepAngle - 360;
    }

    private void updateGradientAngle(float angle) {
        if (mGradientType == -1 || mGradientType == GRADIENT_RADIAL) return;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle, mCircleBounds.centerX(), mCircleBounds.centerY());
        gradient.setLocalMatrix(matrix);
    }

    @Override
    void updateText() {
        if (mTextEditor != null) {
            CharSequence text = (mProvidedTextFormatter != null) ?
                    mProvidedTextFormatter.provideFormattedText(mTextProgress) :
                    defaultTextFormatter.provideFormattedText(mTextProgress);
            mTextEditor.clear();
            mTextEditor.append(text);
        }
    }

    @Override
    public void setStartAngle(float angle) {
        if (this.mDirectionAngle == angle) return;
        this.mDirectionAngle = angle;
        updateDrawingAngles();
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
        updateDrawingAngles();
    }
}
