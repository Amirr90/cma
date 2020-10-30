package com.jccode.mycorrespondence.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.jccode.mycorrespondence.FullScreenImageScreen;
import com.jccode.mycorrespondence.R;
import com.jccode.mycorrespondence.ShowComposeDetail;
import com.jccode.mycorrespondence.ViewAllReplyScreen;
import com.jccode.mycorrespondence.models.Banner;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SliderAdapterExample extends SliderViewAdapter<SliderAdapterExample.SliderAdapterVH> {

    List<Banner> bannerList;
    private Context context;


    public SliderAdapterExample(List<Banner> bannerList, Context context) {
        this.bannerList = bannerList;
        this.context = context;

    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(final SliderAdapterVH viewHolder, final int position) {
        viewHolder.textViewDescription.setText("This is slider item " + position);
        final String image_url = bannerList.get(position).getImage();
        if (!image_url.equals("") && image_url != null)
            Picasso.with(context).load(image_url).networkPolicy(NetworkPolicy.OFFLINE).into(viewHolder.imageViewBackground, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(context).load(image_url).into(viewHolder.imageViewBackground);
                }
            });

        viewHolder.imageViewBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> image_collection = new ArrayList<>();
                for (int a = 0; a < bannerList.size(); a++) {
                    image_collection.add(bannerList.get(a).getImage());
                }
                context.startActivity(new Intent(context, FullScreenImageScreen.class)
                        .putStringArrayListExtra("images", image_collection));
            }
        });
    }

    @Override
    public int getCount() {
        return bannerList.size();
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imageViewBackground;
        TextView textViewDescription;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            textViewDescription = itemView.findViewById(R.id.tv_auto_image_slider);
            this.itemView = itemView;
        }
    }
}