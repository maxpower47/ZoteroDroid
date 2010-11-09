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

package com.zoterodroid.listadapter;

import java.util.ArrayList;

import com.zoterodroid.R;
import com.zoterodroid.providers.CitationContent.Citation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BookmarkListAdapter extends ArrayAdapter<Citation> {
	
	private ArrayList<Citation> bookmarks;
	
    public BookmarkListAdapter(Context context, int textViewResourceId, ArrayList<Citation> bookmarks) {
        super(context, textViewResourceId, bookmarks);
        this.bookmarks = bookmarks;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.bookmark_view, null);
        }
        Citation o = bookmarks.get(position);
        if (o != null) {
         	TextView td = (TextView) v.findViewById(R.id.bookmark_description);
         	TextView tu = (TextView) v.findViewById(R.id.bookmark_tags);
            if (td != null) {
               	td.setText(o.getDescription());                            
            }
            if (tu != null) {
               	tu.setText(o.getTags());                            
            }

        }
        return v;
    }
}
