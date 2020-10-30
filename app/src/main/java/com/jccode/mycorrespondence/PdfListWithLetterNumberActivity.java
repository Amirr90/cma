package com.jccode.mycorrespondence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jccode.mycorrespondence.utility.TimeAgo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.jccode.mycorrespondence.utility.Util.ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.COMPOSE;
import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DATE;
import static com.jccode.mycorrespondence.utility.Util.DOCUMENT;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.IMAGE_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.LETTER_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.PDF_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.SHARED_ACTIVITY;
import static com.jccode.mycorrespondence.utility.Util.SHARED_DOCUMENT;
import static com.jccode.mycorrespondence.utility.Util.SHARED_DOCUMENT_QUERY;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TYPE;

public class PdfListWithLetterNumberActivity extends AppCompatActivity {
    private static final String TAG = "PdfListWithLetterNumberActivity";
    ProgressDialog progressDialog;
    String CId, from;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    List<DocumentSnapshot> snapshots = new ArrayList<>();
    ArrayList<String> imageList0 = new ArrayList<>();
    ArrayList<String> imageList1 = new ArrayList<>();
    ArrayList<String> imageList2 = new ArrayList<>();

    RecyclerView rvParent;
    TimeAgo timeAgo;
    ParentAdapter adapter;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_list_with_letter_number);

        Toolbar toolbar = (Toolbar) findViewById(R.id.pdf_list_toolbar);

        CId = getIntent().getStringExtra("c_id");
        user = FirebaseAuth.getInstance().getCurrentUser();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading pdf file....");

        adapter = new ParentAdapter(this, snapshots);
        rvParent = (RecyclerView) findViewById(R.id.rv_parent_pdf);
        rvParent.setLayoutManager(new LinearLayoutManager(this));
        rvParent.setHasFixedSize(true);
        rvParent.setAdapter(adapter);

        timeAgo = new TimeAgo();
        setToolbar(toolbar);

        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        setParentAdapter();
    }

    private void setParentAdapter() {
        from = getIntent().getStringExtra(FROM);
        progressDialog.show();
        snapshots.clear();
        if (from.equalsIgnoreCase(SHARED_ACTIVITY)) {
            firestore.collection("Users")
                    .document(user.getUid())
                    .collection(SHARED_DOCUMENT_QUERY)
                    .document(CId)
                    .collection(PDF_ATTACHMENT)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> snapshots1 = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : snapshots1) {
                                    imageList0.add(documentSnapshot.getString(DOCUMENT));
                                }
                            }


                            firestore
                                    .collection("Users")
                                    .document(user.getUid())
                                    .collection(SHARED_DOCUMENT_QUERY)
                                    .document(CId)
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    snapshots.add(documentSnapshot);
                                    adapter.notifyDataSetChanged();
                                    firestore
                                            .collection("Users")
                                            .document(user.getUid())
                                            .collection(SHARED_DOCUMENT_QUERY)
                                            .document(CId)
                                            .collection(COMPOSE).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            if (queryDocumentSnapshots.isEmpty()) {
                                                progressDialog.dismiss();
                                                return;
                                            }


                                            snapshots.addAll(queryDocumentSnapshots.getDocuments());
                                            adapter.notifyDataSetChanged();
                                            progressDialog.dismiss();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            adapter.notifyDataSetChanged();
                                            progressDialog.dismiss();
                                        }
                                    });
                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(PdfListWithLetterNumberActivity.this, "try again", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            firestore.collection(CORRESPONDENCE_QUERY)
                    .document(CId)
                    .collection(PDF_ATTACHMENT)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                List<DocumentSnapshot> snapshots1 = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot documentSnapshot : snapshots1) {
                                    imageList0.add(documentSnapshot.getString(DOCUMENT));
                                }
                            }


                            firestore.collection(CORRESPONDENCE_QUERY).document(CId)
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    snapshots.add(documentSnapshot);
                                    adapter.notifyDataSetChanged();
                                    firestore.collection(CORRESPONDENCE_QUERY).document(CId)
                                            .collection(COMPOSE).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            if (queryDocumentSnapshots.isEmpty()) {
                                                progressDialog.dismiss();
                                                return;
                                            }


                                            snapshots.addAll(queryDocumentSnapshots.getDocuments());
                                            adapter.notifyDataSetChanged();
                                            progressDialog.dismiss();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            adapter.notifyDataSetChanged();
                                            progressDialog.dismiss();
                                        }
                                    });
                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(PdfListWithLetterNumberActivity.this, "try again", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public class ParentAdapter extends
            RecyclerView.Adapter<ParentAdapter.MyParentViewHolder> {

        Context context;
        List<DocumentSnapshot> snapshots;

        public ParentAdapter(Context context, List<DocumentSnapshot> snapshots) {
            this.context = context;
            this.snapshots = snapshots;
            imageList1 = getIntent().getStringArrayListExtra("pdfs");
        }

        @NonNull
        @Override
        public ParentAdapter.MyParentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_parent_view, parent, false);
            return new MyParentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ParentAdapter.MyParentViewHolder holder, final int position) {


            final DocumentSnapshot mySnapShots = snapshots.get(position);
            try {
                holder.textView.setText("Letter Number: " + mySnapShots.get(LETTER_NUMBER));
                holder.mLetterType.setText(mySnapShots.getString(TYPE));
                holder.mLetterTime.setText("Letter Date: " + mySnapShots.getString(DATE));
                if (position == 0) {
                    holder.PdfCounter.setText(imageList0.size() + " PDF");
                } else {
                    setImageCounter(holder, mySnapShots.getId());
                }

            } catch (Exception e) {
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (position == 0) {
                        if (imageList0 != null && !imageList0.isEmpty()) {
                            startActivity(new Intent(context, PdfScreen.class)
                                    .putExtra("TYPE", "PDF")
                                    .putExtra("pdfs", imageList0));
                        } else {
                            Snackbar.make(v, "No Pdf ", Snackbar.LENGTH_SHORT).show();
                        }


                    } else {
                        if (!from.equalsIgnoreCase(SHARED_ACTIVITY)) {
                            firestore.collection(CORRESPONDENCE_QUERY).document(CId)
                                    .collection(COMPOSE)
                                    .document(mySnapShots.getId())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (!documentSnapshot.exists())
                                                return;

                                            ArrayList<String> imageList = new ArrayList<>();
                                            if (documentSnapshot.get(PDF_ATTACHMENT) == null) {
                                                progressDialog.dismiss();
                                                Snackbar.make(v, "No Pdf ", Snackbar.LENGTH_SHORT).show();
                                                return;
                                            }

                                            List<Map<String, Object>> maps = (List<Map<String, Object>>) documentSnapshot.get(PDF_ATTACHMENT);
                                            for (int b = 0; b < maps.size(); b++) {
                                                String imageUrl = "" + maps.get(b).get(DOCUMENT).toString();
                                                imageList.add(imageUrl);
                                            }
                                            progressDialog.dismiss();
                                            startActivity(new Intent(context, PdfScreen.class).putExtra("TYPE", "PDF")
                                                    .putExtra("pdfs", imageList));

                                        }
                                    });
                        } else {

                            firestore.collection("Users")
                                    .document(user.getUid())
                                    .collection(SHARED_DOCUMENT_QUERY)
                                    .document(CId)
                                    .collection(COMPOSE)
                                    .document(mySnapShots.getId())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if (!documentSnapshot.exists())
                                                return;

                                            ArrayList<String> imageList = new ArrayList<>();
                                            if (documentSnapshot.get(PDF_ATTACHMENT) == null) {
                                                progressDialog.dismiss();
                                                Snackbar.make(v, "No Pdf ", Snackbar.LENGTH_SHORT).show();
                                                return;
                                            }

                                            List<Map<String, Object>> maps = (List<Map<String, Object>>) documentSnapshot.get(PDF_ATTACHMENT);
                                            for (int b = 0; b < maps.size(); b++) {
                                                String imageUrl = "" + maps.get(b).get(DOCUMENT).toString();
                                                imageList.add(imageUrl);
                                            }
                                            progressDialog.dismiss();
                                            startActivity(new Intent(context, PdfScreen.class).putExtra("TYPE", "PDF")
                                                    .putExtra("pdfs", imageList));

                                        }
                                    });
                        }
                    }
                }
            });
        }


        @Override
        public int getItemCount() {
            return snapshots.size();
        }

        public class MyParentViewHolder extends RecyclerView.ViewHolder {

            RecyclerView rvChild;
            TextView textView, mLetterTime, mLetterType, PdfCounter;

            public MyParentViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.textView);
                mLetterTime = (TextView) itemView.findViewById(R.id.time2);
                mLetterType = (TextView) itemView.findViewById(R.id.type2);
                PdfCounter = (TextView) itemView.findViewById(R.id.textView36);
            }
        }
    }

    private void setToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("PDF Attachment");
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setImageCounter(final ParentAdapter.MyParentViewHolder holder, String docId) {
        try {
            if (!from.equalsIgnoreCase(SHARED_ACTIVITY)) {
                firestore.collection(CORRESPONDENCE_QUERY)
                        .document(CId)
                        .collection(COMPOSE)
                        .document(docId)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    if (documentSnapshot.get(PDF_ATTACHMENT) != null) {
                                        List<Map<String, Object>> maps = (List<Map<String, Object>>) documentSnapshot.get(PDF_ATTACHMENT);
                                        holder.PdfCounter.setText(maps.size() + " PDF");
                                    } else {
                                        holder.PdfCounter.setText("0 PDF");
                                    }
                                }
                            }
                        });
            } else {
                firestore.collection("Users")
                        .document(user.getUid())
                        .collection(SHARED_DOCUMENT_QUERY)
                        .document(CId)
                        .collection(COMPOSE)
                        .document(docId)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    if (documentSnapshot.get(PDF_ATTACHMENT) != null) {
                                        List<Map<String, Object>> maps = (List<Map<String, Object>>) documentSnapshot.get(PDF_ATTACHMENT);
                                        holder.PdfCounter.setText(maps.size() + " PDF");
                                    } else {
                                        holder.PdfCounter.setText("0 PDF");
                                    }
                                }
                            }
                        });
            }
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

    }

}