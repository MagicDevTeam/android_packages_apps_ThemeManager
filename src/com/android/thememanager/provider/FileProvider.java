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

package com.android.thememanager.provider;

import com.android.thememanager.Globals;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;

public class FileProvider extends ContentProvider {
    public static final String CONTENT = "content://com.android.thememanager/";
    public static final Uri CONTENT_URI = Uri
            .parse(CONTENT);
    public static final String CONTENT_BACKUP = "content://com.android.thememanager.backup/";
    public static final Uri CONTENT_BACKUP_URI = Uri
            .parse(CONTENT_BACKUP);
    private static final HashMap<String, String> MIME_TYPES = new HashMap<String, String>();

    static {
        MIME_TYPES.put(".ctz", "application/zip");
        MIME_TYPES.put(".CTZ", "application/zip");
        MIME_TYPES.put(".mtz", "application/zip");
        MIME_TYPES.put(".MTZ", "application/zip");
        MIME_TYPES.put(".mp3", "audio/mp3");
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        String path = uri.toString();

        for (String extension : MIME_TYPES.keySet()) {
            if (path.endsWith(extension)) {
                return (MIME_TYPES.get(extension));
            }
        }

        return null;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        File f = null;
        if (getType(uri).contains("mp3"))
            f = new File(Globals.CACHE_DIR, uri.getPath());
        else if (uri.toString().endsWith("default.ctz"))
            f = new File(Globals.SYSTEM_THEME_PATH, uri.getPath());
        else if (uri.toString().contains("backup"))
            f = new File(Globals.BACKUP_PATH, uri.getPath());
        else
            f = new File(Globals.DEFAULT_THEME_PATH, uri.getPath());

        if (f.exists()) {
            return (ParcelFileDescriptor.open(f,
                    ParcelFileDescriptor.MODE_READ_ONLY));
        }

        throw new FileNotFoundException(uri.getPath());
    }

    @Override
    public Cursor query(Uri url, String[] projection, String selection,
                        String[] selectionArgs, String sort) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
                      String[] whereArgs) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        throw new RuntimeException("Operation not supported");
    }

    static private void copy(InputStream in, File dst) throws IOException {
        FileOutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;

        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }
}
