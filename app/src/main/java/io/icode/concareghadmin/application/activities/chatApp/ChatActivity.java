package io.icode.concareghadmin.application.activities.chatApp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.activities.AdminLoginActivity;
import io.icode.concareghadmin.application.activities.adapters.ViewPagerAdapter;
import io.icode.concareghadmin.application.activities.constants.Constants;
import io.icode.concareghadmin.application.activities.fragments.ChatsFragment;
import io.icode.concareghadmin.application.activities.fragments.GroupsFragment;
import io.icode.concareghadmin.application.activities.fragments.UsersFragment;
import io.icode.concareghadmin.application.activities.models.Admin;
import io.icode.concareghadmin.application.activities.models.Chats;
import io.icode.concareghadmin.application.activities.models.Groups;
import io.icode.concareghadmin.application.activities.models.Users;
import io.icode.concareghadmin.application.activities.notifications.Token;
import maes.tech.intentanim.CustomIntent;

@SuppressWarnings("ALL")
public class ChatActivity extends AppCompatActivity {

    AppBarLayout appBarLayout;

    Toolbar toolbar;

    RelativeLayout internetConnection;

    ConstraintLayout constraintLayout;

    TextView tv_Retry;

    CircleImageView profile_image;
    TextView username;

    Users users;

    Admin admin;

    Groups groups;

    //check if internet is available or not on phone
    boolean isConnected = false;

    ProgressDialog progressDialog;

    DatabaseReference adminRef;

    DatabaseReference chatRef;

    DatabaseReference groupRef;

    ValueEventListener adminEventListener;

    ValueEventListener chatEventListener;

    ValueEventListener groupEventListener;

    String admin_uid;

    // variable for duration of snackbar and toast
    private static final int DURATION_LONG = 5000;

    private static final int DURATION_SHORT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        appBarLayout = findViewById(R.id.appBarLayout);

        internetConnection = findViewById(R.id.no_internet_connection);

        constraintLayout = findViewById(R.id.constraintLayout);

        tv_Retry = findViewById(R.id.tv_Retry);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        //toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);

        profile_image = findViewById(R.id.profile_image);

        username =  findViewById(R.id.username);

        admin = new Admin();

        users = new Users();

        groups = new Groups();

        groupRef = FirebaseDatabase.getInstance().getReference(Constants.GROUP_REF);

        // getting the uid of the admin stored in sharePreference
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        admin_uid = preferences.getString("uid","");

        // method call to check if internet connection is enabled
        isInternetConnnectionEnabled();

        // refresh page if internet is enabled or not
        onTapToRetry();

        // update device token
        updateToken(FirebaseInstanceId.getInstance().getToken());

        // method call to change ProgressDialog style based on the android version of user's phone
        changeProgressDialogBackground();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_admin,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.menu_create_group:

                // method call to signout admin
                requestNewGroup();

                break;

            case R.id.menu_sign_out:

                // method call to signout admin
               signOutAdmin();

                break;

            case R.id.menu_exit:

                // finish activity
                finish();

