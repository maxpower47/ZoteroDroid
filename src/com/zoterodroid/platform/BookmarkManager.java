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

package com.zoterodroid.platform;

import com.zoterodroid.providers.CitationContent.Citation;
import com.zoterodroid.util.Md5Hash;

import android.content.ContentValues;
import android.content.Context;
import android.provider.BaseColumns;
import android.util.Log;

public class BookmarkManager {
	
	public static void AddBookmark(Citation bookmark, String account, Context context) {
		String url = bookmark.getUrl();

		if(!url.endsWith("/")){
			url = url + "/";
		}
		
		String hash = "";
		if(bookmark.getHash() == null || bookmark.getHash() == ""){
			hash = Md5Hash.md5(url);
			Log.d(url, hash);
		} else hash = bookmark.getHash();
		
		ContentValues values = new ContentValues();
		values.put(Citation.Description, bookmark.getDescription());
		values.put(Citation.Url, url);
		values.put(Citation.Notes, bookmark.getNotes());
		values.put(Citation.Tags, bookmark.getTags());
		values.put(Citation.Hash, hash);
		values.put(Citation.Meta, bookmark.getMeta());
		values.put(Citation.Time, bookmark.getTime());
		values.put(Citation.Account, account);
		
		context.getContentResolver().insert(Citation.CONTENT_URI, values);
	}
	
	public static void UpdateBookmark(Citation bookmark, String account, Context context){
		
		String selection = Citation.Hash + "='" + bookmark.getHash() + "' AND " +
							Citation.Account + " = '" + account + "'";
		
		ContentValues values = new ContentValues();
		values.put(Citation.Description, bookmark.getDescription());
		values.put(Citation.Url, bookmark.getUrl());
		values.put(Citation.Notes, bookmark.getNotes());
		values.put(Citation.Tags, bookmark.getTags());
		values.put(Citation.Meta, bookmark.getMeta());
		values.put(Citation.Time, bookmark.getTime());
		
		context.getContentResolver().update(Citation.CONTENT_URI, values, selection, null);
		
	}

	public static void DeleteBookmark(Citation bookmark, Context context){
		
		String selection = BaseColumns._ID + "=" + bookmark.getId();
		
		context.getContentResolver().delete(Citation.CONTENT_URI, selection, null);
	}
	
	public static void SetLastUpdate(Citation bookmark, Long lastUpdate, String account, Context context){
		
		String selection = Citation.Hash + "='" + bookmark.getHash() + "' AND " +
							Citation.Account + " = '" + account + "'";
		
		ContentValues values = new ContentValues();	
		values.put(Citation.LastUpdate, lastUpdate);
		
		context.getContentResolver().update(Citation.CONTENT_URI, values, selection, null);
	}
	
	public static void DeleteOldBookmarks(Long lastUpdate, String account, Context context){
		String selection = "(" + Citation.LastUpdate + "<" + Long.toString(lastUpdate) + " OR " +
		Citation.LastUpdate + " is null) AND " +
		Citation.Account + " = '" + account + "'";
		
		Log.d("DeleteOldSelection", selection);
		
		context.getContentResolver().delete(Citation.CONTENT_URI, selection, null);
	}
}