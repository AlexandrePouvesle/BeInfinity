package com.beinfinity.tools;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

/**
 * Created by Alexandre on 16/10/2016.
 */

public class ProgressView {

    private View mainView;
    private View progressView;

    public ProgressView(View main, View progress) {
        this.mainView = main;
        this.progressView = progress;
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void ShowProgress(final boolean show, final int shortAnimTime) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            //int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            this.mainView.setVisibility(show ? View.GONE : View.VISIBLE);
            this.mainView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mainView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            this.progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            this.progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            this.progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            this.mainView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
