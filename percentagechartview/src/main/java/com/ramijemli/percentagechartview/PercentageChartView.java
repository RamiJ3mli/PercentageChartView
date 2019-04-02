package com.ramijemli.percentagechartview;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.FloatRange;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

public class PercentageChartView extends View {

    // CHART MODE
    public static final int MODE_RING = 0;
    public static final int MODE_PIE = 1;


    // ORIENTATION
    public static final int ORIENTATION_CLOCKWISE = 0;
    public static final int ORIENTATION_COUNTERCLOCKWISE = 1;


    // BACKGROUND
    private static final float DEFAULT_BACKGROUND_DP_WIDTH = 16;
    private static final int DEFAULT_BACKGROUND_COLOR = Color.BLACK;

    private Paint mBackgroundPaint;
    private float mBackgroundWidth;
    private int mBackgroundColor;

    // BACKGROUND FILL
    private Paint mFillBackgroundPaint;
    private int mBackgroundFillColor;
    private boolean mFillBackground;
    private RectF mBackgroundFillBounds;

    // PROGRESS
    private static final float DEFAULT_PERCENTAGE_DP_WIDTH = 16;
    private static final int DEFAULT_PERCENTAGE_COLOR = Color.RED;
    public static final int CAP_ROUND = 0;
    public static final int CAP_SQUARE = 1;

    private Paint mPercentagePaint;
    private Paint.Cap percentageStyle;
    private float mPercentageWidth;
    private int mPercentageColor;
    private int mProvidedPercentageColor;


    // TEXT
    private static float DEFAULT_TEXT_SP_SIZE = 100;

    private Rect mTextBounds;
    private Paint mTextPaint;
    private float mTextSize;
    private int mTextStyle;
    private int mTextColor;
    private int mTextPercentage;
    private Typeface mTypeface;


    // ANIMATION INTERPOLATORS
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


    // COMMON
    private static final int DEFAULT_START_ANGLE = 0;
    private static final float MAX = 100;
    private static final int DEFAULT_ANIMATION_DURATION = 1000;
    private static final int DEFAULT_ANIMATION_INTERPOLATOR = 0;

    private ValueAnimator mValueAnimator;
    private ValueAnimator mColorAnimator;
    private Interpolator mAnimInterpolator;
    private RectF mCircleBounds;
    private int mAnimDuration;
    private float mPercentage;
    private float startAngle;
    private float arcAngle;
    @PercentageOrientation
    private int orientation;
    @ChartMode
    private int mode;

    private ColorProvider mColorProvider;

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

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        mCircleBounds = new RectF();
        mBackgroundFillBounds = new RectF();
        mTextBounds = new Rect();

        initAttributes(context, attrs);

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(mBackgroundColor);

        mPercentagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPercentagePaint.setColor(mPercentageColor);

        switch (mode) {
            case MODE_RING:

                mBackgroundPaint.setStyle(Paint.Style.STROKE);
                mBackgroundPaint.setStrokeCap(percentageStyle);
                mBackgroundPaint.setStrokeWidth(mBackgroundWidth);

                mFillBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mFillBackgroundPaint.setStyle(Paint.Style.FILL);
                mFillBackgroundPaint.setColor(mBackgroundFillColor);


                mPercentagePaint.setStyle(Paint.Style.STROKE);
                mPercentagePaint.setStrokeCap(percentageStyle);
                mPercentagePaint.setStrokeWidth(mPercentageWidth);
                break;

            case MODE_PIE:
                mBackgroundPaint.setStyle(Paint.Style.FILL);
                mPercentagePaint.setStyle(Paint.Style.FILL);
                break;
        }

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        if (mTypeface != null) {
            mTextPaint.setTypeface(mTypeface);
        }

        //ANIMATION
        mValueAnimator = ValueAnimator.ofFloat(0, mPercentage);
        mValueAnimator.setDuration(mAnimDuration);
        mValueAnimator.setInterpolator(mAnimInterpolator);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mPercentage = (float) valueAnimator.getAnimatedValue();

                if (mPercentage > 0 && mPercentage <= 100)
                    mTextPercentage = (int) mPercentage;
                else if (mPercentage > 100)
                    mTextPercentage = 100;
                else mTextPercentage = 0;

