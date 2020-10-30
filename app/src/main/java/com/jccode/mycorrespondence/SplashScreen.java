package com.jccode.mycorrespondence;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplashScreen extends AppCompatActivity {
    private static final String TAG = "SplashScreen";
    CountDownTimer myCountdownTimer;
    String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    ImageView img;
    TextView textView;
    Animation aniFade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = (ImageView) findViewById(R.id.imageView4);
        textView = (TextView) findViewById(R.id.textView4);
        aniFade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
    }

    private void showLoginType() {
        showLoginScreen();
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (myCountdownTimer != null) {
            myCountdownTimer.cancel();
        }
    }


    private void updateUI() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            showLoginType();

        } else {
            startActivity(new Intent(this, HomeScreen3.class));
            finish();
        }
    }

    private void showLoginScreen() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Loading");
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.ic_launcher_foreground)
                        .setTheme(R.style.AppTheme_NoActionBar)
                        .build(),
                10);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10) {
            //IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (null != user) {
                    Toast.makeText(SplashScreen.this, "sign in successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SplashScreen.this, HomeScreen3.class));
                    finish();

                }

            } else {
                Log.d(TAG, "LoginFailed");
                finish();
                //Toast.makeText(SplashScreen.this, "sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(SplashScreen.this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(SplashScreen.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (checkPermissions()) {
            myCountdownTimer = new CountDownTimer(3000, 1000) {
                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    updateUI();
                }
            }.start();
            super.onStart();
            img.startAnimation(aniFade);
            textView.startAnimation(aniFade);
        } else {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                myCountdownTimer = new CountDownTimer(3000, 1000) {
                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        updateUI();
                    }
                }.start();
                img.startAnimation(aniFade);
                textView.startAnimation(aniFade);
            }

        }
    }


}