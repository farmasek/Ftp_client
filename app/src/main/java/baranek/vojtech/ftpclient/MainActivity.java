package baranek.vojtech.ftpclient;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.net.SocketException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {


    @Bind(R.id.button)
    Button button;

    /*********
     * work only for Dedicated IP
     ***********/
    static final String FTP_HOST = "f14-preview.royalwebhosting.net";

    /*********
     * FTP USERNAME
     ***********/
    static final String FTP_USER = "1969250";

    /*********
     * FTP PASSWORD
     ***********/
    static final String FTP_PASS = "ciscoforlife32";
    @Bind(R.id.recyclerViewMain)
    RecyclerView recyclerViewMain;
    @Bind(R.id.progress)
    ProgressBar progress;


    private MyRecyclerAdapter adapter;
    private  FTPFile[] files;
private String strPath ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        recyclerViewMain.setLayoutManager(new LinearLayoutManager(this));
        strPath = "";
        LoadIntoDirectory(strPath);

    }

    public void LoadIntoDirectory(String dir){

    new AssyncFtpTask().execute(dir);

    }

    public class AssyncFtpTask extends AsyncTask<String, Void, Integer>{

        @Override
        protected Integer doInBackground(String... params) {
         FTPClient  mFTPClient = new FTPClient();
            Integer result = 0;

            try {

                mFTPClient.setControlEncoding("UTF-8");
                mFTPClient.connect(FTP_HOST, 21);
                mFTPClient.login(FTP_USER, FTP_PASS);
                mFTPClient.enterLocalPassiveMode();
                if (params[0].equals("/.."))
                    {int i = 0;
                    i = strPath.lastIndexOf("/");
                    strPath = strPath.substring(0,i);}
                else {
                    strPath = strPath +"/"+params[0];
                }
                mFTPClient.changeWorkingDirectory(strPath);
                strPath = mFTPClient.printWorkingDirectory();
                files=mFTPClient.listFiles();
                 result=1;
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
            if (integer==1){
                adapter = new MyRecyclerAdapter(getApplicationContext(),files, MainActivity.this);
                recyclerViewMain.setAdapter(adapter);
                Toast.makeText(getApplicationContext(), "Cur path :" + strPath, Toast.LENGTH_SHORT).show();

            }
            else{
                Toast.makeText(getApplicationContext(), "Nepodařilo se načíst data", Toast.LENGTH_SHORT).show();
            }
        }}








    @OnClick(R.id.button)
    public void buttonclick() {


    }

    }
