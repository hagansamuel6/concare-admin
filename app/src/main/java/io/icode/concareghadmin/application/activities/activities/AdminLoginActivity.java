package io.icode.concareghadmin.application.activities.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.prefs.SavedSharePreference;
import io.icode.concareghadmin.application.activities.chatApp.ChatActivity;
import io.icode.concareghadmin.application.activities.models.Admin;
import maes.tech.intentanim.CustomIntent;

import static io.icode.concareghadmin.application.activities.constants.Constants.ADMIN_REF;

public class AdminLoginActivity extends AppCompatActivity {

    // variable to store Decryption algorithm name
    String AES = "AES";

    // variable to store encrypted password
    String decryptedPassword;

    ProgressBar progressBar;

    private EditText editTextEmail;
    private EditText editTextPassword;

    Button forgot_password;
    Button buttonLogin;
    Button buttonSignUpLink;

    Admin admin;

    DatabaseReference adminRef;

    private CardView my_card;

    private CircleImageView app_logo;

    FirebaseAuth mAuth;

    private RelativeLayout relativeLayout;

    private Animation shake;

    private boolean isCircularImageViewClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        relativeLayout = findViewById(R.id.relativeLayout);

        admin = new Admin();

        adminRef = FirebaseDatabase.getInstance().getReference(ADMIN_REF);

        app_logo = findViewById(R.id.app_logo);

        // initialization of the objects of the views
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        my_card = findViewById(R.id.login_cardView);

        // getting the ids of the views
        forgot_password = findViewById(R.id.forgot_password);
        buttonLogin = findViewById(R.id.buttonLogin);
        //buttonSignUpLink = findViewById(R.id.buttonSignUpLink);

        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        // animation to anim_shake button
        shake = AnimationUtils.loadAnimation(AdminLoginActivity.this, R.anim.anim_shake);

        // animation to bounce  App logo on Login screen
        bounce_views();

        // method call to add animation to card
        onCardViewClickAnim();

