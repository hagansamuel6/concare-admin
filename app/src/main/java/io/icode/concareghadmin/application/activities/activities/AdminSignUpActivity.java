package io.icode.concareghadmin.application.activities.activities;

import android.content.Intent;
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

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.models.Admin;
import maes.tech.intentanim.CustomIntent;

public class AdminSignUpActivity extends AppCompatActivity {

    // variable to store Decryption algorithm name
    String AES = "AES";

    // variable to store encrypted password
    String encryptedPassword;

    ProgressBar progressBar;

    EditText editTextUsername;
    EditText editTextEmail;
    EditText editTextPassword;

    Button forgot_password;
    Button buttonSignUp;

    Admin admin;

    DatabaseReference adminRef;

    private CardView my_card;

    private CircleImageView app_logo;

    RelativeLayout relativeLayout;

    private Animation shake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_sign_up);

        admin = new Admin();

        adminRef = FirebaseDatabase.getInstance().getReference("Admin");

        app_logo = findViewById(R.id.app_logo);

        // initialization of the objects of the views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        my_card = findViewById(R.id.login_cardView);

        // getting the ids of the views
        forgot_password = findViewById(R.id.forgot_password);

        buttonSignUp = findViewById(R.id.buttonSignUp);
        //buttonSignUpLink = findViewById(R.id.buttonSignUpLink);

        relativeLayout = findViewById(R.id.relativeLayout);

        progressBar = findViewById(R.id.progressBar);

        // animation to anim_shake button
        shake = AnimationUtils.loadAnimation(AdminSignUpActivity.this, R.anim.anim_shake);

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
    public void onSignUpButtonClick(View view){

        //gets text from the editTExt fields
        String _username = editTextUsername.getText().toString().trim();
        String _email = editTextEmail.getText().toString().trim();
        String _password = editTextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(_username)){
            editTextUsername.clearAnimation();
            editTextUsername.startAnimation(shake);
            editTextUsername.setError(getString(R.string.error_empty_email));
        }
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
            // a call to the signUp method
            signUpAdmin();
        }
    }

    // Method to handle user login
    public void signUpAdmin(){

        // shakes the button
        buttonSignUp.clearAnimation();
        buttonSignUp.startAnimation(shake);

        // shows the progressBar
        progressBar.setVisibility(View.VISIBLE);

        //gets text from the editTExt fields
        final String _username = editTextEmail.getText().toString().trim();
        final String _email = editTextEmail.getText().toString().trim();
        final String _password = editTextPassword.getText().toString().trim();

        try {
            encryptedPassword = encryptPassword(_password,_email);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String uid = adminRef.push().getKey();

        admin.setAdminUid(uid);
        admin.setUsername(_username);
        admin.setEmail(_email);
        admin.setPassword(encryptedPassword);
        admin.setStatus("offline");

        //String uid = reference.push().getKey();

        adminRef.child(uid).setValue(admin).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    Snackbar.make(relativeLayout,getString(R.string.sign_up_successful), Snackbar.LENGTH_LONG).show();

                }
                else{

                    Snackbar.make(relativeLayout, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();

                }

                // sets visibility to gone
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    // method to encrypt key
    private String encryptPassword(String password, String email) throws Exception{
        SecretKeySpec key = generateKey(email);
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.ENCRYPT_MODE,key);
        byte[] encVal = c.doFinal(password.getBytes());
        String encryptedValue = Base64.encodeToString(encVal,Base64.DEFAULT);
        return encryptedValue;
    }

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // creates an instance of the intent class and opens the signUpctivity
        startActivity(new Intent(AdminSignUpActivity.this,AdminLoginActivity.class));

        // Add a custom animation ot the activity
        CustomIntent.customType(AdminSignUpActivity.this,"fadein-to-fadeout");

        // finish the activity
        finish();
    }
}
