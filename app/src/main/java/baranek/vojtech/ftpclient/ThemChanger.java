package baranek.vojtech.ftpclient;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;

import com.pixplicity.easyprefs.library.Prefs;

/**
 * Created by Farmas on 29.10.2015.
 * support class for theme changes
 */
public class ThemChanger {

    private static int sTheme = 3;
    public final static int THEME_GREEN = 0;
    public final static int THEME_PINK = 1;


    public static void changeToTheme(Activity ac, int theme) {

        new Prefs.Builder()
                .setContext(ac.getApplicationContext())
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(ac.getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();


        sTheme = theme;
        Prefs.putInt("DEFTHEME", theme);
        ac.finish();
        ac.startActivity(new Intent(ac, ac.getClass()));
    }

    public static void onActivityCreateSetTheme(Activity ac) {
        new Prefs.Builder()
                .setContext(ac.getApplicationContext())
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(ac.getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

        if (sTheme == 3)
            sTheme = Prefs.getInt("DEFTHEME", 0);

        switch (sTheme) {
            default:
            case THEME_GREEN:
                ac.setTheme(R.style.AppThemeBlue);

                break;
            case THEME_PINK:
                ac.setTheme(R.style.AppTheme);
                break;

        }


    }


}
