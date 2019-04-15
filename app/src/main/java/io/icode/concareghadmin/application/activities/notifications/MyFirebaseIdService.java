package io.icode.concareghadmin.application.activities.notifications;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import io.icode.concareghadmin.application.activities.models.Admin;

@SuppressWarnings("ALL")
public class MyFirebaseIdService extends FirebaseInstanceIdService {

    DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admin");

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        // getting an instance of currentAdmin
        adminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Admin currentAdmin = snapshot.getValue(Admin.class);
                    assert currentAdmin != null;

                    // getting token from FirebaseInstanceId
                    String refreshedToken = FirebaseInstanceId.getInstance().getToken();

                    if(currentAdmin != null){
                        updateToken(refreshedToken);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }

    private void updateToken(final String refreshToken){

        // getting an instance of currentAdmin
        adminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Admin currentAdmin = snapshot.getValue(Admin.class);
                    assert currentAdmin != null;

                    // creating tokens to send notifications
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
                    Token token = new Token(refreshToken);
                    reference.child(currentAdmin.getAdminUid()).setValue(token);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });


    }

}
