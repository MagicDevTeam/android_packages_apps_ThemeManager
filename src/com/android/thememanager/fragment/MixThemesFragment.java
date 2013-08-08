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

package com.android.thememanager.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.thememanager.R;
import com.android.thememanager.Theme;
import com.android.thememanager.activity.ThemeMixerChooserActivity;

public class MixThemesFragment extends Fragment {

    private GridView mGridView;
    private MixerAdapter mAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {

        View v = layoutInflater.inflate(R.layout.fragment_theme_mixer, viewGroup, false);

        mGridView = (GridView) v.findViewById(R.id.mixer_gridview);
        mAdapter = new MixerAdapter(getActivity());
        mGridView.setAdapter(mAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), ThemeMixerChooserActivity.class);
                intent.putExtra("type", i);
                startActivity(intent);
            }
        });

        return v;
    }

    public static class MixerAdapter extends BaseAdapter {
        private Context mContext;

        public MixerAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return Theme.sElementIcons.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.mixer_item, null);
            }
            ImageView i = (ImageView)v.findViewById(R.id.mixer_icon);//mImages[position];//new ImageView(mContext);
            i.setImageResource(Theme.sElementIcons[position]);

            TextView tv = (TextView) v.findViewById(R.id.mixer_label);

            tv.setText(Theme.sElementLabels[position]);

            return v;
        }
    }
}
