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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.IThemeManagerService;
import android.os.Bundle;
import android.os.ServiceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.thememanager.ElementPreviewManager;
import com.android.thememanager.Globals;
import com.android.thememanager.PreviewHolder;
import com.android.thememanager.R;
import com.android.thememanager.SimpleDialogs;
import com.android.thememanager.Theme;
import com.android.thememanager.ThemesDataSource;
import com.android.thememanager.ThemeUtils;

import java.util.List;

public class ThemeMixerChooserActivity extends Activity {
    private static final String TAG = "ThemeManager";
    private static final String THEMES_PATH = Globals.DEFAULT_THEME_PATH;

    private GridView mGridView = null;
    private List<Theme> mThemeList = null;
    private PreviewAdapter mAdapter = null;
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
        mAdapter = new PreviewAdapter(ThemeMixerChooserActivity.this);
        mGridView.setAdapter(mAdapter);

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
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
                        case Theme.THEME_ELEMENT_TYPE_CONTACTS:
                            ts.resetThemeContacts();
                            break;
                    }
                } catch (Exception e) {
                }
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

    public class PreviewAdapter extends BaseAdapter {
        private Context mContext;

        private ElementPreviewManager mPreviewManager = new ElementPreviewManager();

        private View[] mPreviews;
        private int mPreviewWidth;
        private int mPreviewHeight;

        public PreviewAdapter(Context c) {
            mContext = c;
            DisplayMetrics dm = c.getResources().getDisplayMetrics();
            mPreviewWidth = dm.widthPixels / 3;
            mPreviewHeight = dm.heightPixels / 3;

            preloadPreviews();
        }

        private void preloadPreviews() {
            mPreviews = new View[mThemeList.size()];
            for (int i = 0; i < mPreviews.length; i++) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mPreviews[i] = inflater.inflate(R.layout.theme_preview, null);
                FrameLayout fl = (FrameLayout)mPreviews[i].findViewById(R.id.preview_layout);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)fl.getLayoutParams();
                params.width = mPreviewWidth;
                params.height = mPreviewHeight;
                fl.setLayoutParams(params);
                PreviewHolder holder = new PreviewHolder();
                holder.preview = (ImageView) mPreviews[i].findViewById(R.id.preview_image);
                holder.name = (TextView) mPreviews[i].findViewById(R.id.theme_name);
                holder.osTag = (ImageView) mPreviews[i].findViewById(R.id.os_indicator);
                holder.progress = mPreviews[i].findViewById(R.id.loading_indicator);
                mPreviews[i].setTag(holder);
                mPreviewManager.fetchDrawableOnThread(mThemeList.get(i), mElementType, holder);

                holder.name.setText(mThemeList.get(i).getTitle());
                holder.preview.setImageResource(R.drawable.empty_preview);

                if (mThemeList.get(i).getIsCosTheme())
                    holder.osTag.setImageResource(R.drawable.chaos);
                else
                    holder.osTag.setImageResource(R.drawable.miui);
            }
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
            return mPreviews[position];
        }

        public void destroy() {
            mPreviewManager = null;
            mContext = null;
        }
    }
}
