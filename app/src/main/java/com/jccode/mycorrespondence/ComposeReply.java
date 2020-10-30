package com.jccode.mycorrespondence;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jccode.mycorrespondence.utility.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.jccode.mycorrespondence.utility.Util.ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.COMPOSE;
import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DATE;
import static com.jccode.mycorrespondence.utility.Util.DEPARTMENT;
import static com.jccode.mycorrespondence.utility.Util.DOCUMENT;
import static com.jccode.mycorrespondence.utility.Util.DOCUMENT_NAME;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.IMAGE_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE;
import static com.jccode.mycorrespondence.utility.Util.LETTER_DETAIL;
import static com.jccode.mycorrespondence.utility.Util.LETTER_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.PDF_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.REFERENCE_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.REMARK;
import static com.jccode.mycorrespondence.utility.Util.SENDER_DETAIL;
import static com.jccode.mycorrespondence.utility.Util.SIGNATURE;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TYPE;


public class ComposeReply extends AppCompatActivity {
    private static final String TAG = "ComposeReply";
    FirebaseFirestore firestore;
    public String Description, Subject, Id;
    Util util;
    TextView mSub, mDate, mType, mRefNumber, mPdfFile_tv, mSignature, mPreview;
    String From;
    EditText composeLetter, senderDetail, mRemark, mLetterNumber, mDepartment;
    RecyclerView recyclerView;
    List<Uri> imagesEncodedList = new ArrayList<>();
    List<Uri> documentEncodedList = new ArrayList<>();
    PickedImageAdapter imageAdapter;
    ImageView imageView;
    Uri selectedImage;

