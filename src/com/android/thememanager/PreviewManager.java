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
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.WeakHashMap;
import java.util.Map;

public class PreviewManager {
    private static final boolean DEBUG = false;
    private final Map<String, BitmapDrawable> drawableMap;

    public PreviewManager() {
        drawableMap = new WeakHashMap<String, BitmapDrawable>();
    }

    public BitmapDrawable fetchDrawable(Theme theme) {
        String themeId = theme.getFileName();
        if (drawableMap.containsKey(themeId)) {
            return drawableMap.get(themeId);
        }

        if (DEBUG)
            Log.d(this.getClass().getSimpleName(), "theme ID:" + themeId);
        try {
            InputStream is = fetch(theme);
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

    public void fetchDrawableOnThread(final Theme theme, final ImageView imageView) {
        String themeId = theme.getFileName();
        if (drawableMap.containsKey(themeId)) {
            imageView.setImageDrawable(drawableMap.get(themeId));
            return;
        } else
            imageView.setImageResource(R.drawable.preview);

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
                BitmapDrawable drawable = fetchDrawable(theme);
                Message message = handler.obtainMessage(1, drawable);
                handler.sendMessage(message);
            }
        };
        thread.start();
    }

    private InputStream fetch(Theme theme) throws IOException {
        if (!ThemeUtils.themeCacheDirExists(theme.getFileName())) {
            ThemeUtils.extractThemePreviews(theme.getFileName(), theme.getThemePath());
        }
        FileInputStream fis = new FileInputStream(Globals.CACHE_DIR + "/" + theme.getFileName() + "/preview_launcher_0.png");

        return fis;
    }
}
