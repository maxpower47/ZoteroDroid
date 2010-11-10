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

package com.zoterodroid.providers;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class CitationContent {

	public static class Citation implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + 
				CitationContentProvider.AUTHORITY + "/citation");
		
		public static final  String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.zoterodroid.citations";
		
		public static final String Account = "ACCOUNT";
		public static final String Title = "TITLE";
		public static final String Key = "KEY";
		public static final String Item_Type = "ITEM_TYPE";
		public static final String Creator_Summary = "CREATOR_SUMMARY";
		
		private int mId = 0;
		private String mAccount = null;
        private String mTitle = null;
        private String mKey = null;
        private String mItemType = null;
        private String mCreatorSummary = null;

        public int getId(){
        	return mId;
        }
        
        public String getTitle() {
            return mTitle;
        }

        public String getKey() {
            return mKey;
        }
        
        public String getItemType(){
        	return mItemType;
        }
        
        public String getCreatorSummary(){
        	return mCreatorSummary;
        }

        
        public Citation() {
        }
        
        public Citation(String title, String key, String itemType, String creatorSummary) {
        	mTitle = title;
            mKey = key;
            mItemType = itemType;
            mCreatorSummary = creatorSummary;
        }
               
        public Citation(int id, String title, String key, String itemType, String creatorSummary) {
            mId = id;
        	mTitle = title;
            mKey = key;
            mItemType = itemType;
            mCreatorSummary = creatorSummary;
        }
        
        public static ArrayList<Citation> valueOf(String userBookmark){
        	SAXReader reader = new SAXReader();
        	InputSource inputSource = new InputSource(new StringReader(userBookmark));
        	Document document = null;
			try {
				document = reader.read(inputSource);
			} catch (DocumentException e1) {
				e1.printStackTrace();
			}   	
        	
			document.getRootElement().add(DocumentHelper.createNamespace("atom",
				"http://www.w3.org/2005/Atom")); 
			
            String expression = "/atom:feed/atom:entry";
            ArrayList<Citation> list = new ArrayList<Citation>();
           
        	List<Element> nodes = document.selectNodes(expression);
			
			for(int i = 0; i < nodes.size(); i++){
				Node ntitle = nodes.get(i).selectSingleNode("atom:title");
				Node nkey = nodes.get(i).selectSingleNode("zapi:key");
				Node ntype = nodes.get(i).selectSingleNode("zapi:itemType");
				Node ncreatorSummary = nodes.get(i).selectSingleNode("zapi:creatorSummary");
				
				String stitle = null;
				String skey = null;
				String stype = null;
				String screatorSummary = null;
				
				if(ntitle != null)
					stitle = ntitle.getText();
				if(nkey != null)
					skey = nkey.getText();
				if(ntype != null)
					stype = ntype.getText();
				if(ncreatorSummary != null)
					screatorSummary = ncreatorSummary.getText();

				list.add(new Citation(stitle, skey, stype, screatorSummary));

			}
				
			return list;
        }
	}
}
