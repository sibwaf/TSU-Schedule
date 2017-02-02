package ru.dyatel.tsuschedule;

import android.app.Application;
import android.preference.PreferenceManager;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import ru.dyatel.tsuschedule.data.DatabaseManager;
import ru.dyatel.tsuschedule.events.EventBus;

@ReportsCrashes(formUri = BuildConfig.ACRA_BACKEND)
public class ScheduleApplication extends Application {

	private EventBus eventBus = new EventBus();
	private DatabaseManager databaseManager = new DatabaseManager(this, eventBus);

	@Override
	public void onCreate() {
		super.onCreate();

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		if (!BuildConfig.DISABLE_ACRA) ACRA.init(this);

		ParityReference.init(this);
	}

	public EventBus getEventBus() {
		return eventBus;
	}

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

}
