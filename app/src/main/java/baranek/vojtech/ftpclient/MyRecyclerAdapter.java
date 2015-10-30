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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;

import org.apache.commons.net.ftp.FTPFile;

/**
 * Created by Farmas on 17.10.2015.
 *
 * Custom adapter for RecylclerView for displaying files
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
        /**
         * Set different images for folder / directory
         */
        if (files[position].isDirectory())
        /**
         * Is directory
         */
        {holder.imageView.setBackgroundResource(R.drawable.ic_folder_open_black_48dp);}
        else{
            /**
             * is file
             */
            holder.imageView.setBackgroundResource(R.drawable.ic_insert_drive_file_black_48dp);
        }

        holder.tv1.setText(String.valueOf(files[position].getName()));

        /**
         * Actions for item click
         */
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (files[position].isDirectory())
                /**
                 * Open folder
                 */
                {
                   mAct.LoadIntoDirectory("/"+files[position].getName());
                }
                else
                {
                    /**
                     * Show bottom sheet for file
                     */
                    OpenBottomSheetMenu(position);
                }

            }
        };
        /**
         * Actions for long click on directory
         */
        View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (files[position].isDirectory())
                /**
                 * Open bottom sheet for directory
                 */
                {
                    OpenBottomSheetMenuForDir(position);
                }
                return false;
            }
        };

        holder.tv1.setOnClickListener(clickListener);
        holder.imageView.setOnClickListener(clickListener);

        holder.tv1.setOnLongClickListener(longClickListener);
        holder.imageView.setOnLongClickListener(longClickListener);

        holder.imageView.setTag(holder);
        holder.tv1.setTag(holder);


    }

    /**
     * Bottom sheet for directories actions
     *
     * @param pos position of clicked directory
     */

    private void OpenBottomSheetMenuForDir(int pos) {
        posicionie = pos;
        new BottomSheet.Builder(mAct)
                .setSheet(R.menu.bottomsheet_dirmenu)
                .setTitle(R.string.podrobnosti)
                .setListener(new BottomSheetListener() {
                    @Override
                    public void onSheetShown() {

                    }

                    @Override
                    public void onSheetItemSelected(MenuItem menuItem) {

                        switch (menuItem.getItemId()) {

                            /**
                             * Rename direcotry
                             */
                            case R.id.menRename: {
                                ShowRenameDialog();
                                break;

                            }
                            /**
                             * Delete directory
                             */
                            case R.id.menSmazat: {
                                mAct.DeleteDirFromFtp(files[posicionie].getName());
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

    /**
     * Open bottom sheet for files options
     *
     * @param pos position of file
     */

    private void OpenBottomSheetMenu(int pos) {

        posicionie = pos;
        new BottomSheet.Builder(mAct)
                .setSheet(R.menu.bottomsheet_filemenu)
                .setTitle(R.string.podrobnosti)
                .setListener(new BottomSheetListener() {
                    @Override
                    public void onSheetShown() {

                    }

                    @Override
                    public void onSheetItemSelected(MenuItem menuItem) {

                        switch (menuItem.getItemId()) {

                            /**
                             * Download file, show folder chooser dialog
                             */
                            case R.id.menStahnout :{

                                ShowDownloadDialog();
                                mAct.DownloadFtpFile(files[posicionie].getName());

                                break;

                                /**
                                 * Rename file
                                 */
                            }case R.id.menRename :{

                                ShowRenameDialog();

                                break;

                                /**
                                 * Delete file
                                 */
                            }case R.id.menSmazat :{

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

    /**
     * Show dialog for download method
     */
    private void ShowDownloadDialog() {

        final FolderChooserDialog folderrCh = new FolderChooserDialog.Builder(mAct)
                .chooseButton(R.string.potvrdit)  // changes label of the choose button
                .initialPath("/sdcard/Download")  // changes initial path, defaults to external storage directory
                .show();
    }

    private EditText etNovyNazev;
    private String strNovyNazev;

    /**
     * Show dialog for rename, get name and rename
     */

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


    /**
     * Custom view holder for row
     */

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

