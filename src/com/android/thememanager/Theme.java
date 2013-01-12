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

public class Theme {

    public static final int THEME_ELEMENT_TYPE_ICONS = 0;
    public static final int THEME_ELEMENT_TYPE_WALLPAPER = 1;
    public static final int THEME_ELEMENT_TYPE_SYSTEMUI = 2;
    public static final int THEME_ELEMENT_TYPE_FRAMEWORK = 3;
    public static final int THEME_ELEMENT_TYPE_LOCKSCREEN = 4;
    public static final int THEME_ELEMENT_TYPE_RINGTONES = 5;
    public static final int THEME_ELEMENT_TYPE_BOOTANIMATION = 6;
    public static final int THEME_ELEMENT_TYPE_MMS = 7;

    public static int[] sElementIcons = { android.R.drawable.ic_menu_view,
            android.R.drawable.ic_menu_gallery,
            android.R.drawable.ic_menu_sort_by_size,
            android.R.drawable.ic_menu_today,
            android.R.drawable.ic_menu_rotate,
            android.R.drawable.ic_menu_call,
            android.R.drawable.ic_menu_slideshow,
            android.R.drawable.ic_menu_send };

    public static int[] sElementLabels = { R.string.mixer_icons_label,
            R.string.mixer_walllpaper_label,
            R.string.mixer_systemui_label,
            R.string.mixer_framework_label,
            R.string.mixer_lockscreen_label,
            R.string.mixer_ringtones_label,
            R.string.mixer_bootanimation_label,
            R.string.mixer_mms_label };

    private long id;
    private String fileName;
    private String title;
    private String author;
    private String designer;
    private String version;
    private long uiVersion;
    private String themePath;
    private boolean isCosTheme;
    private boolean hasWallpaper;
    private boolean hasIcons;
    private boolean hasLockscreen;
    private boolean hasSystemUI;
    private boolean hasFramework;
    private boolean hasRingtones;
    private boolean hasBootanimation;
    private boolean hasMms;
    private long lastModified;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDesigner() {
        return designer;
    }

    public void setDesigner(String designer) {
        this.designer = designer;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getUiVersion() {
        return uiVersion;
    }

    public void setUiVersion(long uiVersion) {
        this.uiVersion = uiVersion;
    }

    public String getThemePath() {
        return themePath;
    }

    public void setThemePath(String themePath) {
        this.themePath = themePath;
    }

    public boolean getIsCosTheme() {
        return isCosTheme;
    }

    public void setIsCosTheme(boolean isCosTheme) {
        this.isCosTheme = isCosTheme;
    }

    public boolean getHasWallpaper() {
        return hasWallpaper;
    }

    public void setHasWallpaper(boolean hasWallpaper) {
        this.hasWallpaper = hasWallpaper;
    }

    public boolean getHasIcons() {
        return hasIcons;
    }

    public void setHasIcons(boolean hasIcons) {
        this.hasIcons = hasIcons;
    }

    public boolean getHasLockscreen() {
        return hasLockscreen;
    }

    public void setHasLockscreen(boolean hasLockscreen) {
        this.hasLockscreen = hasLockscreen;
    }

    public boolean getHasSystemUI() {
        return hasSystemUI;
    }

    public void setHasSystemUI(boolean hasSystemUI) {
        this.hasSystemUI = hasSystemUI;
    }

    public boolean getHasFramework() {
        return hasFramework;
    }

    public void setHasFramework(boolean hasFramework) {
        this.hasFramework = hasFramework;
    }

    public boolean getHasRingtones() {
        return hasRingtones;
    }

    public void setHasRingtones(boolean hasRingtones) {
        this.hasRingtones = hasRingtones;
    }

    public boolean getHasBootanimation() {
        return hasBootanimation;
    }

    public void setHasBootanimation(boolean hasBootanimation) {
        this.hasBootanimation = hasBootanimation;
    }

    public boolean getHasMms() {
        return hasMms;
    }

    public void setHasMms(boolean hasMms) {
        this.hasMms = hasMms;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}
