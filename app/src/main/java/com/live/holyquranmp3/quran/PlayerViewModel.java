package com.live.holyquranmp3.quran;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.PowerManager;
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
    private String currentRecitesName = "";
    private int resumePosition = 0;
    private int lastKnownPosition = 0;
    private PowerManager.WakeLock wakeLock;

    public PlayerViewModel(@NonNull Application application) {
        super(application);
        PowerManager powerManager = (PowerManager) application.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "QuranOnAndroid::PlaybackWakeLock");
            wakeLock.setReferenceCounted(false);
        }
    }

    public void updateCurrentPosition(int pos) {
        if (pos > 0) {
            this.lastKnownPosition = pos;
        }
    }

    public void init(String recitesName, int initialSongIndex) {
        if (isInitialized) return;
        isInitialized = true;
        this.currentRecitesName = recitesName;

        SaveSettings sv = new SaveSettings(getApplication());
        sv.LoadData();

        int targetSongIndex = initialSongIndex;
        if (recitesName.equals(SaveSettings.LastReciterName) && SaveSettings.LastSongIndex != -1) {
            if (initialSongIndex == SaveSettings.LastSongIndex) {
                resumePosition = SaveSettings.LastSongPosition;
                lastKnownPosition = SaveSettings.LastSongPosition;
            } else {
                resumePosition = 0;
                lastKnownPosition = 0;
            }
        } else {
            resumePosition = 0;
            lastKnownPosition = 0;
        }

        if (targetSongIndex < 0) {
            targetSongIndex = SaveSettings.LastSongIndex != -1 ? SaveSettings.LastSongIndex : 0;
        }

        final int finalSongIndex = targetSongIndex;

        SongsManager songsManager = new SongsManager();
        songsManager.getPlayListAsync(recitesName, SaveSettings.LanguageSelect, songs -> {
            _songsList.setValue(songs);
            _currentSongIndex.setValue(finalSongIndex);
            MediaPlayer player = new MediaPlayer();
            player.setWakeMode(getApplication().getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            _mediaPlayer.setValue(player);

            player.setOnCompletionListener(mp -> {
                releaseWakeLock();
                stopForegroundService();
                resumePosition = 0;
                lastKnownPosition = 0;
                SaveSettings s = new SaveSettings(getApplication());
                s.LoadData();
                SaveSettings.LastSongPosition = 0;
                s.SaveData();

                if (Boolean.TRUE.equals(_isRepeat.getValue())) {
                    Integer currentIdx = _currentSongIndex.getValue();
                    if (currentIdx != null) {
                        playSong(currentIdx);
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
        });
    }

    public void playSong(int songIndex) {
        MediaPlayer player = mediaPlayer.getValue();
        List<HashMap<String, String>> list = _songsList.getValue();
        if (player != null && list != null && songIndex >= 0 && songIndex < list.size()) {
            try {
                if (player.isPlaying()) {
                    player.stop();
                }
                releaseWakeLock();
                stopForegroundService();
                _currentSongIndex.setValue(songIndex);
                player.reset();
                player.setDataSource(list.get(songIndex).get("songPath"));
                player.prepare();

                if (resumePosition > 0 && songIndex == getTargetResumeSongIndex()) {
                    player.seekTo(resumePosition);
                    resumePosition = 0; // consume once
                }

                player.start();
                acquireWakeLock();
                startForegroundService();

                saveState(songIndex, player.getCurrentPosition());
            } catch (IOException e) {
                Log.e(TAG, "Error playing song", e);
            }
        }
    }

    private int getTargetResumeSongIndex() {
        SaveSettings sv = new SaveSettings(getApplication());
        sv.LoadData();
        return SaveSettings.LastSongIndex;
    }

    public void playPause() {
        MediaPlayer player = mediaPlayer.getValue();
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
                releaseWakeLock();
                stopForegroundService();
            } else {
                player.start();
                acquireWakeLock();
                startForegroundService();
            }
            saveCurrentState();
        }
    }

    public void nextSong() {
        resumePosition = 0;
        lastKnownPosition = 0;
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
        resumePosition = 0;
        lastKnownPosition = 0;
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
        if (!currentRepeat) {
            _isShuffle.setValue(false);
        }
    }

    public void toggleShuffle() {
        boolean currentShuffle = Boolean.TRUE.equals(_isShuffle.getValue());
        _isShuffle.setValue(!currentShuffle);
        if (!currentShuffle) {
            _isRepeat.setValue(false);
        }
    }

    private void acquireWakeLock() {
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire(4 * 60 * 60 * 1000L); // 4 hours timeout max
        }
    }

    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private void startForegroundService() {
        try {
            Intent serviceIntent = new Intent(getApplication(), QuranAudioService.class);
            String currentTitle = "Quran Recitation";
            List<HashMap<String, String>> list = _songsList.getValue();
            Integer songIndex = _currentSongIndex.getValue();
            if (list != null && songIndex != null && songIndex >= 0 && songIndex < list.size()) {
                String t = list.get(songIndex).get("songTitle");
                if (t != null) currentTitle = t;
            }
            serviceIntent.putExtra("title", currentTitle);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplication().startForegroundService(serviceIntent);
            } else {
                getApplication().startService(serviceIntent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting foreground service", e);
        }
    }

    private void stopForegroundService() {
        try {
            Intent serviceIntent = new Intent(getApplication(), QuranAudioService.class);
            getApplication().stopService(serviceIntent);
        } catch (Exception e) {
            Log.e(TAG, "Error stopping foreground service", e);
        }
    }

    public void saveCurrentState() {
        MediaPlayer player = mediaPlayer.getValue();
        Integer songIndex = _currentSongIndex.getValue();
        if (songIndex != null && songIndex != -1) {
            try {
                int currentPos = lastKnownPosition;
                if (player != null) {
                    try {
                        int p = player.getCurrentPosition();
                        if (p > 0) {
                            currentPos = p;
                            lastKnownPosition = p;
                        }
                    } catch (Exception ignored) {}
                }
                if (currentPos > 0) {
                    saveState(songIndex, currentPos);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving state", e);
            }
        }
    }

    private void saveState(int songIndex, int position) {
        SaveSettings sv = new SaveSettings(getApplication());
        sv.LoadData();
        if (currentRecitesName != null && !currentRecitesName.isEmpty()) {
            SaveSettings.LastReciterName = currentRecitesName;
        }
        SaveSettings.LastSongIndex = songIndex;
        SaveSettings.LastSongPosition = position;
        sv.SaveData();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        saveCurrentState();
        releaseWakeLock();
        stopForegroundService();
        MediaPlayer player = mediaPlayer.getValue();
        if (player != null) {
            player.release();
        }
    }
}
