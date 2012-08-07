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

package com.google.android.apps.iosched.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.apps.iosched.R;
import com.google.android.apps.iosched.provider.ScheduleContract;
import com.google.android.apps.iosched.ui.widget.BlockView;
import com.google.android.apps.iosched.ui.widget.BlocksLayout;
import com.google.android.apps.iosched.ui.widget.ObservableScrollView;
import com.google.android.apps.iosched.ui.widget.Workspace;
import com.google.android.apps.iosched.util.Maps;
import com.google.android.apps.iosched.util.MotionEventUtils;
import com.google.android.apps.iosched.util.ParserUtils;
import com.google.android.apps.iosched.util.UIUtils;

public class ScheduleFragment extends Fragment implements ObservableScrollView.OnScrollListener, View.OnClickListener {
	private static final String TAG = "ScheduleFragment";
	public static final String START_DATE = "start_date";
	public static final String END_DATE = "end_date";
	
	/**
     * Flags used with {@link android.text.format.DateUtils#formatDateRange}.
     */
    private static final int TIME_FLAGS = DateUtils.FORMAT_SHOW_DATE
            | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY;
	
	private static final long TUE_START = ParserUtils.parseTime("2011-05-10T00:00:00.000-07:00");
    private static final long WED_START = ParserUtils.parseTime("2011-05-11T00:00:00.000-07:00");
	
	private Workspace mWorkspace;
    private TextView mTitle;
    private int mTitleCurrentDayIndex = -1;
    private View mLeftIndicator;
    private View mRightIndicator;
    
    /**
     * A helper class containing object references related to a particular day in the schedule.
     */
    private class Day {
        private ViewGroup rootView;
        private ObservableScrollView scrollView;
        private View nowView;
        private BlocksLayout blocksView;

        private int index = -1;
        private String label = null;
        private Uri blocksUri = null;
        private long timeStart = -1;
        private long timeEnd = -1;
    }
    
    private List<Day> mDays = new ArrayList<Day>();
    
