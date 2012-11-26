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

import android.os.Environment;
import android.util.Log;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ThemeUtils {
    private static final String TAG = "ThemeUtils";

    /**
     * Checks if CACHE_DIR exists and returns true if it does
     */
    public static boolean cacheDirExists() {
        return (new File(Globals.CACHE_DIR)).exists();
    }

    /**
     * Creates CACHE_DIR if it does not already exist
     */
    public static void createCacheDir() {
        if (!cacheDirExists()) {
            Log.d(TAG, "Creating cache directory");
            File dir = new File(Globals.CACHE_DIR);
            dir.mkdirs();
        }
    }

    /**
     * Checks if CACHE_DIR/themeName exists and returns true if it does
     */
    public static boolean themeCacheDirExists(String themeName) {
        return (new File(Globals.CACHE_DIR + "/" + themeName)).exists();
    }

    /**
     * Creates a directory inside CACHE_DIR for the given theme
     */
    public static void createThemeCacheDir(String themeName) {
        if (!themeCacheDirExists(themeName)) {
            Log.d(TAG, "Creating theme cache directory for " + themeName);
            File dir = new File(Globals.CACHE_DIR + "/" + themeName);
            dir.mkdirs();
        }
    }

    /**
     * Strips the file extension off of the given filename
     */
    public static String stripExtension(String filename) {
        if (filename.lastIndexOf('.') > -1) {
            filename = filename.substring(0, filename.lastIndexOf('.'));
        }

        return filename;
    }

    /**
     * Simple copy routine given an input stream and an output stream
     */
    public static void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        out.close();
    }

    public static boolean extractThemePreviews(String themeId, String themePath) {
        try {
            FileInputStream fis = new FileInputStream(themePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = null;

            if (!themeCacheDirExists(themeId))
                createThemeCacheDir(themeId);

            while ((ze = zis.getNextEntry()) != null) {
                if (ze.getName().contains("preview_")) {
                    String previewName = ze.getName().substring(ze.getName().lastIndexOf('/') + 1);
                    FileOutputStream out = new FileOutputStream(Globals.CACHE_DIR + "/" + themeId + "/" + previewName);
                    copyInputStream(zis, out);
                    zis.closeEntry();
                }
            }
            zis.close();
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
