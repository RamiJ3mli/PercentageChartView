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

package com.ramijemli.percentagechartview;


import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.ramijemli.percentagechartview.annotation.ChartMode;
import com.ramijemli.percentagechartview.annotation.GradientTypes;
import com.ramijemli.percentagechartview.annotation.ProgressBarStyle;
import com.ramijemli.percentagechartview.annotation.ProgressOrientation;
import com.ramijemli.percentagechartview.annotation.TextStyle;
import com.ramijemli.percentagechartview.callback.AdaptiveColorProvider;
import com.ramijemli.percentagechartview.callback.OnProgressChangeListener;
import com.ramijemli.percentagechartview.callback.ProgressTextFormatter;
import com.ramijemli.percentagechartview.renderer.BaseModeRenderer;
import com.ramijemli.percentagechartview.renderer.FillModeRenderer;
import com.ramijemli.percentagechartview.renderer.OffsetEnabledMode;
import com.ramijemli.percentagechartview.renderer.OrientationBasedMode;
import com.ramijemli.percentagechartview.renderer.PieModeRenderer;
import com.ramijemli.percentagechartview.renderer.RingModeRenderer;

import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.GRADIENT_LINEAR;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.GRADIENT_SWEEP;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.MODE_FILL;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.MODE_PIE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.MODE_RING;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.ORIENTATION_CLOCKWISE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.ORIENTATION_COUNTERCLOCKWISE;


@SuppressWarnings({"unused", "UnusedReturnValue"})
public class PercentageChartView extends View implements IPercentageChartView {

    private static final String STATE_SUPER_INSTANCE = "PercentageChartView.STATE_SUPER_INSTANCE";
    private static final String STATE_MODE = "PercentageChartView.STATE_MODE";
    private static final String STATE_ORIENTATION = "PercentageChartView.STATE_ORIENTATION";
    private static final String STATE_START_ANGLE = "PercentageChartView.STATE_START_ANGLE";
    private static final String STATE_DURATION = "PercentageChartView.STATE_DURATION";

    private static final String STATE_PROGRESS = "PercentageChartView.STATE_PROGRESS";
    private static final String STATE_PG_COLOR = "PercentageChartView.STATE_PG_COLOR";

    private static final String STATE_DRAW_BG = "PercentageChartView.STATE_DRAW_BG";
    private static final String STATE_BG_COLOR = "PercentageChartView.STATE_BG_COLOR";
    private static final String STATE_BG_OFFSET = "PercentageChartView.STATE_BG_OFFSET";

    private static final String STATE_TXT_COLOR = "PercentageChartView.STATE_TXT_COLOR";
    private static final String STATE_TXT_SIZE = "PercentageChartView.STATE_TXT_SIZE";
    private static final String STATE_TXT_SHA_COLOR = "PercentageChartView.STATE_TXT_SHD_COLOR";
    private static final String STATE_TXT_SHA_RADIUS = "PercentageChartView.STATE_TXT_SHA_RADIUS";
    private static final String STATE_TXT_SHA_DIST_X = "PercentageChartView.STATE_TXT_SHA_DIST_X";
    private static final String STATE_TXT_SHA_DIST_Y = "PercentageChartView.STATE_TXT_SHA_DIST_Y";

    private static final String STATE_PG_BAR_THICKNESS = "PercentageChartView.STATE_PG_BAR_THICKNESS";
    private static final String STATE_PG_BAR_STYLE = "PercentageChartView.STATE_PG_BAR_STYLE";

    private static final String STATE_DRAW_BG_BAR = "PercentageChartView.STATE_DRAW_BG_BAR";
    private static final String STATE_BG_BAR_COLOR = "PercentageChartView.STATE_BG_BAR_COLOR";
    private static final String STATE_BG_BAR_THICKNESS = "PercentageChartView.STATE_BG_BAR_THICKNESS";

    private static final String STATE_GRADIENT_TYPE = "PercentageChartView.STATE_GRADIENT_TYPE";
    private static final String STATE_GRADIENT_ANGLE = "PercentageChartView.STATE_GRADIENT_ANGLE";
    private static final String STATE_GRADIENT_COLORS = "PercentageChartView.STATE_GRADIENT_COLORS";
    private static final String STATE_GRADIENT_POSITIONS = "PercentageChartView.STATE_GRADIENT_POSITIONS";

