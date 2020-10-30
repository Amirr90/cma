package com.jccode.mycorrespondence;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
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
import static com.jccode.mycorrespondence.utility.Util.DOCUMENT_QUERY;
import static com.jccode.mycorrespondence.utility.Util.FILED;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.IMAGE;
import static com.jccode.mycorrespondence.utility.Util.IMAGE_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE;
import static com.jccode.mycorrespondence.utility.Util.LETTER_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.MAIN_ACTIVITY;
import static com.jccode.mycorrespondence.utility.Util.PARTIALLY_REPLIED;
import static com.jccode.mycorrespondence.utility.Util.PDF_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.REMINDER;
import static com.jccode.mycorrespondence.utility.Util.REPLIED;
import static com.jccode.mycorrespondence.utility.Util.REPLY;
import static com.jccode.mycorrespondence.utility.Util.SHARED_ACTIVITY;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TITLE;
import static com.jccode.mycorrespondence.utility.Util.TYPE;
import static com.jccode.mycorrespondence.utility.Util.UID;
import static com.jccode.mycorrespondence.utility.Util.UN_REPLIED;
import static com.jccode.mycorrespondence.utility.Util.USERS;
import static com.jccode.mycorrespondence.utility.Util.YES;

public class CorrespondingDetail extends AppCompatActivity {
    private static final String TAG = "CorrespondingDetail";
    private static final String STATUS_CLOSED = "Status : Closed";
    private static final String STATUS_OPEN = "Status : Open";
    private static final String RE_OPEN = "Re-open";
    private static final String CLOSE = "Close";
    private static final CharSequence CHANGING_STATUS = "changing status...";
    public String Title, Description, LetterType, Subject, Id, Timestamp, post_uid, reminder, Status, Department, Date, LetterNumber, Type, Tag;
    TextView mSubject, mDescription, mTimestamp, mLetterType, mReminder, mStatusTv, mChangeStatusBtn, mDepartment, mDate, mLetterNumber, mTAG;
    public TextView mAttachmentTextView, PdfAttachmentCounterTv, mImageCounterView;
    FirebaseFirestore firestore;
    TimeAgo timeAgo;

    ArrayList<String> imageList = new ArrayList<>();
    ArrayList<String> imageCounterList = new ArrayList<>();
    ArrayList<String> pdfList = new ArrayList<>();
    ArrayList<String> banner2 = new ArrayList<>();


