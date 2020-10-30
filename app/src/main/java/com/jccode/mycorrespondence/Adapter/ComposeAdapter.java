package com.jccode.mycorrespondence.Adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jccode.mycorrespondence.R;
import com.jccode.mycorrespondence.ShowComposeDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jccode.mycorrespondence.utility.Util.ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.COMPOSE;
import static com.jccode.mycorrespondence.utility.Util.CORRESPONDENCE_QUERY;
import static com.jccode.mycorrespondence.utility.Util.DATE;
import static com.jccode.mycorrespondence.utility.Util.DELETE_LETTER;
import static com.jccode.mycorrespondence.utility.Util.DEPARTMENT;
import static com.jccode.mycorrespondence.utility.Util.DOCUMENT;
import static com.jccode.mycorrespondence.utility.Util.FILED;
import static com.jccode.mycorrespondence.utility.Util.FROM;
import static com.jccode.mycorrespondence.utility.Util.ID;
import static com.jccode.mycorrespondence.utility.Util.IMAGE_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.LETTER_DETAIL;
import static com.jccode.mycorrespondence.utility.Util.LETTER_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.MAIN_ACTIVITY;
import static com.jccode.mycorrespondence.utility.Util.NO_TAG;
import static com.jccode.mycorrespondence.utility.Util.PARTIALLY_REPLIED;
import static com.jccode.mycorrespondence.utility.Util.PDF_ATTACHMENT;
import static com.jccode.mycorrespondence.utility.Util.REMARK;
import static com.jccode.mycorrespondence.utility.Util.REPLIED;
import static com.jccode.mycorrespondence.utility.Util.SHARED_ACTIVITY;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.TAG;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TYPE;
import static com.jccode.mycorrespondence.utility.Util.UN_REPLIED;


public class ComposeAdapter extends RecyclerView.Adapter<ComposeAdapter.MyViewHolder> {


    List<DocumentSnapshot> list;
    Context context;
    String cor_id;
    String LNumber;
    String LType;

    public ComposeAdapter(List<DocumentSnapshot> list, Context context, String cor_id, String LNumber, String LType) {
        this.list = list;
        this.context = context;
        this.cor_id = cor_id;
        this.LNumber = LNumber;
        this.LType = LType;
    }

