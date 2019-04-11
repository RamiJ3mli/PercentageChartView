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
import com.ramijemli.percentagechartview.callback.AdaptiveColorProvider;

import androidx.annotation.Nullable;

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
        mProvidedProgressColor = mProvidedBackgroundColor = mProvidedTextColor = -1;

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

            mSweepAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ?
                    -(mProgress / DEFAULT_MAX * 360) :
                    mProgress / DEFAULT_MAX * 360;
            mBgStartAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ? mStartAngle : mStartAngle + mSweepAngle;
            mBgSweepAngle = 360 - ((orientation == ORIENTATION_COUNTERCLOCKWISE) ? -(mSweepAngle) : mSweepAngle);

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//                    mProgressColorAnimator.setCurrentFraction(mProgressAnimator.getAnimatedFraction());
//                }
            updateText();
            mView.onProgressUpdated(mProgress);
            mView.invalidate();
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
        mesureBackgroundBounds();
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

        if (mAdaptiveColorProvider != null) {
            mAdaptiveColorProvider = null;
        }
    }

    private void mesureBackgroundBounds() {
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
            mBgStartAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ? mStartAngle : mStartAngle + mSweepAngle;
            mBgSweepAngle = 360 - ((orientation == ORIENTATION_COUNTERCLOCKWISE) ? -(mSweepAngle) : mSweepAngle);

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
    }

    @Override
    public void setOrientation(int orientation) {
        if (this.orientation == orientation) return;
        this.orientation = orientation;
        mSweepAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ?
                -(mProgress / DEFAULT_MAX * 360) :
                mProgress / DEFAULT_MAX * 360;
        mBgStartAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ? mStartAngle : mStartAngle + mSweepAngle;
        mBgSweepAngle = 360 - ((orientation == ORIENTATION_COUNTERCLOCKWISE) ? -(mSweepAngle) : mSweepAngle);
    }

    @Override
    public void setStartAngle(float startAngle) {
        if (this.mStartAngle == startAngle) return;
        this.mStartAngle = startAngle;
        mBgStartAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ? mStartAngle : mStartAngle + mSweepAngle;
        mBgSweepAngle = 360 - ((orientation == ORIENTATION_COUNTERCLOCKWISE) ? -(mSweepAngle) : mSweepAngle);
    }

    @Override
    void updateText() {
        textValue = String.valueOf(mTextProgress) + "%";
        mTextPaint.getTextBounds(textValue, 0, textValue.length(), mTextBounds);
        textHeight = mTextBounds.height();
    }

    //BACKGROUND OFFSET
    public float getBackgroundOffset() {
        return mBackgroundOffset;
    }

    public void setBackgroundOffset(int backgroundOffset) {
        if (!mDrawBackground || this.mBackgroundOffset == backgroundOffset)
            return;
        this.mBackgroundOffset = backgroundOffset;
        mesureBackgroundBounds();
    }
}
