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
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.thememanager.Globals;
import com.android.thememanager.R;
import com.android.thememanager.Theme;
import com.android.thememanager.widget.CoverFlowPageTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.chameleonos.support.widget.LinePageIndicator;
import org.chameleonos.support.widget.PagerContainer;

abstract public class DetailBaseActivity extends Activity {
    protected static final String TAG = "ThemeManager";
    protected static final String THEMES_PATH = Globals.DEFAULT_THEME_PATH;

    protected static final int DIALOG_PROGRESS = 0;

    protected PagerContainer mPreviews = null;
    protected String[] mPreviewList = null;
    protected ImageAdapter mAdapter = null;
    protected ProgressDialog mProgressDialog;
    protected Theme mTheme = null;
    protected Handler mHandler;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.destroyImages();
        mAdapter = null;
        System.gc();
    }

    protected void setupPager() {
        mAdapter = new ImageAdapter(this);
        mPreviews = (PagerContainer) findViewById(R.id.previews);
        ViewPager pager = mPreviews.getViewPager();
        pager.setAdapter(mAdapter);
        //Necessary or the pager will only have one extra page to show
        // make this at least however many pages you can see
        pager.setOffscreenPageLimit(2);
        //If hardware acceleration is enabled, you should also remove
        // clipping on the pager for its children.
        pager.setClipChildren(false);
        pager.setPageTransformer(true, new CoverFlowPageTransformer());

        LinePageIndicator pageIndicator = (LinePageIndicator) findViewById(R.id.page_indicator);
        pageIndicator.setViewPager(pager);
        pageIndicator.setStrokeCap(Paint.Cap.ROUND);
    }

    protected void setupActionBar() {
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        setProgressBarIndeterminate(true);
        setProgressBarIndeterminateVisibility(true);
    }

    public class ImageAdapter extends PagerAdapter {
        private Context mContext;

        private ImageView[] mImages;

        public ImageAdapter(Context c) {
            mContext = c;
            if (mImages == null) {
                mImages = new ImageView[mPreviewList.length];
            }
            for (int i = 0; i < mImages.length; i++) {
                mImages[i] = new ImageView(c);
                mImages[i].setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            preloadImages();
        }

        private void preloadImages() {
            (new PreviewLoaderAsyncTask()).execute();
        }

        @Override
        public int getCount() {
            return mPreviewList.length;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            mImages[position].setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            container.addView(mImages[position]);
            return mImages[position];

            //return mImages[position];
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
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

        private class PreviewLoaderAsyncTask extends AsyncTask {

            @Override
            protected Object doInBackground(Object[] params) {
                int i = 0;
                try {
                    ZipFile zip = new ZipFile(mTheme.getThemePath());
                    ZipEntry ze;
                    for (final ImageView preview : mImages) {
                        ze = zip.getEntry(mPreviewList[i]);
                        InputStream is = ze != null ? zip.getInputStream(ze) : null;
                        if (is != null) {
                            BitmapFactory.Options opts = new BitmapFactory.Options();
                            opts.inPreferredConfig = Bitmap.Config.RGB_565;
                            opts.inDensity = DisplayMetrics.DENSITY_HIGH;
                            opts.inTargetDensity = mContext.getResources().getDisplayMetrics().densityDpi;
                            opts.inScaled = true;
                            Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
                            final Drawable drawable = new BitmapDrawable(bmp);
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    preview.setImageDrawable(drawable);
                                }
                            });
                            try {
                                is.close();
                            } catch (IOException e) {
                            }
                        }
                        i++;
                    }
                } catch (IOException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                setProgressBarIndeterminateVisibility(false);
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

}
