package com.raphanum.githubusersearch;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public final class GithubSearchService {

    public static final String API_URL = "https://api.github.com/";

    public static class SearchResult {
        private List<User> items;

        public List<User> getUserList() {
            return items;
        }
    }

    public interface Github {
        @GET("search/users")
        Call<SearchResult> getSearchResult(@Query("q") String q,
                                           @Query("page") int page,
                                           @Query("per_page") int cont);
    }
}
