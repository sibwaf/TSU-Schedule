package ru.dyatel.tsuschedule;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import ru.dyatel.tsuschedule.events.EventBus;
import ru.dyatel.tsuschedule.fragments.MainFragment;
import ru.dyatel.tsuschedule.layout.NavigationDrawerHandler;

public class MainActivity extends AppCompatActivity {

	private NavigationHandler navigationHandler;
	private NavigationDrawerHandler drawerHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity);

		EventBus eventBus = ((ScheduleApplication) getApplication()).getEventBus();

		FragmentManager fragmentManager = getSupportFragmentManager();

		// Replace ActionBar with Toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeButtonEnabled(true);
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerHandler = new NavigationDrawerHandler(this, drawer, eventBus);

		navigationHandler = new NavigationHandler(fragmentManager, drawerHandler);
		fragmentManager.addOnBackStackChangedListener(navigationHandler);

		drawerHandler.initMenu(navigationHandler);

		fragmentManager.beginTransaction()
				.replace(R.id.content_fragment, new MainFragment())
				.commit();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerHandler.syncState();
	}

	@Override
	public void onBackPressed() {
		if (!navigationHandler.onBackPressed()) super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return navigationHandler.onOptionsItemSelected(item)
				|| super.onOptionsItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerHandler.onConfigurationChanged(newConfig);
	}

}
