package com.insa.burnd.utils;

import android.app.Activity;
import android.support.v4.app.Fragment;

import butterknife.ButterKnife;

/* With this Base Class, we can access to parent activity of fragment very easily */
public abstract class BaseFragment extends Fragment {
    protected Activity mActivity;
    protected BaseFragment fragment = this;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}