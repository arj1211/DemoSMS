package com.massey2.demosms.demosms;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
{
    EditText msgEdit, numEdit;
    Button sendBtn, addMsgButton, clearPrefsButton, addNumButton;
    ListView msgList, numList;
    ArrayList<String> messages = new ArrayList<>(), numbers = new ArrayList<>();
    BaseAdapter numberAdapter, messageAdapter;

    SharedPreferences preferences;

    static final String MSGKEY = "preset_messages", NUMKEY = "preset_nums", MSGEDITKEY = "msg_edit", NUMEDITKEY = "num_edit", MSGDELIM = "&&&&", NUMDELIM = ",";

    public void sendMessage(View view){
        Intent startNewActivity = new Intent(this, About.class);
        startActivity(startNewActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numEdit = (EditText) findViewById(R.id.numberText);
        msgEdit = (EditText) findViewById(R.id.messageText);
        sendBtn = (Button) findViewById(R.id.sendButton);
        addMsgButton = (Button) findViewById(R.id.addMsgButton);
        addNumButton = (Button) findViewById(R.id.addNumButton);
        clearPrefsButton = (Button) findViewById(R.id.clearPrefsButton);
        msgList = (ListView) findViewById(R.id.messageList);
        numList = (ListView) findViewById(R.id.numberList);

        //Restore prefs
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        msgEdit.setText(preferences.getString(MSGEDITKEY, ""));
        numEdit.setText(preferences.getString(NUMEDITKEY, ""));
        String numStr = preferences.getString(NUMKEY, "");
        String msgStr = preferences.getString(MSGKEY, "");
        String[] numStrArr = numStr.split(NUMDELIM);
        String[] msgStrArr = msgStr.split(MSGDELIM);
        numbers = new ArrayList<>(Arrays.asList(numStrArr));
        messages = new ArrayList<>(Arrays.asList(msgStrArr));

        numberAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, numbers);
        messageAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, messages);

        if (messages.size() < 2)
        {
            messages.add("Stuck in traffic, will be late.");
            messages.add("Picking up dinner.");
            messages.add("Can't talk right now.");
            messages.add("Will call back later.");
            messages.add("Massey Hacks II was lit!.");
        }
//        if (numbers.size() < 2)
//        {
//            numbers.add("6477128595");
//            numbers.add("6477700458");
//            numbers.add("6476877805");
//        }
        msgList.setAdapter(messageAdapter);
        numList.setAdapter(numberAdapter);

        msgList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                msgEdit.setText(msgList.getItemAtPosition(position).toString());
            }
        });
        numList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                numEdit.setText(numList.getItemAtPosition(position).toString());
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendSMS();
            }
        });
        addMsgButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!msgEdit.getText().toString().trim().equals(""))
                {
                    messages.add(msgEdit.getText().toString().trim());
                    messageAdapter.notifyDataSetChanged();
                }
            }
        });
        addNumButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!numEdit.getText().toString().trim().equals("") && numEdit.getText().toString().matches("^[0-9 ]+$"))
                {
                    numbers.add(numEdit.getText().toString().trim());
                    numberAdapter.notifyDataSetChanged();
                }
            }
        });
        clearPrefsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(getApplicationContext(), "cleared prefs", Toast.LENGTH_SHORT).show();
                preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor ed = preferences.edit();
                ed.clear();
                numEdit.setText("");
                msgEdit.setText("");
                numbers.clear();
                messages.clear();
                numberAdapter.notifyDataSetChanged();
                messageAdapter.notifyDataSetChanged();
                ed.apply();
            }
        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        //Toast.makeText(getApplicationContext(), "paused..", Toast.LENGTH_SHORT).show();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor ed = preferences.edit();
        ed.putString(NUMKEY, TextUtils.join(NUMDELIM, numbers));
        ed.putString(MSGKEY, TextUtils.join(MSGDELIM, messages));
        ed.putString(MSGEDITKEY, msgEdit.getText().toString());
        ed.putString(NUMEDITKEY, numEdit.getText().toString());
        ed.apply();
    }

    private void sendSMS()
    {
        try
        {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(numEdit.getText().toString(), null, msgEdit.getText().toString(), null, null);
            Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
        } catch (Exception e)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
            Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}