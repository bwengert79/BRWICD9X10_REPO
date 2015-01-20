package com.brwsoftware.brwicd9x10;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

public class ICDActivity extends ActionBarActivity implements
		ActionBar.TabListener, ActionBar.OnNavigationListener {

	private ICDFragment[] mFragments = new ICDFragment[2];
	
	private ICDFragment getFragment(int index) {
		if (mFragments[index] == null) {
			switch (index) {
			case 0:
				mFragments[index] = new ICD9Fragment();
				break;
			case 1:
				mFragments[index] = new ICD10Fragment();
				break;
			}
		}

		return mFragments[index];
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		Intent intent = getIntent();
		int icdType = intent.getIntExtra(MainActivity.EXTRA_ICDTYPE, 0);
		
		//ActionBar setup
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(ArrayAdapter.createFromResource(
				this, R.array.icd_nav_spinner,
				R.layout.icd_nav_spinner_item), this);//support_simple_spinner_dropdown_item
		actionBar.setSelectedNavigationItem(icdType);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			ICDFragment f = (ICDFragment)getSupportFragmentManager().findFragmentById(android.R.id.content);
			f.onNewSearch(intent.getStringExtra(SearchManager.QUERY));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.icd_menu, menu);	    
	    
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_search) {
			onSearchRequested();
			return true;
		} else if (itemId == R.id.action_help) {
			startActivity(new Intent(getApplicationContext(), HelpActivity.class));
			return true;
		} else if (itemId == R.id.action_settings) {
			startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		getSupportFragmentManager().beginTransaction()
				.replace(android.R.id.content, getFragment(itemPosition))
				.commit();
		return false;
	}
}
