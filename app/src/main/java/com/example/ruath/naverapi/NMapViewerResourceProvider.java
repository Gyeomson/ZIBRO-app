package com.example.ruath.naverapi;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.ListView;

import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapResourceProvider;

/**
 * Created by ruath on 2017-08-04.
 */

public class NMapViewerResourceProvider extends NMapResourceProvider {
    public NMapViewerResourceProvider(Context context) {
        super(context);
    }

    @Override
    protected int findResourceIdForMarker(int i, boolean b) {
        return 0;
    }

    @Override
    protected Drawable getDrawableForMarker(int i, boolean b, NMapOverlayItem nMapOverlayItem) {
        return null;
    }

    @Override
    public Drawable getCalloutBackground(NMapOverlayItem nMapOverlayItem) {
        return null;
    }

    @Override
    public String getCalloutRightButtonText(NMapOverlayItem nMapOverlayItem) {
        return null;
    }

    @Override
    public Drawable[] getCalloutRightButton(NMapOverlayItem nMapOverlayItem) {
        return new Drawable[0];
    }

    @Override
    public Drawable[] getCalloutRightAccessory(NMapOverlayItem nMapOverlayItem) {
        return new Drawable[0];
    }

    @Override
    public int[] getCalloutTextColors(NMapOverlayItem nMapOverlayItem) {
        return new int[0];
    }

    @Override
    public Drawable[] getLocationDot() {
        Drawable[] drawable = new Drawable[2];

        drawable[0] = mContext.getResources().getDrawable(R.drawable.current_off_edit);
        drawable[1] = mContext.getResources().getDrawable(R.drawable.current_on_edit);

        for (int i = 0; i < drawable.length; i++) {
            int w = drawable[i].getIntrinsicWidth() / 2;
            int h = drawable[i].getIntrinsicHeight() / 2;

            drawable[i].setBounds(-w, -h, w, h);
        }

        return drawable;
    }

    @Override
    public Drawable getDirectionArrow() {
        return null;
    }

    @Override
    public int getParentLayoutIdForOverlappedListView() {
        return 0;
    }

    @Override
    public int getOverlappedListViewId() {
        return 0;
    }

    @Override
    public int getLayoutIdForOverlappedListView() {
        return 0;
    }

    @Override
    public void setOverlappedListViewLayout(ListView listView, int i, int i1, int i2) {

    }

    @Override
    public int getListItemLayoutIdForOverlappedListView() {
        return 0;
    }

    @Override
    public int getListItemTextViewId() {
        return 0;
    }

    @Override
    public int getListItemTailTextViewId() {
        return 0;
    }

    @Override
    public int getListItemImageViewId() {
        return 0;
    }

    @Override
    public int getListItemDividerId() {
        return 0;
    }

    @Override
    public void setOverlappedItemResource(NMapPOIitem nMapPOIitem, ImageView imageView) {

    }
}
