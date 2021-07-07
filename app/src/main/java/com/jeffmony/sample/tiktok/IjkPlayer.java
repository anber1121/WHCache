package com.jeffmony.sample.tiktok;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.jeffmony.playersdk.IPlayer;
import com.jeffmony.playersdk.common.PlayerSettings;
import com.jeffmony.playersdk.control.LocalProxyVideoControl;
import com.jeffmony.videocache.common.ProxyMessage;
import com.jeffmony.videocache.common.VideoParams;
import com.jeffmony.videocache.utils.ProxyCacheUtils;
import com.jeffmony.videocache.utils.VideoParamsUtils;
import com.jeffmony.videocache.utils.VideoProxyThreadUtils;
import com.tencent.videoplayer.player.AbstractPlayer;
import com.tencent.videoplayer.player.VideoViewManager;
import com.tencent.videoplayer.util.L;

import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

public class IjkPlayer extends AbstractPlayer {
    public final String TAG = IjkPlayer.class.getSimpleName();
    protected IjkMediaPlayer mMediaPlayer;
    private int mBufferedPercent;
    private Context mAppContext;
    protected PlayerSettings mPlayerSettings;
    protected float mProxyCachePercent = 0f;
    private IPlayer.OnProxyCacheInfoListener mOnProxyCacheInfoListener;
    private long initSeek = 0;
    private int isrend = 0;
    protected TiktokVideoControl mLocalProxyVideoControl;
    public IjkPlayer(Context context) {
        mAppContext = context;
    }

    @Override
    public void initPlayer() {
        L.i(" -xb- initPlayer");
        mLocalProxyVideoControl = new TiktokVideoControl(this);
        PlayerSettings playerSettings = new PlayerSettings();
        playerSettings.setLocalProxyEnable(true);
        initPlayerSettings(playerSettings);
        mMediaPlayer = new IjkMediaPlayer();
        //native日志
        IjkMediaPlayer.native_setLogLevel(VideoViewManager.getConfig().mIsEnableLog ? IjkMediaPlayer.IJK_LOG_INFO : IjkMediaPlayer.IJK_LOG_SILENT);
        setOptions();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnErrorListener(onErrorListener);
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
        mMediaPlayer.setOnInfoListener(onInfoListener);
        mMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
        mMediaPlayer.setOnPreparedListener(onPreparedListener);
        mMediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
        mMediaPlayer.setOnTimedTextListener(onRendListener);
        mMediaPlayer.setOnNativeInvokeListener(new IjkMediaPlayer.OnNativeInvokeListener() {
            @Override
            public boolean onNativeInvoke(int i, Bundle bundle) {
                L.d("-xb- setOnNativeInvokeListener i: "+i);
                return true;
            }
        });
    }

    public void initPlayerSettings(@NonNull PlayerSettings settings) {
        mPlayerSettings = settings;
    }


    public void setOnProxyCacheInfoListener(IPlayer.OnProxyCacheInfoListener listener) {
        mOnProxyCacheInfoListener = listener;
    }

