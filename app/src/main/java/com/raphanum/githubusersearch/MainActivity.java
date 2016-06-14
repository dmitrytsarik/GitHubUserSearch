package com.raphanum.githubusersearch;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
    private String lastQuery = "";
    private RetainedFragment dataSaveFragment;

    private List<User> userList = null;

    private GithubSearchService.Github github;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: divide onCreate into few methods

        FragmentManager fragmentManager = getSupportFragmentManager();
        dataSaveFragment = (RetainedFragment) fragmentManager.findFragmentByTag("retainedFragment");

        if (savedInstanceState != null) {
            lastQuery = savedInstanceState.getString("lastQuery");
        }

        if (dataSaveFragment != null) {
            userList = dataSaveFragment.getUserList();
        } else {
            dataSaveFragment = new RetainedFragment();
            fragmentManager.beginTransaction().add(dataSaveFragment, "retainedFragment").commit();
        }

        progressBar = (ContentLoadingProgressBar) findViewById(R.id.loading_progress);
//        final ContentLoadingProgressBar loadMoreProgressBar = (ContentLoadingProgressBar)findViewById(R.id.load_more_progress);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_search_result);
        layoutManager = new LinearLayoutManager(this);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(layoutManager);
            adapter = new UserListAdapter(userList, recyclerView);
            recyclerView.setAdapter(adapter);
            recyclerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard();
                    editTextSearch.clearFocus();
                    return false;
                }
            });
        }

        adapter.setOnLoadMoreListener(new UserListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                //TODO: add circle progress bar
                //userList.add(loadMoreProgressBar);
                //adapter.notifyItemInserted(userList.size() - 1);
                page++;
                callUserSearch(lastQuery, page);
            }
        });

        adapter.setOnLoadImageListener(new UserListAdapter.OnLoadImageListener() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Picasso.with(getApplicationContext())
                        .load(Uri.parse(url))
                        .placeholder(R.drawable.github_icon)
                        .into(imageView);
            }
        });

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
                    if (!s.toString().equals(lastQuery)) {
                        progressBar.setVisibility(View.VISIBLE);
                        callUserSearch(lastQuery = s.toString());
                    }
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("lastQuery", lastQuery);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSaveFragment.setUserList(userList);
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
                        userList.addAll(response.body().getUserList());
                        adapter.setUserList(userList);
                        //adapter.addUsers();
                        adapter.setLoaded();
                        Log.i(TAG, "onResponse().isSuccessful() add() page: " + page);
                    } else {
                        adapter.setUserList(userList = response.body().getUserList());
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
                Toast.makeText(MainActivity.this, "onFailure()", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onFailure()");
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) editTextSearch.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
    }
}
