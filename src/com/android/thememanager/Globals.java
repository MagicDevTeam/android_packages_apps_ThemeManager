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

import android.os.Environment;

public class Globals {
    public static final String THEME_PATH = "/ChaOS/theme";
    public static final String DEFAULT_THEME_PATH = Environment.getExternalStorageDirectory() + THEME_PATH;
    public static final String CACHE_DIR = DEFAULT_THEME_PATH + "/.cache";
    public static final String SYSTEM_THEME_PATH = "/system/media";
    public static final String DEFAULT_SYSTEM_THEME = SYSTEM_THEME_PATH + "/default.ctz";

    public static final String ACTION_THEME_APPLIED = "com.android.server.ThemeManager.action.THEME_APPLIED";
    public static final String ACTION_THEME_NOT_APPLIED = "com.android.server.ThemeManager.action.THEME_NOT_APPLIED";
}
