package com.njtransit;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

public class Main extends Activity {
    /** Called when the activity is first created. */
	
	private NJTransitDBAdapter adapter;   
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        new AsyncTask<Void,Void,Integer>() {

			@Override
			protected Integer doInBackground(Void... params) {
				// TODO Auto-generated method stub
				Long now = SystemClock.currentThreadTimeMillis();
				adapter = new NJTransitDBAdapter(Main.this).open();
				Long later = SystemClock.currentThreadTimeMillis();
				Log.d("database", later - now + " seconds");
				return 1;
			}
        	
        }.execute();
        
        
    }
}