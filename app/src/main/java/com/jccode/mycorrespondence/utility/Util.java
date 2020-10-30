package com.jccode.mycorrespondence.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jccode.mycorrespondence.ChatUsScreen;
import com.jccode.mycorrespondence.DocumentDetail;
import com.jccode.mycorrespondence.MyFirebaseMessagingService;
import com.jccode.mycorrespondence.R;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.jccode.mycorrespondence.Offline.DEMO_CHANNEL_ID;

public class Util {

    public static final String USER_TYPE = "type";
    public static final String DRAFTS = "Drafts";
    public static final String USERS = "Users";
    public static final String TITLE = "title";
    public static final String SUBJECT = "subject";
    public static final String SHARE_TO_USER_IMAGE = "shareToUserImage";
    public static final String SHARE_TO_USER_NAME = "shareToUserName";
    public static final String FROM = "From";
    public static final String DESCRIPTION = "description";
    public static final String ID = "id";
    public static final String SUB_DOCUMENT_QUERY = "Sub_Document";
    public static final String CORRESPONDENCE_QUERY = "Correspondence_Data";
    public static final String IS_ACTIVE = "isActive";
    public static final String FILE_NAME = "file_name";
    public static final String ATTACHMENT = "Attachment";
    public static final String REPLY = "Reply";
    public static final String TIMESTAMP = "timestamp";
    public static final String SHARED_TO = "shared_to";
    public static final String SHARED_BY = "shared_by";
    public static final String F_NAME = "fname";
    public static final String L_NAME = "lname";
    public static final String ADDRESS = "add";
    public static final String PERMANENT_ADDRESS = "per_add";
    public static final String EMAIL = "email";
    public static final String BRANCH = "branch";
    public static final String GENDER = "gender";
    public static final String MOBILE = "mobile";
    public static final String IMAGE = "image";
    public static final String NAME = "name";
    public static final String POST = "post";
    public static final String DOCUMENT = "Document";
    public static final String UID = "uid";
    public static final String USERNAME = "username";
    public static final String DATE = "date";
    public static final String IMAGE_ATTACHMENT = "Image_Attachment";
    public static final String OTHER_ATTACHMENT = "Other Attachment";
    public static final String PDF_ATTACHMENT = "PDF_Attachment";
    public static final String PAYTM_PARAMS = "PAYTM PARAMS";
    public static final String TRX_STATUS = "trx_status";
    public static final String DONATIONS = "Donations";
    public static final String AMOUNT = "amount";
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String USER_NAME = "user_name";
    public static final String REMINDER = "Reminder";
    public static final String USER_TOKEN = "token_id";
    public static final String TRX_STATUS_SUCCESS = "TXN_SUCCESS";
    public static final String TRX_STATUS_FAILED = "TXN_FAILURE";
    public static final String LETTER_NUMBER = "Letter_Number";
    public static final String DEPARTMENT = "Department";
    public static final String TYPE = "Type";
    public static final String REMARK = "Remark";
    public static final String SIGNATURE = "Signature";
    public static final String COMPOSE = "Compose";
    private SharedPreferences sharedpreferences;
    public static final String REFERENCE_NUMBER = "reference_number";
    public static final String LETTER_DETAIL = "Letter_Detail";
    public static final String SENDER_DETAIL = "Sender_Detail";
    public static final String DOCUMENT_NAME = "Document_name";
    public static final String CORRESPONDENCE_ID = "Correspondence_id";
    public static final String MY_REMINDER = "My_Reminder";
    public static final String CHAT_US_QUERY = "Chat_us";
    public static final String MESSAGE = "Message";
    public static final String CHAT = "Chats";
    public static final String TAG = "tag";
    public static final String YES = "Yes";
    public static final String CANCEL = "Cancel";
    public static final String UN_REPLIED = "Un-Replied";
    public static final String PARTIALLY_REPLIED = "Partially Replied";
    public static final String REPLIED = "Replied";
    public static final CharSequence DELETE_LETTER = "Delete Letter";
    public static final String NO_TAG = "NO TAG";
    public static final String ADMIN_NUMBER = "number";
    public static final String IS_ADMIN = "Is_Admin";
    public static final String LAST_MSG = "last_msg";
    public static final String IS_SEEN = "is_seen";
    public static final String FILED = "filed";
    public static final String SORT_TYPE = "Sort Type";
    public static final String VIEW_SHARED_CORRESPONDENCE = "Correspondence\nshared by me";
    public static final String SHARED_DOCUMENT = "View Shared\nDocuments";
    public static final String VIEW_RECEIVED_CORRESPONDENCE = "Correspondence\nshared to me";
    public static final String SHARED_DOCUMENT_QUERY = "Shared_Documents";
    public static final String SHARE_APP = "Share App";
    public static final String SEARCH_CORRESPONDENCE = "Search\nCorrespondence";
    public static final CharSequence SHARE_DOCUMENTS = "Share Document";
    public static final String SHARE_LIVE_DOCUMENTS = "Share Live\nDocuments";
    public static final CharSequence DELETE_DOCUMENTS = "Delete Document";
    public static final String SHARED_ACTIVITY = "SharedActivity";
    public static final String MAIN_ACTIVITY = "MainActivity";
    public static final String ADD_DOCUMENT = "Add\nDocument";
    public static final String VIEW_DOCUMENT = "View\nDocument";
    public static final String SEARCH_DOCUMENT = "Search\nDocument";
    public static final String DOCUMENT_QUERY = "Document_QUERY";
    public static final String DOC_ID = "Doc_Id";
    public static final String SUB_DOC_ID = "Sub_doc_Id";
    public static final String SHARED_CORRESPONDENCE = "Shared_Correspondence";
    public static final String VIEW_SHARED_DOCUMENT = "Documents\nshared by me";
    public static final String VIEW_LIVE_SHARED_DOCUMENT_BY_ME = "Live Documents\nShared To me";
    public static final String VIEW_LIVE_SHARED_DOCUMENT_TO_ME = "Live Documents\nShared By me";
    public static final String IS_ACTIVE_TO_SHARER = "isActiveToSharer";
    public static final String SHARE_CORRESPONDENCE = "Share Correspondence";
    public static final String DELETE_CORRESPONDENCE = "Delete Correspondence";
    public static final String SHARE_TO = "shareTo";
    public static final String SHARED = "SHARED";
    public static final String RECEIVED = "RECEIVED";
    public static final String VIEW_RECEIVED_SHARED_DOCUMENT = "Documents\nshared to me";