        // method call
        animateLogo();

    }

    @Override
    protected void onStart(){
        super.onStart();
        // do nothing
    }

    // method to animate the app logo
    public void animateLogo(){
        // scales the image in and out
        app_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // instance of the animation class
                Animation scale_image = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim_scale_imageview);
                app_logo.clearAnimation();
                app_logo.startAnimation(scale_image);
            }
        });
    }

    // method to animate the app logo
    private void bounce_views(){

        // animation to bounce image
        //YoYo.with(Techniques.ZoomIn).repeat(2).playOn(app_logo);

        // bounce the Login Button
        YoYo.with(Techniques.Shake)
                .repeat(1).playOn(my_card);

    }

    // animation loaded when cardView is clicked
    private void onCardViewClickAnim(){

        my_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // adds a waving animation to card
                YoYo.with(Techniques.Wave).playOn(my_card);

            }
        });

    }

    //onLoginButtonClick method
    public void onLoginButtonClick(View view){

        //gets text from the editTExt fields
        String _email = editTextEmail.getText().toString().trim();
        String _password = editTextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(_email)){
            editTextEmail.clearAnimation();
            editTextEmail.startAnimation(shake);
            editTextEmail.setError(getString(R.string.error_empty_email));
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(_email).matches()){
            editTextEmail.clearAnimation();
            editTextEmail.startAnimation(shake);
            editTextEmail.setError(getString(R.string.email_invalid));
        }
        else if(TextUtils.isEmpty(_password)){
            editTextPassword.clearAnimation();
            editTextPassword.startAnimation(shake);
            editTextPassword.setError(getString(R.string.error_empty_password));
            editTextPassword.requestFocus();
        }
        else if(_password.length() < 6 ){
            editTextPassword.clearAnimation();
            editTextPassword.startAnimation(shake);
            editTextPassword.setError(getString(R.string.error_password_length));
            editTextPassword.requestFocus();
        }
        else{
            // a call to the loginUser method
            loginAdmin();
        }
    }

    // Method to handle user login
    private void loginAdmin() {

        progressBar.setVisibility(View.VISIBLE);

        final String email = editTextEmail.getText().toString();
        final String password = editTextPassword.getText().toString();

       adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

               for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                   Admin admin = snapshot.getValue(Admin.class);

                   assert admin != null;
                   String adminEmail = admin.getEmail();
                   String encryptedPassword = admin.getPassword();

                   // decrypt password
                   try {
                       decryptedPassword = decryptPassword(encryptedPassword,email);
                   } catch (Exception e) {
                       e.printStackTrace();
                   }

                   // compares decrypted password to the current password entered and
                   if(password.equals(decryptedPassword) && email.equals(adminEmail)){

                       // dismiss the dialog
                       progressBar.setVisibility(View.GONE);

                       // display a successful login message
                       Toast.makeText(AdminLoginActivity.this,getString(R.string.login_successful),Toast.LENGTH_SHORT).show();

                       // storing email in shared preference
                       SharedPreferences.Editor editor = PreferenceManager
                               .getDefaultSharedPreferences(AdminLoginActivity.this).edit();
                       editor.putString("email", email);
                       editor.putString("uid", admin.getAdminUid());
                       editor.apply();

                       // setting uid and email to getter method in sharedPreferences
                       SavedSharePreference.setEmail(AdminLoginActivity.this, email);
                       SavedSharePreference.setUid(AdminLoginActivity.this, admin.getAdminUid());

                       // clear the text fields
                       clearTextFields();

                       // start the home activity
                       startActivity(new Intent(AdminLoginActivity.this,ChatActivity.class));

                       // Add a custom animation ot the activity
                       CustomIntent.customType(AdminLoginActivity.this,getString(R.string.fadein_to_fadeout));

                       // finishes this activity(prevents user from going back to this activity when back button is pressed)
                       finish();

                   }
                   else{

                       progressBar.setVisibility(View.GONE);
                       // display a message if there is an error
                       Snackbar.make(relativeLayout,getString(R.string.incorrect_pass_email),Snackbar.LENGTH_LONG).show();
                       //Toast.makeText(AdminLoginActivity.this,R.string.incorrect_pass_email,Toast.LENGTH_LONG).show();

                   }

               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {
               // display error message
               Snackbar.make(relativeLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
           }
       });

    }


    // method to decrypt password
    private String decryptPassword(String password, String email) throws Exception {
        SecretKeySpec key  = generateKey(email);
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.DECRYPT_MODE,key);
        byte[] decodedValue = Base64.decode(password,Base64.DEFAULT);
        byte[] decVal = c.doFinal(decodedValue);
        String decryptedValue = new String(decVal);
        return decryptedValue;

    }

    private SecretKeySpec generateKey(String password) throws Exception{
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0 , bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec keySpec = new SecretKeySpec(key,"AES");
        return keySpec;
    }

    // Link to the signUp Interface
    /*public void onSignUpLinkClick(View view){

        // creates an instance of the intent class and opens the signUpctivity
        startActivity(new Intent(AdminLoginActivity.this,AdminSignUpActivity.class));

        // Add a custom animation ot the activity
        CustomIntent.customType(AdminLoginActivity.this,"fadein-to-fadeout");

        // finish the activity
        finish();
    }
    */


    // Method to clear text fields
    public void clearTextFields(){
        editTextEmail.setText(null);
        editTextPassword.setText(null);
    }

    // Forgot password method
    public void onForgotPasswordClick(View view) {

        // shakes the button when clicked
        /*YoYo.with(Techniques.FlipOutX).playOn(forgot_password);

        // start the ResetPassword Activity
        startActivity(new Intent(AdminLoginActivity.this,ResetPasswordActivity.class));

        // Add a custom animation ot the activity
        CustomIntent.customType(AdminLoginActivity.this,"fadein-to-fadeout");

        // finish the activity
        finish();
        */

        Snackbar.make(view,getString(R.string.reset_pass_msg),Snackbar.LENGTH_LONG).show();

    }


    @Override
    public void finish() {
        super.finish();
        // Add a custom animation ot the activity
        CustomIntent.customType(AdminLoginActivity.this,getString(R.string.fadein_to_fadeout));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);

        // finishes the activity
        finish();

    }
}
