package baranek.vojtech.ftpclient;

import android.content.ContextWrapper;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.pixplicity.easyprefs.library.Prefs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity  implements FolderChooserDialog.FolderCallback{




 /*   static final String FTP_HOST = "f14-preview.royalwebhosting.net";

    static final String FTP_USER = "1969250";

    static final String FTP_PASS = "ciscoforlife32";*/

    private String FTP_HOST,FTP_USER,FTP_PASS;
    private String chooserPath = "sdcard/Download";
    @Bind(R.id.recyclerViewMain)
    RecyclerView recyclerViewMain;
    @Bind(R.id.progress)
    ProgressBar progress;
    private EditText etNazev, etPw, etAdr ;

    private boolean isFirstime = true;


    private MyRecyclerAdapter adapter;
    private FTPFile[] files;
    private String strPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        GetLastPreferences();
        recyclerViewMain.setLayoutManager(new LinearLayoutManager(this));


        if (isFirstime) {
            ShowDialog();
            Prefs.putBoolean("prefFirst", false);
        }else{
            LoadIntoDirectory(strPath);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater= getMenuInflater();
                inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        ShowDialog();
        return super.onOptionsItemSelected(item);
    }

    private void GetLastPreferences() {
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        FTP_USER= Prefs.getString("prefUSER","");
        FTP_PASS= Prefs.getString("prefPass","");
        FTP_HOST= Prefs.getString("prefHost","");
        isFirstime = Prefs.getBoolean("prefFirst",true);


    }

    public void LoadIntoDirectory(String dir) {

        new AssyncFtpTask().execute(dir);

    }
    public void DeleteFileFromFtp(String name){
        new AssyncFtpTaskActions().execute("DELETE",name);


    }

    public void RenameFileFromFtp(String original,String nove){
        new AssyncFtpTaskActions().execute("RENAME",original,nove);

    }
    private String nazevsouborustringos;
    public void DownloadFtpFile(String nazevSouboru) {
        nazevsouborustringos = nazevSouboru;

    }

    @Override
    public void onFolderSelection(File file) {

        chooserPath= file.getAbsolutePath()+"/"+nazevsouborustringos;

        new AssyncFtpTaskActions().execute("DOWNLOAD",nazevsouborustringos);
    }



    public class AssyncFtpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            FTPClient mFTPClient = new FTPClient();
            Integer result = 0;

            try {

                mFTPClient.setControlEncoding("UTF-8");
                mFTPClient.connect(FTP_HOST, 21);
                mFTPClient.login(FTP_USER, FTP_PASS);
                mFTPClient.enterLocalPassiveMode();
                if (params[0].equals("/..")) {
                    int i = 0;
                    i = strPath.lastIndexOf("/");
                    strPath = strPath.substring(0, i);
                } else {
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

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            progress.setVisibility(View.GONE);
            if (integer == 1) {
                adapter = new MyRecyclerAdapter(getApplicationContext(), files, MainActivity.this);
                recyclerViewMain.setAdapter(adapter);
                Toast.makeText(getApplicationContext(), "Cur path :" + strPath, Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "Nepodařilo se načíst data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class AssyncFtpTaskActions extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            FTPClient mFTPClient = new FTPClient();
            Integer result = 0;

            try {

                mFTPClient.setControlEncoding("UTF-8");
                mFTPClient.connect(FTP_HOST, 21);
                mFTPClient.login(FTP_USER, FTP_PASS);
                mFTPClient.enterLocalPassiveMode();
                mFTPClient.changeWorkingDirectory(strPath);

                switch (params[0]){
                case "DELETE": {
                    mFTPClient.deleteFile(params[1]);

                        break;
                    }
               case "RENAME": {
                   mFTPClient.rename(params[1],params[2]);
                        break;
                    }
               case "DOWNLOAD": {

                   FileOutputStream fos = new FileOutputStream(chooserPath);
                    mFTPClient.retrieveFile(strPath+"/"+params[1],fos);
                   fos.close();
                //   FileOutputStream fos = new FileOutputStream("/sdcard/Download/fuckshit");
                //   mFTPClient.retrieveFile("/test/složka a/wutshit",fos);
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
        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            progress.setVisibility(View.GONE);
            if (integer == 1) {
                adapter = new MyRecyclerAdapter(getApplicationContext(), files, MainActivity.this);
                recyclerViewMain.setAdapter(adapter);
                Toast.makeText(getApplicationContext(), "Cur path :" + strPath, Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "Nepodařilo se provést požadovanou akci", Toast.LENGTH_SHORT).show();
            }
        }}



    private void ShowDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("Kakdela")
                .customView(R.layout.dialog_layout, false)
                .positiveText("Ok")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {

                        FTP_HOST= etAdr.getText().toString();
                        FTP_USER= etNazev.getText().toString();
                        FTP_PASS= etPw.getText().toString();

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

    private void SavePreffs() {


        Prefs.putString("prefUSER", FTP_USER);
        Prefs.putString("prefPass", FTP_PASS);
        Prefs.putString("prefHost", FTP_HOST);

    }

}