                break;
        }

       return super.onOptionsItemSelected(item);
    }

    public void onTapToRetry(){
        internetConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // method call to refresh page to see if connecting is enabled or not
                isInternetConnnectionEnabled();
            }
        });
    }

    // method to check if internet connection is enabled
    private void isInternetConnnectionEnabled(){

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            //we are connected to a network
            isConnected = true;

            appBarLayout.setVisibility(View.VISIBLE);

            // sets visibility to visible if there is  no internet connection
            internetConnection.setVisibility(View.GONE);

            adminRef = FirebaseDatabase.getInstance().getReference(Constants.ADMIN_REF);

            adminEventListener = adminRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        Admin admin = snapshot.getValue(Admin.class);
                        assert admin != null;
                        username.setText(admin.getUsername());

                        //text if users's imageUrl is equal to default
                        if (admin.getImageUrl() == null) {
                            //profile_image.setImageResource(R.drawable.app_logo);
                            Glide.with(ChatActivity.this).load(R.drawable.app_logo).into(profile_image);
                        } else {
                            // load users's Image Url
                            Glide.with(ChatActivity.this).load(admin.getImageUrl()).into(profile_image);
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // display message if error occurs
                    Toast.makeText(ChatActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
                }
            });

            // getting reference to the views
            final TabLayout tabLayout =  findViewById(R.id.tab_layout);
            final ViewPager viewPager = findViewById(R.id.view_pager);

            // Checks for incoming messages and counts them to be displays together in the chats fragments
            chatRef = FirebaseDatabase.getInstance().getReference(Constants.CHAT_REF);

            chatEventListener = chatRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // instance of the ViewPagerAdapter class
                    ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                    // variable to count the number of unread messages
                    int unreadMessages = 0;
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Chats chats = snapshot.getValue(Chats.class);
                        assert chats != null;
                        if(chats.getReceiver().equals(admin.getAdminUid()) && !chats.isIsseen()){
                            unreadMessages++;
                        }
                    }

                    if(unreadMessages == 0){
                        // adds ChatsFragment and AdminFragment to the viewPager
                        viewPagerAdapter.addFragment(new ChatsFragment(), getString(R.string.text_chats));
                    }
                    else{
                        // adds ChatsFragment and AdminFragment to the viewPager + count of unread messages
                        viewPagerAdapter.addFragment(new ChatsFragment(), "("+unreadMessages+") Chats");
                    }

                    // adds UsersFragment and GroupsFragment to the viewPager
                    viewPagerAdapter.addFragment(new GroupsFragment(),getString(R.string.text_groups));
                    viewPagerAdapter.addFragment(new UsersFragment(), getString(R.string.text_users));
                    //Sets Adapter view of the ViewPager
                    viewPager.setAdapter(viewPagerAdapter);

                    //sets tablayout with viewPager
                    tabLayout.setupWithViewPager(viewPager);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ChatActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
                }
            });


        }
        // else condition
        else{

            isConnected = false;

            appBarLayout.setVisibility(View.GONE);

            // sets visibility to visible if there is  no internet connection
            internetConnection.setVisibility(View.VISIBLE);
        }

    }


    // method to update device token
    private void updateToken(final String token){

        adminRef = FirebaseDatabase.getInstance().getReference(Constants.ADMIN_REF);

        // getting an instance of currentAdmin
         adminRef.child(admin_uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Admin currentAdmin = dataSnapshot.getValue(Admin.class);

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.TOKENS_REF);
                Token token1 = new Token(token);
                reference.child(currentAdmin.getAdminUid()).setValue(token1);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    // request to create new group
    private void requestNewGroup(){

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        builder.setView(dialogView);

        final EditText editTextGroupName = dialogView.findViewById(R.id.editTextGroupName);

        builder.setTitle(R.string.text_group_name);
        builder.setMessage(R.string.enter_group_name);

        // onclick listener for positive  button
        builder.setPositiveButton(R.string.text_create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // getting text from field
                String groupName = editTextGroupName.getText().toString().trim();

                if(TextUtils.isEmpty(groupName)){
                    // display hint to user
                    Toast.makeText(ChatActivity.this, R.string.error_empty_group_name, Toast.LENGTH_SHORT).show();
                }
                else {
                    // create group
                    createNewGroup(groupName);
                }

            }
        });


        // onclick listener for negative button
        builder.setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // dismiss / hides the dialog
                dialog.cancel();
            }
        });

        // display the alertDialog
        builder.show();

    }

    // method to create group in database
    private void createNewGroup(final String groupName){

        // checks if group already exist
        groupRef.child(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    // display hint if group already exist
                    Snackbar.make(constraintLayout,groupName
                            + " group already exist . Please create a group with a different name."
                            + " E.g " + groupName + " 01 , 02...",DURATION_LONG).show();

                }

                else {

                    // open users activity so admin can add users

                    Intent intent = new Intent(ChatActivity.this, AddUsersActivity.class);

                    intent.putExtra("group_name",groupName);

                    startActivity(intent);

                    // adds custom animation
                    CustomIntent.customType(ChatActivity.this, getString(R.string.left_to_right));

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display an error message if group is not created succcessfully
                Toast.makeText(ChatActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void signOutAdmin(){

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle(getString(R.string.text_sign_out));
        builder.setMessage(getString(R.string.sign_out_msg));

        builder.setPositiveButton(getString(R.string.text_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // show dialog
                progressDialog.show();

                // delays the running of the ProgressBar for 3 secs
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // dismiss dialog
                        progressDialog.dismiss();

                        // log admin out of the system and clear all stored data
                        clearEmail(ChatActivity.this);

                        // send admin to login activity
                        startActivity(new Intent(ChatActivity.this, AdminLoginActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                        CustomIntent.customType(ChatActivity.this, getString(R.string.fadein_to_fadeout));

                        finish();

                    }
                },3000);

            }
        });

        builder.setNegativeButton(getString(R.string.text_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    // method to clear sharePreference when admin log outs
    private void clearEmail(Context ctx){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        editor.clear(); // clear all stored data (email)
        editor.commit();
    }

    private void status(String status){

        // admin uid is not null
        if(admin_uid != null){
            adminRef = FirebaseDatabase.getInstance().getReference(Constants.ADMIN_REF)
                    .child(admin_uid);

            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("status",status);
            adminRef.updateChildren(hashMap);
        }


    }

    // setting status to "online" when activity is resumed
    @Override
    protected void onStart() {
        super.onStart();
        status("online");
        // update user's device token
        updateToken(FirebaseInstanceId.getInstance().getToken());

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        status("online");
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        // update user's device token
        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(adminRef != null && chatRef != null){
            adminRef.removeEventListener(adminEventListener);
            chatRef.removeEventListener(chatEventListener);
        }

    }

    // method to change ProgressDialog style based on the android version of user's phone
    private void changeProgressDialogBackground(){

        // if the build sdk version >= android 5.0
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            //sets the background color according to android version
            progressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("");
            progressDialog.setMessage(getString(R.string.signing_out_text));
        }
        //else do this
        else{
            //sets the background color according to android version
            progressDialog = new ProgressDialog(this, ProgressDialog.THEME_HOLO_LIGHT);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("");
            progressDialog.setMessage(getString(R.string.signing_out_text));
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // finish activity and closes app
        finish();
        //closeApp();
    }

    // method to close app
    private void closeApp(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        },1000);

    }

}
