package com.chatt.demo;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.parse.ParseUser;

/**
 * The Class Splash is an Activity class that shows the Splash screen to users.
 * It checks if there already has been a user.
 */
public class Splash extends Activity{
	private Timer splashTimer;
	public static final int TIMEOUT_SPLASH = 1000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);

		splashTimer = new Timer();
		splashTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				ParseUser currentUser = ParseUser.getCurrentUser();
				if (currentUser != null) {
					UserList.user = currentUser;
					startActivity(new Intent(Splash.this, UserList.class));
				} else {
					startActivity(new Intent(Splash.this, Login.class));
				}
				finish();
			}
		}, TIMEOUT_SPLASH);
	}

}
