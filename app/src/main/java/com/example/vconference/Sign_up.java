package com.example.vconference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sign_up extends AppCompatActivity {
    private Button signup;
private    TextView signInview;
EditText email,password,confpass,username;
private FirebaseAuth mauth;
private PreferanceManager preferanceManager;
private FirebaseFirestore firebaseFirestore;
private DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        signup=findViewById(R.id.sign_UpBtn);
        signInview=findViewById(R.id.sign_Inview);
        email=findViewById(R.id.Signup_email);
        password=findViewById(R.id.pass);
        confpass=findViewById(R.id.confpass);
        username=findViewById(R.id.user_name);
        firebaseFirestore=FirebaseFirestore.getInstance();
        preferanceManager=new PreferanceManager(getApplicationContext());
   mauth=FirebaseAuth.getInstance();
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sign_email=email.getText().toString().trim();
                String pass=password.getText().toString().trim();
                String passconf=confpass.getText().toString().trim();
                String usname=username.getText().toString().trim();
                SignUP(sign_email,pass,passconf,usname);
            }
        });
  signInview.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          Intent intent=new Intent(Sign_up.this,Sign_in.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(intent);
      }
  });
    }

    private void SignUP(String sign_email, String pass,String confpas,String usernm) {
        if(TextUtils.isEmpty(sign_email)) {
            email.setError("Please fill all the above fields");
            email.requestFocus();
        }
        else if (TextUtils.isEmpty(pass)){

            password.setError("Please fill all the above fields ");
            password.requestFocus();

        }
        else if(  TextUtils.isEmpty(confpas)){
            confpass.setError("Please fill all the above fields ");
            confpass.requestFocus();
        }
        else if (TextUtils.isEmpty(usernm))
        {
            username.setError("please enter your name");
             username.requestFocus();
        }
        else if(!pass.equals(confpas)){
            confpass.setError("Please enter correct match password ");
            confpass.requestFocus();
        }
        else {
            mauth.createUserWithEmailAndPassword(sign_email,confpas)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Sign_up.this, "Sign Up successfully", Toast.LENGTH_SHORT).show();
                                Storeuserdata(sign_email,confpas,usernm);

                            }
                            else {
                                Toast.makeText(Sign_up.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private void Storeuserdata(String useremail,String userpass,String usernm){
        HashMap<String ,Object>user=new HashMap<>();
        user.put(Constants.key_email,useremail);
        user.put(Constants.Key_name,usernm);
        user.put(Constants.key_password,userpass);
        firebaseFirestore.collection(Constants.Key_collections)
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        preferanceManager.putBoolean(Constants.key_isSigned,true);
                        preferanceManager.putString(Constants.Key_userId,documentReference.getId());
                        preferanceManager.putString(Constants.Key_name,usernm);
                        preferanceManager.putString(Constants.key_email,useremail);
                        preferanceManager.putString(Constants.key_password,userpass);
                        Intent intent=new Intent(Sign_up.this,Sign_in.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        Toast.makeText(Sign_up.this, "Store use data successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Sign_up.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}