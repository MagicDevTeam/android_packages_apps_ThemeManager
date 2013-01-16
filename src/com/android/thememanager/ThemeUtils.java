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
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    }

    public static List<Theme> getAllThemes(Context context) {
        ThemesDataSource dataSource = new ThemesDataSource(context);
        dataSource.open();
        List<Theme> themes = dataSource.getAllThemes();
        dataSource.close();
        return themes;
    }

    public static boolean addThemeEntryToDb(String themeId, String themePath, Context context) {
        try {
            ThemesDataSource dataSource = new ThemesDataSource(context);
            File file = new File(themePath);
            long lastModified = file.lastModified();
            dataSource.open();
            if (dataSource.entryExists(themeId) && !dataSource.entryIsOlder(themeId, lastModified)) {
                dataSource.close();
                return true;
            }

            ZipFile zip = new ZipFile(themePath);
            ZipEntry entry = zip.getEntry("description.xml");
            ThemeDetails details = getThemeDetails(zip.getInputStream(entry));

            Theme theme = new Theme();
            theme.setFileName(themeId);
            theme.setThemePath(themePath);
            theme.setTitle(details.title);
            theme.setAuthor(details.author);
            theme.setDesigner(details.designer);
            theme.setVersion(details.version);
            theme.setUiVersion(Long.parseLong(details.uiVersion));
            theme.setIsCosTheme(details.isCosTheme);
            theme.setHasWallpaper(zip.getEntry("wallpaper") != null);
            theme.setHasIcons(zip.getEntry("icons") != null);
            theme.setHasLockscreen(zip.getEntry("lockscreen") != null);
            theme.setHasSystemUI(zip.getEntry("com.android.systemui") != null);
            theme.setHasFramework(zip.getEntry("framework-res") != null);
            theme.setHasRingtones(zip.getEntry("ringtones") != null);
            theme.setHasBootanimation(zip.getEntry("boots") != null);
            theme.setHasMms(zip.getEntry("com.android.mms") != null);
            theme.setLastModified(lastModified);

            try {
                dataSource.createThemeEntry(theme);
            } catch (Exception e) {}
            dataSource.close();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public static void extractThemePreviews(ZipFile themeZip) {

    }

    public static InputStream getThemePreview(String themePath, String previewName) throws IOException {
        ZipFile zip = new ZipFile(themePath);
        ZipEntry entry = zip.getEntry("preview/" + previewName);
        if (entry == null)
            return null;

        return zip.getInputStream(entry);
    }

    public static ThemeDetails getThemeDetails(InputStream descriptionEntry) {
        ThemeDetails details = new ThemeDetails();
        XmlPullParser parser = Xml.newPullParser();

        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            if (descriptionEntry == null)
                return details;

            parser.setInput(descriptionEntry, null);
            //parser.nextTag();

            int eventType = parser.next();
            while(eventType != XmlPullParser.START_TAG && eventType != XmlPullParser.END_DOCUMENT)
                eventType = parser.next();
            if (eventType != XmlPullParser.START_TAG)
                throw new XmlPullParserException("No start tag found!");
            String str = parser.getName();
            if (parser.getName().equals("ChaOS-Theme"))
                details.isCosTheme = true;
            //parser.require(XmlPullParser.START_TAG, null, "MIUI-Theme");
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
        } catch (XmlPullParserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

            details = getThemeDetails(zip.getInputStream(entry));
            zip.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
