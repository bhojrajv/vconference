package com.example.vconference;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class UserAddatpter extends RecyclerView.Adapter<UserAddatpter.Viewholder> {
    Context context;
    List<Users>usersList;
    UserListner userListner;
    List<Users>selecteduers;
    public UserAddatpter(MainActivity mainActivity, List<Users> usersList,UserListner userListner) {
        this.context=mainActivity;
        this.usersList=usersList;
        this.userListner=userListner;
        selecteduers=new ArrayList<>();
    }
 public List<Users>getSelecteduers(){
        return selecteduers;
 }
    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=layoutInflater.inflate(R.layout.userdata_list,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
//        holder.fstchar.setText(usersList.get(position).getUsername().substring(0,1));
//        holder.usernm.setText(usersList.get(position).getUsername());
        holder.setData(usersList.get(position)
        );

    }



    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        TextView fstchar,usernm,useremail;
        ImageView vmeeting,aumeeting;
        RelativeLayout imagecontainer;
        ImageView selecteduserimg;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            fstchar=itemView.findViewById(R.id.firstchar);
            usernm=itemView.findViewById(R.id.UserName);
            vmeeting=itemView.findViewById(R.id.vieomeeting);
            aumeeting=itemView.findViewById(R.id.audiomeeting);
            useremail=itemView.findViewById(R.id.Useremail);
            imagecontainer=itemView.findViewById(R.id.imagecontainer);
            selecteduserimg=itemView.findViewById(R.id.imageselector);


        }
        private void setData(Users users){
            fstchar.setText(users.username.substring(0,1));
            usernm.setText(users.username);
            useremail.setText(users.email);
            vmeeting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userListner.videomeeting(users);
                }
            });
            aumeeting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userListner.audiomeeting(users);
                }
            });
            imagecontainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(selecteduserimg.getVisibility()!=v.VISIBLE){
                        selecteduers.add(users);
                        selecteduserimg.setVisibility(View.VISIBLE);
                        vmeeting.setVisibility(View.GONE);
                        aumeeting.setVisibility(View.GONE);
                        userListner.multipleUserAction(true);
                    }

                    return true;
                }
            });
            imagecontainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selecteduserimg.getVisibility()==v.VISIBLE){
                        selecteduers.remove(users);
                        selecteduserimg.setVisibility(View.GONE);
                        vmeeting.setVisibility(View.VISIBLE);
                        aumeeting.setVisibility(View.VISIBLE);
                        if(selecteduers.size()==0){
                            userListner.multipleUserAction(false);
                        }

                    }
                    else {
                        if(selecteduers.size()>0){
                            selecteduers.add(users);
                            selecteduserimg.setVisibility(View.VISIBLE);
                            vmeeting.setVisibility(View.GONE);
                            aumeeting.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
    }

}
