package ba.unsa.etf.rma.adem.mychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegistryActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference storeUserDefaultDataReference;

    private android.support.v7.widget.Toolbar registerToolbar;
    private ProgressDialog loadingBar;

    private EditText registerName;
    private EditText registerEmail;
    private EditText registerPassword;
    private Button create_account_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registry);

        mAuth=FirebaseAuth.getInstance();


        registerToolbar=(android.support.v7.widget.Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(registerToolbar);
        getSupportActionBar().setTitle("Sign Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        registerName=(EditText)findViewById(R.id.register_name);
        registerEmail=(EditText)findViewById(R.id.register_email);
        registerPassword=(EditText)findViewById(R.id.register_password);
        create_account_btn=(Button)findViewById(R.id.create_account_btn);
        loadingBar=new ProgressDialog(this);

        create_account_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name=registerName.getText().toString();
                String email=registerEmail.getText().toString();
                String password=registerPassword.getText().toString();

                RegisterAccount(name,email,password);
            }
        });
    }

    private void RegisterAccount(final String name, String email, String password) {

        if(TextUtils.isEmpty(name)){
            Toast.makeText(RegistryActivity.this,"Please write your name.",Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(email)){
            Toast.makeText(RegistryActivity.this,"Please write your email.",Toast.LENGTH_LONG).show();
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(RegistryActivity.this,"Please write your password.",Toast.LENGTH_LONG).show();
        }
        else {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we creatin account for you.");
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String DeviceToken=FirebaseInstanceId.getInstance().getToken();

                        String current_user_id =mAuth.getCurrentUser().getUid();
                        storeUserDefaultDataReference= FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);

                        storeUserDefaultDataReference.child("user_name").setValue(name);
                        storeUserDefaultDataReference.child("user_status").setValue("Hey there, i am using myChAt app, developed by Choga.");
                        storeUserDefaultDataReference.child("user_image").setValue("default_profile");
                        storeUserDefaultDataReference.child("device_token").setValue(DeviceToken);
                        storeUserDefaultDataReference.child("user_thumb_image").setValue("default_image")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Intent mainIntent=new Intent(RegistryActivity.this,MainActivity.class);
                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(mainIntent);
                                            finish();
                                        }
                                    }
                                });
                    }
                    else {
                        Toast.makeText(RegistryActivity.this,"Error Occured, Try Again...",Toast.LENGTH_LONG).show();
                    }

                    loadingBar.dismiss();
                }
            });
        }
    }
}
