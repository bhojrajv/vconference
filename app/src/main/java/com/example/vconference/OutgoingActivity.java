package com.example.vconference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import javax.security.auth.login.LoginException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class OutgoingActivity extends AppCompatActivity {
    private TextView frstchar,username,sendemail;
    private ImageView acceptimg,cancelimg,meetingtypeimg;
    private Users users;
    private PreferanceManager preferanceManager;
    private String inviterToken=null;
    String meetingType=null;
    private String meetingRoom=null;
    private int rejectcount=0;
    private int totalcount=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing);
        frstchar=findViewById(R.id.urfrstchar);
        username=findViewById(R.id.sendName);
        sendemail=findViewById(R.id.sendemail);
        //acceptimg=findViewById(R.id.pickimg);
        cancelimg=findViewById(R.id.sendcancleimg);
        meetingtypeimg=findViewById(R.id.incomingmeetimg);
        preferanceManager=new PreferanceManager(getApplicationContext());
        users =new Users();
      meetingType =getIntent().getStringExtra("type");
        users=(Users)getIntent().getSerializableExtra("username");

        if( meetingType!=null){
            if( meetingType.equals("video")){
                meetingtypeimg.setImageResource(R.drawable.videomeeting);
            }
            else {
                meetingtypeimg.setImageResource(R.drawable.audiomeeting);
            }
        }

        if(users!=null){
            frstchar.setText(users.username.substring(0,1));
            username.setText(users.username);
            sendemail.setText(users.email);

        }
        cancelimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                if(getIntent().getBooleanExtra("isMultipleUsers",false)){
                    Type type= new TypeToken<ArrayList<Users>>(){

                    }.getType();
                    ArrayList<Users>recieverView=new Gson()
                            .fromJson(getIntent().getStringExtra("selectedUsers"),type);
                    cancelInvitationResopnse( null,recieverView);
                }else {
                    if(users!=null){
                        cancelInvitationResopnse(users.token,null);
                    }
                }


            }
        });

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(task.isSuccessful() && task.getResult()!=null){
                            inviterToken=task.getResult().getToken();
                            if(meetingType!=null){
                                if(getIntent().getBooleanExtra("isMultipleUsers",false)){
                                    Type type= new TypeToken<ArrayList<Users>>(){

                                    }.getType();
                                    ArrayList<Users>recieverView=new Gson()
                                            .fromJson(getIntent().getStringExtra("selectedUsers"),type);
                                    if(recieverView!=null)
                                    {
                                        totalcount=recieverView.size();
                                    }
                                    initiateMeeting( meetingType,null,recieverView);
                                }
                                else {
                                    Log.i("inviter2",inviterToken);
                                    if(  users!=null){
                                        totalcount=1;
                                        initiateMeeting( meetingType,users.token,null);
                                        Log.i("invitertoken",users.token);
                                        Log.i("mtype",meetingType);
                                    }
                                }
                            }


                        }
                    }
                });


    }
    private void sendMeetingInvitation(String remotemsgBody,String type){
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/fcm/ ")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        Apiservice apiservice=retrofit.create(Apiservice.class);
        Call<String>call=apiservice.remote(Constants.getremoteMessage(),remotemsgBody);
//        UserClient.getRetrofitclinet().create(Apiservice.class).remote(
//                Constants.getremoteMessage(),remotemsgBody
//        )
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                String res=response.body();
                Log.i("resp",response.message().toLowerCase());
                //Log.i("errorcode",response.errorBody().toString());
                Log.i("code",String.valueOf(response.code()));
                Log.i("returntype",Constants.getremoteMessage().toString());
                if(response.isSuccessful()){
                    if(type.equals(Constants.REMOTE_MSG_INIVITATION)){
                        Log.i("invite","send invitation successfully");
                        Log.i("m2type",
                                Constants.REMOTE_MSG_INIVITATION);
                        Toast.makeText(OutgoingActivity.this, "invitation send successfully", Toast.LENGTH_SHORT).show();
                   } else if(type.equals(Constants.REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE)){
                        Toast.makeText(OutgoingActivity.this, "invitation canceled", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(OutgoingActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else {
                    Toast.makeText(OutgoingActivity.this, "invitation did't send successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(OutgoingActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

private void initiateMeeting(String meetingType, String receiverToken, ArrayList<Users>recieverView){
    try {
        JSONArray tokens=new JSONArray();
        if(receiverToken!=null){
            tokens.put(receiverToken);
        }
        StringBuilder builder=new StringBuilder();
       if(recieverView!=null && recieverView.size()>0){
           for(int i=0;i<recieverView.size();i++){
               tokens.put(recieverView.get(i).token);
               builder.append(recieverView.get(i).username).append("\n");

           }
           frstchar.setVisibility(View.GONE);
           sendemail.setVisibility(View.GONE);
           username.setText(builder.toString());

       }
        JSONObject body=new JSONObject();
       JSONObject data=new JSONObject();
        data.put(Constants.REMOTE_MSG_TYPE,Constants.REMOTE_MSG_INIVITATION);
        data.put(Constants.REMOTE_MSG_MEETING_TYPE,meetingType);
        data.put(Constants.Key_name,preferanceManager.getString(Constants.Key_name));
        data.put(Constants.key_email,preferanceManager.getString(Constants.key_email));
        data.put(Constants.REMOTE_MSG_INVITER_TOKEN,inviterToken);
        meetingRoom=preferanceManager.getString(Constants.Key_userId)+"-"+
                UUID.randomUUID().toString().substring(0,5);
        data.put(Constants.REMOTE_MSG_MEETING_ROOM,meetingRoom);
        body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);
        body.put(Constants.REMOTE_MSG_DATA,data);
        sendMeetingInvitation(body.toString(),Constants.REMOTE_MSG_INIVITATION);
    }catch (Exception e){
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        finish();
    }
}
    private void sendInvitationResopnse(String receiverToken){
        try{
            JSONArray tokens=new JSONArray();
            tokens.put(receiverToken);

            JSONObject data=new JSONObject();
            JSONObject body=new JSONObject();
            data.put(Constants.REMOTE_MSG_TYPE,Constants.REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE);
            data.put(Constants.REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE,Constants.REMOTE_MSG_REMOPTE_Canceled);
            body.put(Constants.REMOTE_MSG_DATA,data);
            body.put(Constants.REMOTE_MSG_INVITER_TOKEN,tokens);
            sendMeetingInvitation(body.toString(),Constants.REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
   private BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type=intent.getStringExtra(Constants.REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE);
            if(type!=null){
                if(type.equals(Constants.REMOTE_MSG_REMOPTE_ACCEPTED)){

                    JitsiMeetConferenceOptions conferenceOptions
                            = null;
                    try {
                        conferenceOptions = new JitsiMeetConferenceOptions.Builder()
                                .setServerURL(new URL("https://meet.jit.si"))
                                .setRoom(Constants.REMOTE_MSG_MEETING_ROOM)
                                .setFeatureFlag("invite.enabled", false)
                                .setFeatureFlag("pip.enabled", true)
                                .setWelcomePageEnabled(false)
                                .build();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        finish();
                    }
                    JitsiMeetActivity.launch(OutgoingActivity.this,conferenceOptions);
                        finish();

                    Toast.makeText(context, "invitation accepted", Toast.LENGTH_SHORT).show();
                }else if(type.equals(Constants.REMOTE_MSG_REMOPTE_rejected)){
                    rejectcount+=1;
                    if(rejectcount==totalcount)
                    {
                        Toast.makeText(context, "invitation rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }

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
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
    }
    private void cancelInvitationResopnse(String receiverToken,ArrayList<Users>recieviewer){
        try{
            JSONArray tokens=new JSONArray();
            if(receiverToken!=null){
                tokens.put(receiverToken);
            }
          if(recieviewer!=null && recieviewer.size()>0){
              for(Users users:recieviewer){
                  tokens.put(users.token);
              }
          }

            JSONObject data=new JSONObject();
            JSONObject body=new JSONObject();
            data.put(Constants.REMOTE_MSG_TYPE,Constants.REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE);
            data.put(Constants.REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE,Constants.REMOTE_MSG_REMOPTE_Canceled);
            body.put(Constants.REMOTE_MSG_DATA,data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);
            sendMeetingInvitation(body.toString(),Constants.REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE);
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }
}