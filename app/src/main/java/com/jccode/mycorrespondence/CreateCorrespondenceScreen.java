package com.jccode.mycorrespondence;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.widget.Button;
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
import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DATE;
import static com.jccode.mycorrespondence.utility.Util.DEPARTMENT;
import static com.jccode.mycorrespondence.utility.Util.DESCRIPTION;
import static com.jccode.mycorrespondence.utility.Util.DOCUMENT;
import static com.jccode.mycorrespondence.utility.Util.DRAFTS;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.F_NAME;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.IMAGE_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE_TO_SHARER;
import static com.jccode.mycorrespondence.utility.Util.LETTER_DETAIL;
import static com.jccode.mycorrespondence.utility.Util.LETTER_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.L_NAME;
import static com.jccode.mycorrespondence.utility.Util.MyPREFERENCES;
import static com.jccode.mycorrespondence.utility.Util.PDF_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.REMARK;
import static com.jccode.mycorrespondence.utility.Util.SENDER_DETAIL;
import static com.jccode.mycorrespondence.utility.Util.SHARE_TO;
import static com.jccode.mycorrespondence.utility.Util.SHARE_TO_USER_IMAGE;
import static com.jccode.mycorrespondence.utility.Util.SHARE_TO_USER_NAME;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TYPE;
import static com.jccode.mycorrespondence.utility.Util.UID;
import static com.jccode.mycorrespondence.utility.Util.USERNAME;
import static com.jccode.mycorrespondence.utility.Util.USERS;

public class CreateCorrespondenceScreen extends AppCompatActivity {
    private static final int PICK_IMAGE_GALLERY = 102;
    private static final String TAG = "ComposeReply";
    public String From = "DraftsScreen";
    public String Title, Description, Subject, Id;
    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    List<Uri> imagesEncodedList = new ArrayList<>();
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

    ImageView imageView;
    Uri selectedImage;


    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;
    ArrayList<String> selectedImagesList = new ArrayList<>();


