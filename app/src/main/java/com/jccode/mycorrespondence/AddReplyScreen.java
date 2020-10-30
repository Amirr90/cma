package com.jccode.mycorrespondence;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jccode.mycorrespondence.utility.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DESCRIPTION;
import static com.jccode.mycorrespondence.utility.Util.F_NAME;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.IMAGE_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.L_NAME;
import static com.jccode.mycorrespondence.utility.Util.MyPREFERENCES;
import static com.jccode.mycorrespondence.utility.Util.PDF_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.REPLY;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TITLE;
import static com.jccode.mycorrespondence.utility.Util.UID;
import static com.jccode.mycorrespondence.utility.Util.USERNAME;
import static com.jccode.mycorrespondence.utility.Util.USERS;

public class AddReplyScreen extends AppCompatActivity {

    private static final int ACTIVITY_CHOOSE_FILE = 101;
    private static final int PICK_IMAGE_CAMERA = 101;
    private static final int PICK_IMAGE_GALLERY = 102;
    private static final String TAG = "AddReplyScreen";
    private static final int WRITE_STORAGE = 105;
    public String Title, Description, Subject, Id, Timestamp;
    ProgressBar progressBar;
    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    List<Uri> imagesEncodedList = new ArrayList<>();
    List<Uri> documentEncodedList = new ArrayList<>();
    PickedImageAdapter imageAdapter;
    RecyclerView recyclerView;
    TextView mSubject, mTimestamp, mPdfFiles;
    FirebaseFirestore firestore;
    EditText mDesEditText;
    SharedPreferences pref;
    FirebaseUser user;
    Bitmap bitmap;
    File destination;
    String imgPath;
    ImageView imageView;
    Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.addReply_toolbar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar3);
        firestore = FirebaseFirestore.getInstance();


        findViewById();

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (getIntent().hasExtra(ID)) {
            Id = getIntent().getStringExtra(ID);
            Title = getIntent().getStringExtra(TITLE);
            Description = getIntent().getStringExtra(DESCRIPTION);
            Subject = getIntent().getStringExtra(SUBJECT);
            Timestamp = getIntent().getStringExtra(TIMESTAMP);

            setToolbar(toolbar, Subject);

            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String dateString = formatter.format(new Date(Long.parseLong(Timestamp)));
            mTimestamp.setText(dateString);

            mSubject.setText(Subject);
        }

        recyclerView = (RecyclerView) findViewById(R.id.picked_image_rec_reply);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        hideKeyBoard();
    }


    private void findViewById() {
        imageView = (ImageView) findViewById(R.id.preview_image3);
        mSubject = (TextView) findViewById(R.id.textView5);
        mTimestamp = (TextView) findViewById(R.id.time);
        mPdfFiles = (TextView) findViewById(R.id.pdf_files);
        mDesEditText = (EditText) findViewById(R.id.editText3);
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

    public void sendReply(View view) {

        pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (pref.contains(F_NAME)) {
            final String reply_text = mDesEditText.getText().toString();
            if (!reply_text.isEmpty()) {
                firestore.collection(USERS).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        String uName = snapshot.getString(F_NAME) + " " + snapshot.getString(L_NAME);
                        Map<String, Object> map = new HashMap<>();
                        map.put(REPLY, reply_text);
                        map.put(Util.TAG, "NO-TAG");
                        map.put(TIMESTAMP, System.currentTimeMillis());
                        map.put(USERNAME, uName);
                        map.put(IMAGE_ATTACHMENT, null);
                        map.put(PDF_ATTACHMENT, null);
                        map.put(UID, FirebaseAuth.getInstance().getCurrentUser().getUid());
                        progressBar.setVisibility(View.VISIBLE);
                        firestore.collection(CORRESPONDENCE_QUERY).document(Id)
                                .collection(REPLY)
                                .add(map)
                                .addOnSuccessListener(AddReplyScreen.this, new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        String id = documentReference.getId();
                                        progressBar.setVisibility(View.GONE);
                                        uploadData(id);
                                    }
                                }).addOnFailureListener(AddReplyScreen.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(AddReplyScreen.this, "could't send", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });


            } else {
                mDesEditText.setError("required");
            }

        } else {
            updateUserNameDialog();
        }


    }

    private void updateUserNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddReplyScreen.this);

        // get the layout inflater
        LayoutInflater inflater = AddReplyScreen.this.getLayoutInflater();

        View view = inflater.inflate(R.layout.update_username_layout, null);
        final EditText fName = (EditText) view.findViewById(R.id.dialog_editText);
        final EditText LName = (EditText) view.findViewById(R.id.dialog_editText2);
        builder.setTitle("Update Username");
        builder.setView(view)
                // action buttons
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String firstName = fName.getText().toString();
                        String lastName = LName.getText().toString();
                        if (firstName.isEmpty() && lastName.isEmpty()) {
                            Toast.makeText(AddReplyScreen.this, "Fill all the fields", Toast.LENGTH_SHORT).show();
                        } else {

                            updateUserName(firstName, lastName);
                        }
                        // your sign in code here
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // remove the dialog from the screen
                    }
                })
                .show();
    }

    private void updateUserName(final String firstName, final String lastName) {

        final ProgressDialog progressDialog = new ProgressDialog(AddReplyScreen.this);
        progressDialog.setMessage("Updating username...");
        progressDialog.show();
        Map<String, Object> map = new HashMap<>();
        map.put(F_NAME, firstName);
        map.put(L_NAME, lastName);
        firestore.collection(USERS).document(user.getUid())
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        final SharedPreferences.Editor editor = pref.edit();
                        editor.putString(F_NAME, firstName);
                        editor.putString(L_NAME, lastName);
                        editor.commit();
                        Toast.makeText(AddReplyScreen.this, "Username updated successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(AddReplyScreen.this, "cant update username, try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadData(final String id) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Sending Data");
        progressDialog.show();

        final List<Map> imageMaps = new ArrayList<>();
        if (!imagesEncodedList.isEmpty()) {
            for (int i = 0; i < imagesEncodedList.size(); i++) {
                final String STORAGE_PATH = "correspondence_images/" + Id + "/" + System.currentTimeMillis() + ".jpg";
                StorageReference spaceRef = storageRef.child(STORAGE_PATH);

                UploadTask uploadTask = spaceRef.putFile(imagesEncodedList.get(i));
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddReplyScreen.this, "failed to change profile", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.child(STORAGE_PATH).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Map<String, Object> attachmentMap = new HashMap<>();
                                attachmentMap.put(IMAGE_ATTACHMENT, uri.toString());
                                imageMaps.add(attachmentMap);
                                firestore.collection(CORRESPONDENCE_QUERY).document(Id).collection(REPLY)
                                        .document(id)
                                        .update(IMAGE_ATTACHMENT, imageMaps).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(AddReplyScreen.this, "sent", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AddReplyScreen.this, "could't sent, try again", Toast.LENGTH_SHORT).show();
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
        final List<Map> pdfMaps = new ArrayList<>();

        if (!documentEncodedList.isEmpty()) {
            for (int i = 0; i < documentEncodedList.size(); i++) {
                final String STORAGE_PATH = "correspondence_documents/" + Id + "/" + System.currentTimeMillis() + ".pdf";
                StorageReference spaceRef = storageRef.child(STORAGE_PATH);
                UploadTask uploadTask = spaceRef.putFile(documentEncodedList.get(i));
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddReplyScreen.this, "failed to change profile", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.child(STORAGE_PATH).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Map<String, Object> attachmentMap = new HashMap<>();
                                attachmentMap.put(PDF_ATTACHMENT, uri.toString());
                                pdfMaps.add(attachmentMap);

                                firestore.collection(CORRESPONDENCE_QUERY).document(Id).collection(REPLY)
                                        .document(id)
                                        .update(PDF_ATTACHMENT, pdfMaps).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(AddReplyScreen.this, "sent", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(AddReplyScreen.this, "could't sent, try again", Toast.LENGTH_SHORT).show();
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

        final List<Map> singleImageMaps = new ArrayList<>();
        if (selectedImage != null) {
            final String STORAGE_PATH = "correspondence_images/" + Id + "/" + System.currentTimeMillis() + ".jpg";
            StorageReference spaceRef = storageRef.child(STORAGE_PATH);

            UploadTask uploadTask = spaceRef.putFile(selectedImage);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddReplyScreen.this, "failed to change profile", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageRef.child(STORAGE_PATH).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map<String, Object> attachmentMap = new HashMap<>();
                            attachmentMap.put(IMAGE_ATTACHMENT, uri.toString());
                            singleImageMaps.add(attachmentMap);
                            firestore.collection(CORRESPONDENCE_QUERY).document(Id).collection(REPLY)
                                    .document(id)
                                    .update(IMAGE_ATTACHMENT, singleImageMaps).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(AddReplyScreen.this, "sent", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(AddReplyScreen.this, "could't sent, try again", Toast.LENGTH_SHORT).show();
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


    public void selectImages(View view) {
        final CharSequence[] items = {"Select Single Image", "Select Multiple Image"};

        AlertDialog.Builder builder = new AlertDialog.Builder(AddReplyScreen.this);
        builder.setTitle("Make your selection");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    selectImage();
                } else {
                    selectMultipleImages();
                }
                dialog.dismiss();

            }
        }).show();
    }

    public void selectMultipleImages() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
        }
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
                imageAdapter = new PickedImageAdapter(imagesEncodedList);
                recyclerView.setAdapter(imageAdapter);
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
                            recyclerView.setVisibility(View.VISIBLE);
                            cursor.close();

                        }
                        Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
                        Toast.makeText(this, "Selected Images" + mArrayUri.size(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

        switch (requestCode) {
            case 1001:
                // Checking whether data is null or not
                if (data != null) {


                    // Checking for selection multiple files or single.
                    if (data.getClipData() != null) {


                        // Getting the length of data and logging up the logs using index
                        for (int index = 0; index < data.getClipData().getItemCount(); index++) {

                            // Getting the URIs of the selected files and logging them into logcat at debug level
                            Uri uri = data.getClipData().getItemAt(index).getUri();
                            Log.d("filesUri [" + uri + "] : ", String.valueOf(uri));
                            documentEncodedList.add(uri);

                        }
                        StringBuilder builder = new StringBuilder();
                        mPdfFiles.setText("");
                        for (int a = 0; a < documentEncodedList.size(); a++) {
                            int counter = a + 1;
                            builder.append("pdf File: " + counter + "\n");
                        }
                        mPdfFiles.setText(builder.toString());

                    } else {

                        // Getting the URI of the selected file and logging into logcat at debug level
                        Uri uri = data.getData();
                        Log.d("fileUri: ", String.valueOf(uri));
                        documentEncodedList.add(uri);
                        mPdfFiles.setText("pdf File: 1\n");
                    }
                }
                break;
        }

        if (requestCode == PICK_IMAGE_CAMERA) {
            try {
                bitmap = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
                selectedImage = getImageUri(AddReplyScreen.this, bitmap);
                Log.e("Activity", "Pick from Camera::>>> ");

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                destination = new File(Environment.getExternalStorageDirectory() + "/" +
                        getString(R.string.app_name), "IMG_" + timeStamp + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                imgPath = destination.getAbsolutePath();
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                e.printStackTrace();
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
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class PickedImageAdapter extends RecyclerView.Adapter<PickedImageAdapter.MyViewHolder> {
        List<Uri> imagesEncodedList;

        public PickedImageAdapter(List<Uri> imagesEncodedList) {
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

            Uri uri = imagesEncodedList.get(position);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                holder.imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
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

    public void addPdf(View view) {
        // Start intent from your method
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("*/*");
        startActivityForResult(intent, 1001);
    }

    private void hideKeyBoard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void selectImage() {
        try {
            PackageManager pm = getPackageManager();
            int hasPerm = pm.checkPermission(Manifest.permission.CAMERA, getPackageName());
            if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                final CharSequence[] options = {"Take Photo", "Choose From Gallery", "Cancel"};
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(AddReplyScreen.this);
                builder.setTitle("Select Option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Photo")) {
                            dialog.dismiss();
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, PICK_IMAGE_CAMERA);
                        } else if (options[item].equals("Choose From Gallery")) {
                            dialog.dismiss();
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, PICK_IMAGE_GALLERY);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            } else {
                Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
                requestCameraPermission();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Camera Permission error", Toast.LENGTH_SHORT).show();
            requestCameraPermission();
            e.printStackTrace();
        }
    }

    private void requestCameraPermission() {
        Log.i(TAG, "CAMERA permission has NOT been granted. Requesting permission.");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            Log.i(TAG,
                    "Displaying camera permission rationale to provide additional context.");
            ActivityCompat.requestPermissions(AddReplyScreen.this,
                    new String[]{Manifest.permission.CAMERA},
                    PICK_IMAGE_CAMERA);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PICK_IMAGE_CAMERA);
        }


        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.i(TAG,
                    "Displaying camera permission rationale to provide additional context.");
            ActivityCompat.requestPermissions(AddReplyScreen.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_STORAGE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_STORAGE);
        }

    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
