package com.termux.app.fragments.settings.termux;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;


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
        SwitchPreferenceCompat authPref = findPreference("auth");
        if (authPref != null) {
            authPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isChecked = (boolean) newValue;
                    
                    Executor executor = ContextCompat.getMainExecutor(context);
                    BiometricPrompt biometricPrompt = new BiometricPrompt(NTermuxPreferencesFragment.this,
                            executor, new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode, CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            Toast.makeText(context, errString, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            authPref.setChecked(isChecked);
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            Toast.makeText(context, "认证失败！", Toast.LENGTH_SHORT).show();
                        }
                    });

                    BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("身份验证")
                        .setSubtitle("当前操作需要验证您的身份")
                        .setNegativeButtonText("取消")
                        .build();
                    biometricPrompt.authenticate(promptInfo);
    
                    return false;
                }
            });
        }
        Preference processPref = findPreference("process");
        if (processPref != null) {
            processPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Process process = null;
                    try {
                        process = Runtime.getRuntime().exec("su");
                        process.getOutputStream().write(("device_config set_sync_disabled_for_tests persistent&&device_config put activity_manager max_phantom_processes 2147483647\n").getBytes());
                        process.getOutputStream().flush();
                        process.getOutputStream().close();
                        Toast.makeText(context, process.waitFor() == 0?"解除成功！":"解除失败！", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(context, "执行失败！", Toast.LENGTH_SHORT).show();
                    } finally {
                        if (process != null) {
                            process.destroy();
                        }
                    }
                    return true;
                }
            });
       }
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
            case "auth":
                mPreferences.setAuthEnabled(value);
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
            case "auth":
                return mPreferences.isAuthEnabled();
            default:
                return false;
        }
    }

}
