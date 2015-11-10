package baranek.vojtech.ftpclient;

import android.content.ContextWrapper;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.devpaul.filepickerlibrary.FilePickerActivity;
import com.pixplicity.easyprefs.library.Prefs;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements FolderChooserDialog.FolderCallback {
    @Bind(R.id.etCesta)
    TextView etCesta;
    @Bind(R.id.btnPridatAdr)
    Button btnPridatAdr;
    @Bind(R.id.btnNahratSoubor)
    Button btnNahratSoubor;


    private String FTP_HOST, FTP_USER, FTP_PASS;
    private String chooserPath = "sdcard/Download";
    @Bind(R.id.recyclerViewMain)
    RecyclerView recyclerViewMain;
    @Bind(R.id.progress)
    ProgressBar progress;
    private EditText etNazev, etPw, etAdr;

    private boolean isFirstime = true;


    private MyRecyclerAdapter adapter;
    private FTPFile[] files;
    private String strPath = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ThemChanger.onActivityCreateSetTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        GetLastPreferences();
        recyclerViewMain.setLayoutManager(new LinearLayoutManager(this));


        /**
         * Open configuration dialog if its first app run
         * */

        if (isFirstime) {
            ShowDialog();
            Prefs.putBoolean("prefFirst", false);
        } else {
            LoadIntoDirectory(strPath);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings: {
                ShowDialog();
                break;
            }
            case R.id.acion_pink: {
                ThemChanger.changeToTheme(this, ThemChanger.THEME_PINK);
                break;
            }
            case R.id.action_green: {
                ThemChanger.changeToTheme(this, ThemChanger.THEME_GREEN);
                break;
            }
            case R.id.acion_info: {
                new MaterialDialog.Builder(this)
                        .title(getString(R.string.menu_navod))
                        .content(getString(R.string.usage_info) +
                                getString(R.string.usage_info2) +
                                getString(R.string.usage_info3))
                        .positiveText(R.string.understand)
                        .show();

                break;
            }
            case R.id.action_disconect: {
                disconnectActions();
                break;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    private void disconnectActions() {

        changeButtonsEnability(false);
        strPath = "";
        etCesta.setText(strPath);
        adapter = new MyRecyclerAdapter(getApplicationContext(), new FTPFile[0], MainActivity.this);
        recyclerViewMain.setAdapter(adapter);

        Toast.makeText(getApplicationContext(), R.string.dcd, Toast.LENGTH_LONG).show();

    }

    private void changeButtonsEnability(boolean val) {
        btnNahratSoubor.setEnabled(val);
        btnPridatAdr.setEnabled(val);

    }

    /**
     * Load last setting preferences
     */
    private void GetLastPreferences() {
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        FTP_USER = Prefs.getString("prefUSER", "");
        FTP_PASS = Prefs.getString("prefPass", "");
        FTP_HOST = Prefs.getString("prefHost", "");
        isFirstime = Prefs.getBoolean("prefFirst", true);


    }

    /**
     * Open directory method
     */
    public void LoadIntoDirectory(String dir) {

        new AssyncFtpTask().execute(dir);

    }

    /**
     * Delete file method
     */
    public void DeleteFileFromFtp(String name) {
        new AssyncFtpTaskActions().execute("DELETE", name);


    }

    /**
     * Rename file method
     */
    public void RenameFileFromFtp(String original, String nove) {
        new AssyncFtpTaskActions().execute("RENAME", original, nove);

    }

    /**
     * Download file method
     * */

    private String nazevsouborustringos;

    public void DownloadFtpFile(String nazevSouboru) {
        nazevsouborustringos = nazevSouboru;

    }

    /**
     *Get directory path from path chooser to for download file
     * */
    @Override
    public void onFolderSelection(File file) {
        chooserPath = file.getAbsolutePath() + "/" + nazevsouborustringos;
        new AssyncFtpTaskActions().execute("DOWNLOAD", nazevsouborustringos);
    }

    /**
     *Choose file from phone to download
     */

    @OnClick (R.id.btnNahratSoubor)
    public void ShowFileBrowser() {
        Intent filepiIntent = new Intent(this, FilePickerActivity.class);
        filepiIntent.putExtra(FilePickerActivity.REQUEST_CODE, FilePickerActivity.REQUEST_FILE);
        startActivityForResult(filepiIntent, FilePickerActivity.REQUEST_FILE);
    }

    /**
     Open dialog create new directory, get directory name
     */
    private EditText etNovyNazev;
    @OnClick(R.id.btnPridatAdr)
    public void CreateDir(){

        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.newName)
                .customView(R.layout.rename_layout, false)
                .positiveText(R.string.potvrdit)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {

                        String strDirName = etNovyNazev.getText().toString();
                        CreateNewDir(strDirName);


                    }
                })
                .negativeText(R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                })
                .build();


        etNovyNazev = (EditText) dialog.getCustomView().findViewById(R.id.etNewName);
        dialog.show();

    }

    /**
     *Create new directory method
     */
    private void CreateNewDir(String strDirName) {
        new AssyncFtpTaskActions().execute("CREATEDIR", strDirName);

    }

    /**
     *Get uploading file's path
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FilePickerActivity.REQUEST_FILE
                && resultCode == RESULT_OK) {

            String filePath = data.
                    getStringExtra(FilePickerActivity.FILE_EXTRA_DATA_PATH);
            if (filePath != null) {
                UploadFileToFtp(filePath);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     Upload file to ftp method
     */
    private void UploadFileToFtp(String filePath) {
        new AssyncFtpTaskActions().execute("UPLOADFILE",filePath);
    }

    /**
     Delete directory from ftp
     */
    public void DeleteDirFromFtp(String name) {
        new AssyncFtpTaskActions().execute("DELETEDIR", name);
    }


    /**
     Assync task for ftp directory browsing
     */
    public class AssyncFtpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            FTPClient mFTPClient = new FTPClient();
            Integer result = 0;

            try {

                /**
                 Establish connection to ftp, set passive mod
                 */
                mFTPClient.setControlEncoding("UTF-8");
                mFTPClient.connect(FTP_HOST, 21);
                mFTPClient.login(FTP_USER, FTP_PASS);
                mFTPClient.enterLocalPassiveMode();

                if (params[0].equals("/..")) {
                    /**
                     Move to previous directory
                     */
                    int i = 0;
                    i = strPath.lastIndexOf("/");
                    strPath = strPath.substring(0, i);
                } else {
                    /**
                     Open directory
                     */
                    strPath = strPath + "/" + params[0];
                }
                mFTPClient.changeWorkingDirectory(strPath);
                strPath = mFTPClient.printWorkingDirectory();
                files = mFTPClient.listFiles();
                result = 1;
                mFTPClient.disconnect();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        /**
         * Show progress bar
         */
        @Override
        protected void onPreExecute() {
            recyclerViewMain.animate().alpha(0.5f);
            progress.setVisibility(View.VISIBLE);
            setProgressBarIndeterminateVisibility(true);
        }

        /**
         * Hide progress bar, show files, if successful, show toast if not.
         */
        @Override
        protected void onPostExecute(Integer integer) {
            progress.setVisibility(View.GONE);
            recyclerViewMain.animate().alpha(1.0f);
            if (integer == 1) {
                adapter = new MyRecyclerAdapter(getApplicationContext(), files, MainActivity.this);
                recyclerViewMain.setAdapter(adapter);
                etCesta.setText(strPath);
                changeButtonsEnability(true);

            } else {
                Toast.makeText(getApplicationContext(), R.string.data_load_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Assync task  for operations with files
     */
    public class AssyncFtpTaskActions extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            FTPClient mFTPClient = new FTPClient();
            Integer result = 0;

            try {
                /**
                 Establish connection
                 */
                mFTPClient.setControlEncoding("UTF-8");
                mFTPClient.connect(FTP_HOST, 21);
                mFTPClient.login(FTP_USER, FTP_PASS);
                mFTPClient.enterLocalPassiveMode();
                mFTPClient.changeWorkingDirectory(strPath);

                /**
                 Action with files
                 */

                switch (params[0]) {
                    case "DELETE": {
                        mFTPClient.deleteFile(params[1]);

                        break;
                    }
                    case "RENAME": {
                        mFTPClient.rename(params[1], params[2]);
                        break;
                    }
                    case "DOWNLOAD": {

                        FileOutputStream fos = new FileOutputStream(chooserPath);
                        mFTPClient.retrieveFile(strPath + "/" + params[1], fos);
                        fos.close();
                        break;
                    }
                    case "DELETEDIR": {
                        boolean rem = mFTPClient.removeDirectory(params[1]);

                        break;
                    }
                    case "CREATEDIR": {

                        if (mFTPClient.changeWorkingDirectory(params[1])) {
                            strPath = strPath + "/" + params[1];
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.folderalreadyexists, Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            mFTPClient.makeDirectory(params[1]);
                        }
                        break;
                    }
                    case "UPLOADFILE": {
                        File f  = new File(params[1]);
                        InputStream input = new FileInputStream(f);
                        if (mFTPClient.getReplyCode() != 550) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), R.string.filealreadyExiists, Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            mFTPClient.storeFile(strPath + "/" + f.getName().toString(), input);
                        }
                        break;
                    }
                }
                files = mFTPClient.listFiles();
                result = 1;

                mFTPClient.disconnect();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        /**
         * show progress bar
         */

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
            setProgressBarIndeterminateVisibility(true);
            recyclerViewMain.animate().alpha(0.5f);
        }

        /**
         Show changed files or fail message
         */

        @Override
        protected void onPostExecute(Integer integer) {
            progress.setVisibility(View.GONE);
            recyclerViewMain.animate().alpha(1.0f);
            if (integer == 1) {

                adapter = new MyRecyclerAdapter(getApplicationContext(), files, MainActivity.this);
                recyclerViewMain.setAdapter(adapter);
                etCesta.setText(strPath);


            } else {
                Toast.makeText(getApplicationContext(), R.string.action_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     Show dialog for  connection settings
     */

    private void ShowDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.conn_config)
                .customView(R.layout.dialog_layout, false)
                .positiveText(R.string.connect)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {

                        FTP_HOST = etAdr.getText().toString();
                        FTP_USER = etNazev.getText().toString();
                        FTP_PASS = etPw.getText().toString();

                        SavePreffs();
                        LoadIntoDirectory(strPath);

                    }
                }).build();

        etNazev = (EditText) dialog.getCustomView().findViewById(R.id.dialNazev);
        etNazev.setText(FTP_USER);
        etPw = (EditText) dialog.getCustomView().findViewById(R.id.dialPw);
        etPw.setText(FTP_PASS);
        etAdr = (EditText) dialog.getCustomView().findViewById(R.id.dialAdr);
        etAdr.setText(FTP_HOST);

        dialog.show();
    }

    /***
     save prefferences
     */

    private void SavePreffs() {
        Prefs.putString("prefUSER", FTP_USER);
        Prefs.putString("prefPass", FTP_PASS);
        Prefs.putString("prefHost", FTP_HOST);
    }

}
