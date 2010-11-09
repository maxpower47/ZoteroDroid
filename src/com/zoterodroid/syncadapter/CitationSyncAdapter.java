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

package com.zoterodroid.syncadapter;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.zoterodroid.R;
import com.zoterodroid.Constants;
import com.zoterodroid.activity.Main;
import com.zoterodroid.client.ZoteroApi;
import com.zoterodroid.client.Update;
import com.zoterodroid.platform.BookmarkManager;
import com.zoterodroid.platform.TagManager;
import com.zoterodroid.providers.CitationContent.Citation;
import com.zoterodroid.providers.TagContent.Tag;

import org.apache.http.ParseException;
import org.apache.http.auth.AuthenticationException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.
 */
public class CitationSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "CitationSyncAdapter";

    private final Context mContext;

    public CitationSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {

         try {
            InsertBookmarks(account, syncResult);
        } catch (final ParseException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "ParseException", e);
        } catch (final AuthenticationException e) {
            syncResult.stats.numAuthExceptions++;
            Log.e(TAG, "AuthException", e);
        } catch (final IOException e) {
            syncResult.stats.numIoExceptions++;
            Log.e(TAG, "IOException", e);
        }
    }
    
    private void InsertBookmarks(Account account, SyncResult syncResult) 
    	throws AuthenticationException, IOException{
    	
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
    	long lastUpdate = settings.getLong(Constants.PREFS_LAST_SYNC, 0);
    	Boolean notifyPref = settings.getBoolean("pref_notification", true);
    	Update update = null;
    	String username = account.name;

    	update = ZoteroApi.lastUpdate(account, mContext);
		
		if(notifyPref && update.getInboxNew() > 0) {
			NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification n = new Notification(R.drawable.icon, "New Zotero Bookmarks", System.currentTimeMillis());
			Intent ni = new Intent(mContext, Main.class);
			PendingIntent ci = PendingIntent.getActivity(mContext, 0, ni, 0);
			n.setLatestEventInfo(mContext, "New Bookmarks", "You Have " + Integer.toString(update.getInboxNew()) + " New Bookmark(s)", ci);
			
			nm.notify(1, n);
		}
    	
    	if(update.getLastUpdate() > lastUpdate) {
	
			ArrayList<Citation> addBookmarkList = new ArrayList<Citation>();
			ArrayList<Citation> updateBookmarkList = new ArrayList<Citation>();
			ArrayList<Citation> changeList = new ArrayList<Citation>();
			ArrayList<Citation> addList = new ArrayList<Citation>();
			ArrayList<Citation> updateList = new ArrayList<Citation>();
			ArrayList<Tag> tagList = new ArrayList<Tag>();

			if(lastUpdate == 0){
				Log.d("BookmarkSync", "In Bookmark Load");
				tagList = ZoteroApi.getTags(account, mContext);
				addBookmarkList = ZoteroApi.getAllBookmarks(null, account, mContext);
			} else {
				Log.d("BookmarkSync", "In Bookmark Update");
				tagList = ZoteroApi.getTags(account, mContext);
				changeList = ZoteroApi.getChangedBookmarks(account, mContext);
				
				for(Citation b : changeList){
				
					String[] projection = new String[] {Citation.Hash, Citation.Meta};
					String selection = Citation.Hash + "=?";
					String[] selectionArgs = new String[] {b.getHash()};
					
					Uri bookmarks = Citation.CONTENT_URI;
					
					Cursor c = mContext.getContentResolver().query(bookmarks, projection, selection, selectionArgs, null);
					
					if(c.getCount() == 0){
						addList.add(b);
					}
					
					if(c.moveToFirst()){
						int metaColumn = c.getColumnIndex(Citation.Meta);
						
						BookmarkManager.SetLastUpdate(b, update.getLastUpdate(), username, mContext);
						Log.d(b.getHash(), Long.toString(update.getLastUpdate()));
						
						do {							
							if(c.getString(metaColumn) == null || !c.getString(metaColumn).equals(b.getMeta())) {
								updateList.add(b);
							}	
						} while(c.moveToNext());
					}
					
					c.close();
				}
	
				BookmarkManager.DeleteOldBookmarks(update.getLastUpdate(), username, mContext);
				
				ArrayList<String> addHashes = new ArrayList<String>();
				for(Citation b : addList){
					addHashes.add(b.getHash());
				}
				Log.d("add size", Integer.toString(addHashes.size()));
				syncResult.stats.numInserts = addHashes.size();
				if(addHashes.size() > 0) {
					addBookmarkList = ZoteroApi.getBookmark(addHashes, account, mContext);
				}
				
				ArrayList<String> updateHashes = new ArrayList<String>();
				for(Citation b : updateList){
					updateHashes.add(b.getHash());
				}
				Log.d("update size", Integer.toString(updateHashes.size()));
				syncResult.stats.numUpdates = updateHashes.size();
				if(updateHashes.size() > 0) {
					updateBookmarkList = ZoteroApi.getBookmark(updateHashes, account, mContext);
				}
			}
			
			TagManager.TruncateTags(username, mContext);
			for(Tag b : tagList){
				TagManager.AddTag(b, username, mContext);
			}

			if(!addBookmarkList.isEmpty()){				
				for(Citation b : addBookmarkList){
					BookmarkManager.AddBookmark(b, username, mContext);
				}
			}
			
			if(!updateBookmarkList.isEmpty()){		
				for(Citation b : updateBookmarkList){
					BookmarkManager.UpdateBookmark(b, username, mContext);
				}
			}
			
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putLong(Constants.PREFS_LAST_SYNC, update.getLastUpdate());
            editor.commit();
    	} else {
    		Log.d("BookmarkSync", "No update needed.  Last update time before last sync.");
    	}
    }
}
