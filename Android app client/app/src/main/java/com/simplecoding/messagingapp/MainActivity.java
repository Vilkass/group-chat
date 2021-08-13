package com.simplecoding.messagingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private Button loginBtn;
    private EditText usernameField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpComponents();

    }

    private void setUpComponents(){
        loginBtn = (Button)findViewById(R.id.loginButton);
        usernameField = (EditText)findViewById(R.id.usernameField);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginPressed();
            }
        });
    }

    private void loginPressed(){
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        if(!usernameField.getText().toString().isEmpty()){
            // Username provided
            intent.putExtra("username", usernameField.getText().toString());
            intent.putExtra("ghostMode", false);
        }else{
            // Ghost mode
            intent.putExtra("username", "");
            intent.putExtra("ghostMode", true);
        }
        startActivity(intent);
        usernameField.setText("");
    }

}