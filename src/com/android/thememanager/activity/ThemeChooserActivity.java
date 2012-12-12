package com.android.thememanager.activity;

import android.content.Context;
import android.content.res.IThemeManagerService;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
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
import java.util.zip.ZipInputStream;

public class ThemeChooserActivity extends Activity {
    private static final String TAG = "ThemeManager";
    private static final String THEMES_PATH = Globals.DEFAULT_THEME_PATH;

    private static final int DIALOG_PROGRESS = 0;

    private Gallery mCoverFlow = null;
    private String[] mThemeList = null;
    private Button mApplyButton = null;
    private ImageAdapter mAdapter = null;
    private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_theme_chooser);

        mThemeList = themeList(THEMES_PATH);
        
        ThemeUtils.createCacheDir();

        mAdapter = new ImageAdapter(this);
        mCoverFlow = (Gallery) findViewById(R.id.coverflow);
        mCoverFlow.setAdapter(mAdapter);
        mCoverFlow.setSpacing(20);
        mCoverFlow.setAnimationDuration(1000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_theme_chooser, menu);
		return true;
	}

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
        
        new ApplyThemeTask().execute(mThemeList[index]);
    }

    public void clearTheme(View view) {
        // have the theme service remove the existing theme
        IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
        try {
            ts.removeTheme();
            ts.applyInstalledTheme();
        } catch (Exception e) {
            Log.e(TAG, "Failed to call ThemeService.removeTheme", e);
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
            if (mImages == null) {
                mImages = new ImageView[mImageIds.length];
                for (int i = 0; i < mImages.length; i++) {
                    mImages[i] = new ImageView(mContext);
                    mImages[i].setImageResource(R.drawable.preview);
                    mPreviewManager.fetchDrawableOnThread(ThemeUtils.stripExtension(mImageIds[i]), mImages[i]);
                }
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

        public void destroyImages() {
            for (int i = 0; i < mImages.length; i++) {
                if (mImages[i].getDrawable() != null)
                    mImages[i].getDrawable().setCallback(null);
                mImages[i].setImageDrawable(null);
            }

            mPreviewManager = null;
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

    private class ApplyThemeTask extends AsyncTask<String, Integer, Boolean> {
		@Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog( DIALOG_PROGRESS );
        }

        protected Boolean doInBackground(String... theme) {
            try{
                ZipInputStream zip = new ZipInputStream(new BufferedInputStream(
                        new FileInputStream(THEMES_PATH + "/" + theme[0])));
                ZipEntry ze = null;

                // have the theme service remove the existing theme
                IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
                try {
                    ts.removeTheme();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to call ThemeService.removeTheme", e);
                }

                while ((ze = zip.getNextEntry()) != null) {
                    if (ze.isDirectory()) {
                        // Assume directories are stored parents first then children
                        Log.d(TAG, "Creating directory /data/system/theme/" + ze.getName());
                        File dir = new File("/data/system/theme/" + ze.getName());
                        dir.mkdir();
                        dir.setReadable(true, false);
                        dir.setWritable(true, false);
                        dir.setExecutable(true, false);
                        zip.closeEntry();
                        continue;
                    }
            
                    Log.d(TAG, "Creating file " + ze.getName());
                    copyInputStream(zip,
                            new BufferedOutputStream(new FileOutputStream("/data/system/theme/" + ze.getName())));
                    (new File("/data/system/theme/" + ze.getName())).setReadable(true, false);
                    zip.closeEntry();
                }
            
                zip.close();
                try {
                    ts.applyInstalledTheme();
                } catch (Exception e) {
                    Log.e(TAG, "Failed to call ThemeService.applyInstalledTheme", e);
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "ApplyThemeTask FileNotFoundException", e);
                return Boolean.FALSE;
            } catch (IOException e) {
                Log.e(TAG, "ApplyThemeTask IOException", e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Boolean result) {
            //dismissDialog(DIALOG_PROGRESS);
            if (result.equals(Boolean.TRUE)) {
            }
        }
    }
}
