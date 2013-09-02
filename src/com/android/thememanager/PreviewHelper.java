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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

public class PreviewHelper {
    public static final String PREVIEW_PREFIX = "preview_";
    public static final String PREVIEW_LAUNCHER_PREFIX = "preview_launcher_";
    public static final String PREVIEW_ICONS_PREFIX = "preview_icons_";
    public static final String PREVIEW_STATUSBAR_PREFIX = "preview_statusbar_";
    public static final String PREVIEW_MMS_PREFIX = "preview_mms_";
    public static final String PREVIEW_CONTACTS_PREFIX = "preview_contact_";
    public static final String PREVIEW_DIALER_PREFIX = "preview_dialer_";
    public static final String PREVIEW_BOOTANIMATION_PREFIX = "preview_animation_";
    public static final String PREVIEW_LOCKSCREEN_PREFIX = "preview_lockscreen_";
    public static final String PREVIEW_FONTS_PREFIX = "preview_fonts_";
    public static final String PREVIEW_WALLPAPER_PREFIX = "default_wallpaper";
    public static final String PREVIEW_LOCK_WALLPAPER_PREFIX = "default_lock_wallpaper";

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

    static FilenameFilter mDialerPreviewsFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            if (s.toLowerCase().contains(PREVIEW_DIALER_PREFIX) &&
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

    static FilenameFilter mFontsPreviewsFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            if (s.toLowerCase().contains(PREVIEW_FONTS_PREFIX) &&
                    s.toLowerCase().endsWith(".png"))
                return true;
            else
                return false;
        }
    };

    static FilenameFilter mLockscreenPreviewsFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            if (s.toLowerCase().contains(PREVIEW_LOCKSCREEN_PREFIX) &&
                    s.toLowerCase().endsWith(".png"))
                return true;
            else
                return false;
        }
    };

    static FilenameFilter mWallpaperPreviewsFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            if (s.toLowerCase().contains(PREVIEW_WALLPAPER_PREFIX) &&
                    s.toLowerCase().endsWith(".jpg"))
                return true;
            else
                return false;
        }
    };

    static FilenameFilter mLockWallpaperPreviewsFilter = new FilenameFilter() {
        @Override
        public boolean accept(File file, String s) {
            if (s.toLowerCase().contains(PREVIEW_LOCK_WALLPAPER_PREFIX) &&
                    s.toLowerCase().endsWith(".jpg"))
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

        if (dirList != null)
            Arrays.sort(dirList);

        return dirList;
    }

    public static String[] getIconPreviews(String path) {
        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(mIconPreviewsFilter);

        if (dirList != null)
            Arrays.sort(dirList);

        return dirList;
    }

    public static String[] getStatusbarPreviews(String path) {
        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(mStatusbarPreviewsFilter);

        if (dirList != null)
            Arrays.sort(dirList);

        return dirList;
    }

    public static String[] getMmsPreviews(String path) {
        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(mMmsPreviewsFilter);

        if (dirList != null)
            Arrays.sort(dirList);

        return dirList;
    }

    public static String[] getContactsPreviews(String path) {
        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(mContactsPreviewsFilter);

        if (dirList != null)
            Arrays.sort(dirList);

        return dirList;
    }

    public static String[] getDialerPreviews(String path) {
        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(mDialerPreviewsFilter);

        if (dirList != null)
            Arrays.sort(dirList);

        return dirList;
    }

    public static String[] getBootanimationPreviews(String path) {
        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(mBootanimationPreviewsFilter);

        if (dirList != null)
            Arrays.sort(dirList);

        return dirList;
    }

    public static String[] getFontsPreviews(String path) {
        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(mFontsPreviewsFilter);

        if (dirList != null)
            Arrays.sort(dirList);

        return dirList;
    }

    public static String[] getWallpaperPreviews(String path) {
        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(mWallpaperPreviewsFilter);

        if (dirList != null)
            Arrays.sort(dirList);

        return dirList;
    }

    public static String[] getLockWallpaperPreviews(String path) {
        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(mLockWallpaperPreviewsFilter);

        if (dirList != null)
            Arrays.sort(dirList);

        return dirList;
    }
}
