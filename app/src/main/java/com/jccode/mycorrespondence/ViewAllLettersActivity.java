package com.jccode.mycorrespondence;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.jccode.mycorrespondence.utility.TimeAgo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.jccode.mycorrespondence.utility.Util.ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.COMPOSE;
import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DATE;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.IMAGE_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.LETTER_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.SHARED_DOCUMENT_QUERY;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TYPE;

public class ViewAllLettersActivity extends AppCompatActivity {

    private static final String TAG = "ViewAllLettersActivity";
    RecyclerView rvParent;
    FirebaseFirestore firestore;
    List<DocumentSnapshot> snapshots = new ArrayList<>();
    ArrayList<String> imageList = new ArrayList<>();
    ProgressDialog progressDialog;
    TimeAgo timeAgo;

    TextView ImageCounter;
    String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_letters);
        Toolbar toolbar = (Toolbar) findViewById(R.id.view_all_letters_toolbar);
        setToolbar(toolbar, "Image Attachment");

        rvParent = (RecyclerView) findViewById(R.id.rv_parent);
        rvParent.setLayoutManager(new LinearLayoutManager(this));
        rvParent.setHasFixedSize(true);

        timeAgo = new TimeAgo();

        from = getIntent().getStringExtra(FROM);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait....");

        firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

        setParentAdapter();

    }

    private void setParentAdapter() {

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            progressDialog.show();
            if (from.equalsIgnoreCase("Main")) {
                final String _ID = getIntent().getStringExtra(ID);
                firestore.collection(CORRESPONDENCE_QUERY)
                        .document(_ID)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                progressDialog.dismiss();
                                if (task.getResult() != null) {
                                    DocumentSnapshot snapshot = task.getResult();
                                    snapshots.add(snapshot);
                                    firestore.collection(CORRESPONDENCE_QUERY).document(_ID)
                                            .collection(COMPOSE)
                                            .orderBy(TIMESTAMP, Query.Direction.ASCENDING)
                                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.getResult() != null && task.isSuccessful()) {
                                                snapshots.addAll(task.getResult().getDocuments());
                                                if (snapshots != null)
                                                    rvParent.setAdapter(new ParentAdapter(getApplicationContext(), snapshots));
                                            } else {
                                                if (snapshots != null)
                                                    rvParent.setAdapter(new ParentAdapter(getApplicationContext(), snapshots));
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "Error " + e.getLocalizedMessage());
                                            if (snapshots != null)
                                                rvParent.setAdapter(new ParentAdapter(getApplicationContext(), snapshots));
                                        }
                                    });
                                }
                            }
                        });
            } else {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                final String _ID = getIntent().getStringExtra(ID);
                firestore.collection("Users")
                        .document(user.getUid())
                        .collection(SHARED_DOCUMENT_QUERY)
                        .document(_ID)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                progressDialog.dismiss();
                                if (task.getResult() != null) {
                                    DocumentSnapshot snapshot = task.getResult();
                                    snapshots.add(snapshot);
                                    firestore.collection("Users")
                                            .document(user.getUid())
                                            .collection(SHARED_DOCUMENT_QUERY)
                                            .document(_ID)
                                            .collection(COMPOSE)
                                            .orderBy(TIMESTAMP, Query.Direction.ASCENDING)
                                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.getResult() != null && task.isSuccessful()) {
                                                snapshots.addAll(task.getResult().getDocuments());
                                                if (snapshots != null)
                                                    rvParent.setAdapter(new ParentAdapter(getApplicationContext(), snapshots));
                                            } else {
                                                if (snapshots != null)
                                                    rvParent.setAdapter(new ParentAdapter(getApplicationContext(), snapshots));
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "Error " + e.getLocalizedMessage());
                                            if (snapshots != null)
                                                rvParent.setAdapter(new ParentAdapter(getApplicationContext(), snapshots));
                                        }
                                    });
                                }
                            }
                        });
            }

        }


    }

    private void setToolbar(Toolbar toolbar, String id) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(id);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public class ParentAdapter extends RecyclerView.Adapter<ParentAdapter.MyParentViewHolder> {

        Context context;
        List<DocumentSnapshot> snapshots;

        public ParentAdapter(Context context, List<DocumentSnapshot> snapshots) {
            this.context = context;
            this.snapshots = snapshots;
            imageList = getIntent().getStringArrayListExtra("pdfs");
        }

        @NonNull
        @Override
        public MyParentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_parent_view, parent, false);
            return new MyParentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyParentViewHolder holder, final int position) {


            DocumentSnapshot mySnapShots = snapshots.get(position);
            try {
                holder.textView.setText("Letter Number: " + mySnapShots.get(LETTER_NUMBER));
                holder.mLetterType.setText(mySnapShots.getString(TYPE));
                holder.mLetterTime.setText("Letter Date: " + mySnapShots.getString(DATE));
                if (position == 0) {
                    holder.ImageCounter.setText(imageList.size() + " Images");
                } else {
                    setImageCounter(holder, mySnapShots.getId());
                }
            } catch (Exception e) {
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    progressDialog.show();
                    if (position == 0) {
                        if (!imageList.isEmpty()) {
                            progressDialog.dismiss();
                            startActivity(new Intent(context, FullScreenImageScreen.class)
                                    .putStringArrayListExtra("images", imageList));
                        } else {
                            progressDialog.dismiss();
                            Snackbar.make(v, "No image for this letter", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        if (from.equalsIgnoreCase("Main")) {
                            firestore.collection(CORRESPONDENCE_QUERY).document(getIntent().getStringExtra(ID))
                                    .collection(COMPOSE)
                                    .document(snapshots.get(position).getId())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                ArrayList<String> childSnapshots = new ArrayList<>();
                                                DocumentSnapshot snapshot = task.getResult();
                                                if (snapshot.get(IMAGE_ATTACHMENT) != null) {
                                                    List<Map<String, Object>> maps = (List<Map<String, Object>>) snapshot.get(IMAGE_ATTACHMENT);
                                                    for (int b = 0; b < maps.size(); b++) {
                                                        String imageUrl = maps.get(b).get(ATTACHMENT).toString();
                                                        childSnapshots.add(imageUrl);
                                                    }
                                                    progressDialog.dismiss();
                                                    startActivity(new Intent(context, FullScreenImageScreen.class)
                                                            .putStringArrayListExtra("images", childSnapshots));
                                                } else {
                                                    progressDialog.dismiss();
                                                    Snackbar.make(v, "No image for this letter", Snackbar.LENGTH_SHORT).show();
                                                    //Toast.makeText(ViewAllLettersActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                progressDialog.dismiss();
                                                Snackbar.make(v, "could't fetch image, Please try again", Snackbar.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            firestore.collection("Users")
                                    .document(user.getUid())
                                    .collection(SHARED_DOCUMENT_QUERY)
                                    .document(getIntent().getStringExtra(ID))
                                    .collection(COMPOSE)
                                    .document(snapshots.get(position).getId())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                ArrayList<String> childSnapshots = new ArrayList<>();
                                                DocumentSnapshot snapshot = task.getResult();
                                                if (snapshot.get(IMAGE_ATTACHMENT) != null) {
                                                    List<Map<String, Object>> maps = (List<Map<String, Object>>) snapshot.get(IMAGE_ATTACHMENT);
                                                    for (int b = 0; b < maps.size(); b++) {
                                                        String imageUrl = maps.get(b).get(ATTACHMENT).toString();
                                                        childSnapshots.add(imageUrl);
                                                    }
                                                    progressDialog.dismiss();
                                                    startActivity(new Intent(context, FullScreenImageScreen.class)
                                                            .putStringArrayListExtra("images", childSnapshots));
                                                } else {
                                                    progressDialog.dismiss();
                                                    Snackbar.make(v, "No image for this letter", Snackbar.LENGTH_SHORT).show();
                                                    //Toast.makeText(ViewAllLettersActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                progressDialog.dismiss();
                                                Snackbar.make(v, "could't fetch image, Please try again", Snackbar.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }


                    }
                }
            });
        }


        @Override
        public int getItemCount() {
            return snapshots.size();
        }

        public class MyParentViewHolder extends RecyclerView.ViewHolder {

            RecyclerView rvChild;
            TextView textView, mLetterTime, mLetterType, ImageCounter;


            public MyParentViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = (TextView) itemView.findViewById(R.id.textView);
                mLetterTime = (TextView) itemView.findViewById(R.id.time2);
                mLetterType = (TextView) itemView.findViewById(R.id.type2);
                ImageCounter = (TextView) itemView.findViewById(R.id.textView36);

            }
        }
    }

    private void setImageCounter(final ParentAdapter.MyParentViewHolder holder, String docId) {
        try {
            if (from.equalsIgnoreCase("Main")) {
                firestore.collection(CORRESPONDENCE_QUERY)
                        .document(getIntent().getStringExtra(ID))
                        .collection(COMPOSE)
                        .document(docId)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    if (documentSnapshot.get(IMAGE_ATTACHMENT) != null) {
                                        List<Map<String, Object>> maps = (List<Map<String, Object>>) documentSnapshot.get(IMAGE_ATTACHMENT);
                                        holder.ImageCounter.setText(maps.size() + " Images");
                                    } else {
                                        holder.ImageCounter.setText("0 Images");
                                    }
                                }
                            }
                        });
            } else {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                firestore.collection("Users")
                        .document(user.getUid())
                        .collection(SHARED_DOCUMENT_QUERY)
                        .document(getIntent().getStringExtra(ID))
                        .collection(COMPOSE)
                        .document(docId)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    if (documentSnapshot.get(IMAGE_ATTACHMENT) != null) {
                                        List<Map<String, Object>> maps = (List<Map<String, Object>>) documentSnapshot.get(IMAGE_ATTACHMENT);
                                        holder.ImageCounter.setText(maps.size() + " Images");
                                    } else {
                                        holder.ImageCounter.setText("0 Images");
                                    }
                                }
                            }
                        });
            }
        } catch (Exception e) {
        }

    }

}
