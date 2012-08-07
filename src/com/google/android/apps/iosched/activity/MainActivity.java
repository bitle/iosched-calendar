package com.google.android.apps.iosched.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import com.google.android.apps.iosched.R;
import com.google.android.apps.iosched.ui.ScheduleFragment;
import com.google.android.apps.iosched.util.ParserUtils;

public class MainActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        long startDate = ParserUtils.parseTime("2011-05-10T00:00:00.000-07:00");
        long endDate = ParserUtils.parseTime("2011-05-11T00:00:00.000-07:00");
        
        Fragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();
        args.putLong(ScheduleFragment.START_DATE, startDate);
        args.putLong(ScheduleFragment.END_DATE, endDate);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
	        .add(R.id.root_container, fragment)
	        .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
