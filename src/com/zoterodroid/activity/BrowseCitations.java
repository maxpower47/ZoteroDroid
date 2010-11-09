/*
 * ZoteroDroid - http://code.google.com/p/ZoteroDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * ZoteroDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * ZoteroDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoteroDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.zoterodroid.activity;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.auth.AuthenticationException;

import com.zoterodroid.R;
import com.zoterodroid.Constants;
import com.zoterodroid.client.ZoteroApi;
import com.zoterodroid.client.DeliciousFeed;
import com.zoterodroid.listadapter.BookmarkListAdapter;
import com.zoterodroid.platform.BookmarkManager;
import com.zoterodroid.platform.TagManager;
import com.zoterodroid.providers.CitationContent.Citation;
import com.zoterodroid.providers.TagContent.Tag;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class BrowseCitations extends AppBaseActivity {
	
	private AccountManager mAccountManager;
	private Account mAccount;
	private ListView lv;
	private Context mContext;
	private Boolean myself;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_bookmarks);
		
		mAccountManager = AccountManager.get(this);
		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		mContext = this;
		
		Log.d("browse bookmarks", getIntent().getDataString());
		Uri data = getIntent().getData();
		String scheme = data.getScheme();
		String path = data.getPath();
		Log.d("path", path);
		String username = data.getQueryParameter("username");
		String tagname = data.getQueryParameter("tagname");
		String recent = data.getQueryParameter("recent");
		
		myself = mAccount.name.equals(username);
		
		ArrayList<Citation> bookmarkList = new ArrayList<Citation>();
		
		if(scheme.equals("content") && path.equals("/bookmarks") && myself){
			
			try{	
				if(tagname != null && tagname != "") {
					setTitle("My Bookmarks Tagged With " + tagname);
				} else {
					setTitle("My Bookmarks");
				}
				
				String[] projection = new String[] {Citation._ID, Citation.Url, Citation.Description, Citation.Meta, Citation.Tags};
				String selection = null;
				String sortorder = null;
				
				if(tagname != null && tagname != "") {
					selection = "(" + Citation.Tags + " LIKE '% " + tagname + " %' OR " +
						Citation.Tags + " LIKE '% " + tagname + "' OR " +
						Citation.Tags + " LIKE '" + tagname + " %' OR " +
						Citation.Tags + " = '" + tagname + "') AND " +
						Citation.Account + " = '" + username + "'";
				}
				
				if(recent != null && recent.equals("1")){
					sortorder = Citation.Time + " DESC";
				}
				
				Uri bookmarks = Citation.CONTENT_URI;
				
				Cursor c = managedQuery(bookmarks, projection, selection, null, sortorder);				
				
				if(c.moveToFirst()){
					int idColumn = c.getColumnIndex(Citation._ID);
					int urlColumn = c.getColumnIndex(Citation.Url);
					int descriptionColumn = c.getColumnIndex(Citation.Description);
					int tagsColumn = c.getColumnIndex(Citation.Tags);
					int metaColumn = c.getColumnIndex(Citation.Meta);
					
					do {
						
						Citation b = new Citation(c.getInt(idColumn), c.getString(urlColumn), 
								c.getString(descriptionColumn), "", c.getString(tagsColumn), "", 
								c.getString(metaColumn), 0);
						
						bookmarkList.add(b);
						
					} while(c.moveToNext());
						
				}

				setListAdapter(new BookmarkListAdapter(this, R.layout.bookmark_view, bookmarkList));	
			}
			catch(Exception e){}
			
		} else if(scheme.equals("content") && path.equals("/bookmarks")) {
			try{
				if(tagname != null && tagname != "") {
					setTitle("Bookmarks For " + username + " Tagged With " + tagname);
				} else {
					setTitle("Bookmarks For " + username);
				}
				
		    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		    	String bookmarkLimit = settings.getString("pref_contact_bookmark_results", "50");
		    	
				bookmarkList = DeliciousFeed.fetchFriendBookmarks(username, tagname, Integer.parseInt(bookmarkLimit));

				setListAdapter(new BookmarkListAdapter(this, R.layout.bookmark_view, bookmarkList));	
			}
			catch(Exception e){}
		} else if(scheme.equals("content") && path.equals("/network")){
			try{
				setTitle("My Network's Recent Bookmarks");
				
				bookmarkList = DeliciousFeed.fetchNetworkRecent(username);

				setListAdapter(new BookmarkListAdapter(this, R.layout.bookmark_view, bookmarkList));	
			}
			catch(Exception e){}
		} else if(scheme.equals("http") || scheme.equals("https")) {
			String url = data.toString();
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
			finish();
		}
		
		lv = getListView();
		lv.setTextFilterEnabled(true);
	
		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	Citation b = (Citation)lv.getItemAtPosition(position);
		    	
		    	String url = b.getUrl();
		    	Uri link = Uri.parse(url);
				Intent i = new Intent(Intent.ACTION_VIEW, link);
				
				startActivity(i);
		    }
		});
		
		/* Add Context-Menu listener to the ListView. */
		lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				menu.setHeaderTitle("Actions");
				if(myself){
					menu.add(Menu.NONE, 0, Menu.NONE, "Delete");
				} else {
					menu.add(Menu.NONE, 1, Menu.NONE, "Add");
				}
				
			}
		});
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem aItem) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
		final Citation b = (Citation)lv.getItemAtPosition(menuInfo.position);
		
		switch (aItem.getItemId()) {
			case 0:
				BookmarkTaskArgs args = new BookmarkTaskArgs(b, mAccount, mContext);	
				new DeleteBookmarkTask().execute(args);	
				return true;
				
			case 1:				
				Intent addBookmark = new Intent(this, AddBookmark.class);
				addBookmark.setAction(Intent.ACTION_SEND);
				addBookmark.putExtra(Intent.EXTRA_TEXT, b.getUrl());
				startActivity(addBookmark);
				return true;
		}
		return false;
	}
	
	private class DeleteBookmarkTask extends AsyncTask<BookmarkTaskArgs, Integer, Boolean>{
		private Context context;
		private Citation bookmark;
		private Account account;
		
		@Override
		protected Boolean doInBackground(BookmarkTaskArgs... args) {
			context = args[0].getContext();
			bookmark = args[0].getBookmark();
			account = args[0].getAccount();
			
			try {
				Boolean success = ZoteroApi.deleteBookmark(bookmark, account, context);
				if(success){
					BookmarkManager.DeleteBookmark(args[0].getBookmark(), context);
					return true;
				} else return false;
					
			} catch (IOException e) {
				return false;
			} catch (AuthenticationException e) {
				return false;
			}
		}

	    protected void onPostExecute(Boolean result) {
			if(result){
    			String[] tags = bookmark.getTags().split(" ");
    			for(String s:tags){
    				Tag t = new Tag(s, 1);    				
    				TagManager.UpleteTag(t, account.name, context);
    			}
				
				BookmarkListAdapter bla = (BookmarkListAdapter) lv.getAdapter();
				bla.remove(bookmark);
				
				Toast.makeText(context, "Bookmark Deleted Successfully", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
			}
			
	    }
	}

	private class BookmarkTaskArgs{
		private Citation bookmark;
		private Account account;
		private Context context;
		
		public Citation getBookmark(){
			return bookmark;
		}
		
		public Account getAccount(){
			return account;
		}
		
		public Context getContext(){
			return context;
		}
		
		public BookmarkTaskArgs(Citation b, Account a, Context c){
			bookmark = b;
			account = a;
			context = c;
		}
	}
}


