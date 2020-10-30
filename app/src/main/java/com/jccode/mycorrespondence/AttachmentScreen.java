package com.jccode.mycorrespondence;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static com.jccode.mycorrespondence.utility.Util.ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_QUERY;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.IMAGE_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE;


public class AttachmentScreen extends AppCompatActivity {
    private static final String TAG = "AttachmentScreen";
    public String Id;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    List<DocumentSnapshot> attachmentList;
    AttachmentAdapter adapter;
    FirebaseAuth auth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attachment_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.attachment_toolbar);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (getIntent().hasExtra(ID)) {
            Id = getIntent().getStringExtra(ID);
            setToolbar(toolbar, "Attachment");
            loadHomeAttachmentData();
        }
    }

    private void loadHomeAttachmentData() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        recyclerView = (RecyclerView) findViewById(R.id.attachment_rec);
        attachmentList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new AttachmentAdapter(attachmentList, this);
        recyclerView.setAdapter(adapter);
        loadData(adapter);

    }

    private void loadData(final AttachmentAdapter adapter) {
        firestore.collection(CORRESPONDENCE_QUERY)
                .document(Id)
                .collection(IMAGE_ATTACHMENT)
                .whereEqualTo(IS_ACTIVE, true)
                //.orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        progressBar.setVisibility(View.GONE);
                        if (e == null) {
                            if (queryDocumentSnapshots != null) {
                                if (queryDocumentSnapshots.size() != 0) {
                                    attachmentList.clear();
                                    attachmentList.addAll(queryDocumentSnapshots.getDocuments());
                                    adapter.notifyDataSetChanged();

                                } else {
                                    Toast.makeText(AttachmentScreen.this, "image not found", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                    finish();
                                }

                            }

                        } else {
                            Log.d(TAG, "onEvent: " + e.getLocalizedMessage());
                            Toast.makeText(AttachmentScreen.this, "load again: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
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

    private class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.MyViewHolder> {
        List<DocumentSnapshot> list;
        Context context;

        public AttachmentAdapter(List<DocumentSnapshot> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @NonNull
        @Override
        public AttachmentAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attachment_view, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final AttachmentAdapter.MyViewHolder holder, int position) {
            String imageUrl = list.get(position).getString(ATTACHMENT);
            Picasso.with(context).load(imageUrl).placeholder(R.drawable.ic_launcher_foreground).into(holder.imageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    holder.imageView.setImageResource(R.drawable.ic_launcher_foreground);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.attachment_image);
            }
        }
    }
}

