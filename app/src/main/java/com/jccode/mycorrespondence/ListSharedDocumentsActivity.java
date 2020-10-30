package com.jccode.mycorrespondence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.jccode.mycorrespondence.utility.TimeAgo;
import com.jccode.mycorrespondence.utility.Util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_ID;
import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DATE;
import static com.jccode.mycorrespondence.utility.Util.DELETE_DOCUMENTS;
import static com.jccode.mycorrespondence.utility.Util.DEPARTMENT;
import static com.jccode.mycorrespondence.utility.Util.DESCRIPTION;
import static com.jccode.mycorrespondence.utility.Util.FILED;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.IMAGE;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE_TO_SHARER;
import static com.jccode.mycorrespondence.utility.Util.LETTER_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.PARTIALLY_REPLIED;
import static com.jccode.mycorrespondence.utility.Util.REMINDER;
import static com.jccode.mycorrespondence.utility.Util.REPLIED;
import static com.jccode.mycorrespondence.utility.Util.SHARED;
import static com.jccode.mycorrespondence.utility.Util.SHARED_CORRESPONDENCE;
import static com.jccode.mycorrespondence.utility.Util.SHARED_DOCUMENT;
import static com.jccode.mycorrespondence.utility.Util.SHARED_DOCUMENT_QUERY;
import static com.jccode.mycorrespondence.utility.Util.SHARE_DOCUMENTS;
import static com.jccode.mycorrespondence.utility.Util.SHARE_TO;
import static com.jccode.mycorrespondence.utility.Util.SHARE_TO_USER_IMAGE;
import static com.jccode.mycorrespondence.utility.Util.SHARE_TO_USER_NAME;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TITLE;
import static com.jccode.mycorrespondence.utility.Util.TYPE;
import static com.jccode.mycorrespondence.utility.Util.UID;
import static com.jccode.mycorrespondence.utility.Util.UN_REPLIED;

public class ListSharedDocumentsActivity extends AppCompatActivity {

