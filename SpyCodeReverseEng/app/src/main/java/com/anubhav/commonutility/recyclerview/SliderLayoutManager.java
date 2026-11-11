package com.anubhav.commonutility.recyclerview;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Uses: on below line we are setting custom layout manager to our recycler view.
 * <p>
 * val layoutManager = SliderLayoutManager(context)
 * layoutManager.setRecycleListSize(itemsList.size)
 * layoutManager.setRecyclerItemSize(34, 34)
 * layoutManager.setOnItemSelectedListener { view, position ->
 *      this.selectedItem = itemsList[position]
 * }
 */
public class SliderLayoutManager extends LinearLayoutManager {

    private boolean changeAlpha = true;
    private float scaleDownBy = 0.66f;
    private float scaleDownDistance = 1f;

    private int rvItemListSize = 0;
    private int rvItemWidthDp = 36;
    private int rvItemHeightDp = 36;

    private Context context;
    private RecyclerView recyclerView;
    private OnItemSelectedListener onItemSelectedListener;
    private static final String TAG = "SliderLayoutManager";

    public SliderLayoutManager(Context context) {
        super(context, VERTICAL, false);
        this.context = context;
    }

    public SliderLayoutManager(Context context, int orientation) {
        super(context, orientation, false);
        this.context = context;
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        view.setClipToPadding(false);
        super.onAttachedToWindow(view);
        this.recyclerView = view;

        // Smart snapping
        new LinearSnapHelper().attachToRecyclerView(recyclerView);
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        setRecyclerPadding();
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        scaleDownView();
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getOrientation() == HORIZONTAL) {
            int scrolled = super.scrollHorizontallyBy(dx, recycler, state);
            scaleDownView();
            return scrolled;
        } else return 0;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getOrientation() == VERTICAL) {
            int scrolled = super.scrollVerticallyBy(dy, recycler, state);
            scaleDownView();
            return scrolled;
        } else return 0;
    }

    private void scaleDownView() {
        if (getOrientation() == HORIZONTAL) {
            float mid = getWidth() / 2.0f;
            float unitScaleDownDist = scaleDownDistance * mid;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child == null) {
                    continue;
                }
                float childMid = (getDecoratedLeft(child) + getDecoratedRight(child)) / 2.0f;
                float distanceFromCenter = Math.abs(mid - childMid);

                //float scale = 1 - (float) Math.sqrt((double) distanceFromCenter / getWidth()) * scaleDownBy;
                float scale = 1.0f + (-1 * scaleDownBy) * (Math.min(unitScaleDownDist, distanceFromCenter)) / unitScaleDownDist;

                // Set scale to view
                child.setScaleX(scale);
                child.setScaleY(scale);
                if (changeAlpha) {
                    child.setAlpha(scale);
                }
            }

        } else if (getOrientation() == VERTICAL) {
            float mid = getHeight() / 2.0f;
            float unitScaleDownDist = scaleDownDistance * mid;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child == null) {
                    continue;
                }
                float childMid = (getDecoratedTop(child) + getDecoratedBottom(child)) / 2.0f;
                float distanceFromCenter = Math.abs(mid - childMid);

                //float scale = 1 - (float) Math.sqrt((double) distanceFromCenter / getHeight()) * scaleDownBy;
                float scale = 1.0f + (-1 * scaleDownBy) * (Math.min(unitScaleDownDist, distanceFromCenter)) / unitScaleDownDist;

                // Set scale to view
                child.setScaleX(scale);
                child.setScaleY(scale);
                if (changeAlpha) {
                    child.setAlpha(scale);
                }
            }

        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        // When scroll stops we notify on the selected item
        if (state == RecyclerView.SCROLL_STATE_IDLE) {

            // Find the closest child to the recyclerView center --> this is the selected item.
            int recyclerViewCenter = getRecyclerViewCenter();
            int minDistance = (getOrientation() == HORIZONTAL) ? recyclerView.getWidth() : recyclerView.getHeight();
            int position = -1;

            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                View child = recyclerView.getChildAt(i);
                if (child == null) {
                    continue;
                }
                int childDistanceFromCenter = Math.abs(getChildViewCenter(child) - recyclerViewCenter);
                if (childDistanceFromCenter < minDistance) {
                    minDistance = childDistanceFromCenter;
                    position = recyclerView.getChildLayoutPosition(child);
                }
            }

            // Notify on the selected item
            if (position >= 0 && onItemSelectedListener != null) {
                // this is temporary solution for vertical recycler view
                Log.e(TAG, "Size -> " + position + " : " + rvItemListSize);
                if (getOrientation() == VERTICAL && (position <= rvItemListSize)) {
                    position--;
                }
                onItemSelectedListener.onItemSelected(getChildAt(position), position);
            }
        }
    }

    private int getRecyclerViewCenter() {
        if (getOrientation() == HORIZONTAL) {
            return (recyclerView.getRight() - recyclerView.getLeft()) / 2 + recyclerView.getLeft();
        } else {
            return (recyclerView.getBottom() - recyclerView.getTop()) / 2 + recyclerView.getTop();
        }
    }

    private int getChildViewCenter(View child) {
        if (getOrientation() == HORIZONTAL) {
            return getDecoratedLeft(child) + (getDecoratedRight(child) - getDecoratedLeft(child)) / 2;
        } else {
            return getDecoratedTop(child) + (getDecoratedBottom(child) - getDecoratedTop(child)) / 2;
        }
    }

    private void setRecyclerPadding() {
        if (getOrientation() == HORIZONTAL) {
            int padding = recyclerView.getWidth() / 2 - dpToPx(rvItemWidthDp);
            recyclerView.setPadding(padding, 0, padding, 0);
        } else {
            int padding = recyclerView.getHeight() / 2 - dpToPx(rvItemHeightDp);
            recyclerView.setPadding(0, padding, 0, padding);
        }
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    /**
     * public methods to object access
     */
    public void setScaleDownDistance(float scaleDownDistance) {
        this.scaleDownDistance = (scaleDownDistance >= 0.1f && scaleDownDistance <= 0.9f) ? scaleDownDistance : 1f;
    }

    public void setChangeAlpha(boolean changeAlpha) {
        this.changeAlpha = changeAlpha;
    }

    public void setRecyclerItemSize(int rvItemWidthDp, int rvItemHeightDp) {
        this.rvItemWidthDp = rvItemWidthDp;
        this.rvItemHeightDp = rvItemHeightDp;
    }

    public void setRecycleListSize(int rvItemListSize) {
        this.rvItemListSize = rvItemListSize;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onScrollStopListener) {
        this.onItemSelectedListener = onScrollStopListener;
    }

    public interface OnItemSelectedListener {
        void onItemSelected(View view, int position);
    }

}