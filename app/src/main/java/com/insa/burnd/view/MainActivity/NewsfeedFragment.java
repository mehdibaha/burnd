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

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.insa.burnd.R;
import com.insa.burnd.controller.NewsfeedAdapter;
import com.insa.burnd.models.Newsfeed;
import com.insa.burnd.network.Connection;
import com.insa.burnd.network.SessionController;
import com.insa.burnd.utils.BaseFragment;
import com.insa.burnd.utils.SPManager;
import com.insa.burnd.utils.Utils;
import com.insa.burnd.view.JoinActivity;
import com.insa.burnd.view.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import trikita.log.Log;

public class NewsfeedFragment extends BaseFragment implements Connection.ResponseListener {
    private final NewsfeedFragment fragment = this;
    public final static String EXTRA_MESSAGE = "com.insa.burnd.text.MESSAGE";

    private Newsfeed newsfeed;
    private boolean askedConnection;

    private NewsfeedAdapter newsfeedAdapter;
    private FloatingActionsMenu  fam;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateNewsfeed();
        newsfeedAdapter = new NewsfeedAdapter((MainActivity) mActivity, fragment, newsfeed);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (swipeRefreshLayout!=null) {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.destroyDrawingCache();
            swipeRefreshLayout.clearAnimation();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View V = inflater.inflate(R.layout.fragment_newsfeed, container, false);

        // Passing View object to methods who need it
        initRefresh(V);
        initFABS();
        initRecyclerView(V);
        initDimmedBackgroud(V);

        return V;
    }

    private void initRefresh(View v) {
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_layout);
        if(askedConnection) {
            swipeRefreshLayout.setProgressViewOffset(false, 0, Utils.dpToPx(mActivity, 50));
            swipeRefreshLayout.setRefreshing(true);
        }
        Log.d("refresh state : " + String.valueOf(askedConnection));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setProgressViewOffset(false, 0, 0);
                swipeRefreshLayout.setRefreshing(true);
                Log.d("user asked for refresh");
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        String lastPostId = SPManager.load(mActivity, "LAST_POST_ID");
                        Log.d(lastPostId);
                        new Connection(mActivity, fragment, "checkparty").execute(lastPostId);
                    }
                });
            }
        });
    }

    private void initFABS() {
        FloatingActionButton fabPost  = (FloatingActionButton) mActivity.findViewById(R.id.fab_post);
        FloatingActionButton fabPhoto = (FloatingActionButton) mActivity.findViewById(R.id.fab_photo);
        fam      = (FloatingActionsMenu) mActivity.findViewById(R.id.multiple_actions);

        fabPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("POST");
                Intent intent = new Intent(mActivity, PostActivity.class);
                intent.putExtra(EXTRA_MESSAGE, "post");
                startActivity(intent);
            }
        });

        fabPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMediaDialog();
            }
        });
    }

    private void initRecyclerView(View v) {
        final RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setAdapter(newsfeedAdapter); // Assigns the recyclerview to its adapter
    }

    private void initDimmedBackgroud(View v) {
        final View dimmedBackground  =  v.findViewById(R.id.dimmed_background);
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

    private void showMediaDialog() {
        FragmentActivity activity = (FragmentActivity) mActivity;
        FragmentManager fm = activity.getSupportFragmentManager();
        MediaDialogFragment mediaDialogFragment = new MediaDialogFragment();
        mediaDialogFragment.show(fm, "dialog_fragment_media");
    }

    /* Requets new newsfeed from a new connexion */
    public void updateNewsfeed() {
        newsfeed = new Newsfeed();
        askedConnection = true;
        new Connection(mActivity, fragment, "checkparty").execute();
    }

    @Override
    public void requestCompleted(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        String message = json.getString("message");
        boolean error = json.getBoolean("error");

        Log.v(response);
        Log.d(message);

        if (!error) {
            refreshNewsfeed(response);
        } else if (message.equals("USER_NOT_IN_PARTY")) {
            Utils.showToast(mActivity, "You're not in a party...");
            startActivity(new Intent(mActivity, JoinActivity.class));
            mActivity.finish();
        } else {
            Utils.showToast(mActivity, "Access denied.");
            new SessionController(mActivity).disconnectFB();
            startActivity(new Intent(mActivity, LoginActivity.class));
            mActivity.finish();
        }
    }

    private void refreshNewsfeed (String json) throws JSONException {
        final JSONArray feedArray = Newsfeed.jsonToFeedArray(json);
        if(feedArray.length()!=0) {
            Log.v("refresh feed list : " + json);
            newsfeed.update(feedArray); // Update newsfeed
            saveLastPostId(newsfeed);
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                askedConnection = false;
                swipeRefreshLayout.setRefreshing(false);
                newsfeedAdapter.refresh();
            }
        });
    }

    private void saveLastPostId(Newsfeed newsfeed) {
        String lastPostId = String.valueOf(newsfeed.get(0).getId());
        SPManager.save(mActivity, lastPostId, "LAST_POST_ID"); // Saves last post id
        Log.d("saving last post id : " + lastPostId);
    }

    public void hideViews() {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fam.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        fam.animate().translationY(fam.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    public void showViews() {
        fam.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    public NewsfeedAdapter getNewsfeedAdapter() {
        return newsfeedAdapter;
    }
}