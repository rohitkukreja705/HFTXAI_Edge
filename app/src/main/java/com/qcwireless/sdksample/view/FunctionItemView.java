package com.qcwireless.sdksample.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.qcwireless.sdksample.R;

public class FunctionItemView extends ConstraintLayout {

    private TextView tvTitle;
    private ImageView ivArrow;
    private View dividerLine;
    private OnClickListener clickListener;

    public FunctionItemView(Context context) {
        this(context, null);
    }

    public FunctionItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FunctionItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_function_item, this, true);
        
        tvTitle = findViewById(R.id.tv_title);
        dividerLine = findViewById(R.id.divider_line);
        ivArrow = findViewById(R.id.iv_arrow);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FunctionItemView);
            
            String titleText = a.getString(R.styleable.FunctionItemView_fiv_titleText);
            if (titleText != null) {
                tvTitle.setText(titleText);
            }
            
            int titleColor = a.getColor(R.styleable.FunctionItemView_fiv_titleColor, Color.BLACK);
            tvTitle.setTextColor(titleColor);
            
            int backgroundColor = a.getColor(R.styleable.FunctionItemView_fiv_backgroundColor, Color.WHITE);
            setBackgroundColor(backgroundColor);
            
            boolean showDivider = a.getBoolean(R.styleable.FunctionItemView_fiv_showDivider, true);
            dividerLine.setVisibility(showDivider ? VISIBLE : GONE);

            boolean showArrow = a.getBoolean(R.styleable.FunctionItemView_fiv_showArrow, false);
            ivArrow.setVisibility(showArrow ? VISIBLE : GONE);
            
            int dividerColor = a.getColor(R.styleable.FunctionItemView_fiv_dividerColor, Color.parseColor("#E0E0E0"));
            dividerLine.setBackgroundColor(dividerColor);
            
            a.recycle();
        }
        
        setClickable(true);
        setBackgroundResource(R.drawable.function_item_background);
        
        super.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClick(v);
            }
        });
    }


    public void setTitleText(String text) {
        tvTitle.setText(text);
    }


    public void setTitleText(int resId) {
        tvTitle.setText(resId);
    }


    public String getTitleText() {
        return tvTitle.getText().toString();
    }


    public void setTitleColor(int color) {
        tvTitle.setTextColor(color);
    }


    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
    }


    public void setShowDivider(boolean show) {
        dividerLine.setVisibility(show ? VISIBLE : GONE);
    }

    public void setShowArrow(boolean show) {
        ivArrow.setVisibility(show ? VISIBLE : GONE);
    }


    public void setDividerColor(int color) {
        dividerLine.setBackgroundColor(color);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        this.clickListener = l;
        super.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClick(v);
            }
        });
    }

    public OnClickListener getClickListener() {
        return clickListener;
    }
}