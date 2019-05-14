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


import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;

import androidx.annotation.Nullable;

import com.ramijemli.percentagechartview.IPercentageChartView;
import com.ramijemli.percentagechartview.callback.AdaptiveColorProvider;


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

    @Override
    void setup() {
        super.setup();
        updateDrawingAngles();
    }

    @Override
    public void measure(int w, int h, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        float centerX = w * 0.5f;
        float centerY = h * 0.5f;
        float radius = Math.min(w, h) * 0.5f;

        mCircleBounds.set(centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius);
        measureBackgroundBounds();
        setupGradientColors(mCircleBounds);
        updateText();
    }

    private void measureBackgroundBounds() {
        mBackgroundBounds.set(mCircleBounds.left + mBackgroundOffset,
                mCircleBounds.top + mBackgroundOffset,
                mCircleBounds.right - mBackgroundOffset,
                mCircleBounds.bottom - mBackgroundOffset);
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
        drawText(canvas);
    }

    @Override
    public void setAdaptiveColorProvider(@Nullable AdaptiveColorProvider adaptiveColorProvider) {
        if (adaptiveColorProvider == null) {
            mProgressColorAnimator = mBackgroundColorAnimator = mTextColorAnimator = null;
            mAdaptiveColorProvider = null;
            mTextPaint.setColor(mTextColor);
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
        if (mGradientType == -1 && bounds.height() == 0) return;

        switch (mGradientType) {
            default:
            case GRADIENT_LINEAR:
                mGradientShader = new LinearGradient(bounds.centerX(), bounds.top, bounds.centerX(), bounds.bottom, mGradientColors, mGradientDistributions, Shader.TileMode.CLAMP);
                updateGradientAngle(mGradientAngle);
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
    void updateDrawingAngles() {
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
        updateDrawingAngles();
        if (mGradientType == GRADIENT_SWEEP) {
            updateGradientAngle(startAngle);
        }
    }

    //BACKGROUND OFFSET
    public int getBackgroundOffset() {
        return mBackgroundOffset;
    }

    public void setBackgroundOffset(int backgroundOffset) {
        if (!mDrawBackground || this.mBackgroundOffset == backgroundOffset)
            return;
        this.mBackgroundOffset = backgroundOffset;
        measureBackgroundBounds();
    }

}
