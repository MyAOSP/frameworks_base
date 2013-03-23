/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.phone;

import java.util.Arrays;

import static com.android.internal.util.cm.QSUtils.getMaxColumns;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.systemui.R;

/**
 *
 */
public class QuickSettingsContainerView extends FrameLayout {

    // The number of columns in the QuickSettings grid
    private int mNumColumns;

    // The gap between tiles in the QuickSettings grid
    private float mCellGap;
    private Context mContext;

    private static final int EMPTY = 0;
    private static final int USED = 1;

    private final static String TAG = "QuickSettingsTiles";

    private static final boolean DEBUG = false;

    public QuickSettingsContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        updateResources();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // TODO: Setup the layout transitions
        LayoutTransition transitions = getLayoutTransition();
    }

    void updateResources() {
        Resources r = getContext().getResources();
        mCellGap = r.getDimension(R.dimen.quick_settings_cell_gap);
        if (r.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mNumColumns = getMaxColumns(mContext, Configuration.ORIENTATION_PORTRAIT);
        } else {
            mNumColumns = getMaxColumns(mContext, Configuration.ORIENTATION_LANDSCAPE);
        }
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Calculate the cell width dynamically
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int availableWidth = (int) (width - getPaddingLeft() - getPaddingRight() -
                (mNumColumns - 1) * mCellGap);
        float cellWidth = (float) Math.ceil(((float) availableWidth) / mNumColumns);
        // we want them to be square cells so set the height equal to the width
        float cellHeight = cellWidth;

        // Update each of the children's widths accordingly to the cell width
        int N = getChildCount();

        int cursor = 0;
        for (int i = 0; i < N; ++i) {
            // Update the child's width
            QuickSettingsTileView v = (QuickSettingsTileView) getChildAt(i);

            if (v.getVisibility() != View.GONE) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                int colSpan = v.getColumnSpan();
                int rowSpan = v.getRowSpan();

                lp.width = (int) ((colSpan * cellWidth) + (colSpan - 1) * mCellGap);
                lp.height = (int) ((rowSpan * cellHeight) + (rowSpan - 1) * mCellGap);

                // Measure the child
                int newWidthSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
                int newHeightSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
                v.measure(newWidthSpec, newHeightSpec);

                // Save the cell height
                if (cellHeight <= 0) {
                    cellHeight = v.getMeasuredHeight();
                }
                cursor += (colSpan * rowSpan);
            }
        }

        // Set the measured dimensions.  We always fill the tray width, but wrap to the height of
        // all the tiles.
        int numRows = (int) Math.ceil((float) cursor / mNumColumns);
        int newHeight = (int) ((numRows * cellHeight) + ((numRows - 1) * mCellGap)) +
                getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, newHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int N = getChildCount();
        int x = getPaddingLeft();
        int y = getPaddingTop();
        // hard code this for now
        int maxRows = 20;
        // keeps track of used spaces in the grid
        int[][] layoutMap = new int[maxRows][mNumColumns];

        for (int i = 0; i < N; ++i) {
            QuickSettingsTileView v = (QuickSettingsTileView) getChildAt(i);
            ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) v.getLayoutParams();
            if (v.getVisibility() != GONE) {
                int colSpan = v.getColumnSpan();
                int rowSpan = v.getRowSpan();

                int[][] tile = new int[rowSpan][colSpan];

                int anchorColumn = 0;
                int anchorRow = 0;

                main:
                for (int row = 0; row < maxRows; row++) {
                    for (int column = 0; column < mNumColumns; column++) {
                        // we found a free spot, now see if this one can fit
                        if(layoutMap[row][column]==EMPTY){
                            // can it fit across? lets check if it is all free
                            if (colSpan <= (mNumColumns - column)) {
                                if (checkFree(layoutMap, tile, row, column)) {
                                    anchorRow = row;
                                    anchorColumn = column;

                                    markUsed(layoutMap, tile, anchorRow, anchorColumn);
                                    break main;
                                }
                            }
                        }
                    }
                }

                if(DEBUG)dumpLayoutMap(layoutMap);

                // calculate x and y based on our anchor point
                int calculatedX = getPaddingLeft() + (anchorColumn) * ((lp.width + (int)mCellGap) / colSpan);
                int calcuatedY = getPaddingTop() + (anchorRow) * ((lp.height + (int)mCellGap) / rowSpan);
                x = calculatedX;
                y = calcuatedY;

                v.layout(x, y, x + lp.width, y + lp.height);
            }
        }
    }

    private void dumpLayoutMap(int[][] layoutMap) {
        Log.d(TAG, Arrays.deepToString(layoutMap));
    }

    /**
     * mark the proper spots used so another tile
     * does not try and occupy the same space
     */
    private void markUsed(int[][] layoutMap, int[][] tile, int anchorRow, int anchorColumn) {
        for (int row = 0; row < tile.length; row++) {
            for (int column = 0; column < tile[row].length; column++) {
                layoutMap[anchorRow+row][anchorColumn+column] = USED;
            }
        }
    }

    /**
     * check to see if the tile can occupy this space
     * @return return true or false
     */
    private boolean checkFree(int[][] layoutMap, int[][] tile, int startRow, int startColumn) {
        for (int row = 0; row < tile.length; row++) {
            for (int column = 0; column < tile[row].length; column++) {
                if(layoutMap[startRow+row][startColumn+column] == USED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setColumnCount(int num) {
        mNumColumns = num;
    }
}
