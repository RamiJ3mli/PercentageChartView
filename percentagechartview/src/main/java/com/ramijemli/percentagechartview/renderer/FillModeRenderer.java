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

import androidx.annotation.Nullable;

import com.ramijemli.percentagechartview.IPercentageChartView;
import com.ramijemli.percentagechartview.callback.AdaptiveColorProvider;

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

    @Override
    void setup() {
        super.setup();
        this.mDirectionAngle = mStartAngle;
    }

    @Override
    public void measure(int w, int h, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
        int centerX = w / 2;
        int centerY = h / 2;
        mRadius = (float) Math.min(w, h) / 2;

        mCircleBounds.set(centerX - mRadius,
                centerY - mRadius,
                centerX + mRadius,
                centerY + mRadius);
        measureBackgroundBounds();
        updateDrawingAngles();
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
        //BACKGROUND
        if (mDrawBackground) {
            canvas.drawArc(mBackgroundBounds, mStartAngle, mBgSweepAngle, false, mBackgroundPaint);
        }

        //FOREGROUND
        canvas.drawArc(mCircleBounds, mStartAngle, mSweepAngle, false, mProgressPaint);

        //TEXT
        drawText(canvas);
    }

    @Override
    public void setAdaptiveColorProvider(@Nullable AdaptiveColorProvider adaptiveColorProvider) {
        if (adaptiveColorProvider == null) {
            mProgressColorAnimator = mBackgroundColorAnimator = mTextColorAnimator = null;
            this.mAdaptiveColorProvider = null;
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
        if (mGradientType == -1 || mGradientType == GRADIENT_SWEEP) return;

        switch (mGradientType) {
            default:
            case GRADIENT_LINEAR:
                mGradientShader = new LinearGradient(bounds.centerX(), bounds.top, bounds.centerX(), bounds.bottom, mGradientColors, mGradientDistributions, Shader.TileMode.CLAMP);
                updateGradientAngle(mGradientAngle);
                break;

            case GRADIENT_RADIAL:
                mGradientShader = new RadialGradient(bounds.centerX(), bounds.centerY(), bounds.bottom - bounds.centerY(), mGradientColors, mGradientDistributions, Shader.TileMode.MIRROR);
                break;
        }

        mProgressPaint.setShader(mGradientShader);
    }

    @Override
    void updateDrawingAngles() {
        float height = mRadius - mProgress * (mRadius * 2) / DEFAULT_MAX;
        double radiusPow = Math.pow(mRadius, 2);
        double heightPow = Math.pow(height, 2);

        mSweepAngle = (height == 0) ? 180 : (float) Math.toDegrees(Math.acos((heightPow + radiusPow - Math.pow(Math.sqrt(radiusPow - heightPow), 2)) / (2 * height * mRadius))) * 2;
        mStartAngle = mDirectionAngle - (mSweepAngle / 2);
        mBgSweepAngle = (mBackgroundOffset > 0) ? 360 : mSweepAngle - 360;
    }

    @Override
    void updateGradientAngle(float angle) {
        if (mGradientType == -1 || mGradientType == GRADIENT_RADIAL) return;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle, mCircleBounds.centerX(), mCircleBounds.centerY());
        mGradientShader.setLocalMatrix(matrix);
    }

    @Override
    public float getStartAngle() {
        return mDirectionAngle;
    }

    @Override
    public void setStartAngle(float angle) {
        if (this.mDirectionAngle == angle) return;
        this.mDirectionAngle = angle;
        updateDrawingAngles();
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
        updateDrawingAngles();
    }
}
