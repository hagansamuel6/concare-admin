package io.icode.concareghadmin.application.activities.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import io.icode.concareghadmin.application.R;
import io.icode.concareghadmin.application.activities.RecyclerItemDoubleClickListener;
import io.icode.concareghadmin.application.activities.chatApp.MessageActivity;
import io.icode.concareghadmin.application.activities.constants.Constants;
import io.icode.concareghadmin.application.activities.models.Chats;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mCtx;
    private List<Chats> mChats;
    private String imageUrl;

    int selectedPosition = -1;

    boolean isMessagHighlighted = false;

    // Global variable to handle OnItemClickListener
    public static OnItemClickListener mListener;

    public MessageAdapter(Context mCtx, List<Chats> mChats, String imageUrl){
        this.mCtx= mCtx;
        this.mChats = mChats;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(mCtx).inflate(R.layout.recyclerview_chat_item_right,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(mCtx).inflate(R.layout.recyclerview_chat_item_left,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        Chats chats = mChats.get(position);

        holder.show_message.setText(chats.getMessage());

        if(chats.getTimeStamp() != null){
            holder.timeStamp.setVisibility(View.VISIBLE);
            holder.timeStamp.setText(chats.getTimeStamp());
        }
        else{
            holder.timeStamp.setVisibility(View.GONE);
            holder.timeStamp.setText(null);
        }

        // checks if imageUrl is empty or not
        if(imageUrl == null){
            // loads the default placeholder into ImageView if ImageUrl is null
            Glide.with(mCtx).load(R.drawable.ic_user_white).into(holder.profile_image);
        }
        else{
            // loads the image url into ImageView if ImageUrl is  not null
            Glide.with(mCtx).load(imageUrl).into(holder.profile_image);
        }

        // checks if chat is seen by user and sets the appropriate text
        if(position == mChats.size()-1){
            if(chats.isIsseen()){
                holder.txt_seen.setText(R.string.text_seen);
            }
            else{
                holder.txt_seen.setText(R.string.text_delivered);
            }
        }
        else{
            holder.txt_seen.setVisibility(View.GONE);
        }


        if(selectedPosition == position){
            //holder.itemView.setForeground();
            holder.itemView.setBackgroundColor(0);
            holder.actionButton.setVisibility(View.VISIBLE);
            //holder.buttonCopy.setVisibility(View.VISIBLE);
            //holder.buttonDelete.setVisibility(View.VISIBLE);
            holder.show_message.setTextColor(Color.parseColor("#ff676767"));

        }
        else{
            //holder.buttonCopy.setVisibility(View.GONE);
            //holder.buttonDelete.setVisibility(View.GONE);
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.show_message.setTextColor(Color.parseColor("#ff292929"));
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }


        // on click listener on item View
        /*holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                selectedPosition = position;

                notifyDataSetChanged();

                return true;
            }
        });
        */

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if(!isMessagHighlighted){

                    selectedPosition = position;

                    notifyDataSetChanged();

                    isMessagHighlighted = true;
                }
                else{

                    //holder.buttonCopy.setVisibility(View.GONE);
                    //holder.buttonDelete.setVisibility(View.GONE);
                    holder.actionButton.setVisibility(View.VISIBLE);
                    holder.show_message.setTextColor(Color.parseColor("#ff292929"));
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT);

                    isMessagHighlighted = false;
                }
                return true;
            }
        });


        // on click listener on copy button
        holder.buttonCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String messageText = holder.show_message.getText().toString();

                ClipboardManager clipboardManager = (ClipboardManager)mCtx.getSystemService(Context.CLIPBOARD_SERVICE);
                // Create a new ClipData.
                ClipData clipData = ClipData.newPlainText(mCtx.getString(R.string.data_label),messageText);
                clipboardManager.setPrimaryClip(clipData);
                // Popup a toast.
                Toast.makeText(mCtx,R.string.text_copied,Toast.LENGTH_SHORT).show();
            }
        });

        // progressDialog to display before deleting message
        final ProgressDialog progressDialog = new ProgressDialog(mCtx);
        // setting message on progressDialog
        progressDialog.setMessage("Deleting message...");

        // on click listener on delete button
        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
                builder.setTitle(R.string.title_delete_message);
                builder.setMessage(R.string.text_delete_message);

                builder.setPositiveButton(R.string.text_yes, new DialogInterface.OnClickListener() {
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

                                holder.chatRef.child(selectedKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(mCtx,R.string.text_msg_deleted,Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(mCtx,e.getMessage(),Toast.LENGTH_LONG).show();
                                    }
                                });


                            }
                        },3000);

                    }
                });

                builder.setNegativeButton(R.string.text_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        // adding on click listener to the itemView
        /*holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition = position;
                notifyDataSetChanged();
            }
        });
        */

        /*holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        */

    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        CircleImageView profile_image;
        TextView show_message,timeStamp;

        DatabaseReference chatRef;

        // Message Seen textView
        TextView txt_seen;

        RelativeLayout actionButton;

        ImageView buttonCopy,buttonDelete;

        public ViewHolder(View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            timeStamp = itemView.findViewById(R.id.timeStamp);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            actionButton = itemView.findViewById(R.id.action_button);
            buttonCopy = itemView.findViewById(R.id.button_copy);
            buttonDelete = itemView.findViewById(R.id.button_delete);

            chatRef = FirebaseDatabase.getInstance().getReference(Constants.CHAT_REF);

            // setting onClickListener on itemView
            itemView.setOnClickListener(this);
            // setting onCreateContextMenuListener on itemView
            itemView.setOnCreateContextMenuListener(this);

        }

        // handling normal Clicks
        @Override
        public void onClick(View view) {
            if(mListener != null){
                //get Adapter position
                int position = getAdapterPosition();
                 /*checks if position of item clicked is equal
                to the position of an item in recyclerView*/
                if(position != RecyclerView.NO_POSITION){
                    mListener.onItemClick(position);
                }
            }
        }

        // Handling Context Menu Item Clicks
        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            // setting a title on ContextMenu
            contextMenu.setHeaderTitle(R.string.delete_msg);
            // menu items to delete message or cancel
            MenuItem delete = contextMenu.add(ContextMenu.NONE,1,1,R.string.text_delete_for_me);
            MenuItem cancel = contextMenu.add(ContextMenu.NONE,2,2, R.string.text_cancel_for_me);

            delete.setOnMenuItemClickListener(this);
            cancel.setOnMenuItemClickListener(this);

        }

        // Handling onItemClick(Actual item) in the ContextMenu
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if(mListener != null){
                //get Adapter position
                int position = getAdapterPosition();
                /*checks if position of item clicked is equal
                to the position of an item in recyclerView*/
                if(position != RecyclerView.NO_POSITION){
                    switch (menuItem.getItemId()){
                        case 1:
                            mListener.onDeleteClick(position);
                            return true;
                        case 2:
                            mListener.onCancelClick(position);
                            return true;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public int getItemViewType(int position) {
        // getting the uid of the admin stored in shared preference
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mCtx);
        final String admin_uid = preferences.getString("uid","");

        if(mChats.get(position).getSender().equals(admin_uid)){
            return MSG_TYPE_RIGHT;
        }
        else{
            return MSG_TYPE_LEFT;
        }

    }

    public interface OnItemClickListener{
        void onItemClick(int position);

        void onDeleteClick(int position);

        void onCancelClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }
}
