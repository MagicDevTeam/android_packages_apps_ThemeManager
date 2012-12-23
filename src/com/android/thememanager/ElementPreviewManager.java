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

package com.android.thememanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import com.android.thememanager.activity.ThemeMixerChooserActivity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;

public class ElementPreviewManager {
    private final Map<String, BitmapDrawable> drawableMap;

    public ElementPreviewManager() {
        drawableMap = new WeakHashMap<String, BitmapDrawable>();
    }

    public BitmapDrawable fetchDrawable(Theme theme, int elementType) {
        String themeId = theme.getFileName();
        if (drawableMap.containsKey(themeId)) {
            return drawableMap.get(themeId);
        }

        Log.d(this.getClass().getSimpleName(), "theme ID:" + themeId);
        try {
            InputStream is = fetch(theme, elementType);
            BitmapDrawable drawable = null;
            if (is != null) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inPreferredConfig = Bitmap.Config.RGB_565;
                opts.inSampleSize = 2;
                Bitmap bmp = BitmapFactory.decodeStream(is, null, opts);
                drawable = new BitmapDrawable(bmp);
                //drawable = (BitmapDrawable)BitmapDrawable.createFromStream(is, "src");
                is.close();
            }

            if (drawable != null) {
                drawableMap.put(themeId, drawable);
                Log.d(this.getClass().getSimpleName(), "got a thumbnail drawable: " + drawable.getBounds() + ", "
                        + drawable.getIntrinsicHeight() + "," + drawable.getIntrinsicWidth() + ", "
                        + drawable.getMinimumHeight() + "," + drawable.getMinimumWidth());
            } else {
                Log.w(this.getClass().getSimpleName(), "could not get thumbnail");
            }

            return drawable;
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
            return null;
        }
    }

    public void fetchDrawableOnThread(final Theme theme, final int elementType, final ImageView imageView) {
        String themeId = theme.getFileName();
        if (drawableMap.containsKey(themeId)) {
            imageView.setImageDrawable(drawableMap.get(themeId));
        }

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.obj != null)
                    imageView.setImageDrawable((BitmapDrawable) message.obj);
                else
                    imageView.setImageResource(R.drawable.no_preview);
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {
                //TODO : set imageView to a "pending" image
                BitmapDrawable drawable = fetchDrawable(theme, elementType);
                Message message = handler.obtainMessage(1, drawable);
                handler.sendMessage(message);
            }
        };
        thread.start();
    }

    private InputStream fetch(Theme theme, int elementType) throws IOException {
//        return ThemeUtils.getThemePreview(Environment.getExternalStorageDirectory() + "/" +
//                Globals.THEME_PATH + "/" + themeId + ".mtz", "preview_launcher_0.png");
        String previewName = null;
        String themeId = theme.getFileName();
        try{
            switch(elementType) {
                case Theme.THEME_ELEMENT_TYPE_ICONS:
                    previewName = PreviewHelper.getIconPreviews(Globals.CACHE_DIR + "/" + themeId)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_WALLPAPER:
                    previewName = PreviewHelper.getLauncherPreviews(Globals.CACHE_DIR + "/" + themeId)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_SYSTEMUI:
                    previewName = PreviewHelper.getStatusbarPreviews(Globals.CACHE_DIR + "/" + themeId)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_FRAMEWORK:
                    previewName = PreviewHelper.getLauncherPreviews(Globals.CACHE_DIR + "/" + themeId)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_LOCKSCREEN:
                    previewName = PreviewHelper.getLockscreenPreviews(Globals.CACHE_DIR + "/" + themeId)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_RINGTONES:
                    previewName = PreviewHelper.getContactsPreviews(Globals.CACHE_DIR + "/" + themeId)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_BOOTANIMATION:
                    previewName = PreviewHelper.getBootanimationPreviews(Globals.CACHE_DIR + "/" + themeId)[0];
                    break;
            }
        } catch (Exception e) {
            previewName = null;
        }

        FileInputStream fis = null;
        if (previewName != null)
            fis = new FileInputStream(Globals.CACHE_DIR + "/" + themeId + "/" + previewName);

        return fis;
    }
}
