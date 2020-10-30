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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dinuscxj.refresh.RecyclerRefreshLayout;
import com.google.android.gms.tasks.OnSuccessListener;
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

import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_ID;
import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DATE;
import static com.jccode.mycorrespondence.utility.Util.DELETE_DOCUMENTS;
import static com.jccode.mycorrespondence.utility.Util.DEPARTMENT;
import static com.jccode.mycorrespondence.utility.Util.DESCRIPTION;
import static com.jccode.mycorrespondence.utility.Util.DOCUMENT_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DOC_ID;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE;
import static com.jccode.mycorrespondence.utility.Util.LETTER_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.REMINDER;
import static com.jccode.mycorrespondence.utility.Util.SHARE_DOCUMENTS;
import static com.jccode.mycorrespondence.utility.Util.SHARE_LIVE_DOCUMENTS;
import static com.jccode.mycorrespondence.utility.Util.SORT_TYPE;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TITLE;
import static com.jccode.mycorrespondence.utility.Util.TYPE;
import static com.jccode.mycorrespondence.utility.Util.UID;

public class ViewAllDocumentActivity extends AppCompatActivity {
    private static final String TAG = "ViewDocumentActivity";
    private static final int REQ_CODE_REFRESH = 001;
    RecyclerView recyclerView;
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
        setContentView(R.layout.activity_view_document);

        Toolbar toolbar = (Toolbar) findViewById(R.id.view_document_toolbar);

        SortType = getIntent().getStringExtra(SORT_TYPE);
        setToolbar(toolbar, "View All Document");


        result = (TextView) findViewById(R.id.view_document_textView34);

        swipe_refresh = (RecyclerRefreshLayout) findViewById(R.id.view_document_refresh_layout);
        recyclerView = (RecyclerView) findViewById(R.id.view_document_rec);

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
                loadData(user.getUid());
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
        loadData(user.getUid());

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
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeAdapter.MyViewHolder holder, final int position) {


            String subject = list.get(position).getString(SUBJECT);
            String description = list.get(position).getString(DESCRIPTION);
            long timeStamp = list.get(position).getLong(TIMESTAMP);

            holder.timeStamp.setText(timeAgo.getlongtoago(timeStamp));

            holder.mSubject.setText(subject);
            holder.mDescription.setText(description);


            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String id = list.get(position).getId();
                    String subject = list.get(position).getString(SUBJECT);
                    String description = list.get(position).getString(DESCRIPTION);
                    String timestamp = "" + list.get(position).getLong(TIMESTAMP);
                    startActivityForResult(new Intent(context, DocumentDetail.class)
                            .putExtra(DESCRIPTION, description)
                            .putExtra(TIMESTAMP, timestamp)
                            .putExtra(SUBJECT, subject)
                            .putExtra(FROM, "ViewAllDocumentActivity")
                            .putExtra(DOC_ID, id), REQ_CODE_REFRESH);


                }
            });


            holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    final String docId = list.get(position).getId();

                    CharSequence[] items = {SHARE_DOCUMENTS, SHARE_LIVE_DOCUMENTS, DELETE_DOCUMENTS};
                    final String doc_id = list.get(position).getId();
                    new AlertDialog.Builder(context)
                            .setItems(items, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    switch (i) {
                                        case 0: {
                                            Intent intent = new Intent(context, ShareCorrespondenceActivity.class);
                                            intent.putExtra(CORRESPONDENCE_ID, doc_id);
                                            intent.putExtra(FROM, "Document");
                                            startActivityForResult(intent, SHARE_CORRESPONDENCE_CODE);
                                        }
                                        break;
                                        case 2:
                                            showDeleteDialog(docId, position);
                                            break;

                                        case 1: {
                                            Intent intent2 = new Intent(context, ShareCorrespondenceActivity.class);
                                            intent2.putExtra(CORRESPONDENCE_ID, doc_id);
                                            intent2.putExtra(FROM, SHARE_LIVE_DOCUMENTS);
                                            startActivityForResult(intent2, SHARE_CORRESPONDENCE_CODE);
                                        }
                                        break;
                                    }
                                }
                            })
                            .show();
                    return false;
                }
            });

        }

        private void showDeleteDialog(final String doc_id, final int position) {
            new AlertDialog.Builder(ViewAllDocumentActivity.this)
                    .setTitle(getString(R.string.delete_document))
                    .setMessage("Want to delete this Document??")
                    .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                            final ProgressDialog progressDialog = new ProgressDialog(ViewAllDocumentActivity.this);
                            progressDialog.setMessage("Deleting....");
                            progressDialog.show();
                            firestore.collection(DOCUMENT_QUERY).document(doc_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    list.remove(position);
                                    adapter.notifyDataSetChanged();
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
                mActiveStatus.setVisibility(View.GONE);
            }
        }
    }

    private void loadData(final String userId) {

        firestore.collection(DOCUMENT_QUERY)
                .whereEqualTo(UID, userId)
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        progressBar.dismiss();
                        swipe_refresh.setRefreshing(false);

                        if (e != null) {
                            Toast.makeText(ViewAllDocumentActivity.this, "please try again " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Error", e);
                        } else {
                            homeList.clear();
                            homeList.addAll(queryDocumentSnapshots.getDocuments());
                            adapter.notifyDataSetChanged();
                            String res;
                            if (homeList.size() == 0)
                                res = homeList.size() + " result found";
                            else
                                res = homeList.size() + " results found";

                            result.setText(res);

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_REFRESH) {
            if (resultCode == RESULT_OK) {

                loadData(user.getUid());
            }
        }

        if (requestCode == SHARE_CORRESPONDENCE_CODE) {
            if (resultCode == RESULT_OK) {
                loadData(user.getUid());
                String msg = data.getStringExtra("result");
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            } else {
                String msg = data.getStringExtra("result");
                Util.showDialog(ViewAllDocumentActivity.this, msg, "Sharing Failed");

            }
        }
    }
}