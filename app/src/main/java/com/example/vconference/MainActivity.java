package com.example.vconference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UserListner{
    Toolbar toolbar;
    TextView username,signOut;
    private PreferanceManager preferanceManager;
    UserAddatpter userAddatpter;
    List<Users> usersList;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager linearLayoutManager;
    private ImageView conferenceimg;
    private static final int requestCode_battery_optmizaition=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar=findViewById(R.id.toobar);
        username=findViewById(R.id.usernm);
        signOut=findViewById(R.id.signout);
        recyclerView=findViewById(R.id.Recyclerview);
        swipeRefreshLayout=findViewById(R.id.swipelayout);
        conferenceimg=findViewById(R.id.vidoconference);
        preferanceManager=new PreferanceManager(getApplicationContext());
        getSupportActionBar();

        username.setText(String.format("%s",preferanceManager.getString(Constants.Key_name)));
       FirebaseInstanceId.getInstance().getInstanceId()
               .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(task.isSuccessful()&& task.getResult()!=null){
                           sendFcmToken(task.getResult().getToken());
                            Log.i("getToken" ,task.getResult().getToken());
                        }
                   }
               });
       getUserdata();
       usersList=new ArrayList<>();
        linearLayoutManager=new LinearLayoutManager(this);
        userAddatpter =new UserAddatpter(this,usersList,this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(userAddatpter);
//        FirebaseMessaging.getInstance().getToken()
//              .addOnCompleteListener(new OnCompleteListener<String>() {
//                   @Override
//                   public void onComplete(@NonNull Task<String> task) {
//                      if(task.isSuccessful() && task.getResult()!=null){
//                         String token= task.getResult();
//                         sendFcmToken(token);
//                         Log.i("testToken",token);
//                      }
//                   }
//              });
    signOut.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Signout();
        }
    });
    swipeRefreshLayout.setOnRefreshListener(this::getUserdata);
    batteryoptmizaiton();
    }
    private void sendFcmToken(String token){
        FirebaseFirestore database =FirebaseFirestore.getInstance();
        DocumentReference documentReference=
                database.collection(Constants.Key_collections)
                        .document(preferanceManager.getString(Constants.Key_userId));
      documentReference.update(Constants.Fcm_token,token)
              .addOnSuccessListener(new OnSuccessListener<Void>() {
                  @Override
                  public void onSuccess(Void aVoid) {
                    //  Toast.makeText(MainActivity.this, "Token updated successfully", Toast.LENGTH_SHORT).show();
                  }
              }).addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
              //Toast.makeText(MainActivity.this, "unenable to update token", Toast.LENGTH_SHORT).show();
          }
      });
    }
    private void Signout(){
        FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
        DocumentReference documentReference=
                firebaseFirestore.collection(Constants.Key_collections)
                .document(preferanceManager.getString(Constants.Key_userId));
        HashMap<String,Object>hashMap=new HashMap<>();
        //hashMap.put(Constants.Fcm_token, FieldValue.delete());
        documentReference.update(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        preferanceManager.clear();
                        Toast.makeText(MainActivity.this, "Sign out successfully", Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(MainActivity.this,Sign_in.class);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "unEnable to signout", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void getUserdata(){
        swipeRefreshLayout.setRefreshing(true);
        FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseFirestore.collection(Constants.Key_collections)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        swipeRefreshLayout.setRefreshing(false);
                        String myuserId=preferanceManager.getString(Constants.Key_userId);
                        if(task.isSuccessful()&& task.getResult()!=null){
                            usersList.clear();

                            for(QueryDocumentSnapshot documentSnapshot:task.getResult()){
                                Users users=new Users();
                                if(myuserId.equals(documentSnapshot.getId())){
                                    continue;
                                }
                                users.username =documentSnapshot.getString(Constants.Key_name);
                               users.email=documentSnapshot.getString(Constants.key_email);
                               users.token =documentSnapshot.getString(Constants.Fcm_token);
                                usersList.add(users);
                            }
                            if(usersList.size()>0){
                                userAddatpter.notifyDataSetChanged();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"unenable to fech data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void audiomeeting(Users users) {
        if(users.token!=null || !users.token.trim().isEmpty()){
            Intent intent=new Intent(MainActivity.this,OutgoingActivity.class);
            intent.putExtra("username",users);
            intent.putExtra("type","audio");
            startActivity(intent);
           // Toast.makeText(this, "User is available for audio meeting", Toast.LENGTH_SHORT).show();
        }
        else {

           // Toast.makeText(this, "User is notavailable for audio meeting", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void videomeeting(Users users) {
if(users.token!=null || !users.token.trim().isEmpty()){
    Intent intent=new Intent(MainActivity.this,OutgoingActivity.class);
    intent.putExtra("username",users);
    intent.putExtra("type","video");
    startActivity(intent);
    Toast.makeText(this, "User is available for video meeting", Toast.LENGTH_SHORT).show();
}
else {
    Toast.makeText(this, "User is not available for video meeting", Toast.LENGTH_SHORT).show();
}
    }

    @Override
    public void multipleUserAction(Boolean isMultipleuserSelected) {
        if(isMultipleuserSelected){
            conferenceimg.setVisibility(View.VISIBLE);
            conferenceimg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(MainActivity.this,OutgoingActivity.class);
                    intent.putExtra("selectedUsers",new Gson().toJson(userAddatpter.getSelecteduers()));
                    intent.putExtra("type","video");
                    intent.putExtra("isMultipleUsers",true);
                    startActivity(intent);
                }
            });
        }
        else {
            conferenceimg.setVisibility(View.GONE);
        }
    }
    private void batteryoptmizaiton(){
        PowerManager powerManager=(PowerManager)getSystemService(POWER_SERVICE);
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("battery power service is enable .it can interrupt backgroun running service");
        builder.setPositiveButton("Disable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent=new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                 startActivityForResult(intent,requestCode_battery_optmizaition);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==requestCode_battery_optmizaition){
            batteryoptmizaiton();
        }
    }
}