package com.jccode.mycorrespondence;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FileDownloadTask;
import com.jccode.mycorrespondence.Adapter.SliderAdapterExample;
import com.jccode.mycorrespondence.Adapter.SliderAdapterExample2;
import com.jccode.mycorrespondence.models.Banner;
import com.smarteist.autoimageslider.SliderView;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FullScreenImageScreen extends AppCompatActivity {

    SliderView sliderView;
    List<Banner> banners = new ArrayList<>();
    DocumentSliderAdapter sliderAdapterExample;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.full_screen_image_toolbar);
        sliderView = (SliderView) findViewById(R.id.full_imageSlider);

        setToolbar(toolbar, "Images");
        ArrayList<String> images = getIntent().getStringArrayListExtra("images");
        for (int a = 0; a < images.size(); a++) {
            banners.add(new Banner(images.get(a), ""));
        }
        sliderAdapterExample = new DocumentSliderAdapter(banners, this);
        sliderView.setSliderAdapter(sliderAdapterExample);
        sliderAdapterExample.notifyDataSetChanged();


    }


    private void setToolbar(Toolbar toolbar, String id) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(id);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public void downloadImageBtn(View view) throws IOException {
        Snackbar.make(view, "coming soon", Snackbar.LENGTH_SHORT).show();
        /*islandRef = storageRef.child("images/island.jpg");

        File localFile = File.createTempFile("images", "jpg");

        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });*/
    }

    private class DocumentSliderAdapter extends SliderViewAdapter<DocumentSliderAdapter.SliderAdapterVH> {

        List<Banner> bannerList;
        private Context context;


        public DocumentSliderAdapter(List<Banner> bannerList, Context context) {
            this.bannerList = bannerList;
            this.context = context;

        }

        @Override
        public DocumentSliderAdapter.SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
            return new SliderAdapterVH(inflate);
        }

        @Override
        public void onBindViewHolder(DocumentSliderAdapter.SliderAdapterVH viewHolder, final int position) {
            viewHolder.textViewDescription.setText("This is slider item " + position);
            String image_url = bannerList.get(position).getImage();
            if (!image_url.equals("") && image_url != null)
                Picasso.with(context).load(image_url).into(viewHolder.imageViewBackground);


            viewHolder.imageViewBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "id " + bannerList.get(position).getId(), Toast.LENGTH_SHORT).show();
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

    {
    }
}
