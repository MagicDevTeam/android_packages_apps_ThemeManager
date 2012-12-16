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
import android.content.res.IThemeManagerService;
import android.os.*;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.android.thememanager.*;
import com.android.thememanager.widget.BootanimationImageView;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ThemeBootanimationDetailActivity extends Activity {
    private static final String TAG = "ThemeManager";
    private static final String THEMES_PATH = Globals.DEFAULT_THEME_PATH;

    private static final int DIALOG_PROGRESS = 0;

    private BootanimationImageView mPreview = null;
    private ProgressDialog mProgressDialog;
    private String mThemeName = "";
    private ThemeUtils.ThemeDetails mDetails;
    private int mElementType = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation_detail);


        mThemeName = getIntent().getStringExtra("theme_name");
        mElementType = getIntent().getIntExtra("type", 0);
        mDetails = ThemeUtils.getThemeDetails(Environment.getExternalStorageDirectory()
                + "/" + Globals.THEME_PATH + "/" + mThemeName + ".mtz");

        ((TextView)findViewById(R.id.theme_name)).setText(mDetails.title);

        if (TextUtils.isEmpty(mThemeName))
            finish();

        setTitle(Theme.sElementLabels[mElementType]);

        mPreview = (BootanimationImageView) findViewById(R.id.preview);
        try {
            extractAnimation(Globals.DEFAULT_THEME_PATH + "/" + mThemeName + ".mtz");
            mPreview.LoadAnimation(Globals.CACHE_DIR + "/bootanimation.zip");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void extractAnimation(String path) throws IOException {
        ZipFile zip = new ZipFile(path);
        ZipEntry entry = zip.getEntry("boots/bootanimation.zip");
        byte[] buffer = new byte[1024];
        FileOutputStream out = new FileOutputStream(Globals.CACHE_DIR + "/bootanimation.zip");
        copyInputStream(zip.getInputStream(entry), out);
        out.close();
        zip.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ugly hack to keep the dialog from reappearing when the app is restarted
        // due to a theme change.
        try {
            dismissDialog(DIALOG_PROGRESS);
        } catch (Exception e) {}
    }

    public void applyTheme(View view) {
        new ApplyThemeTask().execute(mThemeName + ".mtz");
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

    /**
     * Simple copy routine given an input stream and an output stream
     */
    private void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        out.close();
    }

    private class ApplyThemeTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog( DIALOG_PROGRESS );
        }

        protected Boolean doInBackground(String... theme) {
            try{
                ThemeZipUtils.extractThemeElement(THEMES_PATH + "/" + theme[0], "/data/system/theme",
                        Theme.THEME_ELEMENT_TYPE_BOOTANIMATION);

                IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
                ts.applyThemeBootanimation();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "ApplyThemeTask FileNotFoundException", e);
                return Boolean.FALSE;
            } catch (IOException e) {
                Log.e(TAG, "ApplyThemeTask IOException", e);
                return Boolean.FALSE;
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to call ThemeService.applyInstalledTheme", e);
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
