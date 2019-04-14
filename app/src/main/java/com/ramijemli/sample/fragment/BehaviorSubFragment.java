package com.ramijemli.sample.fragment;


import android.animation.TimeInterpolator;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ramijemli.percentagechartview.annotation.ProgressOrientation;
import com.ramijemli.percentagechartview.renderer.BaseModeRenderer;
import com.ramijemli.sample.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
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


public class BehaviorSubFragment extends Fragment {

    public static final String ORIENTATION_STATE_ARG = "BehaviorSubFragment.ORIENTATION_STATE_ARG";

    //ORIENTATION
    @BindView(R.id.orientation_value)
    RadioGroup mOrientationValue;
    @BindView(R.id.orientation_visibility)
    Group mOrientationGroup;

    //START ANGLE
    @BindView(R.id.start_angle_value)
    SeekBar mStartAngleValue;
    @BindView(R.id.start_angle_value_label)
    TextView mStartAngleLabel;

    //DURATION
    @BindView(R.id.duration_value)
    TextView mDurationValue;

    //INTERPOLATOR
    @BindView(R.id.interpolator_value)
    Spinner mInterpolatorValue;

    private OnBehaviorChangedListener mListener;
    private Unbinder unbinder;
    private boolean enableOrientation;

    public BehaviorSubFragment() {
    }

    public static BehaviorSubFragment newInstance() {
        return new BehaviorSubFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBehaviorChangedListener) {
            mListener = (OnBehaviorChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBehaviorChangedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            enableOrientation = getArguments().getBoolean(ORIENTATION_STATE_ARG, false);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sub_fragment_behavior, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(enableOrientation){
            mOrientationGroup.setVisibility(View.VISIBLE);
            setupOrientation();
        } else {
            mOrientationGroup.setVisibility(View.GONE);
        }
        setupStartAngle();
        setupInterpolator();
    }

    @Override
    public void onDestroyView() {
        mOrientationValue.setOnCheckedChangeListener(null);
        mInterpolatorValue.setOnItemSelectedListener(null);
        mStartAngleValue.setOnSeekBarChangeListener(null);
        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

    //##############################################################################################   BEHAVIOR
    private void setupOrientation() {
        mOrientationValue.setOnCheckedChangeListener((group, checkedId) -> {
            if (mListener != null) {
                mListener.onOrientationChanged(checkedId == R.id.clockwise ? BaseModeRenderer.ORIENTATION_CLOCKWISE : BaseModeRenderer.ORIENTATION_COUNTERCLOCKWISE);
            }
        });
    }

    private void setupStartAngle() {
        mStartAngleValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int startAngle, boolean fromUser) {
                if (mListener != null) {
                    mListener.onStartAngleChanged(startAngle);
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

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_interpolator, data);
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
                if (mListener != null) {
                    mListener.onInterpolatorChanged(interpolator);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void tweakAnimDuration(int amount) {
        int value = getTextViewValue(mDurationValue);

        if (value + amount > 4000) {
            value += amount;
            Toast.makeText(getActivity(), "Welcome to the boring animation hell!", Toast.LENGTH_SHORT).show();
        } else if (value + amount <= 0) {
            value = 100;
        } else {
            value += amount;
        }

        mDurationValue.setText(value + " ms");
        if (mListener != null) {
            mListener.onAnimDurationChanged(value);
        }
    }

    private int getTextViewValue(TextView view) {
        String value = view.getText().toString();
        return Integer.parseInt(value.substring(0, value.length() - 3));
    }

    //##############################################################################################   ACTIONS
    @OnClick(R.id.increment_duration)
    void incrementAction() {
        tweakAnimDuration(100);
    }

    @OnClick(R.id.decrement_duration)
    void decrementAction() {
        tweakAnimDuration(-100);
    }

    //##############################################################################################   LISTENER
    public interface OnBehaviorChangedListener {
        void onOrientationChanged(@ProgressOrientation int orientation);

        void onStartAngleChanged(int angle);

        void onAnimDurationChanged(int duration);

        void onInterpolatorChanged(TimeInterpolator interpolator);
    }
}
