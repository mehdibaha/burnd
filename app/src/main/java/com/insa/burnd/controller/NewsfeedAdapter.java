package com.insa.burnd.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.insa.burnd.R;
import com.insa.burnd.models.FeedItem;
import com.insa.burnd.models.Newsfeed;
import com.insa.burnd.network.Connexion;
import com.insa.burnd.network.VolleySingleton;
import com.insa.burnd.view.MainActivity.MainActivity;
import com.insa.burnd.view.MainActivity.NewsfeedFragment;

// Sous-classe d'un adapter classique, qui remplit un layout de feeditems,
// à partir d'une liste de FeedItem (classe FeedItem => utile)

public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.ViewHolder> {

    private static String TAG = "BURND-NewsfeedAdapter";
    private MainActivity activity;
    private NewsfeedFragment fragment;
    private Newsfeed newsfeed;
    private Newsfeed visibleNewsfeed;

    private ImageLoader imageLoader = VolleySingleton.getInstance().getImageLoader();

    // Animation stuff
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration = 100;
    private int lastPosition = -1;

    public NewsfeedAdapter(MainActivity activity, NewsfeedFragment fragment, Newsfeed newsfeed) {
        this.activity = activity;
        this.fragment = fragment;
        this.newsfeed = newsfeed;
        this.visibleNewsfeed = (Newsfeed) newsfeed.clone(); // visibleNewsfeed/newsfeed are two separate objects in memory.
    }

    /* Refreshes the newsfeed in a proper way */
    public void refresh() {
        visibleNewsfeed = (Newsfeed) newsfeed.clone();
        notifyDataSetChanged();
    }

    /* Resets the visibleNewfeed from the original newsfeed */
    public void flushFilter() {
        visibleNewsfeed.clear();
        visibleNewsfeed.addAll(newsfeed);
        Log.d(TAG, newsfeed.toString());
        notifyDataSetChanged();
    }

    /* Setting filter on visibleNewsfeed based on search query */
    public void setFilter(String queryText) {
        visibleNewsfeed.clear();
        for (int i=0; i<newsfeed.size(); i++) {
            if (newsfeed.get(i).getStatus().toLowerCase().contains(queryText))
                visibleNewsfeed.add(newsfeed.get(i));
        }
        notifyDataSetChanged();
    }

    /* Assigns every view from feeditem.xml to a view in NewsfeedAdapter */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected CardView container;
        protected TextView username;
        protected TextView timestamp;
        protected TextView statusMsg;
        protected NetworkImageView profilePic;
        protected TextView votes;
        protected Button buttonUp;
        protected Button buttonDown;

        protected final FeedImageView feedImageView;
        protected final VideoView feedVideoView;

        protected TextView noCommentsMessage;
        protected Button buttonShowComments;
        protected LinearLayout commentView;
        protected ListView commentsListView;
        protected EditText etComment;
        protected Button buttonSendComment;

        protected ProgressBar progressVote;

        public ViewHolder(View v) {
            super(v);

            container = (CardView) v.findViewById(R.id.feeditem_container);
            username = (TextView) v.findViewById(R.id.username);
            timestamp = (TextView) v.findViewById(R.id.timestamp);
            statusMsg = (TextView) v.findViewById(R.id.txtStatusMsg);
            profilePic = (NetworkImageView) v.findViewById(R.id.profilePic);
            votes = (TextView) v.findViewById(R.id.textview_votes);
            buttonUp = (Button) v.findViewById(R.id.button_up);
            buttonDown = (Button) v.findViewById(R.id.button_down);

            // WARNING : feedImage1, due to conflict. It has to be the same in xml layouts.
            feedImageView = (FeedImageView) v.findViewById(R.id.feedImage1);
            feedVideoView = (VideoView) v.findViewById(R.id.videoView);

            buttonShowComments = (Button) v.findViewById(R.id.button_show_comments);
            noCommentsMessage = (TextView) v.findViewById(R.id.no_comments_message);
            commentView = (LinearLayout) v.findViewById(R.id.commentView);
            commentsListView = (ListView) v.findViewById(R.id.comments_listView);
            etComment = (EditText) v.findViewById(R.id.et_comment);
            buttonSendComment = (Button) v.findViewById(R.id.button_send_comment);
            progressVote=(ProgressBar) v.findViewById(R.id.progressv);
        }

    }

    // Assigns the feeditem.xml to the viewholder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.feed_item, viewGroup, false);

        return new ViewHolder(v);
    }

    /* Fills view in VH content from Newsfeed */
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final FeedItem item = visibleNewsfeed.get(position);
        String status=item.getStatus();
        SpannableString ss = new SpannableString(status);
        // Sets username i textview
        holder.username.setText(item.getUser().getName());
        if (item.getStatus().toLowerCase().contains("#music")) {
            //holder.container.setCardBackgroundColor(Color.argb(90, 10, 128, 209));
            //holder.timestamp.setTextColor(Color.WHITE);

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    Log.d("hashtag","hashtag");
                    setFilter("#music");
                }
            };
            ss.setSpan(clickableSpan, status.indexOf(35), status.indexOf(35)+6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }else{
            //holder.container.setCardBackgroundColor(Color.WHITE);
        }

        // Converting timestamp into x ago format
        long timeStampLong;
        if (item.getTimeStamp() != null) {
            timeStampLong = Long.parseLong(item.getTimeStamp());
            //timeStampLong = item.getTimeStamp().getTime();
            CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(timeStampLong,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
            holder.timestamp.setText(timeAgo);
        } else {
            holder.timestamp.setVisibility(View.GONE);
        }

        // Checking for empty status message
        if (!TextUtils.isEmpty(item.getStatus())) {
            holder.statusMsg.setText(ss);
            holder.statusMsg.setMovementMethod(LinkMovementMethod.getInstance());
            holder.statusMsg.setVisibility(View.VISIBLE);
        } else {
            // status is empty, remove from view
            holder.statusMsg.setVisibility(View.GONE);
        }

        // user profile pic
        holder.profilePic.setImageUrl(item.getUser().getProfilePic(), imageLoader);

        // Feed image
        if (!item.getImge().equals("")) {
            holder.feedImageView.setImageUrl(item.getImge(), imageLoader);
            holder.feedImageView.setVisibility(View.VISIBLE);
            holder.feedImageView
                    .setResponseObserver(new FeedImageView.ResponseObserver() {
                        @Override
                        public void onError() {
                        }

                        @Override
                        public void onSuccess() {
                        }
                    });
            holder.feedImageView.setTag(position);
            holder.feedImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    zoomImageFromThumb(holder.feedImageView, item.getId());
                    Log.d(TAG, "" + item.getId());
                }
            });
        } else {
            holder.feedImageView.setVisibility(View.GONE);
        }

        if (!item.getVideo().equals("")) {
            String url="http://burnd.cles-facil.fr/video/";
            Uri video= Uri.parse(item.getVideo());
            // Uri video=Uri.parse("http://www.androidbegin.com/tutorial/AndroidCommercial.3gp");
            Log.i("video",url+item.getVideo());
            holder.feedVideoView.setVisibility(View.VISIBLE);
            holder.feedVideoView.setVideoURI(video);
            holder.feedVideoView.start();
            holder.feedVideoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                        if (holder.feedVideoView.isPlaying()) {
                            holder.feedVideoView.pause();
                        } else {
                            holder.feedVideoView.resume();
                        }
                    return true;
                }
            });

        } else {
            holder.feedVideoView.setVisibility(View.GONE);
        }

        // Votes
        holder.votes.setText(item.getVotesUp() + " | " + item.getVotesDown());

        int up= Integer.parseInt(item.getVotesUp());
        int down=Integer.parseInt(item.getVotesDown());

        holder.progressVote.setProgress(statisticVote(up, down));
        Log.d("statistic", "" + (statisticVote(up, down)));
        holder.progressVote.setProgress(50);
        holder.buttonUp.setTag(position);
        holder.buttonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeVote(item, "up");
            }
        });

        holder.buttonDown.setTag(position);
        holder.buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeVote(item, "down");
            }
        });
        //On remplit le ListView des comments d'abord.
        holder.commentsListView.setAdapter(new CommentListAdapter(activity, item.getCommentList()));

        if(item.getCommentList().size()!=0)
            holder.noCommentsMessage.setVisibility(View.GONE);
        else
            holder.noCommentsMessage.setVisibility(View.VISIBLE);

        holder.buttonShowComments.setTag(position);
        holder.buttonShowComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewComments(holder, item);
            }
        });
        holder.commentsListView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        holder.etComment.setTag(position); // Setting tag to get the correct text

        holder.buttonSendComment.setTag(position);
        holder.buttonSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Connexion(activity, fragment, "comment" ,"Commenting").execute(holder.etComment.getText().toString(), String.valueOf(item.getId()));
            }
        });

        setAnimation(holder.container, position);
    }

    // Expands the comment view and show the list of comments
    @TargetApi(21)
    public void viewComments(ViewHolder holder, FeedItem item) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (holder.commentView.getVisibility()==View.GONE) {
            holder.commentView.setVisibility(View.VISIBLE);
            fragment.hideViews(false);

            if (currentapiVersion >= Build.VERSION_CODES.LOLLIPOP)
                holder.container.setElevation(100);

        } else if(holder.commentView.getVisibility()==View.VISIBLE) {
            holder.commentView.setVisibility(View.GONE);
            fragment.showViews(false);

            if (currentapiVersion >= Build.VERSION_CODES.LOLLIPOP)
                holder.container.setElevation(4.0f);
        }
    }

    public void changeVote(FeedItem item, String flag) {
        new Connexion(activity, fragment, "vote", "Voting...").execute(String.valueOf(item.getId()), flag);
    }

    public int statisticVote(int p,int n) {
        double z=1.96;
        double phat = 1.0*p/n;
        double res= 100*(p+10)/(Math.abs(n)+p+20);

        return (int) res;
    }

    /* Sets the new items ani{mation */
    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(activity, android.R.anim.fade_in);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return visibleNewsfeed.size();
    }

    /* zooms images by clicking in feed item */
    private void zoomImageFromThumb(final View thumbView, int id) {
        final View parentView = fragment.getView(); // Gets the parent view
        String url = "http://burnd.cles-facil.fr/utils/imagezoom.php?id=" + Integer.toString(id);

        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        activity.getNewsfeedFragment().hideViews(true);

        // Load the high-resolution "zoomed-in" image.
        final TouchImageView expandedImageView = (TouchImageView) parentView.findViewById(R.id.expanded_image);
        expandedImageView.setImageUrl(url, imageLoader);
//        expandedImageView.setImageResource(imageResId);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        parentView.findViewById(R.id.fragment_newsfeed)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Reshowing stuff
                activity.getNewsfeedFragment().showViews(true);

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    }
}