    @Override
    public void setOptions() {
        L.i(" -xb- setOptions");
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "volume", 100);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "reconnect", 1);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "af", "loudnorm");
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "vn", 1);
//        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "nodisp", 1);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sn", 1);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek");
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L);
        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);
    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        L.i(" -xb- setDataSource path:"+path);
        if (mMediaPlayer == null) return;
        try {
            Uri uri = Uri.parse(path);
            if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(uri.getScheme())) {
                RawDataSourceProvider rawDataSourceProvider = RawDataSourceProvider.create(mAppContext, uri);
                mMediaPlayer.setDataSource(rawDataSourceProvider);
            } else {
                //处理UA问题
                if (headers != null) {
                    String userAgent = headers.get("User-Agent");
                    if (!TextUtils.isEmpty(userAgent)) {
                        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", userAgent);
                    }
                }
                String playUrl;
                if (mPlayerSettings.getLocalProxyEnable()) {
                    playUrl = ProxyCacheUtils.getProxyUrl(uri.toString(), null, null);
                    //请求放在客户端,非常便于控制
                    mLocalProxyVideoControl.startRequestVideoInfo(uri.toString(),null, null);
                } else {
                    playUrl = uri.toString();
                }
                mMediaPlayer.setDataSource(mAppContext, Uri.parse(playUrl), headers);
            }
        } catch (Exception e) {
            mPlayerEventListener.onError(ERR_SETDATASOURCE);
        }
    }

    public void setInitSeek(long time) {
        initSeek = time;
    }

    @Override
    public void setDataSource(AssetFileDescriptor fd) {
        L.i(" -xb- setDataSource fd:"+fd);
        if (mMediaPlayer == null) return;
        try {
            mMediaPlayer.setDataSource(new RawDataSourceProvider(fd));
        } catch (Exception e) {
            mPlayerEventListener.onError(ERR_SETDATASOURCE);
        }
    }

    @Override
    public void pause() {
        L.i(" -xb- pause");
        if (mMediaPlayer == null) return;
        if (mPlayerSettings.getLocalProxyEnable()) {
            mLocalProxyVideoControl.pauseLocalProxyTask();
        }
        try {
            mMediaPlayer.pause();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError(ERR_PAUSE);
        }
    }

    @Override
    public void start() {
        L.i(" -xb- start");
        if (mMediaPlayer == null) return;
        if (mPlayerSettings.getLocalProxyEnable()) {
            mLocalProxyVideoControl.resumeLocalProxyTask();
        }
        try {
            mMediaPlayer.start();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError(ERR_START);
        }
    }

    @Override
    public void stop() {
        L.i(" -xb- stop");
        if (mMediaPlayer == null) return;
        try {
            mMediaPlayer.stop();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError(ERR_STOP);
        }
    }

    @Override
    public void prepareAsync() {
        L.i(" -xb- prepareAsync");
        if (mMediaPlayer == null) return;
        try {
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError(ERR_PREPAREASYNC);
        }
    }

    @Override
    public void reset() {
        L.i(" -xb- reset");
        if (mMediaPlayer == null) return;
        mMediaPlayer.reset();
        mMediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
        setOptions();
    }

    @Override
    public boolean isPlaying() {
        L.i(" -xb- isPlaying");
        if (mMediaPlayer == null) return true;
        return mMediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(long time) {
        L.i(" -xb- seekTo:"+time);
        if (mMediaPlayer == null) return;
        if (mPlayerSettings.getLocalProxyEnable()) {
            mLocalProxyVideoControl.seekToCachePosition(time);
        }
        try {
            mMediaPlayer.seekTo((int) time);
        } catch (IllegalStateException e) {
            mPlayerEventListener.onError(ERR_SEEKTO);
        }
    }

    @Override
    public void release() {
        L.i(" -xb- release");
        if (mPlayerSettings.getLocalProxyEnable()) {
            mLocalProxyVideoControl.releaseLocalProxyResources();
        }
        if (mMediaPlayer == null) return;
        mMediaPlayer.setOnErrorListener(null);
        mMediaPlayer.setOnCompletionListener(null);
        mMediaPlayer.setOnInfoListener(null);
        mMediaPlayer.setOnBufferingUpdateListener(null);
        mMediaPlayer.setOnPreparedListener(null);
        mMediaPlayer.setOnVideoSizeChangedListener(null);
        new Thread() {
            @Override
            public void run() {
                try {
                    mMediaPlayer.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public long getCurrentPosition() {
        Long mCurrentPosition = mMediaPlayer.getCurrentPosition();
        L.i(" -xb- getCurrentPosition mCurrentPosition:"+mCurrentPosition);
        return mCurrentPosition;
    }

    @Override
    public long getDuration() {
        Long mDuration = mMediaPlayer.getDuration();
        L.i(" -xb- getDuration mDuration:"+mDuration);
        return mDuration;
    }

    @Override
    public int getBufferedPercentage() {
        return mBufferedPercent;
    }

    @Override
    public void setSurface(Surface surface) {
        mMediaPlayer.setSurface(surface);
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        mMediaPlayer.setDisplay(holder);
    }

    @Override
    public void setVolume(float v1, float v2) {
        L.i(" -xb- setVolume v1:"+v1+" v2"+v2);
        mMediaPlayer.setVolume(v1, v2);
    }

    @Override
    public void setLooping(boolean isLooping) {
        L.i(" -xb- setLooping isLooping:"+isLooping);
        mMediaPlayer.setLooping(isLooping);
    }

    @Override
    public void setSpeed(float speed) {
        L.i(" -xb- setSpeed speed:"+speed);
        mMediaPlayer.setSpeed(speed);
    }

    @Override
    public long getTcpSpeed() {
        return mMediaPlayer.getTcpSpeed();
    }

    private IMediaPlayer.OnErrorListener onErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int framework_err, int impl_err) {
            L.i("-xb- OnErrorListener i: "+framework_err + "  impl_err:"+impl_err);
            mPlayerEventListener.onError(framework_err);
            return true;
        }
    };

    private IMediaPlayer.OnCompletionListener onCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            L.i(" -xb- OnCompletionListener");
            mPlayerEventListener.onCompletion();
        }
    };

    private IMediaPlayer.OnInfoListener onInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
            L.i(" -xb- OnInfoListener what:"+what+" extra"+extra);
            mPlayerEventListener.onInfo(what, extra);
            return true;
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
            L.i(" -xb- OnBufferingUpdateListener percent:"+percent);
            mBufferedPercent = percent;
        }
    };


    private IMediaPlayer.OnPreparedListener onPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            L.i(" -xb- OnPreparedListener");
            mPlayerEventListener.onPrepared();
            if(initSeek > 0){
                seekTo(initSeek);
            }
        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
            L.i(" -xb- OnVideoSizeChangedListener");
            int videoWidth = iMediaPlayer.getVideoWidth();
            int videoHeight = iMediaPlayer.getVideoHeight();
            if (videoWidth != 0 && videoHeight != 0) {
                mPlayerEventListener.onVideoSizeChanged(videoWidth, videoHeight);
            }
        }
    };

    private IMediaPlayer.OnTimedTextListener onRendListener = new IMediaPlayer.OnTimedTextListener() {
        @Override
        public void onTimedText(IMediaPlayer mp, IjkTimedText text) {
//            L.i("-xb- OnTimedTextListener:"+text.getText());

            if(isrend%30 == 0){
                mPlayerEventListener.onRender();
                isrend = 0;
            }
            isrend++;
        }
    };

    public void notifyOnProxyCacheInfo(int msg, Map<String, Object> params) {
        VideoProxyThreadUtils.runOnUiThread(() -> {
            if (mOnProxyCacheInfoListener != null) {
                mOnProxyCacheInfoListener.onProxyCacheInfo(msg, params);
            }
            if (msg == ProxyMessage.MSG_VIDEO_PROXY_PROGRESS || msg == ProxyMessage.MSG_VIDEO_PROXY_COMPLETED) {
                mProxyCachePercent = VideoParamsUtils.getFloatValue(params, VideoParams.PERCENT);
            } else if (msg == ProxyMessage.MSG_VIDEO_PROXY_FORBIDDEN || msg == ProxyMessage.MSG_VIDEO_PROXY_ERROR) {
                mPlayerSettings.setLocalProxyEnable(false);
            }
        });
    }
}
