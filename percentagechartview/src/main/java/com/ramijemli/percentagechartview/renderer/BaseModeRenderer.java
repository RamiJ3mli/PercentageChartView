package com.ramijemli.percentagechartview.renderer;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.animation.Interpolator;

import com.ramijemli.percentagechartview.IPercentageChartView;
import com.ramijemli.percentagechartview.annotation.ProgressOrientation;

public abstract class BaseModeRenderer {

    // CHART MODE
    public static final int MODE_RING = 0;
    public static final int MODE_PIE = 1;

    // ORIENTATION
    public static final int ORIENTATION_CLOCKWISE = 0;
    public static final int ORIENTATION_COUNTERCLOCKWISE = 1;

    // BACKGROUND
    static final float DEFAULT_BACKGROUND_DP_WIDTH = 16;
    static final int DEFAULT_BACKGROUND_COLOR = Color.BLACK;

    Paint mBackgroundPaint;
    int mBackgroundColor;
    boolean drawBackground = true;

    // PROGRESS
    static final float DEFAULT_PROGRESS_BAR_DP_WIDTH = 16;
    static final int DEFAULT_PROGRESS_COLOR = Color.RED;
    public static final int CAP_ROUND = 0;
    public static final int CAP_SQUARE = 1;

    Paint mProgressPaint;
    int mProgressColor;

    // TEXT
    static float DEFAULT_TEXT_SP_SIZE = 12;

    Rect mTextBounds;
    Paint mTextPaint;
    float mTextSize;
    int mTextStyle;
    int mTextColor;
    int mTextProgress;
    Typeface mTypeface;

    // ANIMATION INTERPOLATORS
    static final int DEFAULT_ANIMATION_INTERPOLATOR = 0;
    static final int LINEAR = 0;
    static final int ACCELERATE = 1;
    static final int DECELERATE = 2;
    static final int ACCELERATE_DECELERATE = 3;
    static final int ANTICIPATE = 4;
    static final int OVERSHOOT = 5;
    static final int ANTICIPATE_OVERSHOOT = 6;
    static final int BOUNCE = 7;
    static final int FAST_OUT_LINEAR_IN = 8;
    static final int FAST_OUT_SLOW_IN = 9;
    static final int LINEAR_OUT_SLOW_IN = 10;

    // COMMON
    static final int DEFAULT_START_ANGLE = 0;
    static final float DEFAULT_MAX = 100;
    static final int DEFAULT_ANIMATION_DURATION = 1000;

    RectF mCircleBounds;
    ValueAnimator mColorAnimator;
    ValueAnimator mValueAnimator;
    Interpolator mAnimInterpolator;
    int mAnimDuration;
    float mProgress;
    float startAngle;
    float arcAngle;
    SparseIntArray mAdaptiveColors;
    SparseArray<Float> mAdaptiveDistribution;
    int mAdaptiveColor;

    @ProgressOrientation
    int orientation;

    IPercentageChartView mView;

    BaseModeRenderer(IPercentageChartView view) {
        mView = view;
    }

    public abstract void mesure(int w, int h, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom);

    public abstract void draw(Canvas canvas);

    public abstract void destroy();

    //INTERNAL
    int getAdaptiveColor(float progress) {
        if (progress == 100f)
            return mAdaptiveColors.get(mAdaptiveColors.size() - 1);

        if (mAdaptiveDistribution != null)
            return mAdaptiveColors.get(getColorIndex(progress));

        float hueSlice = DEFAULT_MAX / mAdaptiveColors.size();
        int index = (int) (progress / hueSlice);
        if (index == mAdaptiveColors.size()) {
            return mAdaptiveColors.get(index - 1);
        }

        return mAdaptiveColors.get(index);
    }

    int getColorIndex(float progress) {
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

    //STYLE MODIFIERS
    public float getProgress() {
        return mProgress;
    }

    public abstract void setProgress(float progress, boolean animate);
}
