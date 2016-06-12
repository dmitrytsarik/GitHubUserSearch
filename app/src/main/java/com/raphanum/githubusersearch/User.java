package com.raphanum.githubusersearch;

public class User {
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
    public String toString() {
        return login;
    }
}
