package com.anubhav.commonutility.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.anubhav.codereverse.R;

public class ClickableRelativeLayout extends RelativeLayout {

    // If set to true, the children are clickable.
    // If set to false, the children are not clickable.
    private boolean mChildrenTouchEnabled;

    public ClickableRelativeLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ClickableRelativeLayout);
        mChildrenTouchEnabled = a.getBoolean(R.styleable.ClickableRelativeLayout_hasChildrenTouchEnabled, true);
        a.recycle();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !mChildrenTouchEnabled;
    }

    public void setChildrenTouchEnabled(boolean flag) {
        this.mChildrenTouchEnabled = flag;
    }

}