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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.ServiceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.thememanager.Globals;
import com.android.thememanager.PreviewHelper;
import com.android.thememanager.R;
import com.android.thememanager.SimpleDialogs;
import com.android.thememanager.Theme;
import com.android.thememanager.ThemeUtils;
import com.android.thememanager.provider.FileProvider;
import com.android.thememanager.widget.SlidingUpPanelLayout;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

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

public class ThemeDetailActivity extends Activity implements SlidingUpPanelLayout.PanelSlideListener {
    private static final String TAG = "ThemeManager";
    private static final String THEMES_PATH = Globals.DEFAULT_THEME_PATH;

    private static final int DIALOG_PROGRESS = 0;

    private Gallery mPreviews = null;
    private String[] mPreviewList = null;
    private ImageAdapter mAdapter = null;
    private ProgressDialog mProgressDialog;
    private Theme mTheme = null;
    private SlidingUpPanelLayout mSlidingPanel;
    private CheckBox mRemoveExistingThemeCheckBox;
    private boolean mRemoveExistingTheme = false;

    private List<String> mExcludedItemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_detail);

        mTheme = ThemeUtils.getThemeEntryById(getIntent().getLongExtra("theme_id", -1), this);
        mExcludedItemsList = new ArrayList<String>();

        if (mTheme == null)
            finish();

        mPreviewList = PreviewHelper.getAllPreviews(THEMES_PATH + "/.cache/" +
                mTheme.getFileName());

        ((TextView)findViewById(R.id.theme_name)).setText(mTheme.getTitle());

        mAdapter = new ImageAdapter(this);
        mPreviews = (Gallery) findViewById(R.id.previews);
        mPreviews.setAdapter(mAdapter);
        mPreviews.setSpacing(20);
        mPreviews.setAnimationDuration(1000);

        setupSlidingPanel();

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
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
        initChecklistItem(R.id.has_systemui, mTheme.getHasSystemUI());
        initChecklistItem(R.id.has_framework, mTheme.getHasFramework());
        initChecklistItem(R.id.has_contacts, mTheme.getHasContacts());
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
            if (Globals.ACTION_THEME_APPLIED.equals(action)) {
                dismissDialog(DIALOG_PROGRESS);
            } else if (Globals.ACTION_THEME_NOT_APPLIED.equals(action)) {
                dismissDialog(DIALOG_PROGRESS);
                SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body,
                        ThemeDetailActivity.this);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
        mAdapter.destroyImages();
        mAdapter = null;
        System.gc();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ugly hack to keep the dialog from reappearing when the app is restarted
        // due to a theme change.
        try {
            dismissDialog(DIALOG_PROGRESS);
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

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        private ImageView[] mImages;

        public ImageAdapter(Context c) {
            mContext = c;
            if (mImages == null) {
                mImages = new ImageView[mPreviewList.length];
            }
        }

        @Override
        public int getCount() {
            return mPreviewList.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (mImages[position] == null) {
                mImages[position] = new ImageView(mContext);
                mImages[position].setLayoutParams(new Gallery.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                FileInputStream is = null;
                try {
                    is = new FileInputStream(THEMES_PATH + "/.cache/" +
                        mTheme.getFileName() + "/" + mPreviewList[position]);
                } catch (FileNotFoundException e) {
                }
                if (is != null) {
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inPreferredConfig = Bitmap.Config.RGB_565;
                    opts.inDensity = DisplayMetrics.DENSITY_HIGH;
                    opts.inTargetDensity = mContext.getResources().getDisplayMetrics().densityDpi;
                    opts.inScaled = true;
                    Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
                    Drawable drawable = new BitmapDrawable(bmp);
                    mImages[position].setImageDrawable(drawable);
                } else
                    mImages[position].setImageResource(R.drawable.no_preview);
                mImages[position].setScaleType(ImageView.ScaleType.FIT_CENTER);
            }

            return mImages[position];

            //return mImages[position];
        }
        /** Returns the size (0.0f to 1.0f) of the views
         * depending on the 'offset' to the center. */
        public float getScale(boolean focused, int offset) {
            /* Formula: 1 / (2 ^ offset) */
            return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset)));
        }

        public void destroyImages() {
            for (int i = 0; mImages != null && i < mImages.length; i++) {
                if (mImages[i] != null) {
                    if (mImages[i].getDrawable() != null)
                        mImages[i].getDrawable().setCallback(null);
                    mImages[i].setImageDrawable(null);
                }
            }
        }
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

    private void applyTheme(String theme, boolean applyFont, boolean scaleBoot, boolean removeExistingTheme) {
        IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
        try {
            ts.applyTheme(FileProvider.CONTENT + ThemeUtils.stripPath(theme), mExcludedItemsList,
                    applyFont, scaleBoot, removeExistingTheme);
            showDialog(DIALOG_PROGRESS);
        } catch (Exception e) {
            SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body,
                    ThemeDetailActivity.this);
        }
    }
}
