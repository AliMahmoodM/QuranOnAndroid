package com.live.holyquranmp3.quran;

import android.app.Application;
import android.media.MediaPlayer;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class PlayerViewModel extends AndroidViewModel {

    private static final String TAG = "PlayerViewModel";

    private final MutableLiveData<MediaPlayer> _mediaPlayer = new MutableLiveData<>();
    public final LiveData<MediaPlayer> mediaPlayer = _mediaPlayer;

    private final MutableLiveData<List<HashMap<String, String>>> _songsList = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<HashMap<String, String>>> songsList = _songsList;

    private final MutableLiveData<Integer> _currentSongIndex = new MutableLiveData<>(-1);
    public final LiveData<Integer> currentSongIndex = _currentSongIndex;

    private final MutableLiveData<Boolean> _isShuffle = new MutableLiveData<>(false);
    public final LiveData<Boolean> isShuffle = _isShuffle;

    private final MutableLiveData<Boolean> _isRepeat = new MutableLiveData<>(false);
    public final LiveData<Boolean> isRepeat = _isRepeat;

    private boolean isInitialized = false;

    public PlayerViewModel(@NonNull Application application) {
        super(application);
    }

    public void init(String recitesName, int songIndex) {
        if (isInitialized) return;
        isInitialized = true;

        SongsManager songsManager = new SongsManager();
        SaveSettings sv = new SaveSettings(getApplication());
        sv.LoadData();
        
        songsManager.getPlayListAsync(recitesName, sv.LanguageSelect, songs -> {
            _songsList.setValue(songs);
            _currentSongIndex.setValue(songIndex);
            _mediaPlayer.setValue(new MediaPlayer());
            MediaPlayer player = mediaPlayer.getValue();
            if (player != null) {
                player.setOnCompletionListener(mp -> {
                    if (Boolean.TRUE.equals(_isRepeat.getValue())) {
                        Integer currentSongIndex = _currentSongIndex.getValue();
                        if (currentSongIndex != null) {
                            playSong(currentSongIndex);
                        }
                    } else if (Boolean.TRUE.equals(_isShuffle.getValue())) {
                        List<HashMap<String, String>> list = _songsList.getValue();
                        if (list != null && !list.isEmpty()) {
                            Random rand = new Random();
                            int randomIndex = rand.nextInt(list.size());
                            playSong(randomIndex);
                        }
                    } else {
                        nextSong();
                    }
                });
            }
        });
    }

    public void playSong(int songIndex) {
        MediaPlayer player = mediaPlayer.getValue();
        List<HashMap<String, String>> list = _songsList.getValue();
        if (player != null && list != null && songIndex >= 0 && songIndex < list.size()) {
            try {
                if(player.isPlaying()){
                    player.stop();
                }
                _currentSongIndex.setValue(songIndex);
                player.reset();
                player.setDataSource(list.get(songIndex).get("songPath"));
                player.prepare();
                player.start();
            } catch (IOException e) {
                Log.e(TAG, "Error playing song", e);
            }
        }
    }

    public void playPause() {
        MediaPlayer player = mediaPlayer.getValue();
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.start();
            }
        }
    }

    public void nextSong() {
        Integer currentIndex = _currentSongIndex.getValue();
        List<HashMap<String, String>> list = _songsList.getValue();
        if (currentIndex != null && list != null && !list.isEmpty()) {
            int nextIndex = currentIndex + 1;
            if (nextIndex < list.size()) {
                playSong(nextIndex);
            } else {
                playSong(0);
            }
        }
    }

    public void previousSong() {
        Integer currentIndex = _currentSongIndex.getValue();
        List<HashMap<String, String>> list = _songsList.getValue();
        if (currentIndex != null && list != null && !list.isEmpty()) {
            int prevIndex = currentIndex - 1;
            if (prevIndex >= 0) {
                playSong(prevIndex);
            } else {
                playSong(list.size() - 1);
            }
        }
    }

    public void toggleRepeat() {
        boolean currentRepeat = Boolean.TRUE.equals(_isRepeat.getValue());
        _isRepeat.setValue(!currentRepeat);
        if (!currentRepeat) { // if it's now true
            _isShuffle.setValue(false);
        }
    }

    public void toggleShuffle() {
        boolean currentShuffle = Boolean.TRUE.equals(_isShuffle.getValue());
        _isShuffle.setValue(!currentShuffle);
        if (!currentShuffle) { // if it's now true
            _isRepeat.setValue(false);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        MediaPlayer player = mediaPlayer.getValue();
        if (player != null) {
            player.release();
        }
    }
}
