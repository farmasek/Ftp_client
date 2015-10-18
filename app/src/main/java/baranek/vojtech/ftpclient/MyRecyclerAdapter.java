package baranek.vojtech.ftpclient;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;

import org.apache.commons.net.ftp.FTPFile;

/**
 * Created by Farmas on 17.10.2015.
 */
public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.CustomViewHolder>  {

    private int posicionie;
    private  FTPFile[] files;
    private Context c ;
    private MainActivity mAct =null;

    public MyRecyclerAdapter(Context c,  FTPFile[] files, MainActivity mAct) {
        this.c=c;
        this.files = files;
        this.mAct = mAct;

    }

    @Override
    public MyRecyclerAdapter.CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       View View = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_row,null);
        CustomViewHolder viewHolder = new CustomViewHolder(View);
        return  viewHolder;


    }

    @Override
    public void onBindViewHolder(final MyRecyclerAdapter.CustomViewHolder holder, final int position) {
        if (files[position].isDirectory())
            //je složka
        {holder.imageView.setBackgroundResource(R.drawable.ic_folder_open_black_48dp);}
        else{
            // je soubor
            holder.imageView.setBackgroundResource(R.drawable.ic_insert_drive_file_black_48dp);
        }

        holder.tv1.setText(String.valueOf(files[position].getName()));

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (files[position].isDirectory())
                //je složka
                {

                    mAct.LoadIntoDirectory("/"+files[position].getName());

                }
                else
                {
                    OpenBottomSheetMenu(position);
                    
                }

            }
        };

        holder.tv1.setOnClickListener(clickListener);
        holder.imageView.setOnClickListener(clickListener);

        holder.imageView.setTag(holder);
        holder.tv1.setTag(holder);


    }

    private void OpenBottomSheetMenu(int pos) {

        posicionie = pos;
        new BottomSheet.Builder(mAct)
                .setSheet(R.menu.bottomsheet_filemenu)
                .setTitle("Podrobnosti")
                .setListener(new BottomSheetListener() {
                    @Override
                    public void onSheetShown() {

                    }

                    @Override
                    public void onSheetItemSelected(MenuItem menuItem) {

                        switch (menuItem.getItemId()) {

                            case R.id.menStahnout :{

                                Toast.makeText(mAct, "stahnout mejt" , Toast.LENGTH_SHORT).show();
                                ShowDownloadDialog();
                                mAct.DownloadFtpFile(files[posicionie].getName());

                                break;

                            }case R.id.menRename :{
                                Toast.makeText(mAct, "renejm mejt" , Toast.LENGTH_SHORT).show();
                                ShowRenameDialog();

                                break;

                            }case R.id.menSmazat :{
                                Toast.makeText(mAct, "smahnout mejt" , Toast.LENGTH_SHORT).show();
                                mAct.DeleteFileFromFtp(files[posicionie].getName());
                                break;
                        }


                }




                    }

                    @Override
                    public void onSheetDismissed(int i) {

                    }
                })
                .show();



    }

    private void ShowDownloadDialog() {

        final FolderChooserDialog folderrCh = new FolderChooserDialog.Builder(mAct)
                .chooseButton(R.string.potvrdit)  // changes label of the choose button
                .initialPath("/sdcard/Download")  // changes initial path, defaults to external storage directory
                .show();
    }

    private EditText etNovyNazev;
    private String strNovyNazev;

    private void ShowRenameDialog() {

            final MaterialDialog dialog = new MaterialDialog.Builder(mAct)
                    .title("Nový název")
                    .customView(R.layout.rename_layout, false)
                    .positiveText("Ok")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {

                            strNovyNazev = etNovyNazev.getText().toString();
                            mAct.RenameFileFromFtp(files[posicionie].getName(),strNovyNazev);

                        }
                    })
                    .negativeText("Zrušit")
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

    @Override
    public int getItemCount() {
        int i = files.length;
        return i;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder{

        protected TextView tv1 ;
        protected ImageView imageView;

        public CustomViewHolder(View itemView) {
            super(itemView);
            this.tv1 = (TextView)itemView.findViewById(R.id.title);
            this.imageView = (ImageView)itemView.findViewById(R.id.type);
        }
    }

}

