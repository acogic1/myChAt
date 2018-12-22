package ba.unsa.etf.rma.adem.mychat;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    private List<Messages> userMessagesList;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersDatabaseReference;

    public MessageAdapter(List<Messages> userMessagesList){
        this.userMessagesList=userMessagesList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View v=LayoutInflater.from(parent.getContext())
                .inflate(R.layout.messages_layout_of_users,parent,false);

        mAuth=FirebaseAuth.getInstance();

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {
        String message_sender_id=mAuth.getCurrentUser().getUid();
        Messages messages=userMessagesList.get(position);

        String from_user_id=messages.getFrom();
        String from_Message_type=messages.getType();

        UsersDatabaseReference=FirebaseDatabase.getInstance().getReference().child("Users").child(from_user_id);
        UsersDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userName=dataSnapshot.child("user_name").getValue().toString();
                String userImage=dataSnapshot.child("user_thumb_image").getValue().toString();

                Picasso.get().load(userImage).placeholder(R.drawable.default_profile).into(holder.profileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        if(from_Message_type.equals("text")){
            holder.messagePicture.setVisibility(View.INVISIBLE);

            if(from_user_id.equals(message_sender_id)){
                holder.messageText.setBackgroundResource(R.drawable.message_text_background_two);

                holder.messageText.setTextColor(Color.BLACK);

                holder.messageText.setGravity(Gravity.RIGHT);
            }
            else {
                holder.messageText.setBackgroundResource(R.drawable.message_text_background);

                holder.messageText.setTextColor(Color.WHITE);

                holder.messageText.setGravity(Gravity.LEFT);

            }
            holder.messageText.setText(messages.getMessage());
        }
        else {
            holder.messageText.setVisibility(View.INVISIBLE);
            holder.messageText.setPadding(0,0,0,0);


            Picasso.get().load(messages.getMessage()).placeholder(R.drawable.default_profile).into(holder.messagePicture);
        }


    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageText;
        public CircleImageView profileImage;

        public ImageView messagePicture;

        public MessageViewHolder(View view){
            super(view);

            messageText=(TextView)view.findViewById(R.id.message_text);
            messagePicture=(ImageView)view.findViewById(R.id.message_image_ciew);
            profileImage=(CircleImageView)view.findViewById(R.id.message_profile_image);
        }
    }
}
