package io.icode.concareghadmin.application.activities.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.constants.Constants;
import io.icode.concareghadmin.application.activities.models.Admin;
import io.icode.concareghadmin.application.activities.notifications.Token;
import io.icode.concareghadmin.application.activities.prefs.SavedSharePreference;
import io.icode.concareghadmin.application.activities.chatApp.ChatActivity;
import maes.tech.intentanim.CustomIntent;

@SuppressWarnings("ALL")
public class SplashScreenActivity extends AppCompatActivity {

    private TextView app_title;

    private TextView watermark;

    private final int SPLASH_SCREEN_DISPLAY_TIME = 4000;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    DatabaseReference adminRef;

    ProgressBar progressBar;

    ProgressBar progressBar1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // method call to check google play services is available on device
        checkPlayServices();

        // getting reference to the first Top progressBar
        /*progressBar = findViewById(R.id.progressBar);

        // changes color of progressBar to you desired color
        progressBar.getIndeterminateDrawable().setColorFilter(0xff676767,PorterDuff.Mode.MULTIPLY);

        progressBar1 = findViewById(R.id.progressBar1);

        // changes color of progressBar to you desired color
        progressBar1.getIndeterminateDrawable().setColorFilter(0xff676767,PorterDuff.Mode.MULTIPLY);

        // displays the progressBar
        progressBar.setVisibility(View.VISIBLE);

        // displays the progressBar
        progressBar1.setVisibility(View.VISIBLE);
        */

        //method call to update device token
        updateToken(FirebaseInstanceId.getInstance().getToken());

        // method call
        runAnimation();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // update user's device token
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    @Override
    protected void onPause() {
        super.onPause();
        // update user's device token
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // update user's device token
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    // method to update device token
    private void updateToken(final String token){

        adminRef = FirebaseDatabase.getInstance().getReference(Constants.ADMIN_REF);

        // getting an instance of currentAdmin
        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Admin currentAdmin = snapshot.getValue(Admin.class);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.TOKENS_REF);
                    Token token1 = new Token(token);
                    reference.child(currentAdmin.getAdminUid()).setValue(token1);

                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    // checks for availability of Google Play Services
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                //Log.i(TAG, "This device is not supported.");
                // display a toast
                Toast.makeText(SplashScreenActivity.this,"This device is not supported.",Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    protected void onStart() {
        super.onStart();

        // checks if user is currently logged in
        if(SavedSharePreference.getEmail(SplashScreenActivity.this).length() == 0){
            // open splash screen first
            splashScreen();
        }
        else{

            // start the activity
            startActivity(new Intent(SplashScreenActivity.this,ChatActivity.class));

            // Add a custom animation ot the activity
            CustomIntent.customType(SplashScreenActivity.this,getString(R.string.fadein_to_fadeout));

            // finish the activity
            finish();

            // update user's device token
            updateToken(FirebaseInstanceId.getInstance().getToken());

        }
    }

    //class to the handle the splash screen activity
    public void splashScreen() {

        Thread timer = new Thread() {
            @Override
            public void run() {
                try {

                    sleep(SPLASH_SCREEN_DISPLAY_TIME);

                    //Creates and start the intent of the next activity
                    startActivity(new Intent(SplashScreenActivity.this, AdminLoginActivity.class));

                    // Add a custom animation ot the activity
                    CustomIntent.customType(SplashScreenActivity.this,getString(R.string.fadein_to_fadeout));

                    // finishes the activity
                    finish(); // this prevents the app from going back to the splash screen

                    super.run();
                }
                catch (InterruptedException e) {
                    // displays a toast
                    Toast.makeText(SplashScreenActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        };
        //starts the timer
        timer.start();
    }

    // method to set animation on textViews
    private  void runAnimation(){

        // setting animation for the App Title on the splashScreen
        TextView app_title = findViewById(R.id.splash_screen_text);

        //add an animation using the YoYo Library
        YoYo.with(Techniques.SlideInLeft)
                .duration(1000)
                .playOn(app_title);


        // setting animation for the App watermark on the splashScreen
        TextView watermark = findViewById(R.id.water_mark);

        //add an animation using the YoYo Library
        YoYo.with(Techniques.FadeInUp)
                .duration(1000)
                .repeat(1)
                .playOn(watermark);


    }

    @Override
    public void finish() {
        super.finish();
        // Add a custom animation ot the activity
        CustomIntent.customType(SplashScreenActivity.this,getString(R.string.fadein_to_fadeout));
    }
}
