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

package com.android.thememanager.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.IThemeManagerService;
import android.os.*;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;
import com.android.thememanager.*;

import java.util.List;

public class ThemeMixerChooserActivity extends Activity {
    private static final String TAG = "ThemeManager";
    private static final String THEMES_PATH = Globals.DEFAULT_THEME_PATH;

    private GridView mGridView = null;
    private List<Theme> mThemeList = null;
    private ImageAdapter mAdapter = null;
    private ProgressDialog mProgressDialog;
    private int mElementType = Theme.THEME_ELEMENT_TYPE_ICONS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_theme_chooser);

        mElementType = getIntent().getIntExtra("type", 0);
        mThemeList = themeList(mElementType);

        setTitle(Theme.sElementLabels[mElementType]);

        mGridView = (GridView) findViewById(R.id.coverflow);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent;
                if (mElementType == Theme.THEME_ELEMENT_TYPE_BOOTANIMATION) {
                    intent = new Intent(ThemeMixerChooserActivity.this, ThemeBootanimationDetailActivity.class);
                } else if (mElementType == Theme.THEME_ELEMENT_TYPE_RINGTONES) {
                    intent = new Intent(ThemeMixerChooserActivity.this, ThemeRingtoneDetailActivity.class);
                } else {
                    intent = new Intent(ThemeMixerChooserActivity.this, ThemeElementDetailActivity.class);
                }
                intent.putExtra("type", mElementType);
                intent.putExtra("theme_id", mThemeList.get(i).getId());
                startActivity(intent);
            }
        });
        mAdapter = new ImageAdapter(ThemeMixerChooserActivity.this);
        mGridView.setAdapter(mAdapter);
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.destroy();
        mAdapter = null;
        mGridView = null;
        System.gc();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private List<Theme> themeList(int elementType) {
        ThemesDataSource dataSource = new ThemesDataSource(this);
        dataSource.open();
        List<Theme> list = null;
        switch(elementType) {
            case Theme.THEME_ELEMENT_TYPE_ICONS:
                list = dataSource.getIconThemes();
                break;
            case Theme.THEME_ELEMENT_TYPE_WALLPAPER:
                list = dataSource.getWallpaperThemes();
                break;
            case Theme.THEME_ELEMENT_TYPE_SYSTEMUI:
                list = dataSource.getSystemUIThemes();
                break;
            case Theme.THEME_ELEMENT_TYPE_FRAMEWORK:
                list = dataSource.getFrameworkThemes();
                break;
            case Theme.THEME_ELEMENT_TYPE_CONTACTS:
                list = dataSource.getContactsThemes();
                break;
            case Theme.THEME_ELEMENT_TYPE_RINGTONES:
                list = dataSource.getRingtoneThemes();
                break;
            case Theme.THEME_ELEMENT_TYPE_BOOTANIMATION:
                list = dataSource.getBootanimationThemes();
                break;
            case Theme.THEME_ELEMENT_TYPE_MMS:
                list = dataSource.getMmsThemes();
                break;
            case Theme.THEME_ELEMENT_TYPE_FONT:
                list = dataSource.getFontThemes();
                break;
        }

        dataSource.close();
        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_theme_element, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_reset:
                String installedThemeDir = "/data/system/theme/";
                try {
                    final IThemeManagerService ts = 
                            IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
                    switch (mElementType) {
                        case Theme.THEME_ELEMENT_TYPE_ICONS:
                            ts.resetThemeIcons();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_WALLPAPER:
                            ts.resetThemeWallpaper();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_SYSTEMUI:
                            ts.resetThemeSystemUI();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_FRAMEWORK:
                            ts.resetThemeFramework();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_RINGTONES:
                            ts.resetThemeRingtone();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_BOOTANIMATION:
                            ts.resetThemeBootanimation();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_MMS:
                            ts.resetThemeMms();
                            break;
                        case Theme.THEME_ELEMENT_TYPE_FONT:
                            if (ThemeUtils.installedThemeHasFonts()) {
                                SimpleDialogs.displayYesNoDialog(
                                        getString(R.string.dlg_reset_font_and_reboot),
                                        getString(R.string.dlg_reset_font_without_reboot),
                                        getString(R.string.dlg_reset_font_title),
                                        getString(R.string.dlg_reset_font_body),
                                        this,
                                        new SimpleDialogs.OnYesNoResponse() {
                                            @Override
                                            public void onYesNoResponse(boolean isYes) {
                                                if (isYes)
                                                    try {
                                                        ts.resetThemeFontReboot();
                                                    } catch(Exception e) {}
                                            }
                                        });
                            }
                            break;
                    }
                } catch (Exception e) {
                }
                return true;
            default:
                return false;
        }
    }

    public class ImageAdapter extends BaseAdapter {
        int mGalleryItemBackground;
        private Context mContext;

        private ElementPreviewManager mPreviewManager = new ElementPreviewManager();

        private ImageView[] mImages;
        private int mPreviewWidth;
        private int mPreviewHeight;

        public ImageAdapter(Context c) {
            mContext = c;
            DisplayMetrics dm = c.getResources().getDisplayMetrics();
            mPreviewWidth = dm.widthPixels / 3;
            mPreviewHeight = dm.heightPixels / 3;
        }

        public int getCount() {
            return mThemeList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            PreviewHolder holder = null;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.theme_preview, null);
                FrameLayout fl = (FrameLayout)convertView.findViewById(R.id.preview_layout);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)fl.getLayoutParams();
                params.width = mPreviewWidth;
                params.height = mPreviewHeight;
                fl.setLayoutParams(params);
                holder = new PreviewHolder();
                holder.preview = (ImageView) convertView.findViewById(R.id.preview_image);
                holder.name = (TextView) convertView.findViewById(R.id.theme_name);
                holder.osTag = (ImageView) convertView.findViewById(R.id.os_indicator);
                convertView.setTag(holder);
            } else {
                holder = (PreviewHolder) convertView.getTag();
            }
            if (holder.preview.getDrawable() == null) {
                holder.preview.setImageResource(R.drawable.preview);
            }

            mPreviewManager.fetchDrawableOnThread(mThemeList.get(position), mElementType, holder.preview);
            holder.name.setText(mThemeList.get(position).getTitle());

            if (mThemeList.get(position).getIsCosTheme())
                holder.osTag.setImageResource(R.drawable.chaos);
            else
                holder.osTag.setImageResource(R.drawable.miui);

            return convertView;
        }

        public void destroy() {
            /*
            for (int i = 0; i < mImages.length; i++) {
                if (mImages[i] != null && mImages[i].getDrawable() != null) {
                    mImages[i].getDrawable().setCallback(null);
                    mImages[i].setImageDrawable(null);
                }
            }
            */
            mPreviewManager = null;
            mContext = null;
        }
    }
}
