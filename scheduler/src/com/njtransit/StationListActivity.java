package com.njtransit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import com.njtransit.JumpDialog.OnJumpListener;
import com.njtransit.StationListView.OnStationListener;
import com.njtransit.domain.Station;
import com.njtransit.rail.R;
import com.njtransit.ui.adapter.StationAdapter;

import java.util.HashSet;
import java.util.Set;

/**
 * List for display all the stations in a particular order.
 * 
 * @author rgravener
 *
 */
public class StationListActivity extends SchedulerActivity implements OnJumpListener {

	private final Set<Character> stationLetters = new HashSet<Character>();
	
	private StationListView stations;
	
	private boolean canJump;

    private boolean showingFavorites;

	private JumpDialog jumpDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.station_list_home);
		if(savedInstanceState!=null) {
			return;
		}
		final SchedulerApplication app = this.getSchedulerContext();
		stations = (StationListView) findViewById(R.id.station_view);
		stations.setAdapter(new StationAdapter(this, R.layout.station_row,
				 app.getStations(), app));
		stations.setOnStationListener(new OnStationListener() {
			@Override
			public void onStationSelected(Station station) {
				Intent i = new Intent();
				i.putExtra("stationId", station.getId());
				setResult(RESULT_OK, i);
				finish();
			}
		});
		stations.setTextFilterEnabled(true);
		trackPageView(getClass().getSimpleName());
		if(stations.getCount()>20) {
			canJump = true;
			jumpDialog = new JumpDialog(this, this) {

				@Override
				protected void onLetterSelect(String c) {
					trackEvent("jumps", "Button", c+"" , c.hashCode());
				}

				@Override
				public void onBackPressed() {
					trackEvent("jump-cancelled", "Button", "back" , 0);
					super.onBackPressed();
				}
				
			}.only(getStationLetters());
			jumpDialog.show();
			trackEvent("default-jump", "popUp", "jump", 0);
		} else {
			trackEvent("no-jump", "none", "none", 1);
		}
		
	}

    private void showFavorites() {
        showingFavorites = true;
            stations.getStationAdapter().clear();

            for(Station s : getSchedulerContext().getAdapter().getMostVisitedStations(getSchedulerContext(),System.currentTimeMillis())) {
                stations.getStationAdapter().add(s);
            }
            trackPageView("Favorites");
    }

    private void clearFavorites() {
        stations.getStationAdapter().clear();
        for(Station s : getSchedulerContext().getStations()) {
            stations.getStationAdapter().add(s);
        }
        showingFavorites = false;
    }

	@Override
	public void onJump(String c) {
        if("FAV".equals(c)) {
            showFavorites();
            return;
        }
        if(showingFavorites) {
            clearFavorites();
        }
		Object[] sections = stations.getStationAdapter().getSections();
		for(int i = 0; i < sections.length; i++) {
			if(sections[i].equals(c)) {
				int pos = stations.getStationAdapter().getPositionForSection(i);
				stations.setSelectionFromTop(pos,0);
				break;
			}
		}
	}
	
	private Set<Character> getStationLetters() {
		synchronized (this) {
			if (stationLetters.isEmpty()) {
				for (Station s : getSchedulerContext().getStations()) {
					stationLetters.add(s.getName().charAt(0));
				}
			}
		}
		return stationLetters;
	}

    private MenuItem jump;
    private MenuItem favorites;
    private MenuItem clearFavorites;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		trackEvent("menu-click", "MenuButton", "click", 0);
        jump = menu.add(Menu.NONE,1,Menu.FIRST, getString(R.string.abc));
        jump.setIcon(R.drawable.small_tiles);
        favorites = menu.add(Menu.NONE,2,Menu.FIRST, getString(R.string.favorites));
        favorites.setIcon(R.drawable.favorites);
        clearFavorites = menu.add(Menu.NONE,3,Menu.FIRST,getString(R.string.clear_favorites));
        clearFavorites.setIcon(R.drawable.clear_favorites2);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==1) {	
			trackEvent("user-jump", "MenuItem", "click", item.getItemId());
			jumpDialog.show();
		}else
        if(item.getItemId()==2) {
            showFavorites();
            trackEvent("show-favorites","MenuItem","click",item.getItemId());
        }else
        if(item.getItemId()==3) {
            clearFavorites();
        }
		return super.onOptionsItemSelected(item);
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        favorites.setVisible(!showingFavorites);
        jump.setVisible(canJump);
        clearFavorites.setVisible(showingFavorites);
        return super.onPrepareOptionsMenu(menu);

    }
}