    MyFirebaseMessagingService myFirebaseMessagingService;
    NotificationManagerCompat managerCompat;

    public Util(MyFirebaseMessagingService myFirebaseMessagingService) {
        this.myFirebaseMessagingService = myFirebaseMessagingService;
        managerCompat = NotificationManagerCompat.from(myFirebaseMessagingService);
    }

    public Util() {
    }


    public static String getNameFromURI(Uri uri, Activity activity) {
        Cursor c = activity.getContentResolver().query(uri, null, null, null, null);
        c.moveToFirst();
        return c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
    }

    public static boolean isInternetAvailable(Activity activity) {

        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void addSignature(String signature, Context context) {
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(SIGNATURE, signature);
        editor.commit();
    }

    public String getSignature(Context context) {
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        if (sharedpreferences.contains(SIGNATURE)) {
            return sharedpreferences.getString(SIGNATURE, "");
        } else {
            return null;
        }
    }

    public String getUserName(Context context) {
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        if (sharedpreferences.contains(F_NAME)) {
            return sharedpreferences.getString(F_NAME, "") + " " + sharedpreferences.getString(L_NAME, "");
        } else {
            return null;
        }
    }


    public String getUserProfile(Context context) {
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        if (sharedpreferences.contains(IMAGE)) {
            return sharedpreferences.getString(IMAGE, "");
        } else {
            return null;
        }
    }

    public void saveUserData(Context context, FirebaseUser user) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedpreferences.edit();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            user = FirebaseAuth.getInstance().getCurrentUser();
            firestore.collection(USERS).document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    if (snapshot.exists()) {
                        editor.putString(IMAGE, snapshot.getString(IMAGE));
                        editor.putString(F_NAME, snapshot.getString(F_NAME));
                        editor.putString(L_NAME, snapshot.getString(L_NAME));

                        editor.putString(ADDRESS, snapshot.getString(ADDRESS));
                        editor.putString(PERMANENT_ADDRESS, snapshot.getString(PERMANENT_ADDRESS));
                        editor.putString(EMAIL, snapshot.getString(EMAIL));

                        editor.putString(BRANCH, snapshot.getString(BRANCH));
                        editor.putString(POST, snapshot.getString(POST));
                        editor.putString(GENDER, snapshot.getString(GENDER));

                        editor.putString(USER_TOKEN, snapshot.getString(USER_TOKEN));

                        editor.commit();
                    }

                }
            });
        }

    }

    public void showNotification(String title, String body, String click_action, String notificationId) {

        Intent intent;
        if (click_action.equalsIgnoreCase("com.jccode.mycorrespondence.DonateHistoryScreen")) {
            intent = new Intent(click_action);
        } else if (click_action.equalsIgnoreCase("com.cmaadmin.CorrespondenceActivity")) {
            {
                intent = new Intent(click_action);
                intent.putExtra(TYPE, RECEIVED);
            }
        } else {
            intent = new Intent(click_action);
            intent.putExtra(FROM, "notification");
            intent.putExtra(USER_NAME, "notification");
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(myFirebaseMessagingService, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(myFirebaseMessagingService, DEMO_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .build();

        managerCompat.notify((int) System.currentTimeMillis(), notification);
    }


    public static void showDialog(final Activity activity, String msg, String title) {
        new AlertDialog.Builder(activity)
                .setMessage(msg)
                //.setTitle(title)
                .setPositiveButton("Talk To Us", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        activity.startActivity(new Intent(activity, ChatUsScreen.class));
                    }
                }).show();
    }

}
