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

import android.content.ContentValues;
import android.content.Context;
import android.provider.BaseColumns;
import android.util.Log;

public class CitationManager {
	
	public static void AddCitation(Citation citation, String account, Context context) {
			
		Log.d("Adding Citation", citation.getKey());
		ContentValues values = new ContentValues();
		values.put(Citation.Title, citation.getTitle());
		values.put(Citation.Key, citation.getKey());
		values.put(Citation.Item_Type, citation.getItemType());
		values.put(Citation.Creator_Summary, citation.getCreatorSummary());
		values.put(Citation.Account, account);
		
		context.getContentResolver().insert(Citation.CONTENT_URI, values);
	}
	
	public static void UpdateCitation(Citation citation, String account, Context context){
	
		String selection = Citation.Key + "='" + citation.getKey() + "' AND " +
		Citation.Account + " = '" + account + "'";
		
		ContentValues values = new ContentValues();
		values.put(Citation.Title, citation.getTitle());
		values.put(Citation.Key, citation.getKey());
		values.put(Citation.Item_Type, citation.getItemType());
		values.put(Citation.Creator_Summary, citation.getCreatorSummary());
		
		context.getContentResolver().update(Citation.CONTENT_URI, values, selection, null);
	
	}
	
	public static void DeleteCitation(Citation bookmark, Context context){
	
		String selection = BaseColumns._ID + "=" + bookmark.getId();
		
		context.getContentResolver().delete(Citation.CONTENT_URI, selection, null);
	}
}