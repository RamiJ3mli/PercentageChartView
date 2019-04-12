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

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.TypedValue;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.ramijemli.percentagechartview.IPercentageChartView;
import com.ramijemli.percentagechartview.R;
import com.ramijemli.percentagechartview.annotation.ProgressOrientation;
import com.ramijemli.percentagechartview.callback.AdaptiveColorProvider;
import com.ramijemli.percentagechartview.callback.ProgressTextFormatter;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

public abstract class BaseModeRenderer {


    // CHART MODE
    public static final int MODE_RING = 0;
    public static final int MODE_PIE = 1;
    public static final int MODE_FILL = 2;

    // ORIENTATION
    public static final int INVALID_ORIENTATION = -1;
    public static final int ORIENTATION_CLOCKWISE = 0;
    public static final int ORIENTATION_COUNTERCLOCKWISE = 1;

    // TEXT
    private static final float DEFAULT_TEXT_SP_SIZE = 12;

    //ANIMATIONS
    private static final int DEFAULT_ANIMATION_INTERPOLATOR = 0;
    public static final int LINEAR = 0;
    public static final int ACCELERATE = 1;
    public static final int DECELERATE = 2;
    public static final int ACCELERATE_DECELERATE = 3;
    public static final int ANTICIPATE = 4;
    public static final int OVERSHOOT = 5;
    public static final int ANTICIPATE_OVERSHOOT = 6;
    public static final int BOUNCE = 7;
    public static final int FAST_OUT_LINEAR_IN = 8;
    public static final int FAST_OUT_SLOW_IN = 9;
    public static final int LINEAR_OUT_SLOW_IN = 10;

    private static final int DEFAULT_START_ANGLE = 0;
    private static final int DEFAULT_ANIMATION_DURATION = 400;
    static final float DEFAULT_MAX = 100;

    //##############################################################################################
    // BACKGROUND
    boolean mDrawBackground;
    Paint mBackgroundPaint;
    int mBackgroundColor;
    int mBackgroundOffset;

    int mProvidedBackgroundColor;

    // PROGRESS
    Paint mProgressPaint;
    int mProgressColor;

    // TEXT
    Rect mTextBounds;
    Paint mTextPaint;
    int mTextColor;
    int mProvidedTextColor;
    int mTextProgress;
    float mTextSize;
    private int mTextStyle;
    Typeface mTypeface;
    int mTextShadowColor;
    float mTextShadowRadius;
    float mTextShadowDistY;
    float mTextShadowDistX;
    int textHeight;
    String textValue;

    // COMMON
    RectF mBackgroundBounds;
    RectF mCircleBounds;
    ValueAnimator mProgressColorAnimator, mBackgroundColorAnimator, mTextColorAnimator, mBgBarColorAnimator;
    ValueAnimator mProgressAnimator;
    Interpolator mAnimInterpolator;
    int mAnimDuration;
    float mProgress;
    float mStartAngle;
    float mSweepAngle;

    int mProvidedProgressColor;

    @ProgressOrientation
    int orientation;
    @Nullable
    AdaptiveColorProvider mAdaptiveColorProvider;
    @Nullable
    ProgressTextFormatter mProvidedTextFormatter, defaultTextFormatter;

    IPercentageChartView mView;


    BaseModeRenderer(IPercentageChartView view) {
        mView = view;

        //DRAWING ORIENTATION
        orientation = ORIENTATION_CLOCKWISE;

        //START DRAWING ANGLE
        mStartAngle = DEFAULT_START_ANGLE;

        //BACKGROUND DRAW STATE
        mDrawBackground = this instanceof PieModeRenderer;

        //BACKGROUND COLOR
        mBackgroundColor = Color.BLACK;

        //PROGRESS
        mProgress = mTextProgress = 0;

        //PROGRESS COLOR
        mProgressColor = Color.RED;

        //PROGRESS ANIMATION DURATION
        mAnimDuration = DEFAULT_ANIMATION_DURATION;

        //PROGRESS ANIMATION INTERPOLATOR
        mAnimInterpolator = new LinearInterpolator();

        //TEXT COLOR
        mTextColor = Color.WHITE;

        //TEXT SIZE
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                DEFAULT_TEXT_SP_SIZE,
                mView.getViewContext().getResources().getDisplayMetrics());

