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

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.IThemeManagerService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.thememanager.FileUtils;
import com.android.thememanager.Globals;
import com.android.thememanager.R;
import com.android.thememanager.SimpleDialogs;
import com.android.thememanager.provider.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class BackupRestoreFragment extends ListFragment {
    private static final String TAG = "BackupRestoreFragment";

    private String[] mBackupList;
    private BackupThemeAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        File backupDir = new File(Globals.BACKUP_PATH);
        if (!backupDir.exists())
            backupDir.mkdirs();
        mAdapter = new BackupThemeAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.backup_restore_fragment, null);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateList();
        setListAdapter(mAdapter);
        registerForContextMenu(getListView());
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    applyBackup(mBackupList[position]);
                } catch (IOException e) {
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.backup_restore, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_save:
                doBackup();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu_backup, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index;
        switch (item.getItemId()) {
            case R.id.menu_delete_backup:
                index = info.targetView.getId();
                deleteBackupWithConfirmation(index);
                return true;
            case R.id.menu_overwrite_backup:
                index = info.targetView.getId();
                overwriteBackup(index);
                return true;
            case R.id.menu_rename_backup:
                index = info.targetView.getId();
                renameBackup(FileUtils.stripExtension(mBackupList[index]));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteBackup(int index) {
        File backup = new File(Globals.BACKUP_PATH + File.separator + mBackupList[index]);
        if (backup.exists())
            backup.delete();

        updateList();
    }

    private void updateList() {
        File backupDir = new File(Globals.BACKUP_PATH);
        mBackupList = backupDir.list();
        Arrays.sort(mBackupList);
        mAdapter.notifyDataSetChanged();
    }

    private void doBackup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = getActivity().getLayoutInflater().inflate(R.layout.backup_theme_name, null);
        final EditText editTextBackupName = (EditText) v.findViewById(R.id.backup_name);
        builder.setTitle(R.string.dlg_backup_theme_title);
        builder.setView(v);
        builder.setNegativeButton(R.string.dlg_backup_theme_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.dlg_backup_theme_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CharSequence name = editTextBackupName.getText();
                // hide the onscreen keyboard if it's visible
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editTextBackupName.getWindowToken(), 0);
                if (!TextUtils.isEmpty(name)) {
                    (new BackupThemeTask()).execute(name);
                } else {
                    Toast.makeText(getActivity(), R.string.backup_not_saved, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    private void renameBackup(final String current) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View v = getActivity().getLayoutInflater().inflate(R.layout.backup_theme_name, null);
        final EditText editTextBackupName = (EditText) v.findViewById(R.id.backup_name);
        editTextBackupName.setText(current);
        builder.setTitle(R.string.dlg_rename_backup_title);
        builder.setView(v);
        builder.setNegativeButton(R.string.dlg_backup_theme_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.dlg_backup_theme_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CharSequence name = editTextBackupName.getText();
                // hide the onscreen keyboard if it's visible
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editTextBackupName.getWindowToken(), 0);
                if (!TextUtils.isEmpty(name)) {
                    File f = new File(Globals.BACKUP_PATH + File.separator + current + ".ctz");
                    f.renameTo(new File(Globals.BACKUP_PATH + File.separator + name + ".ctz"));
                    updateList();
                }
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void applyBackup(String backupFileName) throws IOException {
        String path = Globals.BACKUP_PATH + File.separator + backupFileName;
        ZipFile zipFile = new ZipFile(path);
        boolean hasFonts = zipFile.getEntry("fonts") != null;
        boolean hasBootani = zipFile.getEntry("boots") != null;
        zipFile.close();
        if (hasFonts) {
            displayThemeFontDialog(path, true);
        } else if (hasBootani) {
            displayBootAnimationFontDialog(path, false);
        } else {
            applyBackup(path, false, false, true);
        }
    }

    private void displayThemeFontDialog(final String path, final boolean hasBootAni) {
        SimpleDialogs.displayYesNoDialog(getString(R.string.dlg_apply_theme_with_font_and_reboot),
                getString(R.string.dlg_apply_theme_with_font_without_reboot),
                getString(R.string.dlg_apply_theme_with_font_title),
                getString(R.string.dlg_apply_theme_with_font_body),
                getActivity(),
                new SimpleDialogs.OnYesNoResponse() {
                    @Override
                    public void onYesNoResponse(boolean isYes) {
                        if (hasBootAni)
                            displayBootAnimationFontDialog(path, isYes);
                        else
                            applyBackup(path, false, isYes, true);
                    }
                });
    }

    private void displayBootAnimationFontDialog(final String path, final boolean applyFont) {
        SimpleDialogs.displayYesNoDialog(getString(R.string.dlg_scale_boot_with_scaling),
                getString(R.string.dlg_scale_boot_no_scaling),
                getString(R.string.dlg_scale_boot_title),
                getString(R.string.dlg_scale_boot_body),
                getActivity(),
                new SimpleDialogs.OnYesNoResponse() {
                    @Override
                    public void onYesNoResponse(boolean isYes) {
                        applyBackup(path, applyFont, isYes, true);
                    }
                });
    }

    private void applyBackup(String theme, boolean applyFont, boolean scaleBoot, boolean removeExistingTheme) {
        IThemeManagerService ts = IThemeManagerService.Stub.asInterface(ServiceManager.getService("ThemeService"));
        try {
            ts.applyTheme(FileProvider.CONTENT_BACKUP + FileUtils.stripPath(theme), new ArrayList<String>(),
                    applyFont, scaleBoot, removeExistingTheme);
            showWorkingDialog(getString(R.string.restoring_theme));
            IntentFilter filter = new IntentFilter();
            filter.addAction(Globals.ACTION_THEME_APPLIED);
            filter.addAction(Globals.ACTION_THEME_NOT_APPLIED);
            getActivity().registerReceiver(mBroadcastReceiver, filter);
        } catch (Exception e) {
            SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body,
                    getActivity());
        }
    }

    private void showWorkingDialog(String info) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        IndeterminateProgressDialogFragment dialogFragment =
                IndeterminateProgressDialogFragment.newInstance(info);
        dialogFragment.show(ft, "dialog");
    }

    private void dismissWorkingDialog() {
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            getFragmentManager().popBackStack();
        }
    }

    private void backupCurrentTheme(CharSequence name) {
        String zipFileName = Globals.BACKUP_PATH + File.separator + name + ".ctz";
        byte[] buffer = new byte[1024];

        try{
            File f = new File(zipFileName);
            if (f.exists())
                f.delete();
            FileOutputStream fos = new FileOutputStream(f);
            ZipOutputStream zos = new ZipOutputStream(fos);
            zos.setLevel(9);
            List<String> fileList = new ArrayList<String>();
            generateFileList(fileList, new File(Globals.DATA_THEME_PATH), Globals.DATA_THEME_PATH);

            for(String file : fileList) {
                if (!file.contains("preview")) {
                    try {
                        ZipEntry ze= new ZipEntry(file);
                        zos.putNextEntry(ze);

                        FileInputStream in =
                                new FileInputStream(Globals.DATA_THEME_PATH + File.separator + file);

                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }

                        in.close();
                    } catch (FileNotFoundException e) {
                        // usually happens when access is denied so just move on
                    }
                }
            }

            fileList.clear();
            generateFileList(fileList, new File(Globals.SYSTEM_FONT_PATH), Globals.SYSTEM_FONT_PATH);
            for(String file : fileList) {
                try {
                    ZipEntry ze= new ZipEntry("fonts" + File.separator + file);
                    zos.putNextEntry(ze);

                    FileInputStream in =
                            new FileInputStream(Globals.SYSTEM_FONT_PATH + File.separator + file);

                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }

                    in.close();
                } catch (FileNotFoundException e) {
                    // usually happens when access is denied so just move on
                }
            }
            zos.closeEntry();
            //remember close it
            zos.close();

        }catch(IOException ex){
            Log.e(TAG, "Shazbot!", ex);
        }
    }

    /**
     * Traverse a directory and get all files,
     * and add the file into fileList
     * @param node file or directory
     * @param root root path
     * @return List of entries to add to zip
     */
    private void generateFileList(List<String> fileList, File node, String root) {
        if (fileList == null)
            return;
        //add file only
        if(node.isFile()) {
            fileList.add(generateZipEntry(node.getAbsoluteFile().toString(), root));
        }

        if(node.isDirectory()){
            String[] subNode = node.list();
            for(String filename : subNode) {
                generateFileList(fileList, new File(node, filename), root);
            }
        }
    }

    /**
     * Format the file path for zip
     * @param file file path
     * @root root path to exclude from entry name
     * @return Formatted file path
     */
    private String generateZipEntry(String file, String root) {
        return file.substring(root.length()+1, file.length());
    }

    class BackupThemeTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            CharSequence name = (CharSequence) params[0];
            backupCurrentTheme(name);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showWorkingDialog(getString(R.string.saving_theme));
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            getFragmentManager().popBackStack();
            updateList();
        }
    }

    private void overwriteBackup(final int index) {
        SimpleDialogs.displayYesNoDialog(getString(R.string.dlg_overwrite_backup_overwrite),
                getString(R.string.dlg_overwrite_backup_cancel),
                getString(R.string.dlg_overwrite_backup_title),
                String.format(getString(R.string.dlg_overwrite_backup_body),
                        FileUtils.stripExtension(mBackupList[index].toString())),
                getActivity(), new SimpleDialogs.OnYesNoResponse() {
                    @Override
                    public void onYesNoResponse(boolean isYes) {
                        if (isYes)
                            (new BackupThemeTask()).execute(FileUtils.stripExtension(mBackupList[index]));
                    }
                });
    }

    private void deleteBackupWithConfirmation(final int index) {
        SimpleDialogs.displayYesNoDialog(getString(R.string.dlg_delete_backup_delete),
                getString(R.string.dlg_delete_backup_cancel),
                getString(R.string.dlg_delete_backup_title),
                String.format(getString(R.string.dlg_delete_backup_body),
                        FileUtils.stripExtension(mBackupList[index].toString())),
                getActivity(), new SimpleDialogs.OnYesNoResponse() {
            @Override
            public void onYesNoResponse(boolean isYes) {
                if (isYes)
                    deleteBackup(index);
            }
        });
    }

    class BackupThemeAdapter extends BaseAdapter {
        private DateFormat mDateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, Locale.getDefault());

        @Override
        public int getCount() {
            return mBackupList.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.backup_list_item, null);
            }

            String backupName = mBackupList[position].toString();
            TextView tv = (TextView) convertView.findViewById(R.id.name);
            tv.setText(FileUtils.stripExtension(backupName));

            File backupFile = new File(Globals.BACKUP_PATH + File.separator + mBackupList[position]);
            tv = (TextView) convertView.findViewById(R.id.date_saved);
            tv.setText(String.format(getString(R.string.backup_saved_on,
                    mDateFormat.format(new Date(backupFile.lastModified())))));

            float size = backupFile.length() / (1024f * 1024f);
            tv = (TextView) convertView.findViewById(R.id.size);
            tv.setText(String.format(getString(R.string.backup_size, size)));

            ImageView iv = (ImageView) convertView.findViewById(R.id.save);
            iv.setTag(Integer.valueOf(position));
            iv.setOnClickListener(mSaveButtonListener);

            iv = (ImageView) convertView.findViewById(R.id.delete);
            iv.setTag(Integer.valueOf(position));
            iv.setOnClickListener(mDeleteButtonListener);

            convertView.setId(position);

            return convertView;
        }

        View.OnClickListener mSaveButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer index = (Integer) v.getTag();
                overwriteBackup(index);
            }
        };

        View.OnClickListener mDeleteButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer index = (Integer) v.getTag();
                deleteBackupWithConfirmation(index);
            }
        };
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Globals.ACTION_THEME_APPLIED.equals(action)) {
                dismissWorkingDialog();
            } else if (Globals.ACTION_THEME_NOT_APPLIED.equals(action)) {
                dismissWorkingDialog();
                SimpleDialogs.displayOkDialog(R.string.dlg_theme_failed_title, R.string.dlg_theme_failed_body,
                        getActivity());
            }
            context.unregisterReceiver(this);
        }
    };
}
