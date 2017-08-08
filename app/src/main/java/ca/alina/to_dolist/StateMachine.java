package ca.alina.to_dolist;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Alina on 8/7/2017.
 */
public class StateMachine {
    // SharedPrefs file for checking initializations
    private static final String STATE_PREF_FILE = "initialstate";
    private static final String PREF_VERSION_INT_KEY = "versionCode";
    private static final String PREF_STATE_INT_KEY = "state";
    private static final String PREF_BACKUP_TYPE_INT_KEY = "backupType";
    private static final int BACKUP_NONE = 0;
    private static final int BACKUP_DROPBOX = 1;

    static final int READY = 100;
    static final int UNKNOWN = 0;
    static final int FIRST_RUN = 1;
    static final int FIRST_INSTALL = 2;
    static final int REINSTALL = 3;
    static final int KV_RESTORE_NEEDED = -1;
    static final int DB_RESTORE_NEEDED = -2;

    private int mState;
    private SharedPreferences sharedPrefs;

    public StateMachine(final Context context) {
        sharedPrefs = context.getSharedPreferences(STATE_PREF_FILE, Context.MODE_PRIVATE);
        // check state from sharedPrefs
        int state = sharedPrefs.getInt(PREF_STATE_INT_KEY, UNKNOWN);
        setState(state);
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
            case FIRST_RUN:
                restoreKVBackup();
                break;
            case FIRST_INSTALL:
                noKVBackupFound();
                break;
            case REINSTALL:
                checkDbBackup();
                break;
            default:
                throw new UnsupportedOperationException(
                        "don't know how to handle this state: " + String.valueOf(mState));
        }

        sharedPrefs.edit().putInt(PREF_STATE_INT_KEY, mState).apply();

        return mState;
    }

    private void checkVersionCode() {
        Log.e("StateMachine", "checking version code");
        final int NOT_FOUND = -1;
        // get stored version code
        int savedVersionCode = sharedPrefs.getInt(PREF_VERSION_INT_KEY, NOT_FOUND);
        if (savedVersionCode == NOT_FOUND) {
            setState(FIRST_RUN);
            restoreKVBackup();
            return;
        }

        int currentVersionCode = BuildConfig.VERSION_CODE;
        // todo check if current version code?
    }

    private void restoreKVBackup() {
        Log.e("StateMachine", "restoring key-value backup");
        // do restore using Android backup helper classes
        // if no backup
        setState(FIRST_INSTALL);
        noKVBackupFound();

        // else if restore failed
        //setState(KV_RESTORE_NEEDED);
        //return;

        // else if restore succeeded
        //setState(REINSTALL);
        //checkDbBackup();
    }

    private void noKVBackupFound() {
        Log.e("StateMachine", "no key-value backup was found");
        // store currentVersionCode in sharedprefs
        int currentVersionCode = BuildConfig.VERSION_CODE;
        sharedPrefs.edit().putInt(PREF_VERSION_INT_KEY, currentVersionCode).apply();

        setState(READY);
    }

    private void checkDbBackup() {
        Log.e("StateMachine", "checking for a database backup");
        // check sharedprefs for type of backup

        // if none - no backup exists
        //setState(READY);
        //return;

        // else if Dropbox
        //setState(DB_RESTORE_NEEDED);
        //return;
    }

    /**
     * Proceed as if this is a fresh install
     */
    void ignoreKVRestore() {
        Log.e("StateMachine", "chose not to retry key-value restore");
        if (mState != KV_RESTORE_NEEDED) {
            return;
        }
        setState(FIRST_INSTALL);
        noKVBackupFound();
    }
}
