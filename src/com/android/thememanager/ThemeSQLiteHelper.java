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

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ThemeSQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_THEMES = "themes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_THEME_FILE_NAME = "file_name";
    public static final String COLUMN_THEME_TITLE = "title";
    public static final String COLUMN_THEME_AUTHOR = "author";
    public static final String COLUMN_THEME_DESIGNER = "designer";
    public static final String COLUMN_THEME_VERSION = "version";
    public static final String COLUMN_THEME_UI_VERRSION = "ui_version";
    public static final String COLUMN_THEME_PATH = "theme_path";
    public static final String COLUMN_IS_COS_THEME = "cos_theme";
    public static final String COLUMN_HAS_WALLPAPER = "has_wallpaper";
    public static final String COLUMN_HAS_ICONS = "has_icons";
    public static final String COLUMN_HAS_LOCKSCREEN = "has_lockscreen";
    public static final String COLUMN_HAS_SYSTEMUI = "has_systemui";
    public static final String COLUMN_HAS_FRAMEWORK = "has_framework";
    public static final String COLUMN_HAS_RINGTONES = "has_ringtones";
    public static final String COLUMN_HAS_BOOTANIMATION = "has_bootanimation";
    public static final String COLUMN_LAST_MODIFIED = "last_modified";

    private static final String DATABASE_NAME = "themesdb";
    private static final int DATABASE_VERSION = 3;

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_THEMES + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_THEME_FILE_NAME
            + " text not null, " + COLUMN_THEME_TITLE
            + " text, " + COLUMN_THEME_AUTHOR
            + " text, " + COLUMN_THEME_DESIGNER
            + " text, " + COLUMN_THEME_VERSION
            + " text, " + COLUMN_THEME_UI_VERRSION
            + " text, " + COLUMN_THEME_PATH
            + " text not null, " + COLUMN_IS_COS_THEME
            + " integer, " + COLUMN_HAS_WALLPAPER
            + " integer, " + COLUMN_HAS_ICONS
            + " integer, " + COLUMN_HAS_LOCKSCREEN
            + " integer, " + COLUMN_HAS_SYSTEMUI
            + " integer, " + COLUMN_HAS_FRAMEWORK
            + " integer, " + COLUMN_HAS_RINGTONES
            + " integer, " + COLUMN_HAS_BOOTANIMATION
            + " integer, " + COLUMN_LAST_MODIFIED
            + " text);";


    public ThemeSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ThemeSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_THEMES);
        onCreate(db);
    }
}
