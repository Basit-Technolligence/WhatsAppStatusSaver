package com.example.whatsappstatussaver;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.IOException;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class ViewVideo extends AppCompatActivity{


    VideoView videoView;
    private View decorView;
    ImageView backArrow,screenLock,previous,next,play,pause,fullScreen,smallScreen,cropScreen,rotateScreen,screenUnlock,popupScreen;
    TextView videoTitleText,currentTime,leftTime,screenSizeText,textVolume,textBrightness,textSeekbar,textPlayPause;
    ArrayList<String> videoUri;
    ArrayList<String> videoTitle;
    int position;
    RelativeLayout mainLayout;
    FrameLayout topFrameLayout,bottomFrameLayout;
    boolean framesVisibility=false;
    boolean screenLandscape=true;
    boolean lockScreen=false;
    SeekBar seekbar,brightbar,volumebar;
    private Handler updateHandler = new Handler();
    int dur = 0;
    int current = 0;
    String videoCurrentTime,videoTotalTime;
    int videoPauseTime;
    boolean videoPlayed;
    private GestureDetector mDetector;
    AudioManager audioManager;
    long currentPosition;
    //scroll intances
    private int mMaxVolume;
    Long lastSeekUpdateTime = null;
    Long lastVolumeUpdateTime = null;
    private float mCurBrightness = -1.0f;
    private int mCurVolume = -1;
    private int brightness;
    //interstitial ad
    private InterstitialAd mInterstitialAd;
    //native ad
    TemplateView template;
    String adsLoaded;



    private Runnable updateVideoTime = new Runnable() {
        @Override
        public void run() {
            dur = videoView.getDuration();
            currentPosition = videoView.getCurrentPosition();
            seekbar.setProgress((int) currentPosition);
            updateHandler.postDelayed(this, 100);
            current = videoView.getCurrentPosition();
            int currentmns = (int) ((current % 3600000.0d) / 60000.0d);
            int currentscs = (int) (((current % 3600000.0d) % 60000.0d) / 1000.0d);
            if (((int) (current / 3600000.0d)) > 0) {
                videoCurrentTime = String.format("%02d:%02d:%02d", new Object[]{Integer.valueOf((int) (current / 3600000.0d)), Integer.valueOf(currentmns), Integer.valueOf(currentscs)});
            } else {
                videoCurrentTime = String.format("%02d:%02d", new Object[]{Integer.valueOf(currentmns), Integer.valueOf(currentscs)});
            }
            int leftmns = (dur % 3600000) / 60000;
            int leftscs = ((dur % 3600000) % 60000) / 1000;
            if (dur / 3600000 > 0) {
                videoTotalTime = String.format("%02d:%02d:%02d", new Object[]{Integer.valueOf(dur / 3600000), Integer.valueOf(leftmns), Integer.valueOf(leftscs)});
            } else {
                videoTotalTime = String.format("%02d:%02d", new Object[]{Integer.valueOf(leftmns), Integer.valueOf(leftscs)});
            }
            currentTime.setText(videoCurrentTime);
            textSeekbar.setText(videoCurrentTime);

            leftTime.setText(videoTotalTime);
            if(videoView.isPlaying()){
                template.setVisibility(View.GONE);
            }
//            if(videoCurrentTime.equals(videoTotalTime)){
////                if(!clickPlayPause) {
//                    play.setVisibility(View.VISIBLE);
//                    pause.setVisibility(View.GONE);
//                }
//            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPauseTime=videoView.getCurrentPosition();
        if(videoView.isPlaying()){
            videoView.pause();
            videoPlayed=true;
        }


    }
    @Override
    protected void onResume() {
        super.onResume();
            if (videoPlayed) {
                videoView.seekTo(videoPauseTime);
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
            }else{
                videoView.seekTo(videoPauseTime);
            }

    }


    @Override
    public void onBackPressed() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_video);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        interstitialAd();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        }, 30000);
        template = findViewById(R.id.my_template);
        template.setVisibility(View.GONE);
        videoTitleText=(TextView) findViewById(R.id.videoTitle);
        backArrow=(ImageView) findViewById(R.id.backArrow);
        videoView=(VideoView) findViewById(R.id.videoView);
        screenLock=(ImageView) findViewById(R.id.screenLock);
        screenUnlock=(ImageView) findViewById(R.id.screenUnlock);
        previous=(ImageView) findViewById(R.id.previous);
        next=(ImageView) findViewById(R.id.next);
        play=(ImageView)findViewById(R.id.play);
        textSeekbar=(TextView) findViewById(R.id.textseekbar);
        textPlayPause=(TextView) findViewById(R.id.textplay);
        pause=(ImageView)findViewById(R.id.pause);
        textBrightness=(TextView) findViewById(R.id.textbrightness);
        textVolume=(TextView) findViewById(R.id.textvolume);
        brightbar=(SeekBar) findViewById(R.id.brightness_seekbar);
        volumebar=(SeekBar) findViewById(R.id.volume_seekbar);
        popupScreen=(ImageView)findViewById(R.id.popupScreen);
        screenSizeText=(TextView) findViewById(R.id.screenSizeText);
        currentTime=(TextView) findViewById(R.id.startTime);
        leftTime=(TextView) findViewById(R.id.endTime);
        fullScreen=(ImageView) findViewById(R.id.fullScreen);
        smallScreen=(ImageView) findViewById(R.id.smallScreen);
        cropScreen=(ImageView) findViewById(R.id.cropScreen);
        rotateScreen=(ImageView) findViewById(R.id.rotateScreen);
        topFrameLayout =(FrameLayout) findViewById(R.id.topFrameLayout);
        bottomFrameLayout=(FrameLayout) findViewById(R.id.bottomFrameLayout);
        mainLayout=(RelativeLayout) findViewById(R.id.mainLayout);
        videoUri=getIntent().getStringArrayListExtra("VideoUri");
        videoTitle=getIntent().getStringArrayListExtra("VideoTitle");
        position=getIntent().getIntExtra("VideoPosition",0);
        adsLoaded=getIntent().getStringExtra("AdsLoaded");
        videoTitleText.setText(videoTitle.get(position));
