package com.ramijemli.sample.fragment;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.ramijemli.percentagechartview.renderer.RingModeRenderer;
import com.ramijemli.sample.R;
import com.ramijemli.sample.util.ScreenUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class ProgressSubFragment extends Fragment {

    public static final String BAR_STATE_ARG = "ProgressSubFragment.BAR_STATE_ARG";

    //PROGRESS
    @BindView(R.id.progress_value)
    SeekBar mProgressValue;
    @BindView(R.id.progress_value_label)
    TextView mProgressLabel;
    @BindView(R.id.animate)
    CheckBox mAnimateProgress;

    //PROGRESS COLOR
    @BindView(R.id.progress_color)
    Button mProgressColor;

    //PROGRESS BAR THICKNESS
    @BindView(R.id.prog_thickness_value)
    TextView mPgBarThicknessValue;

    //PROGRESS BAR STYLE
    @BindView(R.id.prog_bar_style_value)
    RadioGroup mPgBarStyle;

    @BindView(R.id.bar_visibility)
    Group mBarGroup;

    private OnProgressChangedListener mListener;
    private Unbinder unbinder;
    private boolean enableBar;

    public ProgressSubFragment() {
    }

    public static ProgressSubFragment newInstance() {
        return new ProgressSubFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnProgressChangedListener) {
            mListener = (OnProgressChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnProgressChangedListener");
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
            enableBar = getArguments().getBoolean(BAR_STATE_ARG, false);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sub_fragment_progress, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupProgress();
        if(enableBar){
            mBarGroup.setVisibility(View.VISIBLE);
            setupPgBarStyle();
        } else {
            mBarGroup.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        mProgressValue.setOnSeekBarChangeListener(null);
        mAnimateProgress.setOnCheckedChangeListener(null);
        mPgBarStyle.setOnCheckedChangeListener(null);
        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

    //##############################################################################################   BEHAVIOR
    private void setupProgress() {
        mProgressValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mProgressLabel.setText(String.valueOf(progress));

                if (mAnimateProgress.isChecked()) return;

                if (mListener != null) {
                    mListener.onProgressChanged(progress, false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mAnimateProgress.isChecked()) {
                    if (mListener != null) {
                        mListener.onProgressChanged(seekBar.getProgress(), true);
                    }
                }
                mProgressLabel.setText(String.valueOf(seekBar.getProgress()));
            }
        });
    }

    private void setupPgBarStyle() {
        mPgBarStyle.setOnCheckedChangeListener((group, checkedId) -> {
            if (mListener == null) return;
            switch (checkedId) {
                case R.id.round:
                    mListener.onProgBarStyleChanged(RingModeRenderer.CAP_ROUND);
                    break;
                case R.id.square:
                    mListener.onProgBarStyleChanged(RingModeRenderer.CAP_SQUARE);
                    break;
            }
        });
    }

    private void tweakPgThickness(int amount) {
        int value = getTextViewValue(mPgBarThicknessValue);

        int maxValue = (int) ScreenUtil.convertPixelsToDIP(getActivity(), getView().getMeasuredWidth() / 2);
        if (value + amount > maxValue) {
            value =maxValue;
        } else if (value + amount <= 0) {
            value = 0;
        } else {
            value += amount;
        }

        mPgBarThicknessValue.setText(value + " dp");
        if (mListener != null) {
            mListener.onProgBarThicknessChanged(ScreenUtil.convertDIPToPixels(getActivity(), value));
        }
    }

    private int getTextViewValue(TextView view) {
        String value = view.getText().toString();
        return Integer.parseInt(value.substring(0, value.length() - 3));
    }

    //##############################################################################################   ACTIONS
    @OnClick(R.id.progress_color)
    void progressAction() {
        ColorPickerDialogBuilder
                .with(getActivity())
                .setTitle("Choose progress color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    mProgressColor.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                    if (mListener != null) {
                        mListener.onProgressColorChanged(selectedColor);
                    }
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.increment_prog_thickness)
    void incrementPgThicknessAction() {
        tweakPgThickness(1);
    }

    @OnClick(R.id.decrement_prog_thickness)
    void decrementPgThicknessAction() {
        tweakPgThickness(-1);
    }

    //##############################################################################################   LISTENER
    public interface OnProgressChangedListener {
        void onProgressChanged(float progress, boolean animate);

        void onProgressColorChanged(int color);

        default void onProgBarThicknessChanged(int thickness){}

        default void onProgBarStyleChanged(int progressStyle){}
    }
}
