package com.live.holyquranmp3.quran;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        // Get the saved language from SharedPreferences
        SaveSettings sv = new SaveSettings(newBase);
        sv.LoadData();
        String langCode = getLangCode(sv.LanguageSelect);

        // Create a new context with the desired locale
        Locale newLocale = new Locale(langCode);
        Context context = updateResources(newBase, newLocale);
        super.attachBaseContext(context);
    }

    private static Context updateResources(Context context, Locale locale) {
        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }

    private String getLangCode(int languageSelect) {
        switch (languageSelect) {
            case 1:
                return "ar";
            case 2:
                return "ro";
            case 3:
                return "es";
            case 4:
                return "fr";
            case 5:
                return "de";
            case 6:
                return "ru";
            case 7:
                return "it";
            default:
                return "en";
        }
    }
}
