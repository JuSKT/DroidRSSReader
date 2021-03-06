package com.juliencolle.rssreader;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.juliencolle.db.DbAdapter;
import com.juliencolle.model.article.Article;
import com.juliencolle.rssreader.adapter.ArticleListAdapter;
import com.juliencolle.rssreader.service.FetchRssDataService;

/**
 * An activity representing a list of Items. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ItemDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details (if present) is a
 * {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required {@link ItemListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class ItemListActivity extends FragmentActivity implements
		ItemListFragment.Callbacks {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	
	PendingIntent pintent;
	AlarmManager alarm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_list);

		if (findViewById(R.id.item_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((ItemListFragment) getSupportFragmentManager().findFragmentById(
					R.id.item_list)).setActivateOnItemClick(true);
		}
		
		pintent = PendingIntent.getService(this, 
				0, 
				new Intent(this, FetchRssDataService.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP), 
				PendingIntent.FLAG_CANCEL_CURRENT
				);
		alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
	}
	
	protected void onResume(){
		super.onResume();
		// Start every 5 minutes => 5*60 seconds
		alarm.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 60*1000, pintent);
	}
	
	protected void onPause(){
		super.onPause();
		alarm.cancel(pintent);
	}

	/**
	 * Callback method from {@link ItemListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		
		Article selected = (Article) ((ItemListFragment) getSupportFragmentManager().findFragmentById(R.id.item_list)).getListAdapter().getItem(Integer.parseInt(id));
        
        //mark article as read
		DbAdapter dba = new DbAdapter(getApplicationContext());
        dba.openToWrite();
        dba.markAsRead(selected.getGuid());
        dba.close();
        selected.setRead(true);
        ArticleListAdapter adapter = (ArticleListAdapter) ((ItemListFragment) getSupportFragmentManager().findFragmentById(R.id.item_list)).getListAdapter();
        adapter.notifyDataSetChanged();
        Log.e("CHANGE", "Changing to read: "+selected.getGuid());
		
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(ItemDetailFragment.ARG_ITEM_ID, selected.getGuid());
			ItemDetailFragment fragment = new ItemDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.item_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, ItemDetailActivity.class);
			detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, selected.getGuid());
			startActivity(detailIntent);
		}
	}
}
