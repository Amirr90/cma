package com.jccode.mycorrespondence;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dinuscxj.refresh.RecyclerRefreshLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
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
import com.jccode.mycorrespondence.Adapter.AttachmentAdapter;
import com.jccode.mycorrespondence.models.ChatModel;
import com.jccode.mycorrespondence.utility.TimeAgo;
import com.jccode.mycorrespondence.utility.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.jccode.mycorrespondence.utility.Util.CHAT;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.F_NAME;
import static com.jccode.mycorrespondence.utility.Util.IMAGE;
import static com.jccode.mycorrespondence.utility.Util.IS_ADMIN;
import static com.jccode.mycorrespondence.utility.Util.IS_SEEN;
import static com.jccode.mycorrespondence.utility.Util.LAST_MSG;
import static com.jccode.mycorrespondence.utility.Util.L_NAME;
import static com.jccode.mycorrespondence.utility.Util.MESSAGE;
import static com.jccode.mycorrespondence.utility.Util.MyPREFERENCES;
import static com.jccode.mycorrespondence.utility.Util.NAME;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.UID;
import static com.jccode.mycorrespondence.utility.Util.USERNAME;
import static com.jccode.mycorrespondence.utility.Util.USERS;
import static com.jccode.mycorrespondence.utility.Util.USER_NAME;
import static com.jccode.mycorrespondence.utility.Util.USER_TOKEN;

public class ChatUsScreen extends AppCompatActivity {

