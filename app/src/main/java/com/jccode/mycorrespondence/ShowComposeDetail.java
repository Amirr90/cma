package com.jccode.mycorrespondence;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dinuscxj.refresh.RecyclerRefreshLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.jccode.mycorrespondence.utility.DownloadTask;
import com.jccode.mycorrespondence.utility.TimeAgo;
import com.jccode.mycorrespondence.utility.Util;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.jccode.mycorrespondence.utility.Util.COMPOSE;
import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DATE;
import static com.jccode.mycorrespondence.utility.Util.DEPARTMENT;
import static com.jccode.mycorrespondence.utility.Util.DOCUMENT;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE;
import static com.jccode.mycorrespondence.utility.Util.LETTER_DETAIL;
import static com.jccode.mycorrespondence.utility.Util.LETTER_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.PDF_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.REMARK;
import static com.jccode.mycorrespondence.utility.Util.SENDER_DETAIL;
import static com.jccode.mycorrespondence.utility.Util.SHARED_ACTIVITY;
import static com.jccode.mycorrespondence.utility.Util.SHARED_DOCUMENT_QUERY;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TYPE;


public class ShowComposeDetail extends AppCompatActivity {

    String doc_id, cor_id, from, Subject, Department, Type, LetterNumber;
    FirebaseFirestore firestore;
    TimeAgo timeAgo;
    SharedPreferences pref;
    FirebaseUser user;
    Util util;
    ArrayList<String> pdf_urls = new ArrayList<>();

    ArrayList<String> pdfList = new ArrayList<>();

    RecyclerView recyclerView;

    TextView PdfAttachmentCounterTv;

