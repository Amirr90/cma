package com.jccode.mycorrespondence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jccode.mycorrespondence.Adapter.ComposeAdapter;
import com.jccode.mycorrespondence.Adapter.SliderAdapterExample;
import com.jccode.mycorrespondence.models.Banner;
import com.jccode.mycorrespondence.utility.TimeAgo;
import com.jccode.mycorrespondence.utility.Util;
import com.smarteist.autoimageslider.SliderView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.jccode.mycorrespondence.utility.Util.ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.CANCEL;
import static com.jccode.mycorrespondence.utility.Util.COMPOSE;
import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DATE;
import static com.jccode.mycorrespondence.utility.Util.DELETE_LETTER;
import static com.jccode.mycorrespondence.utility.Util.DEPARTMENT;
import static com.jccode.mycorrespondence.utility.Util.DESCRIPTION;
import static com.jccode.mycorrespondence.utility.Util.DOCUMENT;
import static com.jccode.mycorrespondence.utility.Util.FILED;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.IMAGE;
import static com.jccode.mycorrespondence.utility.Util.IMAGE_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE;
import static com.jccode.mycorrespondence.utility.Util.LETTER_DETAIL;
import static com.jccode.mycorrespondence.utility.Util.LETTER_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.NO_TAG;
import static com.jccode.mycorrespondence.utility.Util.PARTIALLY_REPLIED;
import static com.jccode.mycorrespondence.utility.Util.PDF_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.REMARK;
import static com.jccode.mycorrespondence.utility.Util.REMINDER;
import static com.jccode.mycorrespondence.utility.Util.REPLIED;
import static com.jccode.mycorrespondence.utility.Util.REPLY;
import static com.jccode.mycorrespondence.utility.Util.SHARED_ACTIVITY;
import static com.jccode.mycorrespondence.utility.Util.SHARED_DOCUMENT_QUERY;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.TAG;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TITLE;
import static com.jccode.mycorrespondence.utility.Util.TYPE;
import static com.jccode.mycorrespondence.utility.Util.UID;
import static com.jccode.mycorrespondence.utility.Util.UN_REPLIED;
import static com.jccode.mycorrespondence.utility.Util.USERS;
import static com.jccode.mycorrespondence.utility.Util.YES;

public class ViewSharedCorrespondingDetail extends AppCompatActivity {
    private static final String TAG = "CorrespondingDetail";
    private static final String STATUS_CLOSED = "Status : Closed";
    private static final String STATUS_OPEN = "Status : Open";
    private static final String RE_OPEN = "Re-open";
    private static final String CLOSE = "Close";
    private static final CharSequence CHANGING_STATUS = "changing status...";
    public String Title, Description, Subject, Id, Timestamp, post_uid, reminder, Status, Department, Date, LetterNumber, Type, Tag;
    TextView mSubject, mDescription, mTimestamp, mReminder, mStatusTv, mChangeStatusBtn, mDepartment, mDate, mLetterNumber, mTAG;
    public TextView mAttachmentTextView, PdfAttachmentCounterTv, mImageCounterView;
    FirebaseFirestore firestore;
    TimeAgo timeAgo;

    ArrayList<String> imageList = new ArrayList<>();
    ArrayList<String> imageCounterList = new ArrayList<>();
    ArrayList<String> pdfList = new ArrayList<>();
    ArrayList<String> banner2 = new ArrayList<>();


