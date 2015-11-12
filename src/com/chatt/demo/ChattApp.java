package com.chatt.demo;

import android.app.Application;

import com.parse.Parse;

/**
 * The Class ChattApp is the Main Application class of this app. The onCreate
 * method of this class initializes the Parse.
 */
public class ChattApp extends Application
{

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate()
	{
		super.onCreate();

		Parse.initialize(this, "wkq8psgl42aYBTPyE5ymgbrxu4v0QCnsBPSuBD39",
				"YQVhIoVqG99baAxsefOPXAZcuMqqy9MRxUe778I0");

	}
}
