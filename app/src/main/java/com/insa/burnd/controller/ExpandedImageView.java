package com.insa.burnd.controller;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.insa.burnd.R;

/* Class to show expanded image */
public class ExpandedImageView extends ImageView {

    private Context context;
    private ProgressDialog dialog;

	public interface ResponseObservert {
		void onError();

		void onSuccess();
	}

	private FeedImageView.ResponseObserver mObserver;

	public void setResponseObserver(FeedImageView.ResponseObserver observer) {
		mObserver = observer;
	}

	/**
	 * The URL of the network image to load
	 */
	private String mUrl;

	/**
	 * Resource ID of the image to be used as a placeholder until the network
	 * image is loaded.
	 */
	private int mDefaultImageId= R.drawable.loading;

	/**
	 * Resource ID of the image to be used if the network response fails.
	 */
	private int mErrorImageId;

	/**
	 * Local copy of the ImageLoader.
	 */
	private ImageLoader mImageLoader;

	/**
	 * Current ImageContainer. (either in-flight or finished)
	 */
	private ImageContainer mImageContainer;

	public ExpandedImageView(Context context) {
		this(context, null);
        this.context=context;
	}

	public ExpandedImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
        this.context=context;
	}

	public ExpandedImageView(Context context, AttributeSet attrs,
                             int defStyle) {
		super(context, attrs, defStyle);
        this.context=context;
	}


	public void setImageUrl(String url, ImageLoader imageLoader) {
		mUrl = url;
		mImageLoader = imageLoader;
		// The URL has potentially changed. See if we need to load it.
		loadImageIfNecessary(false);

	}


	/**
	 * Sets the default image resource ID to be used for this view until the
	 * attempt to load it completes.
	 */
	public void setDefaultImageResId(int defaultImage) {
		mDefaultImageId = defaultImage;
	}

	/**
	 * Sets the error image resource ID to be used for this view in the event
	 * that the image requested fails to load.
	 */
	public void setErrorImageResId(int errorImage) {
		mErrorImageId = errorImage;
	}

	/**
	 * Loads the image for the view if it isn't already loaded.
	 *
	 * @param isInLayoutPass
	 *            True if this was invoked from a layout pass, false otherwise.
	 */
	private void loadImageIfNecessary(final boolean isInLayoutPass) {
		ImageContainer newContainer = mImageLoader.get(mUrl,
				new ImageListener() {
					@Override
					public void onErrorResponse(VolleyError error) {

						if (mErrorImageId != 0) {
							setImageResource(mErrorImageId);
						}

						if (mObserver != null) {
							mObserver.onError();
						}
					}

					@Override
					public void onResponse(final ImageContainer response,
							boolean isImmediate) {

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


						} else if (mDefaultImageId != 0) {
							setImageResource(mDefaultImageId);
						}

						if (mObserver != null) {
							mObserver.onSuccess();

						}
					}
				});

		// update the ImageContainer to be the new bitmap container.
		mImageContainer = newContainer;

	}




}