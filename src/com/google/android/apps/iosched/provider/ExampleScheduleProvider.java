/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.apps.iosched.provider;

import java.util.Arrays;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.android.apps.iosched.provider.ScheduleContract.Blocks;
import com.google.android.apps.iosched.provider.ScheduleContract.Sessions;

/**
 * Provider that stores {@link ScheduleContract} data. Data is usually inserted
 * by {@link SyncService}, and queried by various {@link Activity} instances.
 */
public class ExampleScheduleProvider extends ContentProvider {
    private static final String TAG = "ScheduleProvider";
    private static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int BLOCKS = 100;
    private static final int BLOCKS_BETWEEN = 101;
    private static final int BLOCKS_ID = 102;
    private static final int BLOCKS_ID_SESSIONS = 103;

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ScheduleContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "blocks", BLOCKS);
        matcher.addURI(authority, "blocks/between/*/*", BLOCKS_BETWEEN);
        matcher.addURI(authority, "blocks/*", BLOCKS_ID);
        matcher.addURI(authority, "blocks/*/sessions", BLOCKS_ID_SESSIONS);

        return matcher;
    }

    @Override
    public boolean onCreate() {
    	Log.v(TAG, "ScheduleProvider.onCreate()");
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BLOCKS:
                return Blocks.CONTENT_TYPE;
            case BLOCKS_BETWEEN:
                return Blocks.CONTENT_TYPE;
            case BLOCKS_ID:
                return Blocks.CONTENT_ITEM_TYPE;
            case BLOCKS_ID_SESSIONS:
                return Sessions.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        if (LOGV) Log.v(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");

        final int match = sUriMatcher.match(uri);
        switch (match) {
        case BLOCKS:
        	Log.v(TAG, "blocks");
        	break;
        case BLOCKS_BETWEEN:
        	Log.v(TAG, "blocks_between");
        	break;
        case BLOCKS_ID:
        	Log.v(TAG, "blocks_id");
        	break;
        case BLOCKS_ID_SESSIONS:
        	Log.v(TAG, "blocks_id_sessions");
        	break;
        }
        
        return null;
    }
    
    @Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}