                invalidate();
            }
        });

    }

    private void initAttributes(@NonNull Context context, @Nullable AttributeSet attrs) {

        //ATTRIBUTES
        if (attrs != null) {

            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.PercentageChartView,
                    0, 0
            );

            try {

                //BACKGROUND COLOR
                mBackgroundColor = a.getColor(R.styleable.PercentageChartView_pcv_backgroundColor, DEFAULT_BACKGROUND_COLOR);

                //BACKGROUND WIDTH
                mBackgroundWidth = a.getDimensionPixelSize(R.styleable.PercentageChartView_pcv_backgroundWidth, dp2px(DEFAULT_BACKGROUND_DP_WIDTH));

                //BACKGROUND FILL ENABLE STATE
                mFillBackground = a.getBoolean(R.styleable.PercentageChartView_pcv_fillBackground, false);

                //BACKGROUND FILL COLOR
                mBackgroundFillColor = a.getColor(R.styleable.PercentageChartView_pcv_fillBackgroundColor, DEFAULT_BACKGROUND_COLOR);

                //PROGRESS
                mPercentage = mTextPercentage = a.getInt(R.styleable.PercentageChartView_pcv_progress, 0);

                //PROGRESS COLOR
                mPercentageColor = a.getColor(R.styleable.PercentageChartView_pcv_percentageColor, DEFAULT_PERCENTAGE_COLOR);

                //PROGRESS WIDTH
                mPercentageWidth = a.getDimensionPixelSize(R.styleable.PercentageChartView_pcv_percentageWidth, dp2px(DEFAULT_PERCENTAGE_DP_WIDTH));

                //PROGRESS BAR STROKE STYLE
                int cap = a.getInt(R.styleable.PercentageChartView_pcv_percentageStyle, CAP_ROUND);
                percentageStyle = (cap == CAP_ROUND) ? Paint.Cap.ROUND : Paint.Cap.BUTT;

                //TEXT COLOR
                mTextColor = a.getColor(R.styleable.PercentageChartView_pcv_textColor, DEFAULT_PERCENTAGE_COLOR);

                //TEXT SIZE
                mTextSize = a.getDimensionPixelSize(R.styleable.PercentageChartView_pcv_textSize, sp2px(DEFAULT_TEXT_SP_SIZE));

                //TEXT TYPEFACE
                String typeface = a.getString(R.styleable.PercentageChartView_pcv_typeface);
                if (typeface != null && !typeface.isEmpty()) {
                    mTypeface = Typeface.createFromAsset(getResources().getAssets(), typeface);
                }

                //TEXT STYLE
                mTextStyle = a.getInt(R.styleable.PercentageChartView_pcv_textStyle, Typeface.NORMAL);
                if (mTextStyle > 0) {
                    if (mTypeface == null) {
                        mTypeface = Typeface.defaultFromStyle(mTextStyle);
                    } else {
                        mTypeface = Typeface.create(mTypeface, mTextStyle);
                    }
                }

                //START DRAWING ANGLE
                startAngle = a.getInt(R.styleable.PercentageChartView_pcv_startAngle, DEFAULT_START_ANGLE);
                if (startAngle < 0 || startAngle > 360) {
                    startAngle = DEFAULT_START_ANGLE;
                }

                //DRAWING ORIENTATION
                orientation = a.getInt(R.styleable.PercentageChartView_pcv_orientation, ORIENTATION_CLOCKWISE);

                //CHART MODE
                mode = a.getInt(R.styleable.PercentageChartView_pcv_mode, MODE_RING);

                //PROGRESS ANIMATION DURATION
                mAnimDuration = a.getInt(R.styleable.PercentageChartView_pcv_animDuration, DEFAULT_ANIMATION_DURATION);

                //PROGRESS ANIMATION INTERPOLATOR
                int interpolator = a.getInt(R.styleable.PercentageChartView_pcv_animInterpolator, DEFAULT_ANIMATION_INTERPOLATOR);
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

            } finally {
                a.recycle();
            }

        } else {

            //DEFAULTS
            mPercentage = mTextPercentage = 0;
            mBackgroundColor = mBackgroundFillColor = DEFAULT_BACKGROUND_COLOR;
            mBackgroundWidth = dp2px(DEFAULT_BACKGROUND_DP_WIDTH);
            mFillBackground = false;

            percentageStyle = Paint.Cap.ROUND;
            mPercentageColor = DEFAULT_PERCENTAGE_COLOR;
            mPercentageWidth = dp2px(DEFAULT_PERCENTAGE_DP_WIDTH);

            mTextColor = mPercentageColor;
            mTextSize = sp2px(DEFAULT_TEXT_SP_SIZE);
            mTextStyle = Typeface.NORMAL;

            startAngle = DEFAULT_START_ANGLE;
            orientation = ORIENTATION_CLOCKWISE;
            mode = MODE_RING;
            mAnimDuration = DEFAULT_ANIMATION_DURATION;
            mAnimInterpolator = new LinearInterpolator();

        }

        arcAngle = mPercentage / MAX * 360;
        mProvidedPercentageColor = -1;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int diameter = Math.min(w, h);
        float maxOffset = Math.max(mPercentageWidth, mBackgroundWidth);

        if (mode == MODE_PIE) maxOffset = 0;

        int centerX = w / 2;
        int centerY = h / 2;
        float radius = (diameter - maxOffset) / 2;

        mCircleBounds.left = centerX - radius;
        mCircleBounds.top = centerY - radius;
        mCircleBounds.right = centerX + radius;
        mCircleBounds.bottom = centerY + radius;

        float fillBackgroundRadius = radius - (mBackgroundWidth / 2);
        mBackgroundFillBounds.left = centerX - fillBackgroundRadius;
        mBackgroundFillBounds.top = centerY - fillBackgroundRadius;
        mBackgroundFillBounds.right = centerX + fillBackgroundRadius;
        mBackgroundFillBounds.bottom = centerY + fillBackgroundRadius;

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mValueAnimator != null) {
            if (mValueAnimator.isRunning()) mValueAnimator.cancel();
            mValueAnimator.removeAllUpdateListeners();
            mValueAnimator = null;
        }
        if (mColorAnimator != null) {
            if (mColorAnimator.isRunning()) mColorAnimator.cancel();
            mColorAnimator.removeAllUpdateListeners();
            mColorAnimator = null;
        }

        mColorProvider = null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public ViewOutlineProvider getOutlineProvider() {
        return new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                if (mode == MODE_PIE) {
                    try {
                        outline.setOval((int) mCircleBounds.left,
                                (int) mCircleBounds.top,
                                (int) mCircleBounds.right,
                                (int) mCircleBounds.bottom);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //BACKGROUND FILL
        if (mode == MODE_RING && mFillBackground) {
            canvas.drawArc(mBackgroundFillBounds, 0, 360, true, mFillBackgroundPaint);
        }

        //BACKGROUND
        canvas.drawArc(mCircleBounds, 0, 360, (mode == MODE_PIE), mBackgroundPaint);

        //FOREGROUND
        if (mPercentage != 0) {
            if (mColorProvider != null)
                mPercentagePaint.setColor(mProvidedPercentageColor);
            arcAngle = this.mPercentage / MAX * 360;
            canvas.drawArc(mCircleBounds, startAngle, arcAngle, (mode == MODE_PIE), mPercentagePaint);
        }

        //TEXT
        String text = String.valueOf(mTextPercentage) + "%";
        mTextPaint.getTextBounds(text, 0, text.length(), mTextBounds);
        int textHeight = mTextBounds.height();
        canvas.drawText(text, mCircleBounds.centerX(), mCircleBounds.centerY() + (textHeight / 2f), mTextPaint);
    }

    public float getPercentage() {
        return mPercentage;
    }

    public void setPercentage(@FloatRange(from = 0f, to = 100f) float percentage, boolean animate) {
        if (this.mPercentage == percentage) return;

        if (mValueAnimator.isRunning()) mValueAnimator.cancel();
        if (mColorAnimator != null && mColorAnimator.isRunning()) mColorAnimator.cancel();

        if (!animate) {
            this.mPercentage = percentage;
            invalidate();
            return;
        }

        mValueAnimator.setFloatValues(mPercentage, percentage);
        mValueAnimator.start();

        if (mColorProvider != null) {
            int startColor = mProvidedPercentageColor != -1 ? mProvidedPercentageColor : mPercentageColor;
            int endColor = mColorProvider.getColor(percentage);
            mColorAnimator.setIntValues(startColor, endColor);
            mColorAnimator.start();
        }
    }

    public @PercentageStyle
    int getPercentageStyle() {
        return (percentageStyle == Paint.Cap.ROUND) ? CAP_ROUND : CAP_SQUARE;
    }

    public void setPercentageStyle(@PercentageStyle int percentageStyle) {
        if (mode == MODE_PIE) return;
        this.percentageStyle = (percentageStyle == CAP_ROUND) ? Paint.Cap.ROUND : Paint.Cap.BUTT;
        invalidate();
    }

    public float getPercentageWidth() {
        return mPercentageWidth;
    }

    public void setPercentageWidth(float mPercentageWidth) {
        if (mode == MODE_PIE) return;
        this.mPercentageWidth = mPercentageWidth;
        invalidate();
    }

    public int getPercentageColor() {
        return mPercentageColor;
    }

    public void setPercentageColor(int mPercentageColor) {
        this.mPercentageColor = mPercentageColor;
        invalidate();
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float mTextSize) {
        this.mTextSize = mTextSize;
        invalidate();
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
        invalidate();
    }

    public int getAnimDuration() {
        return mAnimDuration;
    }

    public void setAnimDuration(int mAnimDuration) {
        this.mAnimDuration = mAnimDuration;
        invalidate();
    }

    public float getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(@FloatRange(from = 0f, to = 360f) float startAngle) {
        this.startAngle = startAngle;
        invalidate();
    }

    public @PercentageOrientation
    int getOrientation() {
        return orientation;
    }

    public void setOrientation(@PercentageOrientation int orientation) {
        this.orientation = orientation;
    }

    public @ChartMode
    int getMode() {
        return mode;
    }

    public @TextStyle
    int getTextStyle() {
        return mTextStyle;
    }

    public void setTextStyle(@TextStyle int mTextStyle) {
        this.mTextStyle = mTextStyle;
        if (mTextStyle > 0) {
            if (mTypeface == null) {
                mTypeface = Typeface.defaultFromStyle(mTextStyle);
            } else {
                mTypeface = Typeface.create(mTypeface, mTextStyle);
            }
        }
        invalidate();
    }

    public Typeface getTypeface() {
        return mTypeface;
    }

    public void setTypeface(@NonNull Typeface typeFace) {
        if (mTextStyle > 0) {
            mTypeface = Typeface.create(mTypeface, mTextStyle);
        } else {
            mTypeface = typeFace;
        }
        mTextPaint.setTypeface(mTypeface);
        invalidate();
    }


    public void setColorProvider(ColorProvider colorProvider) {
        this.mColorProvider = colorProvider;
        if (mColorProvider != null && mColorAnimator == null) {

            mColorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), mPercentageColor, mColorProvider.getColor(mTextPercentage));
            mColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mProvidedPercentageColor = (int) animation.getAnimatedValue();
                }
            });
            mColorAnimator.setDuration(mAnimDuration);
            mColorAnimator.setInterpolator(mAnimInterpolator);
            mProvidedPercentageColor = mColorProvider.getColor(mTextPercentage);
            invalidate();
        } else if (mColorProvider == null)
            mColorAnimator = null;
    }

    private int dp2px(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    private int sp2px(float sp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MODE_PIE, MODE_RING})
    public @interface ChartMode {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ORIENTATION_CLOCKWISE, ORIENTATION_COUNTERCLOCKWISE})
    public @interface PercentageOrientation {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CAP_ROUND, CAP_SQUARE})
    public @interface PercentageStyle {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Typeface.NORMAL, Typeface.ITALIC, Typeface.BOLD, Typeface.BOLD_ITALIC})
    public @interface TextStyle {
    }

    public interface ColorProvider {
        int getColor(float value);
    }
}