        //TEXT STYLE
        mTextStyle = Typeface.NORMAL;

        //TEXT SHADOW
        mTextShadowColor = Color.TRANSPARENT;
        mTextShadowRadius = 0;
        mTextShadowDistX = 0;
        mTextShadowDistY = 0;

        //BACKGROUND OFFSET
        mBackgroundOffset = 0;

        //TEXT FORMATTER
        defaultTextFormatter = progress -> (int)progress + "%";
    }

    BaseModeRenderer(IPercentageChartView view, TypedArray attrs) {
        mView = view;

        //DRAWING ORIENTATION
        orientation = attrs.getInt(R.styleable.PercentageChartView_pcv_orientation, ORIENTATION_CLOCKWISE);

        //START DRAWING ANGLE
        mStartAngle = attrs.getInt(R.styleable.PercentageChartView_pcv_startAngle, DEFAULT_START_ANGLE);
        if (mStartAngle < 0 || mStartAngle > 360) {
            mStartAngle = DEFAULT_START_ANGLE;
        }

        //BACKGROUND DRAW STATE
        mDrawBackground = attrs.getBoolean(R.styleable.PercentageChartView_pcv_drawBackground, (this instanceof PieModeRenderer || this instanceof FillModeRenderer));

        //BACKGROUND COLOR
        mBackgroundColor = attrs.getColor(R.styleable.PercentageChartView_pcv_backgroundColor, Color.BLACK);

        //PROGRESS
        mProgress = attrs.getFloat(R.styleable.PercentageChartView_pcv_progress, 0);
        if (mProgress < 0) {
            mProgress = 0;
        } else if (mProgress > 100) {
            mProgress = 100;
        }
        mTextProgress = (int) mProgress;

        //PROGRESS COLOR
        mProgressColor = attrs.getColor(R.styleable.PercentageChartView_pcv_progressColor, getThemeAccentColor());

        //PROGRESS ANIMATION DURATION
        mAnimDuration = attrs.getInt(R.styleable.PercentageChartView_pcv_animDuration, DEFAULT_ANIMATION_DURATION);

        //PROGRESS ANIMATION INTERPOLATOR
        int interpolator = attrs.getInt(R.styleable.PercentageChartView_pcv_animInterpolator, DEFAULT_ANIMATION_INTERPOLATOR);
        switch (interpolator) {
            default:
                mAnimInterpolator = new LinearInterpolator();
                break;
            case ACCELERATE:
                mAnimInterpolator = new AccelerateInterpolator();
                break;
            case DECELERATE:
                mAnimInterpolator = new DecelerateInterpolator();
                break;
            case ACCELERATE_DECELERATE:
                mAnimInterpolator = new AccelerateDecelerateInterpolator();
                break;
            case ANTICIPATE:
                mAnimInterpolator = new AnticipateInterpolator();
                break;
            case OVERSHOOT:
                mAnimInterpolator = new OvershootInterpolator();
                break;
            case ANTICIPATE_OVERSHOOT:
                mAnimInterpolator = new AnticipateOvershootInterpolator();
                break;
            case BOUNCE:
                mAnimInterpolator = new BounceInterpolator();
                break;
            case FAST_OUT_LINEAR_IN:
                mAnimInterpolator = new FastOutLinearInInterpolator();
                break;
            case FAST_OUT_SLOW_IN:
                mAnimInterpolator = new FastOutSlowInInterpolator();
                break;
            case LINEAR_OUT_SLOW_IN:
                mAnimInterpolator = new LinearOutSlowInInterpolator();
                break;
        }

        //TEXT COLOR
        mTextColor = attrs.getColor(R.styleable.PercentageChartView_pcv_textColor, Color.WHITE);

        //TEXT SIZE
        mTextSize = attrs.getDimensionPixelSize(
                R.styleable.PercentageChartView_pcv_textSize,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                        DEFAULT_TEXT_SP_SIZE,
                        mView.getViewContext().getResources().getDisplayMetrics()
                ));

        //TEXT TYPEFACE
        String typeface = attrs.getString(R.styleable.PercentageChartView_pcv_typeface);
        if (typeface != null && !typeface.isEmpty()) {
            mTypeface = Typeface.createFromAsset(mView.getViewContext().getResources().getAssets(), typeface);
        }

        //TEXT STYLE
        mTextStyle = attrs.getInt(R.styleable.PercentageChartView_pcv_textStyle, Typeface.NORMAL);
        if (mTextStyle > 0) {
            mTypeface = (mTypeface == null) ? Typeface.defaultFromStyle(mTextStyle) : Typeface.create(mTypeface, mTextStyle);
        }

        //TEXT SHADOW
        mTextShadowColor = attrs.getColor(R.styleable.PercentageChartView_pcv_textShadowColor, Color.TRANSPARENT);
        if (mTextShadowColor != Color.TRANSPARENT) {
            mTextShadowRadius = attrs.getFloat(R.styleable.PercentageChartView_pcv_textShadowRadius, 0);
            mTextShadowDistX = attrs.getFloat(R.styleable.PercentageChartView_pcv_textShadowDistX, 0);
            mTextShadowDistY = attrs.getFloat(R.styleable.PercentageChartView_pcv_textShadowDistY, 0);
        }

        //BACKGROUND OFFSET
        mBackgroundOffset = attrs.getDimensionPixelSize(
                R.styleable.PercentageChartView_pcv_backgroundOffset,
                0);

        //TEXT FORMATTER
        defaultTextFormatter = progress -> (int)progress + "%";
    }

    //############################################################################################## BEHAVIOR
    public abstract void mesure(int w, int h, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom);

    public abstract void draw(Canvas canvas);

    public abstract void destroy();

    abstract void updateText();

    public abstract void setStartAngle(float startAngle);

    private int getThemeAccentColor() {
        int colorAttr;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            colorAttr = android.R.attr.colorAccent;
        } else {
            colorAttr = mView.getViewContext().getResources().getIdentifier("colorAccent",
                    "attr",
                    mView.getViewContext().getPackageName()
            );
        }
        TypedValue outValue = new TypedValue();
        mView.getViewContext().getTheme().resolveAttribute(colorAttr, outValue, true);
        return outValue.data;
    }

    //############################################################################################## MODIFIERS
    public abstract void setAdaptiveColorProvider(@Nullable AdaptiveColorProvider adaptiveColorProvider);

    public void setTextFormatter(@Nullable ProgressTextFormatter textFormatter) {
        this.mProvidedTextFormatter = textFormatter;
        mView.invalidate();
    }

    //PROGRESS
    public float getProgress() {
        return mProgress;
    }

    public abstract void setProgress(float progress, boolean animate);

    //DRAW BACKGROUND STATE
    public boolean isDrawBackgroundEnabled() {
        return mDrawBackground;
    }

    public void setDrawBackgroundEnabled(boolean drawBackground) {
        if (this.mDrawBackground == drawBackground) return;
        this.mDrawBackground = drawBackground;
    }

    //START ANGLE
    public float getStartAngle() {
        return mStartAngle;
    }

    //BACKGROUND COLOR
    public int getBackgroundColor() {
        if (!mDrawBackground) return -1;
        return mBackgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        if ((mAdaptiveColorProvider != null && mAdaptiveColorProvider.provideBackgroundColor(mProgress) != -1) || this.mBackgroundColor == backgroundColor)
            return;
        this.mBackgroundColor = backgroundColor;
        if (!mDrawBackground) return;
        mBackgroundPaint.setColor(mBackgroundColor);
    }

    //PROGRESS COLOR
    public int getProgressColor() {
        return mProgressColor;
    }

    public void setProgressColor(int progressColor) {
        if ((mAdaptiveColorProvider != null && mAdaptiveColorProvider.provideProgressColor(mProgress) != -1) || this.mProgressColor == progressColor)
            return;

        this.mProgressColor = progressColor;
        mProgressPaint.setColor(progressColor);
    }


    //ANIMATION DURATION
    public int getAnimationDuration() {
        return mAnimDuration;
    }

    public void setAnimationDuration(int duration) {
        if (this.mAnimDuration == duration) return;
        mAnimDuration = duration;
        mProgressAnimator.setDuration(mAnimDuration);
        if (mProgressColorAnimator != null) {
            mProgressColorAnimator.setDuration(mAnimDuration);
        }
        if (mBackgroundColorAnimator != null) {
            mBackgroundColorAnimator.setDuration(mAnimDuration);
        }
        if (mTextColorAnimator != null) {
            mTextColorAnimator.setDuration(mAnimDuration);
        }
        if (mBgBarColorAnimator != null) {
            mBgBarColorAnimator.setDuration(mAnimDuration);
        }
    }

    //ANIMATION INTERPOLATOR
    public TimeInterpolator getAnimationInterpolator() {
        return mProgressAnimator.getInterpolator();
    }

    public void setAnimationInterpolator(TimeInterpolator interpolator) {
        mProgressAnimator.setInterpolator(interpolator);
    }

    //TEXT COLOR
    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(@ColorInt int textColor) {
        if ((mAdaptiveColorProvider != null && mAdaptiveColorProvider.provideTextColor(mProgress) != -1) || this.mTextColor == textColor)
            return;
        this.mTextColor = textColor;
        mTextPaint.setColor(textColor);
    }

    //TEXT SIZE
    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float textSize) {
        if (this.mTextSize == textSize) return;
        this.mTextSize = textSize;
        mTextPaint.setTextSize(textSize);
        updateText();
    }

    //TEXT TYPEFACE
    public Typeface getTypeface() {
        return mTypeface;
    }

    public void setTypeface(Typeface typeface) {
        if (this.mTypeface != null && this.mTypeface.equals(typeface)) return;
        this.mTypeface = (mTextStyle > 0) ?
                Typeface.create(typeface, mTextStyle) :
                typeface;
        mTextPaint.setTypeface(mTypeface);
        updateText();
    }

    //TEXT STYLE
    public int getTextStyle() {
        return mTextStyle;
    }

    public void setTextStyle(int mTextStyle) {
        if (this.mTextStyle == mTextStyle) return;
        this.mTextStyle = mTextStyle;
        mTypeface = (mTypeface == null) ? Typeface.defaultFromStyle(mTextStyle) : Typeface.create(mTypeface, mTextStyle);

        mTextPaint.setTypeface(mTypeface);
        updateText();
    }

    //TEXT SHADOW
    public int getTextShadowColor() {
        return mTextShadowColor;
    }

    public float getTextShadowRadius() {
        return mTextShadowRadius;
    }

    public float getTextShadowDistY() {
        return mTextShadowDistY;
    }

    public float getTextShadowDistX() {
        return mTextShadowDistX;
    }

    public void setTextShadow(int shadowColor, float shadowRadius, float shadowDistX, float shadowDistY) {
        if (this.mTextShadowColor == shadowColor
                && this.mTextShadowRadius == shadowRadius
                && this.mTextShadowDistX == shadowDistX
                && this.mTextShadowDistY == shadowDistY) return;
        this.mTextShadowColor = shadowColor;
        this.mTextShadowRadius = shadowRadius;
        this.mTextShadowDistX = shadowDistX;
        this.mTextShadowDistY = shadowDistY;

        mTextPaint.setShadowLayer(mTextShadowRadius, mTextShadowDistX, mTextShadowDistY, mTextShadowColor);
        updateText();
    }
}