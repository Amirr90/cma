package com.jccode.mycorrespondence;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dinuscxj.refresh.RecyclerRefreshLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jccode.mycorrespondence.utility.TimeAgo;
import com.jccode.mycorrespondence.utility.Util;
import com.kcode.bottomlib.BottomDialog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jccode.mycorrespondence.utility.Util.ADDRESS;
import static com.jccode.mycorrespondence.utility.Util.ADMIN_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.BRANCH;
import static com.jccode.mycorrespondence.utility.Util.COMPOSE;
import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DATE;
import static com.jccode.mycorrespondence.utility.Util.DEPARTMENT;
import static com.jccode.mycorrespondence.utility.Util.DESCRIPTION;
import static com.jccode.mycorrespondence.utility.Util.EMAIL;
import static com.jccode.mycorrespondence.utility.Util.FILED;
import static com.jccode.mycorrespondence.utility.Util.F_NAME;
import static com.jccode.mycorrespondence.utility.Util.GENDER;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.IMAGE;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE;
import static com.jccode.mycorrespondence.utility.Util.IS_ADMIN;
import static com.jccode.mycorrespondence.utility.Util.LETTER_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.L_NAME;
import static com.jccode.mycorrespondence.utility.Util.MyPREFERENCES;
import static com.jccode.mycorrespondence.utility.Util.PARTIALLY_REPLIED;
import static com.jccode.mycorrespondence.utility.Util.PERMANENT_ADDRESS;
import static com.jccode.mycorrespondence.utility.Util.POST;
import static com.jccode.mycorrespondence.utility.Util.REMINDER;
import static com.jccode.mycorrespondence.utility.Util.REPLIED;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TITLE;
import static com.jccode.mycorrespondence.utility.Util.TYPE;
import static com.jccode.mycorrespondence.utility.Util.UID;
import static com.jccode.mycorrespondence.utility.Util.UN_REPLIED;
import static com.jccode.mycorrespondence.utility.Util.USERS;
import static com.jccode.mycorrespondence.utility.Util.USER_TOKEN;
import static com.jccode.mycorrespondence.utility.Util.USER_TYPE;

