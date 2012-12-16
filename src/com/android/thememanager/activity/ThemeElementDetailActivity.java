/*
 * Copyright (C) 2012 The ChameleonOS Project
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

package com.android.thememanager.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.IThemeManagerService;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.thememanager.*;

import java.io.*;

public class ThemeElementDetailActivity extends Activity {
    private static final String TAG = "ThemeManager";
    private static final String THEMES_PATH = Globals.DEFAULT_THEME_PATH;

    private static final int DIALOG_PROGRESS = 0;

    private Gallery mPreviews = null;
    private String[] mPreviewList = null;
    private ImageAdapter mAdapter = null;
    private ProgressDialog mProgressDialog;
    private String mThemeName = "";
    private ThemeUtils.ThemeDetails mDetails;
    private int mElementType = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_detail);


        mThemeName = getIntent().getStringExtra("theme_name");
        mElementType = getIntent().getIntExtra("type", 0);
        switch (mElementType) {
            case Theme.THEME_ELEMENT_TYPE_ICONS:
                mPreviewList = PreviewHelper.getIconPreviews(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(mThemeName));
                break;
            case Theme.THEME_ELEMENT_TYPE_WALLPAPER:
                mPreviewList = PreviewHelper.getLauncherPreviews(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(mThemeName));
                break;
            case Theme.THEME_ELEMENT_TYPE_SYSTEMUI:
                mPreviewList = PreviewHelper.getStatusbarPreviews(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(mThemeName));
                break;
            case Theme.THEME_ELEMENT_TYPE_FRAMEWORK:
                mPreviewList = PreviewHelper.getLauncherPreviews(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(mThemeName));
                break;
            case Theme.THEME_ELEMENT_TYPE_LOCKSCREEN:
                mPreviewList = PreviewHelper.getLockscreenPreviews(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(mThemeName));
                break;
            case Theme.THEME_ELEMENT_TYPE_RINGTONES:
                mPreviewList = PreviewHelper.getContactsPreviews(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(mThemeName));
                break;
            case Theme.THEME_ELEMENT_TYPE_BOOTANIMATION:
                mPreviewList = PreviewHelper.getBootanimationPreviews(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(mThemeName));
                break;
        }

        mDetails = ThemeUtils.getThemeDetails(Environment.getExternalStorageDirectory()
                + "/" + Globals.THEME_PATH + "/" + mThemeName + ".mtz");

        ((TextView)findViewById(R.id.theme_name)).setText(mDetails.title);

        if (TextUtils.isEmpty(mThemeName))
            finish();

        setTitle(Theme.sElementLabels[mElementType]);

        mAdapter = new ImageAdapter(this);
        mPreviews = (Gallery) findViewById(R.id.previews);
        mPreviews.setAdapter(mAdapter);
        mPreviews.setSpacing(20);
        mPreviews.setAnimationDuration(1000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        mAdapter.destroyImages();
        mAdapter = null;
        System.gc();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ugly hack to keep the dialog from reappearing when the app is restarted
        // due to a theme change.
        try {
            dismissDialog(DIALOG_PROGRESS);
        } catch (Exception e) {}

        mAdapter.notifyDataSetChanged();
    }

    public void applyTheme(View view) {
        new ApplyThemeTask().execute(mThemeName + ".mtz");
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        private ImageView[] mImages;

        public ImageAdapter(Context c) {
            mContext = c;
            if (mImages == null) {
                mImages = new ImageView[mPreviewList.length];
            }
        }

        @Override
        public int getCount() {
            return mPreviewList.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (mImages[position] == null) {
                mImages[position] = new ImageView(mContext);
                FileInputStream is = null;
                try {
                    is = new FileInputStream(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(mThemeName) + "/" + mPreviewList[position]);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                if (is != null) {
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
                    Drawable drawable = new BitmapDrawable(bmp);
                    mImages[position].setImageDrawable(drawable);
                } else
                    mImages[position].setImageResource(R.drawable.no_preview);
                mImages[position].setAdjustViewBounds(true);
            }

            return mImages[position];

            //return mImages[position];
        }
        /** Returns the size (0.0f to 1.0f) of the views
         * depending on the 'offset' to the center. */
        public float getScale(boolean focused, int offset) {
            /* Formula: 1 / (2 ^ offset) */
            return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset)));
        }

        public void destroyImages() {
            for (int i = 0; mImages != null && i < mImages.length; i++) {
                if (mImages[i] != null) {
                    if (mImages[i].getDrawable() != null)
                        mImages[i].getDrawable().setCallback(null);
                    mImages[i].setImageDrawable(null);
                }
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getResources().getText(R.string.applying_theme));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false );
                mProgressDialog.setProgress(0);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    private class ApplyThemeTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog( DIALOG_PROGRESS );
        }

        protected Boolean doInBackground(String... theme) {
            try{
                ThemeZipUtils.extractThemeElement(THEMES_PATH + "/" + theme[0], "/data/system/theme", mElementType);
                IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
                try {
                    switch (mElementType) {
                        case Theme.THEME_ELEMENT_TYPE_ICONS:
                            ts.applyThemeIcons();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_WALLPAPER:
                            ts.applyThemeWallpaper();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_SYSTEMUI:
                            ts.applyThemeSystemUI();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_FRAMEWORK:
                            ts.applyThemeFramework();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_LOCKSCREEN:
                            ts.applyThemeLockscreen();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_RINGTONES:
                            ts.applyThemeRingtones();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_BOOTANIMATION:
                            ts.applyThemeBootanimation();
                            break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to call ThemeService.applyInstalledTheme", e);
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "ApplyThemeTask FileNotFoundException", e);
                return Boolean.FALSE;
            } catch (IOException e) {
                Log.e(TAG, "ApplyThemeTask IOException", e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Boolean result) {
            dismissDialog(DIALOG_PROGRESS);
            if (result.equals(Boolean.TRUE)) {
            }
        }
    }
}
