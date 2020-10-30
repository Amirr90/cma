package com.jccode.mycorrespondence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jccode.mycorrespondence.utility.Util;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jccode.mycorrespondence.utility.Util.*;

public class HomeScreen2 extends AppCompatActivity {

    String userId;
    FirebaseUser user;

    RecyclerView rvParent;
    List<String> list = new ArrayList<>();
    HomeAdapter parentAdapter;

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    String name, Class, subject, image;

    private SharedPreferences sharedpreferences;

    Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen2);

        user = FirebaseAuth.getInstance().getCurrentUser();

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        util = new Util();
        if (user == null)
            return;

        userId = user.getUid();
        checkActiveUser();


        rvParent = (RecyclerView) findViewById(R.id.rv_parent);
        rvParent.setLayoutManager(new GridLayoutManager(this, 3));
        rvParent.setHasFixedSize(true);
        parentAdapter = new HomeAdapter(this, list);

        rvParent.setAdapter(parentAdapter);

        checkAlreadyUserStatus(user);

        loadData();

        loadUserProfile(userId);

        util.saveUserData(this, user);

        subscribeToTopic();

        updateToken();


    }

    private void checkActiveUser() {
        String mobileNumber = user.getPhoneNumber();
        firestore.collection("Users")
                .whereEqualTo(MOBILE, mobileNumber)
                .whereEqualTo(IS_ACTIVE, false)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e == null && !queryDocumentSnapshots.isEmpty()) {
                            new AlertDialog.Builder(HomeScreen2.this)
                                    .setMessage("Your Account is blocked\nContact Admin")
                                    .setPositiveButton("Chat Admin", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            startActivity(new Intent(HomeScreen2.this, ChatUsScreen.class));
                                        }
                                    }).setCancelable(false)
                                    .show();
                        }
                    }
                });
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

    private void updateUser(FirebaseUser user) {
        showChooseUserTypeDialog(user);
    }

    private void showChooseUserTypeDialog(final FirebaseUser user) {

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen2.this);
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

    private void updateUserInformation(FirebaseUser user, int selectedPosition) {
        Map<String, Object> map = new HashMap<>();
        String new_token = FirebaseInstanceId.getInstance().getToken();

        map.put(IS_ACTIVE, true);
        map.put(USER_TOKEN, new_token);
        map.put(F_NAME, FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        String[] types = getResources().getStringArray(R.array.choices);
        map.put(USER_TYPE, types[selectedPosition]);
        firestore.collection(USERS)
                .document(user.getUid())
                .set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(HomeScreen2.this, "updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getUserInformation(DocumentSnapshot document) {
        String type = document.getString(USER_TYPE);

    }

    private void loadData() {

        list.add("View All\nCorrespondence");
        list.add("Active\nCorrespondence");
        list.add("Closed\nCorrespondence");
        list.add(UN_REPLIED);
        list.add("Partial\nReplied");
        list.add(REPLIED);
        list.add(FILED);
        list.add("Donate Some Love");
        list.add("Chat\nwith us");
        list.add("Donation\nHistory");
        list.add("Add New\nCorrespondence");
        list.add("Profile");
        list.add(SHARED_DOCUMENT);
        list.add(SEARCH_CORRESPONDENCE);
        list.add(SHARE_APP);


    }

    private void loadUserProfile(String userId) {

        firestore.collection("Users")
                .document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {

                            TextView tName = (TextView) findViewById(R.id.t_name);
                            TextView tClass = (TextView) findViewById(R.id.t_class);
                            final CircleImageView tProfile = (CircleImageView) findViewById(R.id.t_profile_image);

                            if (!documentSnapshot.contains(F_NAME))
                                showDialogToUpdateName();
                            else {
                                name = documentSnapshot.getString(F_NAME) + documentSnapshot.getString(L_NAME);
                                Class = documentSnapshot.getString(POST);
                                subject = documentSnapshot.getString(EMAIL);
                                image = documentSnapshot.getString(IMAGE);

                                if (name != null)
                                    tName.setText(name);
                                if (Class != null)
                                    tClass.setText(Class);
                                if (image != null)
                                    Picasso.with(HomeScreen2.this).load(image)
                                            .networkPolicy(NetworkPolicy.OFFLINE)
                                            .placeholder(R.drawable.profile)
                                            .into(tProfile, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError() {
                                                    Picasso.with(HomeScreen2.this).load(image).placeholder(R.drawable.profile).into(tProfile);
                                                }
                                            });
                            }
                        }
                    }
                });
    }

    private void showDialogToUpdateName() {
        new AlertDialog.Builder(this)
                .setMessage("Update Username in profile")
                .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(HomeScreen2.this, Profile.class));
                    }
                }).setCancelable(false)
                .show();
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


    private class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyHomeHolder> {
        Context context;
        List<String> homeList;
        int[] iconsList = {R.drawable.correspondence_all,
                R.drawable.correspondence_active, R.drawable.correspondence_close, R.drawable.correspondence_un_replied,
                R.drawable.correspondence_partial, R.drawable.correspondence_replied, R.drawable.file,
                R.drawable.donation, R.drawable.chat_us, R.drawable.donation_history,
                R.drawable.add_correspondence, R.drawable.profile2, R.drawable.share_documents,
                R.drawable.search_icon, R.drawable.share_app};

        public HomeAdapter(Context context, List<String> homeList) {
            this.context = context;
            this.homeList = homeList;
        }

        @NonNull
        @Override
        public HomeAdapter.MyHomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_parent_view2, parent, false);
            return new MyHomeHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeAdapter.MyHomeHolder holder, final int position) {
            holder.textView.setText(list.get(position));
            holder.imageView.setImageResource(iconsList[position]);

            holder.cardHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String selectedText = list.get(position);
                    switch (selectedText) {
                        case "View All\nCorrespondence": {
                            startActivity(new Intent(context, ShowCorrespondence.class)
                                    .putExtra(SORT_TYPE, "All Correspondence"));

                        }
                        break;
                        case "Active\nCorrespondence": {
                            startActivity(new Intent(context, ShowCorrespondence.class)
                                    .putExtra(SORT_TYPE, "Active Correspondence"));

                        }
                        break;
                        case "Closed\nCorrespondence": {
                            startActivity(new Intent(context, ShowCorrespondence.class)
                                    .putExtra(SORT_TYPE, "Close Correspondence"));

                        }
                        break;
                        case UN_REPLIED: {
                            startActivity(new Intent(context, ShowCorrespondence.class)
                                    .putExtra(SORT_TYPE, "Un-Replied Correspondence"));

                        }
                        break;
                        case "Partial\nReplied": {
                            startActivity(new Intent(context, ShowCorrespondence.class)
                                    .putExtra(SORT_TYPE, "Partial Replied"));

                        }
                        break;
                        case REPLIED: {
                            startActivity(new Intent(context, ShowCorrespondence.class)
                                    .putExtra(SORT_TYPE, "Replied Correspondence"));

                        }

                        case SHARED_DOCUMENT: {
                            startActivity(new Intent(context, ListSharedDocumentsActivity.class));
                            //Snackbar.make(v, "coming soon", Snackbar.LENGTH_SHORT).show();
                        }
                        break;
                        case FILED: {
                            startActivity(new Intent(context, ShowCorrespondence.class)
                                    .putExtra(SORT_TYPE, "File Correspondence"));

                        }
                        break;

                        case "Profile":
                            startActivity(new Intent(context, Profile.class));
                            break;

                        case "Donate Some Love":
                            startActivity(new Intent(context, DonateActivity.class));
                            break;
                        case "Add New\nCorrespondence": {
                            {
                                String From = "HomeScreen";
                                startActivity(new Intent(context, CreateCorrespondenceScreen.class)
                                        .putExtra(Util.FROM, From));
                            }

                        }
                        break;
                        case "Donation\nHistory": {
                            startActivity(new Intent(context, DonateHistoryScreen.class));


                        }
                        break;
                        case "Chat\nwith us": {
                            startActivity(new Intent(context, ChatUsScreen.class));
                        }
                        break;
                        case SHARE_APP: {
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
                            sendIntent.setType("text/plain");
                            Intent shareIntent = Intent.createChooser(sendIntent, null);
                            startActivity(shareIntent);
                        }
                        break;

                        default:
                            Snackbar.make(v, "coming soon", Snackbar.LENGTH_SHORT).show();

                    }


                }
            });
        }

        @Override
        public int getItemCount() {
            return homeList.size();
        }

        public class MyHomeHolder extends RecyclerView.ViewHolder {
            RelativeLayout cardHome;
            TextView textView;
            ImageView imageView;

            public MyHomeHolder(@NonNull View itemView) {
                super(itemView);

                textView = (TextView) itemView.findViewById(R.id.textView);
                cardHome = (RelativeLayout) itemView.findViewById(R.id.card_Home);
                imageView = (ImageView) itemView.findViewById(R.id.home_rec_imageView);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkActiveUser();
    }
}