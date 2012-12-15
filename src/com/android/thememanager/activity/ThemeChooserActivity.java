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

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.IThemeManagerService;
import android.os.*;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.android.thememanager.Globals;
import com.android.thememanager.ThemeUtils;
import com.android.thememanager.PreviewManager;
import com.android.thememanager.R;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ThemeChooserActivity extends Activity {
    private static final String TAG = "ThemeManager";
    private static final String THEMES_PATH = Globals.DEFAULT_THEME_PATH;

    private static final int DIALOG_PROGRESS = 0;

    private GridView mGridView = null;
    private String[] mThemeList = null;
    private Button mApplyButton = null;
    private ImageAdapter mAdapter = null;
    private ProgressDialog mProgressDialog;
    private LoadThemesInfoTask mTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_theme_chooser);

        mThemeList = themeList(THEMES_PATH);

        ThemeUtils.createCacheDir();

        mGridView = (GridView) findViewById(R.id.coverflow);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ThemeChooserActivity.this, ThemeDetailActivity.class);
                intent.putExtra("theme_name", mThemeList[i]);
                startActivity(intent);
            }
        });
        getActionBar().show();

        mTask = (LoadThemesInfoTask)getLastNonConfigurationInstance();
        if (mTask == null) {
            mTask = new LoadThemesInfoTask(this);
            mTask.execute();
        } else {
            mTask.attach(this);
            if (mTask.getProgress() >= mThemeList.length)
                markAsDone();
        }
	}

    private Handler mViewUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mAdapter = new ImageAdapter(ThemeChooserActivity.this);
            mGridView.setAdapter(mAdapter);
            dismissDialog(DIALOG_PROGRESS);
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        dismissDialog(DIALOG_PROGRESS);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_theme_chooser, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_reset:
                // have the theme service remove the existing theme
                IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
                try {
                    ts.removeThemeAndApply();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to call ThemeService.removeTheme", e);
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.destroyImages();
        mAdapter = null;
        System.gc();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        mTask.detach();
        return mTask;
    }

    private String[] themeList(String path) {
        Log.d(TAG, "Returning theme list for " + path);
        FilenameFilter themeFilter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if (s.toLowerCase().endsWith(".mtz"))
                    return true;
                else
                    return false;
            }
        };

        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(themeFilter);
        else
            Log.e(TAG, path + " does not exist or is not a directory!");
        return dirList;
    }

    void markAsDone() {
        mViewUpdateHandler.sendEmptyMessage(0);
    }

    public class ImageAdapter extends BaseAdapter {
        int mGalleryItemBackground;
        private Context mContext;

        private PreviewManager mPreviewManager = new PreviewManager();

        private String[] mImageIds = themeList(THEMES_PATH);

        private ImageView[] mImages;

        private ThemeUtils.ThemeDetails[] mDetails;

        public ImageAdapter(Context c) {
            mContext = c;
            if (mImages == null) {
                mImages = new ImageView[mImageIds.length];
                mDetails = new ThemeUtils.ThemeDetails[mImageIds.length];
                for (int i = 0; i < mImages.length; i++) {
                    mDetails[i] = ThemeUtils.getThemeDetails(
                            THEMES_PATH + "/" + mImageIds[i]);
                }
            }
        }

        public int getCount() {
            return mImageIds.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.theme_preview, null);
            }
            ImageView i = (ImageView)v.findViewById(R.id.preview_image);//mImages[position];//new ImageView(mContext);
            if (mImages[position] == null) {
                mPreviewManager.fetchDrawableOnThread(ThemeUtils.stripExtension(mImageIds[position]), i);
                mImages[position] = i;
            } else
                i.setImageDrawable(mImages[position].getDrawable());
            i.setAdjustViewBounds(true);

            TextView tv = (TextView) v.findViewById(R.id.theme_name);

            tv.setText(mDetails[position].title);

            return v;
        }

        public void destroyImages() {
            for (int i = 0; i < mImages.length; i++) {
                if (mImages[i].getDrawable() != null)
                    mImages[i].getDrawable().setCallback(null);
                mImages[i].setImageDrawable(null);
            }

            mPreviewManager = null;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getResources().getText(R.string.loading_themes));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false );
                mProgressDialog.setProgress(0);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    private class LoadThemesInfoTask extends AsyncTask<String, Integer, Boolean> {
        ThemeChooserActivity activity = null;
        int progress = 0;

        LoadThemesInfoTask(ThemeChooserActivity activity) {
            attach(activity);
        }

        void detach() {
            activity = null;
        }

        void attach(ThemeChooserActivity activity) {
            this.activity = activity;
        }

        int getProgress() {
            return(progress);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_PROGRESS);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            for (String themeId : mThemeList) {
                ThemeUtils.addThemeEntryToDb(ThemeUtils.stripExtension(themeId),
                        THEMES_PATH + "/" + themeId,
                        ThemeChooserActivity.this);

                progress++;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (activity != null)
                activity.markAsDone();
        }
    }
}
