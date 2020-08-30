package com.codewithajit.todoapp.UtilsService;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceClass {
    private static final String USER_PREF = "user_todo";
    private SharedPreferences appSharedPref;
    private SharedPreferences.Editor prefsEditor;

    public SharedPreferenceClass(Context context) {
        this.appSharedPref = context.getSharedPreferences(USER_PREF, Activity.MODE_PRIVATE);
        this.prefsEditor = appSharedPref.edit();
    }

    //int
    public int getValue_int(String intKey) {
        return  appSharedPref.getInt(intKey, 0);
    }

    public void setValue_int(String intKey, int intKeyValue) {
        prefsEditor.putInt(intKey, intKeyValue).commit();
    }


    // string
    public String getValue_string(String stringKey) {
        return  appSharedPref.getString(stringKey, "");
    }

    public void setValue_string(String stringKey, String stringKeyValue) {
        prefsEditor.putString(stringKey, stringKeyValue).commit();
    }


    //boolean
    public boolean getValue_boolean(String booleanKey) {
        return  appSharedPref.getBoolean(booleanKey, false);
    }

    public void setValue_boolean(String booleanKey, boolean booleanKeyValue) {
        prefsEditor.putBoolean(booleanKey, booleanKeyValue).commit();
    }


    public void clearData() {
        prefsEditor.clear().commit();
    }

}
