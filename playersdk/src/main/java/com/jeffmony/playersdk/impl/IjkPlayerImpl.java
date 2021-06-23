package com.jeffmony.playersdk.impl;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import com.jeffmony.playersdk.common.SeekType;
import com.jeffmony.videocache.common.CacheType;
import com.jeffmony.videocache.utils.ProxyCacheUtils;

import java.io.IOException;
import java.util.Map;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class IjkPlayerImpl extends BasePlayerImpl {
    private static final String TAG = "IjkPlayerImpl";

    private IjkMediaPlayer mIjkPlayer;

    public IjkPlayerImpl(Context context) {
        super(context);
        mIjkPlayer = new IjkMediaPlayer();
        mIjkPlayer.setLogEnabled(true);
        //不用MediaCodec编解码
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        //不用opensles编解码
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 10000000);
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 16);

        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "volume", 100);
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "reconnect", 1);
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "af", "loudnorm");
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "vn", 1);
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "nodisp", 1);
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sn", 1);
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek");
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L);
        mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);

        mIjkPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        initPlayerListeners();
    }

    private void initPlayerListeners() {
        mIjkPlayer.setOnPreparedListener(mOnPreparedListener);
        mIjkPlayer.setOnTimedTextListener(onRendListener);
        mIjkPlayer.setOnErrorListener(mOnErrorListener);
        mIjkPlayer.setOnInfoListener(mOnInfoListener);
        mIjkPlayer.setOnCompletionListener(mCompletionListener);
        mIjkPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
    }

    @Override
    public void setDataSource(Context context, Uri uri, int cacheMode, String albumId, String trackId, Map<String, String> headers) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        String playUrl;
        if (mPlayerSettings.getLocalProxyEnable()) {
            playUrl = ProxyCacheUtils.getProxyUrl(uri.toString(), null, null);
            //请求放在客户端,非常便于控制
            mLocalProxyVideoControl.startRequestVideoInfo(uri.toString(), cacheMode, albumId, trackId, null, null);
        } else {
            playUrl = uri.toString();
        }
        Log.e("IjkPlayerImpl","playUrl:"+playUrl);
        mIjkPlayer.setDataSource(context, Uri.parse(playUrl), headers);
    }

    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        String playUrl;
        if (mPlayerSettings.getLocalProxyEnable()) {
            playUrl = ProxyCacheUtils.getProxyUrl(uri.toString(), null, null);
            //请求放在客户端,非常便于控制
            mLocalProxyVideoControl.startRequestVideoInfo(uri.toString(),null, null);
        } else {
            playUrl = uri.toString();
        }
        Log.e("IjkPlayerImpl","playUrl:"+playUrl);
        mIjkPlayer.setDataSource(context, Uri.parse(playUrl), headers);
    }

    @Override
    public void setSeekType(SeekType type) {
        if (type == SeekType.EXACT_SYNC) {
            mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        } else {
            mIjkPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 0);
        }
    }

    @Override
    public void setSurface(Surface surface) {
        mIjkPlayer.setSurface(surface);
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        mIjkPlayer.prepareAsync();
    }

    @Override
    public void start() throws IllegalStateException {
        if (mPlayerSettings.getLocalProxyEnable()) {
            mLocalProxyVideoControl.resumeLocalProxyTask();
        }
        mIjkPlayer.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        mIjkPlayer.stop();
    }

    @Override
    public void setVolume(float v1, float v2) throws IllegalStateException {
        mIjkPlayer.setVolume(v1, v2);
    }

    @Override
    public void pause() throws IllegalStateException {
        if (mPlayerSettings.getLocalProxyEnable()) {
            mLocalProxyVideoControl.pauseLocalProxyTask();
        }
        mIjkPlayer.pause();
    }

    @Override
    public void setSpeed(float speed) {
        mIjkPlayer.setSpeed(speed);
    }

    @Override
    public long getCurrentPosition() {
        return mIjkPlayer.getCurrentPosition();
    }

    @Override
    public long getBufferedPosition() {
        if (mPlayerSettings.getLocalProxyEnable()) {
            return (long) (mProxyCachePercent * mIjkPlayer.getDuration() / 100);
        }
        return 0;
    }

    @Override
    public long getDuration() {
        return mIjkPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mIjkPlayer.isPlaying();
    }

    @Override
    public void reset() {
        mIjkPlayer.reset();
    }

    @Override
    public void release() {
        if (mPlayerSettings.getLocalProxyEnable()) {
            mLocalProxyVideoControl.releaseLocalProxyResources();
        }
        mIjkPlayer.release();
    }

    @Override
    public void seekTo(long position) throws IllegalStateException {
        if (mPlayerSettings.getLocalProxyEnable()) {
            mLocalProxyVideoControl.seekToCachePosition(position);
        }
        mIjkPlayer.seekTo(position);
    }

    private IjkMediaPlayer.OnPreparedListener mOnPreparedListener = mp -> notifyOnPrepared();

    private IjkMediaPlayer.OnCompletionListener mCompletionListener = mp -> notifyOnCompletion();

    private IjkMediaPlayer.OnInfoListener mOnInfoListener = (mp, infoCode, msg) -> notifyOnInfo(infoCode, msg);

    private IjkMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = (mp, width, height, sar_num, sar_den) -> notifyOnVideoSizeChanged(width, height, sar_num, sar_den, 0);

    private IjkMediaPlayer.OnVideoDarSizeChangedListener mOnVideoDarSizeChangedListener = (mp, width, height, sar_num, sar_den, dar_num, dar_den) -> {
        float pixelRatio = sar_num * 1.0f / sar_den;
        if (Float.compare(pixelRatio, Float.NaN) == 0) {
            pixelRatio = 1.0f;
        }
        float darRatio = dar_num * 1.0f / dar_den;
        notifyOnVideoSizeChanged(width, height, 0, pixelRatio, darRatio);
    };

    private IjkMediaPlayer.OnErrorListener mOnErrorListener = (mp, what, extra) -> {
        notifyOnError(what, "" + extra);
        return true;
    };

    private IjkMediaPlayer.OnTimedTextListener onRendListener = (mp, text) -> notifyOnRend(text);
}
