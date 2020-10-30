package com.jccode.mycorrespondence;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.google.android.gms.tasks.OnFailureListener;
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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jccode.mycorrespondence.utility.Util.AMOUNT;
import static com.jccode.mycorrespondence.utility.Util.DONATIONS;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TRX_STATUS;
import static com.jccode.mycorrespondence.utility.Util.TRX_STATUS_SUCCESS;
import static com.jccode.mycorrespondence.utility.Util.UID;


public class DonateHistoryScreen extends AppCompatActivity {

    private static final String TAG = "DonateHistoryScreen";
    FirebaseUser user;
    FirebaseFirestore firestore;
    RecyclerView recyclerView;
    ProgressBar progressBar;
    List<DocumentSnapshot> draftList;
    RecyclerRefreshLayout swipe_refresh;
    DonateHistoryAdapter adapter;
    TimeAgo timeAgo;
    LinearLayout noDonationLay;
    Button mDonateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate_history_screen);

        swipe_refresh = (RecyclerRefreshLayout) findViewById(R.id.donate_history_refresh_layout);
        progressBar = (ProgressBar) findViewById(R.id.donate_history_progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.donate_history_rec);
        noDonationLay = (LinearLayout) findViewById(R.id.no_donation_layout);
        mDonateBtn=findViewById(R.id.button2);

        mDonateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DonateHistoryScreen.this, DonateActivity.class));
                finish();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.donate_history_toolbar);

        timeAgo = new TimeAgo();
        firestore = FirebaseFirestore.getInstance();

        user = FirebaseAuth.getInstance().getCurrentUser();
        setToolbar(toolbar, "Donation History");

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
        adapter = new DonateHistoryAdapter(draftList, this);
        recyclerView.setAdapter(adapter);
        loadData(adapter);

    }

    private void loadData(final DonateHistoryAdapter adapter) {
        firestore.collection(DONATIONS)
                .whereEqualTo(UID, user.getUid())
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        swipe_refresh.setRefreshing(false);
                        progressBar.setVisibility(View.GONE);
                        if (queryDocumentSnapshots.isEmpty()) {
                            noDonationLay.setVisibility(View.VISIBLE);
                            swipe_refresh.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.GONE);
                            return;
                        }

                        draftList.clear();
                        draftList.addAll(queryDocumentSnapshots.getDocuments());
                        adapter.notifyDataSetChanged();
                        noDonationLay.setVisibility(View.GONE);
                        swipe_refresh.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                swipe_refresh.setRefreshing(false);
                Toast.makeText(DonateHistoryScreen.this, "Failed to read Donation history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class DonateHistoryAdapter extends RecyclerView.Adapter<DonateHistoryAdapter.MyViewHolder> {

        List<DocumentSnapshot> list;
        Context context;

        public DonateHistoryAdapter(List<DocumentSnapshot> homeList, Context context) {
            this.list = homeList;
            this.context = context;
        }

        @NonNull
        @Override
        public DonateHistoryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_view, parent, false);
            return new DonateHistoryAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DonateHistoryAdapter.MyViewHolder holder, final int position) {
            String status;
            String trx_status = list.get(position).getString(TRX_STATUS);
            String amount = list.get(position).getString(AMOUNT);
            String order_id = list.get(position).getString("PAYTM PARAMS.ORDER_ID");

            if (trx_status.equals(TRX_STATUS_SUCCESS)) {
                status = "Transaction Successful";
                holder.title.setTextColor(Color.GREEN);
            } else {
                status = "Transaction Failed";
                holder.title.setTextColor(Color.RED);
            }


            // String description = list.get(position).getString(DESCRIPTION);
            long timeStamp = list.get(position).getLong(TIMESTAMP);

            holder.timeStamp.setText(timeAgo.getlongtoago(timeStamp));
            holder.title.setText("₹" + amount);
            holder.subject.setText(status);
            holder.description.setText("OrderId:\n" + order_id);

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
