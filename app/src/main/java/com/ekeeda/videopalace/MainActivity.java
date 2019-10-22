package com.ekeeda.videopalace;

import android.net.Uri;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.github.vkay94.dtpv.DoubleTapPlayerView;
import com.github.vkay94.dtpv.PlayerDoubleTapListener;
import com.github.vkay94.dtpv.SeekListener;
import com.github.vkay94.dtpv.YouTubeDoubleTap;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements PlayerDoubleTapListener, Player.EventListener {

    private String TAG = ".MainActivity";
    private SimpleExoPlayer player;
    private YouTubeDoubleTap doubleTapOverlay;
    private DoubleTapPlayerView playerView;
    private ProgressBar progressBar;
    private LinearLayout controlPanel;
    private Spinner spinnerSpeeds;
    private String[] speeds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
        playerView = findViewById(R.id.playerView);
        doubleTapOverlay = findViewById(R.id.doubleTapOverlay);
        progressBar = findViewById(R.id.progressBar);
        controlPanel = findViewById(R.id.control_panel);
        spinnerSpeeds = findViewById(R.id.spinner_speeds);
        speeds = getResources().getStringArray(R.array.speed_values);
        initializePlayer();
        doubleTapOverlay
                .setPlayer(playerView)
                .setForwardRewindIncrementMs(10000)
                .setSeekListener(new SeekListener() {
                    @Override
                    public void onVideoStartReached() {
                        pausePlayer();
                    }

                    @Override
                    public void onVideoEndReached() {

                    }
                });

        playerView.activateDoubleTap(true)
                .setDoubleTapListener(doubleTapOverlay)
                .setDoubleTapDelay(500);
        buildMediaSource();
    }

    private void buildMediaSource() {
         final MediaSource mediaSource = new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(
                 MainActivity.this,
                 getResources().getString(R.string.app_name)
         )).createMediaSource(Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"));
//        final MediaSource mediaSource = new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(
//                MainActivity.this,
//                getResources().getString(R.string.app_name)
//        )).createMediaSource(Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"));
         if (player != null) {
             player.prepare(mediaSource);
             player.setPlayWhenReady(true);
         }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void pausePlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    private void resumePlayer() {
        if (player != null) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    private void initializePlayer() {
        if (player == null) {
            LoadControl loadControl = new DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                            VideoConfig.MIN_BUFFER_DURATION,
                            VideoConfig.MAX_BUFFER_DURATION,
                            VideoConfig.MIN_PLAYBACK_START_BUFFER,
                            VideoConfig.MIN_PLAYBACK_RESUME_BUFFER
                    ).createDefaultLoadControl();
            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(this)
                    .build();
            RenderersFactory  defaultRendersFactory = new DefaultRenderersFactory(this);
            TrackSelection.Factory  defaultFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
            TrackSelector trackSelector = new DefaultTrackSelector(defaultFactory);
            player = ExoPlayerFactory.newSimpleInstance(
                    this,
                    defaultRendersFactory,
                    trackSelector,
                    loadControl
            );
            player.addListener(MainActivity.this);
            player.setPlaybackParameters(new PlaybackParameters(1.0F));
            playerView.setPlayer(player);
            spinnerSpeeds.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    player.setPlaybackParameters(new PlaybackParameters(Float.valueOf(speeds[position])));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pausePlayer();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (Objects.requireNonNull(player.getPlaybackState()) == Player.STATE_READY) {
            Objects.requireNonNull(player).getPlayWhenReady();
            resumePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(player).release();
        releasePlayer();
        player = null;
    }

    @Override
    public void onDoubleTapStarted(float posX, float posY) {

    }

    @Override
    public void onDoubleTapProgressDown(float posX, float posY) {

    }

    @Override
    public void onDoubleTapProgressUp(float posX, float posY) {

    }

    @Override
    public void onDoubleTapFinished() {

    }

    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_BUFFERING) {
            progressBar.setVisibility(View.VISIBLE);
            controlPanel.setVisibility(View.GONE);
        } else if (playbackState == Player.STATE_READY) {
            progressBar.setVisibility(View.GONE);
            controlPanel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
}
