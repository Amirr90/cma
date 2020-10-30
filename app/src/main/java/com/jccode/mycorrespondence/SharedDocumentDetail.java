package com.jccode.mycorrespondence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jccode.mycorrespondence.Adapter.SliderAdapterExample;
import com.jccode.mycorrespondence.models.Banner;
import com.jccode.mycorrespondence.utility.TimeAgo;
import com.smarteist.autoimageslider.SliderView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.jccode.mycorrespondence.utility.Util.ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.DESCRIPTION;
import static com.jccode.mycorrespondence.utility.Util.DOCUMENT_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DOC_ID;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.IMAGE_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE;
import static com.jccode.mycorrespondence.utility.Util.PDF_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.SHARED_DOCUMENT_QUERY;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.SUB_DOCUMENT_QUERY;
import static com.jccode.mycorrespondence.utility.Util.SUB_DOC_ID;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;

public class SharedDocumentDetail extends AppCompatActivity {


    private static final String VIEW_IMAGES = "View Images";
    private static final String VIEW_PDF_FILES = "View Pdf file";
    private static final String DELETE_LETTER = "Delete Document";
    private static final String TAG = "DocumentDetail";
    private static final int FULL_SCREEN_IMAGE_INTENT = 1001;
    private static final int FULL_SCREEN_PDF_INTENT = 1002;
    private static final String UPDATE_REMARK = "Update Remark";
    FirebaseUser user;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    String userId;
    String docId;

    private TextView tvSub, tvDesc, tvTimestamp;

    TimeAgo timeAgo;

    RecyclerView doc_detail_rec;
    DocDetailAdapter docDetailAdapter;
    List<DocumentSnapshot> docList = new ArrayList<>();

    //Slider
    SliderView sliderView;
    SliderAdapterExample sliderAdapterExample;
    List<Banner> sliderData = new ArrayList<>();

    TextView ImageCounterTv, PdfCounterTv;
    ArrayList<String> pdfList = new ArrayList<>();

    ProgressDialog progressDialog;

