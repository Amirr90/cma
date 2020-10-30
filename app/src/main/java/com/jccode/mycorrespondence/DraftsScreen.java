package com.jccode.mycorrespondence;

import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dinuscxj.refresh.RecyclerRefreshLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import static com.jccode.mycorrespondence.utility.Util.DATE;
import static com.jccode.mycorrespondence.utility.Util.DEPARTMENT;
import static com.jccode.mycorrespondence.utility.Util.DESCRIPTION;
import static com.jccode.mycorrespondence.utility.Util.DRAFTS;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.IS_ACTIVE;
import static com.jccode.mycorrespondence.utility.Util.LETTER_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.REMARK;
import static com.jccode.mycorrespondence.utility.Util.SENDER_DETAIL;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TYPE;
import static com.jccode.mycorrespondence.utility.Util.USERS;


public class DraftsScreen extends AppCompatActivity {

    private static final String TAG = "DraftsScreen";
    FirebaseUser user;
    FirebaseFirestore firestore;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    List<DocumentSnapshot> draftList;
    RecyclerRefreshLayout swipe_refresh;
    DraftAdapter adapter;
    TimeAgo timeAgo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drafts_screen);

        swipe_refresh = (RecyclerRefreshLayout) findViewById(R.id.draft_refresh_layout);
        progressBar = (ProgressBar) findViewById(R.id.draft_progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.draft_rec);

        Toolbar toolbar = (Toolbar) findViewById(R.id.draft_toolbar);

        timeAgo = new TimeAgo();
        firestore = FirebaseFirestore.getInstance();

        user = FirebaseAuth.getInstance().getCurrentUser();
        setToolbar(toolbar, "Profile");

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
        draftList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new DraftAdapter(draftList, this);
        recyclerView.setAdapter(adapter);
        loadData(adapter);

    }

    private void loadData(final DraftAdapter adapter) {
        firestore.collection(USERS).document(user.getUid())
                .collection(DRAFTS)
                .whereEqualTo(IS_ACTIVE, true)
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        progressBar.setVisibility(View.GONE);
                        if (e == null) {
                            if (queryDocumentSnapshots != null) {
                                draftList.clear();
                                draftList.addAll(queryDocumentSnapshots.getDocuments());
                                adapter.notifyDataSetChanged();
                                swipe_refresh.setRefreshing(false);

                            }

                        } else {
                            Log.d(TAG, "onEvent: " + e.getLocalizedMessage());
                            Toast.makeText(DraftsScreen.this, "load again: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private class DraftAdapter extends RecyclerView.Adapter<DraftAdapter.MyViewHolder> {

        List<DocumentSnapshot> list;
        Context context;

        public DraftAdapter(List<DocumentSnapshot> homeList, Context context) {
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
            // String description = list.get(position).getString(DESCRIPTION);
            long timeStamp = list.get(position).getLong(TIMESTAMP);

            holder.timeStamp.setText(timeAgo.getlongtoago(timeStamp));

            holder.title.setText(subject);
            // holder.description.setText(description);
            holder.subject.setText(description);
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String id = list.get(position).getId();
                    String letter_number = list.get(position).getString(LETTER_NUMBER);
                    String department = list.get(position).getString(DEPARTMENT);
                    String type = list.get(position).getString(TYPE);
                    String remark = list.get(position).getString(REMARK);
                    String subject = list.get(position).getString(SUBJECT);
                    String description = list.get(position).getString(DESCRIPTION);
                    String date = list.get(position).getString(DATE);
                    String timestamp = "" + list.get(position).getLong(TIMESTAMP);
                    String sender_detail = list.get(position).getString(SENDER_DETAIL);
                    context.startActivity(new Intent(context, CreateCorrespondenceScreen.class)
                            .putExtra(LETTER_NUMBER, letter_number)
                            .putExtra(DATE, date)
                            .putExtra(DEPARTMENT, department)
                            .putExtra(TYPE, type)
                            .putExtra(REMARK, remark)
                            .putExtra(SENDER_DETAIL, sender_detail)
                            .putExtra(DESCRIPTION, description)
                            .putExtra(SUBJECT, subject)
                            .putExtra(TIMESTAMP, timestamp)
                            .putExtra(ID, id)
                            .putExtra(FROM, TAG));

                }
            });

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView title, subject, description, timeStamp;
            private CircleImageView mCategoryImage;
            private RelativeLayout layout;


            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.textView);
                subject = (TextView) itemView.findViewById(R.id.textView2);
                description = (TextView) itemView.findViewById(R.id.textView3);
                mCategoryImage = (CircleImageView) itemView.findViewById(R.id.profile_image);
                timeStamp = (TextView) itemView.findViewById(R.id.textView13);
                layout = (RelativeLayout) itemView.findViewById(R.id.home_view_lay);
            }
        }
    }
}
