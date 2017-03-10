package com.ak93.exoplayerexample;

import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import java.io.File;
import java.util.Formatter;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SimpleExoPlayer exoPlayer;
    private ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            Log.i(TAG,"onTimelineChanged");
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.i(TAG,"onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.i(TAG,"onLoadingChanged");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.i(TAG,"onPlayerStateChanged: playWhenReady = "+String.valueOf(playWhenReady)
                    +" playbackState = "+playbackState);
            switch (playbackState){
                case ExoPlayer.STATE_ENDED:
                    Log.i(TAG,"Playback ended!");
                    break;
                case ExoPlayer.STATE_READY:
                    Log.i(TAG,"ExoPlayer ready!");
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    Log.i(TAG,"Playback buffering!");
                    break;
                case ExoPlayer.STATE_IDLE:
                    Log.i(TAG,"ExoPlayer idle!");
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.i(TAG,"onPlaybackError: "+error.getMessage());
        }

        @Override
        public void onPositionDiscontinuity() {
            Log.i(TAG,"onPositionDiscontinuity");
        }
    };

    private SeekBar seekPlayerProgress;
    private Handler handler;
    private ImageButton btnPlay;
    private TextView txtCurrentTime, txtEndTime;
    private boolean isPlaying = false;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File f_ext_files_dir = getExternalFilesDir(null);

        FileDialog fileDialog = new FileDialog(this, f_ext_files_dir, "");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            @Override
            public void fileSelected(File file) {
                Log.i("File selected:",file.getAbsolutePath());
                prepareExoPlayerFromFileUri(Uri.fromFile(file));
            }
        });
        fileDialog.showDialog();

        //prepareExoPlayerFromRawResourceUri(RawResourceDataSource.buildRawResourceUri(R.raw.audio));
    }

    private void prepareExoPlayerFromFileUri(Uri uri){
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(null), new DefaultLoadControl());
        exoPlayer.addListener(eventListener);

        DataSpec dataSpec = new DataSpec(uri);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };
        MediaSource audioSource = new ExtractorMediaSource(fileDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);

        exoPlayer.prepare(audioSource);
        initMediaControls();
    }

    private void prepareExoPlayerFromRawResourceUri(Uri uri){
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(null), new DefaultLoadControl());
        exoPlayer.addListener(eventListener);

        DataSpec dataSpec = new DataSpec(uri);
        final RawResourceDataSource rawResourceDataSource = new RawResourceDataSource(this);
        try {
            rawResourceDataSource.open(dataSpec);
        } catch (RawResourceDataSource.RawResourceDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return rawResourceDataSource;
            }
        };

        MediaSource audioSource = new ExtractorMediaSource(rawResourceDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);

        exoPlayer.prepare(audioSource);
        initMediaControls();
    }

    private void initMediaControls() {
        initPlayButton();
        initSeekBar();
        initTxtTime();
    }

    private void initPlayButton() {
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnPlay.requestFocus();
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPlaying){
                    exoPlayer.setPlayWhenReady(false);
                    isPlaying =false;
                    btnPlay.setImageResource(android.R.drawable.ic_media_play);
                }else{
                    exoPlayer.setPlayWhenReady(true);
                    isPlaying =true;
                    setProgress();
                    btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });
    }

    private void initTxtTime() {
        txtCurrentTime = (TextView) findViewById(R.id.time_current);
        txtEndTime = (TextView) findViewById(R.id.player_end_time);
    }

    private String stringForTime(int timeMs) {
        StringBuilder mFormatBuilder;
        Formatter mFormatter;
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds =  timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours   = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void setProgress() {
        seekPlayerProgress.setProgress(0);
        seekPlayerProgress.setMax(0);
        seekPlayerProgress.setMax((int) exoPlayer.getDuration()/1000);

        handler = new Handler();
        //Make sure you update Seekbar on UI thread
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (exoPlayer != null && isPlaying) {
                    seekPlayerProgress.setMax(0);
                    seekPlayerProgress.setMax((int) exoPlayer.getDuration()/1000);
                    int mCurrentPosition = (int) exoPlayer.getCurrentPosition() / 1000;
                    seekPlayerProgress.setProgress(mCurrentPosition);
                    txtCurrentTime.setText(stringForTime((int)exoPlayer.getCurrentPosition()));
                    txtEndTime.setText(stringForTime((int)exoPlayer.getDuration()));

                    handler.postDelayed(this, 1000);
                }

            }
        });
    }

    private void initSeekBar() {
        seekPlayerProgress = (SeekBar) findViewById(R.id.mediacontroller_progress);
        seekPlayerProgress.requestFocus();

        seekPlayerProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return;
                }

                exoPlayer.seekTo(progress*1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekPlayerProgress.setMax(0);
        seekPlayerProgress.setMax((int) exoPlayer.getDuration()/1000);

    }
}
