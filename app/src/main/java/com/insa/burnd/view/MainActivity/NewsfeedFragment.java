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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.insa.burnd.R;
import com.insa.burnd.controller.HidingScrollListener;
import com.insa.burnd.controller.NewsfeedAdapter;
import com.insa.burnd.models.Newsfeed;
import com.insa.burnd.network.Connexion;
import com.insa.burnd.network.SessionController;
import com.insa.burnd.utils.BaseFragment;
import com.insa.burnd.utils.SPManager;
import com.insa.burnd.utils.Utils;
import com.insa.burnd.view.JoinActivity;
import com.insa.burnd.view.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NewsfeedFragment extends BaseFragment implements Connexion.ResponseListener {
    private Newsfeed newsfeed;
    private NewsfeedAdapter newsfeedAdapter;

    private Toolbar toolbar;
    private FloatingActionsMenu fam;
    private SwipeRefreshLayout swipeRefreshLayout;

    private boolean askedConnection;

    private final NewsfeedFragment fragment = this;
    private static String TAG = "BURND-NewsfeedFragment";
    public final static String EXTRA_MESSAGE = "com.insa.burnd.text.MESSAGE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);

        try {
            updateNewsfeed();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "updating Newsfeed");

        newsfeedAdapter = new NewsfeedAdapter((MainActivity) mActivity, fragment, newsfeed);
    }

    /* Requets new newsfeed from a new connexion */
    public void updateNewsfeed() throws JSONException {
        Log.d(TAG, "new connexion to get newsfeed");
        newsfeed = new Newsfeed();
        askedConnection = true;
        new Connexion(mActivity, fragment, "checkparty").execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View V = inflater.inflate(R.layout.fragment_newsfeed, container, false);

        // The FABS
        fam = (FloatingActionsMenu) V.findViewById(R.id.multiple_actions);
        final FloatingActionButton fabPost = (FloatingActionButton) V.findViewById(R.id.fab_post);
        final FloatingActionButton fabPhoto = (FloatingActionButton) V.findViewById(R.id.fab_photo);

        // The rest
        swipeRefreshLayout = (SwipeRefreshLayout) V.findViewById(R.id.swipe_layout); // SwipeToRefresh
        final RecyclerView recyclerView = (RecyclerView) V.findViewById(R.id.recyclerView);
        final View dimmedBackground  =  V.findViewById(R.id.dimmed_background);
        dimmedBackground.setVisibility(View.GONE);

        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setAdapter(newsfeedAdapter); // Assigns the recyclerview to its adapter

        initRefresh(swipeRefreshLayout); // Setting up refresh process

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

        recyclerView.setOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                hideViews(true);
            }

            @Override
            public void onShow() {
                showViews(true);
            }
        });

        fabPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "POST");
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

        return V;
    }

    public void initRefresh(final SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setProgressViewOffset(false, 0, Utils.dpToPx(mActivity, 100));
        swipeRefreshLayout.setProgressViewEndTarget(false, Utils.dpToPx(mActivity, 100));
        swipeRefreshLayout.setRefreshing(askedConnection);
        Log.d(TAG, "refresh state : " + String.valueOf(askedConnection));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                Log.d(TAG, "user asked for refresh");
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        String lastPostId = SPManager.load(mActivity, "LAST_POST_ID");
                        Log.d(TAG, lastPostId);
                        new Connexion(mActivity, fragment, "checkparty").execute(lastPostId);
                    }
                });
            }
        });
    }

    @Override
    public void requestCompleted(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        String message = json.getString("message");
        boolean error = json.getBoolean("error");

        Log.d(TAG, response);
        Log.d(TAG, message);

        if (!error) {
            refreshFeedList(response);
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

    private void showMediaDialog() {
        FragmentActivity activity = (FragmentActivity) mActivity;
        FragmentManager fm = activity.getSupportFragmentManager();
        MediaDialogFragment mediaDialogFragment = new MediaDialogFragment();
        mediaDialogFragment.show(fm, "dialog_fragment_media");
    }

    public void hideViews(boolean withToolbar) {
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fam.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        fam.animate().translationY(fam.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();

        if(withToolbar)
            toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));

    }

    public void showViews(boolean withToolbar) {
        fam.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();

        if(withToolbar)
            toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    public void refreshFeedList (String json) throws JSONException {
        final JSONArray feedArray = Newsfeed.jsonToFeedArray(json);
        if(feedArray.length()!=0) {
            Log.d(TAG, "refresh feed list : " + json);
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

    public void saveLastPostId(Newsfeed newsfeed) {
        String lastPostId = String.valueOf(newsfeed.get(0).getId());
        SPManager.save(mActivity, lastPostId, "LAST_POST_ID"); // Saves last post id
        Log.d(TAG, "saving last post id : " + lastPostId);
    }

    public NewsfeedAdapter getNewsfeedAdapter() {
        return newsfeedAdapter;
    }

    public Newsfeed getNewsfeed() {
        return newsfeed;
    }
}