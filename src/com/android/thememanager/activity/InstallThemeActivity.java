/*
 * Copyright (C) 2013 The ChameleonOS Project
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
import android.app.DownloadManager;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.android.thememanager.Globals;
import com.android.thememanager.R;
import com.android.thememanager.SimpleDialogs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class InstallThemeActivity extends Activity implements LoaderManager.LoaderCallbacks {
    private static final int DIALOG_PROGRESS = 0;

    private ProgressDialog mProgressDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri themeUri = getIntent().getData();
        String filePath = null;
        // check if the file is coming from the download provider and
        // if so use the DownloadManager.query() to get the file path
        if (themeUri.toString().contains("content://downloads")) {
            long id = Long.parseLong(themeUri.toString().substring(themeUri.toString().lastIndexOf('/') + 1));
            DownloadManager.Query q = new DownloadManager.Query();
            DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            q.setFilterById(id);
            Cursor c = dm.query(q);
            if (c.moveToFirst()) {
                filePath = c.getString(c.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_FILENAME));
            }
            c.close();
        } else {
            filePath = themeUri.getEncodedPath();
        }
        if (filePath != null) {
            if (!filePath.toLowerCase().endsWith("ctz") && !filePath.toLowerCase().endsWith("mtz")) {
                Toast.makeText(this, R.string.not_a_theme_file, Toast.LENGTH_LONG).show();
                finish();
            }
            final String src = filePath;
            final String dst = Globals.DEFAULT_THEME_PATH + "/" +
                    src.substring(src.lastIndexOf('/') + 1);
            if ((new File(dst)).exists()) {
                SimpleDialogs.displayYesNoDialog(getString(R.string.dlg_theme_exists_install),
                        getString(R.string.dlg_theme_exists_cancel),
                        getString(R.string.dlg_theme_exists_title),
                        getString(R.string.dlg_theme_exists_body),
                        this,
                        new SimpleDialogs.OnYesNoResponse() {
                            @Override
                            public void onYesNoResponse(boolean isYes) {
                                if (isYes) {
                                    (new File(dst)).delete();
                                    useLoader(src);
                                } else
                                    finish();
                            }
                        });
            } else {
                SimpleDialogs.displayYesNoDialog(getString(R.string.dlg_install_theme_install),
                        getString(R.string.dlg_install_theme_cancel),
                        getString(R.string.dlg_install_theme_title),
                        getString(R.string.dlg_install_theme_body),
                        this,
                        new SimpleDialogs.OnYesNoResponse() {
                            @Override
                            public void onYesNoResponse(boolean isYes) {
                                if (isYes) {
                                    useLoader(src);
                                } else
                                    finish();
                            }
                        });
            }
        } else
            finish();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getResources().getText(R.string.installing_theme));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false );
                mProgressDialog.setProgress(0);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    private void useLoader(String src) {
        Bundle args = new Bundle();
        args.putString("themeSource", src);
        Loader loader = getLoaderManager().initLoader(0, args, this);
        loader.forceLoad();
        showDialog(DIALOG_PROGRESS);
    }

    @Override
    public Loader onCreateLoader(int i, Bundle args) {
        return new InstallThemeLoader(this, args);
    }

    @Override
    public void onLoadFinished(Loader loader, Object o) {
        dismissDialog(DIALOG_PROGRESS);
        boolean result = ((Boolean)o).booleanValue();
        if (result == true) {
            Intent intent = new Intent(InstallThemeActivity.this, ThemeManagerTabActivity.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    private static class InstallThemeLoader extends AsyncTaskLoader {
        private String mThemeSource;
        private String mThemeDestination;

        public InstallThemeLoader(Context context, Bundle args) {
            super(context);
            mThemeSource = args.getString("themeSource");
            mThemeDestination = Globals.DEFAULT_THEME_PATH + "/" +
                    mThemeSource.substring(mThemeSource.lastIndexOf('/') + 1);
        }

        @Override
        public Object loadInBackground() {
            try {
                copyFile(mThemeSource, mThemeDestination);
            } catch (IOException e) {
                return Boolean.FALSE;
            }
            (new File(mThemeSource)).delete();
            return Boolean.TRUE;
        }

        private void copyFile(String src, String dst) throws IOException {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(src));
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dst));
            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        }
    }
}
