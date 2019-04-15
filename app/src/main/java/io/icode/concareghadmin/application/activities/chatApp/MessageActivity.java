package io.icode.concareghadmin.application.activities.chatApp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.LogTime;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.RecyclerItemDoubleClickListener;
import io.icode.concareghadmin.application.activities.adapters.MessageAdapter;
import io.icode.concareghadmin.application.activities.constants.Constants;
import io.icode.concareghadmin.application.activities.interfaces.APIService;
import io.icode.concareghadmin.application.activities.models.Admin;
import io.icode.concareghadmin.application.activities.models.Chats;
import io.icode.concareghadmin.application.activities.models.Users;
import io.icode.concareghadmin.application.activities.notifications.Client;
import io.icode.concareghadmin.application.activities.notifications.Data;
import io.icode.concareghadmin.application.activities.notifications.MyResponse;
import io.icode.concareghadmin.application.activities.notifications.Sender;
import io.icode.concareghadmin.application.activities.notifications.Token;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static io.icode.concareghadmin.application.activities.constants.Constants.ADMIN_REF;
import static io.icode.concareghadmin.application.activities.constants.Constants.client_url;

@SuppressWarnings("ALL")
public class MessageActivity extends AppCompatActivity implements View.OnClickListener, MessageAdapter.OnItemClickListener {

    RelativeLayout relativeLayout;

    Toolbar toolbar;

    CircleImageView profile_image;
    TextView username,tv_user_status;

    TextView tv_no_chats;

    // instance of Admin Class
    Admin admin;

    // variable to hold uid of admin from sharePreference
    String admin_uid;

    // dbRef variables
    DatabaseReference userRef;

    DatabaseReference chatRef;

    DatabaseReference adminRef;

    // editText and Button to send Message
    EditText msg_to_send;
    ImageButton img_emoji,btn_send;

    Intent intent;

    // string to get intentExtras
    String  user_id;
    String user_name;

    //Variable to store status of the current user
    String status;

    // variable for MessageAdapter class
    MessageAdapter messageAdapter;
    List<Chats> mChats;

    RecyclerView recyclerView;

    // Listener to listener for messages seen
    ValueEventListener seenListener;

    ValueEventListener mDBListener;

    APIService apiService;

    boolean notify = false;

    // variable to store the current time
    String currentTime;

    String messageText;

    ClipboardManager clipboardManager;

    int selectedPosition = -1;

    // loading bar to load messages
    ProgressBar progressBar;

    ProgressDialog progressDialog;


    //query for the db
    Query query;

    //token array and data array
    final Token[] token = new Token[1];
    final Data[] data = new Data[1];

    private static final String TAG = "MessageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);


