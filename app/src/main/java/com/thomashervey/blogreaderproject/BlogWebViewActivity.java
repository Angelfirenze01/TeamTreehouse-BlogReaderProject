package com.thomashervey.blogreaderproject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

/**
 * 			  This class supports the MainListActivity.java class by providing
 * 			  the setup credentials for displaying a web view within the app.
 * 			  This web view is generated when a user selects a blog post and
 * 			  wants to see further details.
 *
 * 			  This project was created while following the teamtreehouse.com
 * 			  Build A Blog Reader App project
 *
 * @version   Completed Jan 28, 2014
 * @author    Thomas Hervey <thomasahervey@gmail.com>
 */
public class BlogWebViewActivity extends Activity {
	
	protected String mUrl;
	
	/**
	 * Initial create on activity load pulling in intent data
	 * and loading online data into webview
	 * 
	 * @param  saveInstanceState
	 * @return none
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blog_web_view);
		
		Intent intent = getIntent();
		Uri blogUri = intent.getData();
		mUrl = blogUri.toString();
		
		WebView webView = (WebView) findViewById(R.id.webView1);
		webView.loadUrl(mUrl);
	}
	
	/**
	 * Menu action bar inflater
	 * 
	 * @param  menu
	 * @return boolean
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_blog_web_view, menu);
		return true;
	}
	
	/**
	 * Menu item selected handler that shares the selected post
	 * 
	 * @param  item - menu item clicked
	 * @retrun boolean
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_share) { sharePost(); }
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Generate new share intent post for social media
	 * 
	 * @param  none
	 * @return none
	 */
	private void sharePost() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, mUrl);
		startActivity(Intent.createChooser(shareIntent, getString(R.string.share_chooser_title)));
	}
}
