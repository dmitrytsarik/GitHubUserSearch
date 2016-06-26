package com.raphanum.githubusersearch;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentManager;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.raphanum.githubusersearch.api.GitHubApiInterface;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Test";
    public static final int SPEECH_RECOGNIZER_CODE = 42;
    private int page = 1;

    private UserListAdapter adapter;
    private LinearLayoutManager layoutManager;

    private EditText editTextSearch;
    private TextWatcher textWatcher;
    private ProgressBar progressBar;
    private String lastQuery;
    private RetainedFragment dataSaveFragment;

    private List<User> userList = null;

    private GitHubApiInterface gitHubApiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            lastQuery = savedInstanceState.getString("lastQuery");
        } else {
            lastQuery = "";
        }

        progressBar = (ProgressBar) findViewById(R.id.loading_progress);

        initRetainedFragment();
        initRecyclerView();
        initEditTextSearch();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        gitHubApiInterface = retrofit.create(GitHubApiInterface.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        editTextSearch.addTextChangedListener(textWatcher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        editTextSearch.removeTextChangedListener(textWatcher);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_RECOGNIZER_CODE && resultCode == RESULT_OK) {
            ArrayList list = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            lastQuery = (String)list.get(0);
            Log.i(TAG, "Result: " + lastQuery);
            editTextSearch.setText(lastQuery);
            callUserSearch(lastQuery);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initRetainedFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        dataSaveFragment = (RetainedFragment) fragmentManager.findFragmentByTag("retainedFragment");

        if (dataSaveFragment != null) {
            userList = dataSaveFragment.getUserList();
        } else {
            dataSaveFragment = new RetainedFragment();
            fragmentManager.beginTransaction().add(dataSaveFragment, "retainedFragment").commit();
        }
    }

    private void initRecyclerView() {
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
                userList.add(null);
                adapter.notifyItemInserted(userList.size() - 1);
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
    }

    private void initEditTextSearch() {
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

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                page = 1;
                if (s.length() > -1)
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
        };
    }

    private void callUserSearch(String string) {
        callUserSearch(string, 1);
    }

    private void callUserSearch(final String string, final int page) {
        Call<UsersSearchResult> call = gitHubApiInterface.getSearchResult(string, page, Constants.PER_PAGE);
        call.enqueue(new Callback<UsersSearchResult>() {
            @Override
            public void onResponse(Call<UsersSearchResult> call, Response<UsersSearchResult> response) {
                if (response.isSuccessful()) {
                    processSuccessfulResponse(response);
                } else {
                    adapter.setUserList(null);
                    if (!string.equals("")) {
                        Toast.makeText(MainActivity.this, "No results", Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Call<UsersSearchResult> call, Throwable t) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, "onFailure()", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onFailure(): " + t.getMessage());
            }
        });
    }

    private void processSuccessfulResponse(Response<UsersSearchResult> response) {
        if (page > 1) {
            userList.remove(userList.size() - 1);
            adapter.notifyItemRemoved(userList.size());
            userList.addAll(response.body().getUserList());
            adapter.setLoaded();
        } else {
            adapter.setUserList(userList = response.body().getUserList());
            layoutManager.scrollToPosition(0);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) editTextSearch.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
    }

    public void onClick(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        try {
            startActivityForResult(intent, SPEECH_RECOGNIZER_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Not supported", Toast.LENGTH_SHORT).show();
        }
    }
}
