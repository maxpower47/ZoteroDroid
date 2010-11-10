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
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.zoterodroid.Constants;
import com.zoterodroid.authenticator.AuthToken;
import com.zoterodroid.client.User;
import com.zoterodroid.client.User.Status;
import com.zoterodroid.platform.ContactManager;

import org.apache.http.ParseException;
import org.apache.http.auth.AuthenticationException;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.
 */
public class ContactSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";

    private final AccountManager mAccountManager;
    private final Context mContext;
    
    private String authtoken = null;

    public ContactSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {

        try {
        	// use the account manager to request the credentials
        	AuthToken at = new AuthToken(mContext, account);
        	authtoken = at.getAuthToken();
        	 
            // fetch updates from the sample service over the cloud
            // update platform contacts.
            Log.d(TAG, "Calling contactManager's sync contacts");

        } catch (final ParseException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "ParseException", e);
        }
    }
}
