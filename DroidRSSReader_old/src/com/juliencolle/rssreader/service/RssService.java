package com.juliencolle.rssreader.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.juliencolle.rssreader.ItemListFragment;


public class RssService extends Service {

	private static final String RSS_URL = "http://www.ombudsman.europa.eu/rss/rss.xml";
	
	private ItemListFragment itemListFrag;
	
	public RssService(ItemListFragment itemListFrag){
		this.itemListFrag = itemListFrag;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.d(" =====> ", "starting service");
		
		RssAsyncTask rssat = new RssAsyncTask(itemListFrag);
		rssat.execute(RSS_URL);
		
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
}