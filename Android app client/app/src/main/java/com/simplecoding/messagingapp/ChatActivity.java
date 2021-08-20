package com.simplecoding.messagingapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.simplecoding.messagingapp.client.Client;
import com.simplecoding.messagingapp.config.ServerConfig;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.NoSuchElementException;

public class ChatActivity extends AppCompatActivity {

    // Front-End components
    private TextView chat;
    private Button sendBtn;
    private Button discBtn;
    private TextView userLabel;
    private EditText messageField;

    // Back-End components
    private Intent intent;
    Client client;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setUpComponents();

        try {
            Thread clientThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    client = new Client(username, chat);

                }
            });
            clientThread.start();
        }catch (NoSuchElementException e){

            // Username taken alert
            new AlertDialog.Builder(ChatActivity.this).setTitle("Error")
                    .setMessage("Username is taken!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();

        }

    }


    private void setUpComponents(){
        intent = getIntent();
        userLabel = (TextView)findViewById(R.id.userLabel);
        username = intent.getStringExtra("username");
        userLabel.setText("User: " + username);
        chat = (TextView)findViewById(R.id.chatText);
        chat.setMovementMethod(new ScrollingMovementMethod());
        discBtn = (Button)findViewById(R.id.discBtn);
        sendBtn = (Button)findViewById(R.id.sendBtn);
        messageField = (EditText)findViewById(R.id.messageField);
        if(intent.getBooleanExtra("ghostMode", true)){
            // Ghost mode
            messageField.setVisibility(View.INVISIBLE);
            sendBtn.setVisibility(View.INVISIBLE);
            userLabel.setVisibility(View.INVISIBLE);
            username = null;
        }

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                client.getCLIENT_MESSAGES().add(messageField.getText().toString());
                messageField.setText("");
            }
        });

        discBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                client.setLoop(false);
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}



