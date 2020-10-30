package com.jccode.mycorrespondence;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jccode.mycorrespondence.Adapter.SliderAdapterExample;
import com.jccode.mycorrespondence.models.Banner;
import com.jccode.mycorrespondence.utility.TimeAgo;
import com.smarteist.autoimageslider.SliderView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jccode.mycorrespondence.utility.Util.*;


public class DocumentDetail extends AppCompatActivity {
    private static final String VIEW_IMAGES = "View Images";
    private static final String VIEW_PDF_FILES = "View Pdf file";
    private static final String DELETE_LETTER = "Delete Document";
    public static final String ADD_MORE_DOCUMENTS = "Add more Documents";
    private static final String TAG = "DocumentDetail";
    private static final int FULL_SCREEN_IMAGE_INTENT = 1001;
    private static final int FULL_SCREEN_PDF_INTENT = 1002;
    private static final String UPDATE_REMARK = "Update Remark";
    List<Uri> documentEncodedList = new ArrayList<>();
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

    TextView ImageCounterTv, PdfCounterTv, tvBtnShare, tvBtnAddDocuments;
    ArrayList<String> pdfList = new ArrayList<>();

    ProgressDialog progressDialog;

    public static Boolean isUpdated = false;
    private int SHARE_CORRESPONDENCE_CODE = 1001;

