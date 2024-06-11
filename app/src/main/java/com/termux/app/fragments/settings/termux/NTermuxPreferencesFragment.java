package com.termux.app.fragments.settings.termux;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.termux.R;
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;

@Keep
public class NTermuxPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null) return;

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setPreferenceDataStore(NTermuxPreferencesDataStore.getInstance(context));

        setPreferencesFromResource(R.xml.ntermux_preferences, rootKey);
    }

}

class NTermuxPreferencesDataStore extends PreferenceDataStore {

    private final Context mContext;
    private final TermuxAppSharedPreferences mPreferences;

    private static NTermuxPreferencesDataStore mInstance;

    private NTermuxPreferencesDataStore(Context context) {
        mContext = context;
        mPreferences = TermuxAppSharedPreferences.build(context, true);
    }

    public static synchronized NTermuxPreferencesDataStore getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NTermuxPreferencesDataStore(context);
        }
        return mInstance;
    }



    @Override
    public void putBoolean(String key, boolean value) {
        if (mPreferences == null) return;
        if (key == null) return;

        switch (key) {
            case "background":
                    mPreferences.setBackgroundEnabled(value);
                break;
            case "tapi":
                mPreferences.setTapiEnabled(value);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        if (mPreferences == null) return false;

        switch (key) {
            case "background":
                return mPreferences.isBackgroundEnabled();
            case "tapi":
                return mPreferences.isTapiEnabled();
            default:
                return false;
        }
    }

}
