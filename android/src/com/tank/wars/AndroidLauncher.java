package com.tank.wars;

import android.os.Bundle;
import android.util.DisplayMetrics;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useCompass = false;
		config.useAccelerometer = false;
		config.numSamples = 2;
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		final float scl = MainGame.VIRTUAL_HEIGHT / MainGame.VIRTUAL_WIDTH;
		config.resolutionStrategy = (widthMeasureSpec, heightMeasureSpec) -> new ResolutionStrategy.MeasuredDimension((int)(displaymetrics.heightPixels / scl), displaymetrics.heightPixels);
		initialize(new MainGame(), config);
	}
}
