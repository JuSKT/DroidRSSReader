package com.juliencolle.rssreader.service;


import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.juliencolle.db.DbAdapter;
import com.juliencolle.model.article.Article;
import com.juliencolle.rssreader.ItemListFragment;
import com.juliencolle.rssreader.adapter.ArticleListAdapter;
import com.juliencolle.rssreader.parser.RssHandler;
import com.juliencolle.rssreader.parser.RssHandler.EnoughDataSAXTerminatorException;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.SQLException;
import android.os.AsyncTask;
import android.util.Log;


public class RssAsyncTask extends AsyncTask<String, Void, List<Article>> {

	private ProgressDialog progressDatabase;
	private ProgressDialog progressRSS;
	private Context context;
	private ItemListFragment itemListFrag;

	public RssAsyncTask(ItemListFragment itemListFragment) {
		context = itemListFragment.getActivity();
		itemListFrag = itemListFragment;
		
		progressDatabase = new ProgressDialog(context);
		progressRSS = new ProgressDialog(context);
		progressDatabase.setMessage("Loading databse...");
		progressRSS.setMessage("Loading RSS...");
	}


	protected void onPreExecute() {
		Log.e("ASYNC", "PRE EXECUTE");
		progressDatabase.show();
		
		DbAdapter dba = new DbAdapter(itemListFrag.getActivity());
		dba.openToRead();
		//LOAD ALL ARTICLES
		dba.close();
		
		ArticleListAdapter adapter = new ArticleListAdapter(itemListFrag.getActivity(), new ArrayList<Article>());
		itemListFrag.setListAdapter(adapter);
		adapter.notifyDataSetChanged();
		
		progressDatabase.dismiss();
	}


	protected  void onPostExecute(final List<Article>  articles) {
		Log.e("ASYNC", "POST EXECUTE");
		itemListFrag.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (Article a : articles){
					Log.d("DB", "Searching DB for GUID: " + a.getGuid());
					DbAdapter dba = new DbAdapter(itemListFrag.getActivity());
		            dba.openToRead();
		            Article fetchedArticle = null;
					try {
						fetchedArticle = dba.getBlogListing(a.getGuid());
					} catch (SQLException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					}
		            dba.close();
					if (fetchedArticle == null){
						Log.d("DB", "Found entry for first time: " + a.getTitle());
						dba = new DbAdapter(itemListFrag.getActivity());
			            dba.openToWrite();
			            dba.insertBlogListing(a.getGuid());
			            dba.close();
					}else{
						a.setDbId(fetchedArticle.getDbId());
						a.setOffline(fetchedArticle.isOffline());
						a.setRead(fetchedArticle.isRead());
					}
				}
				ArticleListAdapter adapter = new ArticleListAdapter(itemListFrag.getActivity(), articles);
				itemListFrag.setListAdapter(adapter);
				adapter.notifyDataSetChanged();
			}
		});
		//progressRSS.dismiss();
	}


	@Override
	protected List<Article> doInBackground(String... urls) {
		String feed = urls[0];

		URL url = null;
		try {

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();

			url = new URL(feed);
			RssHandler rh = new RssHandler();

			xr.setContentHandler(rh);
			xr.parse(new InputSource(url.openStream()));

			Log.e("ASYNC", "PARSING FINISHED");
			return rh.getArticleList();

		} catch (IOException e) {
			Log.e("RSS Handler IO", e.getMessage() + " >> " + e.toString());
		} catch (EnoughDataSAXTerminatorException e) {
			Log.e("RSS Handler SAX : enough data", e.toString());
		} catch (SAXException e) {
			Log.e("RSS Handler SAX", e.toString());
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			Log.e("RSS Handler Parser Config", e.toString());
		}

		return null;

	}
}