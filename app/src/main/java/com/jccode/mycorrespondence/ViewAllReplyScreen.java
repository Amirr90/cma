package com.jccode.mycorrespondence;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jccode.mycorrespondence.Adapter.AttachmentAdapter;
import com.jccode.mycorrespondence.utility.TimeAgo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_QUERY;
import static com.jccode.mycorrespondence.utility.Util.F_NAME;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.IMAGE;
import static com.jccode.mycorrespondence.utility.Util.IMAGE_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.L_NAME;
import static com.jccode.mycorrespondence.utility.Util.MyPREFERENCES;
import static com.jccode.mycorrespondence.utility.Util.PDF_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.REPLY;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TITLE;
import static com.jccode.mycorrespondence.utility.Util.UID;
import static com.jccode.mycorrespondence.utility.Util.USERNAME;
import static com.jccode.mycorrespondence.utility.Util.USERS;

public class ViewAllReplyScreen extends AppCompatActivity {
    private static final String TAG = "ViewAllReplyScreen";
    String _ID, Title;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    List<DocumentSnapshot> homeList;
    private RecyclerRefreshLayout swipe_refresh;
    AllReplyAdapter adapter;
    FirebaseFirestore firestore;
    TimeAgo timeAgo;
    EditText replyEditText;
    SharedPreferences pref;
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_reply_screen);


        Toolbar toolbar = (Toolbar) findViewById(R.id.viewAllReply_toolbar);

        user = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        timeAgo = new TimeAgo();

        findViewById();
        if (getIntent().hasExtra(ID)) {
            _ID = getIntent().getStringExtra(ID);
            Title = getIntent().getStringExtra(TITLE);
            setToolbar(toolbar, Title);
            loadAllReplyData(_ID);

            swipe_refresh.setRefreshStyle(RecyclerRefreshLayout.RefreshStyle.NORMAL);
            swipe_refresh.setOnRefreshListener(new RecyclerRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipe_refresh.setNestedScrollingEnabled(true);
                    loadData(adapter, _ID);
                }
            });


            hideKeyBoard();
        }

    }

    private void findViewById() {
        swipe_refresh = (RecyclerRefreshLayout) findViewById(R.id.all_reply_refresh_layout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.view_Reply_rec);
        replyEditText = (EditText) findViewById(R.id.editText11);
    }

    private void setToolbar(Toolbar toolbar, String id) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(id);
    }


    private void loadAllReplyData(String _ID) {
        homeList = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new AllReplyAdapter(homeList, this);
        recyclerView.setAdapter(adapter);
        loadData(adapter, _ID);

    }

    private void loadData(final AllReplyAdapter adapter, String id) {
        firestore.collection(CORRESPONDENCE_QUERY).document(id).collection(REPLY).orderBy(TIMESTAMP, Query.Direction.ASCENDING)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        progressBar.setVisibility(View.GONE);
                        swipe_refresh.setRefreshing(false);
                        if (e == null) {
                            if (queryDocumentSnapshots != null) {
                                if (queryDocumentSnapshots.size() > 0) {
                                    homeList.clear();
                                    homeList.addAll(queryDocumentSnapshots.getDocuments());
                                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
                                    adapter.notifyDataSetChanged();


                                } else {
                                    Toast.makeText(ViewAllReplyScreen.this, "No reply found", Toast.LENGTH_SHORT).show();
                                }

                            }

                        } else {
                            Log.d(TAG, "onEvent: " + e.getLocalizedMessage());
                            Toast.makeText(ViewAllReplyScreen.this, "load again: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        finish();
        return true;
    }

    private class AllReplyAdapter extends RecyclerView.Adapter<AllReplyAdapter.MyViewHolder> {
        List<DocumentSnapshot> list;
        Context context;

        public AllReplyAdapter(List<DocumentSnapshot> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @NonNull
        @Override
        public AllReplyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_reply_view, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AllReplyAdapter.MyViewHolder holder, final int position) {
            String reply = list.get(position).getString(REPLY);
            String u_name = list.get(position).getString(USERNAME);
            long timeStamp = list.get(position).getLong(TIMESTAMP);

            holder.reply.setText(reply);
            holder.name.setText(u_name);
            holder.timeStamp.setText(timeAgo.getlongtoago(timeStamp));

            holder.mAttalist.clear();

            if ((List<Map>) list.get(position).get(IMAGE_ATTACHMENT) != null) {
                List<Map> images_map = (List<Map>) list.get(position).get(IMAGE_ATTACHMENT);
                if (images_map != null && images_map.size() > 0) {
                    holder.Image_attachment_counter.setVisibility(View.VISIBLE);
                    holder.Image_attachment_counter.setText("+" + images_map.size() + " image attachments");
                } else {
                    holder.Image_attachment_counter.setVisibility(View.GONE);
                }
            } else {
                holder.Image_attachment_counter.setVisibility(View.GONE);
            }


            if ((List<Map>) list.get(position).get(PDF_ATTACHMENT) != null) {
                List<Map> pdfs = (List<Map>) list.get(position).get(PDF_ATTACHMENT);
                if (pdfs != null && pdfs.size() > 0) {
                    holder.attachmentCounter.setVisibility(View.VISIBLE);
                    holder.attachmentCounter.setText("+" + pdfs.size() + " other attachments");
                } else {
                    holder.attachmentCounter.setVisibility(View.GONE);
                }
            } else {
                holder.attachmentCounter.setVisibility(View.GONE);
            }


            holder.attachmentCounter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<Map> pdfs = (List<Map>) list.get(position).get(PDF_ATTACHMENT);
                    ArrayList<String> list = new ArrayList<String>();
                    for (int a = 0; a < pdfs.size(); a++) {
                        String pdf_path = (String) pdfs.get(a).get(PDF_ATTACHMENT);
                        list.add(pdf_path);
                    }

                    context.startActivity(new Intent(context, PdfScreen.class).putExtra("TYPE", "PDF")
                            .putExtra("pdfs", list));


                }
            });

            holder.Image_attachment_counter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<Map> pdfs = (List<Map>) list.get(position).get(IMAGE_ATTACHMENT);

                    ArrayList<String> list = new ArrayList<String>();
                    for (int a = 0; a < pdfs.size(); a++) {
                        String pdf_path = (String) pdfs.get(a).get(IMAGE_ATTACHMENT);
                        list.add(pdf_path);
                    }

                    context.startActivity(new Intent(context, PdfScreen.class).putExtra("TYPE", IMAGE)
                            .putExtra("images", list));
                }
            });

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

    public void sendReply(View view) {


        pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (pref.contains(F_NAME)) {
            final String reply_text = replyEditText.getText().toString();
            replyEditText.setText("");
            if (!reply_text.isEmpty()) {
                firestore.collection(USERS).document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        String uName = snapshot.getString(F_NAME) + " " + snapshot.getString(L_NAME);
                        Map<String, Object> map = new HashMap<>();
                        map.put(REPLY, reply_text);
                        map.put(TIMESTAMP, System.currentTimeMillis());
                        map.put(USERNAME, uName);
                        map.put(UID, FirebaseAuth.getInstance().getCurrentUser().getUid());
                        progressBar.setVisibility(View.VISIBLE);
                        firestore.collection(CORRESPONDENCE_QUERY).document(_ID)
                                .collection(REPLY)
                                .add(map)
                                .addOnSuccessListener(ViewAllReplyScreen.this, new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        progressBar.setVisibility(View.GONE);
                                        adapter.notifyDataSetChanged();

                                    }
                                }).addOnFailureListener(ViewAllReplyScreen.this, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ViewAllReplyScreen.this, "could't send", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });


            } else {
                replyEditText.setError("required");
            }

        } else {
            updateUserNameDialog();
        }
    }

    private void updateUserNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewAllReplyScreen.this);

        // get the layout inflater
        LayoutInflater inflater = ViewAllReplyScreen.this.getLayoutInflater();

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
                            Toast.makeText(ViewAllReplyScreen.this, "Fill all the fields", Toast.LENGTH_SHORT).show();
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
        final ProgressDialog progressDialog = new ProgressDialog(ViewAllReplyScreen.this);
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
                        Toast.makeText(ViewAllReplyScreen.this, "Username updated successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ViewAllReplyScreen.this, "cant update username, try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideKeyBoard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

}
