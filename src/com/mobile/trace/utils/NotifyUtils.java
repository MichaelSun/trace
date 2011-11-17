package com.mobile.trace.utils;

import java.io.IOException;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Vibrator;

import com.mobile.trace.R;

public class NotifyUtils {
   private static final String TAG = "NoticeUtils";
    
    private AudioManager mAudioManager;
    private Vibrator mVibrator;
    private Context mContext;
    
    private  MediaPlayer mMediaPlayer = null;
    
    private static final long[] VIBRATE_PATTERN = {0L, 100L};
    
    public static final int INNER_NOTICE = 0;
    public static final int GLOBAL_NOTICE = 1;
    
    public NotifyUtils(Context context) {
        this.mContext = context;
        this.mAudioManager = (AudioManager) mContext
                    .getSystemService(Context.AUDIO_SERVICE);
        this.mVibrator = (Vibrator) mContext
                    .getSystemService(Context.VIBRATOR_SERVICE);
    }
    
    public void playRingtone() {
            mMediaPlayer = MediaPlayer.create(mContext, R.raw.message);

            if (mMediaPlayer == null) {
                return;
            } else {
                mMediaPlayer.stop();
            }
            
        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mMediaPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                mp = null;
            }
        });
        mMediaPlayer.start();
    }
    
    public final void vibrateNow() {
        if(null == mVibrator) {
            mVibrator = (Vibrator) mContext
                .getSystemService(Context.VIBRATOR_SERVICE);
        }
        mVibrator.vibrate(VIBRATE_PATTERN, -1);
    }
}
