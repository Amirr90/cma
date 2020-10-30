package com.jccode.mycorrespondence;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jccode.mycorrespondence.utility.Util;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jccode.mycorrespondence.utility.Util.ADDRESS;
import static com.jccode.mycorrespondence.utility.Util.BRANCH;
import static com.jccode.mycorrespondence.utility.Util.EMAIL;
import static com.jccode.mycorrespondence.utility.Util.F_NAME;
import static com.jccode.mycorrespondence.utility.Util.GENDER;
import static com.jccode.mycorrespondence.utility.Util.IMAGE;
import static com.jccode.mycorrespondence.utility.Util.L_NAME;
import static com.jccode.mycorrespondence.utility.Util.MOBILE;
import static com.jccode.mycorrespondence.utility.Util.MyPREFERENCES;
import static com.jccode.mycorrespondence.utility.Util.PERMANENT_ADDRESS;
import static com.jccode.mycorrespondence.utility.Util.POST;
import static com.jccode.mycorrespondence.utility.Util.USERS;
import static com.jccode.mycorrespondence.utility.Util.USER_TOKEN;
import static com.jccode.mycorrespondence.utility.Util.USER_TYPE;

public class Profile extends AppCompatActivity {
    private static final int PICK_IMAGE_REQ_CODE = 1;
    private String STORAGE_PATH;
    Toolbar toolbar;
    EditText firstName, lastName, mobile, address, email, permanentAddress, branch;
    String gender, post;
    FirebaseFirestore firestore;
    ProgressBar progressBar;
    FirebaseUser user;
    CircleImageView mProfileImage;
    SharedPreferences pref;
    TextView tvPost, tvGender;
    Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = (Toolbar) findViewById(R.id.profile_toolbar);

        util = new Util();
        firestore = FirebaseFirestore.getInstance();

        setToolbar(toolbar, "Profile");

        user = FirebaseAuth.getInstance().getCurrentUser();
        STORAGE_PATH = "profile_images/" + user.getPhoneNumber() + ".jpg";
        findViewById();
        loadProfileData();

        hideKeyBoard();

        firestore.collection("Users").document(user.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String type = documentSnapshot.getString(USER_TYPE);
                    if (type.equalsIgnoreCase("Other")) {
                        tvPost.setVisibility(View.GONE);
                    } else {
                        tvPost.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(Profile.this, "No user found", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void hideKeyBoard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void findViewById() {
        mProfileImage = (CircleImageView) findViewById(R.id.profile_image);
        progressBar = (ProgressBar) findViewById(R.id.progressBar4);
        firstName = (EditText) findViewById(R.id.editText);
        lastName = (EditText) findViewById(R.id.editText2);
        mobile = (EditText) findViewById(R.id.mobile);
        address = (EditText) findViewById(R.id.editText5);
        email = (EditText) findViewById(R.id.editText4);
        permanentAddress = (EditText) findViewById(R.id.editText6);
        branch = (EditText) findViewById(R.id.editText7);
        tvGender = (TextView) findViewById(R.id.tv_Gender);
        tvPost = (TextView) findViewById(R.id.textView35);
    }

    public void setGenderSpinner(View view) {
        final CharSequence[] items = {"Male", "Female"};

        AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        builder.setTitle("Make your selection");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String gender = items[item].toString();
                tvGender.setText(gender);
                dialog.dismiss();

            }
        }).show();

    }

