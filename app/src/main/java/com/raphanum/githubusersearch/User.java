package com.raphanum.githubusersearch;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Annotation;

public class User implements Nullable {

    @SerializedName("login")
    private String login;
    @SerializedName("avatar_url")
    private String avatarUrl;

    public User(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }

    @Override
    public String toString() {
        return login;
    }
}
