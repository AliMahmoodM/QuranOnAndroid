package com.live.holyquranmp3.quran;

/**
 * Created by hussienalrubaye & updated by AliMahmood on 12/26/15.
 */
public class AuthorClass {
    public String RealName;
    public String ServerName;
    public String StateName;
    public String ImgUrl;
    public String AudioUrlPrefix;
    public String[] AvailableSurahNumbers = new String[0];

    public AuthorClass() {}

    public AuthorClass(String ServerName, String RealName) {
        this.ServerName = ServerName;
        this.RealName = RealName;
    }

    public AuthorClass(String ServerName, String RealName, String AudioUrlPrefix,
                       String... AvailableSurahNumbers) {
        this.ServerName = ServerName;
        this.RealName = RealName;
        this.AudioUrlPrefix = AudioUrlPrefix;
        this.AvailableSurahNumbers = AvailableSurahNumbers == null
                ? new String[0] : AvailableSurahNumbers.clone();
    }

    public AuthorClass(String ServerName, String RealName, String StateName, String ImgUrl) {
        this.ServerName = ServerName;
        this.RealName = RealName;
        this.StateName = StateName;
        this.ImgUrl = ImgUrl;
    }
}
