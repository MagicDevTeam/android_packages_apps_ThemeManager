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
import java.util.Collections;
import java.util.List;

public class PreviewHelper {
    public static final String PREVIEW_PREFIX = "preview_";
    public static final String PREVIEW_LAUNCHER_PREFIX = "preview_launcher_";
    public static final String PREVIEW_ICONS_PREFIX = "preview_icons_";
    public static final String PREVIEW_STATUSBAR_PREFIX = "preview_statusbar_";
    public static final String PREVIEW_MMS_PREFIX = "preview_mms_";
    public static final String PREVIEW_CONTACTS_PREFIX = "preview_contact_";
    public static final String PREVIEW_DIALER_PREFIX = "preview_dialer_";
    public static final String PREVIEW_BOOTANIMATION_PREFIX = "preview_animation_";
    public static final String PREVIEW_FONTS_PREFIX = "preview_fonts_";
    public static final String PREVIEW_WALLPAPER_PREFIX = "default_wallpaper";
    public static final String PREVIEW_LOCK_WALLPAPER_PREFIX = "default_lock_wallpaper";

    public static String[] getPreviews(Theme theme, String prefix) {
        String[] completeList = getAllPreviews(theme);
        List<String> list = new ArrayList<String>();
        for (String item : completeList) {
            if (item.contains(prefix))
                list.add(item);
        }
        Collections.sort(list);
        return list.toArray(new String[0]);
    }

    public static String[] getAllPreviews(Theme theme) {
        return theme.getPreviewsList().split("\\|");
    }

    public static String[] getLauncherPreviews(Theme theme) {
        return getPreviews(theme, PREVIEW_LAUNCHER_PREFIX);
    }

    public static String[] getIconPreviews(Theme theme) {
        return getPreviews(theme, PREVIEW_ICONS_PREFIX);
    }

    public static String[] getStatusbarPreviews(Theme theme) {
        return getPreviews(theme, PREVIEW_STATUSBAR_PREFIX);
    }

    public static String[] getMmsPreviews(Theme theme) {
        return getPreviews(theme, PREVIEW_MMS_PREFIX);
    }

    public static String[] getContactsPreviews(Theme theme) {
        return getPreviews(theme, PREVIEW_CONTACTS_PREFIX);
    }

    public static String[] getDialerPreviews(Theme theme) {
        return getPreviews(theme, PREVIEW_DIALER_PREFIX);
    }

    public static String[] getBootanimationPreviews(Theme theme) {
        return getPreviews(theme, PREVIEW_BOOTANIMATION_PREFIX);
    }

    public static String[] getFontsPreviews(Theme theme) {
        return getPreviews(theme, PREVIEW_FONTS_PREFIX);
    }

    public static String[] getWallpaperPreviews(Theme theme) {
        return getPreviews(theme, PREVIEW_WALLPAPER_PREFIX);
    }

    public static String[] getLockWallpaperPreviews(Theme theme) {
        return getPreviews(theme, PREVIEW_LOCK_WALLPAPER_PREFIX);
    }
}
