package ca.alina.to_dolist;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;

/**
 * Created by Alina on 8/7/2017.
 */
public class StateMachine {
    // SharedPrefs file for checking initializations
    // NOT backed up
    private static final String STATE_PREF_FILE = "initialstate";
    private static final String PREF_STATE_INT_KEY = "state";

    // keys in default sharedprefs
    private static final String PREF_VERSION_INT_KEY = "versionCode";
    private static final String PREF_BACKUP_TYPE_INT_KEY = "backupType";
    private static final String PREF_BACKUP_TIMESTAMP_LONG_KEY = "backupTime";

    public static final int BACKUP_NONE = 0;
    public static final int BACKUP_DROPBOX = 1;

    static final int READY = 100;
    static final int UNKNOWN = 0;
    static final int FIRST_INSTALL = 2;
    static final int REINSTALL = 3;
    static final int DB_RESTORE_NEEDED = -1;

    private int mState;
    private SharedPreferences stateSharedPrefs;
    private SharedPreferences defaultSharedPrefs;
    private BackupManager backupManager;

    public StateMachine(final Context context) {
        stateSharedPrefs = context.getSharedPreferences(STATE_PREF_FILE, Context.MODE_PRIVATE);
        defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        backupManager = new BackupManager(context);

        // check state from sharedPrefs
        int state = stateSharedPrefs.getInt(PREF_STATE_INT_KEY, UNKNOWN);
        setState(state);
    }

    public void setBackupType(int backupType, Date backupDate) {
        Log.e("StateMachine", String.format("Set backup type: %d, date: TODO", backupType));
        SharedPreferences.Editor edit = defaultSharedPrefs.edit();
        edit.putInt(PREF_BACKUP_TYPE_INT_KEY, backupType);
        if (backupDate != null) {
            edit.putLong(PREF_BACKUP_TIMESTAMP_LONG_KEY, backupDate.getTime());
        }
        edit.apply();

        backupManager.dataChanged();
    }

    public int getBackupType() {
        return defaultSharedPrefs.getInt(PREF_BACKUP_TYPE_INT_KEY, BACKUP_NONE);
    }

    public Date getBackupDate() {
        long l = defaultSharedPrefs.getLong(PREF_BACKUP_TIMESTAMP_LONG_KEY, 0);
        return new Date(l);
    }

    private void setState(int state) {
        mState = state;
        if (BuildConfig.DEBUG)
            Log.e("StateMachine", "state=" + String.valueOf(mState));
    }

    /**
     * Check app state (e.g. first install, reinstall, ready) and perform appropriate initial tasks.
     *
     * @return The state this object stopped at.
     */
    public int run() {
        Log.e("StateMachine", "start");
        // check if app is on first install, reinstalled, etc.
        switch (mState) {
            case READY:
                break;  // do nothing
            case UNKNOWN:
                checkVersionCode();
                break;
            case FIRST_INSTALL:
                noKVBackupFound();
                break;
            case REINSTALL:
                checkDbBackup();
                break;
            case DB_RESTORE_NEEDED:
                // TODO
                break;
            default:
                throw new UnsupportedOperationException(
                        "don't know how to handle this state: " + String.valueOf(mState));
        }

        stateSharedPrefs.edit().putInt(PREF_STATE_INT_KEY, mState).apply();

        return mState;
    }

    private void checkVersionCode() {
        Log.e("StateMachine", "checking version code");
        final int NOT_FOUND = -1;
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // get stored version code
        int savedVersionCode = defaultSharedPrefs.getInt(PREF_VERSION_INT_KEY, NOT_FOUND);

        if (savedVersionCode == NOT_FOUND) {
            setState(FIRST_INSTALL);
            noKVBackupFound();
        }
        else {
            // check if version codes are the same
            if (currentVersionCode != savedVersionCode) {
                Log.e("StateMachine", "current version code is different from saved one");
                // store currentVersionCode in sharedprefs
                defaultSharedPrefs.edit().putInt(PREF_VERSION_INT_KEY, currentVersionCode).apply();
                backupManager.dataChanged();

                // invalidate any existing backups
                setBackupType(BACKUP_NONE, null);
            }
            setState(REINSTALL);
            checkDbBackup();
        }
    }

    private void noKVBackupFound() {
        Log.e("StateMachine", "no key-value backup was found");
        // store currentVersionCode in sharedprefs
        int currentVersionCode = BuildConfig.VERSION_CODE;
        defaultSharedPrefs.edit().putInt(PREF_VERSION_INT_KEY, currentVersionCode).apply();
        backupManager.dataChanged();

        setState(READY);
    }

    private void checkDbBackup() {
        // TODO store/check
        Log.e("StateMachine", "checking for a database backup");
        // check sharedprefs for type of backup
        int backupType = getBackupType();

        switch (backupType) {
            case BACKUP_NONE:
                // no backup exists
                setState(READY);
                break;
            case BACKUP_DROPBOX:
                // request dropbox backup restore
                setState(DB_RESTORE_NEEDED);
                break;
        }
    }
}