    private static final String TAG = "ChatUsScreen";
    String _ID, Title;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    List<ChatModel> homeList;
    private RecyclerRefreshLayout swipe_refresh;
    ChatAdapter adapter;
    FirebaseFirestore firestore;
    TimeAgo timeAgo;
    EditText replyEditText;
    SharedPreferences pref;
    FirebaseUser user;
    Util util;
    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_us_screen);

        Toolbar toolbar = (Toolbar) findViewById(R.id.chat_toolbar);

        user = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        timeAgo = new TimeAgo();
        setToolbar(toolbar, "Chat with us");

        /*if (getIntent().hasExtra(USER_NAME))
            userName = getIntent().getStringExtra(USER_NAME);*/
        util = new Util();

        findViewById();

        hideKeyBoard();

        loadAllReplyData();


        swipe_refresh.setRefreshStyle(RecyclerRefreshLayout.RefreshStyle.NORMAL);
        swipe_refresh.setOnRefreshListener(new RecyclerRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe_refresh.setNestedScrollingEnabled(true);
                loadAllReplyData();
            }
        });


    }


    private void loadAllReplyData() {
        homeList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new ChatAdapter(homeList, this);
        recyclerView.setAdapter(adapter);
        loadData(adapter);

    }

    private void loadData(final ChatAdapter adapter) {
        firestore.collection(CHAT)
                .whereEqualTo(UID, user.getUid())
                .orderBy(TIMESTAMP, Query.Direction.ASCENDING)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        progressBar.setVisibility(View.GONE);
                        swipe_refresh.setRefreshing(false);
                        if (e == null) {
                            if (queryDocumentSnapshots != null) {
                                if (queryDocumentSnapshots.size() > 0) {
                                    homeList.clear();
                                    List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                                    for (DocumentSnapshot chatData : snapshots) {
                                        String from = chatData.getString(FROM);
                                        String msg = chatData.getString(MESSAGE);
                                        long timestamp = chatData.getLong(TIMESTAMP);
                                        homeList.add(new ChatModel(from, msg, timestamp));
                                    }
                                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                                    adapter.notifyDataSetChanged();


                                } else {
                                    Toast.makeText(ChatUsScreen.this, "No reply found", Toast.LENGTH_SHORT).show();
                                }

                            }

                        } else {
                            Log.d(TAG, "onEvent: " + e.getLocalizedMessage());
                            Toast.makeText(ChatUsScreen.this, "load again: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

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
        onBackPressed();
        finish();
        return true;
    }

    private void findViewById() {
        swipe_refresh = (RecyclerRefreshLayout) findViewById(R.id.chat_refresh_layout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.chat_rec);
        replyEditText = (EditText) findViewById(R.id.editText11);
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {
        List<ChatModel> list;
        Context context;

        public ChatAdapter(List<ChatModel> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @NonNull
        @Override
        public ChatAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_reply_view, parent, false);
            return new ChatAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatAdapter.MyViewHolder holder, final int position) {
            String reply = list.get(position).getMessage();
            String from = list.get(position).getFrom();
            long timeStamp = list.get(position).getTimestamp();

            holder.reply.setText(reply);
            holder.name.setText("Admin");
            holder.timeStamp.setText(timeAgo.getlongtoago(timeStamp));

            if (from != null && from.equalsIgnoreCase("Admin"))
                holder.name.setVisibility(View.VISIBLE);
            else
                holder.name.setVisibility(View.GONE);

            holder.mAttalist.clear();


        }


        @Override
        public int getItemCount() {
            return list.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView reply, name, timeStamp, attachmentCounter, Image_attachment_counter;
            private RelativeLayout layout;
            RecyclerView recyclerView;
            List<String> mAttalist = new ArrayList<>();
            AttachmentAdapter attachmentAdapter;


            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                reply = (TextView) itemView.findViewById(R.id.textView3);
                name = (TextView) itemView.findViewById(R.id.name2);
                timeStamp = (TextView) itemView.findViewById(R.id.textView4);
                attachmentCounter = (TextView) itemView.findViewById(R.id.attachmet_counter);
                Image_attachment_counter = (TextView) itemView.findViewById(R.id.image_attachment_counter);
                layout = (RelativeLayout) itemView.findViewById(R.id.home_view_lay);


            }
        }

    }

    public void sendMessage(View view) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE); // 0 - for private mode
        final String reply_text = replyEditText.getText().toString();
        final boolean isAdmin = pref.getBoolean(IS_ADMIN, false);
        String from;
        if (isAdmin)
            from = "Admin";
        else
            from = "user";

     /*if (userName == null && userName.isEmpty())
            userName = user.getPhoneNumber();*/

        final String userName;
        if (util.getUserName(this).isEmpty()) {
            userName = user.getPhoneNumber();
        } else
            userName = util.getUserName(this);
        homeList.add(new ChatModel(from, reply_text, System.currentTimeMillis()));
        adapter.notifyDataSetChanged();

        firestore.collection("Chat_us").document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put(UID, user.getUid());
                    map.put(IMAGE, util.getUserProfile(ChatUsScreen.this));
                    map.put(LAST_MSG, "");
                    map.put(NAME, userName);
                    map.put(TIMESTAMP, 0);
                    map.put(IS_SEEN, false);
                    firestore.collection("Chat_us").document(user.getUid()).set(map);
                }
            }
        });


        pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (pref.contains(F_NAME)) {

            replyEditText.setText("");
            if (!reply_text.isEmpty()) {
                final Map<String, Object> map = new HashMap<>();
                String userToken = FirebaseInstanceId.getInstance().getToken();
                map.put(USER_TOKEN, userToken);
                map.put(MESSAGE, reply_text);
                map.put(USERNAME, userName);
                map.put(IMAGE, util.getUserProfile(this));
                map.put(UID, user.getUid());
                map.put(FROM, from);
                map.put(TIMESTAMP, System.currentTimeMillis());
                firestore.collection(CHAT)
                        .add(map)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Map<String, Object> update_map = new HashMap<>();
                                update_map.put(LAST_MSG, reply_text);
                                update_map.put(IS_SEEN, false);
                                update_map.put(UID, user.getUid());
                                update_map.put(TIMESTAMP, System.currentTimeMillis());
                                replyEditText.setText("");
                                adapter.notifyDataSetChanged();
                                firestore.collection("Chat_us").document(user.getUid()).update(update_map);
                            }
                        });


            } else {

                Snackbar.make(view, "Please Fill All the fields", Snackbar.LENGTH_SHORT).show();
            }

        } else {
            updateUserNameDialog();
        }
    }

    private void updateUserNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatUsScreen.this);

        // get the layout inflater
        LayoutInflater inflater = ChatUsScreen.this.getLayoutInflater();

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
                            Toast.makeText(ChatUsScreen.this, "Fill all the fields", Toast.LENGTH_SHORT).show();
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
        final ProgressDialog progressDialog = new ProgressDialog(ChatUsScreen.this);
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
                        Toast.makeText(ChatUsScreen.this, "Username updated successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ChatUsScreen.this, "cant update username, try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideKeyBoard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
