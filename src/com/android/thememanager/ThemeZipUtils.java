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

package com.android.thememanager;

import android.content.res.IThemeManagerService;
import android.os.ServiceManager;
import android.util.Log;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ThemeZipUtils {
    private static final String TAG = "ThemeZipUtils";

    /**
     * Simple copy routine given an input stream and an output stream
     */
    private static void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        out.close();
    }

    public static void extractTheme(String src, String dst) throws IOException {
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(
                new FileInputStream(src)));
        ZipEntry ze = null;

        while ((ze = zip.getNextEntry()) != null) {
            if (ze.isDirectory()) {
                // Assume directories are stored parents first then children
                Log.d(TAG, "Creating directory /data/system/theme/" + ze.getName());
                File dir = new File(dst + "/" + ze.getName());
                dir.mkdir();
                dir.setReadable(true, false);
                dir.setWritable(true, false);
                dir.setExecutable(true, false);
                zip.closeEntry();
                continue;
            }

            Log.d(TAG, "Creating file " + ze.getName());
            copyInputStream(zip,
                    new BufferedOutputStream(new FileOutputStream(dst + "/" + ze.getName())));
            (new File(dst + "/" + ze.getName())).setReadable(true, false);
            zip.closeEntry();
        }

        zip.close();
    }

    public static void extractThemeElement(String src, String dst, int elementType) throws IOException {
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(
                new FileInputStream(src)));
        ZipEntry ze = null;

        boolean done = false;

        while ((ze = zip.getNextEntry()) != null && !done) {
            switch (elementType) {
                case Theme.THEME_ELEMENT_TYPE_ICONS:
                    if (ze.getName().equals("icons")) {
                        copyInputStream(zip,
                                new BufferedOutputStream(new FileOutputStream(dst + "/" + ze.getName())));
                        (new File(dst + "/" + ze.getName())).setReadable(true, false);
                        done = true;
                    }
                    break;
                case Theme.THEME_ELEMENT_TYPE_WALLPAPER:
                    if (ze.getName().contains("wallpaper")) {
                        if (ze.isDirectory()) {
                            // Assume directories are stored parents first then children
                            Log.d(TAG, "Creating directory " + dst + "/" + ze.getName());
                            File dir = new File(dst + "/" + ze.getName());
                            dir.mkdir();
                            dir.setReadable(true, false);
                            dir.setWritable(true, false);
                            dir.setExecutable(true, false);
                            zip.closeEntry();
                            continue;
                        } else {
                            copyInputStream(zip,
                                    new BufferedOutputStream(new FileOutputStream(dst + "/" + ze.getName())));
                            (new File(dst + "/" + ze.getName())).setReadable(true, false);
                        }
                    }
                    break;
                case Theme.THEME_ELEMENT_TYPE_SYSTEMUI:
                    if (ze.getName().equals("com.android.systemui")) {
                        copyInputStream(zip,
                                new BufferedOutputStream(new FileOutputStream(dst + "/" + ze.getName())));
                        (new File(dst + "/" + ze.getName())).setReadable(true, false);
                        done = true;
                    }
                    break;
                case Theme.THEME_ELEMENT_TYPE_FRAMEWORK:
                    if (ze.getName().equals("framework-res")) {
                        copyInputStream(zip,
                                new BufferedOutputStream(new FileOutputStream(dst + "/" + ze.getName())));
                        (new File(dst + "/" + ze.getName())).setReadable(true, false);
                        done = true;
                    }
                    break;
                case Theme.THEME_ELEMENT_TYPE_LOCKSCREEN:
                    if (ze.getName().equals("lockscreen")) {
                        copyInputStream(zip,
                                new BufferedOutputStream(new FileOutputStream(dst + "/" + ze.getName())));
                        (new File(dst + "/" + ze.getName())).setReadable(true, false);
                        done = true;
                    }
                    break;
                case Theme.THEME_ELEMENT_TYPE_RINGTONES:
                    if (ze.getName().contains("ringtones")) {
                        if (ze.isDirectory()) {
                            // Assume directories are stored parents first then children
                            Log.d(TAG, "Creating directory /data/system/theme/" + ze.getName());
                            File dir = new File(dst + "/" + ze.getName());
                            dir.mkdir();
                            dir.setReadable(true, false);
                            dir.setWritable(true, false);
                            dir.setExecutable(true, false);
                            zip.closeEntry();
                            continue;
                        } else {
                            copyInputStream(zip,
                                    new BufferedOutputStream(new FileOutputStream(dst + "/" + ze.getName())));
                            (new File(dst + "/" + ze.getName())).setReadable(true, false);
                        }
                    }
                    break;
                case Theme.THEME_ELEMENT_TYPE_BOOTANIMATION:
                    if (ze.getName().contains("boots")) {
                        if (ze.isDirectory()) {
                            // Assume directories are stored parents first then children
                            Log.d(TAG, "Creating directory /data/system/theme/" + ze.getName());
                            File dir = new File(dst + "/" + ze.getName());
                            dir.mkdir();
                            dir.setReadable(true, false);
                            dir.setWritable(true, false);
                            dir.setExecutable(true, false);
                            zip.closeEntry();
                            continue;
                        } else {
                            copyInputStream(zip,
                                    new BufferedOutputStream(new FileOutputStream(dst + "/" + ze.getName())));
                            (new File(dst + "/" + ze.getName())).setReadable(true, false);
                        }
                    }
                    break;
            }
            zip.closeEntry();
        }

        zip.close();
    }
}
