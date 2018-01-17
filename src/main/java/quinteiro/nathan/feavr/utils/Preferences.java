package quinteiro.nathan.feavr.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by nathan on 28.06.17.
 */

public class Preferences {

    public static final String preferencesFileKey = "feavr.preferences.file";
    public static final String preferenceLastBLEDeviceKey = "feavr.last.ble.device";
    public static final String preferencesUserEmailKey = "feavr.preferences.user.email";
    public static final String preferencesUserPasswordKey = "feavr.preferences.user.password";
    public static final String preferencesUserTokenKey = "feavr.preferences.user.token";
    public static final String preferencesLastGameReference ="feavr.preferences.last.game.ref";


    public static void saveLastGameReference(String refGame, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(preferencesLastGameReference, refGame);
        editor.commit();
    }

    public static String getLastGameReference(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);

        return sharedPref.getString(preferencesLastGameReference, null);

    }

    public static void saveLastDevice(String address, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(preferenceLastBLEDeviceKey, address);
        editor.commit();
    }

    public static void forgetLastDevice(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(preferenceLastBLEDeviceKey);
        editor.commit();
    }

    public static String getLastDevice(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);

        return sharedPref.getString(preferenceLastBLEDeviceKey, null);
    }


    public static void saveUserEmail(String email, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(preferencesUserEmailKey, email);
        editor.commit();
    }

    public static String getUserEmail(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);

        return sharedPref.getString(preferencesUserEmailKey, null);
    }

    public static void forgetUserEmail(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(preferencesUserEmailKey);
        editor.commit();
    }

    public static void saveUserPassword(String password, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(preferencesUserPasswordKey, password);
        editor.commit();
    }

    public static String getUserPassword(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);

        return sharedPref.getString(preferencesUserPasswordKey, null);
    }

    public static void forgetUserPassword(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(preferencesUserPasswordKey);
        editor.commit();
    }

    public static void saveUserToken(String token, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(preferencesUserTokenKey, token);
        editor.commit();
    }

    public static String getUserToken(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);

        return sharedPref.getString(preferencesUserTokenKey, null);
    }

    public static void forgetUserToken(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(preferencesUserTokenKey);
        editor.commit();
    }


}
