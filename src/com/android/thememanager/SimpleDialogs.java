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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;

public class SimpleDialogs {

	public interface OnYesNoResponse {
		public void onYesNoResponse(boolean isYes);
	}
	
	public static void displayYesNoDialog(String yesText, String noText, String title,
			String bodyText, Context context, final OnYesNoResponse callback) {
		new AlertDialog.Builder(context)
		.setMessage(bodyText)
		.setTitle(title)
		.setPositiveButton(yesText, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				callback.onYesNoResponse(true);
			}

		})
		.setNeutralButton(noText, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				callback.onYesNoResponse(false);
			}
		})
		.show();
	}
	
	public static void displayOkDialog(String title, String body, Context context) {
		new AlertDialog.Builder(context)
		.setMessage(body)
		.setTitle(title)
		.setPositiveButton(context.getString(R.string.btn_ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				}).show();
	}

	public static void displayOkDialog(int titleID, int bodyID, Context context) {
		Resources res = context.getResources();
		new AlertDialog.Builder(context)
		.setMessage(res.getString(bodyID))
		.setTitle(res.getString(titleID))
		.setPositiveButton(context.getString(R.string.btn_ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				}).show();
	}

	public static void displayOkDialogIcon(String title, String body, Context context, int resID) {
		new AlertDialog.Builder(context)
		.setMessage(body)
		.setTitle(title)
		.setIcon(resID)
		.setPositiveButton(context.getString(R.string.btn_ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.dismiss();
					}
				}).show();
	}
}
