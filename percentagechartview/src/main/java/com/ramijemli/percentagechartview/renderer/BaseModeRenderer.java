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
import com.ramijemli.percentagechartview.PercentageChartView;
import com.ramijemli.percentagechartview.R;
import com.ramijemli.percentagechartview.annotation.ProgressOrientation;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

public abstract class BaseModeRenderer {

    // CHART MODE
    public static final int MODE_RING = 0;
    public static final int MODE_PIE = 1;

    // ORIENTATION
    public static final int ORIENTATION_CLOCKWISE = 0;
    public static final int ORIENTATION_COUNTERCLOCKWISE = 1;

    // TEXT
    private static float DEFAULT_TEXT_SP_SIZE = 16;

    //ADAPTIVE MODES
    public static final int DARKER_MODE = 0;
    public static final int LIGHTER_MODE = 1;

    //ANIMATIONS
    private static final int DEFAULT_ANIMATION_INTERPOLATOR = 0;
    private static final int LINEAR = 0;
    private static final int ACCELERATE = 1;
    private static final int DECELERATE = 2;
    private static final int ACCELERATE_DECELERATE = 3;
    private static final int ANTICIPATE = 4;
    private static final int OVERSHOOT = 5;
    private static final int ANTICIPATE_OVERSHOOT = 6;
    private static final int BOUNCE = 7;
    private static final int FAST_OUT_LINEAR_IN = 8;
    private static final int FAST_OUT_SLOW_IN = 9;
    private static final int LINEAR_OUT_SLOW_IN = 10;

    private static final int DEFAULT_START_ANGLE = 0;
    private static final int DEFAULT_ANIMATION_DURATION = 1000;
    static final float DEFAULT_MAX = 100;

    //##############################################################################################
    // BACKGROUND
    boolean drawBackground;
    Paint mBackgroundPaint;
    int mBackgroundColor;
    int mBackgroundOffset;

    int mAdaptiveBackgroundMode;
    float mAdaptiveBackgroundRatio;
    int mAdaptiveBackgroundColor;
    boolean mAdaptBackground;

    // PROGRESS
    Paint mProgressPaint;
    int mProgressColor;

    // TEXT
    Rect mTextBounds;
    Paint mTextPaint;
    float mTextSize;
    private int mTextStyle;
    int mTextColor;
    int mTextProgress;
    Typeface mTypeface;
    int mTextShadowColor;
    float mTextShadowRadius;
    float mTextShadowDistY;
    float mTextShadowDistX;

    int mAdaptiveTextColor;
    int mAdaptiveTextMode;
    float mAdaptiveTextRatio;
    boolean mAdaptText;

    // COMMON
    RectF mBackgroundBounds;
    RectF mCircleBounds;
    ValueAnimator mColorAnimator;
    ValueAnimator mProgressAnimator;
    Interpolator mAnimInterpolator;
    int mAnimDuration;
    float mProgress;
    float mStartAngle;
    float mArcAngle;

    int mAdaptiveColor;

    @ProgressOrientation
    int orientation;
    @Nullable
    PercentageChartView.AdaptiveColorProvider adaptiveColorProvider;

    IPercentageChartView mView;

    BaseModeRenderer(IPercentageChartView view) {
        mView = view;

        //DRAWING ORIENTATION
        orientation = ORIENTATION_CLOCKWISE;

        //START DRAWING ANGLE
        mStartAngle = DEFAULT_START_ANGLE;

        //BACKGROUND DRAW STATE
        drawBackground = true;

        //BACKGROUND COLOR
        mBackgroundColor = ColorUtils.blendARGB(getThemeAccentColor(), Color.BLACK, 0.8f);

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
                mView.getViewContext().getResources().getDisplayMetrics()
        );

        //TEXT STYLE
        mTextStyle = Typeface.NORMAL;

        //TEXT SHADOW
        mTextShadowColor = Color.TRANSPARENT;
        mTextShadowRadius = 5f;
        mTextShadowDistX = 5f;
        mTextShadowDistY = 5f;

        //ADAPTIVE BACKGROUND COLOR
        mAdaptBackground = false;
        mAdaptiveBackgroundRatio = mAdaptiveBackgroundMode = -1;

        //ADAPTIVE TEXT COLOR
        mAdaptText = false;
        mAdaptiveTextRatio = mAdaptiveTextMode = -1;

        //BACKGROUND OFFSET
        mBackgroundOffset = 0;
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
        drawBackground = attrs.getBoolean(R.styleable.PercentageChartView_pcv_drawBackground, true);

