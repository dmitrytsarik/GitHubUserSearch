package com.raphanum.githubusersearch;

import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<User> userList;
    private static final int VIEW_ITEM = 0;
    private static final int PROGRESS_ITEM = 1;
    private boolean loading = false;
    private OnLoadMoreListener onLoadMoreListener;
    private OnLoadImageListener onLoadImageListener;

    public UserListAdapter(List<User> userList, RecyclerView recyclerView) {
        this.userList = userList;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    int visibleThreshold = linearLayoutManager.getChildCount();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
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

    @Override
    public int getItemViewType(int position) {
        return userList.get(position) != null ? VIEW_ITEM : PROGRESS_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            CardView cv = (CardView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_github_user, parent, false);
            vh = new CardViewHolder(cv);
        } else {
            View progressBarItem = LayoutInflater.from(parent.getContext())
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
                ImageView avatar = (ImageView) cardView.findViewById(R.id.user_image);
                onLoadImageListener.loadImage(avatar, userList.get(position).getAvatarUrl());
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

        private ProgressBar progressBar;

        public ProgressViewHolder(View progressItem) {
            super(progressItem);
            this.progressBar = (ProgressBar) progressItem.findViewById(R.id.load_more_progress);
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public interface OnLoadImageListener {
        void loadImage(ImageView imageView, String url);
    }

    public void setOnLoadImageListener(OnLoadImageListener onLoadImageListener) {
        this.onLoadImageListener = onLoadImageListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void setLoaded() {
        loading = false;
    }
}
