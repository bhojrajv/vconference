package com.example.vconference;

import java.util.HashMap;

public class Constants {
    public static final String Key_name="userName";
    public static final String Key_collections="users";
    public static final String key_email="Email";
    public static final String key_password="password";
    public static final String key_preferencename="vconference";
    public static final String key_isSigned="isSigned";
    public static final String Key_userId="user_id";
    public static final String Fcm_token="Token";
    public static final String REMOTE_MSG_TYPE="type";
    public static final String REMOTE_MSG_INIVITATION="invitation";
    public static final String REMOTE_MSG_DATA="data";
    public static final String REMOTE_MSG_REGISTRATION_IDS="registration_ids";
    public static final String REMOTE_MSG_MEETING_TYPE="meetingType";
    public static final String REMOTE_MSG_INVITER_TOKEN="inviterToken";
    public static final String REMOTE_MSG_AUTHORIZATION="Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE="Content-Type";
    public static final String REMOTE_MSG_REMOPTE_INVITIATIONRESPONSE="invitationrespnse";
    public static final String REMOTE_MSG_REMOPTE_ACCEPTED="accepted";
    public static final String REMOTE_MSG_REMOPTE_rejected="rejected";
    public static final String REMOTE_MSG_REMOPTE_Canceled="canceled";
    public static final String REMOTE_MSG_MEETING_ROOM="meetingRoom";

    public static final HashMap<String,String>getremoteMessage(){
        HashMap<String,String>hashMap=new HashMap<>();
        hashMap.put(REMOTE_MSG_CONTENT_TYPE,"application/json");
        hashMap.put(REMOTE_MSG_AUTHORIZATION,"key=AAAAx_4Biz0:APA91bFm4BJu6q5VmBI5tqpIk0-ctekbgYFclZxX92KR3IlckvlUTeA5QcqpXexAZzsYqyX816VnD_DhJRqq9w6sis0Keb8eyzYalm3KqkB25uqiUG2XrU6SMsoU5Lv5p8czo7smy9gv");

        return  hashMap;
    }



}
