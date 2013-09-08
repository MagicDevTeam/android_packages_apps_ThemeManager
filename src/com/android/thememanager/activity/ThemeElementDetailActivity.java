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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.IThemeManagerService;
import android.os.Bundle;
import android.os.Handler;
import android.os.ServiceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.thememanager.FileUtils;
import com.android.thememanager.Globals;
import com.android.thememanager.PreviewHelper;
import com.android.thememanager.R;
import com.android.thememanager.SimpleDialogs;
import com.android.thememanager.Theme;
import com.android.thememanager.ThemeUtils;

import com.android.thememanager.provider.FileProvider;

import java.io.File;

public class ThemeElementDetailActivity extends DetailBaseActivity {
    private int mElementType = 0;
    private boolean mFontReboot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_element_detail);


        mTheme = ThemeUtils.getThemeEntryById(getIntent().getLongExtra("theme_id", -1), this);

        if (mTheme == null)
            finish();

        mHandler = new Handler();
        mElementType = getIntent().getIntExtra("type", 0);
        String themeName = FileUtils.stripPath(mTheme.getFileName());
        switch (mElementType) {
            case Theme.THEME_ELEMENT_TYPE_ICONS:
                mPreviewList = PreviewHelper.getIconPreviews(mTheme);
                break;
            case Theme.THEME_ELEMENT_TYPE_WALLPAPER:
                mPreviewList = PreviewHelper.getWallpaperPreviews(mTheme);
                break;
            case Theme.THEME_ELEMENT_TYPE_LOCK_WALLPAPER:
                mPreviewList = PreviewHelper.getLockWallpaperPreviews(mTheme);
                break;
            case Theme.THEME_ELEMENT_TYPE_SYSTEMUI:
                mPreviewList = PreviewHelper.getStatusbarPreviews(mTheme);
                break;
            case Theme.THEME_ELEMENT_TYPE_FRAMEWORK:
                mPreviewList = PreviewHelper.getLauncherPreviews(mTheme);
                break;
            case Theme.THEME_ELEMENT_TYPE_CONTACTS:
                mPreviewList = PreviewHelper.getContactsPreviews(mTheme);
                break;
            case Theme.THEME_ELEMENT_TYPE_DIALER:
                mPreviewList = PreviewHelper.getDialerPreviews(mTheme);
                break;
            case Theme.THEME_ELEMENT_TYPE_RINGTONES:
                mPreviewList = PreviewHelper.getContactsPreviews(mTheme);
                break;
            case Theme.THEME_ELEMENT_TYPE_BOOTANIMATION:
                mPreviewList = PreviewHelper.getBootanimationPreviews(mTheme);
                break;
            case Theme.THEME_ELEMENT_TYPE_MMS:
                mPreviewList = PreviewHelper.getMmsPreviews(mTheme);
                break;
            case Theme.THEME_ELEMENT_TYPE_FONT:
                mPreviewList = PreviewHelper.getFontsPreviews(mTheme);
                break;
        }

        ((TextView)findViewById(R.id.theme_name)).setText(mTheme.getTitle());

        setTitle(Theme.sElementLabels[mElementType]);
        setupPager();
        setupActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_theme_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_details:
                Theme.showThemeDetails(this, mTheme);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Globals.ACTION_THEME_APPLIED.equals(action)) {
                dismissDialog(DIALOG_PROGRESS);
            } else if (Globals.ACTION_THEME_NOT_APPLIED.equals(action)) {
                dismissDialog(DIALOG_PROGRESS);
                SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body,
                        ThemeElementDetailActivity.this);
            }
            unlockScreenOrientation();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // ugly hack to keep the dialog from reappearing when the app is restarted
        // due to a theme change.
        try {
            dismissDialog(DIALOG_PROGRESS);
            unlockScreenOrientation();
        } catch (Exception e) {}

        mAdapter.notifyDataSetChanged();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Globals.ACTION_THEME_APPLIED);
        filter.addAction(Globals.ACTION_THEME_NOT_APPLIED);
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    public void applyTheme(View view) {
        if (mElementType == Theme.THEME_ELEMENT_TYPE_FONT) {
            SimpleDialogs.displayYesNoDialog(getString(R.string.dlg_apply_font_and_reboot),
                    getString(R.string.dlg_apply_font_without_reboot),
                    getString(R.string.dlg_apply_font_title),
                    getString(R.string.dlg_apply_font_body),
                    this,
                    new SimpleDialogs.OnYesNoResponse() {
                        @Override
                        public void onYesNoResponse(boolean isYes) {
                            mFontReboot = isYes;
                            applyTheme(mTheme.getThemePath());
                        }
                    });
        } else {
            applyTheme(mTheme.getThemePath());
        }
    }

    private void applyTheme(String theme) {
        String themeFileName = FileUtils.stripPath(theme);
        IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
        try {
            switch (mElementType) {
                case Theme.THEME_ELEMENT_TYPE_ICONS:
                    ts.applyThemeIcons(FileProvider.CONTENT + themeFileName);
                    break;
                case Theme.THEME_ELEMENT_TYPE_WALLPAPER:
                    ts.applyThemeWallpaper(FileProvider.CONTENT + themeFileName);
                    break;
                case Theme.THEME_ELEMENT_TYPE_LOCK_WALLPAPER:
                    ts.applyThemeLockscreenWallpaper(FileProvider.CONTENT + themeFileName);
                    break;
                case Theme.THEME_ELEMENT_TYPE_SYSTEMUI:
                    ts.applyThemeSystemUI(FileProvider.CONTENT + themeFileName);
                    break;
                case Theme.THEME_ELEMENT_TYPE_FRAMEWORK:
                    ts.applyThemeFramework(FileProvider.CONTENT + themeFileName);
                    break;
                case Theme.THEME_ELEMENT_TYPE_CONTACTS:
                    ts.applyThemeContacts(FileProvider.CONTENT + themeFileName);
                    break;
                case Theme.THEME_ELEMENT_TYPE_DIALER:
                    ts.applyThemeDialer(FileProvider.CONTENT + themeFileName);
                    break;
                case Theme.THEME_ELEMENT_TYPE_RINGTONES:
                    ts.applyThemeRingtone(FileProvider.CONTENT + themeFileName);
                    break;
                case Theme.THEME_ELEMENT_TYPE_BOOTANIMATION:
                    ts.applyThemeBootanimation(FileProvider.CONTENT + themeFileName, false);
                    break;
                case Theme.THEME_ELEMENT_TYPE_MMS:
                    ts.applyThemeMms(FileProvider.CONTENT + themeFileName);
                    break;
                case Theme.THEME_ELEMENT_TYPE_FONT:
                    if (mFontReboot)
                        ts.applyThemeFontReboot(FileProvider.CONTENT + themeFileName);
                    else
                        ts.applyThemeFont(FileProvider.CONTENT + themeFileName);
                    break;
            }
            lockScreenOrientation();
            showDialog(DIALOG_PROGRESS);
        } catch (Exception e) {
            SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body,
                    ThemeElementDetailActivity.this);
        }
    }
}
