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

import com.ramijemli.percentagechartview.IPercentageChartView;
import com.ramijemli.percentagechartview.PercentageChartView;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

public class PieModeRenderer extends BaseModeRenderer {

    float mBgStartAngle;
    float mBgSweepAngle;

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
        mSweepAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ?
                -(this.mProgress / DEFAULT_MAX * 360) :
                this.mProgress / DEFAULT_MAX * 360;
        mBgStartAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ? mStartAngle : mStartAngle + mSweepAngle;
        mBgSweepAngle = 360 - ((orientation == ORIENTATION_COUNTERCLOCKWISE) ? -(mSweepAngle) : mSweepAngle);
        mAdaptiveColor = -1;

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
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mProgress = (float) valueAnimator.getAnimatedValue();

                if (mProgress > 0 && mProgress <= 100) {
                    mTextProgress = (int) mProgress;
                } else if (mProgress > 100) {
                    mProgress = mTextProgress = 100;
                } else {
                    mProgress = mTextProgress = 0;
                }

                mSweepAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ?
                        -(mProgress / DEFAULT_MAX * 360) :
                        mProgress / DEFAULT_MAX * 360;
                mBgStartAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ? mStartAngle : mStartAngle + mSweepAngle;
                mBgSweepAngle = 360 - ((orientation == ORIENTATION_COUNTERCLOCKWISE) ? -(mSweepAngle) : mSweepAngle);

                updateText();
                mView.onProgressUpdated(mProgress);
                mView.invalidate();
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

        mBackgroundBounds.left = mCircleBounds.left + mBackgroundOffset;
        mBackgroundBounds.top = mCircleBounds.top + mBackgroundOffset;
        mBackgroundBounds.right = mCircleBounds.right - mBackgroundOffset;
        mBackgroundBounds.bottom = mCircleBounds.bottom - mBackgroundOffset;
    }

    @Override
    public void draw(Canvas canvas) {
        //FOREGROUND
        canvas.drawArc(mCircleBounds, mStartAngle, mSweepAngle, true, mProgressPaint);

        //BACKGROUND
        if (mDrawBackground) {
            canvas.drawArc(mBackgroundBounds, mBgStartAngle, mBgSweepAngle, true, mBackgroundPaint);
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
        mCircleBounds = null;
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
            mAdaptBackground = mAdaptText = false;
            mTextPaint.setColor(mTextColor);
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
            mBgStartAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ? mStartAngle : mStartAngle + mSweepAngle;
            mBgSweepAngle = 360 - ((orientation == ORIENTATION_COUNTERCLOCKWISE) ? -(mSweepAngle) : mSweepAngle);

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

    void updateAdaptiveColors(int targetColor) {
        mAdaptiveColor = targetColor;
        mProgressPaint.setColor(mAdaptiveColor);

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
        if(this.orientation ==  orientation) return;
        this.orientation = orientation;
        mSweepAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ?
                -(mProgress / DEFAULT_MAX * 360) :
                mProgress / DEFAULT_MAX * 360;
        mBgStartAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ? mStartAngle : mStartAngle + mSweepAngle;
        mBgSweepAngle = 360 - ((orientation == ORIENTATION_COUNTERCLOCKWISE) ? -(mSweepAngle) : mSweepAngle);
        mView.invalidate();
    }

    @Override
    public void setStartAngle(float startAngle) {
        if (this.mStartAngle == startAngle) return;
        this.mStartAngle = startAngle;
        mBgStartAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ? mStartAngle : mStartAngle + mSweepAngle;
        mBgSweepAngle = 360 - ((orientation == ORIENTATION_COUNTERCLOCKWISE) ? -(mSweepAngle) : mSweepAngle);
        mView.invalidate();
    }

    @Override
    void updateText() {
        textValue = String.valueOf(mTextProgress) + "%";
        mTextPaint.getTextBounds(textValue, 0, textValue.length(), mTextBounds);
        textHeight = mTextBounds.height();
    }

    //ADAPTIVE BACKGROUND
    @Override
    public void setAdaptiveBgEnabled(boolean enable) {
        if (mAdaptiveColorProvider == null || !mDrawBackground || mAdaptBackground == enable) return;
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
}
