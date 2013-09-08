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
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.thememanager.FileUtils;
import com.android.thememanager.Globals;
import com.android.thememanager.PreviewHelper;
import com.android.thememanager.R;
import com.android.thememanager.RingtoneUtils;
import com.android.thememanager.SimpleDialogs;
import com.android.thememanager.Theme;
import com.android.thememanager.ThemeUtils;
import com.android.thememanager.provider.FileProvider;

import java.util.ArrayList;
import java.util.List;

import org.chameleonos.support.widget.SlidingUpPanelLayout;

import static cos.content.res.ThemeResources.BOOTANI_NAME;
import static cos.content.res.ThemeResources.CHAOS_FRAMEWORK_NAME;
import static cos.content.res.ThemeResources.CONTACTS_PACKAGE;
import static cos.content.res.ThemeResources.DIALER_PACKAGE;
import static cos.content.res.ThemeResources.FONTS_NAME;
import static cos.content.res.ThemeResources.ICONS_NAME;
import static cos.content.res.ThemeResources.MMS_PACKAGE;
import static cos.content.res.ThemeResources.RINGTONES_NAME;
import static cos.content.res.ThemeResources.SYSTEMUI_PACKAGE;
import static cos.content.res.ThemeResources.WALLPAPER_NAME;
import static cos.content.res.ThemeResources.LOCKSCREEN_WALLPAPER_NAME;

public class ThemeDetailActivity extends DetailBaseActivity implements SlidingUpPanelLayout.PanelSlideListener {
    private SlidingUpPanelLayout mSlidingPanel;
    private CheckBox mRemoveExistingThemeCheckBox;
    private boolean mRemoveExistingTheme = false;

