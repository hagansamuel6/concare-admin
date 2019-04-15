package io.icode.concareghadmin.application.activities.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import io.icode.concareghadmin.application.R;
import maes.tech.intentanim.CustomIntent;

public class ResetPasswordActivity extends AppCompatActivity {

    // class variables
    ProgressBar progressBar;

    CoordinatorLayout coordinatorLayout;

    FirebaseAuth mAuth;

    private EditText editTextEmail;

    // object creation
    private Animation shake;

    private Button btn_reset_password;
    private Button btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // firebase instance
        mAuth = FirebaseAuth.getInstance();

        // getting references to the views
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        editTextEmail = findViewById(R.id.email);

        btn_reset_password = findViewById(R.id.btn_reset_password);

        btn_back = findViewById(R.id.btn_back);

        // getting reference to the views
        progressBar = findViewById(R.id.progressBar);

        shake = AnimationUtils.loadAnimation(ResetPasswordActivity.this, R.anim.anim_shake);


    }

    @Override
    protected void onStart(){
        super.onStart();
        // checks if user is not currently logged in
        if(mAuth.getCurrentUser() == null){
            // do nothing
        }

    }

    // method to send user back to login Activity
    public void goBackButton(View view) {

        // add an animation to anim_shake the button
        btn_back.setAnimation(shake);

        // starts the activity
        startActivity(new Intent(ResetPasswordActivity.this,AdminLoginActivity.class));

        // Add a custom animation ot the activity
        CustomIntent.customType(ResetPasswordActivity.this,"fadein-to-fadeout");

        // finish the activity
        finish();

    }

    // onClick listener method for reset password Button
    public void resetPasswordButton(View view) {

        String email = editTextEmail.getText().toString().trim();

        if(email.isEmpty()){
            // set animation and error
            editTextEmail.setAnimation(shake);
            editTextEmail.setError(getString(R.string.email_registered));
            editTextEmail.requestFocus();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            // set animation and error
            editTextEmail.setAnimation(shake);
            editTextEmail.setError(getString(R.string.email_valid_registered));
            editTextEmail.requestFocus();
        }
        else{
            //method call
            resetPassword();
        }

    }

    // method to reset password
    private void resetPassword(){

        // displays the progressBar
        progressBar.setVisibility(View.VISIBLE);

        // getting text from user
        String email = editTextEmail.getText().toString().trim();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Snackbar.make(coordinatorLayout,getString(R.string.reset_password_instruction)
                                    ,Snackbar.LENGTH_LONG).show();
                        }
                        else{
                            // displays an error message
                            Snackbar.make(coordinatorLayout,task.getException().getMessage(),Snackbar.LENGTH_LONG).show();
                        }

                        // dismiss the progressBar
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void finish() {
        super.finish();
        // Add a custom animation ot the activity
        CustomIntent.customType(ResetPasswordActivity.this,"fadein-to-fadeout");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // open the LoginActivity
        startActivity(new Intent(ResetPasswordActivity.this,AdminLoginActivity.class));

        // Add a custom animation ot the activity
        CustomIntent.customType(ResetPasswordActivity.this,"fadein-to-fadeout");

        // finishes the activity
        finish();

    }
}
