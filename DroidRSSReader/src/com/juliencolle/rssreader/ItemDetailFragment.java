package com.juliencolle.rssreader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.juliencolle.db.DbAdapter;
import com.juliencolle.model.article.Article;
import com.juliencolle.model.article.ArticleContent;
import com.juliencolle.rssreader.adapter.ArticleListAdapter;
import com.juliencolle.utils.DateUtils;


/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link ItemListActivity} in two-pane mode (on tablets) or a
 * {@link ItemDetailActivity} on handsets.
 */
public class ItemDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The Article content this fragment is presenting.
	 */
	private Article mItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemDetailFragment() {
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the Article content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
//			Log.d("MAP", ArticleContent.ITEMS_MAP.get(getArguments().getString(ARG_ITEM_ID)).getGuid());
			mItem = ArticleContent.ITEMS_MAP.get(getArguments().getString(ARG_ITEM_ID));
//			mItem = (Article) getArguments().getSerializable(Article.KEY);
		}
	}
	
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_menu, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d("item ID : ", "onOptionsItemSelected Item ID" + id);
        if (id == R.id.actionbar_saveoffline) {
        	Toast.makeText(getActivity().getApplicationContext(), "This article has been saved of offline reading.", Toast.LENGTH_LONG).show();
        	return true;
        } else if (id == R.id.actionbar_markunread) {
        	DbAdapter dba = new DbAdapter(getActivity());
            dba.openToWrite();
            dba.markAsUnread(mItem.getGuid());
            dba.close();
            mItem.setRead(false);
            ArticleListAdapter adapter = (ArticleListAdapter) ((ItemListFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.item_list)).getListAdapter();
            adapter.notifyDataSetChanged();
        	return true;
        } else {
        	return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_item_detail,
				container, false);

		// Show the Article content as text in a TextView.
		if (mItem != null) {
//			((TextView) rootView.findViewById(R.id.article_detail)).setText(mItem.getDescription());
			
			String title = mItem.getTitle();
        	String pubDate = mItem.getPubDate().toString();
            SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss Z", Locale.ENGLISH);
            try {
				Date pDate =  df.parse(pubDate);
				pubDate = "This post was published " + DateUtils.getDateDifference(pDate) + " by " + mItem.getAuthor();
			} catch (ParseException e) {
				Log.e("DATE PARSING", "Error parsing date..");
				pubDate = "published by " + mItem.getAuthor();
			} 
            
        	String content = mItem.getEncodedContent();
        	((TextView) rootView.findViewById(R.id.article_title)).setText(title);
        	((TextView) rootView.findViewById(R.id.article_author)).setText(pubDate);
            ((TextView) rootView.findViewById(R.id.article_detail)).setText(Html.fromHtml(content));
		}

		return rootView;
	}
}