    @NonNull
    @Override
    public ComposeAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.doc_view, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComposeAdapter.MyViewHolder holder, final int position) {

        try {
            String letterDate = list.get(position).getString(DATE);
            String letterNumber = list.get(position).getString(LETTER_NUMBER);
            String type = list.get(position).getString(TYPE);
            String remark = list.get(position).getString(REMARK);
            String tag = list.get(position).getString(TAG);

            final int pos = position + 1;
            holder.mSerialnumber.setText("" + pos);

            holder.mLetterDate.setText(letterDate);
            holder.mLetterNumber.setText(letterNumber);
            if (type.equalsIgnoreCase("Received"))
                holder.mType.setText("R");
            else
                holder.mType.setText("S");


            holder.mRemark.setText(remark);

            if (tag != null && !tag.isEmpty()) {
                holder.mDepartment.setText(tag);
                if (tag.equalsIgnoreCase(REPLIED)) {
                    holder.mDepartment.setTextColor(Color.GREEN);
                } else if (tag.equalsIgnoreCase(UN_REPLIED)) {
                    holder.mDepartment.setTextColor(Color.RED);
                } else if (tag.equalsIgnoreCase(PARTIALLY_REPLIED))
                    holder.mDepartment.setTextColor(Color.MAGENTA);
            } else {
                holder.mDepartment.setTextColor(Color.GRAY);
                holder.mDepartment.setText(NO_TAG);
            }

            holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<String> image_urls = new ArrayList<>();
                    ArrayList<String> pdf_urls = new ArrayList<>();
                    if (list.get(position).get(PDF_ATTACHMENT) != null) {

                        List<Map> images_map = (List<Map>) list.get(position).get(PDF_ATTACHMENT);
                        for (int a = 0; a < pdf_urls.size(); a++) {
                            pdf_urls.add((String) images_map.get(a).get(DOCUMENT));
                        }

                    }
                    if (list.get(position).get(IMAGE_ATTACHMENT) != null) {

                        List<Map> images_map = (List<Map>) list.get(position).get(IMAGE_ATTACHMENT);
                        for (int a = 0; a < images_map.size(); a++) {
                            image_urls.add((String) images_map.get(a).get(ATTACHMENT));
                        }

                    }


                    // Toast.makeText(context, "pdf found "+pdf_urls.size(), Toast.LENGTH_SHORT).show();
                    context.startActivity(new Intent(context, ShowComposeDetail.class)
                            .putStringArrayListExtra("image_urls", image_urls)
                            .putStringArrayListExtra("pdf_urls", pdf_urls)
                            .putExtra(SUBJECT, list.get(position).getString(SUBJECT))
                            .putExtra(ID, list.get(position).getId())
                            .putExtra(TYPE, LType)
                            .putExtra(FROM, MAIN_ACTIVITY)
                            .putExtra("CORRESPONDENCE_ID", cor_id)
                            .putExtra(TIMESTAMP, "" + list.get(position).getLong(TIMESTAMP))
                            .putExtra(REMARK, list.get(position).getString(REMARK))
                            .putExtra(DATE, list.get(position).getString(DATE))
                            .putExtra(LETTER_NUMBER, LNumber)
                            .putExtra(LETTER_DETAIL, list.get(position).getString(LETTER_DETAIL))
                            .putExtra(DEPARTMENT, list.get(position).getString(DEPARTMENT)));

                }
            });

            holder.linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {
                    showUpdateTagDialog(position, view);
                    return true;
                }
            });

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showUpdateTagDialog(final int position, final View view) {
        final CharSequence[] options = {FILED, UN_REPLIED, PARTIALLY_REPLIED, REPLIED, DELETE_LETTER};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Update TAG");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String TAG = options[which].toString();
                if (TAG.equalsIgnoreCase("Delete Letter")) {
                    deleteLetter(position, view);
                } else {
                    updateTAG(TAG, position, view);
                }
            }
        });
        builder.show();
    }

    private void updateTAG(String tag, int position, final View view) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Updating TAG");
        progressDialog.setMessage("please wait....");
        progressDialog.show();
        Map<String, Object> updateTAGMap = new HashMap<>();
        updateTAGMap.put(TAG, tag);
        String compose_id = list.get(position).getId();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(CORRESPONDENCE_QUERY).document(cor_id).collection(COMPOSE)
                .document(compose_id).update(updateTAGMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Snackbar.make(view, "TAG updated successfully", Snackbar.LENGTH_SHORT).show();
                // Toast.makeText(context,"TAG updated successfully" , Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Snackbar.make(view, "could't update TAG, please try again", Snackbar.LENGTH_SHORT).show();
                //Toast.makeText(context, "could't update TAG, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteLetter(final int position, final View view) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Letter")
                .setMessage("Do you really want to  Delete this Letter ??")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        String compose_id = list.get(position).getId();
                        final ProgressDialog dialog = new ProgressDialog(context);
                        dialog.setMessage("Deleting");
                        dialog.show();
                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                        firestore.collection(CORRESPONDENCE_QUERY).document(cor_id).collection(COMPOSE)
                                .document(compose_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                notifyDataSetChanged();
                                Snackbar.make(view, "Letter Deleted", Snackbar.LENGTH_SHORT).show();
                                // Toast.makeText(context, "Letter Deleted", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(view, "could't delete Letter, please try again", Snackbar.LENGTH_SHORT).show();
                                //Toast.makeText(context, "try again", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
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
        TextView mSerialnumber, mLetterDate, mLetterNumber, mDepartment, mType, mDocumentNumber, mRemark;
        LinearLayout linearLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);


            mSerialnumber = (TextView) itemView.findViewById(R.id.textView21);
            mLetterDate = (TextView) itemView.findViewById(R.id.textView19);
            mLetterNumber = (TextView) itemView.findViewById(R.id.textView20);
            mDepartment = (TextView) itemView.findViewById(R.id.textView18);
            mType = (TextView) itemView.findViewById(R.id.textView15);
            //mDocumentNumber = (TextView) itemView.findViewById(R.id.textView16);
            mRemark = (TextView) itemView.findViewById(R.id.textView17);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.table_lay);

            mLetterNumber.setPaintFlags(mLetterNumber.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        }
    }
}
