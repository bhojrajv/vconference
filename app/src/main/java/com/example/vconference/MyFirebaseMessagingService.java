package com.example.vconference;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.i("token" ,s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
//        if(remoteMessage.getNotification()!=null){
//           Log.i("token",remoteMessage.getNotification().getBody());
//           }
        String type=remoteMessage.getData().get(Constants.REMOTE_MSG_TYPE);
        if(type!=null){
           if (type.equals(Constants.REMOTE_MSG_INIVITATION)){
                Intent intent=new Intent(getApplicationContext(),InvitationActivity.class);
                intent.putExtra(Constants.REMOTE_MSG_MEETING_TYPE,
                       remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_TYPE));
                intent.putExtra(Constants.Key_name,remoteMessage.getData().get(Constants.Key_name));
                intent.putExtra(Constants.key_email,remoteMessage.getData().get(Constants.key_email));
                intent.putExtra(Constants.REMOTE_MSG_INVITER_TOKEN,remoteMessage.getData().get(Constants.REMOTE_MSG_INVITER_TOKEN));
                intent.putExtra(Constants.REMOTE_MSG_INVITER_TOKEN,remoteMessage.getData().get(Constants.REMOTE_MSG_INVITER_TOKEN));
                intent.putExtra(Constants.REMOTE_MSG_MEETING_ROOM,remoteMessage.getData()
                .get(Constants.REMOTE_MSG_MEETING_ROOM));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if(type.equals(Constants.REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE)){
               Intent intent=new Intent(Constants.REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE);
               intent.putExtra(Constants.REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE,remoteMessage.getData().get(Constants.REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE));
               LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
           }
        }

    }
}
