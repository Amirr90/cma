package com.jccode.mycorrespondence;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jccode.mycorrespondence.utility.Util.ADDRESS;
import static com.jccode.mycorrespondence.utility.Util.BRANCH;
import static com.jccode.mycorrespondence.utility.Util.F_NAME;
import static com.jccode.mycorrespondence.utility.Util.GENDER;
import static com.jccode.mycorrespondence.utility.Util.IMAGE;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE;
import static com.jccode.mycorrespondence.utility.Util.IS_SEEN;
import static com.jccode.mycorrespondence.utility.Util.L_NAME;
import static com.jccode.mycorrespondence.utility.Util.MOBILE;
import static com.jccode.mycorrespondence.utility.Util.PERMANENT_ADDRESS;
import static com.jccode.mycorrespondence.utility.Util.POST;

public class UserProfile extends AppCompatActivity {

    private static final String DEACTIVATE = "Deactivate";
    private static final String ACTIVATE = "Activate";
    Toolbar toolbar;
    Spinner spinnerGender, spinnerPost;
    EditText firstName, lastName, mobile, address, email, permanentAddress, branch, gender;
    CircleImageView mProfileImage;
    FirebaseFirestore firestore;
    String UserId;
    TextView btnActivateDeactivate, btnSendMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        toolbar = (Toolbar) findViewById(R.id.u_profile_toolbar);

        UserId = getIntent().getStringExtra("userId");
        firestore = FirebaseFirestore.getInstance();
        setToolbar(toolbar, "Profile");

        hideKeyBoard();
        findViewById();


        setProfileData();


    }

    private void setProfileData() {

        firestore.collection("Users")
                .document(getIntent().getStringExtra("userId"))
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        final String imageUrl = documentSnapshot.getString(IMAGE);
                        if (imageUrl != null && !imageUrl.equalsIgnoreCase(""))
                            Picasso.with(UserProfile.this).load(imageUrl).networkPolicy(NetworkPolicy.OFFLINE).into(mProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(UserProfile.this).load(imageUrl).into(mProfileImage);
                                }
                            });


                        firstName.setText(documentSnapshot.getString(F_NAME));
                        lastName.setText(documentSnapshot.getString(L_NAME));
                        address.setText(documentSnapshot.getString(ADDRESS));
                        mobile.setText(documentSnapshot.getString(MOBILE));
                        gender.setText(documentSnapshot.getString(GENDER));
                        permanentAddress.setText(documentSnapshot.getString(PERMANENT_ADDRESS));
                        branch.setText(documentSnapshot.getString(BRANCH) + "\nPost: " + documentSnapshot.getString(POST));


                    }
                });
    }

    private void findViewById() {
        mProfileImage = (CircleImageView) findViewById(R.id.u_profile_image);
        firstName = (EditText) findViewById(R.id.u_editText);
        lastName = (EditText) findViewById(R.id.u_editText2);
        mobile = (EditText) findViewById(R.id.u_mobile);
        gender = (EditText) findViewById(R.id.u_gender);
        address = (EditText) findViewById(R.id.u_editText5);
        email = (EditText) findViewById(R.id.u_editText4);
        permanentAddress = (EditText) findViewById(R.id.u_editText6);
        branch = (EditText) findViewById(R.id.u_editText7);
        btnActivateDeactivate = (TextView) findViewById(R.id.acti_deacti_btn);
        btnSendMessage = (TextView) findViewById(R.id.send_msg_btn);

        setActivateDeactivateStatus();
    }

    private void setActivateDeactivateStatus() {
        firestore.collection("Users")
                .document(UserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (!documentSnapshot.exists()) {
                            return;
                        }
                        Boolean isActive = documentSnapshot.getBoolean(IS_ACTIVE);
                        if (isActive != null) {
                            if (isActive) {
                                btnActivateDeactivate.setText(DEACTIVATE);
                            } else {
                                btnActivateDeactivate.setText(ACTIVATE);
                            }
                        }
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
        finish();
        return true;
    }


    public void DeActivateUser(View v) {
        if (btnActivateDeactivate.getText().toString().equalsIgnoreCase(ACTIVATE)) {
            firestore.collection("Users")
                    .document(UserId)
                    .update(IS_ACTIVE, true);
            btnActivateDeactivate.setText(DEACTIVATE);
            Snackbar.make(v, "User Activated", Snackbar.LENGTH_SHORT).show();

        } else {
            firestore.collection("Users")
                    .document(UserId)
                    .update(IS_ACTIVE, false);
            btnActivateDeactivate.setText(ACTIVATE);
            Snackbar.make(v, "User DeActivated", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void sendMessageToUser(View view) {
        startActivity(new Intent(UserProfile.this, ChatUsScreen.class));
    }


    private void hideKeyBoard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

}
