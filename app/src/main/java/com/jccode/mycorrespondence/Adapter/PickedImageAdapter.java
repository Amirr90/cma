package com.jccode.mycorrespondence.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jccode.mycorrespondence.R;

import java.util.ArrayList;

public class PickedImageAdapter extends RecyclerView.Adapter<PickedImageAdapter.MyViewHolder> {
    ArrayList<String> imagesEncodedList;

    public PickedImageAdapter(ArrayList<String> imagesEncodedList) {
        this.imagesEncodedList = imagesEncodedList;
    }

    @NonNull
    @Override
    public PickedImageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picked_image_view, parent, false);
        return new PickedImageAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PickedImageAdapter.MyViewHolder holder, final int position) {

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagesEncodedList.get(position));
            holder.imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.remove_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = position;
                imagesEncodedList.remove(pos);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagesEncodedList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, remove_image_btn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.picked_imageView);
            remove_image_btn = (ImageView) itemView.findViewById(R.id.picked_imageView_close);
        }
    }
}
