package com.android.thememanager;/*
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

import java.io.File;
import java.io.FilenameFilter;

public class PreviewHelper {
    public static final String PREVIEW_PREFIX = "preview_";
    public static final String PREVIEW_LAUNCHER_PREFIX = "preview_launcher_";
    public static final String PREVIEW_ICONS_PREFIX = "preview_icons_";
    public static final String PREVIEW_STATUSBAR_PREFIX = "preview_statusbar_";
    public static final String PREVIEW_MMS_PREFIX = "preview_mms_";
    public static final String PREVIEW_CONTACTS_PREFIX = "preview_contact_";
    public static final String PREVIEW_BOOTANIMATION_PREFIX = "preview_animation_";

    static FilenameFilter mAllPreviewsFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            if (s.toLowerCase().contains(PREVIEW_PREFIX) &&
                    s.toLowerCase().endsWith(".png"))
                return true;
            else
                return false;
        }
    };

    static FilenameFilter mLauncherPreviewsFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            if (s.toLowerCase().contains(PREVIEW_LAUNCHER_PREFIX) &&
                    s.toLowerCase().endsWith(".png"))
                return true;
            else
                return false;
        }
    };

    static FilenameFilter mIconPreviewsFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            if (s.toLowerCase().contains(PREVIEW_ICONS_PREFIX) &&
                    s.toLowerCase().endsWith(".png"))
                return true;
            else
                return false;
        }
    };

    static FilenameFilter mStatusbarPreviewsFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            if (s.toLowerCase().contains(PREVIEW_STATUSBAR_PREFIX) &&
                    s.toLowerCase().endsWith(".png"))
                return true;
            else
                return false;
        }
    };

    static FilenameFilter mMmsPreviewsFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            if (s.toLowerCase().contains(PREVIEW_MMS_PREFIX) &&
                    s.toLowerCase().endsWith(".png"))
                return true;
            else
                return false;
        }
    };

    static FilenameFilter mContactsPreviewsFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            if (s.toLowerCase().contains(PREVIEW_CONTACTS_PREFIX) &&
                    s.toLowerCase().endsWith(".png"))
                return true;
            else
                return false;
        }
    };

    static FilenameFilter mBootanimationPreviewsFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            if (s.toLowerCase().contains(PREVIEW_BOOTANIMATION_PREFIX) &&
                    s.toLowerCase().endsWith(".png"))
                return true;
            else
                return false;
        }
    };

    public static String[] getAllPreviews(String path) {
        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(mAllPreviewsFilter);

        return dirList;
    }

    public static String[] getLauncherPreviews(String path) {
        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(mLauncherPreviewsFilter);

        return dirList;
    }

    public static String[] getIconPreviews(String path) {
        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(mIconPreviewsFilter);

        return dirList;
    }

    public static String[] getStatusbarPreviews(String path) {
        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(mStatusbarPreviewsFilter);

        return dirList;
    }

    public static String[] getMmsPreviews(String path) {
        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(mMmsPreviewsFilter);

        return dirList;
    }

    public static String[] getContactsPreviews(String path) {
        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(mContactsPreviewsFilter);

        return dirList;
    }

    public static String[] getBootanimationPreviews(String path) {
        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(mBootanimationPreviewsFilter);

        return dirList;
    }

}
