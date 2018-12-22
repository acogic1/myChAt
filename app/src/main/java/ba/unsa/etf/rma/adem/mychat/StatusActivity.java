package ba.unsa.etf.rma.adem.mychat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private Button save_changes_btn;
    private EditText status_input;

    private ProgressDialog loadingBar;

    private DatabaseReference ChangeStatusRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mAuth=FirebaseAuth.getInstance();
        String user_id=mAuth.getCurrentUser().getUid();
        ChangeStatusRef=FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);


        mToolbar=(Toolbar)findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Change status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        save_changes_btn=(Button)findViewById(R.id.save_status_change_btn);
        status_input=(EditText)findViewById(R.id.status_input);
        loadingBar=new ProgressDialog(this);

        String old_status=getIntent().getExtras().get("user_status").toString();
        status_input.setText(old_status);

        save_changes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String new_status=status_input.getText().toString();

                ChangeProfileStatus(new_status);
            }
        });
    }

    private void ChangeProfileStatus(String new_status) {
        if(TextUtils.isEmpty(new_status)){
            Toast.makeText(StatusActivity.this,"Please write your status.",Toast.LENGTH_LONG).show();
        }
        else {
            loadingBar.setTitle("Change Profile Status");
            loadingBar.setMessage("Please whait, while we are updating your profile status...");

            ChangeStatusRef.child("user_status").setValue(new_status).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        loadingBar.dismiss();
                        Intent settingsIntent=new Intent(StatusActivity.this,SettingsActivity.class);
                        startActivity(settingsIntent);

                        Toast.makeText(StatusActivity.this,"Profile Status Updated Sucessfully...",Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(StatusActivity.this,"Error Occured...",Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }
}
