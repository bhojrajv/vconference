package com.example.vconference;

public interface UserListner {
    public void audiomeeting(Users users);
    public void videomeeting(Users users);
    public void multipleUserAction(Boolean isMultipleuserSelected);
}
