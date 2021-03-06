package com.insa.burnd.view.MainActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.insa.burnd.R;
import com.insa.burnd.controller.NewsfeedAdapter;
import com.insa.burnd.models.ApiResponse;
import com.insa.burnd.models.Newsfeed;
import com.insa.burnd.network.Connection;
import com.insa.burnd.network.SessionController;
import com.insa.burnd.utils.BaseFragment;
import com.insa.burnd.utils.SPManager;
import com.insa.burnd.utils.Utils;
import com.insa.burnd.view.JoinActivity;
import com.insa.burnd.view.LoginActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import trikita.log.Log;

import static butterknife.ButterKnife.findById;

public class NewsfeedFragment extends BaseFragment implements Connection.ResponseListener {
    public final static String EXTRA_MESSAGE = "com.insa.burnd.text.MESSAGE";
    private final NewsfeedFragment fragment = this;
    private NewsfeedAdapter newsfeedAdapter;
    private Newsfeed mNewsfeed;
    private boolean askedConnection;

    private FloatingActionsMenu fam;
    @Bind(R.id.swipe_layout) SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.recyclerView) RecyclerView recyclerView;
    @Bind(R.id.dimmed_background) View dimmedBackground;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateNewsfeed();
        newsfeedAdapter = new NewsfeedAdapter((MainActivity) mActivity, fragment, mNewsfeed);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_newsfeed, container, false);
        ButterKnife.bind(fragment, v);

        initFABS();
        initRefresh();
        initRecyclerView();
        initDimmedBackgroud();

        return v;
    }

    private void initFABS() {
        fam = findById(mActivity, R.id.multiple_actions);

        findById(mActivity, R.id.fab_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPost();
            }
        });

        findById(mActivity, R.id.fab_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMediaDialog();
            }
        });
    }

    private void initRefresh() {
        if(askedConnection) {
            swipeRefreshLayout.setProgressViewOffset(false, 0, Utils.dpToPx(mActivity, 50));
            swipeRefreshLayout.setRefreshing(true);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                Log.v("User asking for refresh");
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        String lastPostId = SPManager.load(mActivity, "LAST_POST_ID");
                        Log.v(lastPostId);
                        new Connection(mActivity, fragment, "checkparty").execute("true", lastPostId);
                    }
                });
            }
        });
    }

    public void sendPost() {
        Log.d("POST");
        Intent intent = new Intent(mActivity, PostActivity.class);
        intent.putExtra(EXTRA_MESSAGE, "post");
        startActivity(intent);
    }

    public void showMediaDialog() {
        FragmentActivity activity = (FragmentActivity) mActivity;
        FragmentManager fm = activity.getSupportFragmentManager();
        MediaDialogFragment mediaDialogFragment = new MediaDialogFragment();
        mediaDialogFragment.show(fm, "dialog_fragment_media");
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setAdapter(newsfeedAdapter); // Assigns the recyclerview to its adapter
    }

    private void initDimmedBackgroud() {
        dimmedBackground.setVisibility(View.GONE);

        // TODO Could be a nice addition
        // on back pressed return to un dimmed state
        dimmedBackground.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (fam.isExpanded()) {
                    fam.collapse();
                }
                dimmedBackground.setVisibility(View.GONE);
                return true;
            }
        });

        // Listens for any click on the floating action menu
        fam.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                dimmedBackground.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuCollapsed() {
                dimmedBackground.setVisibility(View.GONE);
            }
        });
    }

    public void hideViews() {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fam.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        fam.animate().translationY(fam.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    public void showViews() {
        fam.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    /* Requets new newsfeed from a new connexion */
    public void updateNewsfeed() {
        mNewsfeed = new Newsfeed();
        askedConnection = true;
        new Connection(mActivity, fragment, "checkparty").execute("true");
    }

    @Override
    public void requestCompleted(ApiResponse ar) {
        String message = ar.getMessage();
        boolean error = ar.isError();
        Log.d(ar.toString());

        if (!error) {
            refreshNewsfeed(ar.getNewsfeed());
        } else if (message.equals("USER_NOT_IN_PARTY")) {
            Utils.showToast(mActivity, "You're not in a party.");
            startActivity(new Intent(mActivity, JoinActivity.class));
            mActivity.finish();
        } else {
            Utils.showToast(mActivity, "Access denied.");
            new SessionController(mActivity).disconnectFB();
            startActivity(new Intent(mActivity, LoginActivity.class));
            mActivity.finish();
        }
    }

    private void refreshNewsfeed (Newsfeed newsfeed) {
        if(newsfeed.size()!=0) {
            mNewsfeed.update(newsfeed); // Update newsfeed
            mNewsfeed.saveLastPostId(mActivity);
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                askedConnection = false;
                if(swipeRefreshLayout != null && newsfeedAdapter != null) {
                    swipeRefreshLayout.setRefreshing(false);
                    newsfeedAdapter.refresh();
                }
            }
        });
    }

    public NewsfeedAdapter getNewsfeedAdapter() {
        return newsfeedAdapter;
    }
}