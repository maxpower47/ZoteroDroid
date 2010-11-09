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

package com.zoterodroid;

import android.net.Uri;

public class Constants {

    /**
     * Account type string.
     */
    public static final String ACCOUNT_TYPE = "com.zoterodroid";
    
    public static final Uri BOOKMARK_CONENT_URI = Uri.parse("content://com.zoterodroid.bookmark");
    
    public static final Uri CONTENT_URI_BASE = Uri.parse("content://com.zoterodroid");

    /**
     * Authtoken type string.
     */
    public static final String AUTHTOKEN_TYPE = "com.zoterodroid";
    
    public static final String AUTH_PREFS_NAME = "com.zoterodroid.auth";
    
    public static final String PREFS_AUTH_USER_ID = "user_id";
    
    public static final String PREFS_LAST_SYNC = "last_sync";
    
    public static final String PREFS_AUTH_TYPE = "authentication_type";
    public static final String AUTH_TYPE_OAUTH = "oauth";
    public static final String AUTH_TYPE_ZOTERO = "zotero";
    
    public static final String PREFS_INITIAL_SYNC = "initial_sync";
    
}