    TextView mComposeBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.compose_toolbar);
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
            setToolbar(toolbar, "Create Correspondence");
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
        mComposeBtn = (TextView) findViewById(R.id.compose_btn);
        recyclerView = (RecyclerView) findViewById(R.id.picked_image_rec);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
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

    public void showDateDialog(View view2) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.date_picker, null, false);

        // the time picker on the alert dialog, this is how to get the value
        final DatePicker myDatePicker = (DatePicker) view.findViewById(R.id.myDatePicker);

        // so that the calendar view won't appear
        myDatePicker.setCalendarViewShown(false);

        // the alert dialog
        new AlertDialog.Builder(CreateCorrespondenceScreen.this).setView(view)
                .setTitle("Set Date")
                .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                    @TargetApi(11)
                    public void onClick(DialogInterface dialog, int id) {
                        int month = myDatePicker.getMonth() + 1;
                        int day = myDatePicker.getDayOfMonth();
                        int year = myDatePicker.getYear();

                        String d = day + "-" + month + "-" + year;
                        Toast.makeText(CreateCorrespondenceScreen.this, d, Toast.LENGTH_SHORT).show();
                        date.setText(d);
                        dialog.cancel();

                    }

                }).show();
    }


    public void composeCorrespondence(View view) {
        String LetterNumber = letterNumber.getText().toString();
        String Department = department.getText().toString();
        String Sub = subject.getText().toString();
        String Des = desc.getText().toString();
        String Date = date.getText().toString();
        String Type = type.getText().toString();
        String Remark = remark.getText().toString();
        String SenderDetail = mSenderDetail.getText().toString();

        if (!LetterNumber.isEmpty() && !Sub.isEmpty() && !Des.isEmpty() && !Date.isEmpty() && !Type.isEmpty() && !Department.isEmpty()) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("sending...");
            progressDialog.show();

            SharedPreferences pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            String userName = pref.getString(F_NAME, "") + " " + pref.getString(L_NAME, "");
            Map<String, Object> map = new HashMap<>();
            map.put(LETTER_NUMBER, LetterNumber);
            map.put(DEPARTMENT, Department);
            map.put(TYPE, Type);
            map.put(REMARK, Remark);
            map.put(SUBJECT, Sub);
            map.put(SHARE_TO_USER_IMAGE, "");
            map.put(SHARE_TO_USER_NAME, "");
            map.put(IS_ACTIVE_TO_SHARER, true);
            map.put(SHARE_TO, "");
            map.put(DESCRIPTION, Des);
            map.put(Util.TAG, "");
            map.put(SENDER_DETAIL, SenderDetail);
            map.put(IS_ACTIVE, true);
            map.put(DATE, Date);
            map.put(USERNAME, userName);
            map.put(TIMESTAMP, System.currentTimeMillis());
            map.put(UID, FirebaseAuth.getInstance().getCurrentUser().getUid());


            firestore.collection(CORRESPONDENCE_QUERY)
                    .add(map)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            String id = documentReference.getId();
                            try {
                                uploadData(id, progressDialog);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                Toast.makeText(CreateCorrespondenceScreen.this, "file not found" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CreateCorrespondenceScreen.this, "could'nt send, try again", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            showSnackBar(view);
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
            onBackPressed();
            finish();
        } else {
            Toast.makeText(this, "Uploading Documents", Toast.LENGTH_SHORT).show();

            FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageRef = storage.getReference();

            if (!selectedImagesList.isEmpty()) {
                for (int i = 0; i < selectedImagesList.size(); i++) {
                    final String STORAGE_PATH = "correspondence_images/" + Id + "/" + System.currentTimeMillis() + ".jpg";
                    StorageReference spaceRef = storageRef.child(STORAGE_PATH);

                    InputStream stream = new FileInputStream(new File(selectedImagesList.get(i)));
                    UploadTask uploadTask = spaceRef.putStream(stream);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(CreateCorrespondenceScreen.this, "failed to upload image", Toast.LENGTH_SHORT).show();
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

                                    firestore.collection(CORRESPONDENCE_QUERY).document(id).collection(IMAGE_ATTACHMENT)
                                            .add(attachmentMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(CreateCorrespondenceScreen.this, "Document uploaded", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CreateCorrespondenceScreen.this, "could't upload image", Toast.LENGTH_SHORT).show();
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
                for (int i = 0; i < documentEncodedList.size(); i++) {
                    final String STORAGE_PATH = "correspondence_documents/" + Id + "/" + System.currentTimeMillis() + ".pdf";
                    StorageReference spaceRef = storageRef.child(STORAGE_PATH);
                    UploadTask uploadTask = spaceRef.putFile(documentEncodedList.get(i));
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(CreateCorrespondenceScreen.this, "failed to upload file", Toast.LENGTH_SHORT).show();
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
                                    attachmentMap.put(TIMESTAMP, System.currentTimeMillis());

                                    firestore.collection(CORRESPONDENCE_QUERY).document(id).collection(PDF_ATTACHMENT)
                                            .add(attachmentMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Toast.makeText(CreateCorrespondenceScreen.this, "sent", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CreateCorrespondenceScreen.this, "could't sent file", Toast.LENGTH_SHORT).show();
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
                final String STORAGE_PATH = "correspondence_images/" + Id + "/" + System.currentTimeMillis() + ".jpg";
                StorageReference spaceRef = storageRef.child(STORAGE_PATH);

                UploadTask uploadTask = spaceRef.putFile(selectedImage);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(CreateCorrespondenceScreen.this, "failed to upload image", Toast.LENGTH_SHORT).show();
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

                                firestore.collection(CORRESPONDENCE_QUERY).document(id).collection(IMAGE_ATTACHMENT)
                                        .add(attachmentMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Toast.makeText(CreateCorrespondenceScreen.this, "sent", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(CreateCorrespondenceScreen.this, "could't sent image", Toast.LENGTH_SHORT).show();
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
        //Pdf request Code
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
                            builder.append("attachment " + index + " \n");
                        }
                        pdfFile_tv.setText(builder.toString());
                    } else {

                        // Getting the URI of the selected file and logging into logcat at debug level
                        Uri uri = data.getData();
                        Log.d("fileUri: ", String.valueOf(uri));
                        documentEncodedList.add(uri);
                        builder.append("filesUri [" + uri + "] : \n");
                        pdfFile_tv.setText(builder.toString());
                    }
                }
                break;
        }

        //Image Request
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
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


    private class PickedImageAdapter extends RecyclerView.Adapter<PickedImageAdapter.MyViewHolder> {
        ArrayList<String> imagesEncodedList;

        public PickedImageAdapter(ArrayList<String> imagesEncodedList) {
            this.imagesEncodedList = imagesEncodedList;
        }

        @NonNull
        @Override
        public PickedImageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picked_image_view, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PickedImageAdapter.MyViewHolder holder, final int position) {

            try {
                Bitmap bitmap = BitmapFactory.decodeFile(imagesEncodedList.get(position));
                holder.imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(CreateCorrespondenceScreen.this, "failed to set image " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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


    public void saveDrafts(final View view) {

    }


    private void hideKeyBoard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void gotoComposeCorrespondence(View view) {

        Snackbar.make(view, "Coming Soon", Snackbar.LENGTH_SHORT).show();

    }

    public void showDateTypeDialog(View view) {
        final CharSequence[] items = {"Received", "Sent"};
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateCorrespondenceScreen.this);
        builder.setTitle("Make your selection");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    mComposeBtn.setVisibility(View.GONE);
                } else
                    mComposeBtn.setVisibility(View.VISIBLE);
                type.setText(items[item]);
                dialog.dismiss();

            }
        }).show();
    }


    private void selectImage() {
        final CharSequence[] options = {"Images", "Pdf", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(CreateCorrespondenceScreen.this);
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

    public void image() {

        Options options = Options.init()
                .setRequestCode(1000)                                           //Request code for activity results
                .setCount(10)                                                   //Number of images to restict selection count
                .setFrontfacing(false)                                         //Front Facing camera on start
                //.setPreSelectedUrls(null)                               //Pre selected Image Urls
                .setExcludeVideos(true)                                       //Option to exclude videos
                //.setVideoDurationLimitinSeconds(30)                            //Duration for video recording
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                .setPath("/cma/images");                                       //Custom Path For media Storage

        Pix.start(CreateCorrespondenceScreen.this, options);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(CreateCorrespondenceScreen.this, Options.init().setRequestCode(100));
                } else {
                    Toast.makeText(CreateCorrespondenceScreen.this, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

}
