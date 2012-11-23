/*
 * Copyright (C) 2010 Neil Davies
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
 * 
 * This code is base on the Android Gallery widget and was Created 
 * by Neil Davies neild001 'at' gmail dot com to be a Coverflow widget
 * 
 * @author Neil Davies
 */
package com.android.thememanager.widget;


import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;
import android.widget.ImageView;

@SuppressWarnings("deprecation")
public class ScaledCoverFlow extends Gallery {

    /**
     * Graphics Camera used for transforming the matrix of ImageViews
     */
    private Camera mCamera = new Camera();

    /**
     * The minimum zoom the Child ImageView will scaled to
     */
    private int mMinZoom = -60;

    /**
     * The maximum zoom on the centre Child
     */
    private int mMaxZoom = -120;

    /**
     * The Centre of the Coverflow
     */
    private int mCoveflowCenter;

    public ScaledCoverFlow(Context context) {
        super(context);
        this.setStaticTransformationsEnabled(true);
    }

    public ScaledCoverFlow(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setStaticTransformationsEnabled(true);
    }

    public ScaledCoverFlow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setStaticTransformationsEnabled(true);
    }

    /**
     * Get the max rotational angle of the image
     *
     * @return the mMaxRotationAngle
     */
    public int getMinZoom() {
        return mMinZoom;
    }

    /**
     * Set the max rotational angle of each image
     *
     * @param minZoom the mMaxRotationAngle to set
     */
    public void setMinZoom(int minZoom) {
        mMinZoom = minZoom;
    }

    /**
     * Get the Max zoom of the centre image
     *
     * @return the mMaxZoom
     */
    public int getMaxZoom() {
        return mMaxZoom;
    }

    /**
     * Set the max zoom of the centre image
     *
     * @param maxZoom the mMaxZoom to set
     */
    public void setMaxZoom(int maxZoom) {
        mMaxZoom = maxZoom;
    }

    /**
     * Get the Centre of the Coverflow
     *
     * @return The centre of this Coverflow.
     */
    private int getCenterOfCoverflow() {
        return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
    }

    /**
     * Get the Centre of the View
     *
     * @return The centre of the given view.
     */
    private static int getCenterOfView(View view) {
        return view.getLeft() + view.getWidth() / 2;
    }

    /**
     * {@inheritDoc}
     *
     * @see #setStaticTransformationsEnabled(boolean)
     */
    protected boolean getChildStaticTransformation(View child, Transformation t) {

        final int childCenter = getCenterOfView(child);
        final int childWidth = child.getWidth();
        int zoom = 0;

        t.clear();
        t.setTransformationType(Transformation.TYPE_MATRIX);

        if (childCenter == mCoveflowCenter) {
            transformImageBitmap((ImageView) child, t, 0);
        } else {
            zoom = (int) Math.min((( Math.abs((float)(mCoveflowCenter - childCenter))) * mMinZoom), mMinZoom);
            transformImageBitmap((ImageView) child, t, zoom);
        }

        return true;
    }

    /**
     * This is called during layout when the size of this view has changed. If
     * you were just added to the view hierarchy, you're called with the old
     * values of 0.
     *
     * @param w    Current width of this view.
     * @param h    Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mCoveflowCenter = getCenterOfCoverflow();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Transform the Image Bitmap by the Angle passed
     *
     * @param child         ImageView the ImageView whose bitmap we want to rotate
     * @param t             transformation
     * @param zoomAmount the Angle by which to rotate the Bitmap
     */
    private void transformImageBitmap(ImageView child, Transformation t, int zoomAmount) {
        mCamera.save();
        final Matrix imageMatrix = t.getMatrix();
        ;
        final int imageHeight = child.getLayoutParams().height;
        ;
        final int imageWidth = child.getLayoutParams().width;

        mCamera.translate(0.0f, 0.0f, 100.0f);

        //As the angle of the view gets less, zoom in
        mCamera.translate(0.0f, 0.0f, zoomAmount);

        mCamera.getMatrix(imageMatrix);
        imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2));
        imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2));
        mCamera.restore();
    }
}