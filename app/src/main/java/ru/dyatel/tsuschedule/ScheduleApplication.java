package ru.dyatel.tsuschedule;

import android.app.Application;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(
        formUri = BuildConfig.ACRA_BACKEND
)
public class ScheduleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize ACRA
        ACRA.init(this);

        // Initialize data in reference classes
        ParityReference.init(this);
    }

}
