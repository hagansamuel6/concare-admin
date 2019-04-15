package io.icode.concareghadmin.application.activities.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.chatApp.MessageActivity;
import io.icode.concareghadmin.application.activities.models.Admin;
import io.icode.concareghadmin.application.activities.models.Chats;
import io.icode.concareghadmin.application.activities.models.Users;

import static io.icode.concareghadmin.application.activities.constants.Constants.CHAT_REF;

public class RecyclerViewAdapterUser extends RecyclerView.Adapter<RecyclerViewAdapterUser.ViewHolder> {

    Admin admin;

    // variable to store admin uid from sharePreference
    //String admin_uid;

    private Context mCtx;
    private List<Users> mUsers;
    private boolean isChat;

    // string variable to contain lastMessage from user
    private String theLastMessage;

    public RecyclerViewAdapterUser(Context mCtx, List<Users> mUsers, boolean isChat){
        this.mCtx = mCtx;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item_user,parent, false);

        return new RecyclerViewAdapterUser.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // gets the positions of the all users
        final Users users = mUsers.get(position);

        // sets username to the text of the textView
        holder.username.setText(users.getUsername());
        //holder.last_msg.setText(users.getUid());

        if(users.getImageUrl() == null){
            // loads the default placeholder into ImageView if ImageUrl is null
            Glide.with(mCtx).load(R.drawable.ic_user_filled).into(holder.profile_pic);

        }
        else{

            //holder.profile_pic.setBackground(null);

            // loads users image into the ImageView
            Glide.with(mCtx).load(users.getImageUrl()).into(holder.profile_pic);
        }


        // calling the lastMessage method
        if(isChat){
            lastMessage(users.getUid(),holder.last_msg, holder.lastMessageLoadingBar);
        }
        else{
            holder.last_msg.setVisibility(View.GONE);
        }

        // code to check if user is online
        if(isChat){
            if(users.getStatus().equals("online")){
                holder.status_online.setVisibility(View.VISIBLE);
                holder.status_offline.setVisibility(View.GONE);
            }
            else{
                holder.status_online.setVisibility(View.GONE);
                holder.status_offline.setVisibility(View.VISIBLE);
            }
        }
        else{
            holder.status_online.setVisibility(View.GONE);
            holder.status_offline.setVisibility(View.GONE);
        }

        // onClickListener for view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // passing adminUid as a string to the MessageActivity
                Intent intent = new Intent(mCtx,MessageActivity.class);
                intent.putExtra("uid", users.getUid());
                intent.putExtra("username", users.getUsername());
                intent.putExtra("status", users.getStatus());
                mCtx.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        Admin admin;

        CircleImageView profile_pic;
        TextView username;
        TextView last_msg;

        // status online or offline indicators
        CircleImageView status_online;
        CircleImageView status_offline;

        ProgressBar lastMessageLoadingBar;

        public ViewHolder(View itemView) {
            super(itemView);

            admin = new Admin();

            profile_pic = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            status_online = itemView.findViewById(R.id.status_online);
            status_offline = itemView.findViewById(R.id.status_offline);
            last_msg = itemView.findViewById(R.id.last_msg);
            lastMessageLoadingBar = itemView.findViewById(R.id.lastMessageLoadingBar);
        }
    }

    // checks for last message
    private void lastMessage(final String user_id, final TextView last_msg, final ProgressBar progressBar){

        // display progressBar
        progressBar.setVisibility(View.VISIBLE);

        theLastMessage = "default";

        // getting the uid of the admin stored in shared preference
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mCtx);
        final String admin_uid = preferences.getString("uid","");

        DatabaseReference lastMsgRef = FirebaseDatabase.getInstance().getReference(CHAT_REF);
        lastMsgRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chats chats = snapshot.getValue(Chats.class);

                    assert chats != null;

                    // compares the uid of the admin and user and return the last message
                    if(chats.getReceiver().equals(admin_uid) && chats.getSender().equals(user_id)
                            || chats.getReceiver().equals(user_id) && chats.getSender().equals(admin_uid)
                            || chats.getReceiver().equals("") && chats.getReceivers().contains(user_id)
                            && chats.getSender().equals(admin_uid)){
                        theLastMessage = chats.getMessage();

                        /*SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mCtx).edit();
                        editor.putString("last_msg",chats.getMessage());
                        editor.apply();
                        */

                    }

                }

                // switch case for theLastMessage
                switch (theLastMessage){
                    case "default":
                        last_msg.setText(R.string.no_message);
                        break;

                        default:

                            last_msg.setText(theLastMessage);

                            /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
                            theLastMessage = prefs.getString("last_msg",null);

                            last_msg.setText(theLastMessage);
                            */

                            break;
                }

                theLastMessage = "default";

                // display progressBar
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                // display error message if one should occur
                Toast.makeText(mCtx, databaseError.getMessage(),Toast.LENGTH_LONG).show();

                // display progressBar
                progressBar.setVisibility(View.GONE);

            }
        });
    }

}
