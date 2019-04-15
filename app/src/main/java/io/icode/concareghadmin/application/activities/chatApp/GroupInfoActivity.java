package io.icode.concareghadmin.application.activities.chatApp;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.adapters.RecyclerViewAdapterGroupMembers;
import io.icode.concareghadmin.application.activities.constants.Constants;
import io.icode.concareghadmin.application.activities.models.Groups;
import io.icode.concareghadmin.application.activities.models.Users;

import static android.view.View.GONE;

public class GroupInfoActivity extends AppCompatActivity {

    ImageView ci_group_icon;

    TextView tv_group_name;

    TextView tv_time_created;

    TextView tv_no_search_results;

    EditText editTextSearch;

    Uri profileImageUri;

    String profileImageUrl;

    FirebaseStorage mStorage;

    StorageReference mStorageRef;

    String group_name;
    String group_image_url;
    String date_created;
    String time_created;
    // list to get the ids of selected users from group creating
    private List<String> usersIds;

    private List<Users> membersList;

    RecyclerView recyclerView;

    RecyclerViewAdapterGroupMembers adapterGroupMembers;

    DatabaseReference groupRef;

    DatabaseReference userRef;

    ProgressBar progressBar;

    ProgressBar progressBar1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        ci_group_icon = findViewById(R.id.ci_group_icon);

        tv_group_name = findViewById(R.id.tv_group_name);

        tv_time_created = findViewById(R.id.tv_time_created);

        tv_no_search_results = findViewById(R.id.tv_no_search_results);

        editTextSearch = findViewById(R.id.editTextSearch);

        progressBar =  findViewById(R.id.progressBar);

        progressBar1 =  findViewById(R.id.progressBar1);

        mStorage = FirebaseStorage.getInstance();

        // getting strings passed from recyclerView adapter
        group_name = getIntent().getStringExtra("_group_name");
        group_image_url = getIntent().getStringExtra("_group_icon");
        date_created = getIntent().getStringExtra("_date_created");
        time_created = getIntent().getStringExtra("_time_created");
        usersIds = getIntent().getStringArrayListExtra("_usersIds");

        groupRef = FirebaseDatabase.getInstance().getReference(Constants.GROUP_REF).child(group_name);

        userRef = FirebaseDatabase.getInstance().getReference(Constants.USER_REF);

        membersList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapterGroupMembers = new RecyclerViewAdapterGroupMembers(this,membersList);

        recyclerView.setAdapter(adapterGroupMembers);

        // method call to display profile image
        loadProfile();

        // method call to display group members
        displayGroupMembers();

        // method call to search for user
        search();


    }

    // select image from gallery
    public void selectGroupIcon(View view) {

        Intent intentPick = new Intent();
        intentPick.setType("image/*");
        intentPick.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intentPick, "Select Group Icon"),Constants.REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){

            profileImageUri = data.getData();

            try {
                // loads image Uri using picasso library
                Picasso.get().load(profileImageUri).into(ci_group_icon);
                // method call to update user profile
                updateProfile();
            }
            catch (Exception e){
                // display exception caught
                Toast.makeText(GroupInfoActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }

        }
        else {
            Toast.makeText(this, R.string.msg_no_image, Toast.LENGTH_SHORT).show();
        }

    }

    // method to get the extension of the file
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    // method to load group icon into image view
    private void loadProfile(){

        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Groups groups = dataSnapshot.getValue(Groups.class);

                assert groups != null;

                // sets the group name
                tv_group_name.setText(groups.getGroupName());

                tv_time_created.setText("Created on " + groups.getDateCreated() + " at " + groups.getTimeCreated());

                if(groups.getGroupIcon() == null){
                    // loading default icon as image icon
                    Glide.with(getApplicationContext()).load(R.drawable.ic_group_white).into(ci_group_icon);
                }
                else{
                    // loading image url as image icon
                    Glide.with(getApplicationContext()).load(groups.getGroupIcon()).into(ci_group_icon);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display message if error occurs
                Toast.makeText(GroupInfoActivity.this, databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // method to update group info
    private void updateProfile(){

        // display progress bar
        progressBar.setVisibility(View.VISIBLE);

        if(profileImageUri != null){

            mStorageRef = FirebaseStorage.getInstance()
                    .getReference("Group Profile Pictures/" + System.currentTimeMillis()
                            + getFileExtension(profileImageUri));

            mStorageRef.putFile(profileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // get the uri from image to store in database
                    mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            Uri downloadUrl = uri;

                            // convert image URI to URl
                            profileImageUrl = downloadUrl.toString();

                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("groupIcon", profileImageUrl);
                            groupRef.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(GroupInfoActivity.this, R.string.profile_update,
                                                Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        Toast.makeText(GroupInfoActivity.this, R.string.profile_update,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    });

                    // hides progress bar
                    progressBar.setVisibility(View.GONE);

                }
            });

        }
        else{
            Toast.makeText(GroupInfoActivity.this, R.string.msg_no_image, Toast.LENGTH_SHORT).show();
        }

    }

    // display group members on recycler view
    private void displayGroupMembers(){

        // display progressBar
        progressBar1.setVisibility(View.VISIBLE);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                membersList.clear();

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){

                    Users users = snapshot.getValue(Users.class);

                    assert users != null;

                    if(usersIds.contains(users.getUid())){
                        membersList.add(users);
                    }

                }

                // notify adapter of changes
                adapterGroupMembers.notifyDataSetChanged();

                // hide progressBar
                progressBar1.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                // hide progressBar
                progressBar1.setVisibility(View.GONE);

                // display message if error occurs
                Toast.makeText(GroupInfoActivity.this, databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    private void search(){

       editTextSearch.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {

           }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString().toLowerCase());
           }

           @Override
           public void afterTextChanged(Editable s) {

           }
       });

    }

    private void searchUser(String username){

        Query query = userRef.orderByChild("search")
                .startAt(username)
                .endAt(username + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){

                    // display text
                    tv_no_search_results.setVisibility(View.VISIBLE);

                    // hide recycler view
                    recyclerView.setVisibility(GONE);

                }

                // clear's list
                membersList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Users users = snapshot.getValue(Users.class);

                    assert users != null;

                    if(usersIds.contains(users.getUid())){
                        // hide text
                        tv_no_search_results.setVisibility(View.GONE);

                        // display recycler view
                        recyclerView.setVisibility(View.VISIBLE);

                        membersList.add(users);
                    }

                }

                adapterGroupMembers.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display message if error occurs
                Toast.makeText(GroupInfoActivity.this, databaseError.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // finish activity
        finish();
    }
}
