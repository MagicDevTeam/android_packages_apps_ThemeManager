/*
 * Copyright (C) 2013 The ChameleonOS Project
 *
 * Licensed under the GNU GPLv2 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl-2.0.txt
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
            ThemeSQLiteHelper.COLUMN_LAST_MODIFIED,
            ThemeSQLiteHelper.COLUMN_THEME_TITLE,
            ThemeSQLiteHelper.COLUMN_THEME_AUTHOR,
            ThemeSQLiteHelper.COLUMN_THEME_DESIGNER,
            ThemeSQLiteHelper.COLUMN_THEME_VERSION,
            ThemeSQLiteHelper.COLUMN_THEME_UI_VERSION,
            ThemeSQLiteHelper.COLUMN_THEME_PATH,
            ThemeSQLiteHelper.COLUMN_IS_COS_THEME,
            ThemeSQLiteHelper.COLUMN_IS_DEFAULT_THEME,
            ThemeSQLiteHelper.COLUMN_HAS_WALLPAPER,
            ThemeSQLiteHelper.COLUMN_HAS_LOCK_WALLPAPER,
            ThemeSQLiteHelper.COLUMN_HAS_ICONS,
            ThemeSQLiteHelper.COLUMN_HAS_CONTACTS,
            ThemeSQLiteHelper.COLUMN_HAS_DIALER,
            ThemeSQLiteHelper.COLUMN_HAS_SYSTEMUI,
            ThemeSQLiteHelper.COLUMN_HAS_FRAMEWORK,
            ThemeSQLiteHelper.COLUMN_HAS_RINGTONE,
            ThemeSQLiteHelper.COLUMN_HAS_NOTIFICATION,
            ThemeSQLiteHelper.COLUMN_HAS_BOOTANIMATION,
            ThemeSQLiteHelper.COLUMN_HAS_MMS,
            ThemeSQLiteHelper.COLUMN_HAS_FONT,
            ThemeSQLiteHelper.COLUMN_IS_COMPLETE };

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
        values.put(ThemeSQLiteHelper.COLUMN_LAST_MODIFIED, "" + theme.getLastModified());
        values.put(ThemeSQLiteHelper.COLUMN_THEME_TITLE, theme.getTitle());
        values.put(ThemeSQLiteHelper.COLUMN_THEME_AUTHOR, theme.getAuthor());
        values.put(ThemeSQLiteHelper.COLUMN_THEME_DESIGNER, theme.getDesigner());
        values.put(ThemeSQLiteHelper.COLUMN_THEME_VERSION, theme.getVersion());
        values.put(ThemeSQLiteHelper.COLUMN_THEME_UI_VERSION, theme.getUiVersion());
        values.put(ThemeSQLiteHelper.COLUMN_THEME_PATH, theme.getThemePath());
        values.put(ThemeSQLiteHelper.COLUMN_IS_COS_THEME, theme.getIsCosTheme());
        values.put(ThemeSQLiteHelper.COLUMN_IS_DEFAULT_THEME, theme.getIsDefaultTheme());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_WALLPAPER, theme.getHasWallpaper());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_LOCK_WALLPAPER, theme.getHasLockscreenWallpaper());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_ICONS, theme.getHasIcons());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_CONTACTS, theme.getHasContacts());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_DIALER, theme.getHasDialer());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_SYSTEMUI, theme.getHasSystemUI());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_FRAMEWORK, theme.getHasFramework());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_RINGTONE, theme.getHasRingtone());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_NOTIFICATION, theme.getHasNotification());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_BOOTANIMATION, theme.getHasBootanimation());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_MMS, theme.getHasMms());
        values.put(ThemeSQLiteHelper.COLUMN_HAS_FONT, theme.getHasFont());
        values.put(ThemeSQLiteHelper.COLUMN_IS_COMPLETE, theme.getIsComplete());
        long insertId;
        if (entryExists(theme.getFileName()))
            insertId = database.update(ThemeSQLiteHelper.TABLE_THEMES, values,
                    ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME + "='" + theme.getFileName() + "'",
                    null);
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

        boolean exists = false;
        if (c != null) {
            exists = c.getCount() > 0;
            c.close();
        }
        return exists;
    }

    public boolean entryIsOlder(String themeId, long lastModified) {
        Cursor c = database.query(ThemeSQLiteHelper.TABLE_THEMES, allColumns,
                ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME + "='" + themeId + "'",
                null, null, null, null);
        boolean isOlder = false;

        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                String modified = c.getString(c.getColumnIndex(ThemeSQLiteHelper.COLUMN_LAST_MODIFIED));
                if (modified == null || lastModified > Long.parseLong(modified))
                    isOlder = true;
            }
            c.close();
        }
        return isOlder;
    }

    public Theme getThemeById(long id) {
        Theme theme = null;

        Cursor c = database.query(ThemeSQLiteHelper.TABLE_THEMES, allColumns,
                ThemeSQLiteHelper.COLUMN_ID + "='" + id + "'",
                null, null, null, null);

        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                theme = cursorToTheme(c);
            }
            c.close();
        }
        return theme;
    }

    public List<Theme> getAllThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, null, null, null, null,
                ThemeSQLiteHelper.COLUMN_IS_COMPLETE + " DESC");

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

    public List<Theme> getCompleteThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, ThemeSQLiteHelper.COLUMN_IS_COMPLETE + "=1",
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

    public List<Theme> getLockscreenWallpaperThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, ThemeSQLiteHelper.COLUMN_HAS_LOCK_WALLPAPER + "='1'",
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

    public List<Theme> getRingtoneThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, ThemeSQLiteHelper.COLUMN_HAS_RINGTONE + "='1' OR " +
                ThemeSQLiteHelper.COLUMN_HAS_NOTIFICATION + "='1'",
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

    public List<Theme> getContactsThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, ThemeSQLiteHelper.COLUMN_HAS_CONTACTS + "='1'",
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

    public List<Theme> getDialerThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, ThemeSQLiteHelper.COLUMN_HAS_DIALER + "='1'",
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

    public List<Theme> getFontThemes() {
        List<Theme> themes = new ArrayList<Theme>();

        Cursor cursor = database.query(ThemeSQLiteHelper.TABLE_THEMES,
                allColumns, ThemeSQLiteHelper.COLUMN_HAS_FONT + "='1'",
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
        theme.setId(cursor.getLong(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_ID)));
        theme.setFileName(cursor.getString(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_FILE_NAME)));
        theme.setLastModified(Long.getLong(
                cursor.getString(cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_LAST_MODIFIED)), 0));
        theme.setTitle(cursor.getString(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_TITLE)));
        theme.setAuthor(cursor.getString(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_AUTHOR)));
        theme.setDesigner(cursor.getString(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_DESIGNER)));
        theme.setVersion(cursor.getString(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_VERSION)));
        theme.setUiVersion(cursor.getString(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_UI_VERSION)));
        theme.setThemePath(cursor.getString(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_THEME_PATH)));
        theme.setIsCosTheme(cursor.getInt(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_IS_COS_THEME)) == 1);
        theme.setIsDefaultTheme(cursor.getInt(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_IS_DEFAULT_THEME)) == 1);
        theme.setHasWallpaper(cursor.getInt(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_WALLPAPER)) == 1);
        theme.setHasIcons(cursor.getInt(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_ICONS)) == 1);
        theme.setHasLockscreenWallpaper(cursor.getInt(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_LOCK_WALLPAPER)) == 1);
        theme.setHasContacts(cursor.getInt(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_CONTACTS)) == 1);
        theme.setHasDialer(cursor.getInt(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_DIALER)) == 1);
        theme.setHasSystemUI(cursor.getInt(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_SYSTEMUI)) == 1);
        theme.setHasFramework(cursor.getInt(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_FRAMEWORK)) == 1);
        theme.setHasRingtone(cursor.getInt(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_RINGTONE)) == 1);
        theme.setHasNotification(cursor.getInt(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_NOTIFICATION)) == 1);
        theme.setHasBootanimation(cursor.getInt(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_BOOTANIMATION)) == 1);
        theme.setHasMms(cursor.getInt(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_MMS)) == 1);
        theme.setHasFont(cursor.getInt(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_HAS_FONT)) == 1);
        theme.setIsComplete(cursor.getInt(
                cursor.getColumnIndexOrThrow(ThemeSQLiteHelper.COLUMN_IS_COMPLETE)) == 1);
        return theme;
    }
}
