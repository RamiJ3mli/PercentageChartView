package com.ramijemli.sample.fragment;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.ramijemli.sample.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class TextSubFragment extends Fragment {

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

    //SHADOW DIST X
    @BindView(R.id.distx_value)
    TextView mDistXValue;

    //SHADOW DIST Y
    @BindView(R.id.disty_value)
    TextView mDistYValue;

    private OnTextChangedListener mListener;
    private Unbinder unbinder;
    private int shadowColor;

    public TextSubFragment() {
    }

    public static TextSubFragment newInstance() {
        return new TextSubFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTextChangedListener) {
            mListener = (OnTextChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTextChangedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sub_fragment_text, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupTextFont();
        setupTextStyle();
        setupTextShadow();
    }

    @Override
    public void onDestroyView() {
        mFontValue.setOnItemSelectedListener(null);
        mTextStyleValue.setOnItemSelectedListener(null);
        mUseShadow.setOnCheckedChangeListener(null);
        unbinder.unbind();
        unbinder = null;
        super.onDestroyView();
    }

    //##############################################################################################   BEHAVIOR
    private void setupTextFont() {
        List<String> data = new ArrayList<>();
        data.add("SYSTEM FONT (DEFAULT)");
        data.add("INTERSTELLAR");
        data.add("MARSHMELLOWS");
        data.add("NUAZ");
        data.add("WHALE I TRIED");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_interpolator, data);
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
                        Typeface.createFromAsset(getContext().getResources().getAssets(), font);
                if (mListener != null) {
                    mListener.onTextFontChanged(typeface);
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

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_interpolator, data);
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

                if (mListener != null) {
                    mListener.onTextStyleChanged(textStyle);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupTextShadow() {
        mUseShadow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mListener != null) {
                mListener.onDrawShadowChanged(isChecked);
            }
        });

        shadowColor = Color.WHITE;
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

        if (mListener != null) {
            mListener.onTextSizeChanged((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    value,
                    getActivity().getResources().getDisplayMetrics()
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
        if (mListener != null) {
            mListener.onShadowChanged(shadowColor,
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
        if (mListener != null) {
            mListener.onShadowChanged(shadowColor,
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
        if (mListener != null) {
            mListener.onShadowChanged(shadowColor,
                    Float.parseFloat(mRadiusValue.getText().toString()),
                    Float.parseFloat(mDistXValue.getText().toString()),
                    value);
        }
    }

    private int getTextViewValue(TextView view) {
        String value = view.getText().toString();
        return Integer.parseInt(value.substring(0, value.length() - 3));
    }

    //##############################################################################################   ACTIONS
    @OnClick(R.id.text_color)
    void textColorAction() {
        ColorPickerDialogBuilder
                .with(getActivity())
                .setTitle("Choose text color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    mTextColor.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                    if (mListener != null) {
                        mListener.onTextColorChanged(selectedColor);
                    }
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.increment_text_size)
    void incrementTextSizeAction() {
        tweakTextSize(2);
    }

    @OnClick(R.id.decrement_text_size)
    void decrementTextSizeAction() {
        tweakTextSize(-2);
    }

    @OnClick(R.id.shadow_color)
    void shadowColorAction() {
        ColorPickerDialogBuilder
                .with(getActivity())
                .setTitle("Choose shadow color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(6)
                .setPositiveButton("SET", (dialog, selectedColor, allColors) -> {
                    shadowColor = selectedColor;
                    mShadowColor.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
                    if (mListener != null) {
                        mListener.onShadowChanged(shadowColor,
                                Float.parseFloat(mRadiusValue.getText().toString()),
                                Float.parseFloat(mDistXValue.getText().toString()),
                                Float.parseFloat(mDistYValue.getText().toString()));
                    }
                })
                .setNegativeButton("DISMISS", (dialog, which) -> {
                })
                .build()
                .show();
    }

    @OnClick(R.id.increment_shadow)
    void incrementShadowAction() {
        tweakShadowRadius(1);
    }

    @OnClick(R.id.decrement_shadow)
    void decrementShadowAction() {
        tweakShadowRadius(-1);
    }

    @OnClick(R.id.increment_distx)
    void incrementDistXAction() {
        tweakDistX(1);
    }

    @OnClick(R.id.decrement_distx)
    void decrementDistXAction() {
        tweakDistX(-1);
    }

    @OnClick(R.id.increment_disty)
    void incrementDistYAction() {
        tweakDistY(1);
    }

    @OnClick(R.id.decrement_disty)
    void decrementDistYAction() {
        tweakDistY(-1);
    }

    //##############################################################################################   LISTENER
    public interface OnTextChangedListener {
        void onTextColorChanged(int color);

        void onTextSizeChanged(int textSize);

        void onTextFontChanged(Typeface typeface);

        void onTextStyleChanged(int textStyle);

        void onDrawShadowChanged(boolean draw);

        void onShadowChanged(int color, float blur, float distX, float distY);
    }
}