        // method call to initialize all global variables
        init();

    }

    // initialize variables
    private void init(){

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // creates APIService using Google API from the APIService Class
        apiService = Client.getClient(client_url).create(APIService.class);

        relativeLayout = findViewById(R.id.relativeLayout);

        mChats = new ArrayList<>();

        profile_image =  findViewById(R.id.profile_image);
        username =  findViewById(R.id.username);
        msg_to_send =  findViewById(R.id.editTextMessage);
        btn_send =  findViewById(R.id.btn_send);
        img_emoji =  findViewById(R.id.img_emoji);

        // setting on click listener on image buttons
        btn_send.setOnClickListener(this);
        img_emoji.setOnClickListener(this);

        tv_no_chats = findViewById(R.id.tv_no_chats);

        tv_user_status = findViewById(R.id.user_status);

        //getting reference to the recyclerview and setting it up
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        intent = getIntent();
        user_id = intent.getStringExtra("uid");
        user_name = intent.getStringExtra("username");

        // get the current ststus of user
        status = intent.getStringExtra("status");

        // set status of the admin on toolbar below the username in the message activity
        tv_user_status.setText(status);

        // creating an instance of the Admin Class
        admin = new Admin();

        // getting the uid of the admin stored in sharePreference
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        admin_uid = preferences.getString("uid","");

        userRef = FirebaseDatabase.getInstance().getReference(Constants.USER_REF).child(user_id);


        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Constants.TOKENS_REF);
        query = tokens.orderByKey().equalTo(user_id);

        progressBar =  findViewById(R.id.progressBar);

        // progressDialog to display before deleting message
        progressDialog = new ProgressDialog(this);
        // setting message on progressDialog
        progressDialog.setMessage("Deleting message...");

        clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);

        // method call
        getUserDetails();

        // method call
        seenMessage(user_id);

        // method call to update token
        updateToken(FirebaseInstanceId.getInstance().getToken());

        //subscribe to admin topic
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.ADMIN_TOPIC);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // method call to copy text to clipboard
        //copyTextData();

        // method call to paste text from clipboard
        //pasteCopiedText();

        status(getString(R.string.status_online));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        status(getString(R.string.status_online));
        //currentUser(users_id);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status(getString(R.string.status_online));
        //currentUser(users_id);
    }

    @Override
    protected void onPause() {
        super.onPause();
        status(getString(R.string.status_online));
        //currentUser("none");
    }

    @Override
    protected void onStop() {
        super.onStop();
        status(getString(R.string.status_online));
    }

    // gets the current time
    private void getCurrentTime(){

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = timeFormat.format(calendar.getTime());

    }

    // method to paste Text copied to clipboard
    private void pasteCopiedText(){

        msg_to_send.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                // Get clip data from clipboard.
                ClipData clipData = clipboardManager.getPrimaryClip();
                // Get item count.
                int itemCount = clipData.getItemCount();
                if(itemCount > 0)
                {
                    // Get source text
                    ClipData.Item item = clipData.getItemAt(0);
                    String copiedText = item.getText().toString();

                    // Set the text to target textview.
                    msg_to_send.setText(copiedText);

                    // Show a toast to tell user text has been pasted.
                    Toast.makeText(MessageActivity.this,getString(R.string.text_pasted),Toast.LENGTH_LONG).show();
                }

                return true;
            }
        });


    }

    // Update currentAdmin's token
    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.TOKENS_REF);
        Token token1 = new Token(token);
        reference.child(admin_uid).setValue(token1);
    }

    private void getUserDetails(){

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                assert users != null;
                username.setText(users.getUsername());
                if(users.getImageUrl() == null){
                    // sets a default placeholder into imageView if url is null
                    profile_image.setImageResource(R.drawable.ic_person_unknown);
                }
                else{
                    // loads imageUrl into imageView if url is not null
                    Glide.with(getApplicationContext())
                            .load(users.getImageUrl()).into(profile_image);
                }

                // method call
                readMessages(admin_uid,user_id, users.getImageUrl());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Toast.makeText(MessageActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });


        //get user token
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    token[0] = snapshot.getValue(Token.class);

                    assert token[0] != null;
                    Sender sender = new Sender(data[0], token[0].getToken());

                    Log.d(TAG, "onDataChange: user token " + token[0].getToken());
/*

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200){
                                        if(response.body().success != 1){
                                            */
