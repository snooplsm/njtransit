package com.njtransit;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.njtransit.R;

/** Activity used for viewing and modifying user preferences */
public class Prefs extends PreferenceActivity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
    }
}
