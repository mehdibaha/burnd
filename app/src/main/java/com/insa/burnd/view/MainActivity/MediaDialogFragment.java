package com.insa.burnd.view.MainActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.insa.burnd.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MediaDialogFragment extends DialogFragment {
    public final static String EXTRA_MESSAGE = "com.insa.burnd.text.MESSAGE";
    private final MediaDialogFragment dialog = this;

    public MediaDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override @NonNull
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog_media, container);
        ButterKnife.bind(dialog, v);

        return v;
    }

    @OnClick(R.id.button_picture)
    public void clickPicture() {
        Intent intent = new Intent(getActivity(), PostActivity.class);
        intent.putExtra(EXTRA_MESSAGE, "picture");
        startActivity(intent);
    }

    @OnClick(R.id.button_video)
    public void clickVideo() {
        Intent intent = new Intent(getActivity(), PostActivity.class);
        intent.putExtra(EXTRA_MESSAGE, "camera");
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}