package com.raphanum.githubusersearch;

import android.os.Bundle;

import java.util.List;

public class RetainedFragment extends android.support.v4.app.Fragment {

    private List<User> userList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    public void setUserList(List<User> userList)
    {
        this.userList = userList;
    }

    public List<User> getUserList() {
        return userList;
    }
}
