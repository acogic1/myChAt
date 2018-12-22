package ba.unsa.etf.rma.adem.mychat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartPageActivity extends AppCompatActivity {

    private Button NeedNewAccountButton;
    private Button AlreadyHaveAccountButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        NeedNewAccountButton=(Button)findViewById(R.id.need_account_btn);
        AlreadyHaveAccountButton=(Button)findViewById(R.id.alread_have_account_btn);


        NeedNewAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent=new Intent(StartPageActivity.this,RegistryActivity.class);
                startActivity(registerIntent);
            }
        });


        AlreadyHaveAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent=new Intent(StartPageActivity.this,LoginActivity.class);
                startActivity(loginIntent);
            }
        });
    }
}
