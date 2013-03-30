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
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import com.android.thememanager.Globals;
import com.android.thememanager.R;
import com.android.thememanager.SimpleDialogs;

import java.io.*;

public class InstallThemeActivity extends Activity {
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
                                try {
                                    if (isYes) {
                                        (new File(dst)).delete();
                                        copyFile(src, dst);
                                        (new File(src)).delete();
                                        Intent intent = new Intent(InstallThemeActivity.this, ThemeManagerTabActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else
                                        finish();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    finish();
                                }
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
                                try {
                                    if (isYes) {
                                        copyFile(src, dst);
                                        (new File(src)).delete();
                                        Intent intent = new Intent(InstallThemeActivity.this, ThemeManagerTabActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else
                                        finish();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    finish();
                                }
                            }
                        });
            }
        } else
            finish();
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