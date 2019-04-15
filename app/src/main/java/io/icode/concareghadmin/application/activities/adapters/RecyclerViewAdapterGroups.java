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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.chatApp.GroupMessageActivity;
import io.icode.concareghadmin.application.activities.constants.Constants;
import io.icode.concareghadmin.application.activities.models.Admin;
import io.icode.concareghadmin.application.activities.models.Chats;
import io.icode.concareghadmin.application.activities.models.Groups;

public class RecyclerViewAdapterGroups extends RecyclerView.Adapter<RecyclerViewAdapterGroups.ViewHolder> {

    Admin admin;

    // variable to store admin uid from sharePreference
    //String admin_uid;

    private Context mCtx;
    private List<Groups> mGroups;
    private boolean isChat;

    // string variable to contain lastMessage from user
    private String theLastMessage;

    public RecyclerViewAdapterGroups(Context mCtx, List<Groups> mGroups, boolean isChat){
        this.mCtx = mCtx;
        this.mGroups = mGroups;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item_group,parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // gets the positions of the all users
        final Groups groups = mGroups.get(position);

        // sets username to the text of the textView
        holder.groupName.setText(groups.getGroupName());

        if(groups.getGroupIcon() == null){
            // loads the default placeholder into ImageView if ImageUrl is null
            Glide.with(mCtx).load(R.drawable.ic_group).into(holder.groupIcon);
        }
        else{
            // loads users image into the ImageView
            Glide.with(mCtx).load(groups.getGroupIcon()).into(holder.groupIcon);
        }


        // calling the lastMessage method
        if(isChat){
            lastMessage(groups.getGroupMembersIds(),holder.last_msg);
        }
        else {
            holder.last_msg.setVisibility(View.GONE);
        }

        // onClickListener for view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // passing adminUid as a string to the MessageActivity
                Intent intent = new Intent(mCtx,GroupMessageActivity.class);
                intent.putExtra("group_name", groups.getGroupName());
                intent.putExtra("group_icon", groups.getGroupIcon());
                intent.putExtra("date_created", groups.getDateCreated());
                intent.putExtra("time_created", groups.getTimeCreated());
                // passing an array of the ids of the group members
                intent.putStringArrayListExtra("usersIds",(ArrayList<String>)groups.getGroupMembersIds());
                mCtx.startActivity(intent);

                // storing string in sharePreference

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mCtx).edit();
                editor.putString("group_name",groups.getGroupName());
                editor.putString("group_icon", groups.getGroupIcon());
                //editor.putStringSet("icons",(Set<String>)groups.getGroupMembersIds());
                editor.apply();

            }
        });

    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        Admin admin;

        CircleImageView groupIcon;
        TextView groupName;
        TextView last_msg;

        public ViewHolder(View itemView) {
            super(itemView);

            admin = new Admin();

            groupIcon = itemView.findViewById(R.id.ci_group_icon);
            groupName = itemView.findViewById(R.id.tv_group_name);
            last_msg = itemView.findViewById(R.id.tv_last_msg);

        }
    }

    // checks for last message
    private void lastMessage(final List<String> users_ids, final TextView last_msg){

        theLastMessage = "default";

        // getting the uid of the admin stored in shared preference
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mCtx);
        final String admin_uid = preferences.getString("uid","");

        DatabaseReference lastMsgRef = FirebaseDatabase.getInstance().getReference(Constants.CHAT_REF);
        lastMsgRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chats chats = snapshot.getValue(Chats.class);

                    assert chats != null;

                    // compares the uid of the admin and user and return the last message
                    if(chats.getReceiver().equals("") && chats.getReceivers().containsAll(users_ids)
                            && chats.getSender().equals(admin_uid)){
                        theLastMessage = chats.getMessage();
                    }

                }

                // switch case for theLastMessage
                switch (theLastMessage){
                    case "default":
                        last_msg.setText(R.string.no_message);
                        break;

                        default:
                            last_msg.setText(theLastMessage);
                            break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // display error message if one should occur
                Toast.makeText(mCtx, databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

}
