package com.raphanum.githubusersearch.api;

import com.raphanum.githubusersearch.UsersSearchResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GitHubApiInterface {
    @GET("search/users")
    Call<UsersSearchResult> getSearchResult(@Query("q") String q,
                                            @Query("page") int page,
                                            @Query("per_page") int resultsPerPage);
}
