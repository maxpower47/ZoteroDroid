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
import com.zoterodroid.listadapter.CitationListAdapter;
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
		
		ArrayList<Citation> citationList = new ArrayList<Citation>();
		
		if(scheme.equals("content") && path.equals("/citations") && myself){
			
			try{	
				
				String[] projection = new String[] {Citation._ID, Citation.Title, Citation.Key, Citation.Creator_Summary, Citation.Item_Type};
				String selection = null;
				String sortorder = null;

				selection = Citation.Account + " = '" + username + "'";

				
				Uri citations = Citation.CONTENT_URI;
				
				Cursor c = managedQuery(citations, projection, selection, null, sortorder);				
				
				if(c.moveToFirst()){
					int idColumn = c.getColumnIndex(Citation._ID);
					int titleColumn = c.getColumnIndex(Citation.Title);
					int keyColumn = c.getColumnIndex(Citation.Key);
					int creatorSummaryColumn = c.getColumnIndex(Citation.Creator_Summary);
					int itemTypeColumn = c.getColumnIndex(Citation.Item_Type);
					
					do {
						
						Citation b = new Citation(c.getInt(idColumn), c.getString(titleColumn), 
								c.getString(keyColumn), c.getString(creatorSummaryColumn),
								c.getString(itemTypeColumn));
						
						citationList.add(b);
						
					} while(c.moveToNext());
						
				}

				setListAdapter(new CitationListAdapter(this, R.layout.bookmark_view, citationList));	
			}
			catch(Exception e){}
			
		}
		
		lv = getListView();
		lv.setTextFilterEnabled(true);
	
		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	
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
				return true;
				
			case 1:				
				return true;
		}
		return false;
	}
}


