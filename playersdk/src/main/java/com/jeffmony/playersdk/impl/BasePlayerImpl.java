package com.jeffmony.playersdk.impl;

import android.content.Context;
import android.net.Uri;
import android.os.Message;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.jeffmony.videocache.utils.LogUtils;
import com.jeffmony.playersdk.IPlayer;
import com.jeffmony.playersdk.common.PlayerSettings;
import com.jeffmony.playersdk.common.SeekType;
import com.jeffmony.playersdk.control.LocalProxyVideoControl;
import com.jeffmony.videocache.common.ProxyMessage;
import com.jeffmony.videocache.common.VideoParams;
import com.jeffmony.videocache.utils.VideoParamsUtils;
import com.jeffmony.videocache.utils.VideoProxyThreadUtils;

import java.io.IOException;
import java.util.Map;

import tv.danmaku.ijk.media.player.IjkTimedText;

public abstract class BasePlayerImpl {
    private static final String TAG = "BasePlayerImpl";
    protected Context mContext;
    private IPlayer.OnPreparedListener mOnPreparedListener;
    private IPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private IPlayer.OnErrorListener mOnErrorListener;
    private IPlayer.OnRendListener mOnRendListener;
    private IPlayer.OnLogListener mOnLogListener;
    private IPlayer.OnInfoListener mOnInfoListener;
    private IPlayer.OnCompletionListener mOnCompletionListener;
    private IPlayer.OnProxyCacheInfoListener mOnProxyCacheInfoListener;

    protected LocalProxyVideoControl mLocalProxyVideoControl;
    protected PlayerSettings mPlayerSettings;
    protected boolean mIsM3U8 = false;
    protected float mProxyCachePercent = 0f;

    public BasePlayerImpl(Context context) {
        mContext = context.getApplicationContext();
        mLocalProxyVideoControl = new LocalProxyVideoControl(this);
    }

    public void initPlayerSettings(@NonNull PlayerSettings settings) {
        mPlayerSettings = settings;
    }

    public abstract void setDataSource(Context context, Uri uri, Map<String, String> headers)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    public abstract void setDataSource(Context context, Uri uri, int cacheMode, String albumId, String trackId, Map<String, String> headers)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    public abstract void setSeekType(SeekType type);

    public abstract void setSurface(Surface surface);

    public abstract void prepareAsync() throws IllegalStateException;

    public abstract void start() throws IllegalStateException;

    public abstract void stop() throws IllegalStateException;

    public abstract void setVolume(float v1, float v2) throws IllegalStateException;

    public abstract void pause() throws IllegalStateException;

    public abstract void setSpeed(float speed);

    public abstract long getCurrentPosition();

    public abstract long getBufferedPosition();

    public abstract long getDuration();

    public abstract boolean isPlaying();

    public abstract void reset();

    public abstract void release();

    public abstract void seekTo(long msec) throws IllegalStateException;

    public void setOnPreparedListener(IPlayer.OnPreparedListener listener) {
        mOnPreparedListener = listener;
    }

    public void setOnVideoSizeChangedListener(IPlayer.OnVideoSizeChangedListener listener) {
        mOnVideoSizeChangedListener = listener;
    }

    public void setOnErrorListener(IPlayer.OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    public void setOnRendListener(IPlayer.OnRendListener listener) {
        mOnRendListener = listener;
    }

    public void setOnLogListener(IPlayer.OnLogListener listener) {
        mOnLogListener = listener;
    }

    public void setOnInfoListener(IPlayer.OnInfoListener listener) {
        mOnInfoListener = listener;
    }

    public void setOnCompletionListener(IPlayer.OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    public void setOnProxyCacheInfoListener(IPlayer.OnProxyCacheInfoListener listener) {
        mOnProxyCacheInfoListener = listener;
    }

    protected void notifyOnPrepared() {
        VideoProxyThreadUtils.runOnUiThread(() -> {
            if (mOnPreparedListener != null) {
                LogUtils.i(TAG,"notifyOnPrepared mOnPreparedListener!=null");
                mOnPreparedListener.onPrepared();
            }
        });
    }

    protected void notifyOnCompletion() {
        VideoProxyThreadUtils.runOnUiThread(() -> {
            if (mOnCompletionListener != null) {
                LogUtils.i(TAG,"mOnCompletionListener mOnCompletionListener!=null");
                mOnCompletionListener.onCompletion();
            }
        });
    }

    protected void notifyOnVideoSizeChanged(int width, int height,
                                            int rotationDegree,
                                            float pixelRatio,
                                            float darRatio) {
        VideoProxyThreadUtils.runOnUiThread(() -> {
            if (mOnVideoSizeChangedListener != null) {
                mOnVideoSizeChangedListener.onVideoSizeChanged(width, height, rotationDegree, pixelRatio, darRatio);
            }
        });
    }

    protected void notifyOnError(int what, String msg) {
        VideoProxyThreadUtils.runOnUiThread(() -> {
            if (mOnErrorListener != null) {
                LogUtils.i(TAG,"notifyOnError mOnErrorListener!=null");
                mOnErrorListener.onError(what, msg);
            }
        });
    }

    protected void notifyOnRend(IjkTimedText text) {
        VideoProxyThreadUtils.runOnUiThread(() -> {
            if (mOnRendListener != null) {
                mOnRendListener.onRend(text.getText());
            }
        });
    }

    protected void notifyOnLog(Message msg) {
        VideoProxyThreadUtils.runOnUiThread(() -> {
            if (mOnLogListener != null) {
                mOnLogListener.onLog(msg);
            }
        });
    }

    protected Boolean notifyOnInfo(int infoCode, int msg) {
        VideoProxyThreadUtils.runOnUiThread(() -> {
            if (mOnInfoListener != null) {
                LogUtils.i(TAG,"notifyOnInfo mOnInfoListener!=null");
                mOnInfoListener.onInfo(infoCode, msg);
            }
        });
        return true;
    }

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
