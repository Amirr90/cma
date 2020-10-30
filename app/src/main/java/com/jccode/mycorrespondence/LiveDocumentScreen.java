package com.jccode.mycorrespondence;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jccode.mycorrespondence.Adapter.LiveDataAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.VIEW_LIVE_SHARED_DOCUMENT_TO_ME;

public class LiveDocumentScreen extends AppCompatActivity {


    private static final String TAG = "LiveDocumentScreen";
    String from;
    List<DocumentSnapshot> snapshotList = new ArrayList<>();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    FirebaseUser user;

    RecyclerView recyclerView;
    LiveDataAdapter liveDataAdapter;
    Toolbar toolbar;
    ProgressDialog progressDialog;
    private RecyclerRefreshLayout swipe_refresh;
    TextView result;
    List<DocumentSnapshot> snapshots1 = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_document_screen2);

        user = FirebaseAuth.getInstance().getCurrentUser();

        from = getIntent().getStringExtra(FROM);
        toolbar = (Toolbar) findViewById(R.id.LiveDocumentScreen_toolbar);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading, Please wait....");
        progressDialog.show();


        result = (TextView) findViewById(R.id.LiveDocumentScreen_textView34);
        swipe_refresh = (RecyclerRefreshLayout) findViewById(R.id.LiveDocumentScreen_refresh_layout);

        setRec();

        swipe_refresh.setRefreshStyle(RecyclerRefreshLayout.RefreshStyle.NORMAL);
        swipe_refresh.setOnRefreshListener(new RecyclerRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe_refresh.setNestedScrollingEnabled(true);
                getDocumentsList(from);
            }
        });


    }

    private void setRec() {

        recyclerView = findViewById(R.id.rec_1);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        liveDataAdapter = new LiveDataAdapter(snapshotList, this, from, snapshots1);
        recyclerView.setAdapter(liveDataAdapter);
        getDocumentsList(from);

    }

    private void getDocumentsList(String viewLiveSharedDocumentByMe) {

        final Query ref;
        if (viewLiveSharedDocumentByMe.equalsIgnoreCase(VIEW_LIVE_SHARED_DOCUMENT_TO_ME)) {

            ref = firestore.collection("Live_Document_Shared_data")
                    .document(user.getUid())
                    .collection("MySharedDocuments")
                    .whereEqualTo("shareByMe", user.getUid());
            setToolbar(toolbar, "Shared By me");
        } else {
            ref = firestore.collection("Live_Document_Shared_data")
                    .document(user.getUid())
                    .collection("MySharedDocuments")
                    .whereEqualTo("shareToMe", user.getUid());
            setToolbar(toolbar, "Shared To Me");
        }


        final CollectionReference doc_ref = firestore
                .collection("Document_QUERY");
        ref
                .orderBy(TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        progressDialog.dismiss();
                        if (queryDocumentSnapshots.isEmpty()) {
                            result.setText("0 Results");
                            return;
                        }
                        snapshotList.clear();
                        snapshots1.clear();

                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            snapshots1.add(snapshot);
                            doc_ref.document(snapshot.getString("docId"))
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot snapshot) {
                                    snapshotList.add(snapshot);
                                    liveDataAdapter.notifyDataSetChanged();
                                    result.setText(snapshotList.size() + " Results");

                                }
                            });

                        }

                        swipe_refresh.setRefreshing(false);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d(TAG, "error " + e.getLocalizedMessage());
                Toast.makeText(LiveDocumentScreen.this, "failed to get data", Toast.LENGTH_SHORT).show();
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
}