    RecyclerView recyclerView;
    List<DocumentSnapshot> list = new ArrayList<>();
    ComposeAdapter composeAdapter;
    SliderView sliderView;
    List<Banner> banners = new ArrayList<>();
    SliderAdapterExample sliderAdapterExample;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corresponding_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.correspondence_toolbar);
        sliderView = (SliderView) findViewById(R.id.imageSlider);

        progressDialog = new ProgressDialog(this);
        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        findViewById();
        if (getIntent().hasExtra(ID)) {
            Id = getIntent().getStringExtra(ID);
            Title = getIntent().getStringExtra(TITLE);
            Description = getIntent().getStringExtra(DESCRIPTION);
            LetterType = getIntent().getStringExtra(TYPE);
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
            setLastReply(Id);

            sliderAdapterExample = new SliderAdapterExample(banners, CorrespondingDetail.this);
            sliderView.setSliderAdapter(sliderAdapterExample);


        }

    }


    private void setComposeData() {
        composeAdapter = new ComposeAdapter(list, this, Id, LetterNumber, Type);
        recyclerView = (RecyclerView) findViewById(R.id.doc_rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.hasFixedSize();
        recyclerView.setAdapter(composeAdapter);
        loadComposeData();

    }

    private void loadComposeData() {
        firestore.collection(CORRESPONDENCE_QUERY)
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
                            Toast.makeText(CorrespondingDetail.this, "load again: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setLastReply(String id) {
        firestore.collection(CORRESPONDENCE_QUERY).document(Id).collection(REPLY)
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot snapshot = queryDocumentSnapshots.getDocuments().get(0);
                            TextView reply = (TextView) findViewById(R.id.textView12);
                            TextView reply_count = (TextView) findViewById(R.id.all_reply);
                            if (queryDocumentSnapshots.size() > 1)
                                reply_count.setText(queryDocumentSnapshots.size() - 1 + " more reply");
                            else {
                                reply_count.setText("more reply");
                            }
                            TextView timestamp = (TextView) findViewById(R.id.reply_time);
                            reply.setText(snapshot.getString(REPLY));
                            long stamp = snapshot.getLong(TIMESTAMP);
                            timeAgo = new TimeAgo();
                            timestamp.setText(timeAgo.getlongtoago(stamp));

                            getUserDetail(snapshot.getString(UID));
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }
        });
    }

    private void getUserDetail(String uid) {

        firestore.collection(USERS).document(uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {

                        TextView username_tv = (TextView) findViewById(R.id.textView11);
                        ImageView imageView = (ImageView) findViewById(R.id.profile_image2);
                        String imageUrl = snapshot.getString(IMAGE);
                        if (imageUrl != null && !imageUrl.equals(""))
                            Picasso.with(CorrespondingDetail.this).load(imageUrl).placeholder(R.drawable.profile).into(imageView);
                        else {
                            imageView.setImageResource(R.drawable.profile);
                        }

                    }
                });

    }

    private void setCorrespondenceData() {
        mSubject.setText(Subject);
        mDescription.setText(Description);
        mLetterType.setText("Type: " + LetterType);
        mDepartment.setText(Department);
        mDate.setText(Date);
        mLetterNumber.setText(LetterNumber);
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

        firestore.collection(CORRESPONDENCE_QUERY)
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

                            Toast.makeText(CorrespondingDetail.this, "load again: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


        firestore.collection(CORRESPONDENCE_QUERY)
                .document(Id).collection(PDF_ATTACHMENT).whereEqualTo(IS_ACTIVE, true)//.orderBy(TIMESTAMP, Query.Direction.DESCENDING)
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
                            Toast.makeText(CorrespondingDetail.this, "load again: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });

        firestore.collection(CORRESPONDENCE_QUERY)
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

            firestore.collection(CORRESPONDENCE_QUERY)
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
                                Toast.makeText(CorrespondingDetail.this, "load again: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
            firestore.collection(CORRESPONDENCE_QUERY)
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

        firestore.collection(CORRESPONDENCE_QUERY).document(Id)
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
        mLetterType = (TextView) findViewById(R.id.type3);
        mStatusTv = (TextView) findViewById(R.id.status);
        mChangeStatusBtn = (TextView) findViewById(R.id.change_status);
        mImageCounterView = (TextView) findViewById(R.id.image_attachment);

        mTAG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUpdateTagDialog(0, view);
            }
        });


    }

    private void update(final String data, final String query, final TextView textView, final String prefix) {
        firestore.collection(CORRESPONDENCE_QUERY).document(Id)
                .update(query, data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        textView.setText(data);
                        Toast.makeText(CorrespondingDetail.this, "updated", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(CorrespondingDetail.this, "try again", Toast.LENGTH_SHORT).show();
            }
        });
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
                    .putExtra(FROM, MAIN_ACTIVITY));
        }

    }


    public void gotoComposeScreen(View view) {
        Snackbar.make(view, "Coming Soon", Snackbar.LENGTH_SHORT).show();
        /*startActivity(new Intent(CorrespondingDetail.this, ComposeReply.class)
                .putExtra(SUBJECT, Subject)
                .putExtra(DEPARTMENT, Department)
                .putExtra(FROM, "composeReply")
                .putExtra(ID, Id));*/
    }

    public void viewAllReply(View view) {
        startActivity(new Intent(CorrespondingDetail.this, ViewAllReplyScreen.class)
                .putExtra(ID, Id)
                .putExtra(TITLE, Title));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (Id != null) {
                loadComposeData();
            }
        }
    }

    public void gotoPdfScreen(View view) {

        if (pdfList != null && !pdfList.isEmpty()) {
            startActivity(new Intent(CorrespondingDetail.this, PdfScreen.class).putExtra("TYPE", "PDF")
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
        new AlertDialog.Builder(CorrespondingDetail.this)
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
        startActivity(new Intent(CorrespondingDetail.this, ComposeReply.class)
                .putExtra(SUBJECT, Subject)
                .putExtra(DEPARTMENT, Department)
                .putExtra(FROM, "addReply")
                .putExtra(ID, Id));
    }

    public void viewAllLetters(View view) {
        startActivity(new Intent(CorrespondingDetail.this, ViewAllLettersActivity.class)
                .putExtra(ID, Id)
                .putExtra(FROM, "Main")
                .putExtra("pdfs", banner2));

    }


    private void showUpdateTagDialog(final int position, final View view) {
        final CharSequence[] options = {FILED, UN_REPLIED, PARTIALLY_REPLIED, REPLIED, DELETE_LETTER};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Update TAG");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String TAG = options[which].toString();
                if (TAG.equalsIgnoreCase("Delete Letter")) {
                    deleteLetter(position, view);
                } else {
                    updateTAG(TAG, position, view);
                }
            }
        });
        builder.show();
    }

    private void updateTAG(final String tag, int position, final View view) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Updating TAG");
        progressDialog.setMessage("please wait....");
        progressDialog.show();
        Map<String, Object> updateTAGMap = new HashMap<>();
        updateTAGMap.put(Util.TAG, tag);
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(CORRESPONDENCE_QUERY).document(Id)
                .update(updateTAGMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Snackbar.make(view, "TAG updated successfully", Snackbar.LENGTH_SHORT).show();
                mTAG.setText("TAG: " + tag);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Snackbar.make(view, "could't update TAG, please try again", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteLetter(final int position, final View view) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Letter")
                .setMessage("Do you really want to  Delete this Letter ??")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        String compose_id = list.get(position).getId();
                        final ProgressDialog dialog = new ProgressDialog(CorrespondingDetail.this);
                        dialog.setMessage("Deleting");
                        dialog.show();
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.collection(CORRESPONDENCE_QUERY).document(Id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //notifyDataSetChanged();
                                Snackbar.make(view, "Letter Deleted", Snackbar.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(view, "could't delete Letter, please try again", Snackbar.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        }).show();
    }


    public void updateCorrespondenceData(final View view) {
        int Id = view.getId();
        TextView editText = findViewById(view.getId());
        String edText = editText.getText().toString();
        switch (Id) {
            case R.id.textView5: {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CorrespondingDetail.this);
                LayoutInflater inflater = CorrespondingDetail.this.getLayoutInflater();
                View view1 = inflater.inflate(R.layout.update_data_layout, null);
                final EditText ed = (EditText) view1.findViewById(R.id.update1);
                ed.setHint("Subject");
                ed.setText(edText);

                builder.setTitle("Update Subject");
                builder.setView(view1)
                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                final String data = ed.getText().toString();
                                if (data.isEmpty()) {
                                    Snackbar.make(view, "Please enter data to update", Snackbar.LENGTH_SHORT).show();
                                } else {
                                    progressDialog.setTitle("Updating Subject");
                                    progressDialog.setMessage("please wait...");
                                    progressDialog.show();
                                    update(data, SUBJECT, mSubject, "");
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
            break;

            case R.id.dep: {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CorrespondingDetail.this);
                LayoutInflater inflater = CorrespondingDetail.this.getLayoutInflater();
                View view1 = inflater.inflate(R.layout.update_data_layout, null);
                final EditText ed = (EditText) view1.findViewById(R.id.update1);
                ed.setHint("Description");
                ed.setText(edText);
                builder.setTitle("Update Description");
                builder.setView(view1)
                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                final String data = ed.getText().toString();
                                if (data.isEmpty()) {
                                    Snackbar.make(view, "Please enter data to update", Snackbar.LENGTH_SHORT).show();
                                } else {
                                    progressDialog.setTitle("Updating Description");
                                    progressDialog.setMessage("please wait...");
                                    progressDialog.show();
                                    update(data, DESCRIPTION, mDescription, "Description:\n");
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
            break;

            case R.id.date__: {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view2 = inflater.inflate(R.layout.date_picker, null, false);

                final DatePicker myDatePicker = (DatePicker) view2.findViewById(R.id.myDatePicker);

                myDatePicker.setCalendarViewShown(false);

                new AlertDialog.Builder(CorrespondingDetail.this).setView(view2)
                        .setTitle("Set Date")
                        .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                            @TargetApi(11)
                            public void onClick(DialogInterface dialog, int id) {
                                int month = myDatePicker.getMonth() + 1;
                                int day = myDatePicker.getDayOfMonth();
                                int year = myDatePicker.getYear();

                                String date = (day + "/" + month + "/" + year);

                                dialog.cancel();
                                progressDialog.setTitle("Updating Subject");
                                progressDialog.setMessage("please wait...");
                                progressDialog.show();
                                update(date, DATE, mDate, "Letter Date: ");

                            }

                        }).show();
            }
            break;

            case R.id.letter_number01: {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CorrespondingDetail.this);
                LayoutInflater inflater = CorrespondingDetail.this.getLayoutInflater();
                View view1 = inflater.inflate(R.layout.update_data_layout, null);
                final EditText ed = (EditText) view1.findViewById(R.id.update1);
                ed.setHint("Letter number");
                ed.setText(edText);
                builder.setTitle("Update Letter number");
                builder.setView(view1)
                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // your sign in code here
                                final String data = ed.getText().toString();
                                if (data.isEmpty()) {
                                    Snackbar.make(view, "Please enter data to update", Snackbar.LENGTH_SHORT).show();
                                } else {
                                    progressDialog.setTitle("Updating Letter number");
                                    progressDialog.setMessage("please wait...");
                                    progressDialog.show();
                                    update(data, LETTER_NUMBER, mLetterNumber, "Letter Number: ");
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

            case R.id.textView6: {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CorrespondingDetail.this);
                LayoutInflater inflater = CorrespondingDetail.this.getLayoutInflater();
                View view1 = inflater.inflate(R.layout.update_data_layout, null);
                final EditText ed = (EditText) view1.findViewById(R.id.update1);
                ed.setHint("Department");
                ed.setText(edText);
                builder.setTitle("Update Department");
                builder.setView(view1)
                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // your sign in code here
                                final String data = ed.getText().toString();
                                if (data.isEmpty()) {
                                    Snackbar.make(view, "Please enter data to update", Snackbar.LENGTH_SHORT).show();
                                } else {
                                    progressDialog.setTitle("Updating Department");
                                    progressDialog.setMessage("please wait...");
                                    progressDialog.show();
                                    update(data, DEPARTMENT, mDepartment, "Department: ");
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
        }
    }
}
