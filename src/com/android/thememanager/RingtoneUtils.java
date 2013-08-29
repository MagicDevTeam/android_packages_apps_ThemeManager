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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

public class RingtoneUtils {
    public static final String RINGTONE_NAME = "ringtone.mp3";
    public static final String NOTIFICATION_NAME = "notification.mp3";

    public static void setRingtone(Context context, String title, String author, boolean isNotification) {
        String dstFilePath;
        if (isNotification)
            dstFilePath = Globals.RINGTONES_PATH + File.separator + NOTIFICATION_NAME;
        else
            dstFilePath = Globals.RINGTONES_PATH + File.separator + RINGTONE_NAME;
        if (dstFilePath != null) {
            File f = new File(dstFilePath);
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, dstFilePath);
            values.put(MediaStore.MediaColumns.TITLE, title);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
            values.put(MediaStore.MediaColumns.SIZE, f.length());
            values.put(MediaStore.Audio.Media.ARTIST, author);
            values.put(MediaStore.Audio.Media.IS_RINGTONE, !isNotification);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, isNotification);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_MUSIC, false);

            Uri uri = MediaStore.Audio.Media.getContentUriForPath(f.getAbsolutePath());
            Uri newUri = null;
            Cursor c = context.getContentResolver().query(uri, new String[] {MediaStore.MediaColumns._ID},
                    MediaStore.MediaColumns.DATA + "='" + dstFilePath + "'", null, null);
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                long id = c.getLong(0);
                c.close();
                newUri = Uri.withAppendedPath(Uri.parse("content://media/internal/audio/media"), "" + id);
                context.getContentResolver().update(uri, values, MediaStore.MediaColumns._ID + "=" + id, null);
            }
            if (newUri == null)
                newUri = context.getContentResolver().insert(uri, values);
            try {
                if (!isNotification)
                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);
                else
                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, newUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
