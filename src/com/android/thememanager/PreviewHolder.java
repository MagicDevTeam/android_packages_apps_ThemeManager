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

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.thememanager.widget.FlipImageView;

public class PreviewHolder {
    public FlipImageView preview;
    public ImageView osTag;
    public View progress;
    public TextView name;
    public int index = 0;
}
