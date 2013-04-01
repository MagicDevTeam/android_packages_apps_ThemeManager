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

package com.android.thememanager.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.IThemeManagerService;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.thememanager.Globals;
import com.android.thememanager.R;
import com.android.thememanager.SimpleDialogs;
import com.android.thememanager.Theme;
import com.android.thememanager.ThemeUtils;
import com.android.thememanager.provider.FileProvider;

import java.io.File;
import java.io.IOException;

public class ThemeRingtoneDetailActivity extends Activity
        implements MediaPlayer.OnPreparedListener {
    private static final String RINGTONE_NAME = "ringtone.mp3";
    private static final String NOTIFICATION_NAME = "notification.mp3";

    private int mElementType = 0;
    private Theme mTheme = null;
    private LinearLayout mRingtoneLayout;
    private LinearLayout mNotificationLayout;
    private Button mPlayRingtone;
    private Button mPlayNotification;
    private Button mSetRingtone;
    private Button mSetNotification;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ringtone_layout);

        mTheme = ThemeUtils.getThemeEntryById(getIntent().getLongExtra("theme_id", -1), this);

        if (mTheme == null)
            finish();
        mElementType = getIntent().getIntExtra("type", 0);
        if (mElementType != Theme.THEME_ELEMENT_TYPE_RINGTONES)
            finish();
        setTitle(mTheme.getTitle());

        mRingtoneLayout = (LinearLayout) findViewById(R.id.ringtone_layout);
        mPlayRingtone = (Button) mRingtoneLayout.findViewById(R.id.play_ringtone);
        mSetRingtone = (Button) mRingtoneLayout.findViewById(R.id.set_ringtone);

        mNotificationLayout = (LinearLayout) findViewById(R.id.notification_layout);
        mPlayNotification= (Button) mNotificationLayout.findViewById(R.id.play_notification);
        mSetNotification= (Button) mNotificationLayout.findViewById(R.id.set_notification);

        if (!mTheme.getHasRingtone())
            mRingtoneLayout.setVisibility(View.GONE);
        if (!mTheme.getHasNotification())
            mNotificationLayout.setVisibility(View.GONE);

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                ThemeUtils.extractThemRingtones(ThemeUtils.stripExtension(mTheme.getFileName()),
                        mTheme.getThemePath());
            }
        });

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Globals.ACTION_THEME_APPLIED.equals(action)) {
            } else if (Globals.ACTION_THEME_NOT_APPLIED.equals(action)) {
                SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body,
                        ThemeRingtoneDetailActivity.this);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Globals.ACTION_THEME_APPLIED);
        filter.addAction(Globals.ACTION_THEME_NOT_APPLIED);
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.stop();
        unregisterReceiver(mBroadcastReceiver);
    }

    public void onClick(View v) {
        IThemeManagerService ts = null;
        if (v == mPlayRingtone) {
            playRingtone(Globals.CACHE_DIR + "/" + RINGTONE_NAME);
        } else if (v == mPlayNotification) {
            playRingtone(Globals.CACHE_DIR + "/" + NOTIFICATION_NAME);
        } else if (v == mSetRingtone) {
            ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
            try {
                ts.applyThemeRingtone(FileProvider.CONTENT + RINGTONE_NAME);
                setRingtone(false);
            } catch (RemoteException re) {
                SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body,
                        ThemeRingtoneDetailActivity.this);
            }
        } else if (v == mSetNotification) {
            ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
            try {
                ts.applyThemeRingtone(FileProvider.CONTENT + NOTIFICATION_NAME);
                setRingtone(false);
            } catch (RemoteException re) {
                SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body,
                        ThemeRingtoneDetailActivity.this);
            }
            setRingtone(true);
        }
    }

    private void playRingtone(String path) {
        try {
            if (mMediaPlayer.isPlaying())
                mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
        } catch (IOException e) {
        }
    }

    private void setRingtone(boolean isNotification) {
        String dstFilePath;
        if (isNotification)
            dstFilePath = "/data/system/theme/" + NOTIFICATION_NAME;
        else
            dstFilePath = "/data/system/theme/" + RINGTONE_NAME;
        if (dstFilePath != null) {
            File f = new File(dstFilePath);
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, dstFilePath);
            values.put(MediaStore.MediaColumns.TITLE, mTheme.getTitle());
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
            values.put(MediaStore.MediaColumns.SIZE, f.length());
            values.put(MediaStore.Audio.Media.ARTIST,mTheme.getAuthor());
            values.put(MediaStore.Audio.Media.IS_RINGTONE, !isNotification);
            values.put(MediaStore.Audio.Media.IS_NOTIFICATION, isNotification);
            values.put(MediaStore.Audio.Media.IS_ALARM, false);
            values.put(MediaStore.Audio.Media.IS_MUSIC, false);

            Uri uri = MediaStore.Audio.Media.getContentUriForPath(f.getAbsolutePath());
            Uri newUri = null;
            Cursor c = getContentResolver().query(uri, new String[] {MediaStore.MediaColumns._ID},
                    MediaStore.MediaColumns.DATA + "='" + dstFilePath + "'", null, null);
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                long id = c.getLong(0);
                c.close();
                newUri = Uri.withAppendedPath(Uri.parse("content://media/internal/audio/media"), "" + id);
                getContentResolver().update(uri, values, MediaStore.MediaColumns._ID + "=" + id, null);
            }
            if (newUri == null)
                newUri = getContentResolver().insert(uri, values);
            try {
                if (!isNotification)
                    RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE, newUri);
                else
                    RingtoneManager.setActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION, newUri);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mMediaPlayer.start();
    }
}
