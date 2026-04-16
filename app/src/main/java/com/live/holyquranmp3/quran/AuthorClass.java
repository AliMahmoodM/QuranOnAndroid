package com.live.holyquranmp3.quran;

/**
 * Created by hussienalrubaye & updated by AliMahmood on 12/26/15.
 */
public class AuthorClass {
    public String RealName;
    public String ServerName;
    public String StateName;
    public String ImgUrl;

    public AuthorClass() {}

    public AuthorClass(String ServerName, String RealName) {
        this.ServerName = ServerName;
        this.RealName = RealName;
    }

    public AuthorClass(String ServerName, String RealName, String StateName, String ImgUrl) {
        this.ServerName = ServerName;
        this.RealName = RealName;
        this.StateName = StateName;
        this.ImgUrl = ImgUrl;
    }
}
