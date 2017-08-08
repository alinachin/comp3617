package ca.alina.to_dolist;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Alina on 8/7/2017.
 */
public class StateMachine {
    // SharedPrefs file for checking initializations
    public static final String STATE_PREF_FILE = "initialstate";
    private static final String PREF_VERSION_INT_KEY = "versionCode";
    private static final String PREF_STATE_INT_KEY = "state";
    private static final String PREF_BACKUP_TYPE_INT_KEY = "backupType";
    public static final int BACKUP_NONE = 0;
    public static final int BACKUP_DROPBOX = 1;

    static final int READY = 100;
    static final int UNKNOWN = 0;
    static final int FIRST_INSTALL = 2;
    static final int REINSTALL = 3;
    static final int DB_RESTORE_NEEDED = -1;

    private int mState;
    private SharedPreferences sharedPrefs;
    private BackupManager backupManager;

    public StateMachine(final Context context) {
        sharedPrefs = context.getSharedPreferences(STATE_PREF_FILE, Context.MODE_PRIVATE);
        backupManager = new BackupManager(context);

        // check state from sharedPrefs
        int state = sharedPrefs.getInt(PREF_STATE_INT_KEY, UNKNOWN);
        setState(state);
    }

    public void setBackupType(int backupType) {
        sharedPrefs.edit().putInt(PREF_BACKUP_TYPE_INT_KEY, backupType).apply();
        backupManager.dataChanged();
    }

    public int getBackupType() {
        return sharedPrefs.getInt(PREF_BACKUP_TYPE_INT_KEY, BACKUP_NONE);
    }

    private void setState(int state) {
        mState = state;
        if (BuildConfig.DEBUG)
            Log.e("StateMachine", "StateMachine: state=" + String.valueOf(mState));
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

        sharedPrefs.edit().putInt(PREF_STATE_INT_KEY, mState).apply();
        backupManager.dataChanged();

        return mState;
    }

    private void checkVersionCode() {
        Log.e("StateMachine", "checking version code");
        final int NOT_FOUND = -1;
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // get stored version code
        int savedVersionCode = sharedPrefs.getInt(PREF_VERSION_INT_KEY, NOT_FOUND);

        if (savedVersionCode == NOT_FOUND) {
            setState(FIRST_INSTALL);
            noKVBackupFound();
        }
        else {
            // check if version codes are the same
            if (currentVersionCode != savedVersionCode) {
                Log.e("StateMachine", "current version code is different from saved one");
                // store currentVersionCode in sharedprefs
                sharedPrefs.edit().putInt(PREF_VERSION_INT_KEY, currentVersionCode).apply();

                // invalidate any existing backups
                setBackupType(BACKUP_NONE);
            }
            setState(REINSTALL);
            checkDbBackup();
        }
    }

    private void noKVBackupFound() {
        Log.e("StateMachine", "no key-value backup was found");
        // store currentVersionCode in sharedprefs
        int currentVersionCode = BuildConfig.VERSION_CODE;
        sharedPrefs.edit().putInt(PREF_VERSION_INT_KEY, currentVersionCode).apply();

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
