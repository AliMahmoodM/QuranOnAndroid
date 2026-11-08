package com.live.holyquranmp3.quran;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

/**
 * Created by hussienalrubaye & updated by AliMahmood on 12/17/15.
 */
public class SaveSettings {

    Context context;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs3" ;
    public static int LanguageSelect=0; // Default to English
    public static String ServerURL="http://sellingportal.alruabye.net/";
    public static String  APPURL="com.live.holyquranmp3.quran";
    // public static boolean OnTimeAds=false;
    public static int IsRated=0;//app rate 0 not rate 1 is rate
    public static String LastReciterName = "";
    public static int LastSongIndex = -1;
    public static int LastSongPosition = 0;
    public static String BookmarkReciterName = "";
    public static int BookmarkSongIndex = -1;
    public static int BookmarkSongPosition = 0;
    public static String BookmarkSongTitle = "";
     public SaveSettings(Context context) {
        this.context=context;
        sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


    }
    public void SaveData()  {

        try

        {

            SharedPreferences.Editor editor = sharedpreferences.edit();

            editor.putInt("LanguageSelect", LanguageSelect);
            editor.putInt("IsRated", IsRated);
            editor.putString("LastReciterName", LastReciterName);
            editor.putInt("LastSongIndex", LastSongIndex);
            editor.putInt("LastSongPosition", LastSongPosition);
            editor.putString("BookmarkReciterName", BookmarkReciterName);
            editor.putInt("BookmarkSongIndex", BookmarkSongIndex);
            editor.putInt("BookmarkSongPosition", BookmarkSongPosition);
            editor.putString("BookmarkSongTitle", BookmarkSongTitle);
             editor.commit();
            LoadData( );
        }

        catch( Exception e)

        {

            // Toast.makeText(context, "Unable to write to the SettingFile file.", Toast.LENGTH_LONG).show();
        }
    }
    public   void LoadData( ) {


        LanguageSelect=sharedpreferences.getInt("LanguageSelect", -1);
        if(LanguageSelect==-1) //first time
        {
            String lng= Locale.getDefault().getLanguage();
            if( lng.toLowerCase().equals("ar"))
                LanguageSelect=1;
            else
                LanguageSelect=0; // Default to English
        }

            IsRated=sharedpreferences.getInt("IsRated", 0);
            LastReciterName = sharedpreferences.getString("LastReciterName", "");
            LastSongIndex = sharedpreferences.getInt("LastSongIndex", -1);
            LastSongPosition = sharedpreferences.getInt("LastSongPosition", 0);
            BookmarkReciterName = sharedpreferences.getString("BookmarkReciterName", "");
            BookmarkSongIndex = sharedpreferences.getInt("BookmarkSongIndex", -1);
            BookmarkSongPosition = sharedpreferences.getInt("BookmarkSongPosition", 0);
            BookmarkSongTitle = sharedpreferences.getString("BookmarkSongTitle", "");


    }
}