    ArrayList<String> selectedImagesList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_reply);

        Toolbar toolbar = (Toolbar) findViewById(R.id.compose_reply_toolbar);

        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        util = new Util();
        findViewById();

        hideKeyBoard();

        if (getIntent().hasExtra(FROM)) {
            From = getIntent().getStringExtra(FROM);
            if (From.equals("composeReply")) {
                setToolbar(toolbar, "Compose Reply");
                showEditText(true);
            } else if (From.equals("composeCorrespondence")) {
                setToolbar(toolbar, "Compose Correspondence");
                mType.setText(getIntent().getStringExtra(TYPE));
                mDate.setText(getIntent().getStringExtra(DATE));
                composeLetter.setText(getIntent().getStringExtra(LETTER_DETAIL));
                mLetterNumber.setText(getIntent().getStringExtra(LETTER_NUMBER));
            } else {
                setToolbar(toolbar, "Add Reply");
                showEditText(false);
            }
            Subject = getIntent().getStringExtra(SUBJECT);
            Id = getIntent().getStringExtra(ID);
            mSub.setText(Subject);
            mRefNumber.setText("Reference Number\n" + Id);
            mDepartment.setText(getIntent().getStringExtra(DEPARTMENT));
            mSignature.setText(util.getSignature(this));
        }
    }

    private void showEditText(boolean b) {
        if (!b) {
            mPreview.setVisibility(View.GONE);
            composeLetter.setVisibility(View.GONE);
            mDepartment.setVisibility(View.GONE);
            senderDetail.setVisibility(View.GONE);
        } else {
            mPreview.setVisibility(View.VISIBLE);
            mRemark.setVisibility(View.VISIBLE);
            composeLetter.setVisibility(View.VISIBLE);
            mDepartment.setVisibility(View.VISIBLE);
            senderDetail.setVisibility(View.VISIBLE);
        }
    }

    private void findViewById() {
        mPdfFile_tv = (TextView) findViewById(R.id.compose_pdf_file);
        recyclerView = (RecyclerView) findViewById(R.id.compose_reply_picked_image_rec);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mSub = (TextView) findViewById(R.id.subject);
        mRefNumber = (TextView) findViewById(R.id.ref_num);
        mSignature = (TextView) findViewById(R.id.signature);
        mPreview = (TextView) findViewById(R.id.preview);
        mDate = (TextView) findViewById(R.id.date__);
        mType = (TextView) findViewById(R.id.type);
        composeLetter = (EditText) findViewById(R.id.c_letter);
        mRemark = (EditText) findViewById(R.id.remarkk);
        senderDetail = (EditText) findViewById(R.id.sender_detail);
        mDepartment = (EditText) findViewById(R.id.letter_department);
        mLetterNumber = (EditText) findViewById(R.id.letter_number);
        imageView = (ImageView) findViewById(R.id.preview_image);
    }

    private void setToolbar(Toolbar toolbar, String id) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(id);
    }

    public void sendCompose(final View view) {

        final ProgressDialog progressDialog = new ProgressDialog(ComposeReply.this);
        progressDialog.setMessage("Sending...");
        String ComLetter = composeLetter.getText().toString();
        String SenderDetail = senderDetail.getText().toString();
        String Department = mDepartment.getText().toString();
        String LetterNumber = mLetterNumber.getText().toString();
        String SentReceived = mType.getText().toString();
        String date = mDate.getText().toString();


        if (From.equalsIgnoreCase("composeReply")) {
            if (!ComLetter.isEmpty() && !SenderDetail.isEmpty() && !Department.isEmpty() && !LetterNumber.isEmpty() && !SentReceived.isEmpty() && !date.isEmpty()) {
                progressDialog.show();
                Map<String, Object> map = new HashMap<>();
                map.put(SUBJECT, Subject);
                map.put(REFERENCE_NUMBER, Id);
                map.put(TYPE, mType.getText().toString());
                map.put(LETTER_DETAIL, ComLetter);
                map.put(DEPARTMENT, Department);
                map.put(LETTER_NUMBER, mLetterNumber.getText().toString());
                map.put(SENDER_DETAIL, SenderDetail);
                map.put(SIGNATURE, util.getSignature(ComposeReply.this));
                map.put(IS_ACTIVE, true);
                map.put(Util.TAG, "");
                map.put(TIMESTAMP, System.currentTimeMillis());
                map.put(REMARK, mRemark.getText().toString());
                map.put(PDF_ATTACHMENT, null);
                map.put(IMAGE_ATTACHMENT, null);
                map.put(DATE, mDate.getText().toString());


                firestore.collection(CORRESPONDENCE_QUERY).document(Id).collection(COMPOSE)
                        .add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(ComposeReply.this, "Submitted", Toast.LENGTH_SHORT).show();
                        try {
                            uploadData(documentReference.getId(), progressDialog);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Snackbar.make(view, "try again", Snackbar.LENGTH_SHORT).show();
                    }
                });

            } else {
                Snackbar.make(view, "Fill All the Fields", Snackbar.LENGTH_SHORT).show();
            }
        } else if (From.equalsIgnoreCase("composeCorrespondence")) {
            if (!LetterNumber.isEmpty() && !SentReceived.isEmpty() && !date.isEmpty() && !LetterNumber.isEmpty()) {
                progressDialog.show();
                Map<String, Object> map = new HashMap<>();
                map.put(SUBJECT, Subject);
                map.put(REFERENCE_NUMBER, Id);
                map.put(TYPE, mType.getText().toString());
                map.put(LETTER_DETAIL, ComLetter);
                map.put(DEPARTMENT, Department);
                map.put(LETTER_NUMBER, mLetterNumber.getText().toString());
                map.put(SENDER_DETAIL, SenderDetail);
                map.put(Util.TAG, "");
                map.put(SIGNATURE, util.getSignature(ComposeReply.this));
                map.put(IS_ACTIVE, true);
                map.put(TIMESTAMP, System.currentTimeMillis());
                map.put(REMARK, mRemark.getText().toString());
                map.put(PDF_ATTACHMENT, null);
                map.put(IMAGE_ATTACHMENT, null);
                map.put(DATE, mDate.getText().toString());


                firestore.collection(CORRESPONDENCE_QUERY).document(Id)/*.collection(COMPOSE)*/
                        .set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ComposeReply.this, "Sent", Toast.LENGTH_SHORT).show();
                        try {
                            uploadData(Id, progressDialog);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ComposeReply.this, "cant create, try again", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Snackbar.make(view, "Fill All the Fields", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            if (!LetterNumber.isEmpty() && !SentReceived.isEmpty() && !date.isEmpty()) {
                Toast.makeText(this, "Submitting Document", Toast.LENGTH_SHORT).show();
                progressDialog.show();
                Map<String, Object> map = new HashMap<>();
                map.put(SUBJECT, Subject);
                map.put(REFERENCE_NUMBER, Id);
                map.put(TYPE, mType.getText().toString());
                map.put(LETTER_DETAIL, ComLetter);
                map.put(DEPARTMENT, Department);
                map.put(LETTER_NUMBER, mLetterNumber.getText().toString());
                map.put(SENDER_DETAIL, SenderDetail);
                map.put(SIGNATURE, util.getSignature(ComposeReply.this));
                map.put(IS_ACTIVE, true);
                map.put(TIMESTAMP, System.currentTimeMillis());
                map.put(REMARK, mRemark.getText().toString());
                map.put(PDF_ATTACHMENT, null);
                map.put(Util.TAG, "");
                map.put(IMAGE_ATTACHMENT, null);
                map.put(DATE, mDate.getText().toString());


                firestore.collection(CORRESPONDENCE_QUERY).document(Id).collection(COMPOSE)
                        .add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(ComposeReply.this, "Document Submitted", Toast.LENGTH_SHORT).show();
                        try {
                            uploadData(documentReference.getId(), progressDialog);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Snackbar.make(view, "try again", Snackbar.LENGTH_SHORT).show();
                    }
                });

            } else {
                Snackbar.make(view, "Fill All the Fields", Snackbar.LENGTH_SHORT).show();
            }
        }
    }


    private void hideKeyBoard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void pickDate(View view2) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.date_picker, null, false);

        // the time picker on the alert dialog, this is how to get the value
        final DatePicker myDatePicker = (DatePicker) view.findViewById(R.id.myDatePicker);

        // so that the calendar view won't appear
        myDatePicker.setCalendarViewShown(false);

        // the alert dialog
        new AlertDialog.Builder(ComposeReply.this).setView(view)
                .setTitle("Set Date")
                .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                    @TargetApi(11)
                    public void onClick(DialogInterface dialog, int id) {
                        int month = myDatePicker.getMonth() + 1;
                        int day = myDatePicker.getDayOfMonth();
                        int year = myDatePicker.getYear();

                        String d = day + "-" + month + "-" + year;
                        Toast.makeText(ComposeReply.this, d, Toast.LENGTH_SHORT).show();
                        mDate.setText(d);
                        dialog.cancel();

                    }

                }).show();
    }

    public void showTypeDialog(View view) {
        final CharSequence[] items = {"Received", "Sent"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ComposeReply.this);
        builder.setTitle("Make your selection");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                mType.setText(items[item]);
                dialog.dismiss();

            }
        }).show();
    }


    public void addSignature(final View view2) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ComposeReply.this);

        // get the layout inflater
        LayoutInflater inflater = ComposeReply.this.getLayoutInflater();

        View view = inflater.inflate(R.layout.update_signature_layout, null);
        final EditText signature = (EditText) view.findViewById(R.id.signature_editText);
        String sign = util.getSignature(ComposeReply.this);
        if (sign != null) {
            signature.setText(sign);
        }
        builder.setTitle("Update Signature");
        builder.setView(view)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String Signature = signature.getText().toString();
                        if (Signature.isEmpty()) {
                            Toast.makeText(ComposeReply.this, "Fill all the fields", Toast.LENGTH_SHORT).show();
                        } else {
                            util.addSignature(Signature, ComposeReply.this);
                            mSignature.setText(Signature);
                            Snackbar.make(view2, "Signature Updated", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(ComposeReply.this)
                .setMessage("Your correspondence is in editing mode, want to send before exit??")
                .setTitle("Send Correspondence")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    public void addImage(View view) {
        selectImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        switch (requestCode) {
            case 1001:
                if (data != null) {
                    StringBuilder builder = new StringBuilder();
                    // Checking for selection multiple files or single.
                    if (data.getClipData() != null) {

                        for (int index = 0; index < data.getClipData().getItemCount(); index++) {

                            // Getting the URIs of the selected files and logging them into logcat at debug level
                            Uri uri = data.getClipData().getItemAt(index).getUri();
                            String type = getMimeType(uri);
                            Toast.makeText(this, "mime type = " + type, Toast.LENGTH_SHORT).show();
                        }

                        // Getting the length of data and logging up the logs using index
                        for (int index = 0; index < data.getClipData().getItemCount(); index++) {

                            // Getting the URIs of the selected files and logging them into logcat at debug level
                            Uri uri = data.getClipData().getItemAt(index).getUri();
                            Log.d("filesUri [" + uri + "] : ", String.valueOf(uri));
                            documentEncodedList.add(uri);
                            builder.append("attachment " + index + " \n");
                        }
                        mPdfFile_tv.setText(builder.toString());
                        mPdfFile_tv.setVisibility(View.VISIBLE);
                    } else {

                        // Getting the URI of the selected file and logging into logcat at debug level
                        Uri uri = data.getData();
                        String type = getMimeType(uri);
                        Toast.makeText(this, "mime type = " + type, Toast.LENGTH_SHORT).show();
                        Log.d("fileUri: ", String.valueOf(uri));
                        documentEncodedList.add(uri);
                        builder.append("attachment 01 \n");
                        mPdfFile_tv.setText(builder.toString());
                        mPdfFile_tv.setVisibility(View.VISIBLE);
                    }
                }
                break;
        }


        //Image Request
        if (resultCode == Activity.RESULT_OK && requestCode == 10001) {
            if (data != null) {
                selectedImagesList.addAll(data.getStringArrayListExtra(Pix.IMAGE_RESULTS));
                imageAdapter = new PickedImageAdapter(selectedImagesList);
                recyclerView.setAdapter(imageAdapter);
                imageAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
                Toast.makeText(this, "selected images " + selectedImagesList.size(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "no selected images " + selectedImagesList.size(), Toast.LENGTH_SHORT).show();

            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    private class PickedImageAdapter extends RecyclerView.Adapter<PickedImageAdapter.MyViewHolder> {
        ArrayList<String> imagesEncodedList;

        public PickedImageAdapter(ArrayList<String> imagesEncodedList) {
            this.imagesEncodedList = imagesEncodedList;
        }

        @NonNull
        @Override
        public PickedImageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picked_image_view, parent, false);
            return new PickedImageAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PickedImageAdapter.MyViewHolder holder, final int position) {

            try {
                Bitmap bitmap = BitmapFactory.decodeFile(imagesEncodedList.get(position));
                holder.imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ComposeReply.this, "failed to set image " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }

            holder.remove_image_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = position;
                    imagesEncodedList.remove(pos);
                    imageAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return imagesEncodedList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView, remove_image_btn;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.picked_imageView);
                remove_image_btn = (ImageView) itemView.findViewById(R.id.picked_imageView_close);
            }
        }
    }


    private void uploadData(final String id, final ProgressDialog progressDialog) throws FileNotFoundException {
        if (selectedImagesList.isEmpty() && documentEncodedList.isEmpty() && selectedImage == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "successfully created", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Uploading Documents", Toast.LENGTH_SHORT).show();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageRef = storage.getReference();

            if (!selectedImagesList.isEmpty()) {
                final List<Map> imageMaps = new ArrayList<>();
                for (int i = 0; i < selectedImagesList.size(); i++) {
                    final String STORAGE_PATH = "correspondence_images/" + Id + "/" + System.currentTimeMillis() + ".jpg";
                    StorageReference spaceRef = storageRef.child(STORAGE_PATH);

                    InputStream stream = new FileInputStream(new File(selectedImagesList.get(i)));

                    UploadTask uploadTask = spaceRef.putStream(stream);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(ComposeReply.this, "failed to upload image", Toast.LENGTH_SHORT).show();
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
                                    attachmentMap.put(DOCUMENT_NAME, imageMaps.size() + 1);
                                    attachmentMap.put(TIMESTAMP, System.currentTimeMillis());
                                    imageMaps.add(attachmentMap);
                                    firestore.collection(CORRESPONDENCE_QUERY).document(Id).collection(COMPOSE)
                                            .document(id)
                                            .update(IMAGE_ATTACHMENT, imageMaps).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(ComposeReply.this, "Document not uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Toast.makeText(ComposeReply.this, "Document uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                }
                            });

                        }
                    });
                }
            }

            if (!documentEncodedList.isEmpty()) {
                final List<Map> pdfMaps = new ArrayList<>();
                for (int i = 0; i < documentEncodedList.size(); i++) {
                    final String STORAGE_PATH = "correspondence_documents/" + Id + "/" + System.currentTimeMillis() + ".pdf";
                    StorageReference spaceRef = storageRef.child(STORAGE_PATH);
                    UploadTask uploadTask = spaceRef.putFile(documentEncodedList.get(i));
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(ComposeReply.this, "failed to upload file", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageRef.child(STORAGE_PATH).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Map<String, Object> attachmentMap = new HashMap<>();
                                    attachmentMap.put(DOCUMENT, uri.toString());
                                    attachmentMap.put(IS_ACTIVE, true);
                                    attachmentMap.put(DOCUMENT_NAME, pdfMaps.size() + 1);
                                    attachmentMap.put(TIMESTAMP, System.currentTimeMillis());
                                    pdfMaps.add(attachmentMap);
                                    firestore.collection(CORRESPONDENCE_QUERY).document(Id).collection(COMPOSE)
                                            .document(id)
                                            .update(PDF_ATTACHMENT, pdfMaps).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(ComposeReply.this, "Document not uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Toast.makeText(ComposeReply.this, "Document uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                }
                            });

                        }
                    });
                }
            }

            if (selectedImage != null) {
                final List<Map> imageMaps = new ArrayList<>();
                final String STORAGE_PATH = "correspondence_images/" + Id + "/" + System.currentTimeMillis() + ".jpg";
                StorageReference spaceRef = storageRef.child(STORAGE_PATH);

                UploadTask uploadTask = spaceRef.putFile(selectedImage);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(ComposeReply.this, "failed to upload image", Toast.LENGTH_SHORT).show();
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
                                attachmentMap.put(DOCUMENT_NAME, imageMaps.size() + 1);
                                attachmentMap.put(TIMESTAMP, System.currentTimeMillis());
                                imageMaps.add(attachmentMap);
                                firestore.collection(CORRESPONDENCE_QUERY).document(Id).collection(COMPOSE)
                                        .document(id)
                                        .update(IMAGE_ATTACHMENT, imageMaps).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Toast.makeText(ComposeReply.this, "Document not uploaded", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(ComposeReply.this, "Document uploaded", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });

                    }
                });
            }

            progressDialog.dismiss();
            onBackPressed();
            finish();
        }
    }

    private void selectImage() {
        final CharSequence[] options = {"Images", "Pdf", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ComposeReply.this);
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
                        intent.setType("*/*");
                        startActivityForResult(intent, 1001);
                    }
                    break;
                    default:
                        dialog.dismiss();

                }
            }
        });
        builder.show();
    }

    public void image() {

        Options options = Options.init()
                .setRequestCode(10001)                                           //Request code for activity results
                .setCount(10)                                                   //Number of images to restict selection count
                .setFrontfacing(false)                                         //Front Facing camera on start
                //.setPreSelectedUrls(null)                               //Pre selected Image Urls
                .setExcludeVideos(true)                                       //Option to exclude videos
                //.setVideoDurationLimitinSeconds(30)                            //Duration for video recording
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                .setPath("/cma/images");                                       //Custom Path For media Storage

        Pix.start(ComposeReply.this, options);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(ComposeReply.this, Options.init().setRequestCode(100));
                } else {
                    Toast.makeText(ComposeReply.this, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    public void showPreview(View view) {
        /*
        String letterNumber = mLetterNumber.getText().toString();
        String Date = mDate.getText().toString();
        String letterDetail = composeLetter.getText().toString();
        if (!letterDetail.isEmpty() && !Date.isEmpty() && !letterNumber.isEmpty()) {
            startActivity(new Intent(ComposeReply.this, PreviewScreen.class)
                    .putExtra(ID, Id)
                    .putExtra(FROM, From)
                    .putExtra(SUBJECT, Subject)
                    .putExtra(TAG, "NO-TAG")
                    .putExtra(COMPOSE, composeLetter.getText().toString())
                    .putExtra(SENDER_DETAIL, senderDetail.getText().toString())
                    .putExtra(DEPARTMENT, mDepartment.getText().toString())
                    .putExtra(LETTER_DETAIL, letterDetail)
                    .putExtra(LETTER_NUMBER, letterNumber)
                    .putExtra(DATE, Date));

        } else
            Snackbar.make(view, "FILL ALL THE FIELDS", Snackbar.LENGTH_SHORT).show();*/

        Snackbar.make(view, "coming soon", Snackbar.LENGTH_SHORT).show();
    }


}