    RecyclerView recyclerView;
    List<DocumentSnapshot> list = new ArrayList<>();
    ComposeAdapter2 composeAdapter;
    SliderView sliderView;
    List<Banner> banners = new ArrayList<>();
    SliderAdapterExample sliderAdapterExample;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_shared_corresponding_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.correspondence_toolbar2);
        sliderView = (SliderView) findViewById(R.id.imageSlider);

        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        user = FirebaseAuth.getInstance().getCurrentUser();

        findViewById();
        if (getIntent().hasExtra(ID)) {
            Id = getIntent().getStringExtra(ID);
            Title = getIntent().getStringExtra(TITLE);
            Description = getIntent().getStringExtra(DESCRIPTION);
            Subject = getIntent().getStringExtra(SUBJECT);
            reminder = getIntent().getStringExtra(REMINDER);
            Date = getIntent().getStringExtra(DATE);
            Type = getIntent().getStringExtra(TYPE);
            Tag = getIntent().getStringExtra("TAG");
            LetterNumber = getIntent().getStringExtra(LETTER_NUMBER);
            Timestamp = getIntent().getStringExtra(TIMESTAMP);
            Status = getIntent().getStringExtra(IS_ACTIVE);
            post_uid = getIntent().getStringExtra(UID);
            Department = getIntent().getStringExtra(DEPARTMENT);
            setToolbar(toolbar, "Correspondence Detail");

            setCorrespondenceData();

            setComposeData();

            sliderAdapterExample = new SliderAdapterExample(banners, ViewSharedCorrespondingDetail.this);
            sliderView.setSliderAdapter(sliderAdapterExample);


        }

    }


    private void setComposeData() {
        composeAdapter = new ComposeAdapter2(list, this, Id, LetterNumber, Type);
        recyclerView = (RecyclerView) findViewById(R.id.doc_rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.hasFixedSize();
        recyclerView.setAdapter(composeAdapter);
        loadComposeData();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void loadComposeData() {
        firestore.collection("Users")
                .document(user.getUid())
                .collection(SHARED_DOCUMENT_QUERY)
                .document(Id).collection(COMPOSE).whereEqualTo(IS_ACTIVE, true)
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e == null && queryDocumentSnapshots.getDocuments() != null) {
                            list.clear();
                            list.addAll(queryDocumentSnapshots.getDocuments());
                            composeAdapter.notifyDataSetChanged();

                        } else {
                            Log.d(TAG, "onFailure: " + e.getMessage());
                            Toast.makeText(ViewSharedCorrespondingDetail.this, "load again: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void setCorrespondenceData() {
        mSubject.setText(Subject);
        mDescription.setText("Description:\n" + Description);
        mDepartment.setText("Department: " + Department);
        mDate.setText("Letter Date: " + Date);
        mLetterNumber.setText("Letter Number: " + LetterNumber);
        if (Tag != null)
            mTAG.setText("TAG: " + Tag);

        if (!reminder.equals("null")) {
            mReminder.setVisibility(View.VISIBLE);
            mReminder.setText(REMINDER + " " + reminder);
        } else {
            mReminder.setVisibility(View.GONE);
        }

        if (Status.equals("true")) {
            mStatusTv.setText(STATUS_OPEN);
            mStatusTv.setTextColor(Color.GREEN);
            mChangeStatusBtn.setText(CLOSE);
        } else {
            mStatusTv.setText(STATUS_CLOSED);
            mStatusTv.setTextColor(Color.RED);
            mChangeStatusBtn.setText(RE_OPEN);
        }


        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String dateString = formatter.format(new Date(Long.parseLong(Timestamp)));
        mTimestamp.setText("Updated at " + dateString);

        firestore.collection("Users")
                .document(user.getUid())
                .collection(SHARED_DOCUMENT_QUERY)
                .document(Id).collection(IMAGE_ATTACHMENT).whereEqualTo(IS_ACTIVE, true)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e == null) {
                            if (queryDocumentSnapshots != null) {
                                if (queryDocumentSnapshots.size() != 0) {
                                    int count = queryDocumentSnapshots.size();
                                    mAttachmentTextView.setText(count + " " + ATTACHMENT);
                                    banners.clear();
                                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                        String id = snapshot.getId();
                                        String imageUrl = snapshot.getString(ATTACHMENT);
                                        Banner banner = new Banner(imageUrl, id);
                                        banners.add(banner);
                                        banner2.add(imageUrl);
                                        imageList.add(imageUrl);
                                    }

                                    addImageDataFromCompose(banners, imageList);

                                } else {
                                    mAttachmentTextView.setText("0 " + ATTACHMENT);
                                    //sliderView.setVisibility(View.GONE);
                                    addImageDataFromCompose(banners, imageList);
                                }

                            } else {
                                mAttachmentTextView.setText("0 " + ATTACHMENT);
                                addImageDataFromCompose(banners, imageList);
                            }

                        } else {
                            addImageDataFromCompose(banners, imageList);

                            Toast.makeText(ViewSharedCorrespondingDetail.this, "load again: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


        firestore.collection("Users")
                .document(user.getUid())
                .collection(SHARED_DOCUMENT_QUERY)
                .document(Id)
                .collection(PDF_ATTACHMENT).whereEqualTo(IS_ACTIVE, true)//.orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e == null) {
                            if (queryDocumentSnapshots != null) {
                                if (queryDocumentSnapshots.size() != 0) {

                                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                        String pdfUrl = snapshot.getString(DOCUMENT);
                                        pdfList.add(pdfUrl);
                                        //letterNumber.add(snapshot.getString(LETTER_NUMBER));
                                    }
                                } else {
                                    //PdfAttachmentCounterTv.setVisibility(View.GONE);
                                }

                            } else {
                                //PdfAttachmentCounterTv.setVisibility(View.GONE);
                            }

                        } else {
                            PdfAttachmentCounterTv.setVisibility(View.GONE);
                            Toast.makeText(ViewSharedCorrespondingDetail.this, "load again: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });

        firestore.collection("Users")
                .document(user.getUid())
                .collection(SHARED_DOCUMENT_QUERY)
                .document(Id).collection(COMPOSE)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e == null) {
                            if (queryDocumentSnapshots != null) {
                                if (queryDocumentSnapshots.size() != 0) {


                                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                        if (snapshot.get(PDF_ATTACHMENT) != null) {

                                            List<Map<String, Object>> maps = (List<Map<String, Object>>) snapshot.getData().get(PDF_ATTACHMENT);
                                            //int count = maps.size();

                                            // Toast.makeText(ShowComposeDetail.this, "" + count, Toast.LENGTH_SHORT).show();
                                            for (int a = 0; a < maps.size(); a++) {
                                                String pdfUrl = maps.get(a).get(DOCUMENT).toString();
                                                pdfList.add(pdfUrl);
                                            }

                                            PdfAttachmentCounterTv.setVisibility(View.VISIBLE);
                                            PdfAttachmentCounterTv.setText(pdfList.size() + " " + PDF_ATTACHMENT);
                                        }

                                    }

                                } else {
                                    PdfAttachmentCounterTv.setVisibility(View.VISIBLE);
                                    PdfAttachmentCounterTv.setText(pdfList.size() + " " + PDF_ATTACHMENT);
                                }

                            } else {
                                PdfAttachmentCounterTv.setVisibility(View.VISIBLE);
                                PdfAttachmentCounterTv.setText(pdfList.size() + " " + PDF_ATTACHMENT);
                            }

                        } else {
                            PdfAttachmentCounterTv.setVisibility(View.VISIBLE);
                            PdfAttachmentCounterTv.setText(pdfList.size() + " " + PDF_ATTACHMENT);

                        }
                    }
                });

        //Setting Image Counter
        try {

            firestore.collection("Users")
                    .document(user.getUid())
                    .collection(SHARED_DOCUMENT_QUERY)
                    .document(Id).collection(IMAGE_ATTACHMENT).whereEqualTo(IS_ACTIVE, true)//.orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                    .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e == null) {
                                if (queryDocumentSnapshots != null) {
                                    if (queryDocumentSnapshots.size() != 0) {

                                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                            String pdfUrl = snapshot.getString(ATTACHMENT);
                                            imageCounterList.add(pdfUrl);
                                            //letterNumber.add(snapshot.getString(LETTER_NUMBER));
                                        }
                                    } else {
                                        //PdfAttachmentCounterTv.setVisibility(View.GONE);
                                    }

                                } else {
                                    //PdfAttachmentCounterTv.setVisibility(View.GONE);
                                }

                            } else {
                                PdfAttachmentCounterTv.setVisibility(View.GONE);
                                Toast.makeText(ViewSharedCorrespondingDetail.this, "load again: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
            firestore.collection("Users")
                    .document(user.getUid())
                    .collection(SHARED_DOCUMENT_QUERY)
                    .document(Id).collection(COMPOSE)
                    .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e == null) {
                                if (queryDocumentSnapshots != null) {
                                    if (queryDocumentSnapshots.size() != 0) {


                                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                            if (snapshot.get(IMAGE_ATTACHMENT) != null) {

                                                List<Map<String, Object>> maps = (List<Map<String, Object>>) snapshot.getData().get(IMAGE_ATTACHMENT);

                                                for (int a = 0; a < maps.size(); a++) {
                                                    String pdfUrl = maps.get(a).get(ATTACHMENT).toString();
                                                    imageCounterList.add(pdfUrl);
                                                }

                                                mImageCounterView.setVisibility(View.VISIBLE);
                                                mImageCounterView.setText(imageCounterList.size() + " " + IMAGE_ATTACHMENT);
                                            }

                                        }

                                    } else {
                                        mImageCounterView.setVisibility(View.VISIBLE);
                                        mImageCounterView.setText(imageCounterList.size() + " " + IMAGE_ATTACHMENT);
                                    }

                                } else {
                                    mImageCounterView.setVisibility(View.VISIBLE);
                                    mImageCounterView.setText(imageCounterList.size() + " " + IMAGE_ATTACHMENT);
                                }

                            } else {
                                mImageCounterView.setVisibility(View.VISIBLE);
                                mImageCounterView.setText(imageCounterList.size() + " " + IMAGE_ATTACHMENT);

                            }
                        }
                    });
        } catch (Exception e) {
        }
    }

    private void addImageDataFromCompose(final List<Banner> banners, final ArrayList<String> imageList) {

        firestore.collection("Users")
                .document(user.getUid())
                .collection(SHARED_DOCUMENT_QUERY)
                .document(Id)
                .collection(COMPOSE).whereEqualTo(IS_ACTIVE, true)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e == null && !queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot snapshot : snapshots) {
                                if (snapshot.get(IMAGE_ATTACHMENT) != null) {
                                    List<Map<String, Object>> maps = (List<Map<String, Object>>) snapshot.getData().get(IMAGE_ATTACHMENT);
                                    String id = snapshot.getId();
                                    for (int a = 0; a < maps.size(); a++) {
                                        String imageUrl = maps.get(a).get(ATTACHMENT).toString();
                                        Banner banner = new Banner(imageUrl, id);
                                        banners.add(banner);
                                        imageList.add(imageUrl);
                                    }

                                }

                            }
                            sliderAdapterExample.notifyDataSetChanged();
                            sliderView.setVisibility(View.VISIBLE);
                        } else {
                            sliderAdapterExample.notifyDataSetChanged();
                            sliderView.setVisibility(View.VISIBLE);
                        }
                    }
                });


        if (banners.isEmpty()) {
            sliderView.setVisibility(View.VISIBLE);
        }
    }

    private void findViewById() {

        mAttachmentTextView = (TextView) findViewById(R.id.attachment);
        PdfAttachmentCounterTv = (TextView) findViewById(R.id.pdf_attachment);
        mSubject = (TextView) findViewById(R.id.textView5);
        mDepartment = (TextView) findViewById(R.id.textView6);
        mDescription = (TextView) findViewById(R.id.dep);
        mDate = (TextView) findViewById(R.id.date__);
        mLetterNumber = (TextView) findViewById(R.id.letter_number01);
        mTAG = (TextView) findViewById(R.id.textView_tag);
        mTimestamp = (TextView) findViewById(R.id.time);
        mReminder = (TextView) findViewById(R.id.reminder);
        mStatusTv = (TextView) findViewById(R.id.status);
        mChangeStatusBtn = (TextView) findViewById(R.id.change_status);
        mImageCounterView = (TextView) findViewById(R.id.image_attachment);

    }


    private void setToolbar(Toolbar toolbar, String id) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(id);
    }


    public void gotoAttachmentScreen(View view) {
        if (pdfList != null && !pdfList.isEmpty()) {
            startActivity(new Intent(this, PdfListWithLetterNumberActivity.class)
                    .putExtra("c_id", Id)
                    .putExtra(FROM, SHARED_ACTIVITY));
        }

    }


    public void gotoComposeScreen(View view) {
        startActivity(new Intent(ViewSharedCorrespondingDetail.this, ComposeReply.class)
                .putExtra(SUBJECT, Subject)
                .putExtra(DEPARTMENT, Department)
                .putExtra(FROM, "composeReply")
                .putExtra(ID, Id));
    }

    public void viewAllReply(View view) {
        startActivity(new Intent(ViewSharedCorrespondingDetail.this, ViewAllReplyScreen.class)
                .putExtra(ID, Id)
                .putExtra(TITLE, Title));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (Id != null) {
                //setLastReply(Id);
                loadComposeData();
            }
        }
    }

    public void gotoPdfScreen(View view) {

        if (pdfList != null && !pdfList.isEmpty()) {
            startActivity(new Intent(ViewSharedCorrespondingDetail.this, PdfScreen.class).putExtra("TYPE", "PDF")
                    .putExtra("pdfs", pdfList));
        }

    }


    public void changeStatus(final View view) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(CHANGING_STATUS);
        String msg;
        if (mStatusTv.getText().toString().equalsIgnoreCase(STATUS_OPEN)) {
            msg = "want to close the Correspondence??";
        } else {
            msg = "want to re-open the Correspondence??";
        }
        new AlertDialog.Builder(ViewSharedCorrespondingDetail.this)
                .setMessage(msg)
                .setPositiveButton(YES, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        progressDialog.show();
                        if (mStatusTv.getText().toString().equalsIgnoreCase("Status : Open")) {
                            firestore.collection(CORRESPONDENCE_QUERY).document(Id)
                                    .update(IS_ACTIVE, false);
                            progressDialog.dismiss();
                            mStatusTv.setText(STATUS_CLOSED);
                            mStatusTv.setTextColor(Color.RED);
                            mChangeStatusBtn.setText(RE_OPEN);

                            Snackbar.make(view, "Correspondence Closed", Snackbar.LENGTH_SHORT).show();
                        } else {
                            firestore.collection(CORRESPONDENCE_QUERY).document(Id)
                                    .update(IS_ACTIVE, true);
                            progressDialog.dismiss();
                            mStatusTv.setText(STATUS_OPEN);
                            mStatusTv.setTextColor(Color.GREEN);
                            mChangeStatusBtn.setText(CLOSE);
                            Snackbar.make(view, "Correspondence Re-open", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton(CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        }).show();
    }

    public void addReply(View view) {
        startActivity(new Intent(ViewSharedCorrespondingDetail.this, ComposeReply.class)
                .putExtra(SUBJECT, Subject)
                .putExtra(DEPARTMENT, Department)
                .putExtra(FROM, "addReply")
                .putExtra(ID, Id));
    }

    public void viewAllLetters(View view) {
        startActivity(new Intent(ViewSharedCorrespondingDetail.this, ViewAllLettersActivity.class)
                .putExtra(ID, Id)
                .putExtra(FROM,"Shared")
                .putExtra("pdfs", banner2));

    }

    private class ComposeAdapter2 extends RecyclerView.Adapter<ComposeAdapter2.MyViewHolder> {

        List<DocumentSnapshot> list;
        Context context;
        String cor_id;
        String LNumber;
        String LType;

        public ComposeAdapter2(List<DocumentSnapshot> list, Context context, String cor_id, String LNumber, String LType) {
            this.list = list;
            this.context = context;
            this.cor_id = cor_id;
            this.LNumber = LNumber;
            this.LType = LType;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.doc_view, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {

            try {
                String letterDate = list.get(position).getString(DATE);
                String letterNumber = list.get(position).getString(LETTER_NUMBER);
                String type = list.get(position).getString(TYPE);
                String remark = list.get(position).getString(REMARK);
                String tag = list.get(position).getString(TAG);

                final int pos = position + 1;
                holder.mSerialnumber.setText("" + pos);

                holder.mLetterDate.setText(letterDate);
                holder.mLetterNumber.setText(letterNumber);
                if (type.equalsIgnoreCase("Received"))
                    holder.mType.setText("R");
                else
                    holder.mType.setText("S");


                holder.mRemark.setText(remark);

                if (tag != null && !tag.isEmpty()) {
                    holder.mDepartment.setText(tag);
                    if (tag.equalsIgnoreCase(REPLIED)) {
                        holder.mDepartment.setTextColor(Color.GREEN);
                    } else if (tag.equalsIgnoreCase(UN_REPLIED)) {
                        holder.mDepartment.setTextColor(Color.RED);
                    } else if (tag.equalsIgnoreCase(PARTIALLY_REPLIED))
                        holder.mDepartment.setTextColor(Color.MAGENTA);
                } else {
                    holder.mDepartment.setTextColor(Color.GRAY);
                    holder.mDepartment.setText(NO_TAG);
                }

                holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<String> image_urls = new ArrayList<>();
                        ArrayList<String> pdf_urls = new ArrayList<>();
                        if (list.get(position).get(PDF_ATTACHMENT) != null) {

                            List<Map> images_map = (List<Map>) list.get(position).get(PDF_ATTACHMENT);
                            for (int a = 0; a < pdf_urls.size(); a++) {
                                pdf_urls.add((String) images_map.get(a).get(DOCUMENT));
                            }

                        }
                        if (list.get(position).get(IMAGE_ATTACHMENT) != null) {

                            List<Map> images_map = (List<Map>) list.get(position).get(IMAGE_ATTACHMENT);
                            for (int a = 0; a < images_map.size(); a++) {
                                image_urls.add((String) images_map.get(a).get(ATTACHMENT));
                            }

                        }


                        context.startActivity(new Intent(context, ShowComposeDetail.class)

                                .putStringArrayListExtra("image_urls", image_urls)
                                .putStringArrayListExtra("pdf_urls", pdf_urls)
                                .putExtra(SUBJECT, list.get(position).getString(SUBJECT))
                                .putExtra(ID, list.get(position).getId())
                                .putExtra(TYPE, LType)
                                .putExtra(FROM, SHARED_ACTIVITY)
                                .putExtra("CORRESPONDENCE_ID", cor_id)
                                .putExtra(TIMESTAMP, "" + list.get(position).getLong(TIMESTAMP))
                                .putExtra(REMARK, list.get(position).getString(REMARK))
                                .putExtra(DATE, list.get(position).getString(DATE))
                                .putExtra(LETTER_NUMBER, LNumber)
                                .putExtra(LETTER_DETAIL, list.get(position).getString(LETTER_DETAIL))
                                .putExtra(DEPARTMENT, list.get(position).getString(DEPARTMENT)));

                    }
                });


            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        public int getItemCount() {
            return list.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView mSerialnumber, mLetterDate, mLetterNumber, mDepartment, mType, mDocumentNumber, mRemark;
            LinearLayout linearLayout;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);


                mSerialnumber = (TextView) itemView.findViewById(R.id.textView21);
                mLetterDate = (TextView) itemView.findViewById(R.id.textView19);
                mLetterNumber = (TextView) itemView.findViewById(R.id.textView20);
                mDepartment = (TextView) itemView.findViewById(R.id.textView18);
                mType = (TextView) itemView.findViewById(R.id.textView15);
                mRemark = (TextView) itemView.findViewById(R.id.textView17);
                linearLayout = (LinearLayout) itemView.findViewById(R.id.table_lay);

                mLetterNumber.setPaintFlags(mLetterNumber.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            }
        }
    }
}
