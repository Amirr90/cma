package com.jccode.mycorrespondence;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import retrofit2.http.Url;

public class PDFViewer extends AppCompatActivity {
    PDFView pdfView;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p_d_f_viewer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.pdf_viewer_toolbar);

        pdfView = (PDFView) findViewById(R.id.pdf_view);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading pdf file....");
        setToolbar(toolbar);

        if (getIntent().hasExtra("pdf_url")) {
            String url = getIntent().getStringExtra("pdf_url");
            setPdfView(url);

        }


    }

    private void setPdfView(String url) {
        try {
            progressDialog.show();
            new RetrievePdfStreams().execute(url);
        } catch (Exception pdfView) {
            progressDialog.dismiss();
        }
    }


    private void setToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("PDF file");
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public class RetrievePdfStreams extends AsyncTask<String, Void, InputStream> {

        @Override
        protected InputStream doInBackground(String... strings) {

            InputStream inputStream = null;
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if (urlConnection.getResponseCode() == 200)
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                else
                    Toast.makeText(PDFViewer.this, "" + urlConnection.getResponseCode(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                progressDialog.dismiss();
                Toast.makeText(PDFViewer.this, ""+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                return null;
            }
            return inputStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            pdfView.fromStream(inputStream).load();
            progressDialog.dismiss();

        }
    }
}