        //BACKGROUND COLOR
        mBackgroundColor = attrs.getColor(R.styleable.PercentageChartView_pcv_backgroundColor, ColorUtils.blendARGB(getThemeAccentColor(), Color.BLACK, 0.8f));

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
            case LINEAR:
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
            mTextShadowRadius = attrs.getFloat(R.styleable.PercentageChartView_pcv_textShadowRadius, 5f);
            mTextShadowDistX = attrs.getFloat(R.styleable.PercentageChartView_pcv_textShadowDistX, 5f);
            mTextShadowDistY = attrs.getFloat(R.styleable.PercentageChartView_pcv_textShadowDistY, 5f);
        }

        //ADAPTIVE BACKGROUND COLOR
        mAdaptBackground = attrs.getBoolean(R.styleable.PercentageChartView_pcv_adaptiveBackground, false);
        mAdaptiveBackgroundRatio = attrs.getInt(R.styleable.PercentageChartView_pcv_adaptiveBackgroundRatio, -1);
        mAdaptiveBackgroundMode = attrs.getInt(R.styleable.PercentageChartView_pcv_adaptiveBackgroundMode, -1);


        //ADAPTIVE TEXT COLOR
        mAdaptText = attrs.getBoolean(R.styleable.PercentageChartView_pcv_adaptiveText, false);
        mAdaptiveTextRatio = attrs.getInt(R.styleable.PercentageChartView_pcv_adaptiveTextRatio, -1);
        mAdaptiveTextMode = attrs.getInt(R.styleable.PercentageChartView_pcv_adaptiveTextMode, -1);

        //BACKGROUND OFFSET
        mBackgroundOffset = attrs.getDimensionPixelSize(
                R.styleable.PercentageChartView_pcv_backgroundOffset,
                0);
    }

    //############################################################################################## BEHAVIOR
    public abstract void mesure(int w, int h, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom);

    public abstract void draw(Canvas canvas);

    public abstract void destroy();

    int getThemeAccentColor() {
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
    public abstract void setAdaptiveColorProvider(@Nullable PercentageChartView.AdaptiveColorProvider adaptiveColorProvider);

    //PROGRESS
    public float getProgress() {
        return mProgress;
    }

    public abstract void setProgress(float progress, boolean animate);

    //DRAW BACKGROUND STATE
    public boolean isDrawBackgroundEnabled() {
        return drawBackground;
    }

    public void setDrawBackgroundEnabled(boolean drawBackground) {
        this.drawBackground = drawBackground;
        mView.invalidate();
    }

    //START ANGLE
    public float getStartAngle() {
        return mStartAngle;
    }

    public void setStartAngle(float startAngle) {
        this.mStartAngle = startAngle;
        mView.invalidate();
    }

    //ORIENTATION
    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
//        mArcAngle = (orientation == ORIENTATION_COUNTERCLOCKWISE) ?
//                -(this.mProgress / DEFAULT_MAX * 360) :
//                this.mProgress / DEFAULT_MAX * 360;
        mView.invalidate();
    }

    //BACKGROUND COLOR
    public int getBackgroundColor() {
        if (!drawBackground) return -1;
        return (!mAdaptBackground) ? mBackgroundColor : mAdaptiveBackgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        if (!drawBackground || (adaptiveColorProvider != null && mAdaptBackground))
            return;
        this.mBackgroundColor = backgroundColor;
        mBackgroundPaint.setColor(mBackgroundColor);
        mView.invalidate();
    }

    //BACKGROUND OFFSET
    public float getBackgroundOffset() {
        if (!drawBackground) return -1;
        return mBackgroundOffset;
    }

    public void setBackgroundOffset(int backgroundOffset) {
        if (!drawBackground)
            return;
        this.mBackgroundOffset = backgroundOffset;
        mView.invalidate();
    }


    //PROGRESS COLOR
    public int getProgressColor() {
        return (adaptiveColorProvider != null) ? mAdaptiveColor : mProgressColor;
    }

    public void setProgressColor(int progressColor) {
        if (adaptiveColorProvider != null) return;

        this.mProgressColor = progressColor;
        mProgressPaint.setColor(progressColor);
        mView.invalidate();
    }

    //ADAPTIVE BACKGROUND
    public boolean isAdaptiveBackgroundEnabled() {
        return mAdaptBackground;
    }

    public float getAdaptiveBackgroundRatio() {
        return mAdaptiveBackgroundRatio;
    }

    public int getAdaptiveBackgroundMode() {
        return mAdaptiveBackgroundMode;
    }

    public abstract void setAdaptiveBackground(float ratio, int adaptiveMode);

    //ADAPTIVE TEXT
    public boolean isAdaptiveTextEnabled() {
        return mAdaptText;
    }

    public float getAdaptiveTextRatio() {
        return mAdaptiveTextRatio;
    }

    public int getAdaptiveTextMode() {
        return mAdaptiveTextMode;
    }

    public abstract void setAdaptiveText(float ratio, int adaptiveMode);

    //ANIMATION DURATION
    public int getAnimationDuration() {
        return mAnimDuration;
    }

    public void setAnimationDuration(int duration) {
        mAnimDuration = duration;
        mProgressAnimator.setDuration(mAnimDuration);
        if (mColorAnimator != null)
            mColorAnimator.setDuration(mAnimDuration);
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
        return (!mAdaptText) ? mTextColor : mAdaptiveTextColor;
    }

    public void setTextColor(@ColorInt int textColor) {
        if (adaptiveColorProvider != null && mAdaptText)
            return;
        this.mTextColor = textColor;
        mTextPaint.setColor(textColor);
        mView.invalidate();
    }

    //TEXT SIZE
    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float textSize) {
        this.mTextSize = textSize;
    }

    //TEXT TYPEFACE
    public Typeface getTypeface() {
        return mTypeface;
    }

    public void setTypeface(Typeface typeface) {
        if (mTextStyle > 0) {
            typeface = Typeface.create(mTypeface, mTextStyle);
        }
        this.mTypeface = typeface;
        mTextPaint.setTypeface(mTypeface);
        mView.invalidate();
    }

    //TEXT STYLE
    public int getTextStyle() {
        return mTextStyle;
    }

    public void setTextStyle(int mTextStyle) {
        this.mTextStyle = mTextStyle;
        mTypeface = (mTypeface == null) ? Typeface.defaultFromStyle(mTextStyle) : Typeface.create(mTypeface, mTextStyle);

        mTextPaint.setTypeface(mTypeface);
        mView.invalidate();
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
        this.mTextShadowColor = shadowColor;
        this.mTextShadowRadius = shadowRadius;
        this.mTextShadowDistX = shadowDistX;
        this.mTextShadowDistY = shadowDistY;

        mTextPaint.setShadowLayer(mTextShadowRadius, mTextShadowDistX, mTextShadowDistY, mTextShadowColor);
        mView.invalidate();
    }
}
