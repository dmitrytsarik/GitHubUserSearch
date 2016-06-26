package com.raphanum.githubusersearch;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UsersSearchResult {
    @SerializedName("items")
    private List<User> items;

    public List<User> getUserList() {
        return items;
    }
}
