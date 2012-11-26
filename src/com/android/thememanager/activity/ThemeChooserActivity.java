package com.android.thememanager.activity;

import android.content.Context;
import android.content.res.IThemeManagerService;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.ServiceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import com.android.thememanager.Globals;
import com.android.thememanager.ThemeUtils;
import com.android.thememanager.PreviewManager;
import com.android.thememanager.provider.FileProvider;
import com.android.thememanager.R;
import cos.util.CommandLineUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ThemeChooserActivity extends Activity {
    private static final String TAG = "ThemeManager";
    private static final String THEMES_PATH = Globals.DEFAULT_THEME_PATH;

    private Gallery mCoverFlow = null;
    private String[] mThemeList = null;
    private Button mApplyButton = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_theme_chooser);

        mThemeList = themeList(THEMES_PATH);
        
        ThemeUtils.createCacheDir();

        mCoverFlow = (Gallery) findViewById(R.id.coverflow);
        mCoverFlow.setAdapter(new ImageAdapter(this));
        mCoverFlow.setSpacing(20);
        mCoverFlow.setAnimationDuration(1000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_theme_chooser, menu);
		return true;
	}

    private String[] themeList(String path) {
        Log.d(TAG, "Returning theme list for " + path);
        FilenameFilter themeFilter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                if (s.toLowerCase().endsWith(".mtz"))
                    return true;
                else
                    return false;
            }
        };

        File dir = new File(path);
        String[] dirList = null;
        if (dir.exists() && dir.isDirectory())
            dirList = dir.list(themeFilter);
        else
            Log.e(TAG, path + " does not exist or is not a directory!");
        return dirList;
    }

    public void applyTheme(View view) {
        int index = mCoverFlow.getSelectedItemPosition();

        if (index < 0 || index >= mThemeList.length)
            return;
        
        IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
        try {
            ts.applyTheme(FileProvider.CONTENT + mThemeList[index]);
        } catch (Exception e) {
            Log.e(TAG, "Failed to call ThemeService.applyTheme", e);
        }
    }

    public class ImageAdapter extends BaseAdapter {
        int mGalleryItemBackground;
        private Context mContext;

        private PreviewManager mPreviewManager = new PreviewManager();

        private String[] mImageIds = themeList(THEMES_PATH);

        private ImageView[] mImages;

        public ImageAdapter(Context c) {
            mContext = c;
            mImages = new ImageView[mImageIds.length];
            for (int i = 0; i < mImages.length; i++) {
                mImages[i] = new ImageView(mContext);
                mImages[i].setImageResource(R.drawable.preview);
                mPreviewManager.fetchDrawableOnThread(ThemeUtils.stripExtension(mImageIds[i]), mImages[i]);
            }
        }

        public int getCount() {
            return mImageIds.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            //Use this code if you want to load from resources
            ImageView i = mImages[position];//new ImageView(mContext);
            //i.setImageResource(R.drawable.preview);
            i.setLayoutParams(new Gallery.LayoutParams(240, 427));
            i.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            //mPreviewManager.fetchDrawableOnThread(mImageIds[position], i);

            return i;

            //return mImages[position];
        }
        /** Returns the size (0.0f to 1.0f) of the views
         * depending on the 'offset' to the center. */
        public float getScale(boolean focused, int offset) {
            /* Formula: 1 / (2 ^ offset) */
            return Math.max(0, 1.0f / (float)Math.pow(2, Math.abs(offset)));
        }

    }
}
