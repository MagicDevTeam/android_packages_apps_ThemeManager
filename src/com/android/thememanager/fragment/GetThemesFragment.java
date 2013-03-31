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

package com.android.thememanager.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.thememanager.R;

public class GetThemesFragment extends Fragment {
    private static final String HTML_LINK = "<a href='%s'>%s</a>";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {

        View v = layoutInflater.inflate(R.layout.fragment_get_themes, viewGroup, false);

        setHtmlLink((TextView)v.findViewById(R.id.goo), getString(R.string.get_themes_goo_link),
                getString(R.string.get_themes_goo_title));
        setHtmlLink((TextView)v.findViewById(R.id.chaos_forums), getString(R.string.get_themes_chaos_forums_link),
                getString(R.string.get_themes_chaos_forums_title));
        setHtmlLink((TextView)v.findViewById(R.id.pimpmymiui), getString(R.string.get_themes_pimp_my_miui_link),
                getString(R.string.get_themes_pimp_my_miui_title));
        setHtmlLink((TextView)v.findViewById(R.id.miui_forums), getString(R.string.get_themes_miui_link),
                getString(R.string.get_themes_miui_title));

        return v;
    }

    private void setHtmlLink(TextView tv, String link, String title) {
        tv.setText(Html.fromHtml(String.format(HTML_LINK, link, title)));
        tv.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
