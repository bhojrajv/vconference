package com.example.vconference;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.auth.User;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class InvitationActivity extends AppCompatActivity {
  private TextView frstchar,username,incomingemail;
  private ImageView acceptimg,cancelimg,meetingtypeimg;
  private Users users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);
        frstchar=findViewById(R.id.userfrstchar);
        username=findViewById(R.id.incomingName);
        acceptimg=findViewById(R.id.pickimg);
        cancelimg=findViewById(R.id.cancleimg);
        incomingemail=findViewById(R.id.incomingemail);
        meetingtypeimg=findViewById(R.id.incomingmeetimgvideo);
        users =new Users();
        String type=getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);
        users=(Users)getIntent().getSerializableExtra("username");
        if(type!=null){
            if(type.equals("video")){
                meetingtypeimg.setImageResource(R.drawable.videomeeting);
            }
            else {
                meetingtypeimg.setImageResource(R.drawable.audiomeeting);
            }
        }

         String name=getIntent().getStringExtra(Constants.Key_name);
        String email=getIntent().getStringExtra(Constants.key_email);
       if(name!=null && email!=null){
           frstchar.setText(name.substring(0,1));
           username.setText(name);
           incomingemail.setText(email);
       }
       acceptimg.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

                   sendInvitationResopnse(Constants.REMOTE_MSG_REMOPTE_ACCEPTED,
                           getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN));

           }
       });
 cancelimg.setOnClickListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {

             sendInvitationResopnse(Constants.REMOTE_MSG_REMOPTE_rejected,
                     getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN));

     }
 });
    }

    private void sendMeetingInvitation(String remotemsgBody,String type){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/fcm/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        Apiservice apiservice=retrofit.create(Apiservice.class);
        Call<String>call=apiservice.remote(Constants.getremoteMessage(),remotemsgBody);
//        UserClient.getRetrofitclinet().create(Apiservice.class).remote(
//                Constants.getremoteMessage(),remotemsgBody
//        )
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    if(type.equals(Constants.REMOTE_MSG_REMOPTE_ACCEPTED)){
                        //JitsiMeetView view=new JitsiMeetView(InvitationActivity.this);
                        JitsiMeetConferenceOptions conferenceOptions=null;
                        try {

                            conferenceOptions = new JitsiMeetConferenceOptions.Builder()
                                    .setServerURL(new URL("https://meet.jit.si"))
                                    .setRoom(Constants.REMOTE_MSG_MEETING_ROOM)
                                    .setFeatureFlag("invite.enabled", false)
                                    .setFeatureFlag("pip.enabled", true)
                                    .setWelcomePageEnabled(false)
                                    .build();


                        }catch (Exception e){
                            Toast.makeText(InvitationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        JitsiMeetActivity.launch(InvitationActivity.this,conferenceOptions);
                        finish();

                        Toast.makeText(InvitationActivity.this, "invitation accepted", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(InvitationActivity.this, "invitation rejected", Toast.LENGTH_SHORT).show();

                    }
                }else {
                    Toast.makeText(InvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                }
                finish();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(InvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }
    private void sendInvitationResopnse(String type,String receiverToken){
        try{
            JSONArray tokens=new JSONArray();
            tokens.put(receiverToken);

            JSONObject data=new JSONObject();
            JSONObject body=new JSONObject();
            data.put(Constants.REMOTE_MSG_TYPE,Constants.REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE);
            data.put(Constants.REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE,type);
            body.put(Constants.REMOTE_MSG_DATA,data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);
            sendMeetingInvitation(body.toString(),type);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }
   private BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type=intent.getStringExtra(Constants.REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE);
            if(type!=null){
                if(type.equals(Constants.REMOTE_MSG_REMOPTE_Canceled)){
                    Toast.makeText(context, "invitation canceled", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiver,new IntentFilter(Constants.REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(broadcastReceiver);
    }
}