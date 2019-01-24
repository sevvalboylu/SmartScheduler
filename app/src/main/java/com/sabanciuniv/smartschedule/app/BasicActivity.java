package com.sabanciuniv.smartschedule.app;

import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import com.alamkanak.weekview.MonthLoader;
import java.util.List;

public class BasicActivity extends BaseActivity implements MonthLoader.MonthChangeListener  {
   private DrawerLayout mDrawerLayout;
   @Override
   public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
       // Populate the week view with some events.

       Toolbar toolbar = findViewById(R.id.toolbar);
       setSupportActionBar(toolbar);

       ActionBar actbar = getSupportActionBar();
       actbar.setDisplayHomeAsUpEnabled(true);
       actbar.setHomeAsUpIndicator(R.drawable.ic_menu);
       mDrawerLayout = findViewById(R.id.drawer_layout);

       ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
               this, mDrawerLayout, toolbar,
               R.string.navigation_drawer_open,
               R.string.navigation_drawer_close);
       mDrawerLayout.addDrawerListener(toggle);
       toggle.syncState();


       List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();

       Calendar startTime = Calendar.getInstance();
       startTime.set(Calendar.HOUR_OF_DAY, 3);
       startTime.set(Calendar.MINUTE, 0);
       startTime.set(Calendar.MONTH, newMonth - 1);
       startTime.set(Calendar.YEAR, newYear);
       Calendar endTime = (Calendar) startTime.clone();
       endTime.add(Calendar.HOUR, 1);
       endTime.set(Calendar.MONTH, newMonth - 1);
       WeekViewEvent event = new WeekViewEvent(1, getEventTitle(startTime), startTime, endTime);
       event.setColor(getResources().getColor(R.color.event_color_01));
       events.add(event);

       startTime = Calendar.getInstance();
       startTime.set(Calendar.HOUR_OF_DAY, 3);
       startTime.set(Calendar.MINUTE, 30);
       startTime.set(Calendar.MONTH, newMonth-1);
       startTime.set(Calendar.YEAR, newYear);
       endTime = (Calendar) startTime.clone();
       endTime.set(Calendar.HOUR_OF_DAY, 4);
       endTime.set(Calendar.MINUTE, 30);
       endTime.set(Calendar.MONTH, newMonth-1);
       event = new WeekViewEvent(10, getEventTitle(startTime), startTime, endTime);
       event.setColor(getResources().getColor(R.color.event_color_02));
       events.add(event);

       startTime = Calendar.getInstance();
       startTime.set(Calendar.HOUR_OF_DAY, 4);
       startTime.set(Calendar.MINUTE, 20);
       startTime.set(Calendar.MONTH, newMonth-1);
       startTime.set(Calendar.YEAR, newYear);
       endTime = (Calendar) startTime.clone();
       endTime.set(Calendar.HOUR_OF_DAY, 5);
       endTime.set(Calendar.MINUTE, 0);
       event = new WeekViewEvent(10, getEventTitle(startTime), startTime, endTime);
       event.setColor(getResources().getColor(R.color.event_color_03));
       events.add(event);

       startTime = Calendar.getInstance();
       startTime.set(Calendar.HOUR_OF_DAY, 5);
       startTime.set(Calendar.MINUTE, 30);
       startTime.set(Calendar.MONTH, newMonth-1);
       startTime.set(Calendar.YEAR, newYear);
       endTime = (Calendar) startTime.clone();
       endTime.add(Calendar.HOUR_OF_DAY, 2);
       endTime.set(Calendar.MONTH, newMonth-1);
       event = new WeekViewEvent(2, getEventTitle(startTime), startTime, endTime);
       event.setColor(getResources().getColor(R.color.event_color_02));
       events.add(event);

       startTime = Calendar.getInstance();
       startTime.set(Calendar.HOUR_OF_DAY, 5);
       startTime.set(Calendar.MINUTE, 0);
       startTime.set(Calendar.MONTH, newMonth - 1);
       startTime.set(Calendar.YEAR, newYear);
       startTime.add(Calendar.DATE, 1);
       endTime = (Calendar) startTime.clone();
       endTime.add(Calendar.HOUR_OF_DAY, 3);
       endTime.set(Calendar.MONTH, newMonth - 1);
       event = new WeekViewEvent(3, getEventTitle(startTime), startTime, endTime);
       event.setColor(getResources().getColor(R.color.event_color_03));
       events.add(event);

       startTime = Calendar.getInstance();
       startTime.set(Calendar.DAY_OF_MONTH, 15);
       startTime.set(Calendar.HOUR_OF_DAY, 3);
       startTime.set(Calendar.MINUTE, 0);
       startTime.set(Calendar.MONTH, newMonth-1);
       startTime.set(Calendar.YEAR, newYear);
       endTime = (Calendar) startTime.clone();
       endTime.add(Calendar.HOUR_OF_DAY, 3);
       event = new WeekViewEvent(4, getEventTitle(startTime), startTime, endTime);
       event.setColor(getResources().getColor(R.color.event_color_04));
       events.add(event);

       startTime = Calendar.getInstance();
       startTime.set(Calendar.DAY_OF_MONTH, 1);
       startTime.set(Calendar.HOUR_OF_DAY, 3);
       startTime.set(Calendar.MINUTE, 0);
       startTime.set(Calendar.MONTH, newMonth-1);
       startTime.set(Calendar.YEAR, newYear);
       endTime = (Calendar) startTime.clone();
       endTime.add(Calendar.HOUR_OF_DAY, 3);
       event = new WeekViewEvent(5, getEventTitle(startTime), startTime, endTime);
       event.setColor(getResources().getColor(R.color.event_color_01));
       events.add(event);

       startTime = Calendar.getInstance();
       startTime.set(Calendar.DAY_OF_MONTH, startTime.getActualMaximum(Calendar.DAY_OF_MONTH));
       startTime.set(Calendar.HOUR_OF_DAY, 15);
       startTime.set(Calendar.MINUTE, 0);
       startTime.set(Calendar.MONTH, newMonth-1);
       startTime.set(Calendar.YEAR, newYear);
       endTime = (Calendar) startTime.clone();
       endTime.add(Calendar.HOUR_OF_DAY, 3);
       event = new WeekViewEvent(5, getEventTitle(startTime), startTime, endTime);
       event.setColor(getResources().getColor(R.color.event_color_02));
       events.add(event);

       //AllDay event
       startTime = Calendar.getInstance();
       startTime.set(Calendar.HOUR_OF_DAY, 0);
       startTime.set(Calendar.MINUTE, 0);
       startTime.set(Calendar.MONTH, newMonth-1);
       startTime.set(Calendar.YEAR, newYear);
       endTime = (Calendar) startTime.clone();
       endTime.add(Calendar.HOUR_OF_DAY, 23);
       event = new WeekViewEvent(7, getEventTitle(startTime),null, startTime, endTime, true);
       event.setColor(getResources().getColor(R.color.event_color_04));
       events.add(event);
       events.add(event);

       startTime = Calendar.getInstance();
       startTime.set(Calendar.DAY_OF_MONTH, 8);
       startTime.set(Calendar.HOUR_OF_DAY, 2);
       startTime.set(Calendar.MINUTE, 0);
       startTime.set(Calendar.MONTH, newMonth-1);
       startTime.set(Calendar.YEAR, newYear);
       endTime = (Calendar) startTime.clone();
       endTime.set(Calendar.DAY_OF_MONTH, 10);
       endTime.set(Calendar.HOUR_OF_DAY, 23);
       event = new WeekViewEvent(8, getEventTitle(startTime),null, startTime, endTime, true);
       event.setColor(getResources().getColor(R.color.event_color_03));
       events.add(event);

       // All day event until 00:00 next day
       startTime = Calendar.getInstance();
       startTime.set(Calendar.DAY_OF_MONTH, 10);
       startTime.set(Calendar.HOUR_OF_DAY, 0);
       startTime.set(Calendar.MINUTE, 0);
       startTime.set(Calendar.SECOND, 0);
       startTime.set(Calendar.MILLISECOND, 0);
       startTime.set(Calendar.MONTH, newMonth-1);
       startTime.set(Calendar.YEAR, newYear);
       endTime = (Calendar) startTime.clone();
       endTime.set(Calendar.DAY_OF_MONTH, 11);
       event = new WeekViewEvent(8, getEventTitle(startTime), null, startTime, endTime, true);
       event.setColor(getResources().getColor(R.color.event_color_01));
       events.add(event);

       return events;
   }


}