//        videoView.setVideoURI(Uri.parse(videoUri.get(position)));
        videoView.setVideoPath(videoUri.get(position));
        videoView.start();
        topFrameLayout.setVisibility(View.GONE);
        bottomFrameLayout.setVisibility(View.GONE);
        play.setVisibility(View.GONE);
        cropScreen.setVisibility(View.GONE);
        smallScreen.setVisibility(View.GONE);
        rotateScreen.setVisibility(View.GONE);
        screenUnlock.setVisibility(View.GONE);
        brightbar.setVisibility(View.GONE);
        volumebar.setVisibility(View.GONE);
        textSeekbar.setVisibility(View.GONE);
        textVolume.setVisibility(View.GONE);
        textBrightness.setVisibility(View.GONE);
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        seekbar = (SeekBar) findViewById(R.id.video_seekbar);
        mDetector = new GestureDetector(this, new MyGestureListener());
        lastSeekUpdateTime=Long.valueOf(System.currentTimeMillis());
        lastVolumeUpdateTime=Long.valueOf(System.currentTimeMillis());
        this.brightbar.setMax(100);
        this.volumebar.setMax(this.audioManager.getStreamMaxVolume(3));
        this.volumebar.setProgress(this.audioManager.getStreamVolume(3));
        this.volumebar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ViewVideo.this.audioManager.setStreamVolume(3, progress, 0);
                if (progress > 0) {
                    progress = (progress * 100) / ViewVideo.this.mMaxVolume;
                }
                ViewVideo.this.textVolume.setText(String.valueOf(progress));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        this.volumebar.setKeyProgressIncrement(1);
        this.brightbar.setKeyProgressIncrement(1);
        try {
            this.brightness = Settings.System.getInt(getContentResolver(), "screen_brightness");
        } catch (Settings.SettingNotFoundException e) {
            Log.e("Error", "Cannot access system brightness");
            e.printStackTrace();
        }
        //time and seekbar code starts here
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                seekbar.setProgress(0);
                seekbar.setMax(videoView.getDuration());
                updateHandler.postDelayed(updateVideoTime, 100);
            }
        });
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    // Mean that the seekbar value is changed by user
                    videoView.seekTo(progress); // Verify this
                } else {
                    // Ignore becuase is due to seekBar programmatically change

                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        //my code
        topFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        bottomFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                template.setVisibility(View.GONE);
                position=position-1;
                if(position<0){
                    position=videoUri.size()-1;
                }
               positionAdjustment("minus");
                videoTitleText.setText(videoTitle.get(position));
                videoView.setVideoURI(Uri.parse(videoUri.get(position)));
                if(!videoView.isPlaying()){
                    videoView.start();
                    pause.setVisibility(View.VISIBLE);
                    play.setVisibility(View.GONE);
                }

            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                template.setVisibility(View.GONE);
                position=position+1;
               positionAdjustment("plus");
                if(position>(videoUri.size()-1)){
                    position=0;
                }
                videoTitleText.setText(videoTitle.get(position));
                videoView.setVideoURI(Uri.parse(videoUri.get(position)));
                if(!videoView.isPlaying()){
                    videoView.start();
                    pause.setVisibility(View.VISIBLE);
                    play.setVisibility(View.GONE);
                }

            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.pause();
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
                topFrameLayout.setVisibility(View.VISIBLE);
                bottomFrameLayout.setVisibility(View.VISIBLE);
                framesVisibility=true;
                nativeAd();


            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                template.setVisibility(View.GONE);
                videoView.start();
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
                if(videoView.isPlaying()){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            topFrameLayout.setVisibility(View.GONE);
                            bottomFrameLayout.setVisibility(View.GONE);
                            rotateScreen.setVisibility(View.GONE);
                            framesVisibility=false;
                        }
                    }, 1000);
                }
            }
        });
        rotateScreen.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            public void onClick(View v) {
                if(!screenLandscape) {
                    setRequestedOrientation(0);
                    screenLandscape=true;
                    //videoView.start();
                }else{
                    screenLandscape=false;
                    setRequestedOrientation(1);
                }
            }
        });
        screenLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockScreen=true;
                topFrameLayout.setVisibility(View.GONE);
                bottomFrameLayout.setVisibility(View.GONE);
                rotateScreen.setVisibility(View.GONE);
                screenUnlock.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        screenUnlock.setVisibility(View.GONE);
                    }
                }, 4000);

            }
        });
        screenUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockScreen=false;
                topFrameLayout.setVisibility(View.VISIBLE);
                bottomFrameLayout.setVisibility(View.VISIBLE);
                rotateScreen.setVisibility(View.VISIBLE);
                screenUnlock.setVisibility(View.GONE);
            }
        });
        fullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullScreen.setVisibility(View.GONE);
                cropScreen.setVisibility(View.VISIBLE);
                DisplayMetrics metrics = new DisplayMetrics();
                ViewVideo.this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ViewVideo.this.videoView.getLayoutParams();
                params.width = metrics.widthPixels;
                params.height = metrics.heightPixels;
                params.leftMargin = 0;
                params.rightMargin = 0;
                params.topMargin = 0;
                params.bottomMargin = 0;
                ViewVideo.this.videoView.setLayoutParams(params);
                screenSizeText.setText("FIT TO SCREEN");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        screenSizeText.setText("");
                    }
                }, 4000);

            }
        });
        cropScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropScreen.setVisibility(View.GONE);
                smallScreen.setVisibility(View.VISIBLE);
                DisplayMetrics metrics = new DisplayMetrics();
                ViewVideo.this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ViewVideo.this.videoView.getLayoutParams();
                params.width = (int) (400.0f * metrics.density);
                params.height = (int) (BitmapDescriptorFactory.HUE_MAGENTA * metrics.density);
                params.leftMargin = 150;
                params.rightMargin = 150;
                params.topMargin = 150;
                params.bottomMargin = 150;
                ViewVideo.this.videoView.setLayoutParams(params);

                screenSizeText.setText("CROP");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        screenSizeText.setText("");
                    }
                }, 4000);

            }
        });
        smallScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                smallScreen.setVisibility(View.GONE);
                fullScreen.setVisibility(View.VISIBLE);
                DisplayMetrics metrics = new DisplayMetrics();
                ViewVideo.this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ViewVideo.this.videoView.getLayoutParams();
                params.width = (int) (1000.0f * metrics.density);
                params.leftMargin = 150;
                params.rightMargin = 150;
                params.topMargin = 0;
                params.bottomMargin = 0;
                ViewVideo.this.videoView.setLayoutParams(params);
                screenSizeText.setText("100%");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        screenSizeText.setText("");
                    }
                }, 4000);

            }
        });
        popupScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    final PictureInPictureParams.Builder pictureInPictureParamsBuilder;
                    pictureInPictureParamsBuilder = new PictureInPictureParams.Builder();
                    Rational aspectRatio = new Rational(videoView.getWidth(), videoView.getHeight());
                    pictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();
                    enterPictureInPictureMode(pictureInPictureParamsBuilder.build());
                }else{
                    Toast.makeText(getApplicationContext(),"Your phone not support this feature",Toast.LENGTH_LONG).show();
                }

            }
        });
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
                finish();
            }
        });


        //status bar code
        decorView=getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                decorView.setSystemUiVisibility(hideSystemBars());
            }
        });

        // volume + brightness + seekbar gesture code
        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case 1:
                        mCurVolume = -1;
                        mCurBrightness = -1.0f;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                volumebar.setVisibility(View.GONE);
                                textVolume.setVisibility(View.GONE);
                                brightbar.setVisibility(View.GONE);
                                textBrightness.setVisibility(View.GONE);
                            }
                        }, 500);

                        break;
                }
                return mDetector.onTouchEvent(event);

            }
        });

    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent event) {
//            Toast.makeText(getApplicationContext(),"onDown",Toast.LENGTH_SHORT).show();

            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if(!lockScreen){
                if(framesVisibility){
                    topFrameLayout.setVisibility(View.GONE);
                    bottomFrameLayout.setVisibility(View.GONE);
                    rotateScreen.setVisibility(View.GONE);
                    framesVisibility=false;

                }else{
                    topFrameLayout.setVisibility(View.VISIBLE);
                    bottomFrameLayout.setVisibility(View.VISIBLE);
                    framesVisibility=true;
//                    if(videoView.isPlaying()){
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                if(!framesVisibility) {
//                                    topFrameLayout.setVisibility(View.GONE);
//                                    bottomFrameLayout.setVisibility(View.GONE);
//                                    rotateScreen.setVisibility(View.GONE);
//                                    framesVisibility=false;
//                                }
//                            }
//                        }, 4000);
//                    }else{
//                        framesVisibility=true;
//                    }
                }
            }else{
                screenUnlock.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            screenUnlock.setVisibility(View.GONE);
                        }
                    }, 4000);
                }
            return true;
        }





        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if(videoView.isPlaying()){
                videoView.pause();
                nativeAd();
                pause.setVisibility(View.GONE);
                play.setVisibility(View.VISIBLE);
                topFrameLayout.setVisibility(View.VISIBLE);
                bottomFrameLayout.setVisibility(View.VISIBLE);
                framesVisibility=true;
                textPlayPause.setText("PAUSE");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                   textPlayPause.setText("");
                    }
                }, 1000);
            }else{
                videoView.start();
                template.setVisibility(View.GONE);
                play.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
                textPlayPause.setText("PLAY");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textPlayPause.setText("");
                    }
                }, 1000);
                if(videoView.isPlaying()){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            topFrameLayout.setVisibility(View.GONE);
                            bottomFrameLayout.setVisibility(View.GONE);
                            rotateScreen.setVisibility(View.GONE);
                            framesVisibility=false;
                        }
                    }, 1000);
                }
            }

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            float deltaX = e1.getRawX() - e2.getRawX();
            float deltaY = e1.getRawY() - e2.getRawY();
            Long currentTime = Long.valueOf(System.currentTimeMillis());
            ViewVideo.this.setGestureListener();
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                if (Math.abs(deltaX) > 20.0f && currentTime.longValue() >= ViewVideo.this.lastVolumeUpdateTime.longValue() + 1000) {
                    boolean z;
                    ViewVideo.this.lastSeekUpdateTime = currentTime;
                    ViewVideo viewVideo = ViewVideo.this;
                    if (deltaX < 0.0f) {
                        z = true;
                    } else {
                        z = false;
                    }
                    viewVideo.onHorizontalScroll(z);
                }
            } else if (Math.abs(deltaY) > BitmapDescriptorFactory.HUE_YELLOW && currentTime.longValue() >= ViewVideo.this.lastSeekUpdateTime.longValue() + 1000) {
                if (((double) e1.getX()) < ((double) ViewVideo.getDeviceWidth(getApplicationContext())) * 0.5d) {
                    ViewVideo.this.lastVolumeUpdateTime = currentTime;
                    ViewVideo.this.onVerticalScroll(deltaY / ((float) ViewVideo.getDeviceHeight(getApplicationContext())), 1);
                } else if (((double) e1.getX()) > ((double) ViewVideo.getDeviceWidth(getApplicationContext())) * 0.5d) {
                    ViewVideo.this.lastVolumeUpdateTime = currentTime;
                    ViewVideo.this.onVerticalScroll(deltaY / ((float) ViewVideo.getDeviceHeight(getApplicationContext())), 2);
                }
            }
            return true;
        }

    }
    public void setGestureListener() {
        this.mMaxVolume = this.audioManager.getStreamMaxVolume(3);
    }
    public static int getDeviceWidth(Context context) {
        @SuppressLint("WrongConstant") WindowManager wm = (WindowManager) context.getSystemService("window");
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(mDisplayMetrics);
        return mDisplayMetrics.widthPixels;
    }
    public static int getDeviceHeight(Context context) {
        @SuppressLint("WrongConstant") WindowManager wm = (WindowManager) context.getSystemService("window");
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(mDisplayMetrics);
        return mDisplayMetrics.heightPixels;
    }
    public void onVerticalScroll(float percent, int direction) {
        if (direction == 1) {
            changeBrightness(percent * 2.0f);
        } else {
            changeVolume(percent * 2.0f);
        }
    }

    @SuppressLint("WrongConstant")
    public void onHorizontalScroll(boolean seekForward) {
        textSeekbar.setVisibility(View.VISIBLE);
        if (((seekForward && this.videoView.canSeekForward()) || (!seekForward && this.videoView.canSeekBackward())) ) {
            if (this.bottomFrameLayout.getVisibility() == View.GONE && lockScreen==false) {
                this.bottomFrameLayout.setVisibility(View.VISIBLE);
                this.topFrameLayout.setVisibility(View.VISIBLE);
            }
            if (seekForward) {
                Log.i("ViewGestureListener", "Forwarding");
                this.currentPosition = this.videoView.getCurrentPosition();
                this.currentPosition = this.videoView.getCurrentPosition() + 700;
                this.videoView.seekTo((int) currentPosition);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textSeekbar.setVisibility(View.GONE);
                        bottomFrameLayout.setVisibility(View.GONE);
                        topFrameLayout.setVisibility(View.GONE);
                        rotateScreen.setVisibility(View.GONE);
                        framesVisibility=false;
                    }
                }, 1200);
                return;
            }
            Log.i("ViewGestureListener", "Rewinding");
            this.currentPosition = this.videoView.getCurrentPosition();
            this.currentPosition = this.videoView.getCurrentPosition() - 700;
            this.videoView.seekTo((int) currentPosition);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    textSeekbar.setVisibility(View.GONE);
                    bottomFrameLayout.setVisibility(View.GONE);
                    topFrameLayout.setVisibility(View.GONE);
                    rotateScreen.setVisibility(View.GONE);
                    framesVisibility=false;
                }
            }, 1200);
        }
    }

    @SuppressLint("WrongConstant")
    private void changeBrightness(float percent) {
        brightbar.setVisibility(View.VISIBLE);
        textBrightness.setVisibility(View.VISIBLE);
        if (this.mCurBrightness == -1.0f) {
            this.mCurBrightness = this.getWindow().getAttributes().screenBrightness;
            if (this.mCurBrightness <= 0.01f) {
                this.mCurBrightness = 0.01f;
            }
        }
        this.textBrightness.setVisibility(0);
        WindowManager.LayoutParams attributes = this.getWindow().getAttributes();
        attributes.screenBrightness = this.mCurBrightness + percent;
        if (attributes.screenBrightness >= TextTrackStyle.DEFAULT_FONT_SCALE) {
            attributes.screenBrightness = TextTrackStyle.DEFAULT_FONT_SCALE;
        } else if (attributes.screenBrightness <= 0.01f) {
            attributes.screenBrightness = 0.01f;
        }
        this.getWindow().setAttributes(attributes);
        float p = attributes.screenBrightness * 100.0f;
        this.brightbar.setProgress((int) p);
        this.textBrightness.setText(String.valueOf((int) p));
    }

    @SuppressLint("WrongConstant")
    private void changeVolume(float percent) {
        volumebar.setVisibility(View.VISIBLE);
        textVolume.setVisibility(View.VISIBLE);
        this.volumebar.setVisibility(0);
        this.textVolume.setVisibility(0);
        if (this.mCurVolume == -1) {
            this.mCurVolume = this.audioManager.getStreamVolume(3);
            if (((float) this.mCurVolume) < 0.01f) {
                this.mCurVolume = 0;
            }
        }
        int volume = ((int) (((float) this.mMaxVolume) * percent)) + this.mCurVolume;
        if (volume > this.mMaxVolume) {
            volume = this.mMaxVolume;
        }
        if (((float) volume) < 0.01f) {
            volume = 0;
        }
        this.volumebar.setProgress(volume);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            decorView.setSystemUiVisibility(hideSystemBars());
        }
    }
    private  int hideSystemBars(){
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION ;
    }
    private void interstitialAd(){
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        //interstitial test ad id = ca-app-pub-3940256099942544/1033173712
        //interstitial ad id=
    }
    private  void nativeAd(){
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
        //native test ad id = ca-app-pub-3940256099942544/2247696110
        //native ad id=
        AdLoader adLoader = new AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110")
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        // Show the ad.
                        NativeTemplateStyle styles = new
                                NativeTemplateStyle.Builder().build();

                      template.setVisibility(View.VISIBLE);
                        template.setStyles(styles);
                        template.setNativeAd(unifiedNativeAd);
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // Handle the failure by logging, altering the UI, and so on.
                        //Toast.makeText(ViewVideo.this, "fail", Toast.LENGTH_SHORT).show();
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder()
                        // Methods in the NativeAdOptions.Builder class can be
                        // used here to specify individual options settings.
                        .build())
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }
    private void positionAdjustment(String sign) {
        if (adsLoaded.equals("Yes")) {
            if (videoTitle.size() <= 5) {
                if (position == videoTitle.size() - 1) {
                    if(sign.equals("minus")) {
                        position = position - 1;
                    }else{
                        position = position + 1;
                    }
                }
            } else {
                if (position==4 || position==13 || position == 22 || position == 31 || position == 40 || position == 49) {
                    if(sign.equals("minus")) {
                        position = position - 1;
                    }else{
                        position = position + 1;
                    }
                }
            }
        }
    }

}
