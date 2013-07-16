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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.IThemeManagerService;
import android.os.Bundle;
import android.os.ServiceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.thememanager.Globals;
import com.android.thememanager.R;
import com.android.thememanager.SimpleDialogs;
import com.android.thememanager.Theme;
import com.android.thememanager.ThemeUtils;
import com.android.thememanager.provider.FileProvider;
import com.android.thememanager.widget.BootanimationImageView;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ThemeBootanimationDetailActivity extends Activity {
    private static final String TAG = "ThemeManager";

    private static final int DIALOG_PROGRESS = 0;

    private BootanimationImageView mPreview = null;
    private ProgressDialog mProgressDialog;
    private int mElementType = 0;
    private Theme mTheme = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation_detail);


        mTheme = ThemeUtils.getThemeEntryById(getIntent().getLongExtra("theme_id", -1), this);

        if (mTheme == null)
            finish();
        mElementType = getIntent().getIntExtra("type", 0);
        ((TextView)findViewById(R.id.theme_name)).setText(mTheme.getTitle());

        setTitle(Theme.sElementLabels[mElementType]);

        mPreview = (BootanimationImageView) findViewById(R.id.preview);
        try {
            extractAnimation(mTheme.getThemePath());
            mPreview.LoadAnimation(Globals.CACHE_DIR + "/bootanimation.zip");
        } catch (IOException e) {
        }

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void extractAnimation(String path) throws IOException {
        ZipFile zip = new ZipFile(path);
        ZipEntry entry = zip.getEntry("boots/bootanimation.zip");
        byte[] buffer = new byte[1024];
        FileOutputStream out = new FileOutputStream(Globals.CACHE_DIR + "/bootanimation.zip");
        copyInputStream(zip.getInputStream(entry), out);
        out.close();
        zip.close();
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
                        ThemeBootanimationDetailActivity.this);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ugly hack to keep the dialog from reappearing when the app is restarted
        // due to a theme change.
        try {
            dismissDialog(DIALOG_PROGRESS);
        } catch (Exception e) {}

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void applyTheme(View view) {
        displayBootAnimationFontDialog();
    }

    private void displayBootAnimationFontDialog() {
        SimpleDialogs.displayYesNoDialog(getString(R.string.dlg_scale_boot_with_scaling),
                getString(R.string.dlg_scale_boot_no_scaling),
                getString(R.string.dlg_scale_boot_title),
                getString(R.string.dlg_scale_boot_body),
                this,
                new SimpleDialogs.OnYesNoResponse() {
                    @Override
                    public void onYesNoResponse(boolean isYes) {
                        try{
                            IThemeManagerService ts = IThemeManagerService.Stub.asInterface(
                                    ServiceManager.getService("ThemeService"));
                            ts.applyThemeBootanimation(FileProvider.CONTENT +
                                    ThemeUtils.stripPath(mTheme.getThemePath()), isYes);
                            showDialog(DIALOG_PROGRESS);
                        } catch (Exception e) {
                            SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body,
                                    ThemeBootanimationDetailActivity.this);
                        }
                    }
                });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getResources().getText(R.string.applying_theme));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false );
                mProgressDialog.setProgress(0);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    /**
     * Simple copy routine given an input stream and an output stream
     */
    private void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        out.close();
    }
}
