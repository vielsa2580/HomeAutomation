package com.orvito.homevito.animation;

import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

public final class SwapViews implements Runnable {
	private boolean mIsFirstView;
	LinearLayout linearLayout1;
	LinearLayout linearLayout2;

	public SwapViews(boolean isFirstView, LinearLayout linearLayout, LinearLayout linearLayout2) {
		mIsFirstView = isFirstView;
		this.linearLayout1 = linearLayout;
		this.linearLayout2 = linearLayout2;
	}

	public void run() {
		final float centerX = linearLayout1.getWidth() / 2.0f;
		final float centerY = linearLayout1.getHeight() / 2.0f;
		Flip3dAnimation rotation;

		if (mIsFirstView) {
			linearLayout1.setVisibility(View.GONE);
			linearLayout2.setVisibility(View.VISIBLE);
			linearLayout2.requestFocus();

			rotation = new Flip3dAnimation(-90, 0, centerX, centerY);
		} else {
			linearLayout2.setVisibility(View.GONE);
			linearLayout1.setVisibility(View.VISIBLE);
			linearLayout1.requestFocus();

			rotation = new Flip3dAnimation(-90, 0, centerX, centerY);
		}

		rotation.setDuration(500);
		rotation.setFillAfter(true);
		rotation.setInterpolator(new DecelerateInterpolator());

		if (mIsFirstView) {
			linearLayout2.startAnimation(rotation);
		} else {
			linearLayout1.startAnimation(rotation);
		}
	}
}
