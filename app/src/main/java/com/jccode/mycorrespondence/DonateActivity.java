package com.jccode.mycorrespondence;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bestsoft32.tt_fancy_gif_dialog_lib.TTFancyGifDialog;
import com.bestsoft32.tt_fancy_gif_dialog_lib.TTFancyGifDialogListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jccode.mycorrespondence.Interface.RetrofitService;
import com.jccode.mycorrespondence.models.CheckSumModel;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.jccode.mycorrespondence.utility.Util.AMOUNT;
import static com.jccode.mycorrespondence.utility.Util.DONATIONS;
import static com.jccode.mycorrespondence.utility.Util.PAYTM_PARAMS;
import static com.jccode.mycorrespondence.utility.Util.TIMESTAMP;
import static com.jccode.mycorrespondence.utility.Util.TRX_STATUS;
import static com.jccode.mycorrespondence.utility.Util.TRX_STATUS_FAILED;
import static com.jccode.mycorrespondence.utility.Util.TRX_STATUS_SUCCESS;
import static com.jccode.mycorrespondence.utility.Util.UID;


public class DonateActivity extends AppCompatActivity {

    private static final int UPI_PAYMENT_CODE = 101;
    EditText editText_donation;
    ProgressDialog progressDialog;
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate_avtivity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.donate_toolbar);
        setToolbar(toolbar, "Donation");

        editText_donation = (EditText) findViewById(R.id.edit_donation);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("please wait...");

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            user = FirebaseAuth.getInstance().getCurrentUser();
        } else {
            finish();
        }


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

    public void gotoPaymentPage(View view) {
        String donationAmount = editText_donation.getText().toString();
        if (!donationAmount.isEmpty()) {
            //startPayTmTransaction(donationAmount);
            payUsingUPI(donationAmount);
        } else {
            editText_donation.setError("required");
        }
    }

    private void payUsingUPI(String donationAmount) {

         String upiText = "7985392039@upi";
       // String upiText = "9044865611@ybl";
        Uri uri = Uri.parse("upi://pay").buildUpon().appendQueryParameter("pa", upiText)
                .appendQueryParameter("am", donationAmount)
                .appendQueryParameter("cu", "INR")
                .appendQueryParameter("tn", "Armaan Trust")
                .appendQueryParameter("pn", "abc")
                .build();
        Intent upi_payment = new Intent(Intent.ACTION_VIEW);
        upi_payment.setData(uri);
        Intent chooser = Intent.createChooser(upi_payment, "Pay with");

        if (null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT_CODE);
        } else {
            Toast.makeText(this, "No Upi App found", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPI_PAYMENT_CODE) {
            progressDialog.show();
            if (resultCode == RESULT_OK || resultCode == 11) {
                if (data != null) {
                    String res = data.getStringExtra("response");
                    ArrayList<String> list = new ArrayList<>();
                    //Toast.makeText(this, "" + list, Toast.LENGTH_SHORT).show();
                    list.add(res);
                    updatePaymentResponse(list);

                } else {
                    Toast.makeText(this, "payment failure, No data found ", Toast.LENGTH_SHORT).show();
                }
            } else {
                progressDialog.dismiss();
            }
        }

       /* if (resultCode == Activity.RESULT_OK) {
            if (requestCode == UPI_PAYMENT_CODE) {
                String paymentResponse = data.getStringExtra("response");
                Toast.makeText(this, "" + paymentResponse, Toast.LENGTH_SHORT).show();
            }
        } else {
            String s = "request code: " + requestCode + "\n result code: " + resultCode + "\ndata: " + data;
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        }*/
    }

    private void updatePaymentResponse(ArrayList<String> list) {
        String txnId;
        String responsecode;
        String appRefNum;
        String staus;
        String res = list.get(0);
        if (null != res) {
            if (res.length() >= 2) {
                String respones1[] = res.split("&");

                String respones2[] = respones1[0].split("=");
                txnId = respones2[1];

                String respones21[] = respones1[1].split("=");
                responsecode = respones21[1];

                String respones22[] = respones1[2].split("=");
                appRefNum = respones22[1];

                String respones23[] = respones1[3].split("=");
                staus = respones23[1];

                String amount = editText_donation.getText().toString();

                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("txnId", txnId);
                paramMap.put("responsecode", responsecode);
                paramMap.put("appRefNum", appRefNum);

                paramMap.put("ORDER_ID", txnId);
                paramMap.put("TXN_AMOUNT", amount);
                paramMap.put("CUST_ID", user.getUid());

                if (staus.equalsIgnoreCase("success")) {
                    updateTransactionResponse(staus, paramMap, amount);
                } else {
                    progressDialog.show();
                    Toast.makeText(this, "Transaction failed", Toast.LENGTH_SHORT).show();
                    updateTransactionResponse(staus, paramMap, amount);
                }
            }
        }

    }

    private void startPayTmTransaction(String amount) {
        if (ContextCompat.checkSelfPermission(DonateActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            progressDialog.dismiss();
            Toast.makeText(this, "permission required", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(DonateActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        } else {
            generateCheckSum(amount);
        }
    }

    private void generateCheckSum(final String amount) {
        final String orderId = generateString();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://us-central1-cmafirebaseproject-d7ae8.cloudfunctions.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitService uploadInterFace = retrofit.create(RetrofitService.class);
        Call<CheckSumModel> call = uploadInterFace.getCheckSum(orderId, user.getUid(), amount);

        call.enqueue(new Callback<CheckSumModel>() {
            @Override
            public void onResponse(Call<CheckSumModel> call, Response<CheckSumModel> response) {
                if (response.isSuccessful()) {
                    progressDialog.dismiss();
                    CheckSumModel sumModel = response.body();
                    try {
                        String checkSum = sumModel.getChecksum();
                        initPayTmTransaction(checkSum, orderId, user.getUid(), amount);

                    } catch (Exception e) {
                        Toast.makeText(DonateActivity.this, "no checkSum Found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<CheckSumModel> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(DonateActivity.this, "try again", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void initPayTmTransaction(String sum, String id, String custId, final String amount) {
        String call_back_url = "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";
        // PaytmPGService Service = PaytmPGService.getStagingService("");
        PaytmPGService Service = PaytmPGService.getProductionService();
        final HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("MID", "fgFdyz89468312808479");
        paramMap.put("ORDER_ID", id);
        paramMap.put("CUST_ID", custId);
        paramMap.put("CHANNEL_ID", "WAP");
        paramMap.put("TXN_AMOUNT", amount);
        paramMap.put("WEBSITE", "DEFAULT");
        paramMap.put("INDUSTRY_TYPE_ID", "Retail");
        paramMap.put("CALLBACK_URL", call_back_url);
        paramMap.put("CHECKSUMHASH", sum);
         /* paramMap.put( "MOBILE_NO", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() );
        paramMap.put( "EMAIL", "aamirr.1232@gmail.com" );*/
        PaytmOrder Order = new PaytmOrder(paramMap);

        Service.initialize(Order, null);


        Service.startPaymentTransaction(this, true, true, new PaytmPaymentTransactionCallback() {
            //Call Backs
            public void someUIErrorOccurred(String inErrorMessage) {
                //  Display the error message as below
                Toast.makeText(getApplicationContext(), "UI Error " + inErrorMessage, Toast.LENGTH_LONG).show();
            }

            public void onTransactionResponse(Bundle inResponse) {
                progressDialog.show();
                String status = inResponse.getString("STATUS");
                updateTransactionResponse(status, paramMap, amount);

            }

            public void networkNotAvailable() {
                // Display the message as below
                Toast.makeText(getApplicationContext(), "Network connection error: Check your internet connectivity", Toast.LENGTH_LONG).show();
            }

            public void clientAuthenticationFailed(String inErrorMessage) {
                //Display the message as below
                Toast.makeText(getApplicationContext(), "Authentication failed: Server error" + inErrorMessage, Toast.LENGTH_LONG).show();
            }

            public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                //Display the message as below
                Toast.makeText(getApplicationContext(), "Unable to load webpage " + inErrorMessage, Toast.LENGTH_LONG).show();
            }

            public void onBackPressedCancelTransaction() {
                //Display the message as below
                Toast.makeText(getApplicationContext(), "Transaction cancelled", Toast.LENGTH_LONG).show();

            }

            public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                String status = inResponse.getString("STATUS");
                updateTransactionResponse(status, paramMap, amount);
            }
        });
    }

    private void updateTransactionResponse(final String inResponse, Map<String, Object> paramMap, final String amount) {

        String status;
        if (inResponse.equalsIgnoreCase("success")) {
            status = TRX_STATUS_SUCCESS;
        } else status = TRX_STATUS_FAILED;
        Map<String, Object> map = new HashMap<>();
        map.put(PAYTM_PARAMS, paramMap);
        map.put(TIMESTAMP, System.currentTimeMillis());
        map.put(UID, user.getUid());
        map.put(AMOUNT, amount);
        map.put(TRX_STATUS, status);


        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(DONATIONS).add(map).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(DonateActivity.this, "can't update", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                progressDialog.dismiss();
                if (inResponse.equalsIgnoreCase("success")) {
                    String tittle = "Transaction Successful";
                    String msg = "You have successfully paid ₹ " + amount;
                    int icon = R.drawable.ic_baseline_check_24;
                    showDialog(tittle, msg, icon);
                }
                Toast.makeText(getApplicationContext(), "Payment Transaction response " + inResponse, Toast.LENGTH_LONG).show();
            }
        });


    }

    private void updateTransactionResponse(final String inResponse, HashMap<String, String> paramMap, final String amount) {

        Map<String, Object> map = new HashMap<>();
        map.put(PAYTM_PARAMS, paramMap);
        map.put(TIMESTAMP, System.currentTimeMillis());
        map.put(UID, user.getUid());
        map.put(AMOUNT, amount);
        map.put(TRX_STATUS, inResponse);


        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(DONATIONS).add(map).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(DonateActivity.this, "can't update", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                progressDialog.dismiss();
                if (inResponse.equals("TXN_SUCCESS")) {
                    String tittle = "Transaction Successful";
                    String msg = "You have successfully paid ₹" + amount;
                    int icon = R.drawable.ic_baseline_check_24;
                    showDialog(tittle, msg, icon);
                    //utils.showSuccessDialog(PaymentPage.this);
                }
                Toast.makeText(getApplicationContext(), "Payment Transaction response " + inResponse, Toast.LENGTH_LONG).show();
            }
        });


    }

    private void showDialog(String tittle, String msg, int icon) {
        new TTFancyGifDialog.Builder(DonateActivity.this)
                .setTitle(tittle)
                .setMessage(msg)
                .setPositiveBtnText("View")
                .setPositiveBtnBackground("#22b573")
                .setGifResource(icon)      //pass your gif, png or jpg
                .isCancellable(false)
                .OnPositiveClicked(new TTFancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        startActivity(new Intent(DonateActivity.this, DonateHistoryScreen.class));
                        finish();
                    }
                })
                .build();
    }

    private String generateString() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }
}
