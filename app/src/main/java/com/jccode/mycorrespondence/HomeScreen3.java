package com.jccode.mycorrespondence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.gauravk.bubblenavigation.BubbleNavigationConstraintView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jccode.mycorrespondence.Fragments.Correspondence;
import com.jccode.mycorrespondence.Fragments.DocumentFragment;
import com.jccode.mycorrespondence.Fragments.HouseHoldsFragment;
import com.jccode.mycorrespondence.Fragments.OldNewFragment;
import com.jccode.mycorrespondence.utility.Util;

import java.util.HashMap;
import java.util.Map;

import static com.jccode.mycorrespondence.utility.Util.F_NAME;
import static com.jccode.mycorrespondence.utility.Util.IMAGE;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE;
import static com.jccode.mycorrespondence.utility.Util.L_NAME;
import static com.jccode.mycorrespondence.utility.Util.MOBILE;
import static com.jccode.mycorrespondence.utility.Util.MyPREFERENCES;
import static com.jccode.mycorrespondence.utility.Util.TAG;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.USERS;
import static com.jccode.mycorrespondence.utility.Util.USER_TOKEN;
import static com.jccode.mycorrespondence.utility.Util.USER_TYPE;
import static com.jccode.mycorrespondence.utility.Util.isInternetAvailable;

public class HomeScreen3 extends AppCompatActivity {


    BubbleNavigationConstraintView bubbleNavigation;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseUser user;
    Util util;
    private SharedPreferences sharedpreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen3);


        util = new Util();
        user = FirebaseAuth.getInstance().getCurrentUser();
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        if (user == null) {
            Toast.makeText(this, "No user found", Toast.LENGTH_SHORT).show();
            return;
        }


        setDocumentCount(3);
        checkAlreadyUserStatus(user);

        bubbleNavigation = (BubbleNavigationConstraintView) findViewById(R.id.top_navigation_constraint);

        bubbleNavigation.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                Fragment fragment = null;
                switch (position) {
                    case 0:
                        fragment = new Correspondence();
                        loadFragment(fragment);
                        break;
                    case 1:
                        fragment = new DocumentFragment();
                        loadFragment(fragment);
                        break;
                    case 2:
                        fragment = new HouseHoldsFragment();
                        loadFragment(fragment);
                        break;
                    case 3:
                        fragment = new OldNewFragment();
                        loadFragment(fragment);
                        break;


                }
            }
        });

        loadFragment(new Correspondence());

        util.saveUserData(this, user);

        subscribeToTopic();

        updateToken();
    }

    private void setDocumentCount(int i) {
        final DocumentReference reference = firestore.collection("Users").document(user.getUid());
        reference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (!snapshot.contains("docCount")) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("docCount", 3);
                                reference.update(map);
                            }
                        }
                    }
                });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isInternetAvailable(this)) {
            checkActiveStatus();
        } else {
            Toast.makeText(this, "No internet connectivity", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkActiveStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        if (user != null)
            firestore.collection("Users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot snapshot) {
                            if (snapshot != null) {
                                try {
                                    Boolean isActive = snapshot.getBoolean(IS_ACTIVE);
                                    if (!isActive) {
                                        showBlockedDialog();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(HomeScreen3.this, "failed to get user Status", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void showBlockedDialog() {

        new AlertDialog.Builder(HomeScreen3.this)
                .setTitle("Blocked Account")
                .setMessage("Your Account is temporarily Blocked ")
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false)
                .show();
    }

    private void checkAlreadyUserStatus(final FirebaseUser user) {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        DocumentReference docIdRef = rootRef.collection("Users").document(user.getUid());
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "Document exists!");
                        getUserInformation(document);

                    } else {
                        Log.d(TAG, "Document does not exist!");
                        updateUser(user);
                    }
                } else {
                    Log.d(TAG, "Failed to read user Data: ", task.getException());
                }
            }
        });


    }

    private void getUserInformation(DocumentSnapshot document) {
        String type = document.getString(USER_TYPE);

    }


    private void updateUser(FirebaseUser user) {
        showChooseUserTypeDialog(user);
    }

    private void showChooseUserTypeDialog(final FirebaseUser user) {

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen3.this);
        builder.setTitle("You are??")
                .setSingleChoiceItems(R.array.choices, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }

                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        updateUserInformation(user, selectedPosition);
                    }
                })
                .setCancelable(false)

                .show();
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_scree_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("newCorrespondenceAdded")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                    }
                });


        FirebaseMessaging.getInstance().subscribeToTopic("notification")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                    }
                });
    }

    private void updateToken() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (pref.contains(F_NAME)) {
            String token = pref.getString(USER_TOKEN, "");
            if (token != null && token.equals("")) {
                String new_token = FirebaseInstanceId.getInstance().getToken();
                firestore.collection(USERS).document(user.getUid())
                        .update(USER_TOKEN, new_token);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(IMAGE, new_token);
                editor.commit();
            }
        }
    }

    private void updateUserInformation(FirebaseUser user, int selectedPosition) {
        Map<String, Object> map = new HashMap<>();
        String new_token = FirebaseInstanceId.getInstance().getToken();

        map.put(IS_ACTIVE, true);
        map.put(USER_TOKEN, new_token);
        map.put(MOBILE, user.getPhoneNumber());
        map.put(L_NAME, "");
        map.put(TIMESTAMP, System.currentTimeMillis());
        map.put(F_NAME, user.getPhoneNumber());
        String[] types = getResources().getStringArray(R.array.choices);
        map.put(USER_TYPE, types[selectedPosition]);
        firestore.collection(USERS)
                .document(user.getUid())
                .set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(HomeScreen3.this, "updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.notification:
                // Action goes here
                //startActivity( new Intent( HomeScreen3.this,NotificationActivity.class ) );
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


}