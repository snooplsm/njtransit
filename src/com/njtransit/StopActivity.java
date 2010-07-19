package com.njtransit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.njtransit.domain.Session;
import com.njtransit.domain.Station;

public class StopActivity extends Activity {

	private Session session = Session.get();

	private StopListView stopTimes;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stop_list_home);
		((TextView) findViewById(R.id.title)).setText(renderTitle(
				session.getDepartureStation(), session.getArrivalStation()));
		stopTimes = (StopListView) findViewById(R.id.list);
	}

	protected void onPause() {
		super.onPause();
		if (stopTimes != null) {
			stopTimes.onPause();
		}
	}

	protected void onResume() {
		super.onResume();
		if (stopTimes != null) {
			stopTimes.onResume();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE,1,Menu.FIRST,"Reverse");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==1) {
			session.reverseTrip();
			Intent intent = new Intent(this, StopActivity.class);
			startActivity(intent);
		}		
//		switch (item.getItemId()) {
//		case MENU_REFRESH:
//			executeSearchTask(null);
//			return true;
//		case MENU_SHOUT:
//			Intent intent = new Intent(this,
//					CheckinOrShoutGatherInfoActivity.class);
//			intent.putExtra(
//					CheckinOrShoutGatherInfoActivity.INTENT_EXTRA_IS_SHOUT,
//					true);
//			startActivity(intent);
//			return true;
//		case MENU_MYINFO:
//			Intent intentUser = new Intent(FriendsActivity.this,
//					UserDetailsActivity.class);
//			intentUser.putExtra(UserDetailsActivity.EXTRA_USER_ID,
//					((Foursquared) getApplication()).getUserId());
//			startActivity(intentUser);
//			return true;
//		case MENU_MORE:
//			// Submenu items generate id zero, but we check on item title below.
//			return true;
//		default:
//			if (item.getTitle().equals(
//					mMenuMoreSubitems.get(MENU_MORE_SORT_METHOD))) {
//				showDialog(DIALOG_SORT_METHOD);
//				return true;
//			} else if (item.getTitle().equals("Map")) {
//				startActivity(new Intent(FriendsActivity.this,
//						FriendsMapActivity.class));
//				return true;
//			} else if (item.getTitle().equals(
//					mMenuMoreSubitems.get(MENU_MORE_LEADERBOARD))) {
//				startActivity(new Intent(FriendsActivity.this,
//						StatsActivity.class));
//				return true;
//			} else if (item.getTitle().equals(
//					mMenuMoreSubitems.get(MENU_MORE_ADD_FRIENDS))) {
//				startActivity(new Intent(FriendsActivity.this,
//						AddFriendsActivity.class));
//				return true;
//			} else if (item.getTitle().equals(
//					mMenuMoreSubitems.get(MENU_MORE_FRIEND_REQUESTS))) {
//				startActivity(new Intent(FriendsActivity.this,
//						FriendRequestsActivity.class));
//				return true;
//			}
//			break;
//		}
		return super.onOptionsItemSelected(item);
	}

	private String renderTitle(Station departing, Station arriving) {
		return String.format("%s > %s", departing, arriving);
	}
}