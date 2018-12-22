package ba.unsa.etf.rma.adem.mychat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private Button SendfriendRequestButton;
    private Button DeclinefriendRequestButton;
    private TextView profileName;
    private TextView profileStatus;
    private ImageView profileImage;

    private DatabaseReference UsersReference;


    private String CURRENT_STATE;

    private DatabaseReference FriendRequestReference;
    private FirebaseAuth mAuth;

    String sender_user_id;
    String receiver_user_id;

    private DatabaseReference FriendsReference;
    private DatabaseReference NotificationReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FriendRequestReference=FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        FriendRequestReference.keepSynced(true);

        NotificationReference=FirebaseDatabase.getInstance().getReference().child("Notifications");
        NotificationReference.keepSynced(true);

        mAuth=FirebaseAuth.getInstance();
        sender_user_id=mAuth.getCurrentUser().getUid();

        FriendsReference=FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsReference.keepSynced(true);

        UsersReference=FirebaseDatabase.getInstance().getReference().child("Users");

        receiver_user_id=getIntent().getExtras().get("visit_user_id").toString();

        SendfriendRequestButton=(Button)findViewById(R.id.profile_visit_send_request_btn);
        DeclinefriendRequestButton=(Button)findViewById(R.id.profile_decline_friend_request_btn);
        profileName=(TextView)findViewById(R.id.profile_visit_user_name);
        profileStatus=(TextView)findViewById(R.id.profile_visit_user_status);
        profileImage=(ImageView)findViewById(R.id.profile_visit_userImage);

        CURRENT_STATE="not_friends";



        UsersReference.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("user_name").getValue().toString();
                String status=dataSnapshot.child("user_status").getValue().toString();
                String image=dataSnapshot.child("user_image").getValue().toString();

                profileName.setText(name);
                profileStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.default_profile).into(profileImage);

                FriendRequestReference.child(sender_user_id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(receiver_user_id)){
                                        String req_type=dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();

                                        if(req_type.equals("sent")){
                                            CURRENT_STATE="request_sent";
                                            SendfriendRequestButton.setText("Cancel Friend Request");

                                            DeclinefriendRequestButton.setVisibility(View.INVISIBLE);
                                            DeclinefriendRequestButton.setEnabled(false);
                                        }
                                        else if(req_type.equals("received")){
                                            CURRENT_STATE="request_received";
                                            SendfriendRequestButton.setText("Accept Friend Request");

                                            DeclinefriendRequestButton.setVisibility(View.VISIBLE);
                                            DeclinefriendRequestButton.setEnabled(true);

                                            DeclinefriendRequestButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    DeclineFriendRequest();
                                                }
                                            });
                                        }
                                    }
                                    else {
                                        FriendsReference.child(sender_user_id)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.hasChild(receiver_user_id)){
                                                        CURRENT_STATE="friends";
                                                        SendfriendRequestButton.setText("Unfriend this Person");

                                                        DeclinefriendRequestButton.setVisibility(View.INVISIBLE);
                                                        DeclinefriendRequestButton.setEnabled(false);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        DeclinefriendRequestButton.setVisibility(View.INVISIBLE);
        DeclinefriendRequestButton.setEnabled(false);


        if(!sender_user_id.equals(receiver_user_id)){
            SendfriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendfriendRequestButton.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friends")){
                        SendfriendRequestToAPerson();
                    }
                    if(CURRENT_STATE.equals(("request_sent"))){
                        CancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received")){
                        AcceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("friends")){
                        UnFriendaFriend();
                    }
                }
            });
        }
        else {
            DeclinefriendRequestButton.setVisibility(View.INVISIBLE);
            SendfriendRequestButton.setVisibility(View.INVISIBLE);
        }

    }

    private void DeclineFriendRequest() {

        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendfriendRequestButton.setEnabled(true);
                                                CURRENT_STATE="not_friends";
                                                SendfriendRequestButton.setText("Send Friend Request");

                                                DeclinefriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclinefriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void UnFriendaFriend() {
        FriendsReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendsReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendfriendRequestButton.setEnabled(true);
                                                CURRENT_STATE="not_friends";
                                                SendfriendRequestButton.setText("Send Friend Request");

                                                DeclinefriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclinefriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest() {
        Calendar calForAte=Calendar.getInstance();
        final SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
        final String saveCurrentDate=currentDate.format(calForAte.getTime());

        FriendsReference.child(sender_user_id).child(receiver_user_id).child("date").setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FriendsReference.child(receiver_user_id).child(sender_user_id).child("date").setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                SendfriendRequestButton.setEnabled(true);
                                                                                CURRENT_STATE="friends";
                                                                                SendfriendRequestButton.setText("Unfriend this Person");

                                                                                DeclinefriendRequestButton.setVisibility(View.INVISIBLE);
                                                                                DeclinefriendRequestButton.setEnabled(false);
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void CancelFriendRequest() {
        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendfriendRequestButton.setEnabled(true);
                                                CURRENT_STATE="not_friends";
                                                SendfriendRequestButton.setText("Send Friend Request");

                                                DeclinefriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclinefriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendfriendRequestToAPerson() {
        FriendRequestReference.child(sender_user_id).child(receiver_user_id).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestReference.child(receiver_user_id).child(sender_user_id)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                HashMap<String,String>notificationsData=new HashMap<>();
                                                notificationsData.put("from",sender_user_id);
                                                notificationsData.put("type","request");

                                                NotificationReference.child(receiver_user_id).push().setValue(notificationsData)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    SendfriendRequestButton.setEnabled(true);
                                                                    CURRENT_STATE="request_sent";
                                                                    SendfriendRequestButton.setText("Cancel Friend Request");

                                                                    DeclinefriendRequestButton.setVisibility(View.INVISIBLE);
                                                                    DeclinefriendRequestButton.setEnabled(false);
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
