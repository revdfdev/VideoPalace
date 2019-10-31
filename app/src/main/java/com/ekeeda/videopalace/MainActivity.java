package com.ekeeda.videopalace;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.FrameLayout;
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
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.material.appbar.AppBarLayout;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements PlayerDoubleTapListener, Player.EventListener, PlayerControlView.VisibilityListener {

    private String TAG = ".MainActivity";
    private SimpleExoPlayer player;
    private YouTubeDoubleTap doubleTapOverlay;
    private DoubleTapPlayerView playerView;
    private ProgressBar progressBar;
    private LinearLayout controlPanel;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private int speedCheckedItem = -1;
    private int qualityCheckedItem = -1;
//    private Spinner spinnerSpeeds;
//    private Spinner spinnerQuality;
    private String[] speeds;
    private String[] qualities;
    private boolean isSystemUiShown;
    private FrameLayout fullScreenEnterButton;
    private FrameLayout fullScreenExitButton;


    //@SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playerView = findViewById(R.id.playerView);
        doubleTapOverlay = findViewById(R.id.doubleTapOverlay);
        progressBar = findViewById(R.id.progressBar);
        controlPanel = findViewById(R.id.control_panel);
        appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.setOutlineProvider(null);
        toolbar = findViewById(R.id.toolbar);
        fullScreenEnterButton = findViewById(R.id.exo_fullscreen_button);
        fullScreenExitButton = findViewById(R.id.exo_fullscreen_exit_button);
        int orientation = getResources().getConfiguration().orientation;
        fullScreenEnterButton.setOnClickListener( v -> setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        fullScreenExitButton.setOnClickListener(v -> setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            fullScreenExitButton.setVisibility(View.GONE);
            fullScreenEnterButton.setVisibility(View.VISIBLE);
        } else {
            fullScreenEnterButton.setVisibility(View.GONE);
            fullScreenExitButton.setVisibility(View.VISIBLE);
        }
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0F);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Video Title");
        initializePlayer();
        playerView.setControllerVisibilityListener(this);
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
//        spinnerSpeeds = findViewById(R.id.spinner_speeds);
//        spinnerQuality = findViewById(R.id.spinner_quality);
        speeds = getResources().getStringArray(R.array.speed_values);
        qualities = getResources().getStringArray(R.array.quality_values);

//        playerView.setOnTouchListener((view, motionEvent) -> {
//            if (controlPanel.getVisibility() == View.VISIBLE) {
//                controlPanel.setVisibility(View.GONE);
//                getSupportActionBar().hide();
//            } else {
//                controlPanel.setVisibility(View.VISIBLE);
//                getSupportActionBar().show();
//            }
//            return true;
//        });

        playerView.activateDoubleTap(true)
                .setDoubleTapListener(doubleTapOverlay);
        buildMediaSourceAuto();
    }

    private void buildMediaSource480p() {
        final MediaSource mediaSource = new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(
                MainActivity.this,
                getResources().getString(R.string.app_name)
        )).createMediaSource(Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"));
//        final MediaSource mediaSource = new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(
//                MainActivity.this,
//                getResources().getString(R.string.app_name)
//        )).createMediaSource(Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"));
        if (player != null) {
            player.prepare(mediaSource);
            player.setPlayWhenReady(true);
        }
    }

    private void buildMediaSource720p() {
        final MediaSource mediaSource = new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(
                MainActivity.this,
                getResources().getString(R.string.app_name)
        )).createMediaSource(Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"));
//        final MediaSource mediaSource = new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(
//                MainActivity.this,
//                getResources().getString(R.string.app_name)
//        )).createMediaSource(Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"));
        if (player != null) {
            player.prepare(mediaSource);
            player.setPlayWhenReady(true);
        }
    }

    private void buildMediaSourceAuto() {
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


    private void buildMediaSource360p() {
         final MediaSource mediaSource = new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(
                 MainActivity.this,
                 getResources().getString(R.string.app_name)
         )).createMediaSource(Uri.parse("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"));
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
            buildMediaSource360p();
            /*spinnerSpeeds.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    player.setPlaybackParameters(new PlaybackParameters(Float.valueOf(speeds[position])));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            spinnerQuality.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    switch (position) {
                        case 0:
                            buildMediaSourceAuto();
                            break;
                        case 1:
                            buildMediaSource720p();
                            break;
                        case 2:
                            buildMediaSource480p();
                            break;
                        case 3:
                            buildMediaSource360p();
                            break;
                        default:
                            buildMediaSourceAuto();
                            break;

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });*/
        }
    }

    private void showSpeedAlertDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Select speed");
        builder.setSingleChoiceItems(speeds, speedCheckedItem, (dialogInterface, position) -> {
            speedCheckedItem = position;
            player.setPlaybackParameters(new PlaybackParameters(Float.valueOf(speeds[position].substring(0,1))));
            dialogInterface.dismiss();
        });
        builder.create();
        builder.show();
    }

    private final Runnable checkSystemUiRunnable = this::checkHideSystemUI;

    private void checkHideSystemUI() {
        // Check if system UI is shown and hide it by post a delayed handler
        if (isSystemUiShown) {
            hideSystemUI();
            new Handler().postDelayed(checkSystemUiRunnable, 10000);
        }
    }

    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_VISIBLE
        );
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void showQualityAlertDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Select quality");
        builder.setSingleChoiceItems(qualities, qualityCheckedItem, (dialogInterface, position) -> {
            qualityCheckedItem = position;
            switch (position) {
                case 0:
                    buildMediaSourceAuto();
                    break;
                case 1:
                    buildMediaSource720p();
                    break;
                case 2:
                    buildMediaSource480p();
                    break;
                case 3:
                    buildMediaSource360p();
                    break;
                default:
                    buildMediaSourceAuto();
                    break;

            }
            dialogInterface.dismiss();
        });
        builder.create();
        builder.show();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isSystemUiShown = false;
            fullScreenEnterButton.setVisibility(View.GONE);
            fullScreenExitButton.setVisibility(View.VISIBLE);
            Objects.requireNonNull(player).setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            Objects.requireNonNull(playerView).setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            Objects.requireNonNull(getWindow());
            hideSystemUI();
            Objects.requireNonNull(getWindow()).getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    new Handler().postDelayed(checkSystemUiRunnable, 10000);
                    isSystemUiShown = true;
                } else {
                    isSystemUiShown = false;
                }
            });
        } else {
            Objects.requireNonNull(player).setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            Objects.requireNonNull(playerView).setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            showSystemUI();
            isSystemUiShown = false;
            fullScreenExitButton.setVisibility(View.GONE);
            fullScreenEnterButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_quality:
                showQualityAlertDialog();
                return true;
            case R.id.action_speed:
                showSpeedAlertDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    @Override
    public void onVisibilityChange(int visibility) {
        switch (visibility) {
            case View.VISIBLE:
                appBarLayout.setExpanded(true, true);
                Objects.requireNonNull(getSupportActionBar()).show();
                Log.d("Controls", "controls are visible");
                break;
            case View.GONE:
            case View.INVISIBLE:
                appBarLayout.setExpanded(false, true);
                Objects.requireNonNull(getSupportActionBar()).hide();
                Log.d("Controls", "controls are invisible");
        }
    }
}
