package com.jccode.mycorrespondence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jccode.mycorrespondence.utility.Util.F_NAME;
import static com.jccode.mycorrespondence.utility.Util.IMAGE;
import static com.jccode.mycorrespondence.utility.Util.L_NAME;
import static com.jccode.mycorrespondence.utility.Util.POST;

public class ShowAllUsers extends AppCompatActivity {


    RecyclerView recyclerView;
    private RecyclerRefreshLayout swipe_refresh;

    FirebaseFirestore firestore;


    List<DocumentSnapshot> userList = new ArrayList<>();
    AllUserAdapter allUserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_users);


        Toolbar toolbar = (Toolbar) findViewById(R.id.all_user_toolbar2);
        setToolbar(toolbar, "All users");

        recyclerView = (RecyclerView) findViewById(R.id.all_user_rec2);
        swipe_refresh = (RecyclerRefreshLayout) findViewById(R.id.all_user_refresh_layout2);

        firestore = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        allUserAdapter = new AllUserAdapter(userList, this);
        recyclerView.setAdapter(allUserAdapter);
        loadData(allUserAdapter);

        swipe_refresh.setRefreshStyle(RecyclerRefreshLayout.RefreshStyle.NORMAL);
        swipe_refresh.setOnRefreshListener(new RecyclerRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                swipe_refresh.setNestedScrollingEnabled(true);
                loadData(allUserAdapter);
            }
        });

    }


    private void loadData(final AllUserAdapter allUserAdapter) {
        firestore.collection("Users")
                // .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e == null) {
                            if (queryDocumentSnapshots != null) {
                                swipe_refresh.setRefreshing(false);
                                userList.clear();
                                userList.addAll(queryDocumentSnapshots.getDocuments());
                                allUserAdapter.notifyDataSetChanged();
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

    private class AllUserAdapter extends RecyclerView.Adapter<AllUserAdapter.MyAdapter> {

        List<DocumentSnapshot> allUserList;
        Context context;

        public AllUserAdapter(List<DocumentSnapshot> allUserList, Context context) {
            this.allUserList = allUserList;
            this.context = context;
        }

        @NonNull
        @Override
        public AllUserAdapter.MyAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_view, parent, false);
            return new AllUserAdapter.MyAdapter(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final AllUserAdapter.MyAdapter holder, final int position) {

            DocumentSnapshot snapshot = allUserList.get(position);
            String userName = snapshot.getString(F_NAME) + " " + snapshot.getString(L_NAME);
            holder.name.setText(userName);
            holder.post.setText(snapshot.getString(POST));
            final String imageUrl = snapshot.getString(IMAGE);
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


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userId = userList.get(position).getId();
                    startActivity(new Intent(context, UserProfile.class).putExtra("userId", userId));
                }
            });

        }

        @Override
        public int getItemCount() {
            return allUserList.size();
        }

        public class MyAdapter extends RecyclerView.ViewHolder {

            CircleImageView mProfile;
            TextView name, post;

            public MyAdapter(@NonNull View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.all_user_name);
                post = (TextView) itemView.findViewById(R.id.all_user_message);
                mProfile = (CircleImageView) itemView.findViewById(R.id.all_user_profile);
            }
        }
    }
}
