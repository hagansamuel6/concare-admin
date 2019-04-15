package io.icode.concareghadmin.application.activities.chatApp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.adapters.RecyclerViewAdapterAddUsers;
import io.icode.concareghadmin.application.activities.models.Groups;
import io.icode.concareghadmin.application.activities.models.Users;
import maes.tech.intentanim.CustomIntent;

import static android.view.View.GONE;

public class AddUsersActivity extends AppCompatActivity {

    RelativeLayout relativeLayout;

    Toolbar toolbar;

    TextView toolbar_title;

    TextView tv_no_users;

    TextView tv_no_search_results;

    RecyclerView recyclerView;

    ProgressBar progressBar;

    private RecyclerViewAdapterAddUsers adapterUsers;

    private List<Users> mUsers;

    // list to get the ids of selected users from group creating
    private List<String> selectedUserIds;

    DatabaseReference userRef;

    Groups groups;

    DatabaseReference groupRef;

    // material searchView
    MaterialSearchView searchView;

    String admin_uid;

    // getting the groupName passed
    String groupName;

    // string variable to store the time and date at which a group was created
    String currentDate;

    String currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_users);

        relativeLayout = findViewById(R.id.relativeLayout);

        toolbar = findViewById(R.id.toolbar);
        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_no_users = findViewById(R.id.tv_no_users);

        tv_no_search_results = findViewById(R.id.tv_no_search_results);

        mUsers = new ArrayList<>();

        groups = new Groups();

        progressBar = findViewById(R.id.progressBar);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userRef = FirebaseDatabase.getInstance().getReference("Users");

        groupRef = FirebaseDatabase.getInstance().getReference("Groups");

        // getting the uid of the admin stored in shared preference
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        admin_uid = preferences.getString("uid","");

        adapterUsers = new RecyclerViewAdapterAddUsers(this,mUsers,true);

        // getting the list of ids of selected users
        selectedUserIds = adapterUsers.getSelectedUserIds();

        recyclerView.setAdapter(adapterUsers);

        // getting string from intent
        groupName = getIntent().getStringExtra("group_name");

        // method call
        displayUsers();

        // method call to get the current time and data
        getTimeAndDate();

    }

    // message to read the admin from the database
    public  void displayUsers(){

        // display the progressBar
        progressBar.setVisibility(View.VISIBLE);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){

                    // display text if no data exist
                    tv_no_users.setVisibility(View.VISIBLE);


                }
                else{

                    // clear's list
                    mUsers.clear();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        Users users = snapshot.getValue(Users.class);

                        assert users != null;

                        // hides text to be displayed if no data exist
                        tv_no_users.setVisibility(View.GONE);

                        // display recycler view
                        recyclerView.setVisibility(View.VISIBLE);

                        mUsers.add(users);

                    }

                }

                // notifies any data change
                adapterUsers.notifyDataSetChanged();

                // display the progressBar
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                // dismiss the progressBar
                progressBar.setVisibility(View.GONE);

                // display db error message
                Snackbar.make(relativeLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();

            }
        });

    }

    // gets the current date and time
    private void getTimeAndDate(){

        // gets the current date
        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new  SimpleDateFormat("MMM dd,yyyy");
        currentDate = currentDateFormat.format(calendarDate.getTime());

        // gets the current time
        Calendar calendarTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(calendarTime.getTime());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search,menu);
        MenuItem item = menu.findItem(R.id.menu_search);

        // Material SearchView
        searchView = findViewById(R.id.search_view);
        searchView.setMenuItem(item);
        searchView.setEllipsize(true);
        searchView.setSubmitOnClick(true);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String username) {
                // test if searchView is not empty
                if(!username.isEmpty()){
                    // method search to search for document by title
                    searchUser(username.toLowerCase());
                    searchView.clearFocus();
                }

                //else
                else{
                    searchUser("");
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String username) {
                // test if searchView is not empty
                if(!username.isEmpty()){
                    searchUser(username.toLowerCase());
                }

                //else
                else{
                    searchUser("");
                }

                return true;
            }
        });


        return true;
    }

    // method to search for user in the system
    private void searchUser(String username) {

        Query query = userRef.orderByChild("search")
                .startAt(username)
                .endAt(username + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // checks if any search results found
                if(!dataSnapshot.exists()){

                    // displays this textView to tell user that no search results found
                    tv_no_search_results.setVisibility(View.VISIBLE);

                    // hides the recycler view
                    recyclerView.setVisibility(GONE);

                }
                else {
                    mUsers.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Users users = snapshot.getValue(Users.class);

                        assert users != null;

                        // displays the recycler view
                        recyclerView.setVisibility(View.VISIBLE);

                        // hides this textView to tell user that no search results found
                        tv_no_search_results.setVisibility(View.GONE);

                        mUsers.add(users);

                    }
                }

                // notify data change in adapter
                adapterUsers.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display message if error occurs
                Snackbar.make(relativeLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });

    }

    // attaching on click listener for floating action Button
    public void createGroup(View view) {

        // method call to create group
        createGroup(groupName);

        // waits for 2 secs to navigate user back to the chat activity
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                // finishes the activity after 3 secs
                finish();

            }
        },3000);



    }

    // method that creates group
    private void createGroup(final String groupName){

        groups.setGroupName(groupName);
        groups.setGroupIcon("");
        groups.setGroupMembersIds(selectedUserIds);
        groups.setDateCreated(currentDate);
        groups.setTimeCreated(currentTime);

        groupRef.child(groupName).setValue(groups)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            // display a success message if group is created successfully
                            Snackbar.make(relativeLayout,
                                    groupName + " group is created successfully ",Snackbar.LENGTH_LONG).show();
                        }

                        else {
                            // display an error message if group is not created successfully
                            Toast.makeText(AddUsersActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // finishes activity and does not take user to the interface until user goes back again
        finish();
    }


}
