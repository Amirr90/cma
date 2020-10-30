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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jccode.mycorrespondence.utility.TimeAgo;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_ID;
import static com.jccode.mycorrespondence.utility.Util.DELETE_DOCUMENTS;
import static com.jccode.mycorrespondence.utility.Util.DESCRIPTION;
import static com.jccode.mycorrespondence.utility.Util.DOCUMENT_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DOC_ID;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE_TO_SHARER;
import static com.jccode.mycorrespondence.utility.Util.SHARED;
import static com.jccode.mycorrespondence.utility.Util.SHARED_DOCUMENT_QUERY;
import static com.jccode.mycorrespondence.utility.Util.SHARE_DOCUMENTS;
import static com.jccode.mycorrespondence.utility.Util.SHARE_TO;
import static com.jccode.mycorrespondence.utility.Util.SHARE_TO_USER_NAME;
import static com.jccode.mycorrespondence.utility.Util.SORT_TYPE;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TITLE;
import static com.jccode.mycorrespondence.utility.Util.TYPE;
import static com.jccode.mycorrespondence.utility.Util.UID;
import static com.jccode.mycorrespondence.utility.Util.USER_NAME;

public class ViewListSharedDocuments extends AppCompatActivity {

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
    String type, title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list_shared_documents);
        Toolbar toolbar = (Toolbar) findViewById(R.id.shared_view_document_toolbar);

        SortType = getIntent().getStringExtra(SORT_TYPE);
        type = getIntent().getStringExtra(TYPE);
        if (type.equalsIgnoreCase(SHARED)){
            setToolbar(toolbar, "Shared Documents");
        }
        else {
            setToolbar(toolbar, "Received Documents");
        }



        result = (TextView) findViewById(R.id.shared_view_document_textView34);

        swipe_refresh = (RecyclerRefreshLayout) findViewById(R.id.shared_view_document_refresh_layout);
        recyclerView = (RecyclerView) findViewById(R.id.shared_view_document_rec);

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
            return new HomeAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeAdapter.MyViewHolder holder, final int position) {


            DocumentSnapshot snapshot = list.get(position);
            String subject = snapshot.getString(SUBJECT);
            String shareToUserName = snapshot.getString(SHARE_TO_USER_NAME);
            String userName = snapshot.getString("username");
            String description = snapshot.getString(DESCRIPTION);
            long timeStamp = snapshot.getLong(TIMESTAMP);

            holder.timeStamp.setText(timeAgo.getlongtoago(timeStamp));

            holder.mSubject.setText(subject);
            holder.mDescription.setText(description);
            holder.mActiveStatus.setText("Received from: " + userName);

            if (type.equalsIgnoreCase(SHARED)) {
                holder.mActiveStatus.setText("Shared To: " + shareToUserName);
            } else {
                holder.mActiveStatus.setText("Received from: " + userName);
            }

            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String id = list.get(position).getId();
                    String subject = list.get(position).getString(SUBJECT);
                    String description = list.get(position).getString(DESCRIPTION);
                    String timestamp = "" + list.get(position).getLong(TIMESTAMP);
                    startActivityForResult(new Intent(context, SharedDocumentDetail.class)
                            .putExtra(DESCRIPTION, description)
                            .putExtra(TIMESTAMP, timestamp)
                            .putExtra(SUBJECT, subject)
                            .putExtra(DOC_ID, id), REQ_CODE_REFRESH);


                }
            });


            holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String docId = list.get(position).getId();
                    showDeleteDialog(docId, position);
                    return false;
                }
            });

        }

        private void showDeleteDialog(final String doc_id, final int position) {
            new AlertDialog.Builder(ViewListSharedDocuments.this)
                    .setTitle("Delete Document")
                    .setMessage("Want to delete this Document??")
                    .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                            final ProgressDialog progressDialog = new ProgressDialog(ViewListSharedDocuments.this);
                            progressDialog.setMessage("Deleting....");
                            progressDialog.show();
                            firestore.collection(SHARED_DOCUMENT_QUERY).document(doc_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    list.remove(position);
                                    adapter.notifyDataSetChanged();
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

    private void loadData(final String userId) {

        Query ref;
        if (!type.equalsIgnoreCase("SHARED")) {
            ref = firestore.collection(SHARED_DOCUMENT_QUERY)
                    .whereEqualTo(SHARE_TO, userId)
                    .whereEqualTo(IS_ACTIVE_TO_SHARER, true)
                    .orderBy(TIMESTAMP, Query.Direction.DESCENDING);
        } else {
            ref = firestore.collection(SHARED_DOCUMENT_QUERY)
                    .whereEqualTo(UID, userId)
                    .whereEqualTo(IS_ACTIVE, true)
                    .orderBy(TIMESTAMP, Query.Direction.DESCENDING);
        }
        ref.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                progressBar.dismiss();
                swipe_refresh.setRefreshing(false);

                if (e != null) {
                    Toast.makeText(ViewListSharedDocuments.this, "please try again " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Shared", Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, "failed to share", Toast.LENGTH_SHORT).show();
        }
    }

}