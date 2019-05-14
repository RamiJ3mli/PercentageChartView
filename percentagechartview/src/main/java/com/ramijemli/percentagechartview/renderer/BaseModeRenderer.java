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
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.text.DynamicLayout;
import android.text.Editable;
import android.text.Layout;
import android.text.TextPaint;
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

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.ramijemli.percentagechartview.IPercentageChartView;
import com.ramijemli.percentagechartview.R;
import com.ramijemli.percentagechartview.annotation.ProgressOrientation;
import com.ramijemli.percentagechartview.callback.AdaptiveColorProvider;
import com.ramijemli.percentagechartview.callback.ProgressTextFormatter;


public abstract class BaseModeRenderer {


    // CHART MODE
    public static final int MODE_RING = 0;
    public static final int MODE_PIE = 1;
    public static final int MODE_FILL = 2;

    // ORIENTATION
    public static final int INVALID_ORIENTATION = -1;
    public static final int ORIENTATION_CLOCKWISE = 0;
    public static final int ORIENTATION_COUNTERCLOCKWISE = 1;

    // CHART MODE
    public static final int INVALID_GRADIENT = -1;
    public static final int GRADIENT_LINEAR = 0;
    public static final int GRADIENT_RADIAL = 1;
    public static final int GRADIENT_SWEEP = 2;

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

    private int mProvidedBackgroundColor;

    // PROGRESS
    Paint mProgressPaint;
    int mProgressColor;

    int[] mGradientColors;
    float[] mGradientDistributions;
    int mGradientType;
    float mGradientAngle;
    Shader mGradientShader;

    // TEXT
    TextPaint mTextPaint;
    int mTextColor;
    private int mProvidedTextColor;
    private int mTextProgress;
    private float mTextSize;
    private int mTextStyle;
    private Typeface mTypeface;
    private int mTextShadowColor;
    private float mTextShadowRadius;
    private float mTextShadowDistY;
    private float mTextShadowDistX;
    private Editable mTextEditor;
    private DynamicLayout mTextLayout;

    // COMMON
    RectF mBackgroundBounds;
    RectF mCircleBounds;
    ValueAnimator mProgressColorAnimator, mBackgroundColorAnimator, mTextColorAnimator, mBgBarColorAnimator;
    private ValueAnimator mProgressAnimator;
    private Interpolator mAnimInterpolator;
    int mAnimDuration;
    float mProgress;
    float mStartAngle;
    float mSweepAngle;

    private int mProvidedProgressColor;

    @ProgressOrientation
    int orientation;
    @Nullable
    AdaptiveColorProvider mAdaptiveColorProvider;
    @Nullable
    private ProgressTextFormatter mProvidedTextFormatter, defaultTextFormatter;

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

        //GRADIENT COLORS
        mGradientType = -1;
        mGradientAngle = (int) mStartAngle;

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

        //GRADIENT COLORS
        initGradientColors(attrs);

        //PROGRESS ANIMATION DURATION
        mAnimDuration = attrs.getInt(R.styleable.PercentageChartView_pcv_animDuration, DEFAULT_ANIMATION_DURATION);

        //PROGRESS ANIMATION INTERPOLATOR
        int interpolator = attrs.getInt(R.styleable.PercentageChartView_pcv_animInterpolator, DEFAULT_ANIMATION_INTERPOLATOR);
        switch (interpolator) {
            default:
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
            mTextShadowRadius = attrs.getFloat(R.styleable.PercentageChartView_pcv_textShadowRadius, 0);
            mTextShadowDistX = attrs.getFloat(R.styleable.PercentageChartView_pcv_textShadowDistX, 0);
            mTextShadowDistY = attrs.getFloat(R.styleable.PercentageChartView_pcv_textShadowDistY, 0);
        }

