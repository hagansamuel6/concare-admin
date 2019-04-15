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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.chatApp.MessageActivity;
import io.icode.concareghadmin.application.activities.models.Admin;
import io.icode.concareghadmin.application.activities.models.Chats;
import io.icode.concareghadmin.application.activities.models.Users;

public class RecyclerViewAdapterAddUsers extends RecyclerView.Adapter<RecyclerViewAdapterAddUsers.ViewHolder> {

    Admin admin;

    // variable to store admin uid from sharePreference
    //String admin_uid;

    private Context mCtx;
    private List<Users> mUsers;
    private boolean isChat;

    // string variable to contain lastMessage from user
    private String theLastMessage;

    // strings to store the ids of chat users <--------------------------------------------
    private List<String> selectedUserIds;

    public RecyclerViewAdapterAddUsers(Context mCtx, List<Users> mUsers, boolean isChat){
        this.mCtx = mCtx;
        this.mUsers = mUsers;
        this.isChat = isChat;
        this.selectedUserIds = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_add_users,parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        // gets the positions of the all users
        final Users users = mUsers.get(position);

        // sets username to the text of the textView
        holder.username.setText(users.getUsername());

        // sets gender to the text of the textView
        holder.gender.setText(users.getGender());

        if(users.getImageUrl() == null){
            // loads the default placeholder into ImageView if ImageUrl is null
            Glide.with(mCtx).load(R.drawable.ic_user).into(holder.profile_pic);
        }
        else{
            // loads users image into the ImageView
            Glide.with(mCtx).load(users.getImageUrl()).into(holder.profile_pic);
        }

        // uncheck check boxes
        //holder.mCheckBox.setChecked(false);

        // prevents the recycler view to automatically select an item from list
        holder.mCheckBox.setOnCheckedChangeListener(null);
        holder.mCheckBox.setSelected(users.isSelected());

        // attaching on click listener to checkbox to select users and add to group
        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                    // add user to list if checked
                    selectedUserIds.add(users.getUid());

                    // set Selected to true
                    users.setSelected(true);


                }
                else{

                    // removes user id from list if user is unchecked
                    selectedUserIds.remove(users.getUid());

                    // set Selected to false
                    users.setSelected(false);

                }
            }
        });

        holder.mCheckBox.setChecked(users.isSelected());


    }

    // getter method to return the list of selected users
    public List<String> getSelectedUserIds() {
        return selectedUserIds;
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        Admin admin;

        CircleImageView profile_pic;
        TextView username;
        TextView gender;
        CheckBox mCheckBox;


        public ViewHolder(View itemView) {
            super(itemView);

            admin = new Admin();

            profile_pic = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            gender = itemView.findViewById(R.id.gender);
            mCheckBox = itemView.findViewById(R.id.add_user);
        }
    }

}
