package com.jccode.mycorrespondence;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.jccode.mycorrespondence.utility.Util;

import static android.os.Build.ID;
import static com.jccode.mycorrespondence.utility.Util.DEPARTMENT;
import static com.jccode.mycorrespondence.utility.Util.DESCRIPTION;
import static com.jccode.mycorrespondence.utility.Util.LETTER_NUMBER;
import static com.jccode.mycorrespondence.utility.Util.REMARK;
import static com.jccode.mycorrespondence.utility.Util.SENDER_DETAIL;
import static com.jccode.mycorrespondence.utility.Util.SUBJECT;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TYPE;

public class ComposeCorrespondence extends AppCompatActivity {

    public String Title, Description, Subject, Id;
    Util util;
    private String LetterNumber, Timestamp, Type, SenderDetail, Remark;
    private String Department;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_correspondence);

        Toolbar toolbar = (Toolbar) findViewById(R.id.compose_corres_toolbar);
        util = new Util();

        hideKeyBoard();

        if (getIntent().hasExtra(SUBJECT)) {
       /*     Id = getIntent().getStringExtra(ID);
            Title = getIntent().getStringExtra(TITLE);
            Description = getIntent().getStringExtra(DESCRIPTION);*/


            Id = getIntent().getStringExtra(ID);
            LetterNumber = getIntent().getStringExtra(LETTER_NUMBER);
            Description = getIntent().getStringExtra(DESCRIPTION);
            Subject = getIntent().getStringExtra(SUBJECT);
            Department = getIntent().getStringExtra(DEPARTMENT);
            Timestamp = getIntent().getStringExtra(TIMESTAMP);
            Type = getIntent().getStringExtra(TYPE);
            SenderDetail = getIntent().getStringExtra(SENDER_DETAIL);
            Remark = getIntent().getStringExtra(REMARK);
            setToolbar(toolbar, "Compose Correspondence");

            setDetails();


        } else {
            setToolbar(toolbar, "Compose New Correspondence");
        }

    }

    private void setDetails() {

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



    public void addSignature(final View view2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ComposeCorrespondence.this);

        // get the layout inflater
        LayoutInflater inflater = ComposeCorrespondence.this.getLayoutInflater();

        View view = inflater.inflate(R.layout.update_signature_layout, null);
        final EditText signature = (EditText) view.findViewById(R.id.signature_editText);
        String sign = util.getSignature(ComposeCorrespondence.this);
        if (sign != null) {
            signature.setText(sign);
        }
        builder.setTitle("Update Signature");
        builder.setView(view)
                // action buttons
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String Signature = signature.getText().toString();
                        if (Signature.isEmpty()) {
                            Toast.makeText(ComposeCorrespondence.this, "Fill all the fields", Toast.LENGTH_SHORT).show();
                        } else {
                            util.addSignature(Signature, ComposeCorrespondence.this);
                            Snackbar.make(view2, "Signature Updated", Snackbar.LENGTH_SHORT).show();
                        }
                        // your sign in code here
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // remove the dialog from the screen
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void hideKeyBoard() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
