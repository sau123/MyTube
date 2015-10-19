package com.mytube.helper;

/**
 * Created by shivang on 10/18/15.
 */

import android.content.Context;
import android.widget.ImageButton;

import com.mytube.R;

public class StarTracker extends ImageButton{

    private int mImageResource = 0;

    public StarTracker(Context context) {
        super(context);
        mImageResource = R.mipmap.ic_star_unfilled;
    }

    @Override
    public void setImageResource (int resId) {
        mImageResource = resId;
        super.setImageResource(resId);
    }

    public int getImageResource() {
        return mImageResource;
    }

}