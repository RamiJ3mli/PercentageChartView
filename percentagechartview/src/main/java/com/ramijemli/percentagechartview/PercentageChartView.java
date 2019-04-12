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
import android.util.AttributeSet;
import android.view.View;

import com.ramijemli.percentagechartview.annotation.ChartMode;
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

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.MODE_FILL;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.MODE_PIE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.MODE_RING;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.ORIENTATION_CLOCKWISE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.ORIENTATION_COUNTERCLOCKWISE;

@SuppressWarnings("unused")
public class PercentageChartView extends View implements IPercentageChartView {

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
            renderer = new PieModeRenderer(this);
        }
    }

    //##############################################################################################   BEHAVIOR
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        renderer.mesure(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec), getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
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
     * @throws IllegalArgumentException if the given orientation is not a non-negative ProgressOrientation constant.
     */
    public void setOrientation(@ProgressOrientation int orientation) {
        if (orientation != ORIENTATION_CLOCKWISE && orientation != ORIENTATION_COUNTERCLOCKWISE) {
            throw new IllegalArgumentException("Orientation must be a ProgressOrientation constant.");
        } else if (!(renderer instanceof OrientationBasedMode)) {
            throw new IllegalArgumentException("Orientation is not supported by the used percentage chart mode.");
        }

        ((OrientationBasedMode) renderer).setOrientation(orientation);
        invalidate();
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
     * @throws IllegalArgumentException if the given start angle is not positive, or, less or equal to 360.
     */
    public void setStartAngle(@FloatRange(from = 0f, to = 360f) float startAngle) {
        if (startAngle < 0 || startAngle > 360) {
            throw new IllegalArgumentException("Start angle value must be positive and less or equal to 360.");
        }
        this.renderer.setStartAngle(startAngle);
        invalidate();
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
        this.renderer.setDrawBackgroundEnabled(enabled);
        invalidate();
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
        this.renderer.setBackgroundColor(color);
        invalidate();
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
        this.renderer.setProgressColor(color);
        invalidate();
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
     * @throws IllegalArgumentException if the given duration is less than 50.
     */
    public void setAnimationDuration(@IntRange(from = 50) int duration) {
        if (duration < 50) {
            throw new IllegalArgumentException("Duration must be equal or greater than 50.");
        }
        renderer.setAnimationDuration(duration);
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
     * @throws IllegalArgumentException if the given TimeInterpolator instance is null.
     */
    @SuppressWarnings("ConstantConditions")
    public void setAnimationInterpolator(@NonNull TimeInterpolator interpolator) {
        if (interpolator == null) {
            throw new IllegalArgumentException("Animation interpolator cannot be null");
        }
        renderer.setAnimationInterpolator(interpolator);
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
        renderer.setTextColor(color);
        invalidate();
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
     * @throws IllegalArgumentException if the given text size is zero or a negative value.
     */
    public void setTextSize(float size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Text size must be a nonzero positive value.");
        }
        renderer.setTextSize(size);
        invalidate();
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
     * @throws IllegalArgumentException if the given typeface is null.
     */
    @SuppressWarnings("ConstantConditions")
    public void setTypeface(@NonNull Typeface typeface) {
        if (typeface == null) {
            throw new IllegalArgumentException("Text TypeFace cannot be null");
        }
        renderer.setTypeface(typeface);
        invalidate();
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
     * @throws IllegalArgumentException if the given text style is not a valid TextStyle constant.
     */
    public void setTextStyle(@TextStyle int style) {
        if (style < 0 || style > 3) {
            throw new IllegalArgumentException("Text style must be a valid TextStyle constant.");
        }
        renderer.setTextStyle(style);
        invalidate();
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
        renderer.setTextShadow(shadowColor, shadowRadius, shadowDistX, shadowDistY);
        invalidate();
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
     * @throws IllegalArgumentException if the given offset is a negative value.
     */
    public void setBackgroundOffset(@IntRange(from = 0) int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Background offset must be a positive value.");
        }

        try {
            ((OffsetEnabledMode) renderer).setBackgroundOffset(offset);
            invalidate();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Background offset is not supported by the used percentage chart mode.");
        }
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
        try {
            ((RingModeRenderer) renderer).setDrawBackgroundBarEnabled(enabled);
            invalidate();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Background bar's drawing state is not support by the used percentage chart mode.");
        }
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
        try {
            ((RingModeRenderer) renderer).setBackgroundBarColor(color);
            invalidate();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Background bar color is not support by the used percentage chart mode.");
        }
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
     * @throws IllegalArgumentException if the given value is negative.
     */
    public void setBackgroundBarThickness(@FloatRange(from = 0) float thickness) {
        if (thickness < 0) {
            throw new IllegalArgumentException("Background bar thickness must be a positive value.");
        }

        try {
            ((RingModeRenderer) renderer).setBackgroundBarThickness(thickness);
            invalidate();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Background bar thickness is not support by the used percentage chart mode.");
        }
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
     * @throws IllegalArgumentException if the given value is negative.
     */
    public void setProgressBarThickness(@FloatRange(from = 0) float thickness) {
        if (thickness < 0) {
            throw new IllegalArgumentException("Progress bar thickness must be a positive value.");
        }

        try {
            ((RingModeRenderer) renderer).setProgressBarThickness(thickness);
            invalidate();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Progress bar thickness is not support by the used percentage chart mode.");
        }
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
     * @throws IllegalArgumentException if the given progress bar style is not a valid ProgressBarStyle constant.
     */
    public void setProgressBarStyle(@ProgressBarStyle int style) {
        if (style < 0 || style > 1) {
            throw new IllegalArgumentException("Text style must be a valid TextStyle constant.");
        }

        try {
            ((RingModeRenderer) renderer).setProgressBarStyle(style);
            invalidate();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Progress bar style is not support by the used percentage chart mode.");
        }
    }

    //############################################################################################## UPDATE PIPELINE AS A FLUENT API

    /**
     * Sets the circular drawing direction. Default orientation is ORIENTATION_CLOCKWISE.
     *
     * @param orientation non-negative orientation constant.
     * @throws IllegalArgumentException if the given orientation is not a non-negative ProgressOrientation constant.
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
     * @throws IllegalArgumentException if the given offset is a negative value.
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
     * @throws IllegalArgumentException if the given value is negative.
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
     * @throws IllegalArgumentException if the given value is negative.
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
     * @throws IllegalArgumentException if the given progress bar style is not a valid ProgressBarStyle constant.
     */
    public PercentageChartView progressBarStyle(@ProgressBarStyle int style) {
        if (style < 0 || style > 1) {
            throw new IllegalArgumentException("Text style must be a valid TextStyle constant.");
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
        invalidate();
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
