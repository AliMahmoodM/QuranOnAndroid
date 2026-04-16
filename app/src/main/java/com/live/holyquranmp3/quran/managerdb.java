package com.live.holyquranmp3.quran;


import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class managerdb extends BaseActivity implements SeekBar.OnSeekBarChangeListener {
    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private MaterialButton btnDownload;

    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar songProgressBar;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    private LinearLayout layoutLoading;
    private ProgressBar progressBar;

    private PlayerViewModel viewModel;

    // Handler to update UI timer, progress bar etc,.
    private final Handler mHandler = new Handler();
    private Utilities utils;
    private final int seekForwardTime = 5000; // 5000 milliseconds
    private final int seekBackwardTime = 5000; // 5000 milliseconds

    private boolean isDownloading = false;
    private final ExecutorService downloadExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managerdb);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            String recitesName = b.getString("RecitesName");
            int songIndex = 0;
            try {
                songIndex = Integer.parseInt(b.getString("RecitesAYA", "0"));
            } catch (NumberFormatException e) {
                // Handle the case where the string is not a valid integer
            }

            if (recitesName != null) {
                viewModel.init(recitesName, songIndex);
            }
        }


        // All player buttons
        btnPlay = findViewById(R.id.btnPlay);
        btnForward = findViewById(R.id.btnForward);
        btnBackward = findViewById(R.id.btnBackward);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnRepeat = findViewById(R.id.btnRepeat);
        btnShuffle = findViewById(R.id.btnShuffle);
        songProgressBar = findViewById(R.id.songProgressBar);
        songCurrentDurationLabel = findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = findViewById(R.id.songTotalDurationLabel);
        btnDownload = findViewById(R.id.btnDownload);
        layoutLoading = findViewById(R.id.LayoutLoading);
        progressBar = findViewById(R.id.progressBar);

        if (layoutLoading != null) {
            layoutLoading.setVisibility(View.GONE);
        }

        utils = new Utilities();

        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important

        viewModel.mediaPlayer.observe(this, mediaPlayer -> {
            updateProgressBar();
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) { // Only play if the media player is ready and not playing
                Integer songIndex = viewModel.currentSongIndex.getValue();
                if (songIndex != null && songIndex != -1) {
                    viewModel.playSong(songIndex);
                }
            }
        });

        viewModel.currentSongIndex.observe(this, songIndex -> {
            if (songIndex != null && songIndex != -1 && viewModel.songsList.getValue() != null && !viewModel.songsList.getValue().isEmpty() && songIndex < viewModel.songsList.getValue().size()) {
                String songTitle = viewModel.songsList.getValue().get(songIndex).get("songTitle");
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(songTitle);
                }
                btnPlay.setImageResource(R.drawable.btn_pause);
                songProgressBar.setProgress(0);
                songProgressBar.setMax(100);
                updateProgressBar();
            }
        });

        viewModel.isRepeat.observe(this, isRepeat -> {
            if (isRepeat) {
                btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
                btnShuffle.setImageResource(R.drawable.btn_shuffle);
            } else {
                btnRepeat.setImageResource(R.drawable.btn_repeat);
            }
        });

        viewModel.isShuffle.observe(this, isShuffle -> {
            if (isShuffle) {
                btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
                btnRepeat.setImageResource(R.drawable.btn_repeat);
            } else {
                btnShuffle.setImageResource(R.drawable.btn_shuffle);
            }
        });


        btnPlay.setOnClickListener(arg0 -> {
            viewModel.playPause();
            if (viewModel.mediaPlayer.getValue() != null && viewModel.mediaPlayer.getValue().isPlaying()) {
                btnPlay.setImageResource(R.drawable.btn_pause);
            } else {
                btnPlay.setImageResource(R.drawable.btn_play);
            }
        });

        btnForward.setOnClickListener(arg0 -> {
            if (viewModel.mediaPlayer.getValue() != null) {
                int currentPosition = viewModel.mediaPlayer.getValue().getCurrentPosition();
                if (currentPosition + seekForwardTime <= viewModel.mediaPlayer.getValue().getDuration()) {
                    viewModel.mediaPlayer.getValue().seekTo(currentPosition + seekForwardTime);
                } else {
                    viewModel.mediaPlayer.getValue().seekTo(viewModel.mediaPlayer.getValue().getDuration());
                }
            }
        });

        btnBackward.setOnClickListener(arg0 -> {
            if (viewModel.mediaPlayer.getValue() != null) {
                int currentPosition = viewModel.mediaPlayer.getValue().getCurrentPosition();
                if (currentPosition - seekBackwardTime >= 0) {
                    viewModel.mediaPlayer.getValue().seekTo(currentPosition - seekBackwardTime);
                } else {
                    viewModel.mediaPlayer.getValue().seekTo(0);
                }
            }
        });

        btnNext.setOnClickListener(arg0 -> {
            viewModel.nextSong();
        });

        btnPrevious.setOnClickListener(arg0 -> {
            viewModel.previousSong();
        });

        btnRepeat.setOnClickListener(arg0 -> {
            viewModel.toggleRepeat();
        });

        btnShuffle.setOnClickListener(arg0 -> {
            viewModel.toggleShuffle();
        });

        btnDownload.setOnClickListener(v -> {
            if (viewModel.currentSongIndex.getValue() != null && viewModel.songsList.getValue() != null) {
                String url = viewModel.songsList.getValue().get(viewModel.currentSongIndex.getValue()).get("songPath");
                String serverName = viewModel.songsList.getValue().get(viewModel.currentSongIndex.getValue()).get("songTitle");
                startDownload(url, serverName);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_managerdb, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.gbackmenu) {
            this.finish();
        }


        return super.onOptionsItemSelected(item);
    }


    // @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (viewModel.mediaPlayer.getValue() != null && viewModel.mediaPlayer.getValue().isPlaying()) {
                viewModel.mediaPlayer.getValue().pause();
            }

            this.finish();
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private final Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            try {
                if (viewModel.mediaPlayer.getValue() != null) {
                    long totalDuration = viewModel.mediaPlayer.getValue().getDuration();
                    long currentDuration = viewModel.mediaPlayer.getValue().getCurrentPosition();

                    // Displaying Total Duration time
                    songTotalDurationLabel.setText(utils.milliSecondsToTimer(totalDuration));
                    // Displaying time completed playing
                    songCurrentDurationLabel.setText(utils.milliSecondsToTimer(currentDuration));

                    // Updating progress bar
                    int progress = (utils.getProgressPercentage(currentDuration, totalDuration));
                    //Log.d("Progress", ""+progress);
                    songProgressBar.setProgress(progress);

                    // Running this thread after 100 milliseconds
                    mHandler.postDelayed(this, 100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message updates from handler
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress handler
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        if (viewModel.mediaPlayer.getValue() != null) {
            int totalDuration = viewModel.mediaPlayer.getValue().getDuration();
            int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

            // forward or backward to certain seconds
            viewModel.mediaPlayer.getValue().seekTo(currentPosition);

            // update timer progress again
            updateProgressBar();
        }
    }

    public void startDownload(String ImgUrl, String ServerName) {
        final String url = ImgUrl;
        if (isDownloading) {
            Toast.makeText(this, "A download is already in progress", Toast.LENGTH_SHORT).show();
            return;
        }
        isDownloading = true;
        runOnUiThread(() -> {
            if (layoutLoading != null) {
                layoutLoading.setVisibility(View.VISIBLE);
            }
            if (progressBar != null) {
                progressBar.setProgress(0);
                progressBar.setMax(100);
            }
        });

        downloadExecutor.submit(() -> {
            OutputStream output = null;
            BufferedInputStream input = null;
            try {
                URL u = new URL(url);
                URLConnection conexion = u.openConnection();
                conexion.connect();
                int lenghtOfFile = conexion.getContentLength();

                input = new BufferedInputStream(u.openStream());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, ServerName + ".mp3");
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg");
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                    Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    output = getContentResolver().openOutputStream(uri);
                } else {
                    File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!downloadsDir.exists()) {
                        downloadsDir.mkdirs();
                    }
                    File outFile = new File(downloadsDir, ServerName + ".mp3");
                    output = new FileOutputStream(outFile);
                }

                byte[] data = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    if (lenghtOfFile > 0) {
                        int progress = (int) ((total * 100) / lenghtOfFile);
                        final int p = progress;
                        runOnUiThread(() -> {
                            if (progressBar != null) {
                                progressBar.setProgress(p);
                            }
                        });
                    }
                    output.write(data, 0, count);
                }
                output.flush();

                runOnUiThread(() -> {
                    if (layoutLoading != null) {
                        layoutLoading.setVisibility(View.GONE);
                    }
                    isDownloading = false;
                    Toast.makeText(managerdb.this, "Download complete", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                Log.e("managerdb", "Download error", e);
                runOnUiThread(() -> {
                    if (layoutLoading != null) {
                        layoutLoading.setVisibility(View.GONE);
                    }
                    isDownloading = false;
                    Toast.makeText(managerdb.this, "Download failed", Toast.LENGTH_SHORT).show();
                });
            } finally {
                try {
                    if (output != null) output.close();
                    if (input != null) input.close();
                } catch (IOException ignored) {
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdateTimeTask);
        downloadExecutor.shutdownNow();
    }


}
