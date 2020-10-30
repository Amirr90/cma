package com.jccode.mycorrespondence.Adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jccode.mycorrespondence.DocumentDetail;
import com.jccode.mycorrespondence.R;
import com.jccode.mycorrespondence.utility.TimeAgo;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.jccode.mycorrespondence.utility.Util.DESCRIPTION;
import static com.jccode.mycorrespondence.utility.Util.DOC_ID;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.VIEW_LIVE_SHARED_DOCUMENT_BY_ME;

public class LiveDataAdapter extends RecyclerView.Adapter<LiveDataAdapter.ViewHolder> {
    List<DocumentSnapshot> snapshots;
    List<DocumentSnapshot> snapshots1;
    Context context;
    TimeAgo timeAgo;
    String from;

    public LiveDataAdapter(List<DocumentSnapshot> snapshots, Context context, String from, List<DocumentSnapshot> snapshots1) {
        this.snapshots = snapshots;
        this.context = context;
        this.from = from;
        this.snapshots1 = snapshots1;
    }


    @NonNull
    @Override
    public LiveDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_view, parent, false);
        timeAgo = new TimeAgo();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LiveDataAdapter.ViewHolder holder, final int position) {


        String subject = snapshots.get(position).getString(SUBJECT);
        String description = snapshots.get(position).getString(DESCRIPTION);
        long time = snapshots1.get(position).getLong(TIMESTAMP);
        if (from.equalsIgnoreCase(VIEW_LIVE_SHARED_DOCUMENT_BY_ME)) {
            holder.mActiveStatus.setText(FROM + " : " + snapshots1.get(position).getString("shareByName"));
        } else {
            holder.mActiveStatus.setText("To : " + snapshots1.get(position).getString("shareToName"));
        }


        holder.timeStamp.setText(timeAgo.getlongtoago(time));
        holder.mSubject.setText(subject);
        holder.mDescription.setText(description);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = snapshots.get(position).getId();
                String subject = snapshots.get(position).getString(SUBJECT);
                String description = snapshots.get(position).getString(DESCRIPTION);
                String timestamp = "" + snapshots.get(position).getLong(TIMESTAMP);
                context.startActivity(new Intent(context, DocumentDetail.class)
                        .putExtra(DESCRIPTION, description)
                        .putExtra(TIMESTAMP, timestamp)
                        .putExtra(SUBJECT, subject)
                        .putExtra(DOC_ID, id)
                        .putExtra(FROM, from));


            }
        });


        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!from.equalsIgnoreCase(VIEW_LIVE_SHARED_DOCUMENT_BY_ME)) {
                    new AlertDialog.Builder(context).setTitle("Remove Document")
                            .setMessage("want to remove this Document from Live Sharing??")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    final ProgressDialog progressDialog = new ProgressDialog(context);
                                    progressDialog.setMessage("Removing Document, Please wait...");
                                    progressDialog.show();
                                    final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                    firestore.collection("Live_Document_Shared_data")
                                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .collection("MySharedDocuments")
                                            .document(snapshots1.get(position).getId())
                                            .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            firestore.collection("Live_Document_Shared_data")
                                                    .document(snapshots1.get(position).getString("shareId"))
                                                    .collection("MySharedDocuments")
                                                    .document(snapshots1.get(position).getId())
                                                    .delete();
                                            progressDialog.dismiss();
                                            Toast.makeText(context, "Document Removed Successfully", Toast.LENGTH_SHORT).show();
                                            snapshots.remove(position);
                                            snapshots1.remove(position);
                                            notifyDataSetChanged();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(context, "try again", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();

                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return snapshots.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mSubject, mDescription, mActiveStatus, timeStamp;
        private CircleImageView mCategoryImage;
        private RelativeLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mSubject = (TextView) itemView.findViewById(R.id.textView);
            mDescription = (TextView) itemView.findViewById(R.id.textView2);
            mActiveStatus = (TextView) itemView.findViewById(R.id.textView3);
            mCategoryImage = (CircleImageView) itemView.findViewById(R.id.profile_image);
            timeStamp = (TextView) itemView.findViewById(R.id.textView13);
            layout = (RelativeLayout) itemView.findViewById(R.id.home_view_lay);
            // mActiveStatus.setVisibility(View.GONE);
        }
    }
}
