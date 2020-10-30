package com.jccode.mycorrespondence;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jccode.mycorrespondence.Adapter.AttachmentAdapter;

import java.util.ArrayList;

import static com.jccode.mycorrespondence.utility.Util.IMAGE;

public class PdfScreen extends AppCompatActivity {


    String Type;
    ArrayList<String> imageList;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    AttachmentAdapter adapter;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.pdf_screen_toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar5);
        recyclerView = (RecyclerView) findViewById(R.id.attachment_rec);

        progressDialog=new ProgressDialog(this);

        if (getIntent().hasExtra("TYPE")) {
            progressDialog.setMessage("please wait....");
            progressDialog.show();
            Type = getIntent().getStringExtra("TYPE");
            setToolbar(toolbar, Type);
            if (Type.equalsIgnoreCase(IMAGE)) {
                imageList = getIntent().getStringArrayListExtra("images");
                loadImages(imageList);
            } else {
                progressBar.setVisibility(View.GONE);
                imageList = getIntent().getStringArrayListExtra("pdfs");
                loadPdf(imageList);
            }
        }

    }

    private void loadPdf(ArrayList<String> imageList) {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
        adapter = new AttachmentAdapter(imageList, this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        progressDialog.dismiss();
    }

    private void loadImages(ArrayList<String> imageList) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new AttachmentAdapter(imageList, this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        progressDialog.dismiss();
    }

    private void setToolbar(Toolbar toolbar, String id) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(id);
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}
