package com.live.holyquranmp3.quran;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SongsManager {

    public interface SongsListListener {
        void onSongsListReady(ArrayList<HashMap<String, String>> songsList);
    }

    public void getPlayListAsync(String RecitesName, int languageSelect, SongsListListener listener) {
        LnaguageClass.GuranAyaAsync(RecitesName, languageSelect, surahs -> {
            ArrayList<HashMap<String, String>> songsList = new ArrayList<>();
            for (AuthorClass temp : surahs) {
                HashMap<String, String> song = new HashMap<>();
                song.put("songTitle", temp.RealName);
                song.put("songPath", temp.ImgUrl);
                songsList.add(song);
            }
            listener.onSongsListReady(songsList);
        });
    }
}
