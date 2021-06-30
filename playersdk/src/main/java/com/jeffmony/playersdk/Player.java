package com.jeffmony.playersdk;

import android.content.Context;
import android.net.Uri;
import android.view.Surface;

import com.jeffmony.playersdk.common.PlayerSettings;
import com.jeffmony.playersdk.common.PlayerType;
import com.jeffmony.playersdk.common.SeekType;
import com.jeffmony.playersdk.impl.BasePlayerImpl;
import com.jeffmony.playersdk.impl.ExoPlayerImpl;
import com.jeffmony.playersdk.impl.IjkPlayerImpl;

import java.io.IOException;
import java.util.Map;

public class Player implements IPlayer {

    private BasePlayerImpl mPlayerImpl;

    public Player(Context context) {
        this(context, PlayerType.IJK_PLAYER);
    }

    public Player(Context context, PlayerType type) {
        if (type == PlayerType.EXO_PLAYER) {
            mPlayerImpl = new ExoPlayerImpl(context);
        } else {
            mPlayerImpl = new IjkPlayerImpl(context);
        }
    }

    @Override
    public void initPlayerSettings(PlayerSettings settings) {
        mPlayerImpl.initPlayerSettings(settings);
    }

    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mPlayerImpl.setDataSource(context, uri, headers);
    }

    @Override
    public void setDataSource(Context context, Uri uri, int cacheMode, String albumId, String trackId, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mPlayerImpl.setDataSource(context, uri, cacheMode, albumId, trackId, headers);
    }

    @Override
    public void setSeekType(SeekType type) {
        mPlayerImpl.setSeekType(type);
    }

    @Override
    public void setSurface(Surface surface) {
        mPlayerImpl.setSurface(surface);
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        mPlayerImpl.prepareAsync();
    }

    @Override
    public void start() throws IllegalStateException {
        mPlayerImpl.start();
    }

    @Override
    public void stop() throws IllegalStateException {
        mPlayerImpl.stop();
    }

    @Override
    public void setVolume(float v1, float v2) throws IllegalStateException {
        mPlayerImpl.setVolume(v1, v2);
    }

    @Override
    public void pause() throws IllegalStateException {
        mPlayerImpl.pause();
    }

    @Override
    public void setSpeed(float speed) {
        mPlayerImpl.setSpeed(speed);
    }

    @Override
    public long getCurrentPosition() {
        return mPlayerImpl.getCurrentPosition();
    }

    @Override
    public long getBufferedPosition() {
        return mPlayerImpl.getBufferedPosition();
    }

    @Override
    public long getDuration() {
        return mPlayerImpl.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mPlayerImpl.isPlaying();
    }

    @Override
    public void reset() {
        mPlayerImpl.reset();
    }

    @Override
    public void release() {
        mPlayerImpl.release();
    }

    @Override
    public void seekTo(long msec) throws IllegalStateException {
        mPlayerImpl.seekTo(msec);
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        mPlayerImpl.setOnPreparedListener(listener);
    }

    @Override
    public void setOnLogListener(OnLogListener listener) {
        mPlayerImpl.setOnLogListener(listener);
    }

    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
        mPlayerImpl.setOnVideoSizeChangedListener(listener);
    }

    @Override
    public void setOnErrorListener(OnErrorListener listener) {
        mPlayerImpl.setOnErrorListener(listener);
    }

    @Override
    public void setOnRendListener(OnRendListener listener) {
        mPlayerImpl.setOnRendListener(listener);
    }

    @Override
    public void setOnInfoListener(OnInfoListener listener) {
        mPlayerImpl.setOnInfoListener(listener);
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        mPlayerImpl.setOnCompletionListener(listener);
    }

    @Override
    public void setOnProxyCacheInfoListener(OnProxyCacheInfoListener listener) {
        mPlayerImpl.setOnProxyCacheInfoListener(listener);
    }
}
