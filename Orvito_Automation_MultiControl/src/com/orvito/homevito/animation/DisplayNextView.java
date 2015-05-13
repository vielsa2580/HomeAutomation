package com.orvito.homevito.animation;

import android.view.animation.Animation;
import android.widget.LinearLayout;

public final class DisplayNextView implements Animation.AnimationListener {
	private boolean mCurrentView;
	LinearLayout linearLayout;
	LinearLayout linearLayout2;

	public DisplayNextView(boolean currentView, LinearLayout _UILLResidentDetail, LinearLayout _UILLBlockDetail) {
		mCurrentView = currentView;
		this.linearLayout = _UILLResidentDetail;
		this.linearLayout2 = _UILLBlockDetail;
	}

	public void onAnimationStart(Animation animation) {
	}

	public void onAnimationEnd(Animation animation) {
		linearLayout.post(new SwapViews(mCurrentView, linearLayout, linearLayout2));
	}

	public void onAnimationRepeat(Animation animation) {
	}
}