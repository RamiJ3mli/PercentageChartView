package com.ramijemli.sample;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.ramijemli.percentagechartview.PercentageChartView;
import com.ramijemli.percentagechartview.renderer.BaseModeRenderer;
import com.ramijemli.percentagechartview.renderer.RingModeRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.ACCELERATE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.ACCELERATE_DECELERATE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.ANTICIPATE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.ANTICIPATE_OVERSHOOT;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.BOUNCE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.DECELERATE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.FAST_OUT_LINEAR_IN;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.FAST_OUT_SLOW_IN;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.LINEAR_OUT_SLOW_IN;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.OVERSHOOT;


public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.root_layout)
    ConstraintLayout mConstraintLayout;
    @BindView(R.id.background)
    View mBackground;
    @BindView(R.id.toggle_light)
    ImageView mLightToggle;

    //CHARTS
    @BindView(R.id.pie_chart)
    PercentageChartView pieChart;
    @BindView(R.id.ring_chart)
    PercentageChartView ringChart;

    //ORIENTATION
    @BindView(R.id.orientation_value)
    RadioGroup mOrientationValue;

    //START ANGLE
    @BindView(R.id.start_angle_value)
    SeekBar mStartAngleValue;
    @BindView(R.id.start_angle_value_label)
    TextView mStartAngleLabel;

    //PROGRESS
    @BindView(R.id.progress_value)
    SeekBar mProgressValue;
    @BindView(R.id.progress_value_label)
    TextView mProgressLabel;
    @BindView(R.id.animate)
    CheckBox mAnimateProgress;

    //DURATION
    @BindView(R.id.duration_value)
    TextView mDurationValue;

    //INTERPOLATOR
    @BindView(R.id.interpolator_value)
    Spinner mInterpolatorValue;

    //DRAW BACKGROUND STATE
    @BindView(R.id.draw_background)
    CheckBox mDrawBackground;

    //TEXT COLOR
    @BindView(R.id.text_color)
    Button mTextColor;

    //TEXT SIZE
    @BindView(R.id.text_size_value)
    TextView mTextSizeValue;

    //TEXT FONT
    @BindView(R.id.font_value)
    Spinner mFontValue;

    //TEXT STYLE
    @BindView(R.id.text_style_value)
    Spinner mTextStyleValue;

    //USE SHADOW STATE
    @BindView(R.id.use_shadow)
    CheckBox mUseShadow;

    //SHADOW COLOR
    @BindView(R.id.shadow_color)
    Button mShadowColor;

    //SHADOW RADIUS
    @BindView(R.id.radius_value)
    TextView mRadiusValue;

    //DURATION
    @BindView(R.id.distx_value)
    TextView mDistXValue;

    //DURATION
    @BindView(R.id.disty_value)
    TextView mDistYValue;

    //BACKGROUND COLOR
    @BindView(R.id.background_color)
    Button mBackgroundColor;

    //BACKGROUND OFFSET
    @BindView(R.id.offset_value)
    TextView mOffsetValue;

    //PROGRESS COLOR
    @BindView(R.id.progress_color)
    Button mProgressColor;

    //PROGRESS BAR THICKNESS
    @BindView(R.id.prog_thickness_value)
    TextView mPgBarThicknessValue;

    //PROGRESS BAR STYLE
    @BindView(R.id.prog_bar_style_value)
    RadioGroup mPgBarStyle;

    //DRAW BACKGROUND BAR STATE
    @BindView(R.id.draw_bg_bar)
    CheckBox mDrawBgBar;

    //BACKGROUND BAR COLOR
    @BindView(R.id.bg_bar_color)
    Button mBgBarColor;

    //BACKGROUND BAR THICKNESS
    @BindView(R.id.bg_bar_thickness_value)
    TextView mBgBarThicknessValue;

    //PROVIDED COLORS
    @BindView(R.id.color_one)
    Button mColorOne;
    @BindView(R.id.color_two)
    Button mColorTwo;
    @BindView(R.id.color_three)
    Button mColorThree;
    @BindView(R.id.color_four)
    Button mColorFour;
    @BindView(R.id.use_provider)
    CheckBox mUseProvider;

    //ADAPT TEXT COLOR STATE
    @BindView(R.id.use_adaptive_text)
    CheckBox mAdaptText;

    //ADAPT TEXT COLOR RATIO
    @BindView(R.id.text_ratio_seekbar)
    SeekBar mTextRatioSeekbar;
    @BindView(R.id.text_ratio_value)
    TextView mTextRatioValue;

    //ADAPT TEXT COLOR MODE
    @BindView(R.id.text_mode_value)
    RadioGroup mTextModeValue;

    //ADAPT BACKGROUND COLOR STATE
    @BindView(R.id.use_adaptive_bg)
    CheckBox mAdaptBackground;

    //ADAPT BACKGROUND COLOR RATIO
    @BindView(R.id.bg_ratio_seekbar)
    SeekBar mBgRatioSeekbar;
    @BindView(R.id.bg_ratio_value)
    TextView mBgRatioValue;

    //ADAPT BACKGROUND COLOR MODE
    @BindView(R.id.bg_mode_value)
    RadioGroup mBgModeValue;

    //ADAPT BG BAR COLOR STATE
    @BindView(R.id.use_adaptive_bg_bar)
    CheckBox mAdaptBgBar;

    //ADAPT BG BAR COLOR RATIO
    @BindView(R.id.bg_bar_ratio_seekbar)
    SeekBar mBgBarRatioSeekbar;
    @BindView(R.id.bg_bar_ratio_value)
    TextView mBgBarRatioValue;

    //ADAPT BG BAR COLOR MODE
    @BindView(R.id.bg_bar_mode_value)
    RadioGroup mBgBarModeValue;

    private Unbinder unbinder;
    private ConstraintSet mConstraintSet;
    private Transition transition;
    private ObjectAnimator bgAnimator;

    private PercentageChartView.AdaptiveColorProvider colorProvider;
    private boolean isFromUser;
    private boolean isPieDisplayed;
    private boolean isDarkDisplayed;
    private int shadowColor;
    private int colorOne;
    private int colorTwo;
    private int colorThree;
    private int colorFour;
    private int darkColor;
    private int lightColor;
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
        
        setupLayoutAnimation();
        setupOrientation();
        setupStartAngle();
        setupProgress();
        setupInterpolator();
        setupDrawBackgroundState();
        setupTextFont();
        setupTextStyle();
        setupTextShadow();
        setupPgBarStyle();
        setupDrawBgBarState();
        setupColorProvider();
        setupAdaptiveText();
        setupAdaptiveBackground();
        setupAdaptiveBgBar();
    }

    @Override
    protected void onDestroy() {
        mOrientationValue.setOnCheckedChangeListener(null);
        mInterpolatorValue.setOnItemSelectedListener(null);
        mTextStyleValue.setOnItemSelectedListener(null);
        mFontValue.setOnItemSelectedListener(null);
        mUseShadow.setOnCheckedChangeListener(null);
        mDrawBackground.setOnCheckedChangeListener(null);
        mPgBarStyle.setOnCheckedChangeListener(null);
        mDrawBgBar.setOnCheckedChangeListener(null);
        mUseProvider.setOnCheckedChangeListener(null);
        mAdaptText.setOnCheckedChangeListener(null);
        mAdaptBackground.setOnCheckedChangeListener(null);
        mAdaptBgBar.setOnCheckedChangeListener(null);
        mTextRatioSeekbar.setOnSeekBarChangeListener(null);
        mBgRatioSeekbar.setOnSeekBarChangeListener(null);
        mBgBarRatioSeekbar.setOnSeekBarChangeListener(null);
        mTextModeValue.setOnCheckedChangeListener(null);
        mBgModeValue.setOnCheckedChangeListener(null);
        mBgBarModeValue.setOnCheckedChangeListener(null);

        bgAnimator = null;
        transition = null;
        colorProvider = null;
        mConstraintSet = null;
        unbinder.unbind();
        unbinder = null;
        super.onDestroy();
    }

    //INIT
    private void setupLayoutAnimation() {
        isPieDisplayed = true;
        mConstraintSet = new ConstraintSet();
        mConstraintSet.clone(mConstraintLayout);
        transition = new ChangeBounds();
        transition.setDuration(600);
        transition.setInterpolator(new OvershootInterpolator());

        isDarkDisplayed = true;
        darkColor = Color.parseColor("#263238");
        lightColor = Color.parseColor("#eeeeee");
        bgAnimator = ObjectAnimator.ofArgb(mBackground, "backgroundColor", Color.parseColor("#263238"), Color.WHITE);
        bgAnimator.setDuration(400);
    }

    private void setupOrientation() {
        mOrientationValue.setOnCheckedChangeListener((group, checkedId) -> {
            if (isPieDisplayed) {
                pieChart.setOrientation(checkedId == R.id.clockwise ? BaseModeRenderer.ORIENTATION_CLOCKWISE : BaseModeRenderer.ORIENTATION_COUNTERCLOCKWISE);
            } else {
                ringChart.setOrientation(checkedId == R.id.clockwise ? BaseModeRenderer.ORIENTATION_CLOCKWISE : BaseModeRenderer.ORIENTATION_COUNTERCLOCKWISE);
            }
        });
    }

    private void setupStartAngle() {
        mStartAngleValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int startAngle, boolean fromUser) {
                if (isPieDisplayed) {
                    pieChart.setStartAngle(startAngle);
                } else {
                    ringChart.setStartAngle(startAngle);
                }
                mStartAngleLabel.setText(String.valueOf(startAngle));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setupProgress() {
        mProgressValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mProgressLabel.setText(String.valueOf(progress));
                isFromUser = fromUser;

                if (!fromUser || mAnimateProgress.isChecked()) return;

                if (isPieDisplayed) {
                    pieChart.setProgress(progress, false);
                } else {
                    ringChart.setProgress(progress, false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mAnimateProgress.isChecked() && isFromUser) {
                    if (isPieDisplayed) {
                        pieChart.setProgress(seekBar.getProgress(), mAnimateProgress.isChecked());
                    } else {
                        ringChart.setProgress(seekBar.getProgress(), mAnimateProgress.isChecked());
                    }
                    mProgressLabel.setText(String.valueOf(seekBar.getProgress()));
                }
            }
        });

        //PROGRESS CHANGE LISTENER
        ringChart.setOnProgressChangeListener(progress -> {
            Log.d(TAG, String.valueOf(progress));
        });

        pieChart.setOnProgressChangeListener(progress -> {
            Log.d(TAG, String.valueOf(progress));
        });

        //ANIMATE STATE CHANGE LISTENER
        mAnimateProgress.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (getTextViewValue(mDurationValue) == 0) {
                    mDurationValue.setText("1000 ms");
                    if (isPieDisplayed) {
                        pieChart.setAnimationDuration(1000);
                    } else {
                        ringChart.setAnimationDuration(1000);
                    }
                }
            }
        });
    }

    private void setupInterpolator() {

        List<String> data = new ArrayList<>();
        data.add("LINEAR");
        data.add("ACCELERATE");
        data.add("DECELERATE");
        data.add("ACCELERATE_DECELERATE");
        data.add("ANTICIPATE");
        data.add("OVERSHOOT");
        data.add("ANTICIPATE_OVERSHOOT");
        data.add("BOUNCE");
        data.add("FAST_OUT_LINEAR_IN");
        data.add("FAST_OUT_SLOW_IN");
        data.add("LINEAR_OUT_SLOW_IN");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.item_interpolator, data);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mInterpolatorValue.setAdapter(dataAdapter);
        mInterpolatorValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TimeInterpolator interpolator;
                switch (position) {
                    default:
                        interpolator = new LinearInterpolator();
                        break;
                    case ACCELERATE:
                        interpolator = new AccelerateInterpolator();
                        break;
                    case DECELERATE:
                        interpolator = new DecelerateInterpolator();
                        break;
                    case ACCELERATE_DECELERATE:
                        interpolator = new AccelerateDecelerateInterpolator();
                        break;
                    case ANTICIPATE:
                        interpolator = new AnticipateInterpolator();
                        break;
                    case OVERSHOOT:
                        interpolator = new OvershootInterpolator();
                        break;
                    case ANTICIPATE_OVERSHOOT:
                        interpolator = new AnticipateOvershootInterpolator();
                        break;
                    case BOUNCE:
                        interpolator = new BounceInterpolator();
                        break;
                    case FAST_OUT_LINEAR_IN:
                        interpolator = new FastOutLinearInInterpolator();
                        break;
                    case FAST_OUT_SLOW_IN:
                        interpolator = new FastOutSlowInInterpolator();
                        break;
                    case LINEAR_OUT_SLOW_IN:
                        interpolator = new LinearOutSlowInInterpolator();
                        break;
                }
                if (isPieDisplayed) {
                    pieChart.setAnimationInterpolator(interpolator);
                } else {
                    ringChart.setAnimationInterpolator(interpolator);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupDrawBackgroundState() {
        mDrawBackground.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String text = String.valueOf(isChecked);
            mDrawBackground.setText(Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase());
            if (isPieDisplayed) {
                pieChart.setDrawBackgroundEnabled(isChecked);
            } else {
                ringChart.setDrawBackgroundEnabled(isChecked);
            }
        });
    }

    private void setupTextFont() {
        List<String> data = new ArrayList<>();
        data.add("SYSTEM FONT (DEFAULT)");
        data.add("INTERSTELLAR");
        data.add("MARSHMELLOWS");
        data.add("NUAZ");
        data.add("WHALE I TRIED");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.item_interpolator, data);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFontValue.setAdapter(dataAdapter);
        mFontValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String font = null;

                switch (position) {
                    case 1:
                        font = "interstellar.ttf";
                        break;
                    case 2:
                        font = "marshmallows.ttf";
                        break;
                    case 3:
                        font = "nuaz.otf";
                        break;
                    case 4:
                        font = "whaleitried.ttf";
                        break;
                }

                Typeface typeface = (position == 0) ?
                        Typeface.defaultFromStyle(Typeface.NORMAL) :
                        Typeface.createFromAsset(getApplicationContext().getResources().getAssets(), font);
                if (isPieDisplayed) {
                    pieChart.setTypeface(typeface);
                } else {
                    ringChart.setTypeface(typeface);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupTextStyle() {
        List<String> data = new ArrayList<>();
        data.add("NORMAL");
        data.add("BOLD");
        data.add("ITALIC");
        data.add("BOLD_ITALIC");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, R.layout.item_interpolator, data);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTextStyleValue.setAdapter(dataAdapter);
        mTextStyleValue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int textStyle = -1;

                switch (position) {
                    case 0:
                        textStyle = Typeface.NORMAL;
                        break;
                    case 1:
                        textStyle = Typeface.BOLD;
                        break;
                    case 2:
                        textStyle = Typeface.ITALIC;
                        break;
                    case 3:
                        textStyle = Typeface.BOLD_ITALIC;
                        break;
                }

                if (isPieDisplayed) {
                    pieChart.setTextStyle(textStyle);
                } else {
                    ringChart.setTextStyle(textStyle);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupTextShadow() {
        mUseShadow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isPieDisplayed) {
                pieChart.setTextShadow(isChecked ? shadowColor : 0,
                        isChecked ? Float.parseFloat(mRadiusValue.getText().toString()) : 0,
                        isChecked ? Float.parseFloat(mDistXValue.getText().toString()) : 0,
                        isChecked ? Float.parseFloat(mDistYValue.getText().toString()) : 0
                );
            } else {
                ringChart.setTextShadow(isChecked ? shadowColor : 0,
                        isChecked ? Float.parseFloat(mRadiusValue.getText().toString()) : 0,
                        isChecked ? Float.parseFloat(mDistXValue.getText().toString()) : 0,
                        isChecked ? Float.parseFloat(mDistYValue.getText().toString()) : 0
                );
            }
        });
    }

    private void setupPgBarStyle() {
        mPgBarStyle.setOnCheckedChangeListener((group, checkedId) -> {
            if (isPieDisplayed) {
                Toast.makeText(this, "Applicable only to ring chart!", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (checkedId) {
                case R.id.round:
                    ringChart.setProgressBarStyle(RingModeRenderer.CAP_ROUND);
                    break;
                case R.id.square:
                    ringChart.setProgressBarStyle(RingModeRenderer.CAP_SQUARE);
                    break;
            }
        });
    }

    private void setupDrawBgBarState() {
        mDrawBgBar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String text = String.valueOf(isChecked);
            mDrawBgBar.setText(Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase());
            if (!isPieDisplayed) {
                ringChart.setDrawBackgroundBarEnabled(isChecked);
            } else {
                Toast.makeText(this, "Applicable only to ring chart!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupColorProvider() {
        colorOne = Color.parseColor("#F44336");
        colorTwo = Color.parseColor("#FFEA00");
        colorThree = Color.parseColor("#03A9F4");
        colorFour = Color.parseColor("#00E676");

        //COLOR PROVIDER
        colorProvider = value -> {
            if (value <= 25)
                return colorOne;
            else if (value <= 50)
                return colorTwo;
            else if (value <= 75)
                return colorThree;
            else
                return colorFour;
        };

        mUseProvider.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isPieDisplayed) {
                pieChart.setAdaptiveColorProvider(isChecked ? colorProvider : null);
            } else {
                ringChart.setAdaptiveColorProvider(isChecked ? colorProvider : null);
            }
        });
    }

    private void setupAdaptiveText() {
        mAdaptText.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!mUseProvider.isChecked()) {
                mAdaptText.setChecked(false);
                Toast.makeText(this, "Please enable color provider first!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isPieDisplayed) {
                pieChart.setAdaptiveTextEnabled(isChecked);
            } else {
                ringChart.setAdaptiveTextEnabled(isChecked);
            }
        });

        mTextRatioSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextRatioValue.setText(progress + "%");
                mAdaptText.setChecked(true);
                if (isPieDisplayed) {
                    pieChart.setAdaptiveText(progress,
                            mTextModeValue.getCheckedRadioButtonId() == R.id.text_darker ? BaseModeRenderer.DARKER_MODE : BaseModeRenderer.LIGHTER_MODE);
                } else {
                    ringChart.setAdaptiveText(progress,
                            mTextModeValue.getCheckedRadioButtonId() == R.id.text_darker ? BaseModeRenderer.DARKER_MODE : BaseModeRenderer.LIGHTER_MODE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mTextModeValue.setOnCheckedChangeListener((group, checkedId) -> {
            mAdaptText.setChecked(true);
            if (isPieDisplayed) {
                pieChart.setAdaptiveText(mTextRatioSeekbar.getProgress(),
                        mTextModeValue.getCheckedRadioButtonId() == R.id.text_darker ? BaseModeRenderer.DARKER_MODE : BaseModeRenderer.LIGHTER_MODE);
            } else {
                ringChart.setAdaptiveText(mTextRatioSeekbar.getProgress(),
                        mTextModeValue.getCheckedRadioButtonId() == R.id.text_darker ? BaseModeRenderer.DARKER_MODE : BaseModeRenderer.LIGHTER_MODE);
            }
        });
    }

    private void setupAdaptiveBackground() {
        mAdaptBackground.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!mUseProvider.isChecked()) {
                mAdaptBackground.setChecked(false);
                Toast.makeText(this, "Please enable color provider first!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isPieDisplayed) {
                pieChart.setAdaptiveBackgroundEnabled(isChecked);
            } else {
                ringChart.setAdaptiveBackgroundEnabled(isChecked);
            }
        });

        mBgRatioSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mBgRatioValue.setText(progress + "%");
                mAdaptBackground.setChecked(true);
                if (isPieDisplayed) {
                    pieChart.setAdaptiveBackground(progress,
                            mBgModeValue.getCheckedRadioButtonId() == R.id.bg_darker ? BaseModeRenderer.DARKER_MODE : BaseModeRenderer.LIGHTER_MODE);
                } else {
                    ringChart.setAdaptiveBackground(progress,
                            mBgModeValue.getCheckedRadioButtonId() == R.id.bg_darker ? BaseModeRenderer.DARKER_MODE : BaseModeRenderer.LIGHTER_MODE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mBgModeValue.setOnCheckedChangeListener((group, checkedId) -> {
            mAdaptBackground.setChecked(true);
            if (isPieDisplayed) {
                pieChart.setAdaptiveBackground(mBgRatioSeekbar.getProgress(),
                        mBgModeValue.getCheckedRadioButtonId() == R.id.bg_darker ? BaseModeRenderer.DARKER_MODE : BaseModeRenderer.LIGHTER_MODE);
            } else {
                ringChart.setAdaptiveBackground(mBgRatioSeekbar.getProgress(),
                        mBgModeValue.getCheckedRadioButtonId() == R.id.bg_darker ? BaseModeRenderer.DARKER_MODE : BaseModeRenderer.LIGHTER_MODE);
            }
        });
    }

    private void setupAdaptiveBgBar() {
        mAdaptBgBar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!mUseProvider.isChecked() && isChecked) {
                mAdaptBgBar.setChecked(false);
                Toast.makeText(this, "Please enable color provider first!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isPieDisplayed) {
                ringChart.setAdaptiveBgBarEnabled(isChecked);
            } else {
                Toast.makeText(this, "Applicable only to ring chart!", Toast.LENGTH_SHORT).show();
            }
        });

        mBgBarRatioSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mBgBarRatioValue.setText(progress + "%");
                mAdaptBgBar.setChecked(true);
                if (!isPieDisplayed) {
                    ringChart.setAdaptiveBackgroundBar(progress,
                            mBgBarModeValue.getCheckedRadioButtonId() == R.id.bg_bar_darker ? BaseModeRenderer.DARKER_MODE : BaseModeRenderer.LIGHTER_MODE);
                } else {
                    Toast.makeText(MainActivity.this, "Applicable only to ring chart!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mBgBarModeValue.setOnCheckedChangeListener((group, checkedId) -> {
            mAdaptBgBar.setChecked(true);
            if (!isPieDisplayed) {
                ringChart.setAdaptiveBackgroundBar(mBgBarRatioSeekbar.getProgress(),
                        mBgBarModeValue.getCheckedRadioButtonId() == R.id.bg_bar_darker ? BaseModeRenderer.DARKER_MODE : BaseModeRenderer.LIGHTER_MODE);
            } else {
                Toast.makeText(this, "Applicable only to ring chart!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //VALUE TWEAKERS
    private void tweakAnimDuration(int amount) {
        int value = getTextViewValue(mDurationValue);

        if (value + amount > 4000) {
            value += amount;
            Toast.makeText(this, "Welcome to the boring animation hell!", Toast.LENGTH_SHORT).show();
        } else if (value + amount <= 0) {
            value = 0;
            mAnimateProgress.setChecked(false);
        } else {
            mAnimateProgress.setChecked(true);
            value += amount;
        }

        mDurationValue.setText(value + " ms");
        if (isPieDisplayed) {
            pieChart.setAnimationDuration(value);
        } else {
            ringChart.setAnimationDuration(value);
        }
    }

    private void tweakTextSize(int size) {
        int value = getTextViewValue(mTextSizeValue);

        if (value + size > 100) {
            value = 100;
        } else if (value + size <= 10) {
            value = 10;
        } else {
            value += size;
        }

        mTextSizeValue.setText(value + " sp");
        if (isPieDisplayed) {
            pieChart.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    value,
                    getApplicationContext().getResources().getDisplayMetrics()
            ));
        } else {
            ringChart.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    value,
                    getApplicationContext().getResources().getDisplayMetrics()
            ));
        }
    }

    private void tweakShadowRadius(int amount) {
        float value = Float.parseFloat(mRadiusValue.getText().toString());

        if (value + amount > 100) {
            value = 100;
        } else if (value + amount < 1) {
            value = 1;
        } else {
            value += amount;
        }

        mRadiusValue.setText(String.valueOf(value));
        if (isPieDisplayed) {
            pieChart.setTextShadow(shadowColor,
                    value,
                    Float.parseFloat(mDistXValue.getText().toString()),
                    Float.parseFloat(mDistYValue.getText().toString())
            );
        } else {
            ringChart.setTextShadow(shadowColor,
                    value,
                    Float.parseFloat(mDistXValue.getText().toString()),
                    Float.parseFloat(mDistYValue.getText().toString())
            );
        }
    }

    private void tweakDistX(int amount) {
        float value = Float.parseFloat(mDistXValue.getText().toString());

        if (value + amount > 36) {
            value = 36;
        } else if (value + amount < 0) {
            value = 0;
        } else {
            value += amount;
        }

        mDistXValue.setText(String.valueOf(value));
        if (isPieDisplayed) {
            pieChart.setTextShadow(shadowColor,
                    Float.parseFloat(mRadiusValue.getText().toString()),
                    value,
                    Float.parseFloat(mDistYValue.getText().toString())
            );
        } else {
            ringChart.setTextShadow(shadowColor,
                    Float.parseFloat(mRadiusValue.getText().toString()),
                    value,
                    Float.parseFloat(mDistYValue.getText().toString())
            );
        }
    }

    private void tweakDistY(int amount) {
        float value = Float.parseFloat(mDistYValue.getText().toString());

        if (value + amount > 36) {
            value = 36;
        } else if (value + amount < 0) {
            value = 0;
        } else {
            value += amount;
        }

        mDistYValue.setText(String.valueOf(value));
        if (isPieDisplayed) {
            pieChart.setTextShadow(shadowColor,
                    Float.parseFloat(mRadiusValue.getText().toString()),
                    Float.parseFloat(mDistXValue.getText().toString()),
                    value
            );
        } else {
            ringChart.setTextShadow(shadowColor,
                    Float.parseFloat(mRadiusValue.getText().toString()),
                    Float.parseFloat(mDistXValue.getText().toString()),
                    value
            );
        }
    }

    private void tweakOffset(int amount) {
        int value = getTextViewValue(mOffsetValue);

        if (value + amount > pieChart.getMeasuredHeight() / 2) {
            value = pieChart.getMeasuredHeight() / 2;
        } else if (value + amount <= 0) {
            value = 0;
        } else {
            value += amount;
        }

        mOffsetValue.setText(value + " dp");
        if (isPieDisplayed) {
            pieChart.setBackgroundOffset((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    value,
                    getApplicationContext().getResources().getDisplayMetrics())
            );
        } else {
            Toast.makeText(this, "Applicable only to pie chart!", Toast.LENGTH_SHORT).show();
        }
    }

    private void tweakPgThickness(int amount) {
        int value = getTextViewValue(mPgBarThicknessValue);

        if (value + amount > pieChart.getMeasuredHeight() / 2) {
            value = pieChart.getMeasuredHeight() / 2;
        } else if (value + amount <= 0) {
            value = 0;
        } else {
            value += amount;
        }

        mPgBarThicknessValue.setText(value + " dp");
        if (!isPieDisplayed) {
            ringChart.setProgressBarThickness((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    value,
                    getApplicationContext().getResources().getDisplayMetrics()
            ));
        } else {
            Toast.makeText(this, "Applicable only to ring chart!", Toast.LENGTH_SHORT).show();
        }
    }

    private void tweakBgThickness(int amount) {
        int value = getTextViewValue(mBgBarThicknessValue);

        if (value + amount > pieChart.getMeasuredHeight() / 2) {
            value = pieChart.getMeasuredHeight() / 2;
        } else if (value + amount <= 0) {
            value = 0;
        } else {
            value += amount;
        }

        mBgBarThicknessValue.setText(value + " dp");
        if (isPieDisplayed) {
            pieChart.setBackgroundBarThickness((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    value,
                    getApplicationContext().getResources().getDisplayMetrics()
            ));
        } else {
            ringChart.setBackgroundBarThickness((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    value,
                    getApplicationContext().getResources().getDisplayMetrics()
            ));
        }
    }

    private int getTextViewValue(TextView view) {
        String value = view.getText().toString();
        return Integer.parseInt(value.substring(0, value.length() - 3));
    }

    private void centerHorizontally(int viewId, int toViewId) {
        mConstraintSet.connect(viewId, ConstraintSet.START, toViewId, ConstraintSet.START);
        mConstraintSet.connect(viewId, ConstraintSet.END, toViewId, ConstraintSet.END);
    }

    private void applyTransition() {
        TransitionManager.beginDelayedTransition(mConstraintLayout, transition);
        mConstraintSet.applyTo(mConstraintLayout);
    }

    private void resetLayout() {
        mAdaptText.setOnCheckedChangeListener(null);
        mAdaptBackground.setOnCheckedChangeListener(null);
        mAdaptBgBar.setOnCheckedChangeListener(null);
        mTextRatioSeekbar.setOnSeekBarChangeListener(null);
        mBgRatioSeekbar.setOnSeekBarChangeListener(null);
        mBgBarRatioSeekbar.setOnSeekBarChangeListener(null);
        mTextModeValue.setOnCheckedChangeListener(null);
        mBgModeValue.setOnCheckedChangeListener(null);
        mBgBarModeValue.setOnCheckedChangeListener(null);

        mOrientationValue.check(R.id.clockwise);

        mStartAngleValue.setProgress(90);
        mStartAngleLabel.setText("90");

        mProgressValue.setProgress(10);
        mProgressLabel.setText("10");

        mAnimateProgress.setChecked(true);
        mDurationValue.setText("400 ms");
        mInterpolatorValue.setSelection(0);

        mTextColor.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        mTextSizeValue.setText("50 sp");
        mFontValue.setSelection(0);
        mTextStyleValue.setSelection(0);

        mUseShadow.setChecked(false);
        mShadowColor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#dddddd")));
        mRadiusValue.setText("2.0");
        mDistXValue.setText("2.0");
        mDistYValue.setText("2.0");

        mBackgroundColor.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));

        mProgressColor.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent)));

        if (mUseProvider.isChecked()) {
            mAdaptText.setChecked(false);
            mAdaptBackground.setChecked(false);
            mAdaptBgBar.setChecked(false);
            mUseProvider.setChecked(false);
        }

        mTextRatioSeekbar.setProgress(50);
        mTextModeValue.check(R.id.text_lighter);

        mBgRatioSeekbar.setProgress(50);
        mBgModeValue.check(R.id.bg_darker);

        if (isPieDisplayed) {
            mDrawBackground.setChecked(true);
            mOffsetValue.setText("0 dp");
        } else {
            mDrawBackground.setChecked(false);
            mDrawBgBar.setChecked(true);
            mBgBarColor.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            mBgBarRatioSeekbar.setProgress(50);
            mBgBarModeValue.check(R.id.bg_bar_darker);
            mBgBarThicknessValue.setText("24 dp");
            mPgBarThicknessValue.setText("24 dp");
            mPgBarStyle.check(R.id.round);
        }

        if (isPieDisplayed) {
            pieChart.setOrientation(BaseModeRenderer.ORIENTATION_CLOCKWISE);
            pieChart.setProgress(10, false);
            pieChart.setStartAngle(90);
            pieChart.setAnimationDuration(400);
            pieChart.setAnimationInterpolator(new LinearInterpolator());
            pieChart.setTextColor(Color.WHITE);
            pieChart.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    50,
                    getResources().getDisplayMetrics()));
            pieChart.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            pieChart.setTextShadow(0, 0, 0, 0);
            pieChart.setDrawBackgroundEnabled(true);
            pieChart.setBackgroundColor(Color.BLACK);
            pieChart.setBackgroundOffset(0);
            pieChart.setProgressColor(ContextCompat.getColor(this, R.color.colorAccent));
            pieChart.setAdaptiveColorProvider(null);
        } else {
            ringChart.setOrientation(BaseModeRenderer.ORIENTATION_CLOCKWISE);
            ringChart.setProgress(10, false);
            ringChart.setStartAngle(90);
            ringChart.setAnimationDuration(400);
            ringChart.setAnimationInterpolator(new LinearInterpolator());
            ringChart.setTextColor(Color.WHITE);
            ringChart.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    50,
                    getResources().getDisplayMetrics()));
            ringChart.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            ringChart.setTextShadow(0, 0, 0, 0);
            ringChart.setDrawBackgroundEnabled(false);
            ringChart.setBackgroundColor(Color.BLACK);
            ringChart.setProgressColor(ContextCompat.getColor(this, R.color.colorAccent));
            ringChart.setAdaptiveColorProvider(null);
            ringChart.setProgressBarThickness(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    24,
                    getResources().getDisplayMetrics()));
        }

        setupAdaptiveText();
        setupAdaptiveBackground();
        setupAdaptiveBgBar();
    }

    //ACTIONS
    @OnClick(R.id.toggle_light)
    public void toggleLightAction() {
        if (bgAnimator.isRunning()) return;

        if (isDarkDisplayed) {
            bgAnimator.setIntValues(darkColor, lightColor);
            mLightToggle.setImageTintList(ColorStateList.valueOf(darkColor));
            isDarkDisplayed = false;
        } else {
            bgAnimator.setIntValues(lightColor, darkColor);
            mLightToggle.setImageTintList(ColorStateList.valueOf(lightColor));
            isDarkDisplayed = true;
        }
        bgAnimator.start();
    }

    @OnClick(R.id.pie_mode)
    public void pieModeAction() {
        if (isPieDisplayed) return;
        isPieDisplayed = true;
        resetLayout();
        centerHorizontally(R.id.ring_chart, R.id.ring_holder);
        centerHorizontally(R.id.pie_chart, R.id.spotlight);

        applyTransition();
    }

    @OnClick(R.id.ring_mode)
    public void ringModeAction() {
        if (!isPieDisplayed) return;
        isPieDisplayed = false;
        resetLayout();

        centerHorizontally(R.id.pie_chart, R.id.pie_holder);
        centerHorizontally(R.id.ring_chart, R.id.spotlight);

        applyTransition();
    }

    @OnClick(R.id.pie_chart)
    public void pieChartClickAction() {
        if (!isPieDisplayed) return;
        isFromUser = false;
        int rand = new Random().nextInt(100);
        mProgressValue.setProgress(rand);
        pieChart.setProgress(rand, mAnimateProgress.isChecked());
    }

    @OnClick(R.id.ring_chart)
    public void ringChartClickAction() {
        if (isPieDisplayed) return;
        isFromUser = false;
        int rand = new Random().nextInt(100);
        mProgressValue.setProgress(rand);
        ringChart.setProgress(rand, mAnimateProgress.isChecked());
    }

    @OnClick(R.id.increment_duration)
    public void incrementAction() {
        tweakAnimDuration(100);
    }

    @OnClick(R.id.decrement_duration)
    public void decrementAction() {
        tweakAnimDuration(-100);
    }

    @OnClick(R.id.text_color)
    public void textColorAction() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose text color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    mTextColor.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                    if (isPieDisplayed) {
                        pieChart.setTextColor(selectedColor);
                    } else {
                        ringChart.setTextColor(selectedColor);
                    }
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.increment_text_size)
    public void incrementTextSizeAction() {
        tweakTextSize(2);
    }

    @OnClick(R.id.decrement_text_size)
    public void decrementTextSizeAction() {
        tweakTextSize(-2);
    }

    @OnClick(R.id.shadow_color)
    public void shadowColorAction() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose shadow color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    shadowColor = selectedColor;
                    mShadowColor.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                    if (isPieDisplayed) {
                        pieChart.setTextShadow(shadowColor,
                                Float.parseFloat(mRadiusValue.getText().toString()),
                                Float.parseFloat(mDistXValue.getText().toString()),
                                Float.parseFloat(mDistYValue.getText().toString())
                        );
                    } else {
                        ringChart.setTextShadow(shadowColor,
                                Float.parseFloat(mRadiusValue.getText().toString()),
                                Float.parseFloat(mDistXValue.getText().toString()),
                                Float.parseFloat(mDistYValue.getText().toString())
                        );
                    }
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.increment_shadow)
    public void incrementShadowAction() {
        tweakShadowRadius(1);
    }

    @OnClick(R.id.decrement_shadow)
    public void decrementShadowAction() {
        tweakShadowRadius(-1);
    }

    @OnClick(R.id.increment_distx)
    public void incrementDistXAction() {
        tweakDistX(1);
    }

    @OnClick(R.id.decrement_distx)
    public void decrementDistXAction() {
        tweakDistX(-1);
    }

    @OnClick(R.id.increment_disty)
    public void incrementDistYAction() {
        tweakDistY(1);
    }

    @OnClick(R.id.decrement_disty)
    public void decrementDistYAction() {
        tweakDistY(-1);
    }

    @OnClick(R.id.background_color)
    public void backgroundAction() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose background color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    mBackgroundColor.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                    if (isPieDisplayed) {
                        pieChart.setBackgroundColor(selectedColor);
                    } else {
                        ringChart.setBackgroundColor(selectedColor);
                    }
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.progress_color)
    public void progressAction() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose text color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    mProgressColor.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                    if (isPieDisplayed) {
                        pieChart.setProgressColor(selectedColor);
                    } else {
                        ringChart.setProgressColor(selectedColor);
                    }
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.increment_offset)
    public void incrementOffsetAction() {
        tweakOffset(1);
    }

    @OnClick(R.id.decrement_offset)
    public void decrementOffsetAction() {
        tweakOffset(-1);
    }

    @OnClick(R.id.increment_prog_thickness)
    public void incrementPgThicknessAction() {
        tweakPgThickness(1);
    }

    @OnClick(R.id.decrement_prog_thickness)
    public void decrementPgThicknessAction() {
        tweakPgThickness(-1);
    }

    @OnClick(R.id.bg_bar_color)
    public void bgBarColorAction() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose Background bar color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    if (!isPieDisplayed) {
                        mBgBarColor.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                        ringChart.setBackgroundBarColor(selectedColor);
                    } else {
                        Toast.makeText(this, "Applicable only to ring chart!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.increment_bg_bar_thickness)
    public void incrementBgThicknessAction() {
        tweakBgThickness(1);
    }

    @OnClick(R.id.decrement_bg_bar_thickness)
    public void decrementBgThicknessAction() {
        tweakBgThickness(-1);
    }

    @OnClick(R.id.color_one)
    public void colorOneAction() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose first color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    colorOne = selectedColor;
                    mColorOne.setBackgroundTintList(ColorStateList.valueOf(colorOne));
                    if (isPieDisplayed) {
                        pieChart.setAdaptiveColorProvider(mUseProvider.isChecked() ? colorProvider : null);
                    } else {
                        ringChart.setAdaptiveColorProvider(mUseProvider.isChecked() ? colorProvider : null);
                    }
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.color_two)
    public void colorTwoAction() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose second color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    colorTwo = selectedColor;
                    mColorTwo.setBackgroundTintList(ColorStateList.valueOf(colorTwo));
                    if (isPieDisplayed) {
                        pieChart.setAdaptiveColorProvider(mUseProvider.isChecked() ? colorProvider : null);
                    } else {
                        ringChart.setAdaptiveColorProvider(mUseProvider.isChecked() ? colorProvider : null);
                    }
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.color_three)
    public void colorThreeAction() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose three color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    colorThree = selectedColor;
                    mColorThree.setBackgroundTintList(ColorStateList.valueOf(colorThree));
                    if (isPieDisplayed) {
                        pieChart.setAdaptiveColorProvider(mUseProvider.isChecked() ? colorProvider : null);
                    } else {
                        ringChart.setAdaptiveColorProvider(mUseProvider.isChecked() ? colorProvider : null);
                    }
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.color_four)
    public void colorFourAction() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose four color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    colorFour = selectedColor;
                    mColorFour.setBackgroundTintList(ColorStateList.valueOf(colorFour));
                    if (isPieDisplayed) {
                        pieChart.setAdaptiveColorProvider(mUseProvider.isChecked() ? colorProvider : null);
                    } else {
                        ringChart.setAdaptiveColorProvider(mUseProvider.isChecked() ? colorProvider : null);
                    }
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.github)
    public void githubAction() {
        Uri url = Uri.parse(getString(R.string.github_url));
        Intent intent = new Intent(Intent.ACTION_VIEW, url);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
