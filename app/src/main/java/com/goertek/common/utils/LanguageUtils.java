package com.goertek.common.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class LanguageUtils {
    private static final String TAG = "LanguageUtils";
    private static final String KEY_LANGUAGE = "KEY_LANGUAGE";
    private static final String KEY_COUNTRY = "KEY_COUNTRY";
    /** 使用内存缓存，减少IO操作 */
    private static Locale sCurrentLocale;

    public static Context attachBaseContext(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = getUserLocale();
        configuration.setLocale(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 8.0需要使用createConfigurationContext处理
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
            return context.createConfigurationContext(configuration);
        } else {
            resources.updateConfiguration(configuration, null);
            return context;
        }
    }

    public static synchronized void setUserLocale(Locale locale) {
        if (sCurrentLocale == null || !sCurrentLocale.toString().equals(locale.toString())) {
            LogUtils_goertek.d(TAG, "setUserLocale to SP " + locale.toString());
            sCurrentLocale = locale;
            SharedPreferenceUtils.put(KEY_LANGUAGE, locale.getLanguage());
            SharedPreferenceUtils.put(KEY_COUNTRY, locale.getCountry());
        }
    }

    public static synchronized Locale getUserLocale() {
        if (sCurrentLocale == null) {
            String language = SharedPreferenceUtils.get(KEY_LANGUAGE, Locale.US.getLanguage());
            String country = SharedPreferenceUtils.get(KEY_COUNTRY, Locale.US.getCountry());
            sCurrentLocale = new Locale(language, country);
            LogUtils_goertek.d(TAG, "getUserLocale from SP " + sCurrentLocale.toString());
        }
        return sCurrentLocale;
    }
}