    private static final String TAG = "ListSharedDocumentsActivity";
    RecyclerView recyclerView;
    String title;
    private RecyclerRefreshLayout swipe_refresh;
    TextView result;
    ProgressDialog progressBar;
    FirebaseUser user;
    HomeAdapter adapter;
    TimeAgo timeAgo;
    List<DocumentSnapshot> homeList = new ArrayList<>();

    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    String from, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_shared_documents);

        Toolbar toolbar = (Toolbar) findViewById(R.id.share_correspondence_cardView2);
        type = getIntent().getStringExtra(TYPE);
        if (type.equalsIgnoreCase(SHARED)) {
            setToolbar(toolbar, "Shared Correspondence");
        } else {
            setToolbar(toolbar, "Received Correspondence");
        }


        result = (TextView) findViewById(R.id.textView342);

        from = getIntent().getStringExtra(FROM);

        swipe_refresh = (RecyclerRefreshLayout) findViewById(R.id.refresh_layout2);
        recyclerView = (RecyclerView) findViewById(R.id.home_rec2);

        progressBar = new ProgressDialog(this);
        progressBar.setMessage("Loading, please wait....");
        progressBar.show();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;


        loadHomeScreenData();

        swipe_refresh.setRefreshStyle(RecyclerRefreshLayout.RefreshStyle.NORMAL);
        swipe_refresh.setOnRefreshListener(new RecyclerRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe_refresh.setNestedScrollingEnabled(true);
                loadData(adapter);
            }
        });

    }

    private void loadData(final HomeAdapter adapter) {

        Query ref;
        if (!type.equalsIgnoreCase("SHARED")) {
            ref = firestore.collection("Users")
                    .document(user.getUid())
                    .collection(SHARED_DOCUMENT_QUERY)
                    .whereEqualTo(SHARE_TO, user.getUid())
                    .whereEqualTo(IS_ACTIVE_TO_SHARER, true)
                    .orderBy(TIMESTAMP, Query.Direction.DESCENDING);
        } else {
            ref = firestore.collection("Users")
                    .document(user.getUid())
                    .collection(SHARED_DOCUMENT_QUERY)
                    .whereEqualTo(IS_ACTIVE, true)
                    .orderBy(TIMESTAMP, Query.Direction.DESCENDING);
        }

        homeList.clear();
        ref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                progressBar.dismiss();
                swipe_refresh.setRefreshing(false);
                homeList.addAll(queryDocumentSnapshots.getDocuments());
                adapter.notifyDataSetChanged();
                String res;
                if (homeList.size() == 1)
                    res = homeList.size() + " result found";
                else
                    res = homeList.size() + " results found";
                result.setText(res);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.dismiss();
                Log.d(TAG, e.getLocalizedMessage());
                Toast.makeText(ListSharedDocumentsActivity.this, "failed " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadHomeScreenData() {
        timeAgo = new TimeAgo();
        homeList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new HomeAdapter(homeList, this);
        recyclerView.setAdapter(adapter);
        loadData(adapter);

    }

    private void setToolbar(Toolbar toolbar, String id) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(id);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();

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
        public HomeAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_view, parent, false);
            return new HomeAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeAdapter.MyViewHolder holder, final int position) {


            final DocumentSnapshot snapshot = list.get(position);
            String subject = snapshot.getString(SUBJECT);
            String userName = snapshot.getString("username");
            String shareToUserName = snapshot.getString(SHARE_TO_USER_NAME);
            String description = snapshot.getString(DESCRIPTION);
            long timeStamp = snapshot.getLong(TIMESTAMP);

            holder.timeStamp.setText(timeAgo.getlongtoago(timeStamp));

            holder.mSubject.setText(subject);
            holder.mDescription.setText(description);

            if (type.equalsIgnoreCase(SHARED)) {
                holder.mActiveStatus.setText("Share To: " + shareToUserName);
            } else {
                holder.mActiveStatus.setText("Received from: " + userName);
            }

            /*if (list.get(position).getBoolean(IS_ACTIVE)) {
                holder.mActiveStatus.setText("Open");
                holder.mActiveStatus.setTextColor(Color.GREEN);
            } else {
                holder.mActiveStatus.setText("Close");
                holder.mActiveStatus.setTextColor(Color.RED);
            }*/

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
                    String letterNumber = list.get(position).getString(LETTER_NUMBER);
                    String timestamp = "" + list.get(position).getLong(TIMESTAMP);
                    String date = list.get(position).getString(DATE);
                    context.startActivity(new Intent(context, ViewSharedCorrespondingDetail.class)
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
                            .putExtra(LETTER_NUMBER, letterNumber)
                            .putExtra(ID, id));


                }
            });


            holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    final String selected_corrspondence_id = snapshot.getId();
                    new AlertDialog.Builder(context).setTitle("Delete Correspondence")
                            .setMessage("Do you really want to delete this correspondences")
                            .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    progressBar.setMessage("Deleting,please wait...");
                                    progressBar.setCancelable(false);
                                    progressBar.show();
                                    final DocumentReference ref = firestore.collection("Users").document(user.getUid())
                                            .collection(SHARED_DOCUMENT_QUERY).document(selected_corrspondence_id);

                                    ref.update(IS_ACTIVE, false).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            list.remove(position);
                                            adapter.notifyDataSetChanged();
                                            progressBar.dismiss();
                                            Snackbar.make(v, "Deleted Successfully", Snackbar.LENGTH_SHORT).setAction("Re-store", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    ref.update(IS_ACTIVE, true);
                                                    Toast.makeText(ListSharedDocumentsActivity.this, "Re-stored", Toast.LENGTH_SHORT).show();
                                                    list.add(position, snapshot);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }).show();
                                        }
                                    });
                                }
                            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                    return true;
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

}