    private BaseModeRenderer renderer;

    @ChartMode
    private int mode;

    @Nullable
    private OnProgressChangeListener onProgressChangeListener;

    public PercentageChartView(Context context) {
        super(context);
        init(context, null);
    }

    public PercentageChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PercentageChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public PercentageChartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        if (attributeSet != null) {
            TypedArray attrs = context.getTheme().obtainStyledAttributes(
                    attributeSet,
                    R.styleable.PercentageChartView,
                    0, 0
            );

            try {
                //CHART MODE (DEFAULT PIE MODE)
                mode = attrs.getInt(R.styleable.PercentageChartView_pcv_mode, MODE_PIE);
                switch (mode) {
                    case MODE_RING:
                        renderer = new RingModeRenderer(this, attrs);
                        break;
                    case MODE_FILL:
                        renderer = new FillModeRenderer(this, attrs);
                        break;

                    default:
                    case MODE_PIE:
                        renderer = new PieModeRenderer(this, attrs);
                        break;
                }

            } finally {
                attrs.recycle();
            }
        } else {
            mode = MODE_PIE;
            renderer = new PieModeRenderer(this);
        }
        setSaveEnabled(true);
    }

    //##############################################################################################   BEHAVIOR
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        if (renderer != null)
            renderer.measure(w, h, getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        renderer.destroy();
        renderer = null;

        if (onProgressChangeListener != null) {
            onProgressChangeListener = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        renderer.draw(canvas);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_SUPER_INSTANCE, super.onSaveInstanceState());

        bundle.putInt(STATE_MODE, mode);
        if (renderer instanceof OrientationBasedMode) {
            bundle.putInt(STATE_ORIENTATION, ((OrientationBasedMode) renderer).getOrientation());
        }
        bundle.putFloat(STATE_START_ANGLE, renderer.getStartAngle());
        bundle.putInt(STATE_DURATION, renderer.getAnimationDuration());

        bundle.putFloat(STATE_PROGRESS, renderer.getProgress());
        bundle.putInt(STATE_PG_COLOR, renderer.getProgressColor());

        bundle.putBoolean(STATE_DRAW_BG, renderer.isDrawBackgroundEnabled());
        bundle.putInt(STATE_BG_COLOR, renderer.getBackgroundColor());
        if (renderer instanceof OffsetEnabledMode) {
            bundle.putInt(STATE_BG_OFFSET, ((OffsetEnabledMode) renderer).getBackgroundOffset());
        }

        bundle.putInt(STATE_TXT_COLOR, renderer.getTextColor());
        bundle.putFloat(STATE_TXT_SIZE, renderer.getTextSize());
        bundle.putInt(STATE_TXT_SHA_COLOR, renderer.getTextShadowColor());
        bundle.putFloat(STATE_TXT_SHA_RADIUS, renderer.getTextShadowRadius());
        bundle.putFloat(STATE_TXT_SHA_DIST_X, renderer.getTextShadowDistX());
        bundle.putFloat(STATE_TXT_SHA_DIST_Y, renderer.getTextShadowDistY());

        if (renderer instanceof RingModeRenderer) {
            bundle.putFloat(STATE_PG_BAR_THICKNESS, ((RingModeRenderer) renderer).getProgressBarThickness());
            bundle.putInt(STATE_PG_BAR_STYLE, ((RingModeRenderer) renderer).getProgressBarStyle());
            bundle.putBoolean(STATE_DRAW_BG_BAR, ((RingModeRenderer) renderer).isDrawBackgroundBarEnabled());
            bundle.putInt(STATE_BG_BAR_COLOR, ((RingModeRenderer) renderer).getBackgroundBarColor());
            bundle.putFloat(STATE_BG_BAR_THICKNESS, ((RingModeRenderer) renderer).getBackgroundBarThickness());
        }

        if (renderer.getGradientType() != -1) {
            bundle.putInt(STATE_GRADIENT_TYPE, renderer.getGradientType());
            bundle.putFloat(STATE_GRADIENT_ANGLE, renderer.getGradientAngle());
            bundle.putIntArray(STATE_GRADIENT_COLORS, renderer.getGradientColors());
            bundle.putFloatArray(STATE_GRADIENT_POSITIONS, renderer.getGradientDistributions());
        }

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mode = bundle.getInt(STATE_MODE);
            switch (mode) {
                case MODE_RING:
                    renderer = new RingModeRenderer(this);
                    ((RingModeRenderer) renderer).setProgressBarThickness(bundle.getFloat(STATE_PG_BAR_THICKNESS));
                    ((RingModeRenderer) renderer).setProgressBarStyle(bundle.getInt(STATE_PG_BAR_STYLE));
                    ((RingModeRenderer) renderer).setDrawBackgroundBarEnabled(bundle.getBoolean(STATE_DRAW_BG_BAR));
                    ((RingModeRenderer) renderer).setBackgroundBarColor(bundle.getInt(STATE_BG_BAR_COLOR));
                    ((RingModeRenderer) renderer).setBackgroundBarThickness(bundle.getFloat(STATE_BG_BAR_THICKNESS));
                    break;
                case MODE_FILL:
                    renderer = new FillModeRenderer(this);
                    break;

                default:
                case MODE_PIE:
                    renderer = new PieModeRenderer(this);
                    break;
            }

            if (renderer instanceof OrientationBasedMode) {
                ((OrientationBasedMode) renderer).setOrientation(bundle.getInt(STATE_ORIENTATION));
            }
            renderer.setStartAngle(bundle.getFloat(STATE_START_ANGLE));
            renderer.setAnimationDuration(bundle.getInt(STATE_DURATION));

            renderer.setProgress(bundle.getFloat(STATE_PROGRESS), false);
            renderer.setProgressColor(bundle.getInt(STATE_PG_COLOR));

            renderer.setDrawBackgroundEnabled(bundle.getBoolean(STATE_DRAW_BG));
            renderer.setBackgroundColor(bundle.getInt(STATE_BG_COLOR));
            if (renderer instanceof OffsetEnabledMode) {
                ((OffsetEnabledMode) renderer).setBackgroundOffset(bundle.getInt(STATE_BG_OFFSET));
            }

            renderer.setTextColor(bundle.getInt(STATE_TXT_COLOR));
            renderer.setTextSize(bundle.getFloat(STATE_TXT_SIZE));
            renderer.setTextShadow(bundle.getInt(STATE_TXT_SHA_COLOR),
                    bundle.getFloat(STATE_TXT_SHA_RADIUS),
                    bundle.getFloat(STATE_TXT_SHA_DIST_X),
                    bundle.getFloat(STATE_TXT_SHA_DIST_Y));

            if (bundle.getInt(STATE_GRADIENT_TYPE, -1) != -1) {
                renderer.setGradientColorsInternal(bundle.getInt(STATE_GRADIENT_TYPE),
                        bundle.getIntArray(STATE_GRADIENT_COLORS),
                        bundle.getFloatArray(STATE_GRADIENT_POSITIONS),
                        bundle.getFloat(STATE_GRADIENT_ANGLE));
            }
            super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER_INSTANCE));
            return;
        }

        super.onRestoreInstanceState(state);
    }

    //RENDERER CALLBACKS
    @Override
    public Context getViewContext() {
        return getContext();
    }

    @Override
    public void onProgressUpdated(float progress) {
        if (onProgressChangeListener != null)
            onProgressChangeListener.onProgressChanged(progress);
    }

    //##############################################################################################   STYLE MODIFIERS

    /**
     * Gets the percentage chart view mode.
     *
     * @return the percentage chart view mode
     */
    @ChartMode
    public int getMode() {
        return mode;
    }

    /**
     * Gets the current drawing orientation.
     *
     * @return the current drawing orientation
     */
    @ProgressOrientation
    public int getOrientation() {
        if (!(renderer instanceof OrientationBasedMode))
            return BaseModeRenderer.INVALID_ORIENTATION;
        return ((OrientationBasedMode) renderer).getOrientation();
    }

    /**
     * Sets the circular drawing direction. Default orientation is ORIENTATION_CLOCKWISE.
     *
     * @param orientation non-negative orientation constant.
     */
    public void setOrientation(@ProgressOrientation int orientation) {
        orientation(orientation);
        postInvalidate();
    }

    /**
     * Gets the current circular drawing's start angle.
     *
     * @return the current circular drawing's start angle
     */
    @FloatRange(from = 0f, to = 360f)
    public float getStartAngle() {
        return renderer.getStartAngle();
    }

    /**
     * Sets the current circular drawing's start angle in degrees. Default start angle is0.
     *
     * @param startAngle A positive start angle value that is less or equal to 360.
     */
    public void setStartAngle(@FloatRange(from = 0f, to = 360f) float startAngle) {
        startAngle(startAngle);
        postInvalidate();
    }

    /**
     * Gets whether drawing background has been enabled.
     *
     * @return whether drawing background has been enabled
     */
    public boolean isDrawBackgroundEnabled() {
        return renderer.isDrawBackgroundEnabled();
    }

    /**
     * Sets whether background should be drawn.
     *
     * @param enabled True if background have to be drawn, false otherwise.
     */
    public void setDrawBackgroundEnabled(boolean enabled) {
        drawBackgroundEnabled(enabled);
        postInvalidate();
    }

    /**
     * Gets the circular background color for this view.
     *
     * @return the color of the circular background
     */
    @ColorInt
    public int getBackgroundColor() {
        return renderer.getBackgroundColor();
    }

    /**
     * Sets the circular background color for this view.
     *
     * @param color the color of the circular background
     */
    public void setBackgroundColor(@ColorInt int color) {
        backgroundColor(color);
        postInvalidate();
    }

    /**
     * Gets the current progress.
     *
     * @return the current progress
     */
    @FloatRange(from = 0f, to = 100f)
    public float getProgress() {
        return renderer.getProgress();
    }

    /**
     * Sets a new progress value. Passing true in animate will cause an animated progress update.
     *
     * @param progress New progress float value to set.
     * @param animate  Animation boolean value to set whether to animate progress change or not.
     * @throws IllegalArgumentException if the given progress is negative, or, less or equal to 100.
     */
    public void setProgress(@FloatRange(from = 0f, to = 100f) float progress, boolean animate) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("Progress value must be positive and less or equal to 100.");
        }

        renderer.setProgress(progress, animate);
    }

    /**
     * Gets the progress/progress bar color for this view.
     *
     * @return the progress/progress bar color.
     */
    @ColorInt
    public int getProgressColor() {
        return renderer.getProgressColor();
    }

    /**
     * Sets the progress/progress bar color for this view.
     *
     * @param color the color of the progress/progress bar
     */
    public void setProgressColor(@ColorInt int color) {
        progressColor(color);
        postInvalidate();
    }


    /**
     * Gets progress gradient type.
     *
     * @return Gets progress gradient type.
     */
    @GradientTypes
    public int getGradientType() {
        return renderer.getGradientType();
    }


    /**
     * Sets progress gradient colors.
     *
     * @param type      The gradient type which is a GradientTypes constant
     * @param colors    The colors to be distributed.
     *                  There must be at least 2 colors in the array.
     * @param positions May be NULL. The relative position of
     *                  each corresponding color in the colors array, beginning
     *                  with 0 and ending with 1.0. If the values are not
     *                  monotonic, the drawing may produce unexpected results.
     *                  If positions is NULL, then the colors are automatically
     *                  spaced evenly.
     * @param angle     Defines the direction for linear gradient type.
     */
    public void setGradientColors(@GradientTypes int type, int[] colors, float[] positions, @FloatRange(from = 0f, to = 360f) float angle) {
        gradientColors(type, colors, positions, angle);
        postInvalidate();
    }

    /**
     * Gets the duration of the progress change's animation.
     *
     * @return the duration of the progress change's animation
     */
    @IntRange(from = 0)
    public int getAnimationDuration() {
        return renderer.getAnimationDuration();
    }

    /**
     * Sets the duration of the progress change's animation.
     *
     * @param duration non-negative duration value.
     */
    public void setAnimationDuration(@IntRange(from = 50) int duration) {
        animationDuration(duration);
    }

    /**
     * Gets the interpolator of the progress change's animation.
     *
     * @return the interpolator of the progress change's animation
     */
    public TimeInterpolator getAnimationInterpolator() {
        return renderer.getAnimationInterpolator();
    }

    /**
     * Sets the interpolator of the progress change's animation.
     *
     * @param interpolator TimeInterpolator instance.
     */
    @SuppressWarnings("ConstantConditions")
    public void setAnimationInterpolator(@NonNull TimeInterpolator interpolator) {
        animationInterpolator(interpolator);
    }

    /**
     * Gets the text color.
     *
     * @return the text color
     */
    @ColorInt
    public int getTextColor() {
        return renderer.getTextColor();
    }

    /**
     * Sets the text color for this view.
     *
     * @param color the text color
     */
    public void setTextColor(@ColorInt int color) {
        textColor(color);
        postInvalidate();
    }

    /**
     * Gets the text size.
     *
     * @return the text size
     */
    public float getTextSize() {
        return renderer.getTextSize();
    }

    /**
     * Sets the text size.
     *
     * @param size the text size
     */
    public void setTextSize(float size) {
        textSize(size);
        postInvalidate();
    }

    /**
     * Gets the text font.
     *
     * @return the text typeface
     */
    public Typeface getTypeface() {
        return renderer.getTypeface();
    }

    /**
     * Sets the text font.
     *
     * @param typeface the text font as a Typeface instance
     */
    @SuppressWarnings("ConstantConditions")
    public void setTypeface(@NonNull Typeface typeface) {
        typeface(typeface);
        postInvalidate();
    }

    /**
     * Gets the text style.
     *
     * @return the text style
     */
    @TextStyle
    public int getTextStyle() {
        return renderer.getTextStyle();
    }

    /**
     * Sets the text style.
     *
     * @param style the text style.
     */
    public void setTextStyle(@TextStyle int style) {
        textStyle(style);
        postInvalidate();
    }

    /**
     * Gets the text shadow color.
     *
     * @return the text shadow color
     */
    @ColorInt
    public int getTextShadowColor() {
        return renderer.getTextShadowColor();
    }

    /**
     * Gets the text shadow radius.
     *
     * @return the text shadow radius
     */
    public float getTextShadowRadius() {
        return renderer.getTextShadowRadius();
    }

    /**
     * Gets the text shadow y-axis distance.
     *
     * @return the text shadow y-axis distance
     */
    public float getTextShadowDistY() {
        return renderer.getTextShadowDistY();
    }

    /**
     * Gets the text shadow x-axis distance.
     *
     * @return the text shadow x-axis distance
     */
    public float getTextShadowDistX() {
        return renderer.getTextShadowDistX();
    }

    /**
     * Sets the text shadow. Passing zeros will remove the shadow.
     *
     * @param shadowColor  text shadow color value.
     * @param shadowRadius text shadow radius.
     * @param shadowDistX  text shadow y-axis distance.
     * @param shadowDistY  text shadow x-axis distance.
     */
    public void setTextShadow(@ColorInt int shadowColor, @FloatRange(from = 0) float shadowRadius, @FloatRange(from = 0) float shadowDistX, @FloatRange(from = 0) float shadowDistY) {
        textShadow(shadowColor, shadowRadius, shadowDistX, shadowDistY);
        postInvalidate();
    }

    /**
     * Gets the offset of the circular background.
     *
     * @return the offset of the circular background.-1 if chart mode is not set to pie.
     */
    public float getBackgroundOffset() {
        if (!(renderer instanceof OffsetEnabledMode)) return -1;
        return ((OffsetEnabledMode) renderer).getBackgroundOffset();
    }

    /**
     * Sets the offset of the circular background. Works only if chart mode is set to pie.
     *
     * @param offset A positive offset value.
     */
    public void setBackgroundOffset(@IntRange(from = 0) int offset) {
        backgroundOffset(offset);
        postInvalidate();
    }

    /**
     * Gets whether drawing the background bar has been enabled.
     *
     * @return whether drawing the background bar has been enabled
     */
    public boolean isDrawBackgroundBarEnabled() {
        if (!(renderer instanceof RingModeRenderer)) return false;
        return ((RingModeRenderer) renderer).isDrawBackgroundBarEnabled();
    }

    /**
     * Sets whether background bar should be drawn.
     *
     * @param enabled True if background bar have to be drawn, false otherwise.
     */
    public void setDrawBackgroundBarEnabled(boolean enabled) {
        drawBackgroundBarEnabled(enabled);
        postInvalidate();
    }

    /**
     * Gets the background bar color.
     *
     * @return the background bar color. -1 if chart mode is not set to ring.
     */
    public int getBackgroundBarColor() {
        if (!(renderer instanceof RingModeRenderer)) return -1;
        return ((RingModeRenderer) renderer).getBackgroundBarColor();
    }

    /**
     * Sets the background bar color.
     *
     * @param color the background bar color
     */
    public void setBackgroundBarColor(@ColorInt int color) {
        backgroundBarColor(color);
        postInvalidate();
    }

    /**
     * Gets the background bar thickness in pixels.
     *
     * @return the background bar thickness in pixels. -1 if chart mode is not set to ring.
     */
    public float getBackgroundBarThickness() {
        if (!(renderer instanceof RingModeRenderer)) return -1;
        return ((RingModeRenderer) renderer).getBackgroundBarThickness();
    }

    /**
     * Sets the background bar thickness in pixels. Works only if chart mode is set to ring.
     *
     * @param thickness non-negative thickness value in pixels.
     */
    public void setBackgroundBarThickness(@FloatRange(from = 0) float thickness) {
        backgroundBarThickness(thickness);
        postInvalidate();
    }

    /**
     * Gets the progress bar thickness in pixels.
     *
     * @return the progress bar thickness in pixels. -1 if chart mode is not set to ring.
     */
    public float getProgressBarThickness() {
        if (!(renderer instanceof RingModeRenderer)) return -1;
        return ((RingModeRenderer) renderer).getProgressBarThickness();
    }

    /**
     * Sets the progress bar thickness in pixels. Works only if chart mode is set to ring.
     *
     * @param thickness non-negative thickness value in pixels.
     */
    public void setProgressBarThickness(@FloatRange(from = 0) float thickness) {
        progressBarThickness(thickness);
        postInvalidate();
    }

    /**
     * Gets the progress bar stroke style.
     *
     * @return the progress bar stroke style. -1 if chart mode is not set to ring.
     */
    public int getProgressBarStyle() {
        if (!(renderer instanceof RingModeRenderer)) return -1;
        return ((RingModeRenderer) renderer).getProgressBarStyle();
    }

    /**
     * Sets the progress bar stroke style. Works only if chart mode is set to ring.
     *
     * @param style Progress bar stroke style as a ProgressStyle constant.
     */
    public void setProgressBarStyle(@ProgressBarStyle int style) {
        progressBarStyle(style);
        postInvalidate();
    }

    //############################################################################################## UPDATE PIPELINE AS A FLUENT API

    /**
     * Sets the circular drawing direction. Default orientation is ORIENTATION_CLOCKWISE.
     *
     * @param orientation non-negative orientation constant.
     * @throws IllegalArgumentException if the given orientation is not a ProgressOrientation constant or not supported by the current used chart mode.
     */
    public PercentageChartView orientation(@ProgressOrientation int orientation) {
        if (orientation != ORIENTATION_CLOCKWISE && orientation != ORIENTATION_COUNTERCLOCKWISE) {
            throw new IllegalArgumentException("Orientation must be a ProgressOrientation constant.");
        }

        try {
            ((OrientationBasedMode) renderer).setOrientation(orientation);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Orientation is not support by the used percentage chart mode.");
        }
        return this;
    }

    /**
     * Sets the current circular drawing's start angle in degrees. Default start angle is0.
     *
     * @param startAngle A positive start angle value that is less or equal to 360.
     * @throws IllegalArgumentException if the given start angle is not positive, or, less or equal to 360.
     */
    public PercentageChartView startAngle(@FloatRange(from = 0f, to = 360f) float startAngle) {
        if (startAngle < 0 || startAngle > 360) {
            throw new IllegalArgumentException("Start angle value must be positive and less or equal to 360.");
        }
        this.renderer.setStartAngle(startAngle);
        return this;
    }

    /**
     * Sets whether background should be drawn.
     *
     * @param enabled True if background have to be drawn, false otherwise.
     */
    public PercentageChartView drawBackgroundEnabled(boolean enabled) {
        this.renderer.setDrawBackgroundEnabled(enabled);
        return this;
    }

    /**
     * Sets the circular background color for this view.
     *
     * @param color the color of the circular background
     */
    public PercentageChartView backgroundColor(@ColorInt int color) {
        this.renderer.setBackgroundColor(color);
        return this;
    }

    /**
     * Sets the progress/progress bar color for this view.
     *
     * @param color the color of the progress/progress bar
     */
    public PercentageChartView progressColor(@ColorInt int color) {
        this.renderer.setProgressColor(color);
        return this;
    }

    /**
     * Sets progress gradient colors.
     *
     * @param type      The gradient type which is a GradientTypes constant
     * @param colors    The colors to be distributed.
     *                  There must be at least 2 colors in the array.
     * @param positions May be NULL. The relative position of
     *                  each corresponding color in the colors array, beginning
     *                  with 0 and ending with 1.0. If the values are not
     *                  monotonic, the drawing may produce unexpected results.
     *                  If positions is NULL, then the colors are automatically
     *                  spaced evenly.
     * @param angle     Defines the direction for linear gradient type.
     * @throws IllegalArgumentException If type is not a GradientTypes constant and if colors array is null
     */
    public PercentageChartView gradientColors(@GradientTypes int type, int[] colors, float[] positions, @FloatRange(from = 0f, to = 360f) float angle) {
        if (type < GRADIENT_LINEAR || type > GRADIENT_SWEEP) {
            throw new IllegalArgumentException("Invalid value for progress gradient type.");
        } else if (colors == null) {
            throw new IllegalArgumentException("Gradient colors int array cannot be null.");
        }

        this.renderer.setGradientColors(type, colors, positions, angle);
        return this;
    }

    /**
     * Sets the duration of the progress change's animation.
     *
     * @param duration non-negative duration value.
     * @throws IllegalArgumentException if the given duration is less than 50.
     */
    public PercentageChartView animationDuration(@IntRange(from = 50) int duration) {
        if (duration < 50) {
            throw new IllegalArgumentException("Duration must be equal or greater than 50.");
        }
        renderer.setAnimationDuration(duration);
        return this;
    }

    /**
     * Sets the interpolator of the progress change's animation.
     *
     * @param interpolator TimeInterpolator instance.
     * @throws IllegalArgumentException if the given TimeInterpolator instance is null.
     */
    @SuppressWarnings("ConstantConditions")
    public PercentageChartView animationInterpolator(@NonNull TimeInterpolator interpolator) {
        if (interpolator == null) {
            throw new IllegalArgumentException("Animation interpolator cannot be null");
        }

        renderer.setAnimationInterpolator(interpolator);
        return this;
    }

    /**
     * Sets the text color for this view.
     *
     * @param color the text color
     */
    public PercentageChartView textColor(@ColorInt int color) {
        renderer.setTextColor(color);
        return this;
    }

    /**
     * Sets the text size.
     *
     * @param size the text size
     * @throws IllegalArgumentException if the given text size is zero or a negative value.
     */
    public PercentageChartView textSize(float size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Text size must be a nonzero positive value.");
        }
        renderer.setTextSize(size);
        return this;
    }

    /**
     * Sets the text font.
     *
     * @param typeface the text font as a Typeface instance
     * @throws IllegalArgumentException if the given typeface is null.
     */
    @SuppressWarnings("ConstantConditions")
    public PercentageChartView typeface(@NonNull Typeface typeface) {
        if (typeface == null) {
            throw new IllegalArgumentException("Text TypeFace cannot be null");
        }
        renderer.setTypeface(typeface);
        return this;
    }

    /**
     * Sets the text style.
     *
     * @param style the text style.
     * @throws IllegalArgumentException if the given text style is not a valid TextStyle constant.
     */
    public PercentageChartView textStyle(@TextStyle int style) {
        if (style < 0 || style > 3) {
            throw new IllegalArgumentException("Text style must be a valid TextStyle constant.");
        }
        renderer.setTextStyle(style);
        return this;
    }

    /**
     * Sets the text shadow. Passing zeros will remove the shadow.
     *
     * @param shadowColor  text shadow color value.
     * @param shadowRadius text shadow radius.
     * @param shadowDistX  text shadow y-axis distance.
     * @param shadowDistY  text shadow x-axis distance.
     */
    public PercentageChartView textShadow(@ColorInt int shadowColor, @FloatRange(from = 0) float shadowRadius, @FloatRange(from = 0) float shadowDistX, @FloatRange(from = 0) float shadowDistY) {
        renderer.setTextShadow(shadowColor, shadowRadius, shadowDistX, shadowDistY);
        return this;
    }

    /**
     * Sets the offset of the circular background. Works only if chart mode is set to pie.
     *
     * @param offset A positive offset value.
     * @throws IllegalArgumentException if the given offset is a negative value, or, not supported by the current used chart mode.
     */
    public PercentageChartView backgroundOffset(@IntRange(from = 0) int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Background offset must be a positive value.");
        }

        try {
            ((OffsetEnabledMode) renderer).setBackgroundOffset(offset);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Background offset is not support by the used percentage chart mode.");
        }
        return this;
    }

    /**
     * Sets whether background bar should be drawn.
     *
     * @param enabled True if background bar have to be drawn, false otherwise.
     * @throws IllegalArgumentException if background bar's drawing state is not supported by the current used chart mode.
     */
    public PercentageChartView drawBackgroundBarEnabled(boolean enabled) {
        try {
            ((RingModeRenderer) renderer).setDrawBackgroundBarEnabled(enabled);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Background bar's drawing state is not support by the used percentage chart mode.");
        }
        return this;
    }

    /**
     * Sets the background bar color.
     *
     * @param color the background bar color
     * @throws IllegalArgumentException if background bar color is not supported by the current used chart mode.
     */
    public PercentageChartView backgroundBarColor(@ColorInt int color) {
        try {
            ((RingModeRenderer) renderer).setBackgroundBarColor(color);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Background bar color is not support by the used percentage chart mode.");
        }
        return this;
    }

    /**
     * Sets the background bar thickness in pixels. Works only if chart mode is set to ring.
     *
     * @param thickness non-negative thickness value in pixels.
     * @throws IllegalArgumentException if the given value is negative, or, background bar thickness is not supported by the current used chart mode.
     */
    public PercentageChartView backgroundBarThickness(@FloatRange(from = 0) float thickness) {
        if (thickness < 0) {
            throw new IllegalArgumentException("Background bar thickness must be a positive value.");
        }

        try {
            ((RingModeRenderer) renderer).setBackgroundBarThickness(thickness);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Background bar thickness is not support by the used percentage chart mode.");
        }
        return this;
    }

    /**
     * Sets the progress bar thickness in pixels. Works only if chart mode is set to ring.
     *
     * @param thickness non-negative thickness value in pixels.
     * @throws IllegalArgumentException if the given value is negative, or, progress bar thickness is not supported by the current used chart mode.
     */
    public PercentageChartView progressBarThickness(@FloatRange(from = 0) float thickness) {
        if (thickness < 0) {
            throw new IllegalArgumentException("Progress bar thickness must be a positive value.");
        }

        try {
            ((RingModeRenderer) renderer).setProgressBarThickness(thickness);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Progress bar thickness is not support by the used percentage chart mode.");
        }
        return this;
    }

    /**
     * Sets the progress bar stroke style. Works only if chart mode is set to ring.
     *
     * @param style Progress bar stroke style as a ProgressStyle constant.
     * @throws IllegalArgumentException if the given progress bar style is not a valid ProgressBarStyle constant, or, not supported by the current used chart mode.
     */
    public PercentageChartView progressBarStyle(@ProgressBarStyle int style) {
        if (style < 0 || style > 1) {
            throw new IllegalArgumentException("Progress bar style must be a valid TextStyle constant.");
        }

        try {
            ((RingModeRenderer) renderer).setProgressBarStyle(style);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Progress bar style is not support by the used percentage chart mode.");
        }
        return this;
    }

    /**
     * Apply all the requested changes.
     */
    public void apply() {
        postInvalidate();
    }

    //##############################################################################################   ADAPTIVE COLOR PROVIDER
    public void setAdaptiveColorProvider(@Nullable AdaptiveColorProvider adaptiveColorProvider) {
        this.renderer.setAdaptiveColorProvider(adaptiveColorProvider);
    }

    //##############################################################################################   TEXT FORMATTER
    public void setTextFormatter(@Nullable ProgressTextFormatter textFormatter) {
        this.renderer.setTextFormatter(textFormatter);
    }

    //##############################################################################################   LISTENER
    public void setOnProgressChangeListener(@Nullable OnProgressChangeListener onProgressChangeListener) {
        this.onProgressChangeListener = onProgressChangeListener;
    }

}
