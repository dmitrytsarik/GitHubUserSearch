package com.raphanum.githubusersearch;

import android.support.annotation.Nullable;

import java.lang.annotation.Annotation;

public class User implements Nullable {
    private String login;
    private String avatar_url;

    public User(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public String getAvatarUrl() {
        return avatar_url;
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
