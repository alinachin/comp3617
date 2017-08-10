package ca.alina.to_dolist;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

/**
 * Created by Alina on 8/7/2017.
 */

public class MyBackupHelper extends BackupAgentHelper {
    static final String myPrefs = "myprefs";

    @Override
    public void onCreate() {
        String defaultPrefsName = getPackageName() + "_preferences";

        SharedPreferencesBackupHelper backupHelper = new SharedPreferencesBackupHelper(
                this,
                defaultPrefsName
        );
        addHelper(myPrefs, backupHelper);
    }
}
