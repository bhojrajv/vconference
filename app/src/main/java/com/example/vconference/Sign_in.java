package com.example.vconference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class Sign_in extends AppCompatActivity {
    private Button signin;
    private TextView signUpview;
   private EditText email2,password,confpass;
    private FirebaseAuth mauth;
    private FirebaseFirestore db;
    private PreferanceManager preferanceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        signin=findViewById(R.id.sign_inBtn);
        signUpview=findViewById(R.id.sign_Upview);
        email2=findViewById(R.id.SignIn_email);
        password=findViewById(R.id.password);
         mauth=FirebaseAuth.getInstance();
         db=FirebaseFirestore.getInstance();
         preferanceManager=new PreferanceManager(getApplicationContext());
         if(preferanceManager.getBoolean(Constants.key_isSigned))
         {
             Intent intent=new Intent(Sign_in.this,MainActivity.class);
             startActivity(intent);
         }
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String signEmail=email2.getText().toString();
                String pass= password.getText().toString();
                SignIn(signEmail,pass);
            }
        });
        signUpview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartActivity();
            }
        });
    }

    private void SignIn(String signEmail, String pass) {
        if(TextUtils.isEmpty(signEmail)  ) {
            email2.setError("Please fill all the above fields");
            email2.requestFocus();


        }
        else if(TextUtils.isEmpty(pass))
        {
            password.setError("Please fill all the above fields ");
            password.requestFocus();
        }
        else {
            mauth.signInWithEmailAndPassword(signEmail,pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                getUserdata(signEmail,pass);

                            }
                            else {
                                Toast.makeText(Sign_in.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private void StartActivity(){
        Intent intent=new Intent(Sign_in.this,Sign_up.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    private void getUserdata(String email,String pass)
    {
        db.collection(Constants.Key_collections)
                .whereEqualTo(Constants.key_email,email)
                .whereEqualTo(Constants.key_password,pass)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult().getDocuments()!=null&&task.getResult().getDocuments().size()>0){
                            Intent intent=new Intent(Sign_in.this,MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                           DocumentSnapshot documentSnapshot= task.getResult().getDocuments().get(0);
                           preferanceManager.putBoolean(Constants.key_isSigned,true);
                           preferanceManager.putString(Constants.Key_userId,documentSnapshot.getId());
                           preferanceManager.putString(Constants.Key_name,documentSnapshot.get(Constants.Key_name).toString());
                           preferanceManager.putString(Constants.key_email,documentSnapshot.get(Constants.key_email).toString());
                           preferanceManager.putString(Constants.key_password,documentSnapshot.get(Constants.key_password).toString());
                            Toast.makeText(Sign_in.this, "you logged in successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Sign_in.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}