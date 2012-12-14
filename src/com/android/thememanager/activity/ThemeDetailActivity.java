package com.android.thememanager.activity;

import android.content.Context;
import android.content.res.IThemeManagerService;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.thememanager.*;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ThemeDetailActivity extends Activity {
    private static final String TAG = "ThemeManager";
    private static final String THEMES_PATH = Globals.DEFAULT_THEME_PATH;

    private static final int DIALOG_PROGRESS = 0;

    private Gallery mPreviews = null;
    private String[] mPreviewList = null;
    private ImageAdapter mAdapter = null;
    private ProgressDialog mProgressDialog;
    private String mThemeName = "";
    private ThemeUtils.ThemeDetails mDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_detail);


        mThemeName = getIntent().getStringExtra("theme_name");
        mPreviewList = PreviewHelper.getAllPreviews(THEMES_PATH + "/.cache/" +
                ThemeUtils.stripExtension(mThemeName));

        mDetails = ThemeUtils.getThemeDetails(Environment.getExternalStorageDirectory()
                + "/" + Globals.THEME_PATH + "/" + mThemeName);

        ((TextView)findViewById(R.id.theme_name)).setText(mDetails.title);

        if (TextUtils.isEmpty(mThemeName))
            finish();

        mAdapter = new ImageAdapter(this);
        mPreviews = (Gallery) findViewById(R.id.previews);
        mPreviews.setAdapter(mAdapter);
        mPreviews.setSpacing(20);
        mPreviews.setAnimationDuration(1000);

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

        mAdapter.notifyDataSetChanged();
    }

    public void applyTheme(View view) {
        new ApplyThemeTask().execute(mThemeName);
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
                FileInputStream is = null;
                try {
                    is = new FileInputStream(THEMES_PATH + "/.cache/" +
                        ThemeUtils.stripExtension(mThemeName) + "/" + mPreviewList[position]);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                if (is != null) {
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
                    Drawable drawable = new BitmapDrawable(bmp);
                    mImages[position].setImageDrawable(drawable);
                } else
                    mImages[position].setImageResource(R.drawable.no_preview);
                mImages[position].setAdjustViewBounds(true);
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
