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

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.zoterodroid.authenticator.AuthToken;
import com.zoterodroid.providers.CitationContent.Citation;
import com.zoterodroid.providers.TagContent.Tag;

public class ZoteroApi {
	
    private static final String TAG = "ZoteroApi";

    public static final String USER_AGENT = "AuthenticationService/1.0";
    public static final int REGISTRATION_TIMEOUT = 30 * 1000; // ms

    public static final String FETCH_TAGS_URI = "tags/get";
    public static final String FETCH_CITATIONS_URI = "users/<userID>/items/top";
    public static final String FETCH_CHANGED_BOOKMARKS_URI = "posts/all";
    public static final String FETCH_BOOKMARK_URI = "posts/get";
    public static final String LAST_UPDATE_URI = "posts/update";
    public static final String DELETE_BOOKMARK_URI = "posts/delete";
    public static final String ADD_BOOKMARKS_URI = "posts/add";
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
            HttpConnectionParams.setConnectionTimeout(params, REGISTRATION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT);
            ConnManagerParams.setTimeout(params, REGISTRATION_TIMEOUT);
        }
    }
    
    /**
     * Fetches users bookmarks
     * 
     * @param account The account being synced.
     * @param authtoken The authtoken stored in the AccountManager for the
     *        account
     * @return list The list of bookmarks received from the server.
     * @throws AuthenticationException 
     */
    public static Update lastUpdate(Account account, Context context)
    	throws IOException, AuthenticationException {

    	String response = null;
    	TreeMap<String, String> params = new TreeMap<String, String>();
    	Update update = null;
    	String url = LAST_UPDATE_URI;
    	
    	response = ZoteroApiCall(url, params, account, context);
    	
        if (response.contains("<?xml")) {
        	update = Update.valueOf(response);
        } else {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
        }
        return update;
    }
    
    /**
     * Fetches users bookmarks
     * 
     * @param account The account being synced.
     * @param authtoken The authtoken stored in the AccountManager for the
     *        account
     * @return list The list of bookmarks received from the server.
     * @throws AuthenticationException 
     */
    public static ArrayList<Citation> getBookmark(ArrayList<String> hashes, Account account,
        Context context) throws IOException, AuthenticationException {

    	ArrayList<Citation> bookmarkList = new ArrayList<Citation>();
    	TreeMap<String, String> params = new TreeMap<String, String>();
    	String hashString = "";
    	String response = null;
    	String url = FETCH_BOOKMARK_URI;

    	for(String h : hashes){
    		if(hashes.get(0) != h){
    			hashString += "+";
    		}
    		hashString += h;
    	}
    	params.put("meta", "yes");
    	params.put("hashes", hashString);

    	response = ZoteroApiCall(url, params, account, context);
    	
        if (response.contains("<?xml")) {
            bookmarkList = Citation.valueOf(response);
        } else {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
        }
        return bookmarkList;
    }
    
    /**
     * Fetches users bookmarks
     * 
     * @param account The account being synced.
     * @param authtoken The authtoken stored in the AccountManager for the
     *        account
     * @return list The list of bookmarks received from the server.
     * @throws AuthenticationException 
     */
    public static ArrayList<Citation> getAllCitations(String tagName, Account account, String userid, Context context) 
    	throws IOException, AuthenticationException {
    	
    	ArrayList<Citation> citationList = new ArrayList<Citation>();
    	String response = null;
    	TreeMap<String, String> params = new TreeMap<String, String>();
    	String url = FETCH_CITATIONS_URI.replace("<userID>", userid);

    	response = ZoteroApiCall(url, params, account, context);
    	
        if (response.contains("<?xml")) {
        	Log.d("citationresp", response);
        	citationList = Citation.valueOf(response);
         
        } else {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
        }
        return citationList;
    }
    
    /**
     * Fetches users bookmarks
     * 
     * @param account The account being synced.
     * @param authtoken The authtoken stored in the AccountManager for the
     *        account
     * @return list The list of bookmarks received from the server.
     * @throws AuthenticationException 
     */
    public static ArrayList<Citation> getChangedBookmarks(Account account, Context context) 
    	throws IOException, AuthenticationException {
    	
    	ArrayList<Citation> bookmarkList = new ArrayList<Citation>();
    	String response = null;
    	TreeMap<String, String> params = new TreeMap<String, String>();
    	String url = FETCH_CHANGED_BOOKMARKS_URI;

    	params.put("hashes", "yes");

    	response = ZoteroApiCall(url, params, account, context);

        if (response.contains("<?xml")) {

        	bookmarkList = Citation.valueOf(response);
         
        } else {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
        }
        return bookmarkList;
    }
    
    /**
     * Fetches status messages for the user's friends from the server
     * 
     * @param account The account being synced.
     * @param authtoken The authtoken stored in the AccountManager for the
     *        account
     * @return list The list of status messages received from the server.
     * @throws AuthenticationException 
     */
    public static ArrayList<Tag> getTags(Account account, Context context) 
    	throws IOException, AuthenticationException {
    	
    	ArrayList<Tag> tagList = new ArrayList<Tag>();
    	String response = null;
    	TreeMap<String, String> params = new TreeMap<String, String>();
    	String url = FETCH_TAGS_URI;
    	  	
    	response = ZoteroApiCall(url, params, account, context);
    	Log.d("loadTagResponse", response);
    	
        if (response.contains("<?xml")) {
        	tagList = Tag.valueOf(response);
        } else {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
        }
        return tagList;
    }
    
    private static String ZoteroApiCall(String url, TreeMap<String, String> params, 
    		Account account, Context context) throws IOException, AuthenticationException{
    	
    	String username = account.name;
    	String authtoken = null;
    	String scheme = null;
    	
    	AuthToken at = new AuthToken(context, account);
    	authtoken = at.getAuthToken();
    	
    	scheme = SCHEME;
    	
    	HttpResponse resp = null;
    	HttpGet post = null;
    	
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(scheme);
		builder.authority(ZOTERO_AUTHORITY);
		builder.appendEncodedPath(url);
		for(String key : params.keySet()){
			builder.appendQueryParameter(key, params.get(key));
		}
		
		Log.d("apiCallUrl", builder.build().toString());
		post = new HttpGet(builder.build().toString());

		maybeCreateHttpClient();
		post.setHeader("User-Agent", "ZoteroDroid");
    	
        CredentialsProvider provider = mHttpClient.getCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials(username, authtoken);
        provider.setCredentials(SCOPE, credentials);
        
        resp = mHttpClient.execute(post);
	        
    	if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
    		return EntityUtils.toString(resp.getEntity());
    	} else if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    		throw new AuthenticationException();
    	} else {
    		throw new IOException();
    	}
    }
}