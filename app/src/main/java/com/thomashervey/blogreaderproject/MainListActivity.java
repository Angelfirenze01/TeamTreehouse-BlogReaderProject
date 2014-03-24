package com.thomashervey.blogreaderproject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 			  This class sets up a view that displays a blog post list from
 * 			  online. The class utilizes asynchronous tasks and error handling
 * 			  to make sure that a blog list is populated quickly and without
 * 			  errors.
 *
 * 			  This project was created while following the teamtreehouse.com
 * 			  Build A Blog Reader App project
 *
 * @version   Completed Jan 28, 2014
 * @author    Thomas Hervey <thomasahervey@gmail.com>
 */
public class MainListActivity extends ListActivity {
	
	public static final int NUMBER_OF_POSTS = 20;
	public static final String TAG = MainListActivity.class.getSimpleName();
	protected JSONObject mBlogData;
	protected ProgressBar mProgressBar;
	
	private final String KEY_TITLE = "title";
	private final String KEY_AUTHOR = "author";
	
	/**
	 * Initial create on activity load setting up layout
	 * and starting a new blog post task
	 * 
	 * @param  savaInstanceState
	 * @return none
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
        
        // set progress bar and start new blog post task
        if (isNetworkAvailable()) {
        	mProgressBar.setVisibility(View.VISIBLE);
        	GetBlogPostsTask getBlogPostsTask = new GetBlogPostsTask();
        	getBlogPostsTask.execute();
        }
        else {
        	Toast.makeText(this, "Network is unavailable!", Toast.LENGTH_LONG).show();
        }
        
        //Toast.makeText(this, getString(R.string.no_items), Toast.LENGTH_LONG).show();
    }
    
    /**
     * Handler for click on specific blog post sending user
     * to online post. This is accomplished by starting a new
     * blog web view activity
     * 
     * @param  
     * @return none
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	try {
    		// get JSON data from clicked blog post
    		JSONArray jsonPosts = mBlogData.getJSONArray("posts");
    		JSONObject jsonPost = jsonPosts.getJSONObject(position);
    		String blogUrl = jsonPost.getString("url");
    		
    		// start new blog web view activity based on post data
    		Intent intent = new Intent(this, BlogWebViewActivity.class);
    		intent.setData(Uri.parse(blogUrl));
    		startActivity(intent);
    	}
    	catch (JSONException e) {
    		logException(e);
    	}
    }
    
    /**
     * General exception caught logging
     * 
     * @param  e
     * @return none
     */
    private void logException(Exception e) {
    	Log.e(TAG, "Exception caught!", e);
	}
    
    /**
     * Checks to see if internet connection is available
     * 
     * @param  none
     * @return boolean
     */
	private boolean isNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) 
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		
		boolean isAvailable = false;
		if (networkInfo != null && networkInfo.isConnected()) {
			isAvailable = true;
		}
		
		return isAvailable;
	}
	
	/**
	 * Blog post handler that controls online pull request
	 * 
	 * @param  none
	 * @return none
	 */
	public void handleBlogResponse() {
		mProgressBar.setVisibility(View.INVISIBLE);
		
		// display error if no blog data to retrieve
		if (mBlogData == null) {
			updateDisplayForError();
		}
		// if there's data, populate & display an array of posts
		else {
			try {
				JSONArray jsonPosts = mBlogData.getJSONArray("posts");
				ArrayList<HashMap<String, String>> blogPosts = 
						new ArrayList<HashMap<String, String>>();
				// for each retrieved post
				for (int i = 0; i < jsonPosts.length(); i++) {
					// get JSON data
					JSONObject post = jsonPosts.getJSONObject(i);
					String title = post.getString(KEY_TITLE);
					title = Html.fromHtml(title).toString();
					String author = post.getString(KEY_AUTHOR);
					author = Html.fromHtml(author).toString();
					
					// generate a HashMap blog post
					HashMap<String, String> blogPost = new HashMap<String, String>();
					blogPost.put(KEY_TITLE, title);
					blogPost.put(KEY_AUTHOR, author);
					
					blogPosts.add(blogPost);
				}
				
				// create a simple adapter based on blog titles & authors
				String[] keys = { KEY_TITLE, KEY_AUTHOR };
				int[] ids = { android.R.id.text1, android.R.id.text2 };
				SimpleAdapter adapter = new SimpleAdapter(this, blogPosts,
						android.R.layout.simple_list_item_2, 
						keys, ids);
				
				setListAdapter(adapter);
			} 
			catch (JSONException e) {
				logException(e);
			}
		}
	}
	
	/**
	 * Display's an error when there's no blog data to retrieve
	 * 
	 * @param  none
	 * @return none
	 */
	private void updateDisplayForError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.error_title));
		builder.setMessage(getString(R.string.error_message));
		builder.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
		
		TextView emptyTextView = (TextView) getListView().getEmptyView();
		emptyTextView.setText(getString(R.string.no_items));
	}
    
	/**
	 * Helper class asynchronous task handler to connect,
	 * get online blog posts, and generate JSON response
	 */
    private class GetBlogPostsTask extends AsyncTask<Object, Void, JSONObject> {
    	
    	/**
    	 * Background HTTP connection setting up input stream
    	 * 
    	 * @param  ...
    	 * @return JSONObject jsonResponse - json connection
    	 */
		@Override
		protected JSONObject doInBackground(Object... arg0) {
			int responseCode = -1;
			JSONObject jsonResponse = null;
			
	        try {
	        	// connect to blog list url
	        	URL blogFeedUrl = new URL("http://blog.teamtreehouse.com/api/get_recent_summary/?count=" + NUMBER_OF_POSTS);
	        	HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
	        	connection.connect();
	        	
	        	responseCode = connection.getResponseCode();
	        	
	        	// if there's a connection, read data into a JSON response
	        	if (responseCode == HttpURLConnection.HTTP_OK) {
	        		InputStream inputStream = connection.getInputStream();
	        		Reader reader = new InputStreamReader(inputStream);
	        		int contentLength = connection.getContentLength();
	        		char[] charArray = new char[contentLength];
	        		reader.read(charArray);
	        		String responseData = new String(charArray);
	        		
	        		jsonResponse = new JSONObject(responseData);
	        	}
	        	else {
	        		Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
	        	}
	        }
	        catch (MalformedURLException e) {
	        	logException(e);
	        }
	        catch (IOException e) {
	        	logException(e);
	        }
	        catch (Exception e) {
	        	logException(e);
	        }
	        
	        return jsonResponse;
		}
		
		/**
		 * Handle the blog response on execute
		 * 
		 * @param  none
		 * @return none
		 */
		@Override
		protected void onPostExecute(JSONObject result) {
			mBlogData = result;
			handleBlogResponse();
		}
    }
}
