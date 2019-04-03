package com.ramijemli.percentagechartview.renderer;

import android.animation.ValueAnimator;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.InflateException;
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

    // BACKGROUND
    boolean drawBackground = true;
    int mAdaptiveBackgroundMode;
    float mAdaptiveBackgroundRatio;
    int mAdaptiveBackgroundColor;
    boolean mAdaptBackground;

    // PROGRESS
    Paint mProgressPaint;
    int mProgressColor;

    // TEXT
    private static float DEFAULT_TEXT_SP_SIZE = 16;

    Rect mTextBounds;
    Paint mTextPaint;
    float mTextSize;
    private int mTextStyle;
    int mTextColor;
    int mTextProgress;
    Typeface mTypeface;
    int mAdaptiveTextColor;
    int mAdaptiveTextMode;
    float mAdaptiveTextRatio;
    boolean mAdaptText;

    // COMMON
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
    static final float DEFAULT_MAX = 100;
    private static final int DEFAULT_ANIMATION_DURATION = 1000;
    static final int DARKER_COLOR = 0;
    static final int LIGHTER_COLOR = 1;

    RectF mCircleBounds;
    ValueAnimator mColorAnimator;
    ValueAnimator mValueAnimator;
    Interpolator mAnimInterpolator;
    int mAnimDuration;
    float mProgress;
    float mStartAngle;
    float mArcAngle;
    SparseIntArray mAdaptiveColors;
    private SparseArray<Float> mAdaptiveDistribution;
    int mAdaptiveColor;

    @ProgressOrientation
    int orientation;

    IPercentageChartView mView;

    BaseModeRenderer(IPercentageChartView view) {
        mView = view;

        //DRAWING ORIENTATION
        orientation = ORIENTATION_CLOCKWISE;

        //START DRAWING ANGLE
        mStartAngle = DEFAULT_START_ANGLE;

        //BACKGROUND DRAW STATE
        drawBackground = true;

        //PROGRESS
        mProgress = mTextProgress = 0;

        //PROGRESS COLOR
        mProgressColor = Color.RED;

        //PROGRESS ANIMATION DURATION
        mAnimDuration = DEFAULT_ANIMATION_DURATION;

        //PROGRESS ANIMATION INTERPOLATOR
        mAnimInterpolator = new LinearInterpolator();

        //TEXT COLOR
        mTextColor = mProgressColor;

        //TEXT SIZE
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                DEFAULT_TEXT_SP_SIZE,
                mView.getViewContext().getResources().getDisplayMetrics()
        );

        //TEXT STYLE
        mTextStyle = Typeface.NORMAL;

        //ADAPT COLORS
        mAdaptText = mAdaptBackground = false;

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
        mTextColor = attrs.getColor(R.styleable.PercentageChartView_pcv_textColor, ColorUtils.blendARGB(getThemeAccentColor(), Color.WHITE, 0.8f));

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
            if (mTypeface == null) {
                mTypeface = Typeface.defaultFromStyle(mTextStyle);
            } else {
                mTypeface = Typeface.create(mTypeface, mTextStyle);
            }
        }

        initAdaptiveColors(attrs);
    }

    private void initAdaptiveColors(TypedArray attrs) {
        //ADAPTIVE COLORS
        String adaptiveColors = attrs.getString(R.styleable.PercentageChartView_pcv_adaptiveColors);
        if (adaptiveColors != null) {
            try {
                String[] colors = adaptiveColors.split(",");
                mAdaptiveColors = new SparseIntArray(colors.length);

                for (int i = 0; i < colors.length; i++) {
                    mAdaptiveColors.append(i, Color.parseColor(colors[i].trim()));
                }

            } catch (Exception e) {
                throw new InflateException("pcv_adaptiveColors attribute contains an invalid hex color value.");
            }

            //ADAPTIVE COLORS DISTRIBUTION
            String distribution = attrs.getString(R.styleable.PercentageChartView_pcv_adaptiveDistribution);
            if (distribution != null) {
                try {
                    String[] values = distribution.split(",");
                    mAdaptiveDistribution = new SparseArray<>(distribution.length());

                    for (int i = 0; i < values.length; i++) {
                        mAdaptiveDistribution.append(i, Float.parseFloat(values[i].trim()));
                    }

                } catch (Exception e) {
                    throw new InflateException("pcv_adaptiveDistribution attribute contains an invalid value.");
                }
            }

            if (mAdaptiveDistribution != null && mAdaptiveDistribution.size() != mAdaptiveColors.size())
                throw new InflateException("pcv_adaptiveDistribution and pcv_adaptiveColors attributes should have same number of elements contained.");

            mAdaptiveColor = getAdaptiveColor(mProgress);

            //ADAPTIVE BACKGROUND COLOR
            mAdaptBackground = attrs.getBoolean(R.styleable.PercentageChartView_pcv_adaptiveBackground, false);
            if (drawBackground && mAdaptBackground) {
                mAdaptiveBackgroundRatio = attrs.getInt(R.styleable.PercentageChartView_pcv_adaptiveBackgroundRatio, -1);
                mAdaptiveBackgroundMode = attrs.getInt(R.styleable.PercentageChartView_pcv_adaptiveBackgroundMode, -1);
                if (mAdaptiveBackgroundMode != -1 && mAdaptiveBackgroundRatio != -1) {
                    mAdaptiveBackgroundColor = ColorUtils.blendARGB(mAdaptiveColor,
                            (mAdaptiveBackgroundMode == DARKER_COLOR) ? Color.BLACK : Color.WHITE,
                            mAdaptiveBackgroundRatio / 100);
                } else {
                    mAdaptiveBackgroundColor = ColorUtils.blendARGB(mAdaptiveColor,
                            Color.BLACK,
                            .5f);
                }
            }

            //ADAPTIVE TEXT COLOR
            mAdaptText = attrs.getBoolean(R.styleable.PercentageChartView_pcv_adaptiveText, false);
            if (mAdaptText) {
                mAdaptiveTextRatio = attrs.getInt(R.styleable.PercentageChartView_pcv_adaptiveTextRatio, -1);
                mAdaptiveTextMode = attrs.getInt(R.styleable.PercentageChartView_pcv_adaptiveTextMode, -1);
                if (mAdaptiveTextMode != -1 && mAdaptiveTextRatio != -1) {
                    mAdaptiveTextColor = ColorUtils.blendARGB(mAdaptiveColor,
                            (mAdaptiveTextMode == DARKER_COLOR) ? Color.BLACK : Color.WHITE,
                            mAdaptiveTextRatio / 100);
                } else {
                    mAdaptiveTextColor = ColorUtils.blendARGB(mAdaptiveColor,
                            Color.WHITE,
                            .5f);
                }
            }

        }
    }

    public abstract void mesure(int w, int h, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom);

    public abstract void draw(Canvas canvas);

    public abstract void destroy();

    //INTERNAL
    int getAdaptiveColor(float progress) {
        if (progress == 0f) {
            return mAdaptiveColors.get(0);
        }

        if (progress == 100f) {
            return mAdaptiveColors.get(mAdaptiveColors.size() - 1);
        }

        if (mAdaptiveDistribution != null) {
            return mAdaptiveColors.get(getColorIndex(progress));
        }

        float hueSlice = DEFAULT_MAX / mAdaptiveColors.size();
        int index = (int) (progress / hueSlice);
        if (index == mAdaptiveColors.size()) {
            return mAdaptiveColors.get(index - 1);
        }

        return mAdaptiveColors.get(index);
    }

    private int getColorIndex(float progress) {
        int left = 0, right = mAdaptiveDistribution.size();
        while (left != right) {
            int mid = (left + right) / 2;
            if (mAdaptiveDistribution.get(mid) <= progress) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return right;
    }

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

    //STYLE MODIFIERS
    public float getProgress() {
        return mProgress;
    }

    public abstract void setProgress(float progress, boolean animate);
}