    private static HashMap<String, Integer> buildTypeColumnMap() {
        final HashMap<String, Integer> map = Maps.newHashMap();
        map.put(ParserUtils.BLOCK_TYPE_FOOD, 0);
        map.put(ParserUtils.BLOCK_TYPE_SESSION, 1);
        map.put(ParserUtils.BLOCK_TYPE_OFFICE_HOURS, 2);
        return map;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO:
        //mHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_schedule, null);

        mWorkspace = (Workspace) root.findViewById(R.id.workspace);

        mTitle = (TextView) root.findViewById(R.id.block_title);

        mLeftIndicator = root.findViewById(R.id.indicator_left);
        mLeftIndicator.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if ((motionEvent.getAction() & MotionEventUtils.ACTION_MASK)
                        == MotionEvent.ACTION_DOWN) {
                    mWorkspace.scrollLeft();
                    return true;
                }
                return false;
            }
        });
        mLeftIndicator.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mWorkspace.scrollLeft();
            }
        });

        mRightIndicator = root.findViewById(R.id.indicator_right);
        mRightIndicator.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if ((motionEvent.getAction() & MotionEventUtils.ACTION_MASK)
                        == MotionEvent.ACTION_DOWN) {
                    mWorkspace.scrollRight();
                    return true;
                }
                return false;
            }
        });
        mRightIndicator.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mWorkspace.scrollRight();
            }
        });

        setupDays(inflater);

        updateWorkspaceHeader(0);
        mWorkspace.setOnScrollListener(new Workspace.OnScrollListener() {
            public void onScroll(float screenFraction) {
                updateWorkspaceHeader(Math.round(screenFraction));
            }
        }, true);

        return root;
    }

	public void updateWorkspaceHeader(int dayIndex) {
        if (mTitleCurrentDayIndex == dayIndex) {
            return;
        }

        mTitleCurrentDayIndex = dayIndex;
        Day day = mDays.get(dayIndex);
        mTitle.setText(day.label);

        mLeftIndicator
                .setVisibility((dayIndex != 0) ? View.VISIBLE : View.INVISIBLE);
        mRightIndicator
                .setVisibility((dayIndex < mDays.size() - 1) ? View.VISIBLE : View.INVISIBLE);
    }
	
	private void setupDays(LayoutInflater inflater) {
		long startDate = getArguments().getLong(START_DATE, -1);
		long endDate = getArguments().getLong(END_DATE, -1);
		
		if (startDate < 0 || endDate < 0) {
			throw new IllegalArgumentException("Start and End dates must be provided");
		}
		
		if (startDate > endDate) {
			throw new IllegalArgumentException("End date must be later than start date");
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(startDate);
		calendar.clear(Calendar.HOUR);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MILLISECOND);
		startDate = calendar.getTimeInMillis();
		
		calendar.setTimeInMillis(endDate);
		calendar.clear(Calendar.HOUR);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MILLISECOND);
		endDate = calendar.getTimeInMillis();
		
		Log.d(TAG, "Start date: " + new Date(startDate));
		Log.d(TAG, "End date: " + new Date(endDate));
		
		for (long currentDay = startDate; currentDay <= endDate; currentDay += DateUtils.DAY_IN_MILLIS) {
			setupDay(inflater, currentDay);
		}
	}
    
    private void setupDay(LayoutInflater inflater, long startMillis) {
        Day day = new Day();

        // Setup data
        day.index = mDays.size();
        day.timeStart = startMillis;
        day.timeEnd = startMillis + DateUtils.DAY_IN_MILLIS;
        day.blocksUri = ScheduleContract.Blocks.buildBlocksBetweenDirUri(
                day.timeStart, day.timeEnd);

        // Setup views
        day.rootView = (ViewGroup) inflater.inflate(R.layout.blocks_content, null);

        day.scrollView = (ObservableScrollView) day.rootView.findViewById(R.id.blocks_scroll);
        day.scrollView.setOnScrollListener(this);

        day.blocksView = (BlocksLayout) day.rootView.findViewById(R.id.blocks);
        day.nowView = day.rootView.findViewById(R.id.blocks_now);

        day.blocksView.setDrawingCacheEnabled(true);
        day.blocksView.setAlwaysDrawnWithCacheEnabled(true);

        TimeZone.setDefault(UIUtils.CONFERENCE_TIME_ZONE);
        day.label = DateUtils.formatDateTime(getActivity(), startMillis, TIME_FLAGS);

        mWorkspace.addView(day.rootView);
        mDays.add(day);
    }
    
    @Override
    public void onResume() {
        super.onResume();

        // Since we build our views manually instead of using an adapter, we
        // need to manually requery every time launched.
        requery();

        // TODO:
//        getActivity().getContentResolver().registerContentObserver(
//                ScheduleContract.Sessions.CONTENT_URI, true, mSessionChangesObserver);

        // Start listening for time updates to adjust "now" bar. TIME_TICK is
        // triggered once per minute, which is how we move the bar over time.
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        // TODO:
        //getActivity().registerReceiver(mReceiver, filter, null, new Handler());
    }
    
    private void requery() {
        for (Day day : mDays) {
        	// TODO:
//            mHandler.startQuery(0, day, day.blocksUri, BlocksQuery.PROJECTION,
//                    null, null, ScheduleContract.Blocks.DEFAULT_SORT);
        }
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                updateNowView(true);
            }
        });
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // TODO:
//        getActivity().unregisterReceiver(mReceiver);
//        getActivity().getContentResolver().unregisterContentObserver(mSessionChangesObserver);
    }
    
    /** {@inheritDoc} */
    public void onClick(View view) {
        if (view instanceof BlockView) {
            String title = ((BlockView)view).getText().toString();

            final String blockId = ((BlockView) view).getBlockId();
            final Uri sessionsUri = ScheduleContract.Blocks.buildSessionsUri(blockId);

            // TODO:
//            final Intent intent = new Intent(Intent.ACTION_VIEW, sessionsUri);
//            intent.putExtra(SessionsFragment.EXTRA_SCHEDULE_TIME_STRING,
//                    ((BlockView) view).getBlockTimeString());
//            ((BaseActivity) getActivity()).openActivityOrFragment(intent);
        }
    }
    
    /**
     * Update position and visibility of "now" view.
     */
    private boolean updateNowView(boolean forceScroll) {
        final long now = UIUtils.getCurrentTime(getActivity());

        Day nowDay = null; // effectively Day corresponding to today
        for (Day day : mDays) {
            if (now >= day.timeStart && now <= day.timeEnd) {
                nowDay = day;
                day.nowView.setVisibility(View.VISIBLE);
            } else {
                day.nowView.setVisibility(View.GONE);
            }
        }

        if (nowDay != null && forceScroll) {
            // Scroll to show "now" in center
            mWorkspace.setCurrentScreen(nowDay.index);
            final int offset = nowDay.scrollView.getHeight() / 2;
            nowDay.nowView.requestRectangleOnScreen(new Rect(0, offset, 0, offset), true);
            nowDay.blocksView.requestLayout();
            return true;
        }

        return false;
    }
    
    public void onScrollChanged(ObservableScrollView view) {
        // Keep each day view at the same vertical scroll offset.
        final int scrollY = view.getScrollY();
        for (Day day : mDays) {
            if (day.scrollView != view) {
                day.scrollView.scrollTo(0, scrollY);
            }
        }
    }
}
