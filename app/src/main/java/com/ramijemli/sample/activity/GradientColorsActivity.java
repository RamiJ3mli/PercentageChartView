package com.ramijemli.sample.activity;


import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.ramijemli.percentagechartview.PercentageChartView;
import com.ramijemli.sample.R;

import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.GRADIENT_LINEAR;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.GRADIENT_RADIAL;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.GRADIENT_SWEEP;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.MODE_FILL;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.MODE_PIE;
import static com.ramijemli.percentagechartview.renderer.BaseModeRenderer.MODE_RING;


public class GradientColorsActivity extends AppCompatActivity {


    @BindView(R.id.constraint_layout)
    ConstraintLayout mConstraintLayout;

    @BindView(R.id.pie_chart)
    PercentageChartView mPieChart;
    @BindView(R.id.ring_chart)
    PercentageChartView mRingChart;
    @BindView(R.id.fill_chart)
    PercentageChartView mFillChart;

    //GRADIENT TYPE
    @BindView(R.id.gradient_value)
    RadioGroup mGradientValue;

    //GRADIENT ANGLE
    @BindView(R.id.gradient_angle_value)
    SeekBar mGradientAngleValue;
    @BindView(R.id.gradient_angle_value_label)
    TextView mGradientAngleLabel;

    //GRADIENT COLORS
    @BindView(R.id.color_one)
    Button mColorOne;
    @BindView(R.id.color_two)
    Button mColorTwo;
    @BindView(R.id.color_three)
    Button mColorThree;
    @BindView(R.id.color_four)
    Button mColorFour;


    private Unbinder unbinder;
    private ConstraintSet mConstraintSet;
    private Transition fade;
    private int colorOne;
    private int colorTwo;
    private int colorThree;
    private int colorFour;
    private int displayedMode;
    private int gradientType;
    private int gradientAngle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gradient_colors);
        unbinder = ButterKnife.bind(this);
        setupLayoutAnimation();
        setupGradientColors();
    }

    @Override
    protected void onDestroy() {
        mGradientValue.setOnCheckedChangeListener(null);
        mGradientAngleValue.setOnSeekBarChangeListener(null);
        unbinder.unbind();
        unbinder = null;
        super.onDestroy();
    }

    private void setupLayoutAnimation() {
        Explode transition = new Explode();
        transition.setDuration(600);
        transition.setInterpolator(new OvershootInterpolator(1f));
        getWindow().setEnterTransition(transition);

        displayedMode = MODE_PIE;

        mConstraintSet = new ConstraintSet();
        mConstraintSet.clone(mConstraintLayout);

        fade = new Fade();
        fade.setDuration(400);
    }

    private void setupGradientColors() {
        colorOne = Color.parseColor("#F44336");
        colorTwo = Color.parseColor("#FFEA00");
        colorThree = Color.parseColor("#03A9F4");
        colorFour = Color.parseColor("#00E676");

        gradientType = GRADIENT_LINEAR;
        gradientAngle = 90;

        setGradientColors(gradientType, new int[]{colorOne, colorTwo, colorThree, colorFour}, null, gradientAngle);

        //GRADIENT TYPE
        mGradientValue.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                default:
                case R.id.linear:
                    gradientType = GRADIENT_LINEAR;
                    break;

                case R.id.radial:
                    gradientType = GRADIENT_RADIAL;
                    break;

                case R.id.sweep:
                    gradientType = GRADIENT_SWEEP;
                    break;
            }

            setGradientColors(gradientType, new int[]{colorOne, colorTwo, colorThree, colorFour}, null, 90);
        });

        //GRADIENT ANGLE
        mGradientAngleValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int angle, boolean fromUser) {
                gradientAngle = angle;
                setGradientColors(gradientType, new int[]{colorOne, colorTwo, colorThree, colorFour}, null, gradientAngle);
                mGradientAngleLabel.setText(String.valueOf(angle));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setGradientColors(int type, int[] colors, float[] positions, float angle) {
        mPieChart.setGradientColors(type, colors, positions, angle);
        mRingChart.setGradientColors(type, colors, positions, angle);
        mFillChart.setGradientColors(type, colors, positions, angle);
    }

    private void changeMode(int pendingMode) {
        switch (pendingMode) {
            case MODE_PIE:
                mConstraintSet.setVisibility(R.id.pie_chart, ConstraintSet.VISIBLE);
                mConstraintSet.setVisibility(R.id.fill_chart, ConstraintSet.GONE);
                mConstraintSet.setVisibility(R.id.ring_chart, ConstraintSet.GONE);
                break;
            case MODE_FILL:
                mConstraintSet.setVisibility(R.id.pie_chart, ConstraintSet.GONE);
                mConstraintSet.setVisibility(R.id.fill_chart, ConstraintSet.VISIBLE);
                mConstraintSet.setVisibility(R.id.ring_chart, ConstraintSet.GONE);
                break;
            case MODE_RING:
                mConstraintSet.setVisibility(R.id.pie_chart, ConstraintSet.GONE);
                mConstraintSet.setVisibility(R.id.fill_chart, ConstraintSet.GONE);
                mConstraintSet.setVisibility(R.id.ring_chart, ConstraintSet.VISIBLE);
                break;
        }

        displayedMode = pendingMode;
        applyTransition();
    }

    private void applyTransition() {
        TransitionManager.beginDelayedTransition(mConstraintLayout, fade);
        mConstraintSet.applyTo(mConstraintLayout);
    }

    //##############################################################################################   ACTIONS
    @OnClick(R.id.pie_chart)
    public void pieChartClickAction() {
        mPieChart.setProgress(new Random().nextInt(100), true);
    }

    @OnClick(R.id.ring_chart)
    public void ringChartClickAction() {
        mRingChart.setProgress(new Random().nextInt(100), true);
    }

    @OnClick(R.id.fill_chart)
    public void fillChartClickAction() {
        mFillChart.setProgress(new Random().nextInt(100), true);
    }

    @OnClick(R.id.pie_mode)
    public void pieModeAction() {
        if (displayedMode == MODE_PIE) return;
        changeMode(MODE_PIE);
    }

    @OnClick(R.id.ring_mode)
    public void ringModeAction() {
        if (displayedMode == MODE_RING) return;
        changeMode(MODE_RING);
    }

    @OnClick(R.id.fill_mode)
    public void fillModeAction() {
        if (displayedMode == MODE_FILL) return;
        changeMode(MODE_FILL);
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
                    setGradientColors(gradientType, new int[]{colorOne, colorTwo, colorThree, colorFour}, null, gradientAngle);
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
                    setGradientColors(gradientType, new int[]{colorOne, colorTwo, colorThree, colorFour}, null, gradientAngle);
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
                    setGradientColors(gradientType, new int[]{colorOne, colorTwo, colorThree, colorFour}, null, gradientAngle);
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
                    setGradientColors(gradientType, new int[]{colorOne, colorTwo, colorThree, colorFour}, null, gradientAngle);
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

}
