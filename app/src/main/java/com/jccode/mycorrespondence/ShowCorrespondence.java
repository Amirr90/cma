package com.jccode.mycorrespondence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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

import static com.jccode.mycorrespondence.utility.Util.*;


public class ShowCorrespondence extends AppCompatActivity {

    private static final String TAG = "ShowCorrespondence";

    RecyclerView recyclerView;
    String userId;
    FirebaseUser user;
    List<DocumentSnapshot> homeList = new ArrayList<>();
    HomeAdapter adapter;
    TimeAgo timeAgo;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    ProgressDialog progressBar;
    private RecyclerRefreshLayout swipe_refresh;
    String SortType;
    TextView result;
    private int SHARE_CORRESPONDENCE_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_correspondence);


        Toolbar toolbar = (Toolbar) findViewById(R.id.show_correspondence_toolbar);

        SortType = getIntent().getStringExtra(SORT_TYPE);
        setToolbar(toolbar, SortType);


        result = (TextView) findViewById(R.id.textView34);

        swipe_refresh = (RecyclerRefreshLayout) findViewById(R.id.refresh_layout);
        recyclerView = (RecyclerView) findViewById(R.id.home_rec);

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

    private void loadHomeScreenData() {
        timeAgo = new TimeAgo();
        homeList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new HomeAdapter(homeList, this);
        recyclerView.setAdapter(adapter);
        loadData(adapter);

    }

    private void loadData(final HomeAdapter adapter) {

        homeList.clear();
        switch (SortType) {
            case "All Correspondence": {
                firestore.collection(CORRESPONDENCE_QUERY)
                        .whereEqualTo(UID, FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                        .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                progressBar.dismiss();
                                if (e != null && queryDocumentSnapshots.isEmpty()) {
                                    Toast.makeText(ShowCorrespondence.this, "no correspondence found", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                homeList.addAll(queryDocumentSnapshots.getDocuments());
                                adapter.notifyDataSetChanged();
                                String Tag;
                                if (homeList.size() == 1)
                                    Tag = " result found";
                                else
                                    Tag = " results found";
                                result.setText(homeList.size() + Tag);
                                swipe_refresh.setRefreshing(false);
                            }
                        });
            }
            break;
            case "Active Correspondence": {
                firestore.collection(CORRESPONDENCE_QUERY)
                        .whereEqualTo(UID, FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .whereEqualTo(IS_ACTIVE, true)
                        .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                        .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                progressBar.dismiss();
                                if (e != null && queryDocumentSnapshots.isEmpty()) {
                                    Toast.makeText(ShowCorrespondence.this, "no correspondence found", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                homeList.addAll(queryDocumentSnapshots.getDocuments());
                                adapter.notifyDataSetChanged();
                                String Tag;
                                if (homeList.size() == 1)
                                    Tag = " result found";
                                else
                                    Tag = " results found";
                                result.setText(homeList.size() + Tag);
                                swipe_refresh.setRefreshing(false);
                            }
                        });
            }
            break;
            case "Close Correspondence": {
                firestore.collection(CORRESPONDENCE_QUERY)
                        .whereEqualTo(UID, FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .whereEqualTo(IS_ACTIVE, false)
                        .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                        .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                progressBar.dismiss();
                                if (e != null && queryDocumentSnapshots.isEmpty()) {
                                    Toast.makeText(ShowCorrespondence.this, "no correspondence found", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                homeList.addAll(queryDocumentSnapshots.getDocuments());
                                adapter.notifyDataSetChanged();
                                String Tag;
                                if (homeList.size() == 1)
                                    Tag = " result found";
                                else
                                    Tag = " results found";
                                result.setText(homeList.size() + Tag);
                                swipe_refresh.setRefreshing(false);
                            }
                        });
            }
            break;
            case "Un-Replied Correspondence": {
                loadData(UN_REPLIED);
            }
            break;
            case "Partial Replied":
                loadData(PARTIALLY_REPLIED);
                break;
            case "Replied Correspondence":
                loadData(REPLIED);
                break;
            case "File Correspondence":
                loadData(FILED);
                break;
        }
    }

    private void loadData(final String tag) {
        progressBar.show();
        homeList.clear();
        firestore.collection(CORRESPONDENCE_QUERY)
                .whereEqualTo(UID, FirebaseAuth.getInstance().getCurrentUser().getUid())
                //.orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(ShowCorrespondence.this, "please try again", Toast.LENGTH_SHORT).show();
                        } else {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                swipe_refresh.setRefreshing(false);
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
                                                        String Tag;
                                                        if (homeList.size() == 1)
                                                            Tag = " result found";
                                                        else
                                                            Tag = " results found";
                                                        result.setText(homeList.size() + Tag);
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
                            progressBar.dismiss();
                        }
                    }
                });

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
                            .putExtra(LETTER_NUMBER, letterNumber)
                            .putExtra(ID, id));


                }
            });


            holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    CharSequence[] items = {SHARE_CORRESPONDENCE, DELETE_CORRESPONDENCE};
                    final String doc_id = list.get(position).getId();
                    showDeleteDialog(doc_id);
                  /*  new AlertDialog.Builder(context)
                            .setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case 0:
                                            Intent intent = new Intent(context, ShareCorrespondenceActivity.class);
                                            intent.putExtra(CORRESPONDENCE_ID, doc_id);
                                            intent.putExtra(FROM, "Correspondence");
                                            startActivityForResult(intent, SHARE_CORRESPONDENCE_CODE);
                                            break;
                                        case 1:

                                            break;
                                    }
                                }
                            })
                            .show();*/
                    return false;
                }
            });

        }

        private void showDeleteDialog(final String doc_id) {
            new AlertDialog.Builder(ShowCorrespondence.this)
                    .setTitle("Delete Correspondence")
                    .setMessage("Want to delete this correspondence??")
                    .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                            final ProgressDialog progressDialog = new ProgressDialog(ShowCorrespondence.this);
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
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHARE_CORRESPONDENCE_CODE) {
            if (resultCode == RESULT_OK)
                Toast.makeText(this, "Shared", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "failed to share", Toast.LENGTH_SHORT).show();
        }
    }
}