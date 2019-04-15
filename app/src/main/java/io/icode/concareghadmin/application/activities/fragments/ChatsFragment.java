package io.icode.concareghadmin.application.activities.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.adapters.RecyclerViewAdapterChat;
import io.icode.concareghadmin.application.activities.adapters.RecyclerViewAdapterUser;
import io.icode.concareghadmin.application.activities.chatApp.AddUsersActivity;
import io.icode.concareghadmin.application.activities.chatApp.ChatActivity;
import io.icode.concareghadmin.application.activities.constants.Constants;
import io.icode.concareghadmin.application.activities.models.Chatlist;
import io.icode.concareghadmin.application.activities.models.Users;
import io.icode.concareghadmin.application.activities.notifications.Token;

import static io.icode.concareghadmin.application.activities.constants.Constants.CHAT_LIST_REF;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressWarnings("ALL")
public class ChatsFragment extends Fragment {

    View view;

    TextView tv_no_chats;

    private RecyclerView recyclerView;

    private RecyclerViewAdapterChat recyclerViewAdapterChat;

    private List<Chatlist> usersList;

    private List<Users> mUsers;

    String admin_uid;

    DatabaseReference userRef;

    DatabaseReference chatListRef;

    ProgressBar progressBar;

    ChatActivity applicationContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        applicationContext = (ChatActivity)context;
    }

    @SuppressWarnings("ALL")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chats,container,false);

        // method call to initialize views
        init();

        return view;
    }

    // method to initialize views
    private void init(){

        tv_no_chats = view.findViewById(R.id.tv_no_chats);

        usersList = new ArrayList<>();

        mUsers = new ArrayList<>();

        // getting reference to view
        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);

        // setting layout for recyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(applicationContext);

        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        progressBar = view.findViewById(R.id.progressBar);

        // initializing recyclerView adapter
        recyclerViewAdapterChat = new RecyclerViewAdapterChat(applicationContext,mUsers,true);

        // setting adapter
        recyclerView.setAdapter(recyclerViewAdapterChat);

        // enable smooth scrolling in recycler view
        recyclerView.setNestedScrollingEnabled(false);

        // getting the uid of the admin stored in shared preference
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);

        admin_uid = preferences.getString("uid","");

        addUsersToChatList();

        // method call to update token
        updateToken(FirebaseInstanceId.getInstance().getToken());

        //addUsersToChatList();

    }

    private void addUsersToChatList(){

        chatListRef = FirebaseDatabase.getInstance().getReference(CHAT_LIST_REF).child(admin_uid);

        chatListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    usersList.add(chatlist);
                }
                // method call to populate current chats of the chats of the Admin
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message
                Toast.makeText(getContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    // Update currentAdmin's token
    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.TOKENS_REF);
        Token token1 = new Token(token);
        reference.child(admin_uid).setValue(token1);
    }

    // method to populate fragment with Admin chats
    private void chatList(){

        // display the  progressBar
        progressBar.setVisibility(View.VISIBLE);

        // db reference to users table
        userRef = FirebaseDatabase.getInstance().getReference(Constants.USER_REF);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // checks if there is no recent chat
                if(!dataSnapshot.exists()){
                    // sets visibility of recyclerView to gone
                    recyclerView.setVisibility(View.GONE);
                    // textView to visible if no recent chat exist
                    tv_no_chats.setVisibility(View.VISIBLE);
                }
                else{
                    mUsers.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Users user = snapshot.getValue(Users.class);
                        for(Chatlist chatlist : usersList){
                            assert user != null;
                            if(user.getUid().equals(chatlist.getId())){
                                // set visibility to gone
                                tv_no_chats.setVisibility(View.GONE);
                                // sets visibility to Visible if ther are recent chats
                                recyclerView.setVisibility(View.VISIBLE);
                                // adds current users admin has chat with
                                mUsers.add(user);
                            }
                        }
                    }
                }

                // notifies adapter of any changes
                recyclerViewAdapterChat.notifyDataSetChanged();

                // dismiss the progressBar
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                // dismiss the progressBar
                progressBar.setVisibility(View.GONE);

                // display error message
                Toast.makeText(getContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }


}
