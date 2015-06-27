package com.insa.burnd.view.MainActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.insa.burnd.R;

public class MediaDialogFragment extends DialogFragment {
    public final static String EXTRA_MESSAGE = "com.insa.burnd.text.MESSAGE";

    public MediaDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.dialog_fragment_media, container);

        Button buttonPicture = (Button) V.findViewById(R.id.button_picture);
        buttonPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PostActivity.class);
                intent.putExtra(EXTRA_MESSAGE, "picture");
                startActivity(intent);
            }
        });

        Button buttonVideo = (Button) V.findViewById(R.id.button_video);
        buttonVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PostActivity.class);
                intent.putExtra(EXTRA_MESSAGE, "camera");
                startActivity(intent);
            }
        });

        return V;
    }
}