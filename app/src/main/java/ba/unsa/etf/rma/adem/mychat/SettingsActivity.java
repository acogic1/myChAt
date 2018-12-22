package ba.unsa.etf.rma.adem.mychat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.net.Uri;
import android.os.Debug;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
//import com.theartofdev.edmodo.cropper.CropImage;
//import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import io.reactivex.schedulers.Schedulers;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView settingsDisplayImage;
    private TextView settingsDisplayName;
    private TextView settingsDisplayStatus;
    private Button settingsChangeProfileImage;
    private Button settingsChangeStatus;

    private final static int Gallery_Pick=1;
    private StorageReference storeProfileImagestorageRef;

    private DatabaseReference getUserDataReference;
    private FirebaseAuth mAuth;

    Bitmap thumb_bitmap=null;

    private StorageReference thumbImageRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth=FirebaseAuth.getInstance();
        String online_user_id=mAuth.getCurrentUser().getUid();

        getUserDataReference= FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);
        getUserDataReference.keepSynced(true);

        storeProfileImagestorageRef=FirebaseStorage.getInstance().getReference().child("Profile_Images");
        thumbImageRef=FirebaseStorage.getInstance().getReference().child("Thumb_Images");

        settingsDisplayImage=(CircleImageView)findViewById(R.id.settings_profile_image);
        settingsDisplayName=(TextView)findViewById(R.id.settings_username);
        settingsDisplayStatus=(TextView)findViewById(R.id.settings_userstatus);
        settingsChangeProfileImage=(Button)findViewById(R.id.settings_change_profile_image_btn);
        settingsChangeStatus=(Button)findViewById(R.id.settings_change_status_btn);
        loadingBar=new ProgressDialog(this);

        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("user_name").getValue().toString();
                String status=dataSnapshot.child("user_status").getValue().toString();
                final String image=dataSnapshot.child("user_image").getValue().toString();
                String thumb_image=dataSnapshot.child("user_thumb_image").getValue().toString();

                settingsDisplayName.setText(name);
                settingsDisplayStatus.setText(status);

                if(!image.equals("default_profile")){


                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_profile).into(settingsDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.default_profile).into(settingsDisplayImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        settingsChangeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_Pick);
            }
        });

        settingsChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String old_status=settingsDisplayStatus.getText().toString();

                Intent statusIntent=new Intent(SettingsActivity.this,StatusActivity.class);
                statusIntent.putExtra("user_status",old_status);
                startActivity(statusIntent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null){

            loadingBar.setTitle("Updating Profile Image");
            loadingBar.setMessage("Please wait, while we are updating your profile image....");
            loadingBar.show();
            Uri ImageUri=data.getData();

            String filePath1 = null;
            Uri _uri = data.getData();
            Log.d("","URI = "+ _uri);
            if (_uri != null && "content".equals(_uri.getScheme())) {
                Cursor cursor = this.getContentResolver().query(_uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
                cursor.moveToFirst();
                filePath1 = cursor.getString(0);
                cursor.close();
            } else {
                filePath1 = _uri.getPath();
            }
            Log.d("","Chosen path = "+ filePath1);


            File thumb_filePathUri=new File(filePath1);

            Date now = new Date();
            android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            String path = Environment.getExternalStorageDirectory().toString();





            String user_id=mAuth.getCurrentUser().getUid();




            try{
                thumb_bitmap=new Compressor(this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(50)
                        .compressToBitmap(thumb_filePathUri);
            }
            catch (IOException e){
                e.printStackTrace();
            }

            ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
            final byte[]  thumb_byte=byteArrayOutputStream.toByteArray();

            StorageReference filePath=storeProfileImagestorageRef.child(user_id + ".jpg");

            final StorageReference thumb_filePath=thumbImageRef.child(user_id + ".jpg");


            filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(SettingsActivity.this,"Saving your profile image to Firebase Storage",Toast.LENGTH_LONG).show();

                        final String download_url=task.getResult().getDownloadUrl().toString();

                        UploadTask uploadTask=thumb_filePath.putBytes(thumb_byte);

                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                String thumb_downloadUrl=thumb_task.getResult().getDownloadUrl().toString();

                                if(thumb_task.isSuccessful()){
                                    Map update_user_data=new HashMap();
                                    update_user_data.put("user_image",download_url);
                                    update_user_data.put("user_thumb_image",thumb_downloadUrl);

                                    getUserDataReference.updateChildren(update_user_data)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(SettingsActivity.this,"Profile Image Updated Sucessifully ...",Toast.LENGTH_LONG).show();
                                            loadingBar.dismiss();
                                        }
                                    });
                                }
                            }
                        });

                    }
                    else {
                        Toast.makeText(SettingsActivity.this,"Error occured, while uploading your profile pic.",Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }



}
