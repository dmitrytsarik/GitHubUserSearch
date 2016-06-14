package com.raphanum.githubusersearch;

import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int visibleThreshold;
    private int lastVisibleItem, totalItemCount;
    private List<User> userList;
    private static final int ITEM_VIEW = 0;
    private static final int PROGRESS_VIEW = 1;
    private boolean loading = false;
    private OnLoadMoreListener onLoadMoreListener;

    public UserListAdapter(List<User> userList, RecyclerView recyclerView) {
        this.userList = userList;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    visibleThreshold = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        // End has been reached
                        // Do something
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    public void addUsers(List<User> users) {
        userList.addAll(users);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return userList.get(position) != null ? ITEM_VIEW : PROGRESS_VIEW;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == ITEM_VIEW) {
            CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_github_user, parent, false);
            vh = new CardViewHolder(cv);
        } else {
            ContentLoadingProgressBar progressBarItem = (ContentLoadingProgressBar) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.progress_bar_item, parent, false);
            vh = new ProgressViewHolder(progressBarItem);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (userList != null) {
            if (holder instanceof CardViewHolder) {
                CardView cardView = ((CardViewHolder)holder).cardView;
                TextView textView = (TextView) cardView.findViewById(R.id.login_label);
                textView.setText(userList.get(position).getLogin());
            } else {
                ((ProgressViewHolder)holder).progressBar.setIndeterminate(true);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (userList != null) {
            return userList.size();
        }
        return 0;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {

        private CardView cardView;

        public CardViewHolder(CardView cardView) {
            super(cardView);
            this.cardView = cardView;
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {

        private ContentLoadingProgressBar progressBar;

        public ProgressViewHolder(ContentLoadingProgressBar progressBar) {
            super(progressBar);
            this.progressBar = progressBar;
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void setLoaded() {
        loading = false;
    }
}
