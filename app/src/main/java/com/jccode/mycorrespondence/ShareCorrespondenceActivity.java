package com.jccode.mycorrespondence;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.jccode.mycorrespondence.Interface.RetrofitService;
import com.jccode.mycorrespondence.models.ResponseModel;
import com.jccode.mycorrespondence.utility.Util;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_ID;
import static com.jccode.mycorrespondence.utility.Util.DOC_ID;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.F_NAME;
import static com.jccode.mycorrespondence.utility.Util.IMAGE;
import static com.jccode.mycorrespondence.utility.Util.L_NAME;
import static com.jccode.mycorrespondence.utility.Util.MOBILE;
import static com.jccode.mycorrespondence.utility.Util.POST;
import static com.jccode.mycorrespondence.utility.Util.SHARED_BY;
import static com.jccode.mycorrespondence.utility.Util.SHARED_TO;
import static com.jccode.mycorrespondence.utility.Util.SHARE_DOCUMENTS;
import static com.jccode.mycorrespondence.utility.Util.SHARE_LIVE_DOCUMENTS;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.getNameFromURI;

public class ShareCorrespondenceActivity extends AppCompatActivity {

    Intent intent;
    String CID, from;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    ProgressDialog progressDialog;

    RecyclerView recyclerView;
    private RecyclerRefreshLayout swipe_refresh;


    List<DocumentSnapshot> userList = new ArrayList<>();
    AllUserAdapter allUserAdapter;
    ProgressBar progressBar;

    FirebaseUser user;
    Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_correspondence);

        Toolbar toolbar = (Toolbar) findViewById(R.id.share_correspondence_cardView);
        setToolbar(toolbar, SHARE_DOCUMENTS.toString());
        intent = new Intent();

        user = FirebaseAuth.getInstance().getCurrentUser();
        progressBar = (ProgressBar) findViewById(R.id.progressBar6);

        util = new Util();
        progressDialog = new ProgressDialog(this);
        CID = getIntent().getStringExtra(CORRESPONDENCE_ID);
        from = getIntent().getStringExtra(FROM);

        recyclerView = (RecyclerView) findViewById(R.id.all_user_rec3);
        swipe_refresh = (RecyclerRefreshLayout) findViewById(R.id.all_user_refresh_layout3);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        allUserAdapter = new AllUserAdapter(userList, this);
        recyclerView.setAdapter(allUserAdapter);
        //loadData(allUserAdapter);

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
                .limit(20)
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

    @Override
    public boolean onSupportNavigateUp() {

        setResult(RESULT_CANCELED, intent);
        finish();
        return super.onSupportNavigateUp();

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
            return new MyAdapter(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final AllUserAdapter.MyAdapter holder, final int position) {

            final DocumentSnapshot snapshot = allUserList.get(position);
            final String name;
            String lastName = snapshot.getString(L_NAME);
            String firstName = snapshot.getString(F_NAME);
            if (lastName != null && !lastName.isEmpty())
                name = firstName + " " + lastName;
            else
                name = firstName;
            holder.name.setText(name);
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
            else
                holder.mProfile.setImageResource(R.drawable.profile);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String shareId = snapshot.getId();
                    final String userName = name;
                    new AlertDialog.Builder(ShareCorrespondenceActivity.this)
                            .setMessage("Want to Share with " + userName)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    shareCorrespondence(shareId, userName);
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }).show();
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

    private void shareCorrespondence(String shareId, String userName) {

        progressDialog.setTitle("Sharing Document");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://us-central1-cmafirebaseproject-d7ae8.cloudfunctions.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final RetrofitService uploadInterFace = retrofit.create(RetrofitService.class);
        Call<Void> call;
        if (from.equalsIgnoreCase("Document")) {
            call = uploadInterFace.shareDocuments(user.getUid(), shareId, CID);
            call.enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {

                    progressDialog.dismiss();
                    if (response.isSuccessful() && response.code() == 200)
                        setResult(RESULT_OK);
                    else
                        setResult(RESULT_CANCELED);

                    finish();

                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(ShareCorrespondenceActivity.this, "failed " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
        } else if (from.equalsIgnoreCase(SHARE_LIVE_DOCUMENTS)) {
            String myUserName = util.getUserName(ShareCorrespondenceActivity.this);
            Call<ResponseModel> call2 = uploadInterFace.shareLiveDocuments(user.getUid(), shareId, CID, userName, myUserName);

            call2.enqueue(new retrofit2.Callback<ResponseModel>() {
                @Override
                public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                    progressDialog.dismiss();

                    if (response.isSuccessful() && response.code() == 200) {
                        if (response.body().getResponseCode() == 1) {
                            intent.putExtra("result", "Shared Successfully");
                            setResult(RESULT_OK, intent);
                        } else if (response.body().getResponseCode() == 0) {
                            intent.putExtra("result", response.body().getResult());
                            setResult(RESULT_CANCELED, intent);
                        }
                    } else {
                        intent.putExtra("result", "failed to share");
                        setResult(RESULT_CANCELED, intent);


                    }
                    finish();
                }

                @Override
                public void onFailure(Call<ResponseModel> call, Throwable t) {
                    intent.putExtra("result", "failed to share");
                    progressDialog.dismiss();
                    setResult(RESULT_CANCELED, intent);
                    finish();
                }
            });
        } else {
            call = uploadInterFace.shareCorrespondence(user.getUid(), shareId, CID);
            call.enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful() && response.code() == 200)
                        setResult(RESULT_OK);
                    else
                        setResult(RESULT_CANCELED);

                    finish();

                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(ShareCorrespondenceActivity.this, "failed " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });

        }


    }

    private void uploadLiveSharingData(String shareId, String uid, String CID, String userName) {


        /*DocumentReference shareByMeRef = firestore.collection("Live_Document_Shared_data")
                .document(uid).collection("Shared_By_Me")
                .document(System.currentTimeMillis() + "");

        final DocumentReference shareToMeRef = firestore.collection("Live_Document_Shared_data")
                .document(shareId).collection("Shared_To_Me")
                .document(System.currentTimeMillis() + "");

        final Map<String, Object> map = new HashMap<>();
        map.put(DOC_ID, CID);
        map.put(TIMESTAMP, System.currentTimeMillis());
        map.put(SHARED_TO, shareId);
        // map.put(NAME, userName);


        final Map<String, Object> map2 = new HashMap<>();
        map2.put(DOC_ID, CID);
        map2.put(TIMESTAMP, System.currentTimeMillis());
        map2.put(SHARED_BY, user.getUid());
        //map.put(NAME, getUserName(ShareCorrespondenceActivity.this));


        shareByMeRef.set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                shareToMeRef.set(map2).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        setResult(RESULT_OK);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });*/


    }

    public void searchUsers(final View view) {
        int id = view.getId();
        if (id == R.id.button3) {
            EditText searchNumber = (EditText) findViewById(R.id.editTextPhone);
            String number = searchNumber.getText().toString();
            if (number.isEmpty()) {
                Snackbar.make(view, "Enter number ", Snackbar.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                firestore.collection("Users")
                        .whereEqualTo(MOBILE, "+91" + number)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                userList.clear();
                                progressBar.setVisibility(View.GONE);
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    userList.addAll(queryDocumentSnapshots.getDocuments());
                                    allUserAdapter.notifyDataSetChanged();
                                } else {
                                    Snackbar.make(view, "No user found ", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ShareCorrespondenceActivity.this, "try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
    }

}
