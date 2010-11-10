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
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.zoterodroid.Constants;
import com.zoterodroid.client.ZoteroApi;
import com.zoterodroid.platform.CitationManager;
import com.zoterodroid.providers.CitationContent.Citation;

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
            InsertCitations(account, syncResult);
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
    
    private void InsertCitations(Account account, SyncResult syncResult) 
    	throws AuthenticationException, IOException{
    	
    	AccountManager am = AccountManager.get(mContext);
    	String userid = am.getUserData(account, Constants.PREFS_AUTH_USER_ID);
    	Log.d("userid", userid);
    	String username = account.name;

		ArrayList<Citation> addCitationList = new ArrayList<Citation>();

		Log.d("CitationSync", "In Citation Load");
		addCitationList = ZoteroApi.getAllCitations(null, account, userid, mContext);


		if(!addCitationList.isEmpty()){				
			for(Citation b : addCitationList){
				CitationManager.AddCitation(b, username, mContext);
			}
		}
    }
}