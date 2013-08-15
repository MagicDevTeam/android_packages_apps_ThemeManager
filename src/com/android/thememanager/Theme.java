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

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class Theme {

    public static final int THEME_ELEMENT_TYPE_ICONS = 0;
    public static final int THEME_ELEMENT_TYPE_WALLPAPER = 1;
    public static final int THEME_ELEMENT_TYPE_SYSTEMUI = 2;
    public static final int THEME_ELEMENT_TYPE_FRAMEWORK = 3;
    public static final int THEME_ELEMENT_TYPE_CONTACTS = 4;
    public static final int THEME_ELEMENT_TYPE_RINGTONES = 5;
    public static final int THEME_ELEMENT_TYPE_BOOTANIMATION = 6;
    public static final int THEME_ELEMENT_TYPE_MMS = 7;
    public static final int THEME_ELEMENT_TYPE_FONT = 8;

    public static int[] sElementIcons = { android.R.drawable.ic_menu_view,
            android.R.drawable.ic_menu_gallery,
            android.R.drawable.ic_menu_sort_by_size,
            android.R.drawable.ic_menu_today,
            android.R.drawable.ic_menu_call,
            android.R.drawable.ic_menu_call,
            android.R.drawable.ic_menu_slideshow,
            android.R.drawable.ic_menu_send,
            android.R.drawable.ic_menu_sort_alphabetically };

    public static int[] sElementLabels = { R.string.mixer_icons_label,
            R.string.mixer_walllpaper_label,
            R.string.mixer_systemui_label,
            R.string.mixer_framework_label,
            R.string.mixer_contacts_label,
            R.string.mixer_ringtones_label,
            R.string.mixer_bootanimation_label,
            R.string.mixer_mms_label,
            R.string.mixer_font_label };

    private long id;
    private String fileName;
    private String title;
    private String author;
    private String designer;
    private String version;
    private String uiVersion;
    private String themePath;
    private boolean isCosTheme;
    private boolean isDefaultTheme;
    private boolean hasWallpaper;
    private boolean hasIcons;
    private boolean hasLockscreen;
    private boolean hasContacts;
    private boolean hasSystemUI;
    private boolean hasFramework;
    private boolean hasRingtone;
    private boolean hasNotification;
    private boolean hasBootanimation;
    private boolean hasMms;
    private boolean hasFont;
    private boolean isComplete;
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

    public String getUiVersion() {
        return uiVersion;
    }

    public void setUiVersion(String uiVersion) {
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

    public boolean getIsDefaultTheme() {
        return isDefaultTheme;
    }

    public void setIsDefaultTheme(boolean isDefaultTheme) {
        this.isDefaultTheme = isDefaultTheme;
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

    public boolean getHasContacts() {
        return hasContacts;
    }

    public void setHasContacts(boolean hasContacts) {
        this.hasContacts = hasContacts;
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

    public boolean getHasRingtone() {
        return hasRingtone;
    }

    public void setHasRingtone(boolean hasRingtone) {
        this.hasRingtone = hasRingtone;
    }

    public boolean getHasNotification() {
        return hasNotification;
    }

    public void setHasNotification(boolean hasNotification) {
        this.hasNotification = hasNotification;
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

    public boolean getHasFont() {
        return hasFont;
    }

    public void setHasFont(boolean hasFont) {
        this.hasFont = hasFont;
    }

    public boolean getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public static void showThemeDetails(Context context, Theme theme) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View content = inflater.inflate(R.layout.theme_info, null);
        ((TextView)content.findViewById(R.id.theme_name)).setText(theme.getTitle());
        ((TextView)content.findViewById(R.id.theme_version)).setText(theme.getVersion());
        ((TextView)content.findViewById(R.id.theme_author)).setText(theme.getAuthor());
        ((TextView)content.findViewById(R.id.theme_designer)).setText(theme.getDesigner());
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.theme_info_details))
                .setPositiveButton(context.getString(R.string.btn_ok), null)
                .setView(content)
                .setIcon(android.R.drawable.ic_menu_info_details)
                .create().show();
    }

    public static void showExtendedThemeDetails(Context context, Theme theme) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View content = inflater.inflate(R.layout.theme_info_extended, null);
        ((TextView)content.findViewById(R.id.theme_name)).setText(theme.getTitle());
        ((TextView)content.findViewById(R.id.theme_version)).setText(theme.getVersion());
        ((TextView)content.findViewById(R.id.theme_author)).setText(theme.getAuthor());
        ((TextView)content.findViewById(R.id.theme_designer)).setText(theme.getDesigner());
        ((CheckBox)content.findViewById(R.id.has_icons)).setChecked(theme.getHasIcons());
        ((CheckBox)content.findViewById(R.id.has_wallpaper)).setChecked(theme.getHasWallpaper());
        ((CheckBox)content.findViewById(R.id.has_systemui)).setChecked(theme.getHasSystemUI());
        ((CheckBox)content.findViewById(R.id.has_framework)).setChecked(theme.getHasFramework());
        ((CheckBox)content.findViewById(R.id.has_contacts)).setChecked(theme.getHasContacts());
        ((CheckBox)content.findViewById(R.id.has_ringtones)).setChecked(theme.getHasRingtone() || theme.getHasNotification());
        ((CheckBox)content.findViewById(R.id.has_bootani)).setChecked(theme.getHasBootanimation());
        ((CheckBox)content.findViewById(R.id.has_mms)).setChecked(theme.getHasMms());
        ((CheckBox)content.findViewById(R.id.has_fonts)).setChecked(theme.getHasFont());
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.theme_info_details))
                .setPositiveButton(context.getString(R.string.btn_ok), null)
                .setView(content)
                .setIcon(android.R.drawable.ic_menu_info_details)
                .create().show();
    }
}
