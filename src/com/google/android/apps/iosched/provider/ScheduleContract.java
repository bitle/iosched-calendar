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

import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.DateUtils;

import com.google.android.apps.iosched.util.ParserUtils;

/**
 * Contract class for interacting with {@link ExampleScheduleProvider}. Unless
 * otherwise noted, all time-based fields are milliseconds since epoch and can
 * be compared against {@link System#currentTimeMillis()}.
 * <p>
 * The backing {@link android.content.ContentProvider} assumes that {@link Uri} are generated
 * using stronger {@link String} identifiers, instead of {@code int}
 * {@link BaseColumns#_ID} values, which are prone to shuffle during sync.
 */
public class ScheduleContract {

    /**
     * Special value for {@link SyncColumns#UPDATED} indicating that an entry
     * has never been updated, or doesn't exist yet.
     */
    public static final long UPDATED_NEVER = -2;

    /**
     * Special value for {@link SyncColumns#UPDATED} indicating that the last
     * update time is unknown, usually when inserted from a local file source.
     */
    public static final long UPDATED_UNKNOWN = -1;

    public interface SyncColumns {
        /** Last time this entry was updated or synchronized. */
        String UPDATED = "updated";
    }

    interface BlocksColumns {
        /** Unique string identifying this block of time. */
        String BLOCK_ID = "block_id";
        /** Title describing this block of time. */
        String BLOCK_TITLE = "block_title";
        /** Time when this block starts. */
        String BLOCK_START = "block_start";
        /** Time when this block ends. */
        String BLOCK_END = "block_end";
        /** Type describing this block. */
        String BLOCK_TYPE = "block_type";
    }

    public static final String CONTENT_AUTHORITY = "com.google.android.apps.iosched.calendar";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_BLOCKS = "blocks";
    private static final String PATH_BETWEEN = "between";
    private static final String PATH_SESSIONS = "sessions";

    /**
     * Blocks are generic timeslots that {@link Sessions} and other related
     * events fall into.
     */
    public static class Blocks implements BlocksColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BLOCKS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.iosched.block";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.iosched.block";

        /** Count of {@link Sessions} inside given block. */
        public static final String SESSIONS_COUNT = "sessions_count";

        /**
         * Flag indicating that at least one {@link Sessions#SESSION_ID} inside
         * this block has {@link Sessions#SESSION_STARRED} set.
         */
        public static final String CONTAINS_STARRED = "contains_starred";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = BlocksColumns.BLOCK_START + " ASC, "
                + BlocksColumns.BLOCK_END + " ASC";

        /** Build {@link Uri} for requested {@link #BLOCK_ID}. */
        public static Uri buildBlockUri(String blockId) {
            return CONTENT_URI.buildUpon().appendPath(blockId).build();
        }

        /**
         * Build {@link Uri} that references any {@link Sessions} associated
         * with the requested {@link #BLOCK_ID}.
         */
        public static Uri buildSessionsUri(String blockId) {
            return CONTENT_URI.buildUpon().appendPath(blockId).appendPath(PATH_SESSIONS).build();
        }

        /**
         * Build {@link Uri} that references any {@link Blocks} that occur
         * between the requested time boundaries.
         */
        public static Uri buildBlocksBetweenDirUri(long startTime, long endTime) {
            return CONTENT_URI.buildUpon().appendPath(PATH_BETWEEN).appendPath(
                    String.valueOf(startTime)).appendPath(String.valueOf(endTime)).build();
        }

        /** Read {@link #BLOCK_ID} from {@link Blocks} {@link Uri}. */
        public static String getBlockId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        /**
         * Generate a {@link #BLOCK_ID} that will always match the requested
         * {@link Blocks} details.
         */
        public static String generateBlockId(long startTime, long endTime) {
            startTime /= DateUtils.SECOND_IN_MILLIS;
            endTime /= DateUtils.SECOND_IN_MILLIS;
            return ParserUtils.sanitizeId(startTime + "-" + endTime);
        }
    }

    private ScheduleContract() {
    }

}
