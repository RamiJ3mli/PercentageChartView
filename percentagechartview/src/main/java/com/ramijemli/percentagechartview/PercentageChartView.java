package com.ramijemli.percentagechartview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.ramijemli.percentagechartview.annotation.ChartMode;
import com.ramijemli.percentagechartview.renderer.BaseModeRenderer;
import com.ramijemli.percentagechartview.renderer.PieModeRenderer;
import com.ramijemli.percentagechartview.renderer.RingModeRenderer;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.MODE_PIE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.MODE_RING;

public class PercentageChartView extends View implements IPercentageChartView {

    private BaseModeRenderer renderer;

    @ChartMode
    private int mode;

    @Nullable
    private OnProgressChangeListener onProgressChangeListener;
    @Nullable
    private AdaptiveColorProvider adaptiveColorProvider;

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
                    case MODE_PIE:
                        renderer = new PieModeRenderer(this, attrs);
                        break;
                }

            } finally {
                attrs.recycle();
                attrs = null;
            }

        } else {
            renderer = new PieModeRenderer(this);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        renderer.mesure(w, h, getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        renderer.destroy();
        renderer = null;

        if (onProgressChangeListener != null) {
            onProgressChangeListener = null;
        }

        if (adaptiveColorProvider != null) {
            adaptiveColorProvider = null;
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
    public void requestInvalidate() {
        invalidate();
    }

    @Override
    public void onProgressUpdated(float progress) {
        if (onProgressChangeListener != null)
            onProgressChangeListener.onProgressChanged(progress);
    }

    //STYLE MODIFIERS
    public void setProgress(@FloatRange(from = 0f, to = 100f) float progress, boolean animate) {
        if (renderer == null) return;
        renderer.setProgress(progress, animate);
    }

    //ADAPTIVE COLOR PROVIDER
    public void setAdaptiveColorProvider(@Nullable AdaptiveColorProvider adaptiveColorProvider) {
        this.adaptiveColorProvider = adaptiveColorProvider;
    }

    @Override
    public int getProvidedColor(float progress) {
        return (adaptiveColorProvider != null) ? adaptiveColorProvider.getColor(progress) : -1;
    }

    //LISTENER
    public void setOnProgressChangeListener(@Nullable OnProgressChangeListener onProgressChangeListener) {
        this.onProgressChangeListener = onProgressChangeListener;
    }

    public interface AdaptiveColorProvider {
        int getColor(float value);
    }

    public interface OnProgressChangeListener {
        void onProgressChanged(float progress);
    }
}
