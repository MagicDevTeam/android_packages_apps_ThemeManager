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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
                long delay = 0;
                delay = holder.index * 50;
                if (message.obj != null)
                    holder.preview.setImageDrawableAnimated((BitmapDrawable) message.obj, delay);
                else
                    holder.preview.setImageResourceAnimated(R.drawable.no_preview, delay);
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
        String previewName = "";
        try{
            switch(elementType) {
                case Theme.THEME_ELEMENT_TYPE_ICONS:
                    previewName = PreviewHelper.getIconPreviews(theme)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_WALLPAPER:
                    previewName = PreviewHelper.getWallpaperPreviews(theme)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_LOCK_WALLPAPER:
                    previewName = PreviewHelper.getLockWallpaperPreviews(theme)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_SYSTEMUI:
                    previewName = PreviewHelper.getStatusbarPreviews(theme)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_FRAMEWORK:
                    previewName = PreviewHelper.getLauncherPreviews(theme)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_CONTACTS:
                    previewName = PreviewHelper.getContactsPreviews(theme)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_DIALER:
                    previewName = PreviewHelper.getDialerPreviews(theme)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_RINGTONES:
                    previewName = PreviewHelper.getContactsPreviews(theme)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_BOOTANIMATION:
                    previewName = PreviewHelper.getBootanimationPreviews(theme)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_MMS:
                    previewName = PreviewHelper.getMmsPreviews(theme)[0];
                    break;
                case Theme.THEME_ELEMENT_TYPE_FONT:
                    previewName = PreviewHelper.getFontsPreviews(theme)[0];
                    break;
            }
        } catch (Exception e) {
            previewName = null;
        }

        ZipFile zip = new ZipFile(theme.getThemePath());
        ZipEntry ze = previewName != null ? zip.getEntry(previewName) : null;
        return ze != null ? zip.getInputStream(ze) : null;
    }
}