/*Toast.makeText(MessageActivity.this,"Failed : "
                                                    + !response.isSuccessful(),Toast.LENGTH_LONG).show();*//*

                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    // display error message
                                    Snackbar.make(relativeLayout,t.getMessage(),Snackbar.LENGTH_LONG).show();
                                }
                            });
*/
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Snackbar.make(relativeLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.img_emoji:{

                Toast.makeText(MessageActivity.this,
                        getString(R.string.text_hi),Toast.LENGTH_LONG).show();

                break;
            }

            case R.id.btn_send:{

                // sets notify to true
                notify = true;

                // method call to get current time
                getCurrentTime();

                String message  = msg_to_send.getText().toString();

                // checks if the edit field is not message before sending message
                if(!message.equals("")){
                    //btn_send.setVisibility(View.VISIBLE);
                    // call to method to sendMessage
                    sendMessage(admin_uid, user_id, message);
                }
                else{

                    Toast.makeText(MessageActivity.this,
                            getString(R.string.no_text_message),Toast.LENGTH_LONG).show();
                }
                // clear the field after message is sent
                msg_to_send.setText("");

                break;
            }
        }

    }

    // sends message to user by taking in these three parameters
    private void sendMessage(String sender, final String receiver, String message){

        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver", receiver);
        hashMap.put("receivers", new ArrayList<String>(){{add(receiver);}});
        hashMap.put("message",message);
        hashMap.put("timeStamp",currentTime);
        hashMap.put("isseen", false);

        messageRef.child(Constants.CHAT_REF).push().setValue(hashMap);

        // add chat to the chatList so that it can be added to the Chats fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference(Constants.CHAT_LIST_REF)
                .child(admin_uid)
                .child(user_id);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef.child("id").setValue(user_id);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Toast.makeText(MessageActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

        // variable to hold the message to be sent
        final String msg = message;

        adminRef = FirebaseDatabase.getInstance().getReference(ADMIN_REF).child(admin_uid);

        adminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Admin admin = dataSnapshot.getValue(Admin.class);
                assert admin != null;
                if(notify) {
                  // method call to send notification to user when admin sends a message
                  sendNotification(receiver, admin.getUsername(), msg);
                }
                // sets notify to false
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Toast.makeText(MessageActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    // sends notification to admin as soon as message is sent
    private void sendNotification(String receiver, final String username, final String message){
        Log.d(TAG, "sendNotification: send notification called");

        data[0] = new Data(admin_uid,R.mipmap.app_logo_round, username+": "+message,
                getString(R.string.application_name),user_id);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        APIService api = retrofit.create(APIService.class);

        Call<ResponseBody> call = api.sendSingleNotification(token[0].getToken(), username ,message);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // response to be handled here
                if (response.code() == 200){
                    Snackbar.make(relativeLayout, "sent", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Snackbar.make(relativeLayout, "failed to send message", Snackbar.LENGTH_LONG).show();
            }
        });


    }


    // method to check if user has seen message
    private void seenMessage(final String user_id){

        chatRef = FirebaseDatabase.getInstance().getReference(Constants.CHAT_REF);

        seenListener = chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chats chats = snapshot.getValue(Chats.class);
                    assert chats != null;
                    if(chats.getReceiver().equals(admin_uid) && chats.getSender().equals(user_id)
                            || chats.getReceiver().equals(user_id) && chats.getSender().equals(admin_uid)
                            || chats.getReceiver().equals("") && chats.getReceivers().contains(user_id)
                            && chats.getSender().equals(admin_uid)
                            || chats.getReceivers().contains(admin_uid)
                            && chats.getSender().equals(user_id)){
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(relativeLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });


    }

    // method to readMessages from the database
    private void readMessages(final String adminId, final String userId, final String imageUrl){

        // display progressBar
        progressBar.setVisibility(View.VISIBLE);

        // array initialization
        //mChats = new ArrayList<>();

        chatRef = FirebaseDatabase.getInstance().getReference(Constants.CHAT_REF);

        mDBListener = chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){

                    // displays text if there are no recent chats
                    tv_no_chats.setVisibility(View.VISIBLE);

                    // dismiss progressBar
                    progressBar.setVisibility(View.GONE);

                }
                else{

                    // clears the chats to avoid reading duplicate message
                    mChats.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Chats chats = snapshot.getValue(Chats.class);
                        // gets the unique keys of the chats
                        chats.setKey(snapshot.getKey());

                        assert chats != null;

                        if(chats.getReceiver().equals(adminId) && chats.getSender().equals(userId)
                                || chats.getReceiver().equals(userId) && chats.getSender().equals(adminId)
                                || chats.getReceiver().equals("") && chats.getReceivers().contains(userId)
                                && chats.getSender().equals(adminId)
                                || chats.getReceivers().contains(adminId)
                                && chats.getSender().equals(userId)){
                            mChats.add(chats);
                        }

                        // initializing the messageAdapter and setting adapter to recyclerView
                        messageAdapter = new MessageAdapter(MessageActivity.this,mChats,imageUrl);
                        // setting adapter
                        recyclerView.setAdapter(messageAdapter);
                        // notify data change in adapter
                        messageAdapter.notifyDataSetChanged();

                        // hides text if there are recent chats
                        tv_no_chats.setVisibility(View.GONE);

                        // dismiss progressBar
                        progressBar.setVisibility(View.GONE);

                        // setting on OnItemClickListener in this activity as an interface for ContextMenu
                        messageAdapter.setOnItemClickListener(MessageActivity.this);

                    }


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                // dismiss progressBar
                progressBar.setVisibility(View.GONE);

                // display error message
                Snackbar.make(relativeLayout,databaseError.getMessage(),Snackbar.LENGTH_LONG).show();
            }
        });

    }

    /**handling ContextMenu
     Click Listeners in activity
     */

    @Override
    public void onItemClick(int position) {
        Toast.makeText(this,getString(R.string.long_click_msg_alert),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeleteClick(final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
        builder.setTitle(getString(R.string.title_delete_message));
        builder.setMessage(getString(R.string.text_delete_message));

        builder.setPositiveButton((getString(R.string.text_yes)), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                // show dialog
                progressDialog.show();

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // dismiss dialog
                        progressDialog.dismiss();

                        // gets the position of the selected message
                        Chats selectedMessage = mChats.get(position);

                        //gets the key at the selected position
                        String selectedKey = selectedMessage.getKey();

                        chatRef.child(selectedKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(MessageActivity.this,getString(R.string.text_msg_deleted),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MessageActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });


                    }
                },3000);

            }
        });

        builder.setNegativeButton((getString(R.string.text_no)), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    @Override
    public void onCancelClick(int position) {
        // do nothing / close ContextMenu
    }

    // keeping track of the current user the admin is chatting to avoid sending notification everytime
    private void currentUser(String users_id){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("currentuser",users_id);
        editor.apply();
    }

    // setting the status of the users
    private void status(String status){

        adminRef = FirebaseDatabase.getInstance().getReference(ADMIN_REF)
        .child(admin_uid);

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        adminRef.updateChildren(hashMap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        status(getString(R.string.status_online));
        // removes eventListeners when activity is destroyed
        if(chatRef != null){
            chatRef.removeEventListener(seenListener);
            chatRef.removeEventListener(mDBListener);
        }

    }

}
