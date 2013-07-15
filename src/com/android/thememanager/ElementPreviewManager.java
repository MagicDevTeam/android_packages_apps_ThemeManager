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

package com.android.thememanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;

public class ElementPreviewManager {
    private static final boolean DEBUG = false;
    private final Map<String, BitmapDrawable> drawableMap;

    public ElementPreviewManager() {
        drawableMap = new WeakHashMap<String, BitmapDrawable>();
    }

    @SuppressWarnings("deprecation")
    public BitmapDrawable fetchDrawable(Theme theme, int elementType) {
        String themeId = theme.getFileName();
        if (drawableMap.containsKey(themeId)) {
            return drawableMap.get(themeId);
        }

        if (DEBUG)
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
                is.close();
            }

            if (drawable != null) {
                drawableMap.put(themeId, drawable);
                if (DEBUG)
                    Log.d(this.getClass().getSimpleName(), "got a thumbnail drawable: " + drawable.getBounds() + ", "
                            + drawable.getIntrinsicHeight() + "," + drawable.getIntrinsicWidth() + ", "
                            + drawable.getMinimumHeight() + "," + drawable.getMinimumWidth());
            } else {
                if (DEBUG)
                    Log.w(this.getClass().getSimpleName(), "could not get thumbnail");
            }

            return drawable;
        } catch (IOException e) {
            if (DEBUG)
                Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
            return null;
        }
    }

    public void fetchDrawableOnThread(final Theme theme, final int elementType, final PreviewHolder holder) {
        String themeId = theme.getFileName();
        if (drawableMap.containsKey(themeId)) {
            holder.preview.setImageDrawable(drawableMap.get(themeId));
            holder.progress.setVisibility(View.GONE);
            return;
        }

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (message.obj != null)
                    holder.preview.setImageDrawable((BitmapDrawable) message.obj);
                else
                    holder.preview.setImageResource(R.drawable.no_preview);
                holder.progress.setVisibility(View.GONE);
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
        if (!ThemeUtils.themeCacheDirExists(theme.getFileName())) {
            ThemeUtils.extractThemePreviews(theme.getFileName(), theme.getThemePath());
        }
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
                case Theme.THEME_ELEMENT_TYPE_CONTACTS:
                    previewName = PreviewHelper.getContactsPreviews(Globals.CACHE_DIR + "/" + themeId)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_RINGTONES:
                    previewName = PreviewHelper.getContactsPreviews(Globals.CACHE_DIR + "/" + themeId)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_BOOTANIMATION:
                    previewName = PreviewHelper.getBootanimationPreviews(Globals.CACHE_DIR + "/" + themeId)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_MMS:
                    previewName = PreviewHelper.getMmsPreviews(Globals.CACHE_DIR + "/" + themeId)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_FONT:
                    previewName = PreviewHelper.getFontsPreviews(Globals.CACHE_DIR + "/" + themeId)[0];
                    break;
            }
        } catch (Exception e) {
            previewName = null;
        }

        FileInputStream fis = null;
        if (previewName != null) {
            fis = new FileInputStream(Globals.CACHE_DIR + "/" + themeId + "/" + previewName);
        }

        return fis;
    }
}
