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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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

    public static void deleteFile(File file) {
        if (file.isDirectory())
            for (File f : file.listFiles())
                deleteFile(f);
        else
            file.delete();
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
     * Deletes a theme directory inside CACHE_DIR for the given theme
     * @param themeName theme to delete cache directory for
     */
    public static void deleteThemeCacheDir(String themeName) {
        File f = new File(Globals.CACHE_DIR + "/" + themeName);
        if (f.exists())
            deleteFile(f);
        f.delete();
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
     * Strips all path information and returns just the filename
     * @param filename complete path and filename
     * @return filename without any path information
     */
    public static String stripPath(String filename) {
        int index = filename.lastIndexOf('/');
        if (index > -1) {
            filename = filename.substring(index + 1);
        }

        return filename;
    }

    public static boolean installedThemeHasFonts() {
        File fontsDir = new File("/data/fonts");
        return fontsDir.exists() && fontsDir.list().length > 0;
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

    public static boolean isSymbolicLink(File f) throws IOException {
        return !f.getAbsolutePath().equals(f.getCanonicalPath());
    }

    public static void setDirPerms(File f) {
        try {
            if (isSymbolicLink(f))
                return;
        } catch (IOException e) {
            return;
        }

        if (!f.isDirectory())
            return;
        f.setReadable(true, false);
        f.setWritable(true, false);
        f.setExecutable(true, false);
    }

    public static void setFilePerms(File f) {
        try {
            if (isSymbolicLink(f))
                return;
        } catch (IOException e) {
            return;
        }
        f.setReadable(true, false);
        f.setWritable(true, false);
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

    public static boolean extractThemeWallpaper(String themeId, String themePath) {
        try {
            ZipFile zip = new ZipFile(themePath);
            ZipEntry ze = null;

            if (!themeCacheDirExists(themeId))
                createThemeCacheDir(themeId);

            ze = zip.getEntry("wallpaper/default_wallpaper.jpg");
            if (ze == null)
                ze = zip.getEntry("wallpaper/default_wallpaper.png");

            if (ze != null) {
                InputStream is = zip.getInputStream(ze);
                FileOutputStream  out = new FileOutputStream(Globals.CACHE_DIR + "/" +
                        themeId + "/default_wallpaper.jpg");
                BitmapFactory.decodeStream(is).compress(Bitmap.CompressFormat.JPEG,
                        50, out);
            }
            zip.close();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static boolean extractThemRingtones(String themeId, String themePath) {
        try {
            ZipFile zip = new ZipFile(themePath);
            ZipEntry ze = null;

            if(!cacheDirExists())
                createCacheDir();

            InputStream is = null;
            FileOutputStream out = null;
            ze = zip.getEntry("ringtones/ringtone.mp3");
            if (ze != null) {
                is = zip.getInputStream(ze);
                out = new FileOutputStream(Globals.CACHE_DIR + "/ringtone.mp3");
                copyInputStream(is, out);
            }
            ze = zip.getEntry("ringtones/notification.mp3");
            if (ze != null) {
                is = zip.getInputStream(ze);
                out = new FileOutputStream(Globals.CACHE_DIR + "/notification.mp3");
                copyInputStream(is, out);
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static Theme getThemeEntryById(long id, Context context) {
        Theme theme = null;

        ThemesDataSource dataSource = new ThemesDataSource(context);
        dataSource.open();
        theme = dataSource.getThemeById(id);
        dataSource.close();

        return theme;
    }

    public static void deleteTheme(Theme theme, Context context) {
        ThemesDataSource dataSource = new ThemesDataSource(context);
        dataSource.open();
        dataSource.deleteTheme(theme);
        dataSource.close();
        (new File(theme.getThemePath())).delete();
    }

    public static List<Theme> getAllThemes(Context context) {
        ThemesDataSource dataSource = new ThemesDataSource(context);
        dataSource.open();
        List<Theme> themes = dataSource.getAllThemes();
        dataSource.close();
        return themes;
    }

    public static boolean addThemeEntryToDb(String themeId, String themePath,
            Context context, boolean isDefaultTheme) {
        try {
            ThemesDataSource dataSource = new ThemesDataSource(context);
            File file = new File(themePath);
            long lastModified = file.lastModified();
            dataSource.open();
            if (dataSource.entryExists(themeId)) {
                if(!dataSource.entryIsOlder(themeId, lastModified)) {
                    dataSource.close();
                    return true;
                } else
                    deleteThemeCacheDir(themeId);
            }

            ZipFile zip = new ZipFile(themePath);
            ZipEntry entry = zip.getEntry("description.xml");
            ThemeDetails details = null;
            try {
                details = getThemeDetails(zip.getInputStream(entry));
            } catch (Exception e) {
                return false;
            }

            Theme theme = new Theme();
            theme.setFileName(themeId);
            theme.setThemePath(themePath);
            theme.setTitle(details.title);
            theme.setAuthor(details.author);
            theme.setDesigner(details.designer);
            theme.setVersion(details.version);
            theme.setUiVersion(details.uiVersion);
            theme.setIsCosTheme(details.isCosTheme);
            theme.setIsDefaultTheme(isDefaultTheme);
            theme.setHasWallpaper(zip.getEntry("wallpaper/default_wallpaper.jpg") != null ||
                    zip.getEntry("wallpaper/default_wallpaper.png") != null);
            theme.setHasIcons(zip.getEntry("icons") != null);
            theme.setHasLockscreen(zip.getEntry("lockscreen") != null);
            theme.setHasContacts(zip.getEntry("com.android.contacts") != null);
            theme.setHasSystemUI(zip.getEntry("com.android.systemui") != null);
            theme.setHasFramework(zip.getEntry("framework-res") != null);
            theme.setHasRingtone(zip.getEntry("ringtones/ringtone.mp3") != null);
            theme.setHasNotification(zip.getEntry("ringtones/notification.mp3") != null);
            theme.setHasBootanimation(zip.getEntry("boots") != null);
            theme.setHasMms(zip.getEntry("com.android.mms") != null);
            theme.setHasFont(zip.getEntry("fonts") != null);
            theme.setIsComplete(theme.getHasSystemUI() && theme.getHasFramework() &&
                    theme.getHasMms() && theme.getHasContacts());
            theme.setLastModified(lastModified);

            try {
                dataSource.createThemeEntry(theme);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dataSource.close();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public static ThemeDetails getThemeDetails(InputStream descriptionEntry)
            throws XmlPullParserException, IOException {
        ThemeDetails details = new ThemeDetails();
        XmlPullParser parser = Xml.newPullParser();

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        if (descriptionEntry == null)
            return details;

        parser.setInput(descriptionEntry, null);

        int eventType = parser.next();
        while(eventType != XmlPullParser.START_TAG && eventType != XmlPullParser.END_DOCUMENT)
            eventType = parser.next();
        if (eventType != XmlPullParser.START_TAG)
            throw new XmlPullParserException("No start tag found!");
        String str = parser.getName();
        if (parser.getName().equals("ChaOS-Theme"))
            details.isCosTheme = true;
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if ("title".equals(name)) {
                if (parser.next() == XmlPullParser.TEXT) {
                    details.title = parser.getText();
                    parser.nextTag();
                }
            } else if ("designer".equals(name)) {
                if (parser.next() == XmlPullParser.TEXT) {
                    details.designer = parser.getText();
                    parser.nextTag();
                }
            } else if ("author".equals(name)) {
                if (parser.next() == XmlPullParser.TEXT) {
                    details.author = parser.getText();
                    parser.nextTag();
                }
            } else if ("version".equals(name)) {
                if (parser.next() == XmlPullParser.TEXT) {
                    details.version = parser.getText();
                    parser.nextTag();
                }
            } else if ("uiVersion".equals(name)) {
                if (parser.next() == XmlPullParser.TEXT) {
                    details.uiVersion = parser.getText();
                    parser.nextTag();
                }
            }
        }

        return details;
    }

    public static ThemeDetails getThemeDetails(String themePath) {
        ThemeDetails details = new ThemeDetails();

        try {
            ZipFile zip = new ZipFile(themePath);
            ZipEntry entry = zip.getEntry("description.xml");
            if (entry == null)
                return details;

            try {
                details = getThemeDetails(zip.getInputStream(entry));
            } catch (Exception e) {
                return null;
            }
            zip.close();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }

        return details;
    }

    public static class ThemeDetails {
        public String title;
        public String designer;
        public String author;
        public String version;
        public String uiVersion;
        public boolean isCosTheme;
    }
}