    private List<String> mExcludedItemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_theme_detail);

        mTheme = ThemeUtils.getThemeEntryById(getIntent().getLongExtra("theme_id", -1), this);
        mExcludedItemsList = new ArrayList<String>();

        if (mTheme == null)
            finish();

        mHandler = new Handler();

        mPreviewList = PreviewHelper.getAllPreviews(mTheme);

        ((TextView)findViewById(R.id.theme_name)).setText(mTheme.getTitle());

        setupPager();
        setupSlidingPanel();
        setupActionBar();
    }

    private void setupSlidingPanel() {
        mSlidingPanel = (SlidingUpPanelLayout) findViewById(R.id.sliding_panel);
        final View dragView = findViewById(R.id.drag_view);
        mSlidingPanel.setDragView(dragView);
        mSlidingPanel.setShadowDrawable(R.drawable.panel_shadow_holo_dark);
        mSlidingPanel.setPanelSlideListener(this);
        ViewTreeObserver observer = dragView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mSlidingPanel.setPanelHeight(dragView.getHeight());
            }
        });
        initItemsChecklist();
    }

    private void initItemsChecklist() {
        initChecklistItem(R.id.has_icons, mTheme.getHasIcons());
        initChecklistItem(R.id.has_wallpaper, mTheme.getHasWallpaper());
        initChecklistItem(R.id.has_lockscreen_wallpaper, mTheme.getHasLockscreenWallpaper());
        initChecklistItem(R.id.has_systemui, mTheme.getHasSystemUI());
        initChecklistItem(R.id.has_framework, mTheme.getHasFramework());
        initChecklistItem(R.id.has_contacts, mTheme.getHasContacts());
        initChecklistItem(R.id.has_dialer, mTheme.getHasDialer());
        initChecklistItem(R.id.has_ringtones, mTheme.getHasRingtone() || mTheme.getHasNotification());
        initChecklistItem(R.id.has_bootani, mTheme.getHasBootanimation());
        initChecklistItem(R.id.has_mms, mTheme.getHasMms());
        initChecklistItem(R.id.has_fonts, mTheme.getHasFont());
        initChecklistItem(R.id.has_third_party, true);

        mRemoveExistingThemeCheckBox = (CheckBox) findViewById(R.id.remove_existing_theme);
        mRemoveExistingThemeCheckBox.setOnCheckedChangeListener(mChecklistItemChanged);
        if (mTheme.getIsComplete()) {
            mRemoveExistingThemeCheckBox.setChecked(true);
        }
    }

    CompoundButton.OnCheckedChangeListener mChecklistItemChanged = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            switch (id) {
                case R.id.has_icons:
                    if (isChecked) {
                        if (mExcludedItemsList.contains(ICONS_NAME))
                            mExcludedItemsList.remove(ICONS_NAME);
                    } else {
                        if (!mExcludedItemsList.contains(ICONS_NAME))
                            mExcludedItemsList.add(ICONS_NAME);
                        mRemoveExistingThemeCheckBox.setChecked(false);
                    }
                    break;
                case R.id.has_wallpaper:
                    if (isChecked) {
                        if (mExcludedItemsList.contains(WALLPAPER_NAME))
                            mExcludedItemsList.remove(WALLPAPER_NAME);
                    } else {
                        if (!mExcludedItemsList.contains(WALLPAPER_NAME))
                            mExcludedItemsList.add(WALLPAPER_NAME);
                        mRemoveExistingThemeCheckBox.setChecked(false);
                    }
                    break;
                case R.id.has_lockscreen_wallpaper:
                    if (isChecked) {
                        if (mExcludedItemsList.contains(LOCKSCREEN_WALLPAPER_NAME))
                            mExcludedItemsList.remove(LOCKSCREEN_WALLPAPER_NAME);
                    } else {
                        if (!mExcludedItemsList.contains(LOCKSCREEN_WALLPAPER_NAME))
                            mExcludedItemsList.add(LOCKSCREEN_WALLPAPER_NAME);
                        mRemoveExistingThemeCheckBox.setChecked(false);
                    }
                    break;
                case R.id.has_systemui:
                    if (isChecked) {
                        if (mExcludedItemsList.contains(SYSTEMUI_PACKAGE))
                            mExcludedItemsList.remove(SYSTEMUI_PACKAGE);
                    } else {
                        if (!mExcludedItemsList.contains(SYSTEMUI_PACKAGE))
                            mExcludedItemsList.add(SYSTEMUI_PACKAGE);
                        mRemoveExistingThemeCheckBox.setChecked(false);
                    }
                    break;
                case R.id.has_framework:
                    if (isChecked) {
                        if (mExcludedItemsList.contains(CHAOS_FRAMEWORK_NAME))
                            mExcludedItemsList.remove(CHAOS_FRAMEWORK_NAME);
                    } else {
                        if (!mExcludedItemsList.contains(CHAOS_FRAMEWORK_NAME))
                            mExcludedItemsList.add(CHAOS_FRAMEWORK_NAME);
                        mRemoveExistingThemeCheckBox.setChecked(false);
                    }
                    break;
                case R.id.has_contacts:
                    if (isChecked) {
                        if (mExcludedItemsList.contains(CONTACTS_PACKAGE))
                            mExcludedItemsList.remove(CONTACTS_PACKAGE);
                    } else {
                        if (!mExcludedItemsList.contains(CONTACTS_PACKAGE))
                            mExcludedItemsList.add(CONTACTS_PACKAGE);
                        mRemoveExistingThemeCheckBox.setChecked(false);
                    }
                    break;
                case R.id.has_dialer:
                    if (isChecked) {
                        if (mExcludedItemsList.contains(DIALER_PACKAGE))
                            mExcludedItemsList.remove(DIALER_PACKAGE);
                    } else {
                        if (!mExcludedItemsList.contains(DIALER_PACKAGE))
                            mExcludedItemsList.add(DIALER_PACKAGE);
                        mRemoveExistingThemeCheckBox.setChecked(false);
                    }
                    break;
                case R.id.has_ringtones:
                    if (isChecked) {
                        if (mExcludedItemsList.contains(RINGTONES_NAME))
                            mExcludedItemsList.remove(RINGTONES_NAME);
                    } else {
                        if (!mExcludedItemsList.contains(RINGTONES_NAME))
                            mExcludedItemsList.add(RINGTONES_NAME);
                        mRemoveExistingThemeCheckBox.setChecked(false);
                    }
                    break;
                case R.id.has_bootani:
                    if (isChecked) {
                        if (mExcludedItemsList.contains(BOOTANI_NAME))
                            mExcludedItemsList.remove(BOOTANI_NAME);
                    } else {
                        if (!mExcludedItemsList.contains(BOOTANI_NAME))
                            mExcludedItemsList.add(BOOTANI_NAME);
                        mRemoveExistingThemeCheckBox.setChecked(false);
                    }
                    break;
                case R.id.has_mms:
                    if (isChecked) {
                        if (mExcludedItemsList.contains(MMS_PACKAGE))
                            mExcludedItemsList.remove(MMS_PACKAGE);
                    } else {
                        if (!mExcludedItemsList.contains(MMS_PACKAGE))
                            mExcludedItemsList.add(MMS_PACKAGE);
                        mRemoveExistingThemeCheckBox.setChecked(false);
                    }
                    break;
                case R.id.has_fonts:
                    if (isChecked) {
                        if (mExcludedItemsList.contains(FONTS_NAME))
                            mExcludedItemsList.remove(FONTS_NAME);
                    } else {
                        if (!mExcludedItemsList.contains(FONTS_NAME))
                            mExcludedItemsList.add(FONTS_NAME);
                        mRemoveExistingThemeCheckBox.setChecked(false);
                    }
                    break;
                case R.id.has_third_party:
                    if (isChecked) {
                        if (mExcludedItemsList.contains("third_party"))
                            mExcludedItemsList.remove("third_party");
                    } else {
                        if (!mExcludedItemsList.contains("third_party"))
                            mExcludedItemsList.add("third_party");
                        mRemoveExistingThemeCheckBox.setChecked(false);
                    }
                    break;
                case R.id.remove_existing_theme:
                    mRemoveExistingTheme = isChecked;
                    break;
            }
        }
    };

    private void initChecklistItem(CheckBox item, boolean isAvailable) {
        if (isAvailable) {
            item.setChecked(true);
            item.setOnCheckedChangeListener(mChecklistItemChanged);
        } else
            item.setEnabled(false);
    }

    private void initChecklistItem(int itemId, boolean isAvailable) {
        CheckBox item = (CheckBox) findViewById(itemId);
        if (item == null)
            return;
        initChecklistItem(item, isAvailable);
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
                Theme.showExtendedThemeDetails(this, mTheme);
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
            dismissDialog(DIALOG_PROGRESS);
            if (Globals.ACTION_THEME_APPLIED.equals(action)) {
                if (!mExcludedItemsList.contains(RINGTONES_NAME)) {
                    if (mTheme.getHasRingtone())
                        RingtoneUtils.setRingtone(ThemeDetailActivity.this, mTheme.getTitle(),
                                mTheme.getAuthor(), false);
                    if (mTheme.getHasNotification())
                        RingtoneUtils.setRingtone(ThemeDetailActivity.this, mTheme.getTitle(),
                                mTheme.getAuthor(), true);
                }
            } else if (Globals.ACTION_THEME_NOT_APPLIED.equals(action)) {
                SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body,
                        ThemeDetailActivity.this);
            }
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
        if (mTheme.getHasFont() && !mExcludedItemsList.contains(FONTS_NAME)) {
            displayThemeFontDialog();
        } else if (mTheme.getHasBootanimation() && !mExcludedItemsList.contains(BOOTANI_NAME)) {
            displayBootAnimationFontDialog(false);
        } else {
            applyTheme(mTheme.getThemePath(), false, false, mRemoveExistingTheme);
        }
    }

    private void displayThemeFontDialog() {
        SimpleDialogs.displayYesNoDialog(getString(R.string.dlg_apply_theme_with_font_and_reboot),
                getString(R.string.dlg_apply_theme_with_font_without_reboot),
                getString(R.string.dlg_apply_theme_with_font_title),
                getString(R.string.dlg_apply_theme_with_font_body),
                this,
                new SimpleDialogs.OnYesNoResponse() {
                    @Override
                    public void onYesNoResponse(boolean isYes) {
                        if (mTheme.getHasBootanimation())
                            displayBootAnimationFontDialog(isYes);
                        else
                            applyTheme(mTheme.getThemePath(), false, isYes, mRemoveExistingTheme);
                    }
                });
    }

    private void displayBootAnimationFontDialog(final boolean applyFont) {
        SimpleDialogs.displayYesNoDialog(getString(R.string.dlg_scale_boot_with_scaling),
                getString(R.string.dlg_scale_boot_no_scaling),
                getString(R.string.dlg_scale_boot_title),
                getString(R.string.dlg_scale_boot_body),
                this,
                new SimpleDialogs.OnYesNoResponse() {
                    @Override
                    public void onYesNoResponse(boolean isYes) {
                        applyTheme(mTheme.getThemePath(), applyFont, isYes, mRemoveExistingTheme);
                    }
                });
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
    }

    @Override
    public void onPanelCollapsed(View panel) {
        ((ImageView)panel.findViewById(R.id.drag_view)).setImageResource(R.drawable.ic_slide_up);
    }

    @Override
    public void onPanelExpanded(View panel) {
        ((ImageView)panel.findViewById(R.id.drag_view)).setImageResource(R.drawable.ic_slide_down);
    }

    private void applyTheme(String theme, boolean applyFont, boolean scaleBoot, boolean removeExistingTheme) {
        IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
        try {
            ts.applyTheme(FileProvider.CONTENT + FileUtils.stripPath(theme), mExcludedItemsList,
                    applyFont, scaleBoot, removeExistingTheme);
            lockScreenOrientation();
            showDialog(DIALOG_PROGRESS);
        } catch (Exception e) {
            SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body,
                    ThemeDetailActivity.this);
        }
    }
}
