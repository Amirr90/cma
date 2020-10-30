package com.jccode.mycorrespondence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jccode.mycorrespondence.utility.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jccode.mycorrespondence.utility.Util.ADD_DOCUMENT;
import static com.jccode.mycorrespondence.utility.Util.ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DATE;
import static com.jccode.mycorrespondence.utility.Util.DEPARTMENT;
import static com.jccode.mycorrespondence.utility.Util.DESCRIPTION;
import static com.jccode.mycorrespondence.utility.Util.DOCUMENT;
import static com.jccode.mycorrespondence.utility.Util.DOCUMENT_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DOC_ID;
import static com.jccode.mycorrespondence.utility.Util.DRAFTS;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.F_NAME;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.IMAGE_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE;
import static com.jccode.mycorrespondence.utility.Util.LETTER_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.L_NAME;
import static com.jccode.mycorrespondence.utility.Util.MyPREFERENCES;
import static com.jccode.mycorrespondence.utility.Util.PDF_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.REMARK;
import static com.jccode.mycorrespondence.utility.Util.SENDER_DETAIL;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.SUB_DOCUMENT_QUERY;
import static com.jccode.mycorrespondence.utility.Util.SUB_DOC_ID;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TYPE;
import static com.jccode.mycorrespondence.utility.Util.UID;
import static com.jccode.mycorrespondence.utility.Util.USERNAME;
import static com.jccode.mycorrespondence.utility.Util.USERS;
import static com.jccode.mycorrespondence.utility.Util.getNameFromURI;

