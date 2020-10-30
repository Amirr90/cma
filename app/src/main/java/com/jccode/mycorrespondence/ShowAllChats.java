package com.jccode.mycorrespondence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dinuscxj.refresh.RecyclerRefreshLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jccode.mycorrespondence.utility.Util.IMAGE;
import static com.jccode.mycorrespondence.utility.Util.IS_SEEN;
import static com.jccode.mycorrespondence.utility.Util.LAST_MSG;
import static com.jccode.mycorrespondence.utility.Util.NAME;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;

public class ShowAllChats extends AppCompatActivity {

    RecyclerView recyclerView;
    private RecyclerRefreshLayout swipe_refresh;

    FirebaseFirestore firestore;


    List<DocumentSnapshot> userList = new ArrayList<>();
    AllUserChatsAdapter allUserChatsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.all_user_toolbar);
        setToolbar(toolbar, "All users");

        recyclerView = (RecyclerView) findViewById(R.id.all_user_rec);
        swipe_refresh = (RecyclerRefreshLayout) findViewById(R.id.all_user_refresh_layout);

        firestore = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        allUserChatsAdapter = new AllUserChatsAdapter(userList, this);
        recyclerView.setAdapter(allUserChatsAdapter);
        loadData(allUserChatsAdapter);


        swipe_refresh.setRefreshStyle(RecyclerRefreshLayout.RefreshStyle.NORMAL);
        swipe_refresh.setOnRefreshListener(new RecyclerRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                swipe_refresh.setNestedScrollingEnabled(true);
                loadData(allUserChatsAdapter);
            }
        });


    }

    private void loadData(final AllUserChatsAdapter allUserChatsAdapter) {
        firestore.collection("Chat_us")
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING).addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e == null) {
                    if (queryDocumentSnapshots != null) {
                        swipe_refresh.setRefreshing(false);
                        userList.clear();
                        userList.addAll(queryDocumentSnapshots.getDocuments());
                        allUserChatsAdapter.notifyDataSetChanged();
                    }
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

    private class AllUserChatsAdapter extends RecyclerView.Adapter<AllUserChatsAdapter.MyAdapter> {

        List<DocumentSnapshot> allUserList;
        Context context;

        public AllUserChatsAdapter(List<DocumentSnapshot> allUserList, Context context) {
            this.allUserList = allUserList;
            this.context = context;
        }

        @NonNull
        @Override
        public AllUserChatsAdapter.MyAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_view, parent, false);
            return new MyAdapter(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final AllUserChatsAdapter.MyAdapter holder, final int position) {

            Boolean isSeen = allUserList.get(position).getBoolean(IS_SEEN);
            holder.message.setText(allUserList.get(position).getString(LAST_MSG));
            holder.name.setText(allUserList.get(position).getString(NAME));
            final String imageUrl = allUserList.get(position).getString(IMAGE);
            if (imageUrl != null && !imageUrl.equalsIgnoreCase(""))
                Picasso.with(context).load(imageUrl).networkPolicy(NetworkPolicy.OFFLINE).into(holder.mProfile, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(context).load(imageUrl).into(holder.mProfile);
                    }
                });

            if (isSeen != null) {

                if (isSeen) {
                    holder.name.setTypeface(null, Typeface.NORMAL);
                    holder.message.setTypeface(holder.name.getTypeface(), Typeface.NORMAL);
                } else {
                    holder.name.setTypeface(null, Typeface.BOLD_ITALIC);
                    holder.message.setTypeface(holder.name.getTypeface(), Typeface.BOLD_ITALIC);
                }

            }


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userId = userList.get(position).getId();
                    firestore.collection("Chat_us").document(userId).update(IS_SEEN, true);
                    startActivity(new Intent(context, ChatUsScreen.class));
                }
            });

        }

        @Override
        public int getItemCount() {
            return allUserList.size();
        }

        public class MyAdapter extends RecyclerView.ViewHolder {

            CircleImageView mProfile;
            TextView name, message;

            public MyAdapter(@NonNull View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.all_user_name);
                message = (TextView) itemView.findViewById(R.id.all_user_message);
                mProfile = (CircleImageView) itemView.findViewById(R.id.all_user_profile);
            }
        }
    }
}