    public void setPostSpinner(View view) {


        final CharSequence[] text0 = {"Office Assistant", "Officer"};
        AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        builder.setTitle("Make your selection");
        builder.setItems(text0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 1) {
                    final String[] items = {"Scale I", "Scale II", "Scale III", "Scale IV", "Scale V"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                    builder.setTitle("Make your selection");
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            post = "Officer: " + items[item];
                            tvPost.setText(post);
                            dialog.dismiss();
                        }
                    }).show();
                } else {
                    post = "Office Assistant";
                    tvPost.setText(post);
                }
            }
        }).show();


    }

    public void logout(View view) {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(Profile.this, "Logged out", Toast.LENGTH_SHORT).show();
                        ProgressDialog progressDialog = new ProgressDialog(Profile.this);
                        progressDialog.setMessage("Logging Out...");
                        progressDialog.show();
                        firestore.collection(USERS).document(user.getUid())
                                .update(USER_TOKEN, "");
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString(IMAGE, "");
                        progressDialog.dismiss();
                        Intent intent = new Intent(Profile.this,
                                SplashScreen.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        SharedPreferences settings = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        settings.edit().clear().commit();
                        startActivity(intent);
                        finish();
                    }
                });
    }

    public void saveProfileData(final View view) {
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> map = new HashMap<>();
        map.put(F_NAME, firstName.getText().toString());
        map.put(L_NAME, lastName.getText().toString());
        map.put(ADDRESS, address.getText().toString());
        map.put(MOBILE, user.getPhoneNumber());
        map.put(PERMANENT_ADDRESS, permanentAddress.getText().toString());
        map.put(EMAIL, email.getText().toString());
        map.put(BRANCH, branch.getText().toString());
        map.put(GENDER, tvGender.getText().toString());
        map.put(POST, tvPost.getText().toString());

        //send To database
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            firestore.collection(USERS)
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .update(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            util.saveUserData(Profile.this, user);
                            Snackbar.make(view, "Profile updated successfully", Snackbar.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(view, "could't updated profile", Snackbar.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }

    }


    public void pickImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("outputX", 256);
        intent.putExtra("outputY", 256);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PICK_IMAGE_REQ_CODE);
    }

    private void loadProfileData() {
        pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE); // 0 - for private mode
        final String imageUrl = pref.getString(IMAGE, null);
        if (imageUrl != null && !imageUrl.equalsIgnoreCase(""))
            Picasso.with(Profile.this).load(imageUrl).networkPolicy(NetworkPolicy.OFFLINE).into(mProfileImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(Profile.this).load(imageUrl).into(mProfileImage);
                }
            });

        if (pref.contains(F_NAME)) {
            firstName.setText(pref.getString(F_NAME, ""));
        }

        if (pref.contains(L_NAME)) {
            lastName.setText(pref.getString(L_NAME, ""));
        }

        if (pref.contains(ADDRESS)) {
            address.setText(pref.getString(ADDRESS, ""));
        }

        if (pref.contains(EMAIL)) {
            email.setText(pref.getString(EMAIL, ""));
        }

        if (pref.contains(PERMANENT_ADDRESS)) {
            permanentAddress.setText(pref.getString(PERMANENT_ADDRESS, ""));
        }

        if (pref.contains(BRANCH)) {
            branch.setText(pref.getString(BRANCH, ""));
        }

        if (pref.contains(GENDER)) {
            tvGender.setText(pref.getString(GENDER, ""));
        }

        if (pref.contains(POST)) {
            tvPost.setText(pref.getString(POST, ""));
        }
        mobile.setText(user.getPhoneNumber());


    }

    private void setToolbar(Toolbar toolbar, String id) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(id);
    }

    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_drafts:
                startActivity(new Intent(this, DraftsScreen.class));
                return true;
            case R.id.action_donate_screen:
                startActivity(new Intent(this, DonateActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == PICK_IMAGE_REQ_CODE) {
            final Bundle extras = data.getExtras();
            if (extras != null && data.getData() != null) {
                //Get image
                Bitmap bitmap = extras.getParcelable("data");
                mProfileImage.setImageBitmap(bitmap);
                //uploading image
                try {

                    progressBar.setVisibility(View.VISIBLE);
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    final StorageReference storageRef = storage.getReference();
                    StorageReference spaceRef = storageRef.child(STORAGE_PATH);

                    mProfileImage.setDrawingCacheEnabled(true);
                    mProfileImage.buildDrawingCache();
                    Bitmap bitmap2 = ((BitmapDrawable) mProfileImage.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] compressData = baos.toByteArray();

                    UploadTask uploadTask = spaceRef.putBytes(compressData);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Profile.this, "failed to change profile", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageRef.child(STORAGE_PATH).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    firestore.collection(USERS).document(user.getUid())
                                            .update(IMAGE, uri.toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(Profile.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(Profile.this, "image upload failed", Toast.LENGTH_SHORT).show();
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
                } catch (Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }
    }
}
