package com.anubhav.commonutility.ui;

import com.google.android.material.appbar.AppBarLayout;

public abstract class AppBarStateChangeListener implements AppBarLayout.OnOffsetChangedListener {

    public enum State {
        EXPANDED,
        COLLAPSED,
        IDLE,
        HALF_EXPANDED,
        HALF_COLLAPSED
    }

    private State mCurrentState = State.IDLE;

    @Override
    public final void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        int scrollRange = appBarLayout.getTotalScrollRange();
        int rangeOne = calPercentage(scrollRange, 33f);
        int rangeTwo = calPercentage(scrollRange, 66f);

        if (i == 0) {
            setCurrentState(appBarLayout, State.EXPANDED);

        } else if (Math.abs(i) == scrollRange) {
            setCurrentState(appBarLayout, State.COLLAPSED);

        } else if (Math.abs(i) <= rangeTwo && Math.abs(i) >= rangeOne) {
            setCurrentState(appBarLayout, State.IDLE);

        } else if (Math.abs(i) < rangeTwo) {
            setCurrentState(appBarLayout, State.HALF_EXPANDED);

        } else if (Math.abs(i) > rangeOne) {
            setCurrentState(appBarLayout, State.HALF_COLLAPSED);

        }
    }

    private void setCurrentState(AppBarLayout appBarLayout, State state) {
        if (mCurrentState != state) {
            onStateChanged(appBarLayout, state);
        }
        mCurrentState = state;
    }

    private int calPercentage(int value, float percentage) {
        return (int) (value * (percentage / 100.0f));
    }

    public abstract void onStateChanged(AppBarLayout appBarLayout, State state);
}