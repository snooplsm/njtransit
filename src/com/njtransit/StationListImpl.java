package com.njtransit;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.njtransit.domain.Session;
import com.njtransit.ui.adapter.StationAdapter;

public class StationListImpl extends ListView {
	
	private Integer type;
	
	private StationAdapter adapter;

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
		adapter.setType(type);
		invalidateViews();
	}

	public StationListImpl(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);		
		NJTransitDBAdapter adapt = new NJTransitDBAdapter(context).open();
		Session sess = Session.get();
		sess.setStations(adapt.getAllStations());
		adapter = new StationAdapter(context,R.layout.station_row,StationAdapter.ALPHA, sess.getStations(), sess);
		setAdapter(adapter);
		setType(attrs.getAttributeIntValue(null, "type", StationAdapter.ALPHA));
		setFastScrollEnabled(true);
		setTextFilterEnabled(true);
	}

	public StationListImpl(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StationListImpl(Context context) {
		this(context,null);
	}

}
