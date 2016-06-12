package com.raphanum.githubusersearch;

import android.content.Context;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Test";
    private int page = 1;

    private UserListAdapter adapter;
    private LinearLayoutManager layoutManager;

    private EditText editTextSearch;
    private ContentLoadingProgressBar progressBar;
    private boolean isLoading = false;
    private String searchQuery = "";

    private List<User> userList = null;

    private GithubSearchService.Github github;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ContentLoadingProgressBar) findViewById(R.id.loading_progress);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_search_result);
        layoutManager = new LinearLayoutManager(this);
        adapter = new UserListAdapter(userList);
        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) > 0) {
                    hideKeyboard();
                }
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItems = layoutManager.findFirstVisibleItemPosition();
                if ((visibleItemCount + firstVisibleItems) >= totalItemCount) {
                    if (!isLoading) {
                        isLoading = true;
                        page++;
                        callUserSearch(searchQuery, page);
                    }
                }
            }
        };
        if (recyclerView != null) {
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            recyclerView.addOnScrollListener(scrollListener);
            recyclerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard();
                    editTextSearch.clearFocus();
                    return false;
                }
            });
        }

        editTextSearch = (EditText)findViewById(R.id.search_edit_text);

        editTextSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard();
                    editTextSearch.clearFocus();
                }
            }
        });

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                page = 1;
                if (s.length() > 0)
                {
                    progressBar.setVisibility(View.VISIBLE);
                    callUserSearch(s.toString());
                    searchQuery = s.toString();
                } else {
                    adapter.setUserList(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GithubSearchService.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        github = retrofit.create(GithubSearchService.Github.class);
    }

    private void callUserSearch(String string) {
        callUserSearch(string, 1);
    }

    private void callUserSearch(String string, final int page) {
        Call<GithubSearchService.SearchResult> call = github.getSearchResult(string, page, 100);
        call.enqueue(new Callback<GithubSearchService.SearchResult>() {
            @Override
            public void onResponse(Call<GithubSearchService.SearchResult> call, Response<GithubSearchService.SearchResult> response) {
                if (response.isSuccessful()) {
                    if (page > 1) {
                        adapter.addUsers(response.body().getUserList());
                        isLoading = false;
                        Log.i(TAG, "onResponse().isSuccessful() add() page: " + page);
                    } else {
                        adapter.setUserList(response.body().getUserList());
                        layoutManager.scrollToPosition(0);
                        Log.i(TAG, "onResponse().isSuccessful() set()");
                    }
                } else {
                    adapter.setUserList(null);
                    String str = null;
                    try {
                        str = response.errorBody().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "onResponse(): " + str);
                }
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<GithubSearchService.SearchResult> call, Throwable t) {
                Log.i(TAG, "onFailure()");
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) editTextSearch.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
    }
}
