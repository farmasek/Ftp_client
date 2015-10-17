package baranek.vojtech.ftpclient;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPFile;

/**
 * Created by Farmas on 17.10.2015.
 */
public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.CustomViewHolder> {

    private  FTPFile[] files;
    private Context c ;
    private MainActivity mAct;

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

            }
        };

        holder.tv1.setOnClickListener(clickListener);
        holder.imageView.setOnClickListener(clickListener);

        holder.imageView.setTag(holder);
        holder.tv1.setTag(holder);


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

