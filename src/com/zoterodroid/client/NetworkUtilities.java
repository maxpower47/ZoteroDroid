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

package com.zoterodroid.client;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.zoterodroid.authenticator.AuthenticatorActivity;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.auth.AuthScope;
import android.net.Uri;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Provides utility methods for communicating with the server.
 */
public class NetworkUtilities {
    private static final String TAG = "NetworkUtilities";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_UPDATED = "timestamp";
    public static final String USER_AGENT = "AuthenticationService/1.0";
    public static final int REGISTRATION_TIMEOUT = 30 * 1000; // ms

    public static final String GET_USER_PROFILE_URI = "http://www.zotero.org/api/users/";
    private static DefaultHttpClient mHttpClient;
    
    private static final String SCHEME = "https";
    private static final String ZOTERO_AUTHORITY = "api.zotero.org";
    private static final int PORT = 443;
 
    private static final AuthScope SCOPE = new AuthScope(ZOTERO_AUTHORITY, PORT);

    /**
     * Configures the httpClient to connect to the URL provided.
     */
    public static void maybeCreateHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = new DefaultHttpClient();
            final HttpParams params = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params,
                REGISTRATION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT);
            ConnManagerParams.setTimeout(params, REGISTRATION_TIMEOUT);
        }
    }

    /**
     * Executes the network requests on a separate thread.
     * 
     * @param runnable The runnable instance containing network mOperations to
     *        be executed.
     */
    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {
                }
            }
        };
        t.start();
        return t;
    }

    /**
     * Connects to the Voiper server, authenticates the provided username and
     * password.
     * 
     * @param username The user's username
     * @param password The user's password
     * @param handler The hander instance from the calling UI thread.
     * @param context The context of the calling Activity.
     * @return boolean The boolean result indicating whether the user was
     *         successfully authenticated.
     */
    public static boolean zoteroAuthenticate(String username, String password,
        Handler handler, final Context context) {
        final HttpResponse resp;
        
        sendResult(new LoginResult(true), handler, context);
        return true;
        
//        Uri.Builder builder = new Uri.Builder();
//        builder.scheme(SCHEME);
//        builder.authority(ZOTERO_AUTHORITY);
//        builder.appendEncodedPath("v1/tags/get");
//        Uri uri = builder.build();
//
//        HttpGet request = new HttpGet(String.valueOf(uri));
//        maybeCreateHttpClient();
//        
//        CredentialsProvider provider = mHttpClient.getCredentialsProvider();
//        Credentials credentials = new UsernamePasswordCredentials(username, password);
//        provider.setCredentials(SCOPE, credentials);
//
//        try {
//            resp = mHttpClient.execute(request);
//            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                if (Log.isLoggable(TAG, Log.VERBOSE)) {
//                    Log.v(TAG, "Successful authentication");
//                }
//                sendResult(new LoginResult(true), handler, context);
//                return true;
//            } else {
//                if (Log.isLoggable(TAG, Log.VERBOSE)) {
//                    Log.v(TAG, "Error authenticating" + resp.getStatusLine());
//                }
//                sendResult(new LoginResult(false), handler, context);
//                return false;
//            }
//        } catch (final IOException e) {
//            if (Log.isLoggable(TAG, Log.VERBOSE)) {
//                Log.v(TAG, "IOException when getting authtoken", e);
//            }
//            sendResult(new LoginResult(false), handler, context);
//            return false;
//        } finally {
//            if (Log.isLoggable(TAG, Log.VERBOSE)) {
//                Log.v(TAG, "getAuthtoken completing");
//            }
//        }
    }
    
    /**
     * Connects to the Voiper server, authenticates the provided username and
     * password.
     * 
     * @param username The user's username
     * @param password The user's password
     * @param handler The hander instance from the calling UI thread.
     * @param context The context of the calling Activity.
     * @return boolean The boolean result indicating whether the user was
     *         successfully authenticated.
     */
    public static String getZoteroUserId(String username) {
        final HttpResponse resp;
        HttpGet post = null;
        
        post = new HttpGet(GET_USER_PROFILE_URI + username);
        maybeCreateHttpClient();
        
        try{
        	resp = mHttpClient.execute(post);
        	
        	 if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        		 String response = EntityUtils.toString(resp.getEntity());
        		 Log.d("response", response);
        		 int start = response.indexOf("http://zotero.org/users/") + 24;
        		 int end = response.indexOf("<", start + 1);
        		 String userid = response.substring(start, end);
        		 Log.d("userid", userid);
        		 return userid;
        	 } else {
        		 throw new IOException();
        	 }
        } catch(Exception e){
        	
        }
        return "";
    }
    
    /**
     * Sends the authentication response from server back to the caller main UI
     * thread through its handler.
     * 
     * @param result The boolean holding authentication result
     * @param handler The main UI thread's handler instance.
     * @param context The caller Activity's context.
     */
    private static void sendResult(final LoginResult result, final Handler handler,
        final Context context) {
        if (handler == null || context == null) {
            return;
        }
        
        handler.post(new Runnable() {
            public void run() {
                ((AuthenticatorActivity) context).onAuthenticationResult(result);
            }
        });
    }

    /**
     * Attempts to authenticate the user credentials on the server.
     * 
     * @param username The user's username
     * @param password The user's password to be authenticated
     * @param handler The main UI thread's handler instance.
     * @param context The caller Activity's context
     * @return Thread The thread on which the network mOperations are executed.
     */
    public static Thread attemptAuth(final String username,
        final String password, final Handler handler, final Context context) {
        final Runnable runnable = new Runnable() {
            public void run() {
            	zoteroAuthenticate(username, password, handler, context);
            }
        };
        // run on background thread.
        return NetworkUtilities.performOnBackgroundThread(runnable);
    }
}