public class HomeScreen extends AppCompatActivity {
    private static final String TAG = "HomeScreen";
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    androidx.appcompat.widget.Toolbar toolbar;
    public TextView more, btnLogin;
    public CircleImageView mProfileImage;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    List<DocumentSnapshot> homeList;
    private RecyclerRefreshLayout swipe_refresh;
    HomeAdapter adapter;
    private long backPressedTime;
    private String From = "HomeScreen";
    TimeAgo timeAgo;
    private SharedPreferences sharedpreferences;
    FirebaseUser user;
    RelativeLayout mNoCorrespondenceLayout;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading data,please wait....");
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        timeAgo = new TimeAgo();
        findViewById();
        if (auth.getCurrentUser() != null) {
            FirebaseUser user = auth.getCurrentUser();
            checkAlreadyUserStatus(user);
            loadHomeScreenData();

            //saveUserData(this);
            //loadProfileData();
            swipe_refresh.setRefreshStyle(RecyclerRefreshLayout.RefreshStyle.NORMAL);
            swipe_refresh.setOnRefreshListener(new RecyclerRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipe_refresh.setNestedScrollingEnabled(true);
                    loadData(adapter);
                }
            });

            //subscribeToTopic();

            //updateToken();

            //checkAdminStatus();
        } else {
            finish();
        }
    }

    private void checkAdminStatus() {

        sharedpreferences = this.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(IS_ADMIN, false);
        editor.commit();
        final String userNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        firestore.collection("Admin")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> adminSnapshots = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot snapshot : adminSnapshots) {
                            if (snapshot.getString(ADMIN_NUMBER).equalsIgnoreCase(userNumber)) {
                                editor.putBoolean(IS_ADMIN, true);
                                editor.commit();
                            }

                        }
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

    private void loadProfileData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE); // 0 - for private mode
        final String imageUrl = pref.getString(IMAGE, null);
        if (imageUrl != null && !imageUrl.equalsIgnoreCase(""))
            Picasso.with(HomeScreen.this).load(imageUrl).networkPolicy(NetworkPolicy.OFFLINE).into(mProfileImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(HomeScreen.this).load(imageUrl).into(mProfileImage);
                }
            });


    }


    public void saveUserData(Context context) {
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedpreferences.edit();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            user = FirebaseAuth.getInstance().getCurrentUser();
            firestore.collection(USERS).document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    if (snapshot.exists()) {
                        editor.putString(IMAGE, snapshot.getString(IMAGE));
                        editor.putString(F_NAME, snapshot.getString(F_NAME));
                        editor.putString(L_NAME, snapshot.getString(L_NAME));

                        editor.putString(ADDRESS, snapshot.getString(ADDRESS));
                        editor.putString(PERMANENT_ADDRESS, snapshot.getString(PERMANENT_ADDRESS));
                        editor.putString(EMAIL, snapshot.getString(EMAIL));

                        editor.putString(BRANCH, snapshot.getString(BRANCH));
                        editor.putString(POST, snapshot.getString(POST));
                        editor.putString(GENDER, snapshot.getString(GENDER));

                        editor.putString(USER_TOKEN, snapshot.getString(USER_TOKEN));

                        editor.commit();
                    }

                }
            });
        }

    }

    private void loadHomeScreenData() {
        homeList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new HomeAdapter(homeList, this);
        recyclerView.setAdapter(adapter);
        loadData(adapter);

    }

    private void loadData(final HomeAdapter adapter, boolean isActive) {
        firestore.collection(CORRESPONDENCE_QUERY)
                .whereEqualTo(UID, FirebaseAuth.getInstance().getCurrentUser().getUid())
                .whereEqualTo(IS_ACTIVE, isActive)
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        progressBar.setVisibility(View.GONE);
                        if (e == null) {
                            if (queryDocumentSnapshots != null) {
                                homeList.clear();
                                homeList.addAll(queryDocumentSnapshots.getDocuments());
                                adapter.notifyDataSetChanged();
                                swipe_refresh.setRefreshing(false);

                            }

                        } else {
                            Log.d(TAG, "onEvent: " + e.getLocalizedMessage());
                            Toast.makeText(HomeScreen.this, "load again: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void loadData(final HomeAdapter adapter) {

        firestore.collection(CORRESPONDENCE_QUERY)
                .whereEqualTo(UID, FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        progressBar.setVisibility(View.GONE);

                        if (e == null) {
                            if (queryDocumentSnapshots.isEmpty()) {
                                mNoCorrespondenceLayout.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                                swipe_refresh.setRefreshing(false);
                            } else {
                                if (queryDocumentSnapshots != null) {
                                    mNoCorrespondenceLayout.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    homeList.clear();
                                    homeList.addAll(queryDocumentSnapshots.getDocuments());
                                    adapter.notifyDataSetChanged();
                                    swipe_refresh.setRefreshing(false);
                                }
                            }

                        } else {
                            swipe_refresh.setRefreshing(false);
                            Log.d(TAG, "onEvent: " + e.getLocalizedMessage());
                            Toast.makeText(HomeScreen.this, "load again: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void loadData(final String tag) {
        progressDialog.show();
        firestore.collection(CORRESPONDENCE_QUERY)
                .whereEqualTo(UID, FirebaseAuth.getInstance().getCurrentUser().getUid())
                //.orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(HomeScreen.this, "please try again", Toast.LENGTH_SHORT).show();
                        } else {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                homeList.clear();
                                final List<DocumentSnapshot> snapshot = queryDocumentSnapshots.getDocuments();
                                for (final DocumentSnapshot snapshot1 : snapshot) {
                                    String Tag = snapshot1.getString(Util.TAG);
                                    if (Tag.equalsIgnoreCase(tag)) {
                                        homeList.add(snapshot1);
                                    } else {
                                        firestore.collection(CORRESPONDENCE_QUERY)
                                                .document(snapshot1.getId())
                                                .collection(COMPOSE)
                                                //.orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                                                .get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                                                        for (DocumentSnapshot documentSnapshot : snapshots) {
                                                            if (documentSnapshot.getString(Util.TAG) != null
                                                                    && documentSnapshot.getString(Util.TAG).equalsIgnoreCase(tag)) {
                                                                homeList.add(snapshot1);
                                                            }
                                                        }
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                adapter.notifyDataSetChanged();
                                            }
                                        });

                                    }

                                }


                                adapter.notifyDataSetChanged();

                            }
                            progressDialog.dismiss();
                        }
                    }
                });

    }


    private void findViewById() {
        mNoCorrespondenceLayout = (RelativeLayout) findViewById(R.id.no_correspondence);
        swipe_refresh = (RecyclerRefreshLayout) findViewById(R.id.refresh_layout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.home_rec);
        more = (TextView) toolbar.findViewById(R.id.more);
        btnLogin = (TextView) toolbar.findViewById(R.id.login_text);
        mProfileImage = (CircleImageView) toolbar.findViewById(R.id.tool_profile_image);

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (auth.getCurrentUser() != null)
                    startActivity(new Intent(HomeScreen.this, Profile.class));

            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });
    }

    private void showBottomDialog() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE); // 0 - for private mode
        final boolean isAdmin = pref.getBoolean(IS_ADMIN, false);
        String chats, Donate_or_All_user;
        if (isAdmin) {
            Donate_or_All_user = "Show All Users";
            chats = "Chats";
        } else {
            Donate_or_All_user = "Donate Some Love";
            chats = "Chat with us";
        }

        final String shareText = "Share CMA App\nhttps://play.google.com/store/apps/details?id=com.e.quizzy";
        BottomDialog dialog = BottomDialog.newInstance("More", "DISMISS", new String[]{Donate_or_All_user, "Drafts",
                "Donation history", "Sort Correspondence", chats, "Share App"});
        dialog.show(getSupportFragmentManager(), "CMA");
        //add item click listener
        dialog.setListener(new BottomDialog.OnClickListener() {
            @Override
            public void click(int position) {
                switch (position) {

                    case 0:
                        if (isAdmin)
                            startActivity(new Intent(HomeScreen.this, ShowAllUsers.class));
                        else
                            startActivity(new Intent(HomeScreen.this, DonateActivity.class));
                        break;

                    case 1:
                        startActivity(new Intent(HomeScreen.this, DraftsScreen.class));
                        break;

                    case 2:
                        startActivity(new Intent(HomeScreen.this, DonateHistoryScreen.class));
                        break;
                    case 3:
                        final CharSequence[] items = {"Show All Correspondence", "Show Active only",
                                "Show Close only",
                                "Show Un-Replied only", "Show Partial-Replied only",
                                "Show Replied only", "Show File only"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);
                        builder.setTitle("Make your selection");
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                switch (item) {
                                    case 0:
                                        loadData(adapter);
                                        break;
                                    case 1:
                                        loadData(adapter, true);
                                        break;
                                    case 2:
                                        loadData(adapter, false);
                                        break;
                                    case 3:
                                        loadData(UN_REPLIED);
                                        break;

                                    case 4:
                                        loadData(PARTIALLY_REPLIED);
                                        break;

                                    case 5:
                                        loadData(REPLIED);
                                        break;

                                    case 6:
                                        loadData(FILED);
                                        break;

                                    default: {
                                        dialog.dismiss();
                                    }
                                }
                                dialog.dismiss();

                            }
                        }).show();
                        break;

                    case 4: {
                        if (isAdmin) {
                            startActivity(new Intent(HomeScreen.this, ShowAllChats.class));
                        } else
                            startActivity(new Intent(HomeScreen.this, ChatUsScreen.class));
                    }

                    break;
                    case 5:
                        Intent shareIntent = ShareCompat.IntentBuilder.from(HomeScreen.this)
                                .setType("text/plain")
                                .setText(shareText)
                                .getIntent();
                        if (shareIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(shareIntent);
                        }
                        /* dispatchTakePictureIntent();*/
                        break;
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

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreen.this);
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
        String[] types = getResources().getStringArray(R.array.choices);
        map.put(USER_TYPE, types[selectedPosition]);
        firestore.collection(USERS)
                .document(user.getUid())
                .set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(HomeScreen.this, "updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getUserInformation(DocumentSnapshot document) {
        String type = document.getString(USER_TYPE);

    }


    private class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {

        List<DocumentSnapshot> list;
        Context context;

        public HomeAdapter(List<DocumentSnapshot> homeList, Context context) {
            this.list = homeList;
            this.context = context;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_view, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {


            String subject = list.get(position).getString(SUBJECT);
            String description = list.get(position).getString(DESCRIPTION);
            long timeStamp = list.get(position).getLong(TIMESTAMP);

            holder.timeStamp.setText(timeAgo.getlongtoago(timeStamp));

            holder.mSubject.setText(subject);
            holder.mDescription.setText(description);

            if (list.get(position).getBoolean(IS_ACTIVE)) {
                holder.mActiveStatus.setText("Open");
                holder.mActiveStatus.setTextColor(Color.GREEN);
            } else {
                holder.mActiveStatus.setText("Close");
                holder.mActiveStatus.setTextColor(Color.RED);
            }

            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String id = list.get(position).getId();
                    String post_uid = list.get(position).getString(UID);
                    String reminder = list.get(position).getLong(REMINDER) + "";
                    String title = list.get(position).getString(TITLE);
                    String tag = list.get(position).getString(Util.TAG);
                    String Type = list.get(position).getString(TYPE);
                    String isActive = "" + list.get(position).getBoolean(IS_ACTIVE);
                    String subject = list.get(position).getString(SUBJECT);
                    String description = list.get(position).getString(DESCRIPTION);
                    String department = list.get(position).getString(DEPARTMENT);
                    String letterType = list.get(position).getString(TYPE);
                    String letterNumber = list.get(position).getString(LETTER_NUMBER);
                    String timestamp = "" + list.get(position).getLong(TIMESTAMP);
                    String date = list.get(position).getString(DATE);
                    context.startActivity(new Intent(context, CorrespondingDetail.class)
                            .putExtra(TITLE, title)
                            .putExtra(REMINDER, reminder)
                            .putExtra("TAG", tag)
                            .putExtra(DATE, date)
                            .putExtra(DEPARTMENT, department)
                            .putExtra(DESCRIPTION, description)
                            .putExtra(UID, post_uid)
                            .putExtra(TYPE, Type)
                            .putExtra(SUBJECT, subject)
                            .putExtra(IS_ACTIVE, isActive)
                            .putExtra(TIMESTAMP, timestamp)
                            .putExtra(TYPE, letterType)
                            .putExtra(LETTER_NUMBER, letterNumber)
                            .putExtra(ID, id));


                }
            });


            holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    final String doc_id = list.get(position).getId();
                    new AlertDialog.Builder(HomeScreen.this)
                            .setTitle("Delete Correspondence")
                            .setMessage("Want to delete this correspondence??")
                            .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    dialog.cancel();
                                    final ProgressDialog progressDialog = new ProgressDialog(HomeScreen.this);
                                    progressDialog.setMessage("Deleting....");
                                    progressDialog.show();
                                    firestore.collection(CORRESPONDENCE_QUERY).document(doc_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progressDialog.dismiss();
                                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    }).show();
                    return false;
                }
            });

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView mSubject, mDescription, mActiveStatus, timeStamp;
            private CircleImageView mCategoryImage;
            private RelativeLayout layout;


            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                mSubject = (TextView) itemView.findViewById(R.id.textView);
                mDescription = (TextView) itemView.findViewById(R.id.textView2);
                mActiveStatus = (TextView) itemView.findViewById(R.id.textView3);
                mCategoryImage = (CircleImageView) itemView.findViewById(R.id.profile_image);
                timeStamp = (TextView) itemView.findViewById(R.id.textView13);
                layout = (RelativeLayout) itemView.findViewById(R.id.home_view_lay);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {

            finish();

        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_LONG).show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    public void composeNewCorrespondence(View view) {
        startActivity(new Intent(this, CreateCorrespondenceScreen.class)
                .putExtra(Util.FROM, From));
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

    @Override
    protected void onResume() {
        super.onResume();
        saveUserData(this);
    }
}
