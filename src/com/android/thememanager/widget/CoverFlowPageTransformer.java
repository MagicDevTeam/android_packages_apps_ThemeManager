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

package com.android.thememanager.widget;

import android.support.v4.view.ViewPager;
import android.view.View;

public class CoverFlowPageTransformer implements ViewPager.PageTransformer {
    private static float MIN_SCALE = 0.75f;

    public void transformPage(View view, float position) {
        // slightly rotate views along the Y-axis
        view.setRotationY(position * -15f);

        // make off page views slightly transparent
        if (position <= 0) {
            view.setAlpha(Math.max(0.25f, 1 + position*0.75f));
        } else {
            view.setAlpha(Math.max(0.25f, 1 - position*0.75f));
        }
    }
}