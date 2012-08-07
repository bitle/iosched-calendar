package com.mobeplace.android.ui.calendarview;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import com.google.android.apps.iosched.R;
import com.google.android.apps.iosched.ui.ScheduleFragment;

public class MainActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        getSupportFragmentManager().beginTransaction()
        .add(R.id.root_container, new ScheduleFragment())
        .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
