package com.jccode.mycorrespondence;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.jccode.mycorrespondence.utility.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.jccode.mycorrespondence.utility.Util.COMPOSE;
import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DATE;
import static com.jccode.mycorrespondence.utility.Util.DEPARTMENT;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.LETTER_DETAIL;
import static com.jccode.mycorrespondence.utility.Util.LETTER_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.SENDER_DETAIL;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.TYPE;

public class PreviewScreen extends AppCompatActivity {
    private float MARGIN_LEFT = 20;
    private float MARGIN_TOP = 40;
    String Id, LetterDetail, LetterNumber, Date, ComposeData, SenderData;
    FirebaseFirestore firestore;
    TextView mSubject, mLetterNumber, mDate, mRef, mSignature, mSenderDetail, mTo, mDescription, mComposeData, mSenderData;
    Util util;
    ProgressDialog progressDialog;
    String date, From;
    StringBuilder sent;
    StringBuilder received;
    public static final String FONT = "resources/font/freesans.ttf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_screen);


        Toolbar toolbar = (Toolbar) findViewById(R.id.preview_toolbar);

        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        util = new Util();


        progressDialog = new ProgressDialog(this);

        findViewById();
        if (getIntent().hasExtra(ID)) {
            From = getIntent().getStringExtra(FROM);
            Id = getIntent().getStringExtra(ID);
            LetterDetail = getIntent().getStringExtra(LETTER_DETAIL);
            LetterNumber = getIntent().getStringExtra(LETTER_NUMBER);
            ComposeData = getIntent().getStringExtra(COMPOSE);
            SenderData = getIntent().getStringExtra(SENDER_DETAIL);
            Date = getIntent().getStringExtra(DATE);
            setToolbar(toolbar, "Preview");

            setPreviewData();
            mSignature.setText(util.getSignature(this));
        }
    }

    private void findViewById() {
        mSubject = (TextView) findViewById(R.id.textView24);
        mLetterNumber = (TextView) findViewById(R.id.textView14);
        mComposeData = (TextView) findViewById(R.id.composedata);
        mSenderData = (TextView) findViewById(R.id.senderdata);
        mDate = (TextView) findViewById(R.id.textView23);
        mRef = (TextView) findViewById(R.id.textView25);
        mSignature = (TextView) findViewById(R.id.textView27);
        mSenderDetail = (TextView) findViewById(R.id.textView28);
        mDescription = (TextView) findViewById(R.id.textView_des);
        mTo = (TextView) findViewById(R.id.textView22);
    }

    private void setPreviewData() {


        mLetterNumber.setText("Letter No:" + LetterNumber);
        mDescription.setText("Description:\n" + LetterDetail);

        mComposeData.setText(ComposeData);
        mSenderData.setText(SenderData);
        mDate.setText("Date\n" + Date);

        if (!From.equalsIgnoreCase("composeCorrespondence")) {
            firestore.collection(CORRESPONDENCE_QUERY).document(Id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    if (snapshot != null) {
                        String subject = snapshot.getString(SUBJECT);
                        mSubject.setText("Subject: " + subject);
                        mTo.setText("To,\n" + snapshot.getString(DEPARTMENT));

                    }
                }
            });


            firestore.collection(CORRESPONDENCE_QUERY).document(Id)
                    .collection(COMPOSE).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (queryDocumentSnapshots != null) {
                        sent = new StringBuilder();
                        received = new StringBuilder();
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            if (snapshot.getString(TYPE).equalsIgnoreCase("sent")) {
                                sent.append(snapshot.getString(LETTER_NUMBER) + ",");
                            } else {
                                received.append(snapshot.getString(LETTER_NUMBER) + ",");
                            }
                        }

                        // mRef.setText("Ref:\nOur Letter number\n" + sent.toString() + "\n\nYour Letter number\n" + received.toString());
                        firestore.collection(CORRESPONDENCE_QUERY).document(Id)
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.getResult() != null) {
                                    DocumentSnapshot snapshot = task.getResult();
                                    if (snapshot.getString(TYPE).equalsIgnoreCase("sent")) {
                                        sent.append(snapshot.getString(LETTER_NUMBER) + ",");
                                    } else {
                                        received.append(snapshot.getString(LETTER_NUMBER) + ",");
                                    }
                                    mRef.setText("Ref:\nOur Letter number\n" + sent.toString() + "\n\nYour Letter number\n" + received.toString());
                                } else {
                                    mRef.setText("Ref:\nOur Letter number\n" + sent.toString() + "\n\nYour Letter number\n" + received.toString());
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mRef.setText("Ref:\nOur Letter number\n" + sent.toString() + "\n\nYour Letter number\n" + received.toString());
                            }
                        });

                    }
                }
            });
        } else {
            mSubject.setText("Subject: " + getIntent().getStringExtra(SUBJECT));
            mTo.setText("To,\n   " + getIntent().getStringExtra(DEPARTMENT));
            mRef.setText("");
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
        onBackPressed();
        return super.onSupportNavigateUp();
    }


    public void savePDF(View view) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            createPdf();

        } else {
            Toast.makeText(this, "Not created", Toast.LENGTH_SHORT).show();
        }
    }


    public void createPdf() {

        final String FONT = "res/fonts/freesans.ttf";
        Document doc = new Document();

        try {
            String HINDI_FONT = "freesans.ttf";
            try {
                File file = new File(getFilesDir(), HINDI_FONT);
                if (file.length() == 0) {
                    InputStream fs = getAssets().open(HINDI_FONT);
                    FileOutputStream os = new FileOutputStream(file);
                    int i;
                    while ((i = fs.read()) != -1) {
                        os.write(i);
                    }
                    os.flush();
                    os.close();
                    fs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PDF";

            File dir = new File(path);
            if (!dir.exists())
                dir.mkdirs();

            File file = new File(dir, LetterNumber + ".pdf");
            FileOutputStream fOut = new FileOutputStream(file);

            PdfWriter.getInstance(doc, fOut);
            doc.open();

            //Font fontSize_16 = FontFactory.getFont(Font.FontFamily.TIMES_ROMAN, 16.0f);
            Font fontSize_18 = FontFactory.getFont(FONT, 18.0f);
            Font fontSize_16 = new Font(Font.FontFamily.TIMES_ROMAN, 16.0f, Font.NORMAL);


            Font fontStyle_Bold = FontFactory.getFont(FontFactory.HELVETICA, 24.0f, Font.BOLD);


            Paragraph letterNo = new Paragraph("Letter number: " + LetterNumber, fontSize_16);
            letterNo.setAlignment(Paragraph.ALIGN_LEFT);
            doc.add(letterNo);


            Paragraph date = new Paragraph("Date: " + Date, fontSize_16);
            date.setAlignment(Paragraph.ALIGN_RIGHT);
            doc.add(date);

            Paragraph subject = new Paragraph(mSubject.getText().toString(), fontStyle_Bold);
            subject.setAlignment(Paragraph.ALIGN_LEFT);
            doc.add(subject);

            Paragraph to_data = new Paragraph(getIntent().getStringExtra(DEPARTMENT), fontSize_18);
            to_data.setAlignment(Paragraph.ALIGN_LEFT);
            doc.add(to_data);
            doc.add(new Paragraph("   \n", fontSize_16));

            Paragraph ref = new Paragraph("References-", fontSize_18);
            ref.setAlignment(Paragraph.ALIGN_LEFT);
            doc.add(ref);


            if (sent != null) {
                Paragraph oLetters = new Paragraph("Our Letters:-" + sent.toString(), fontSize_18);
                oLetters.setAlignment(Paragraph.ALIGN_LEFT);
                doc.add(oLetters);
            }


            if (received != null) {
                Paragraph yLetters = new Paragraph("Your Letters:-" + received.toString(), fontSize_18);
                yLetters.setAlignment(Paragraph.ALIGN_LEFT);
                doc.add(yLetters);
            }

            doc.add(new Paragraph("   \n", fontSize_16));

            Paragraph LetterDetails = new Paragraph(mComposeData.getText().toString(), fontSize_18);
            LetterDetails.setAlignment(Paragraph.ALIGN_LEFT);
            doc.add(LetterDetails);

            doc.add(new Paragraph("   \n", fontSize_16));

            Paragraph sig = new Paragraph(mSignature.getText().toString(), fontSize_18);
            sig.setAlignment(Paragraph.ALIGN_RIGHT);
            doc.add(sig);


            new AlertDialog.Builder(this)
                    .setTitle("Saved")
                    .setMessage("Your document Saved in your phone memory\n'Phone_Storage/PDF/" + LetterNumber + ".png'")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        } catch (DocumentException de) {
            Toast.makeText(this, "error, could't create pdf" + de.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            Log.e("PDFCreator", "DocumentException:" + de);
        } catch (IOException e) {
            Toast.makeText(this, "error, could't create pdf" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            Log.e("PDFCreator", "ioException:" + e);
        } finally {
            doc.close();
        }

    }


}