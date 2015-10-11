package baranek.vojtech.ftpclient;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

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
    @Bind(R.id.listView)
    ListView listView;

    private FTPClient mFTPClient;
    public String[] strFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        StrictMode.ThreadPolicy tp = StrictMode.ThreadPolicy.LAX;
        StrictMode.setThreadPolicy(tp);




    }

    @OnClick(R.id.button)
    public void buttonclick() {
        mFTPClient = new FTPClient();
        try {


            mFTPClient.connect(FTP_HOST, 21);
            int reply = mFTPClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                return;
            }

            mFTPClient.login(FTP_USER, FTP_PASS);


            // Use passive mode as default because most of us are
            // behind firewalls these days.
            mFTPClient.enterLocalPassiveMode();


            FTPFile[] files = mFTPClient.listFiles("/test");

            strFiles = new String[files.length];

            for (int i = 0; i < files.length; i++) {
                strFiles[i] = files[i].getName();
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        listView.setAdapter( new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strFiles));

    }


    private void connecttoFTP() throws SocketException, IOException {

        mFTPClient.connect(FTP_HOST, 21);
        int reply = mFTPClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            return;
        }

        mFTPClient.login(FTP_USER, FTP_PASS);


        // Use passive mode as default because most of us are
        // behind firewalls these days.
        mFTPClient.enterLocalPassiveMode();
    }


}
