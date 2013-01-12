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

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ThemesDataSource {

    // Database fields
    private SQLiteDatabase database;
    private ThemeSQLiteHelper dbHelper;
    private String[] allColumns = {
            ThemeSQLiteHelper.COLUMN_ID,
            ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME,
            ThemeSQLiteHelper.COLUMN_THEME_TITLE,
            ThemeSQLiteHelper.COLUMN_THEME_AUTHOR,
            ThemeSQLiteHelper.COLUMN_THEME_DESIGNER,
            ThemeSQLiteHelper.COLUMN_THEME_VERSION,
            ThemeSQLiteHelper.COLUMN_THEME_UI_VERRSION,
            ThemeSQLiteHelper.COLUMN_THEME_PATH,
            ThemeSQLiteHelper.COLUMN_IS_COS_THEME,
            ThemeSQLiteHelper.COLUMN_HAS_WALLPAPER,
            ThemeSQLiteHelper.COLUMN_HAS_ICONS,
            ThemeSQLiteHelper.COLUMN_HAS_LOCKSCREEN,
            ThemeSQLiteHelper.COLUMN_HAS_SYSTEMUI,
            ThemeSQLiteHelper.COLUMN_HAS_FRAMEWORK,
            ThemeSQLiteHelper.COLUMN_HAS_RINGTONES,
            ThemeSQLiteHelper.COLUMN_HAS_BOOTANIMATION,
            ThemeSQLiteHelper.COLUMN_HAS_MMS,
            ThemeSQLiteHelper.COLUMN_LAST_MODIFIED };

    public ThemesDataSource(Context context) {
        dbHelper = new ThemeSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Theme createThemeEntry(Theme theme) {
        ContentValues values = new ContentValues();
        values.put(ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME, theme.getFileName());
        values.put(ThemeSQLiteHelper.COLUMN_THEME_TITLE, theme.getTitle());
        values.put(ThemeSQLiteHelper.COLUMN_THEME_AUTHOR, theme.getAuthor());
        values.put(ThemeSQLiteHelper.COLUMN_THEME_DESIGNER, theme.getDesigner());
        values.put(ThemeSQLiteHelper.COLUMN_THEME_VERSION, theme.getVersion());
        values.put(ThemeSQLiteHelper.COLUMN_THEME_UI_VERRSION, theme.getUiVersion());
        values.put(ThemeSQLiteHelper.COLUMN_THEME_PATH, theme.getThemePath());
        values.put(ThemeSQLiteHelper.COLUMN_IS_COS_THEME, theme.getIsCosTheme());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_WALLPAPER, theme.getHasWallpaper());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_ICONS, theme.getHasIcons());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_LOCKSCREEN, theme.getHasLockscreen());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_SYSTEMUI, theme.getHasSystemUI());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_FRAMEWORK, theme.getHasFramework());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_RINGTONES, theme.getHasRingtones());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_BOOTANIMATION, theme.getHasBootanimation());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_MMS, theme.getHasMms());
        values.put(ThemeSQLiteHelper.COLUMN_LAST_MODIFIED, "" + theme.getLastModified());
        long insertId;
        if (entryExists(theme.getFileName()))
            insertId = database.update(ThemeSQLiteHelper.TABLE_THEMES, values,
                    ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME + "='" + theme.getFileName() + "'",
                    allColumns);
        else
            insertId = database.insert(ThemeSQLiteHelper.TABLE_THEMES, null,
                    values);
        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, ThemeSQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Theme newTheme = cursorToTheme(cursor);
        cursor.close();
        return newTheme;
    }

    public void deleteTheme(Theme theme) {
        long id = theme.getId();
        System.out.println("Theme deleted with id: " + id);
        database.delete(ThemeSQLiteHelper.TABLE_THEMES, ThemeSQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public boolean entryExists(String themeId) {
        Cursor c = database.query(ThemeSQLiteHelper.TABLE_THEMES, allColumns,
                ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME + "='" + themeId + "'",
                null, null, null, null);

        boolean exists = (c != null && c.getCount() > 0);
        c.close();
        return exists;
    }

    public boolean entryIsOlder(String themeId, long lastModified) {
        Cursor c = database.query(ThemeSQLiteHelper.TABLE_THEMES, allColumns,
                ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME + "='" + themeId + "'",
                null, null, null, null);
        boolean isOlder = false;

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            String modified = c.getString(c.getColumnIndex(ThemeSQLiteHelper.COLUMN_LAST_MODIFIED));
            if (modified == null || lastModified > Long.parseLong(modified))
                isOlder = true;
        }
        c.close();
        return isOlder;
    }

    public Theme getThemeById(long id) {
        Theme theme = null;

        Cursor c = database.query(ThemeSQLiteHelper.TABLE_THEMES, allColumns,
                ThemeSQLiteHelper.COLUMN_ID + "='" + id + "'",
                null, null, null, null);

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            theme = cursorToTheme(c);
        }

        c.close();
        return theme;
    }

    public List<Theme> getAllThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Theme theme = cursorToTheme(cursor);
            themes.add(theme);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return themes;
    }

    public List<Theme> getIconThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, ThemeSQLiteHelper.COLUMN_HAS_ICONS + "='1'",
                null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Theme theme = cursorToTheme(cursor);
            themes.add(theme);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return themes;
    }

    public List<Theme> getWallpaperThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, ThemeSQLiteHelper.COLUMN_HAS_WALLPAPER + "='1'",
                null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Theme theme = cursorToTheme(cursor);
            themes.add(theme);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return themes;
    }

    public List<Theme> getSystemUIThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, ThemeSQLiteHelper.COLUMN_HAS_SYSTEMUI + "='1'",
                null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Theme theme = cursorToTheme(cursor);
            themes.add(theme);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return themes;
    }

    public List<Theme> getFrameworkThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, ThemeSQLiteHelper.COLUMN_HAS_FRAMEWORK + "='1'",
                null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Theme theme = cursorToTheme(cursor);
            themes.add(theme);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return themes;
    }

    public List<Theme> getLockscreenThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, ThemeSQLiteHelper.COLUMN_HAS_LOCKSCREEN + "='1'",
                null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Theme theme = cursorToTheme(cursor);
            themes.add(theme);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return themes;
    }

    public List<Theme> getRingtoneThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, ThemeSQLiteHelper.COLUMN_HAS_RINGTONES + "='1'",
                null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Theme theme = cursorToTheme(cursor);
            themes.add(theme);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return themes;
    }

    public List<Theme> getBootanimationThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, ThemeSQLiteHelper.COLUMN_HAS_BOOTANIMATION + "='1'",
                null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Theme theme = cursorToTheme(cursor);
            themes.add(theme);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return themes;
    }

    public List<Theme> getMmsThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, ThemeSQLiteHelper.COLUMN_HAS_MMS + "='1'",
                null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Theme theme = cursorToTheme(cursor);
            themes.add(theme);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return themes;
    }

    private Theme cursorToTheme(Cursor cursor) {
        Theme theme = new Theme();
        theme.setId(cursor.getLong(0));
        theme.setFileName(cursor.getString(1));
        theme.setTitle(cursor.getString(2));
        theme.setAuthor(cursor.getString(3));
        theme.setDesigner(cursor.getString(4));
        theme.setVersion(cursor.getString(5));
        theme.setUiVersion(cursor.getLong(6));
        theme.setThemePath(cursor.getString(7));
        theme.setIsCosTheme(cursor.getInt(8) == 1);
        theme.setHasWallpaper(cursor.getInt(9) == 1);
        theme.setHasIcons(cursor.getInt(10) == 1);
        theme.setHasLockscreen(cursor.getInt(11) == 1);
        theme.setHasSystemUI(cursor.getInt(12) == 1);
        theme.setHasFramework(cursor.getInt(13) == 1);
        theme.setHasRingtones(cursor.getInt(14) == 1);
        theme.setHasBootanimation(cursor.getInt(15) == 1);
        theme.setHasBootanimation(cursor.getInt(16) == 1);
        theme.setLastModified(Long.getLong(cursor.getString(17), 0));
        return theme;
    }
}