    StringBuilder sent;
    StringBuilder received;
    LinearLayout linearLayout;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_compose_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.compose_screen_toolbar);

        PdfAttachmentCounterTv = (TextView) findViewById(R.id.pdf_counter);
        user = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        timeAgo = new TimeAgo();
        setToolbar(toolbar, "Compose Detail");

        util = new Util();
        findViewById();
        if (getIntent().hasExtra("image_urls")) {

            pdf_urls = getIntent().getStringArrayListExtra("pdf_urls");
            Subject = getIntent().getStringExtra(SUBJECT);
            LetterNumber = getIntent().getStringExtra(LETTER_NUMBER);
            Type = getIntent().getStringExtra(TYPE);
            Department = getIntent().getStringExtra(DEPARTMENT);
            doc_id = getIntent().getStringExtra(ID);
            from = getIntent().getStringExtra(FROM);
            cor_id = getIntent().getStringExtra("CORRESPONDENCE_ID");
            setComposeDetail(doc_id, cor_id);

            if (!pdf_urls.isEmpty()) {

            }
        }
    }

    private void setComposeDetail(final String doc_id, String c_id) {

        final ArrayList<String> image_collection = getIntent().getStringArrayListExtra("image_urls");

        TextView subject = (TextView) findViewById(R.id.m_subject);
        subject.setText("Subject: " + getIntent().getStringExtra(SUBJECT));

        TextView timeStamp = (TextView) findViewById(R.id.m_time);
        long TimeStamp = Long.parseLong(getIntent().getStringExtra(TIMESTAMP));
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String dateString = formatter.format(new Date(TimeStamp));
        timeStamp.setText("Updated at: " + dateString);


        TextView date = (TextView) findViewById(R.id.m_date);
        date.setText("Letter Date: " + getIntent().getStringExtra(DATE));

        final TextView letter_detail = (TextView) findViewById(R.id.m_desc);
        letter_detail.setText("Letter Detail: " + getIntent().getStringExtra(LETTER_DETAIL));

        TextView department = (TextView) findViewById(R.id.m_dep);
        department.setText("Department: " + getIntent().getStringExtra(DEPARTMENT));
        TextView ImageTitle = (TextView) findViewById(R.id.image_title);
        recyclerView = (RecyclerView) findViewById(R.id.rec_all_compose_image);
        recyclerView.setLayoutManager(new GridLayoutManager(ShowComposeDetail.this, 4));
        recyclerView.setHasFixedSize(true);
        if (!image_collection.isEmpty()) {
            ImageTitle.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(new ImageAdapter(image_collection, this));
        }


        if (from.equalsIgnoreCase(SHARED_ACTIVITY)) {
            linearLayout.setVisibility(View.GONE);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            firestore.collection("Users")
                    .document(user.getUid())
                    .collection(SHARED_DOCUMENT_QUERY)
                    .document(c_id)
                    .collection(COMPOSE).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots != null) {
                        sent = new StringBuilder();
                        received = new StringBuilder();
                        if (Type.equalsIgnoreCase("Sent")) {
                            sent.append(LetterNumber + ",");
                        } else {
                            received.append(LetterNumber + ",");
                        }

                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            if (snapshot.getString(TYPE).equalsIgnoreCase("Sent")) {
                                sent.append(snapshot.getString(LETTER_NUMBER) + ",");
                            } else {
                                received.append(snapshot.getString(LETTER_NUMBER) + ",");
                            }
                        }

                        TextView tv_sent = (TextView) findViewById(R.id.textView30);
                        TextView tv_received = (TextView) findViewById(R.id.textView29);
                        if (sent != null)
                            tv_sent.setText(sent.toString());
                        if (received != null)
                            tv_received.setText(received.toString());

                    }
                }
            });


            firestore.collection("Users")
                    .document(user.getUid())
                    .collection(SHARED_DOCUMENT_QUERY)
                    .document(c_id).collection(COMPOSE)
                    .document(doc_id)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.getResult() != null) {

                                DocumentSnapshot snapshot = task.getResult();
                                TextView letter_number = (TextView) findViewById(R.id.m_letter_number);
                                letter_number.setText("Letter number: " + snapshot.get(LETTER_NUMBER));

                                TextView senderDetail = (TextView) findViewById(R.id.senderdetail);
                                String mSenderDetail = snapshot.get(SENDER_DETAIL).toString();
                                senderDetail.setText("Sender Details: " + mSenderDetail);

                                TextView letterDetail = (TextView) findViewById(R.id.letterdetail);
                                String mLetterDetail = snapshot.get(LETTER_DETAIL).toString();
                                letterDetail.setText("Letter Details: " + mLetterDetail);


                                TextView remark = (TextView) findViewById(R.id.m_remark);
                                String mRemark = snapshot.get(REMARK).toString();
                                remark.setText("Remark: " + mRemark);

                                if (mRemark.equalsIgnoreCase(""))
                                    remark.setVisibility(View.GONE);
                                else
                                    remark.setVisibility(View.VISIBLE);

                                if (mSenderDetail.equalsIgnoreCase(""))
                                    senderDetail.setVisibility(View.GONE);
                                else
                                    senderDetail.setVisibility(View.VISIBLE);

                                if (mLetterDetail.equalsIgnoreCase(""))
                                    letterDetail.setVisibility(View.GONE);
                                else
                                    letterDetail.setVisibility(View.VISIBLE);


                                if (snapshot.get(PDF_ATTACHMENT) != null) {

                                    List<Map<String, Object>> maps = (List<Map<String, Object>>) snapshot.getData().get(PDF_ATTACHMENT);
                                    for (int a = 0; a < maps.size(); a++) {
                                        String pdfUrl = maps.get(a).get(DOCUMENT).toString();
                                        pdfList.add(pdfUrl);
                                    }

                                    PdfAttachmentCounterTv.setVisibility(View.VISIBLE);
                                    PdfAttachmentCounterTv.setText(pdfList.size() + " " + PDF_ATTACHMENT);

                                } else {
                                    PdfAttachmentCounterTv.setVisibility(View.GONE);
                                }

                            }
                        }
                    });
        } else {
            linearLayout.setVisibility(View.VISIBLE);
            firestore.collection(CORRESPONDENCE_QUERY).document(c_id)
                    .collection(COMPOSE).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots != null) {
                        sent = new StringBuilder();
                        received = new StringBuilder();
                        if (Type.equalsIgnoreCase("Sent")) {
                            sent.append(LetterNumber + ",");
                        } else {
                            received.append(LetterNumber + ",");
                        }

                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            if (snapshot.getString(TYPE).equalsIgnoreCase("Sent")) {
                                sent.append(snapshot.getString(LETTER_NUMBER) + ",");
                            } else {
                                received.append(snapshot.getString(LETTER_NUMBER) + ",");
                            }
                        }

                        TextView tv_sent = (TextView) findViewById(R.id.textView30);
                        TextView tv_received = (TextView) findViewById(R.id.textView29);
                        if (sent != null)
                            tv_sent.setText(sent.toString());
                        if (received != null)
                            tv_received.setText(received.toString());

                    }
                }
            });


            firestore.collection(CORRESPONDENCE_QUERY)
                    .document(c_id).collection(COMPOSE)
                    .document(doc_id)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.getResult() != null) {

                                DocumentSnapshot snapshot = task.getResult();
                                TextView letter_number = (TextView) findViewById(R.id.m_letter_number);
                                letter_number.setText("Letter number: " + snapshot.get(LETTER_NUMBER));

                                TextView senderDetail = (TextView) findViewById(R.id.senderdetail);
                                String mSenderDetail = snapshot.get(SENDER_DETAIL).toString();
                                senderDetail.setText("Sender Details: " + mSenderDetail);

                                TextView letterDetail = (TextView) findViewById(R.id.letterdetail);
                                String mLetterDetail = snapshot.get(LETTER_DETAIL).toString();
                                letterDetail.setText("Letter Details: " + mLetterDetail);


                                TextView remark = (TextView) findViewById(R.id.m_remark);
                                String mRemark = snapshot.get(REMARK).toString();
                                remark.setText("Remark: " + mRemark);

                                if (mRemark.equalsIgnoreCase(""))
                                    remark.setVisibility(View.GONE);
                                else
                                    remark.setVisibility(View.VISIBLE);

                                if (mSenderDetail.equalsIgnoreCase(""))
                                    senderDetail.setVisibility(View.GONE);
                                else
                                    senderDetail.setVisibility(View.VISIBLE);

                                if (mLetterDetail.equalsIgnoreCase(""))
                                    letterDetail.setVisibility(View.GONE);
                                else
                                    letterDetail.setVisibility(View.VISIBLE);


                                if (snapshot.get(PDF_ATTACHMENT) != null) {

                                    List<Map<String, Object>> maps = (List<Map<String, Object>>) snapshot.getData().get(PDF_ATTACHMENT);
                                    for (int a = 0; a < maps.size(); a++) {
                                        String pdfUrl = maps.get(a).get(DOCUMENT).toString();
                                        pdfList.add(pdfUrl);
                                    }

                                    PdfAttachmentCounterTv.setVisibility(View.VISIBLE);
                                    PdfAttachmentCounterTv.setText(pdfList.size() + " " + PDF_ATTACHMENT);

                                } else {
                                    PdfAttachmentCounterTv.setVisibility(View.GONE);
                                }

                            }
                        }
                    });
        }


    }

    private void findViewById() {
        linearLayout = (LinearLayout) findViewById(R.id.button_layout);
    }


    public void gotoPdfScreen(View view) {

        if (pdfList != null && !pdfList.isEmpty()) {
            startActivity(new Intent(ShowComposeDetail.this, PdfScreen.class).putExtra("TYPE", "PDF")
                    .putExtra("pdfs", pdfList));
        }

    }

    private void setToolbar(Toolbar toolbar, String id) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(id);
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }

    public void addReply(View view) {
        if (doc_id != null) {
            startActivity(new Intent(ShowComposeDetail.this, ComposeReply.class)
                    .putExtra(SUBJECT, Subject)
                    .putExtra(DEPARTMENT, Department)
                    .putExtra(FROM, "addReply")
                    .putExtra(ID, cor_id));
        }
    }

    public void gotoComposeScreen(View view) {
        if (doc_id != null) {
            startActivity(new Intent(ShowComposeDetail.this, ComposeReply.class)
                    .putExtra(SUBJECT, Subject)
                    .putExtra(DEPARTMENT, Department)
                    .putExtra(FROM, "composeReply")
                    .putExtra(ID, cor_id));
        }
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {
        ArrayList<String> image_collection;
        Context context;

        public ImageAdapter(ArrayList<String> image_collection, Context context) {
            this.image_collection = image_collection;
            this.context = context;
        }

        @NonNull
        @Override
        public ImageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attachment_view, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageAdapter.MyViewHolder holder, final int position) {
            String imageUrl = image_collection.get(position);
            if (imageUrl != null && !imageUrl.trim().equals(""))
                Picasso.with(context).load(imageUrl).into(holder.imageView);

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(ShowComposeDetail.this, FullScreenImageScreen.class)
                            .putStringArrayListExtra("images", image_collection));

                }
            });

        }

        @Override
        public int getItemCount() {

            if (image_collection.size() <= 4)
                return image_collection.size();
            else
                return 4;

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
