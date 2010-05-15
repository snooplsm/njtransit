package com.njtransit;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

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
				adapter = new NJTransitDBAdapter(Main.this).open();
				return 1;
			}
        	
        }.execute();
        
        
    }
}