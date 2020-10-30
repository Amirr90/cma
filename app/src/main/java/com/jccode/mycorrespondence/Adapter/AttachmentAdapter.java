package com.jccode.mycorrespondence.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.jccode.mycorrespondence.PDFViewer;
import com.jccode.mycorrespondence.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.MyViewHolder> {
    ArrayList<String> list;
    Context context;

    public AttachmentAdapter(ArrayList<String> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public AttachmentAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_reply_attachment, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AttachmentAdapter.MyViewHolder holder, final int position) {
        String url = list.get(position);
        int po = position + 1;
        holder.name.setText("" + po + ".pdf");
    /*    if (url != null && !url.equalsIgnoreCase(""))
            Picasso.with(context).load(url).into(holder.imageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    holder.imageView.setImageResource(R.drawable.document);
                    int pos = position + 1;
                    // holder.view.setText("Document " + pos);
                }
            });*/

        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = list.get(position);
            }
        });

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = list.get(position);
                if (url != null)
                    context.startActivity(new Intent(context, PDFViewer.class).putExtra("pdf_url", url));
            }
        });


    }


    public void displaypdf() {
        final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyPdf/abc.pdf";
        File pdfFile = new File(filePath);
        Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
        pdfIntent.setDataAndType(Uri.fromFile(pdfFile), "application/pdf");
        pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        //Create Viewer Intent
        Intent viewerIntent = Intent.createChooser(pdfIntent, "Open PDF");
        context.startActivity(viewerIntent);
    }

    private void showDialog(final MyViewHolder holder, int position) {

        final String url = list.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        final AlertDialog dialog = builder.create();
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogLayout = inflater.inflate(R.layout.go_pro_dialog_layout, null);
        dialog.setView(dialogLayout);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ImageView image = (ImageView) dialog.findViewById(R.id.goProDialogImage);
        if (url != null && !url.equalsIgnoreCase(""))
            Picasso.with(context).load(url).into(image);
        dialog.show();

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView name;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            name = (TextView) itemView.findViewById(R.id.btn_download);
        }
    }


}
