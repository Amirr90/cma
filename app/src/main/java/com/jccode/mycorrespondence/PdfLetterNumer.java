package com.jccode.mycorrespondence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.jccode.mycorrespondence.Adapter.AttachmentAdapter;
import com.jccode.mycorrespondence.utility.Util;

import java.util.ArrayList;
import java.util.List;

import static com.jccode.mycorrespondence.utility.Util.PDF_ATTACHMENT;

public class PdfLetterNumer extends AppCompatActivity {

    String Type, Id;
    ArrayList<String> imageList;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    PdfAdapter adapter;
    ProgressDialog progressDialog;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    List<DocumentSnapshot> snapshotList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_letter_numer);


        Toolbar toolbar = (Toolbar) findViewById(R.id.pdf_screen_toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar5);
        recyclerView = (RecyclerView) findViewById(R.id.attachment_rec);


        progressDialog = new ProgressDialog(this);

        if (getIntent().hasExtra("ID")) {
            Id = getIntent().getStringExtra("ID");
            loadData();
        }
    }

    private void loadData() {
        firestore.collection(Util.CORRESPONDENCE_QUERY)
                .document(Id)
                .collection(PDF_ATTACHMENT)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty())
                            return;

                    }
                });


    }

    private class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.MyAdapter> {
        Context context;
        List<DocumentSnapshot> snapshots;

        public PdfAdapter(Context context, List<DocumentSnapshot> snapshots) {
            this.context = context;
            this.snapshots = snapshots;
        }

        @NonNull
        @Override
        public PdfAdapter.MyAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_parent_view, parent, false);
            return new MyAdapter(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PdfAdapter.MyAdapter holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        public class MyAdapter extends RecyclerView.ViewHolder {
            private TextView textView, mLetterTime, mLetterType;

            public MyAdapter(@NonNull View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.textView);
                mLetterTime = (TextView) itemView.findViewById(R.id.time2);
                mLetterType = (TextView) itemView.findViewById(R.id.type2);
            }
        }
    }
}
