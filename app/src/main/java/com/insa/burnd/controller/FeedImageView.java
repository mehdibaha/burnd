package com.insa.burnd.controller;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.insa.burnd.R;

// Sous-classe d'ImageView qui adapte le ratio des images du newsfeed
// (méthodes loadimageifnecessary et adjustimageaspect)
// Cette classe a été importée d'un tutoriel sur internet.

public class FeedImageView extends ImageView {

    public interface ResponseObserver {
        public void onError();

        public void onSuccess();
    }

    private ResponseObserver responseObserver;

    public void setResponseObserver(ResponseObserver responseObserver) {
        this.responseObserver = responseObserver;
    }

    private String url; // The URL of the network image to load
    private int defaultImageId; // Resource ID of the image to be used as a placeholder until the network image is loaded.
    private int errorImageId = R.drawable.loading; // Resource ID of the image to be used if the network response fails.
    private ImageLoader imageLoader; // Local copy of the ImageLoader.
    private ImageContainer imageContainer; // Current ImageContainer. (either in-flight or finished)


    public FeedImageView(Context context) {
        this(context, null);
    }

    public FeedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FeedImageView(Context context, AttributeSet attrs,
                         int defStyle) {
        super(context, attrs, defStyle);
    }

    // CALL setDefaultImageResID and setErrorImageResId before calling setImageUrl, to NOT show nothing.
    public void setImageUrl(String url, ImageLoader imageLoader) {
        this.url = url;
        this.imageLoader = imageLoader;
        // The URL has potentially changed. See if we need to load it.
        loadImageIfNecessary(false);
    }

    //Sets the default image resource ID to be used for this view until the attempt to load it completes.
    public void setDefaultImageResId(int defaultImage) {
        this.defaultImageId = defaultImage;
    }

    // Same as above, but in case of errors
    public void setErrorImageResId(int errorImage) {
        this.errorImageId = errorImage;
    }

    /* @param isInLayoutPass
    *            True if this was invoked from a layout pass, false otherwise.*/
    //Loads the image for the view if it isn't already loaded.
    private void loadImageIfNecessary(final boolean isInLayoutPass) {
        final int width = getWidth();
        int height = getHeight();

        boolean isFullyWrapContent = getLayoutParams() != null
                && getLayoutParams().height == LayoutParams.WRAP_CONTENT
                && getLayoutParams().width == LayoutParams.WRAP_CONTENT;
        // if the view's bounds aren't known yet, and this is not a
        // wrap-content/wrap-content
        // view, hold off on loading the image.
        if (width == 0 && height == 0 && !isFullyWrapContent) {
            return;
        }

        // if the URL to be loaded in this view is empty, cancel any old
        // requests and clear the
        // currently loaded image.
        if (TextUtils.isEmpty(url)) {
            if (imageContainer != null) {
                imageContainer.cancelRequest();
                imageContainer = null;
            }
            setDefaultImageOrNull();
            return;
        }

        // if there was an old request in this view, check if it needs to be
        // canceled.
        if (imageContainer != null && imageContainer.getRequestUrl() != null) {
            if (imageContainer.getRequestUrl().equals(url)) {
                // if the request is from the same URL, return.
                return;
            } else {
                // if there is a pre-existing request, cancel it if it's
                // fetching a different URL.
                imageContainer.cancelRequest();
                setDefaultImageOrNull();
            }
        }

        // The pre-existing content of this view didn't match the current URL.
        // Load the new image
        // from the network.
        ImageContainer newContainer = imageLoader.get(url,
                new ImageListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (errorImageId != 0) {
                            setImageResource(errorImageId);
                        }

                        if (responseObserver != null) {
                            responseObserver.onError();
                        }
                    }

                    @Override
                    public void onResponse(final ImageContainer response,
                                           boolean isImmediate) {
                        // If this was an immediate response that was delivered
                        // inside of a layout
                        // pass do not set the image immediately as it will
                        // trigger a requestLayout
                        // inside of a layout. Instead, defer setting the image
                        // by posting back to
                        // the main thread.
                        if (isImmediate && isInLayoutPass) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    onResponse(response, false);
                                }
                            });
                            return;
                        }

                        if (response.getBitmap() != null) {

                            setImageBitmap(response.getBitmap());
                            int bWidth = response.getBitmap().getWidth();
                            int bHeight = response.getBitmap().getHeight();
                            adjustImageAspect(bWidth, bHeight);

                        } else if (defaultImageId != 0) {
                            setImageResource(defaultImageId);
                        }

                        if (responseObserver != null) {
                            responseObserver.onSuccess();

                        }
                    }
                });

        // update the ImageContainer to be the new bitmap container.
        imageContainer = newContainer;

    }

    private void setDefaultImageOrNull() {
        if (defaultImageId != 0) {
            setImageResource(defaultImageId);
        } else {
            setImageBitmap(null);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        loadImageIfNecessary(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (imageContainer != null) {
            // If the view was bound to an image request, cancel it and clear
            // out the image from the view.
            imageContainer.cancelRequest();
            setImageBitmap(null);
            // also clear out the container so we can reload the image if
            // necessary.
            imageContainer = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    /*
     * Adjusting imageview height
     * */
    private void adjustImageAspect(int bWidth, int bHeight) {
        LinearLayout.LayoutParams params = (LayoutParams) getLayoutParams();

        if (bWidth == 0 || bHeight == 0)
            return;

        int swidth = getWidth();
        int new_height = swidth * bHeight / bWidth;
        params.width = swidth;
        params.height = new_height;
        setLayoutParams(params);
    }
}