    ArrayList<String> selectedImagesList = new ArrayList<>();
    TextView subDocId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.document_detail_toolbar);
        ImageCounterTv = (TextView) findViewById(R.id.doc_image_attachment);
        PdfCounterTv = (TextView) findViewById(R.id.doc_pdf_attachment);
        tvBtnShare = (TextView) findViewById(R.id.shareBtn);
        tvBtnAddDocuments = (TextView) findViewById(R.id.addbtn);
        subDocId = findViewById(R.id.textView33);

        setToolbar(toolbar, "Document Detail");
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

        if (!getIntent().getStringExtra(FROM).equalsIgnoreCase("ViewAllDocumentActivity")) {
            changeVisibilityTo(false);
        } else {
            changeVisibilityTo(true);
        }
    }

    private void changeVisibilityTo(boolean b) {
        tvDesc.setEnabled(b);
        tvSub.setEnabled(b);
        tvBtnAddDocuments.setVisibility(b ? View.VISIBLE : View.GONE);
        tvBtnShare.setVisibility(b ? View.VISIBLE : View.GONE);
    }


    private void setDocSliderImage() {
        sliderView = (SliderView) findViewById(R.id.doc_imageSlider);
        sliderAdapterExample = new SliderAdapterExample(sliderData, DocumentDetail.this);
        sliderView.setSliderAdapter(sliderAdapterExample);

        //getting Slider Data
        CollectionReference ref1 = firestore.collection(DOCUMENT_QUERY)
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
                Toast.makeText(DocumentDetail.this, "error in reading images", Toast.LENGTH_SHORT).show();
                sliderView.setVisibility(View.GONE);
                ImageCounterTv.setText(sliderData.size() + " Images");
            }
        });


        CollectionReference ref2 = firestore.collection(DOCUMENT_QUERY)
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

    public void updateDocument(final View view) {
        TextView editText = findViewById(view.getId());
        String edText = editText.getText().toString();
        if (view.getId() == R.id.tv_sub) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DocumentDetail.this);
            LayoutInflater inflater = DocumentDetail.this.getLayoutInflater();
            View view1 = inflater.inflate(R.layout.update_data_layout, null);
            final EditText ed = (EditText) view1.findViewById(R.id.update1);

            ed.setHint("Subject");
            ed.setText(edText);
            builder.setTitle("Update Subject");
            builder.setView(view1)
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // your sign in code here
                            final String data = ed.getText().toString();
                            if (data.isEmpty()) {
                                Snackbar.make(view, "Please enter data to update", Snackbar.LENGTH_SHORT).show();
                            } else {
                                progressDialog.setTitle("Updating subject");
                                progressDialog.setMessage("please wait...");
                                progressDialog.show();
                                update(data, SUBJECT);
                            }
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // remove the dialog from the screen
                            dialog.dismiss();
                        }
                    })
                    .show();

        } else if (view.getId() == R.id.tv_desc) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DocumentDetail.this);
            LayoutInflater inflater = DocumentDetail.this.getLayoutInflater();
            View view1 = inflater.inflate(R.layout.update_data_layout, null);
            final EditText ed = (EditText) view1.findViewById(R.id.update1);
            ed.setHint("Description");
            ed.setText(edText);
            builder.setTitle("Update Description");
            builder.setView(view1)
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // your sign in code here
                            final String data = ed.getText().toString();
                            if (data.isEmpty()) {
                                Snackbar.make(view, "Please enter data to update", Snackbar.LENGTH_SHORT).show();
                            } else {
                                progressDialog.setTitle("Updating description");
                                progressDialog.setMessage("please wait...");
                                progressDialog.show();
                                update(data, DESCRIPTION);
                            }
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // remove the dialog from the screen
                            dialog.dismiss();
                        }
                    })
                    .show();
        }

    }

    private void update(final String data, final String query) {
        firestore.collection(DOCUMENT_QUERY).document(docId)
                .update(query, data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        isUpdated = true;
                        progressDialog.dismiss();
                        if (query.equalsIgnoreCase(SUBJECT))
                            tvSub.setText(data);
                        else
                            tvDesc.setText(data);
                        Toast.makeText(DocumentDetail.this, "updated", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(DocumentDetail.this, "try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDocDetailRec() {
        doc_detail_rec = (RecyclerView) findViewById(R.id.doc_sub_rec);
        doc_detail_rec.setLayoutManager(new LinearLayoutManager(this));
        doc_detail_rec.setHasFixedSize(true);
        doc_detail_rec.setAdapter(docDetailAdapter);

        loadSubDocumentData();
    }

    public void gotoAttachmentScreen2(final View view) {
        if (pdfList != null && !pdfList.isEmpty()) {
            startActivity(new Intent(this, PdfScreen.class).putExtra("TYPE", "PDF")
                    .putExtra("pdfs", pdfList));
        } else {
            Snackbar.make(view, "No pdf", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void gotoFullImageScreen(View view) {
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
        firestore.collection(DOCUMENT_QUERY)
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
                Toast.makeText(DocumentDetail.this, "failed to read data,try again" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void setDocDetails() {
        tvSub = (TextView) findViewById(R.id.tv_sub);
        tvDesc = (TextView) findViewById(R.id.tv_desc);
        tvTimestamp = (TextView) findViewById(R.id.tv_timestamp);


        try {
            tvSub.setText(getIntent().getStringExtra(SUBJECT));
            tvDesc.setText(getIntent().getStringExtra(DESCRIPTION));

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
        if (isUpdated) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
        return super.onSupportNavigateUp();
    }


    public void addDocument(View view) {
        startActivity(new Intent(this, AddDocumentActivity.class)
                .putExtra(FROM, "Document")
                .putExtra(DOC_ID, docId));
        finish();
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
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final DocDetailAdapter.MyViewHolder holder, final int position) {

            final DocumentSnapshot snapshot = documentSnapshots.get(position);
            if (snapshot != null) {
                int pos = documentSnapshots.size() - position;
                holder.tvSn.setText("" + pos);
                holder.tvRemark.setText(snapshot.getString(SUBJECT));
            }


            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    final String remark = holder.tvRemark.getText().toString();
                    final String subDOC_ID = snapshot.getId();
                    if (getIntent().getStringExtra(FROM).equalsIgnoreCase("ViewAllDocumentActivity")) {
                        {
                            final CharSequence[] items = {UPDATE_REMARK, VIEW_IMAGES, VIEW_PDF_FILES, DELETE_LETTER, ADD_MORE_DOCUMENTS};
                            subDocId.setText(snapshot.getId());
                            AlertDialog.Builder builder = new AlertDialog.Builder(DocumentDetail.this);
                            builder.setTitle("Make your selection");
                            builder.setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    dialog.dismiss();
                                    String selected_text = items[item].toString();
                                    switch (selected_text) {
                                        case DELETE_LETTER: {
                                            deleteLetter(snapshot.getId(), position, view, snapshot);
                                        }
                                        break;

                                        case VIEW_IMAGES: {
                                            progressDialog.setMessage("Please wait...");
                                            progressDialog.show();
                                            firestore.collection(DOCUMENT_QUERY)
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
                                                    Log.d(TAG, "reading document Image", e);
                                                    Toast.makeText(context, "try again " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                        }
                                        break;

                                        case VIEW_PDF_FILES: {
                                            progressDialog.setMessage("Please wait...");
                                            progressDialog.show();
                                            firestore.collection(DOCUMENT_QUERY)
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
                                                        Intent imageIntent = new Intent(DocumentDetail.this, PdfScreen.class);
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

                                        case UPDATE_REMARK: {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(DocumentDetail.this);
                                            LayoutInflater inflater = DocumentDetail.this.getLayoutInflater();
                                            View view1 = inflater.inflate(R.layout.update_data_layout, null);
                                            final EditText ed = (EditText) view1.findViewById(R.id.update1);
                                            ed.setHint("Remark");
                                            ed.setText(remark);
                                            builder.setTitle("Update Remark");
                                            builder.setView(view1)
                                                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            final String data = ed.getText().toString();
                                                            if (data.isEmpty()) {
                                                                Snackbar.make(view, "Please enter data to update", Snackbar.LENGTH_SHORT).show();
                                                            } else {
                                                                progressDialog.setTitle("Updating Remark");
                                                                progressDialog.setMessage("please wait...");
                                                                progressDialog.show();
                                                                update(data, subDOC_ID, holder);
                                                            }
                                                            dialog.dismiss();
                                                        }
                                                    })
                                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            // remove the dialog from the screen
                                                            dialog.dismiss();
                                                        }
                                                    })
                                                    .show();
                                        }
                                        break;

                                        case ADD_MORE_DOCUMENTS: {
                                            selectImage();

                                        }
                                        break;
                                        default:
                                            Snackbar.make(view, "Coming Soon", Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            }).show();
                        }


                    } else {
                        final CharSequence[] items = {VIEW_IMAGES, VIEW_PDF_FILES};
                        subDocId.setText(snapshot.getId());
                        AlertDialog.Builder builder = new AlertDialog.Builder(DocumentDetail.this);
                        builder.setTitle("Make your selection");
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                dialog.dismiss();
                                String selected_text = items[item].toString();
                                switch (selected_text) {

                                    case VIEW_IMAGES: {
                                        progressDialog.setMessage("Please wait...");
                                        progressDialog.show();
                                        firestore.collection(DOCUMENT_QUERY)
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
                                                Log.d(TAG, "reading document Image", e);
                                                Toast.makeText(context, "try again " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                    break;

                                    case VIEW_PDF_FILES: {
                                        progressDialog.setMessage("Please wait...");
                                        progressDialog.show();
                                        firestore.collection(DOCUMENT_QUERY)
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
                                                    Intent imageIntent = new Intent(DocumentDetail.this, PdfScreen.class);
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

                                    default:
                                        Snackbar.make(view, "Coming Soon", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }).show();
                    }

                }
            });
        }


        private void selectImage() {

            final CharSequence[] options = {"Images", "Pdf"};
            AlertDialog.Builder builder = new AlertDialog.Builder(DocumentDetail.this);
            builder.setTitle("Select Option");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    String selectedItem = options[item].toString();
                    switch (selectedItem) {
                        case "Images": {
                            dialog.dismiss();
                            image();
                        }
                        break;
                        case "Pdf": {
                            dialog.dismiss();
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            intent.setType("application/pdf");
                            startActivityForResult(intent, 10012);
                        }
                        break;
                        default:
                            dialog.dismiss();

                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }

        public void image() {

            Options options = Options.init()
                    .setRequestCode(1100)                                           //Request code for activity results
                    .setCount(1)                                                   //Number of images to restict selection count
                    .setFrontfacing(false)                                         //Front Facing camera on start
                    //.setPreSelectedUrls(null)                               //Pre selected Image Urls
                    .setExcludeVideos(true)                                       //Option to exclude videos
                    //.setVideoDurationLimitinSeconds(30)                            //Duration for video recording
                    .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                    .setPath("/cma/images");                                       //Custom Path For media Storage

            Pix.start(DocumentDetail.this, options);
        }


        private void update(final String data, String subDOC_ID, final MyViewHolder holder) {
            firestore.collection(DOCUMENT_QUERY).document(docId)
                    .collection(SUB_DOCUMENT_QUERY)
                    .document(subDOC_ID)
                    .update(SUBJECT, data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressDialog.dismiss();
                            holder.tvRemark.setText(data);
                            Toast.makeText(DocumentDetail.this, "updated", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(DocumentDetail.this, "try again", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void deleteLetter(final String sub_DOC_id, final int position, final View view, final DocumentSnapshot snapshot) {
            progressDialog.setTitle("Deleting Document");
            progressDialog.setTitle("Please wait.....");
            progressDialog.show();


            final DocumentReference ref = firestore.collection(DOCUMENT_QUERY)
                    .document(docId)
                    .collection(SUB_DOCUMENT_QUERY)
                    .document(sub_DOC_id);

            final CollectionReference imageRef = firestore.collection(DOCUMENT_QUERY)
                    .document(docId)
                    .collection(IMAGE_ATTACHMENT);

            final CollectionReference pdfRef = firestore.collection(DOCUMENT_QUERY)
                    .document(docId)
                    .collection(PDF_ATTACHMENT);

            ref.update(IS_ACTIVE, false)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            imageRef.whereEqualTo(DOC_ID, docId)
                                    .whereEqualTo(SUB_DOC_ID, sub_DOC_id).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                                        for (DocumentSnapshot data : snapshots) {
                                            imageRef.document(data.getId()).update(IS_ACTIVE, false);
                                        }
                                    }
                                }
                            });

                            pdfRef.whereEqualTo(DOC_ID, docId)
                                    .whereEqualTo(SUB_DOC_ID, sub_DOC_id).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                                        for (DocumentSnapshot data : snapshots) {
                                            pdfRef.document(data.getId()).update(IS_ACTIVE, false);
                                        }
                                    }
                                }
                            });

                            progressDialog.dismiss();
                            documentSnapshots.remove(position);
                            docDetailAdapter.notifyDataSetChanged();
                            Snackbar.make(view, "Document Deleted", Snackbar.LENGTH_SHORT).setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ref.update(IS_ACTIVE, true);
                                    isUpdated = true;
                                    documentSnapshots.add(position, snapshot);
                                    docDetailAdapter.notifyDataSetChanged();
                                    Toast.makeText(DocumentDetail.this, "Document Re-Stored", Toast.LENGTH_SHORT).show();
                                }
                            }).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(context, "try again", Toast.LENGTH_SHORT).show();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //new ImageGetter
        if (resultCode == Activity.RESULT_OK && requestCode == 1100) {
            if (data != null) {
                selectedImagesList.clear();
                selectedImagesList.addAll(data.getStringArrayListExtra(Pix.IMAGE_RESULTS));
                showSelectedImageAsDialog(selectedImagesList);

            }

        }

        switch (requestCode) {
            case 10012:
                // Checking whether data is null or not

                if (data != null) {
                    StringBuilder builder = new StringBuilder();
                    if (data.getClipData() != null) {
                        for (int index = 0; index < data.getClipData().getItemCount(); index++) {
                            Uri uri = data.getClipData().getItemAt(index).getUri();
                            Log.d("filesUri [" + uri + "] : ", String.valueOf(uri));
                            documentEncodedList.add(uri);
                            builder.append("attachment " + index + " \n");
                            showSelectedPdfAsDialog(documentEncodedList);
                        }
                    } else {
                        Uri uri = data.getData();
                        Log.d("fileUri: ", String.valueOf(uri));
                        documentEncodedList.add(uri);
                        builder.append("filesUri [" + uri + "] : \n");
                        showSelectedPdfAsDialog(documentEncodedList);

                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void showSelectedPdfAsDialog(List<Uri> documentEncodedList) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View myScrollView = inflater.inflate(R.layout.pdf_text, null, false);
        TextView tv = (TextView) myScrollView
                .findViewById(R.id.textViewWithScroll);
        tv.setText("1. " + getNameFromURI(documentEncodedList.get(0), this));

        new AlertDialog.Builder(DocumentDetail.this).setView(myScrollView)
                .setTitle("Pdf file")
                .setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        try {
                            uploadDocument();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(DocumentDetail.this, "failed to upload " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();

    }

    private void showSelectedImageAsDialog(ArrayList<String> selectedImagesList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DocumentDetail.this);
        LayoutInflater inflater = DocumentDetail.this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.image, null);
        ImageView imageView = view.findViewById(R.id.imageView5);
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(selectedImagesList.get(0));
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(DocumentDetail.this, "failed to set image " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
        builder.setView(view)

                .setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        try {
                            uploadDocument();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(DocumentDetail.this, "failed to upload " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // remove the dialog from the screen
                    }
                })
                .show();
    }

    public void uploadDocument() throws FileNotFoundException {


        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("uploading...");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();

        if (!selectedImagesList.isEmpty()) {
            progressDialog.show();
            int i;
            for (i = 0; i < selectedImagesList.size(); i++) {
                final String STORAGE_PATH = "document_images/" + docId + "/" + System.currentTimeMillis() + ".jpg";
                StorageReference spaceRef = storageRef.child(STORAGE_PATH);

                InputStream stream = new FileInputStream(new File(selectedImagesList.get(i)));
                UploadTask uploadTask = spaceRef.putStream(stream);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(DocumentDetail.this, "failed to upload image" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.child(STORAGE_PATH).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                Map<String, Object> attachmentMap = new HashMap<>();
                                attachmentMap.put(ATTACHMENT, uri.toString());
                                attachmentMap.put(IS_ACTIVE, true);
                                attachmentMap.put(FILE_NAME, "default");
                                attachmentMap.put(TIMESTAMP, System.currentTimeMillis());
                                attachmentMap.put(SUB_DOC_ID, subDocId.getText().toString());
                                attachmentMap.put(DOC_ID, docId);

                                firestore.collection(DOCUMENT_QUERY)
                                        .document(docId)
                                        .collection(IMAGE_ATTACHMENT)
                                        .add(attachmentMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        progressDialog.dismiss();
                                        loadSubDocumentData();
                                        Toast.makeText(DocumentDetail.this, " Document uploaded", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(DocumentDetail.this, "could't upload Document", Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                progressDialog.dismiss();
                            }
                        });

                    }
                });
            }
        }

        if (!documentEncodedList.isEmpty()) {
            progressDialog.show();
            for (int i = 0; i < documentEncodedList.size(); i++) {
                final int counter = i + 1;
                final String STORAGE_PATH = "document_documents/" + docId + "/" + System.currentTimeMillis() + ".pdf";
                StorageReference spaceRef = storageRef.child(STORAGE_PATH);
                UploadTask uploadTask = spaceRef.putFile(documentEncodedList.get(i));
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(DocumentDetail.this, "failed to upload file", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.child(STORAGE_PATH).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Map<String, Object> attachmentMap = new HashMap<>();
                                attachmentMap.put(ATTACHMENT, uri.toString());
                                attachmentMap.put(IS_ACTIVE, true);
                                attachmentMap.put(FILE_NAME, "default");
                                attachmentMap.put(TIMESTAMP, System.currentTimeMillis());
                                attachmentMap.put(SUB_DOC_ID, subDocId.getText().toString());
                                attachmentMap.put(DOC_ID, docId);
                                firestore.collection(DOCUMENT_QUERY)
                                        .document(docId)
                                        .collection(PDF_ATTACHMENT)
                                        .add(attachmentMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        progressDialog.dismiss();
                                        loadSubDocumentData();
                                        Toast.makeText(DocumentDetail.this, counter + " document uploaded", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(DocumentDetail.this, "could't upload document", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                                progressDialog.dismiss();

                            }
                        });

                    }
                });
            }
        }
    }


    public void shareDocument(View view) {
        int id = view.getId();
        if (id == R.id.shareBtn) {
            Intent intent = new Intent(DocumentDetail.this, ShareCorrespondenceActivity.class);
            intent.putExtra(CORRESPONDENCE_ID, docId);
            intent.putExtra(FROM, "Document");
            startActivityForResult(intent, SHARE_CORRESPONDENCE_CODE);
        }
    }
}