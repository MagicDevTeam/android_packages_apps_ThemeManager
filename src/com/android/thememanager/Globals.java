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

import android.os.Environment;

import java.io.File;

public class Globals {
    public static final String THEME_PATH = "/ChaOS/theme";
    public static final String DEFAULT_THEME_PATH = Environment.getExternalStorageDirectory() + THEME_PATH;
    public static final String CACHE_DIR = DEFAULT_THEME_PATH + "/.cache";
    public static final String SYSTEM_THEME_PATH = "/system/media";
    public static final String DATA_THEME_PATH = "/data/system/theme";
    public static final String SYSTEM_FONT_PATH = "/data/fonts";
    public static final String RINGTONES_PATH = DATA_THEME_PATH + File.separator + "ringtones";
    public static final String DEFAULT_SYSTEM_THEME = SYSTEM_THEME_PATH + "/default.ctz";
    public static final String BACKUP_PATH = Environment.getExternalStorageDirectory() + "/ChaOS/backup";

    public static final String ACTION_THEME_APPLIED = "com.android.server.ThemeManager.action.THEME_APPLIED";
    public static final String ACTION_THEME_NOT_APPLIED = "com.android.server.ThemeManager.action.THEME_NOT_APPLIED";
}
