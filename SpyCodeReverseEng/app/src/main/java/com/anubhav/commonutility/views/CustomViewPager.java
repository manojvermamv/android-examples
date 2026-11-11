package com.anubhav.commonutility.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class CustomViewPager extends ViewPager {

    private boolean swipeable = false;

    public CustomViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (swipeable) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (swipeable) {
            return super.onInterceptTouchEvent(ev);
        }
        return false;
    }

    public void setSwipeable(boolean swipeable) {
        this.swipeable = swipeable;
    }

}