public class AddDocumentActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_CAMERA = 101;
    private static final int PICK_IMAGE_GALLERY = 102;
    private static final String TAG = "ComposeReply";
    public String From = "DraftsScreen";
    public String Title, Description, Subject, Id;
    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded, doc_ID;
    List<Uri> imagesEncodedList = new ArrayList<>();
    ArrayList<String> selectedImagesList = new ArrayList<>();
    List<Uri> documentEncodedList = new ArrayList<>();
    PickedImageAdapter imageAdapter;
    RecyclerView recyclerView;
    TextView pdfFile_tv;
    EditText letterNumber, subject, desc, department, remark, mSenderDetail;
    TextView date;

    TextView type;
    FirebaseFirestore firestore;
    int i, j;
    private String LetterNumber, Timestamp, Type, SenderDetail, Remark, Date;
    private String Department;

    Bitmap bitmap;
    File destination;
    String imgPath;
    ImageView imageView;
    Uri selectedImage;


    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    String currentPhotoPath;
    String from;


    TextView mSubmitBtn;

    ArrayList<String> returnValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_document);
        Toolbar toolbar = (Toolbar) findViewById(R.id.add_doc_toolbar);
        setToolbar(toolbar, ADD_DOCUMENT);

        returnValue = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        findViewById();
        if (getIntent().getStringExtra(FROM).equals(From)) {
            Id = getIntent().getStringExtra(ID);
            LetterNumber = getIntent().getStringExtra(LETTER_NUMBER);
            Description = getIntent().getStringExtra(DESCRIPTION);
            Subject = getIntent().getStringExtra(SUBJECT);
            Department = getIntent().getStringExtra(DEPARTMENT);
            Timestamp = getIntent().getStringExtra(TIMESTAMP);
            Type = getIntent().getStringExtra(TYPE);
            Date = getIntent().getStringExtra(DATE);
            SenderDetail = getIntent().getStringExtra(SENDER_DETAIL);
            Remark = getIntent().getStringExtra(REMARK);
            setToolbar(toolbar, "Compose Correspondence");
            setIntentData();

        } else {
            from = getIntent().getStringExtra(FROM);

            if (from.equalsIgnoreCase("Document")) {
                desc.setVisibility(View.GONE);
                subject.setHint("Add Remark");
            } else {
                desc.setVisibility(View.VISIBLE);
                subject.setHint("Subject");
            }

            setToolbar(toolbar, "Add Document");
        }


        hideKeyBoard();
    }

    private void setIntentData() {

        letterNumber.setText(LetterNumber);
        subject.setText(Subject);
        desc.setText(Description);

        department.setText(Department);
        type.setText(Type);
        remark.setText(Remark);
        mSenderDetail.setText(SenderDetail);

        date.setText(Date);

    }

    private void findViewById() {
        imageView = (ImageView) findViewById(R.id.preview_image2);
        letterNumber = (EditText) findViewById(R.id.editText_number);
        subject = (EditText) findViewById(R.id.editText9);
        desc = (EditText) findViewById(R.id.editText10);
        department = (EditText) findViewById(R.id.editText_department);
        date = (TextView) findViewById(R.id.date_);
        remark = (EditText) findViewById(R.id.remark);
        mSenderDetail = (EditText) findViewById(R.id.sender_detail);
        type = (TextView) findViewById(R.id.sent_received);
        pdfFile_tv = (TextView) findViewById(R.id.pdf_file);
        mSubmitBtn = (TextView) findViewById(R.id.submit_btn);
        recyclerView = (RecyclerView) findViewById(R.id.picked_image_rec);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadDocument(view);
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
        finish();
        return true;
    }


    public void uploadDocument(View view) {
        String sub, shortDesc;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("uploading...");


        SharedPreferences pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String userName = pref.getString(F_NAME, "") + " " + pref.getString(L_NAME, "");
        Map<String, Object> map = new HashMap<>();
        if (from.equalsIgnoreCase("Document")) {
            sub = subject.getText().toString();
            shortDesc = "No description";

            if (sub.isEmpty()) {
                showSnackBar(view);
                return;
            }
            map.put(SUBJECT, sub);
            map.put(DESCRIPTION, shortDesc);
            map.put(IS_ACTIVE, true);
            map.put(DATE, Date);
            map.put("shareCount", 0);
            map.put(USERNAME, userName);
            map.put(TIMESTAMP, System.currentTimeMillis());
            map.put(UID, FirebaseAuth.getInstance().getCurrentUser().getUid());

            progressDialog.show();
            doc_ID = getIntent().getStringExtra(DOC_ID);
            firestore.collection(DOCUMENT_QUERY)
                    .document(doc_ID)
                    .collection(SUB_DOCUMENT_QUERY)
                    .add(map)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            String id = documentReference.getId();
                            try {
                                uploadData(id, progressDialog);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddDocumentActivity.this, "could'nt send, try again", Toast.LENGTH_SHORT).show();
                }
            });

        } else {

            sub = subject.getText().toString();
            shortDesc = desc.getText().toString();

            if (sub.isEmpty() && shortDesc.isEmpty()) {
                showSnackBar(view);
                return;
            }
            progressDialog.show();
            map.put(SUBJECT, sub);
            map.put(DESCRIPTION, shortDesc);
            map.put(IS_ACTIVE, true);
            map.put(DATE, Date);
            map.put("shareCount", 0);
            map.put(USERNAME, userName);
            map.put(TIMESTAMP, System.currentTimeMillis());
            map.put(UID, FirebaseAuth.getInstance().getCurrentUser().getUid());

            firestore.collection(DOCUMENT_QUERY)
                    .add(map)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            String id = documentReference.getId();
                            try {
                                uploadData(id, progressDialog);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                Toast.makeText(AddDocumentActivity.this, "file not found" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddDocumentActivity.this, "could'nt send, try again", Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

    private void showSnackBar(View view) {
        final Snackbar snackbar = Snackbar.make(view, "PLEASE FILL ALL THE FIELDS", Snackbar.LENGTH_SHORT);
        snackbar.show();
        snackbar.setActionTextColor(Color.WHITE);
        snackbar.setAction("DISMISS", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
    }

    private void uploadData(final String id, ProgressDialog progressDialog) throws FileNotFoundException {
        if (selectedImagesList.isEmpty() && documentEncodedList.isEmpty() && selectedImage == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "successfully created", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "uploading Document", Toast.LENGTH_LONG).show();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageRef = storage.getReference();

            if (!selectedImagesList.isEmpty()) {
                int i;
                for (i = 0; i < selectedImagesList.size(); i++) {
                    final int counter = i + 1;
                    final String STORAGE_PATH = "document_images/" + Id + "/" + System.currentTimeMillis() + ".jpg";
                    StorageReference spaceRef = storageRef.child(STORAGE_PATH);

                    InputStream stream = new FileInputStream(new File(selectedImagesList.get(i)));
                    UploadTask uploadTask = spaceRef.putStream(stream);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddDocumentActivity.this, "failed to upload image" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
                                    attachmentMap.put(TIMESTAMP, System.currentTimeMillis());

                                    if (from.equalsIgnoreCase("Document")) {
                                        attachmentMap.put(SUB_DOC_ID, id);
                                        attachmentMap.put(DOC_ID, doc_ID);
                                        firestore.collection(DOCUMENT_QUERY)
                                                .document(doc_ID)
                                                .collection(IMAGE_ATTACHMENT)
                                                .add(attachmentMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(AddDocumentActivity.this, " Document uploaded", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(AddDocumentActivity.this, "could't upload Document", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    } else {
                                        attachmentMap.put(SUB_DOC_ID, null);
                                        attachmentMap.put(DOC_ID, doc_ID);
                                        firestore.collection(DOCUMENT_QUERY)
                                                .document(id)
                                                .collection(IMAGE_ATTACHMENT)
                                                .add(attachmentMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(AddDocumentActivity.this, counter + " Document uploaded", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(AddDocumentActivity.this, "could't upload document", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

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
                for (int i = 0; i < documentEncodedList.size(); i++) {
                    final int counter = i + 1;
                    final String STORAGE_PATH = "document_documents/" + Id + "/" + System.currentTimeMillis() + ".pdf";
                    StorageReference spaceRef = storageRef.child(STORAGE_PATH);
                    UploadTask uploadTask = spaceRef.putFile(documentEncodedList.get(i));
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(AddDocumentActivity.this, "failed to upload file", Toast.LENGTH_SHORT).show();
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
                                    attachmentMap.put(TIMESTAMP, System.currentTimeMillis());

                                    if (from.equalsIgnoreCase("Document")) {
                                        attachmentMap.put(SUB_DOC_ID, id);
                                        attachmentMap.put(DOC_ID, doc_ID);
                                        firestore.collection(DOCUMENT_QUERY)
                                                .document(doc_ID)
                                                .collection(PDF_ATTACHMENT)
                                                .add(attachmentMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(AddDocumentActivity.this, counter + " document uploaded", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(AddDocumentActivity.this, "could't upload document", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        attachmentMap.put(SUB_DOC_ID, null);
                                        attachmentMap.put(DOC_ID, id);
                                        firestore.collection(DOCUMENT_QUERY)
                                                .document(id)
                                                .collection(PDF_ATTACHMENT)
                                                .add(attachmentMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                Toast.makeText(AddDocumentActivity.this, counter + " document uploaded", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(AddDocumentActivity.this, "could't sent image", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

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
                final String STORAGE_PATH = "document_images/" + Id + "/" + System.currentTimeMillis() + ".jpg";
                StorageReference spaceRef = storageRef.child(STORAGE_PATH);

                UploadTask uploadTask = spaceRef.putFile(selectedImage);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(AddDocumentActivity.this, "failed to upload image", Toast.LENGTH_SHORT).show();
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
                                attachmentMap.put(TIMESTAMP, System.currentTimeMillis());

                                if (from.equalsIgnoreCase("Document")) {
                                    attachmentMap.put(SUB_DOC_ID, id);
                                    attachmentMap.put(DOC_ID, doc_ID);
                                    firestore.collection(DOCUMENT_QUERY)
                                            .document(doc_ID)
                                            .collection(IMAGE_ATTACHMENT)
                                            .add(attachmentMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(AddDocumentActivity.this, "uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AddDocumentActivity.this, "could't upload document", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    attachmentMap.put(SUB_DOC_ID, null);
                                    attachmentMap.put(DOC_ID, id);
                                    firestore.collection(DOCUMENT_QUERY)
                                            .document(id)
                                            .collection(IMAGE_ATTACHMENT)
                                            .add(attachmentMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(AddDocumentActivity.this, "uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(AddDocumentActivity.this, "could't uploaded document", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

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
            finish();
        }
    }

    public void selectMultipleImages(View view) {

    }


    public void addImage(View view) {
        selectImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                //imagesEncodedList = new ArrayList<Uri>();
                //imageAdapter = new PickedImageAdapter(imagesEncodedList);
                //recyclerView.setAdapter(imageAdapter);
                if (data.getData() != null) {

                    Uri mImageUri = data.getData();

                    // Get the cursor
                    Cursor cursor = getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncoded = cursor.getString(columnIndex);
                    cursor.close();

                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            // Get the cursor
                            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                            // Move to first row
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncoded = cursor.getString(columnIndex);
                            imagesEncodedList.add(uri);
                            imageAdapter.notifyDataSetChanged();
                            cursor.close();

                        }
                        Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
                        Toast.makeText(this, "Selected Images" + mArrayUri.size(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
        }
        switch (requestCode) {
            case 1001:
                // Checking whether data is null or not
                if (data != null) {
                    StringBuilder builder = new StringBuilder();
                    // Checking for selection multiple files or single.
                    if (data.getClipData() != null) {


                        // Getting the length of data and logging up the logs using index
                        for (int index = 0; index < data.getClipData().getItemCount(); index++) {

                            // Getting the URIs of the selected files and logging them into logcat at debug level
                            Uri uri = data.getClipData().getItemAt(index).getUri();
                            Log.d("filesUri [" + uri + "] : ", String.valueOf(uri));
                            documentEncodedList.add(uri);
                            builder.append((index + 1) + " " + getNameFromURI(documentEncodedList.get(index), this) + " \n");
                        }
                        pdfFile_tv.setText(builder.toString());
                    } else {

                        // Getting the URI of the selected file and logging into logcat at debug level
                        Uri uri = data.getData();
                        Log.d("fileUri: ", String.valueOf(uri));
                        documentEncodedList.add(uri);
                        builder.append("1 " + getNameFromURI(uri, this));
                        pdfFile_tv.setText(builder.toString());
                    }
                }
                break;
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {

                File f = new File(currentPhotoPath);
                Uri contentUri = Uri.fromFile(f);

                selectedImage = contentUri;
                Toast.makeText(this, "1 image Selected ", Toast.LENGTH_SHORT).show();

                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);


            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "error reading image from camera " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_IMAGE_GALLERY) {
            selectedImage = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
                Log.e("Activity", "Pick from Gallery::>>> ");

                imgPath = getRealPathFromURI(selectedImage);
                destination = new File(imgPath.toString());
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //new ImageGetter
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            if (data != null) {
                // returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
                selectedImagesList.addAll(data.getStringArrayListExtra(Pix.IMAGE_RESULTS));
                imageAdapter = new PickedImageAdapter(selectedImagesList);
                recyclerView.setAdapter(imageAdapter);
                imageAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
            }

        }


        super.onActivityResult(requestCode, resultCode, data);
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
                Toast.makeText(AddDocumentActivity.this, "failed to set image " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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


    private void hideKeyBoard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void gotoComposeCorrespondence(View view) {
        Snackbar.make(view, "Coming Soon", Snackbar.LENGTH_SHORT).show();

    }

    public void showDateTypeDialog(View view) {
        final CharSequence[] items = {"Received", "Sent"};
        AlertDialog.Builder builder = new AlertDialog.Builder(AddDocumentActivity.this);
        builder.setTitle("Make your selection");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                type.setText(items[item]);
                dialog.dismiss();

            }
        }).show();
    }


    private void selectImage() {

        final CharSequence[] options = {"Images", "Pdf", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(AddDocumentActivity.this);
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


    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void image() {

        Options options = Options.init()
                .setRequestCode(100)                                           //Request code for activity results
                .setCount(10)                                                   //Number of images to restict selection count
                .setFrontfacing(false)                                         //Front Facing camera on start
                //.setPreSelectedUrls(null)                               //Pre selected Image Urls
                .setExcludeVideos(true)                                       //Option to exclude videos
                //.setVideoDurationLimitinSeconds(30)                            //Duration for video recording
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                .setPath("/cma/images");                                       //Custom Path For media Storage

        Pix.start(AddDocumentActivity.this, options);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(AddDocumentActivity.this, Options.init().setRequestCode(100));
                } else {
                    Toast.makeText(AddDocumentActivity.this, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


}