    public static Boolean isUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_document_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.shared_document_detail_toolbar);
        ImageCounterTv = (TextView) findViewById(R.id.shared_doc_image_attachment);
        PdfCounterTv = (TextView) findViewById(R.id.shared_doc_pdf_attachment);

        setToolbar(toolbar, "Shared Document Detail");
        user = FirebaseAuth.getInstance().getCurrentUser();
        timeAgo = new TimeAgo();
        progressDialog = new ProgressDialog(this);

        docDetailAdapter = new DocDetailAdapter(docList, this, isUpdated);
        if (user != null) {
            userId = user.getUid();
            docId = getIntent().getStringExtra(DOC_ID);

            setDocDetails();
            setDocDetailRec();
            setDocSliderImage();

        }
    }


    private void setDocSliderImage() {
        sliderView = (SliderView) findViewById(R.id.shared_doc_imageSlider);
        sliderAdapterExample = new SliderAdapterExample(sliderData, SharedDocumentDetail.this);
        sliderView.setSliderAdapter(sliderAdapterExample);

        //getting Slider Data
        CollectionReference ref1 = firestore.collection(SHARED_DOCUMENT_QUERY)
                .document(docId)
                .collection(IMAGE_ATTACHMENT);


        //getting from ref1
        ref1.whereEqualTo(IS_ACTIVE, true).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot snapshot : snapshots) {
                        sliderData.add(new Banner(snapshot.getString(ATTACHMENT), snapshot.getId()));
                    }

                    sliderAdapterExample.notifyDataSetChanged();
                    sliderView.setVisibility(View.VISIBLE);

                    ImageCounterTv.setText(sliderData.size() + " Images");

                } else {
                    ImageCounterTv.setText(sliderData.size() + " Images");

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SharedDocumentDetail.this, "error in reading images", Toast.LENGTH_SHORT).show();
                sliderView.setVisibility(View.GONE);
                ImageCounterTv.setText(sliderData.size() + " Images");
            }
        });


        CollectionReference ref2 = firestore.collection(SHARED_DOCUMENT_QUERY)
                .document(docId)
                .collection(PDF_ATTACHMENT);
        ref2.whereEqualTo(IS_ACTIVE, true).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot snapshot : snapshots) {
                        pdfList.add(snapshot.getString(ATTACHMENT));
                    }
                    PdfCounterTv.setText(pdfList.size() + " Pdf");

                } else {
                    PdfCounterTv.setText(pdfList.size() + " Pdf");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                PdfCounterTv.setText(pdfList.size() + " Pdf");
            }
        });


    }

    private void setDocDetailRec() {
        doc_detail_rec = (RecyclerView) findViewById(R.id.shared_doc_sub_rec);
        doc_detail_rec.setLayoutManager(new LinearLayoutManager(this));
        doc_detail_rec.setHasFixedSize(true);
        doc_detail_rec.setAdapter(docDetailAdapter);

        loadSubDocumentData();
    }

    public void gotoAttachmentScreen3(final View view) {
        if (pdfList != null && !pdfList.isEmpty()) {
            startActivity(new Intent(this, PdfScreen.class).putExtra("TYPE", "PDF")
                    .putExtra("pdfs", pdfList));
        } else {
            Snackbar.make(view, "No pdf", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void gotoFullImageScreen3(View view) {
        if (sliderData.isEmpty()) {
            Snackbar.make(view, "No Image", Snackbar.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> imageData = new ArrayList<>();
        for (Banner banner : sliderData) {
            imageData.add(banner.getImage());
        }
        startActivity(new Intent(this, FullScreenImageScreen.class)
                .putStringArrayListExtra("images", imageData));
    }

    private void loadSubDocumentData() {
        firestore.collection(SHARED_DOCUMENT_QUERY)
                .document(docId)
                .collection(SUB_DOCUMENT_QUERY)
                .whereEqualTo(IS_ACTIVE, true)
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.isEmpty()) {
                            return;
                        }

                        docList.clear();
                        docList.addAll(queryDocumentSnapshots.getDocuments());
                        docDetailAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "error in reading sub document", e);
                docDetailAdapter.notifyDataSetChanged();
                Toast.makeText(SharedDocumentDetail.this, "failed to read data,try again" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void setDocDetails() {
        tvSub = (TextView) findViewById(R.id.shared_tv_sub);
        tvDesc = (TextView) findViewById(R.id.shared_tv_desc);
        tvTimestamp = (TextView) findViewById(R.id.shared_tv_timestamp);


        try {
            tvSub.setText(getIntent().getStringExtra(SUBJECT));
            tvDesc.setText("Description: " + getIntent().getStringExtra(DESCRIPTION));

            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String dateString = formatter.format(new Date(Long.parseLong(getIntent().getStringExtra(TIMESTAMP))));
            tvTimestamp.setText("Updated at " + dateString);
        } catch (Exception e) {
        }

    }

    private void setToolbar(Toolbar toolbar, String id) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(id);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }


    private class DocDetailAdapter extends RecyclerView.Adapter<DocDetailAdapter.MyViewHolder> {

        private static final String TAG = "DocDetailAdapter";
        List<DocumentSnapshot> documentSnapshots;
        Context context;
        Boolean isUpdated;

        public DocDetailAdapter(List<DocumentSnapshot> documentSnapshots, Context context, Boolean isUpdated) {
            this.documentSnapshots = documentSnapshots;
            this.context = context;
            this.isUpdated = isUpdated;
        }

        @NonNull
        @Override
        public DocDetailAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.doc_detail_view, parent, false);
            return new DocDetailAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final DocDetailAdapter.MyViewHolder holder, final int position) {

            final DocumentSnapshot snapshot = documentSnapshots.get(position);
            if (snapshot != null) {
                int pos = position + 1;
                holder.tvSn.setText("" + pos);
                holder.tvRemark.setText(snapshot.getString(SUBJECT));
            }


            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    final String subDOC_ID = snapshot.getId();
                    final CharSequence[] items = {VIEW_IMAGES, VIEW_PDF_FILES};

                    AlertDialog.Builder builder = new AlertDialog.Builder(SharedDocumentDetail.this);
                    builder.setTitle("Make your selection");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            dialog.dismiss();
                            String selected_text = items[item].toString();
                            switch (selected_text) {
                                case VIEW_IMAGES: {
                                    progressDialog.setMessage("Please wait...");
                                    progressDialog.show();
                                    firestore.collection(SHARED_DOCUMENT_QUERY)
                                            .document(docId)
                                            .collection(IMAGE_ATTACHMENT)
                                            .whereEqualTo(SUB_DOC_ID, subDOC_ID)
                                            .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            if (queryDocumentSnapshots.isEmpty()) {
                                                progressDialog.dismiss();
                                                Snackbar.make(view, "No Image for this Document", Snackbar.LENGTH_SHORT).show();
                                            } else {
                                                List<DocumentSnapshot> imageSnap = queryDocumentSnapshots.getDocuments();
                                                ArrayList<String> imageData = new ArrayList<>();
                                                for (DocumentSnapshot snapshot1 : imageSnap) {
                                                    Boolean isActiveImage = snapshot1.getBoolean(IS_ACTIVE);
                                                    if (isActiveImage != null) {
                                                        if (isActiveImage) {
                                                            imageData.add(snapshot1.getString(ATTACHMENT));
                                                        }
                                                    }
                                                }
                                                progressDialog.dismiss();
                                                Intent intent = new Intent(context, FullScreenImageScreen.class);
                                                intent.putStringArrayListExtra("images", imageData);
                                                startActivityForResult(intent, FULL_SCREEN_IMAGE_INTENT);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Log.d(TAG, "reading doument Image", e);
                                            Toast.makeText(context, "try again " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                                break;

                                case VIEW_PDF_FILES: {
                                    progressDialog.setMessage("Please wait...");
                                    progressDialog.show();
                                    firestore.collection(SHARED_DOCUMENT_QUERY)
                                            .document(docId)
                                            .collection(PDF_ATTACHMENT)
                                            .whereEqualTo(SUB_DOC_ID, subDOC_ID)
                                            .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            if (queryDocumentSnapshots.isEmpty()) {
                                                progressDialog.dismiss();
                                                Snackbar.make(view, "No pdf for this Document", Snackbar.LENGTH_SHORT).show();
                                            } else {
                                                List<DocumentSnapshot> imageSnap = queryDocumentSnapshots.getDocuments();
                                                ArrayList<String> pdfList = new ArrayList<>();
                                                for (DocumentSnapshot snapshot1 : imageSnap) {
                                                    Boolean isActiveImage = snapshot1.getBoolean(IS_ACTIVE);
                                                    if (isActiveImage != null) {
                                                        if (isActiveImage) {
                                                            pdfList.add(snapshot1.getString(ATTACHMENT));
                                                        }
                                                    }
                                                }
                                                progressDialog.dismiss();
                                                Intent imageIntent = new Intent(SharedDocumentDetail.this, PdfScreen.class);
                                                imageIntent.putExtra("TYPE", "PDF");
                                                imageIntent.putExtra("pdfs", pdfList);
                                                startActivityForResult(imageIntent, FULL_SCREEN_PDF_INTENT);
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Log.d(TAG, "reading doument Image", e);
                                            Toast.makeText(context, "try again " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                                break;

                            }
                        }
                    }).show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return documentSnapshots.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tvSn, tvRemark;
            private LinearLayout linearLayout;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSn = (TextView) itemView.findViewById(R.id.tv_sn);
                tvRemark = (TextView) itemView.findViewById(R.id.tv_remark);
                linearLayout = (LinearLayout) itemView.findViewById(R.id.lin_layout);
            }
        }
    }


}