        //BACKGROUND OFFSET
        mBackgroundOffset = attrs.getDimensionPixelSize(
                R.styleable.PercentageChartView_pcv_backgroundOffset,
                0);
    }

    private void initGradientColors(TypedArray attrs) {
        //PROGRESS GRADIENT TYPE
        mGradientType = attrs.getInt(R.styleable.PercentageChartView_pcv_gradientType, -1);
        if (mGradientType == -1) return;

        //ANGLE FOR LINEAR GRADIENT
        mGradientAngle = attrs.getInt(R.styleable.PercentageChartView_pcv_gradientAngle, (int) mStartAngle);

        //PROGRESS GRADIENT COLORS
        String gradientColors = attrs.getString(R.styleable.PercentageChartView_pcv_gradientColors);
        if (gradientColors != null) {
            String[] colors = gradientColors.split(",");
            mGradientColors = new int[colors.length];
            try {
                for (int i = 0; i < colors.length; i++) {
                    mGradientColors[i] = Color.parseColor(colors[i].trim());
                }
            } catch (Exception e) {
                throw new InflateException("pcv_gradientColors attribute contains invalid hex color values.");
            }
        }

        //PROGRESS GRADIENT COLORS'S DISTRIBUTIONS
        String gradientDist = attrs.getString(R.styleable.PercentageChartView_pcv_gradientDistributions);
        if (gradientDist != null) {
            String[] distributions = gradientDist.split(",");
            mGradientDistributions = new float[distributions.length];
            try {
                for (int i = 0; i < distributions.length; i++) {
                    mGradientDistributions[i] = Float.parseFloat(distributions[i].trim());
                }
            } catch (Exception e) {
                throw new InflateException("pcv_gradientDistributions attribute contains invalid values.");
            }
        }
    }

    void setup() {
        mCircleBounds = new RectF();
        mBackgroundBounds = new RectF();
        mProvidedProgressColor = mProvidedBackgroundColor = mProvidedTextColor = -1;

        //BACKGROUND PAINT
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(mBackgroundColor);

        //PROGRESS PAINT
        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setColor(mProgressColor);

        //TEXT PAINT
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

        //TEXT LAYOUT
        defaultTextFormatter = progress -> (int) progress + "%";
        mTextEditor = Editable.Factory.getInstance().newEditable(defaultTextFormatter.provideFormattedText(mTextProgress));

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
            mView.postInvalidateOnAnimation();
        });
    }

    public void attach(IPercentageChartView view) {
        mView = view;
        setup();
    }

    //############################################################################################## INNER BEHAVIOR
    public abstract void measure(int w, int h, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom);

    public abstract void draw(Canvas canvas);

    void drawText(Canvas canvas) {
        canvas.save();
        canvas.translate(mCircleBounds.centerX(), mCircleBounds.centerY() - (mTextLayout.getHeight() >> 1));
        mTextLayout.draw(canvas);
        canvas.restore();
    }

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
        mCircleBounds = mBackgroundBounds = null;
        mBackgroundPaint = mProgressPaint = mTextPaint = null;
        mGradientShader = null;
        mAdaptiveColorProvider = null;
        defaultTextFormatter = mProvidedTextFormatter = null;
    }

    void updateText() {
        if (mTextEditor != null) {
            CharSequence text = (mProvidedTextFormatter != null) ?
                    mProvidedTextFormatter.provideFormattedText(mTextProgress) :
                    defaultTextFormatter.provideFormattedText(mTextProgress);
            mTextEditor.clear();
            mTextEditor.append(text);
        }
    }

    abstract void updateDrawingAngles();

    void updateProvidedColors(float progress) {
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

    void setupColorAnimations(){
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

    void updateAnimations(float progress) {
        mProgressAnimator.setFloatValues(mProgress, progress);
        mProgressAnimator.start();

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

    void cancelAnimations() {
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

    abstract void setupGradientColors(RectF bounds);

    abstract void updateGradientAngle(float angle);

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
        updateText();
        mView.postInvalidate();
    }

    //PROGRESS
    public float getProgress() {
        return mProgress;
    }

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
            mView.postInvalidate();
            return;
        }

        updateAnimations(progress);
    }

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

    public abstract void setStartAngle(float startAngle);

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

    //GRADIENT COLORS
    public int getGradientType() {
        return mGradientType;
    }

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

    public void setGradientColorsInternal(int type, int[] colors, float[] positions, float angle) {
        mGradientType = type;
        mGradientColors = colors;
        mGradientDistributions = positions;
        if (mGradientType == GRADIENT_LINEAR && mGradientAngle != angle) {
            mGradientAngle = angle;
        }
    }

    public float getGradientAngle() {
        return mGradientAngle;
    }

    public int[] getGradientColors() {
        return mGradientColors;
    }

    public float[] getGradientDistributions() {
        return mGradientDistributions;
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