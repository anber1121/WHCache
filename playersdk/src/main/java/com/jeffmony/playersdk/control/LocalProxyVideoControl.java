package com.jeffmony.playersdk.control;

import androidx.annotation.NonNull;

import com.jeffmony.playersdk.impl.BasePlayerImpl;
import com.jeffmony.videocache.VideoProxyCacheManager;
import com.jeffmony.videocache.common.CacheType;
import com.jeffmony.videocache.common.ProxyMessage;
import com.jeffmony.videocache.common.VideoParams;
import com.jeffmony.videocache.listener.IVideoCacheListener;
import com.jeffmony.videocache.model.VideoCacheInfo;
import com.jeffmony.videocache.utils.LogUtils;
import com.jeffmony.videocache.utils.ProxyCacheUtils;

import java.util.HashMap;
import java.util.Map;

public class LocalProxyVideoControl {

    private static final String TAG = "LocalProxyVideoControl";

    private BasePlayerImpl mPlayer;
    private String mVideoUrl;

    private IVideoCacheListener mListener = new IVideoCacheListener() {
        @Override
        public void onCacheStart(VideoCacheInfo cacheInfo) {
            LogUtils.i(TAG, "onCacheStart");
        }

        @Override
        public void onCacheProgress(VideoCacheInfo cacheInfo) {
            LogUtils.i(TAG, "onCacheProgress:"+cacheInfo.getPercent());
            Map<String, Object> params = new HashMap<>();
            params.put(VideoParams.PERCENT, cacheInfo.getPercent());
            params.put(VideoParams.CACHE_SIZE, cacheInfo.getCachedSize());
            mPlayer.notifyOnProxyCacheInfo(ProxyMessage.MSG_VIDEO_PROXY_PROGRESS, params);
        }

        @Override
        public void onCacheError(VideoCacheInfo cacheInfo, int errorCode) {
            LogUtils.i(TAG, "onCacheError");
        }

        @Override
        public void onCacheForbidden(VideoCacheInfo cacheInfo) {
            LogUtils.i(TAG, "onCacheForbidden");
        }

        @Override
        public void onCacheFinished(VideoCacheInfo cacheInfo) {
            LogUtils.i(TAG, "onCacheFinished:"+cacheInfo.getTotalSize());
            Map<String, Object> params = new HashMap<>();
            params.put(VideoParams.PERCENT, 100f);
            params.put(VideoParams.TOTAL_SIZE, cacheInfo.getTotalSize());
            mPlayer.notifyOnProxyCacheInfo(ProxyMessage.MSG_VIDEO_PROXY_COMPLETED, params);
        }
    };

    public LocalProxyVideoControl(@NonNull BasePlayerImpl player) {
        mPlayer = player;
    }

    public void startRequestVideoInfo(String videoUrl, int cacheMode, String albumId, Map<String, String> headers, Map<String, Object> extraParams) {
        mVideoUrl = videoUrl;
        VideoProxyCacheManager.getInstance().addCacheListener(videoUrl, mListener);
        VideoProxyCacheManager.getInstance().setPlayingUrlMd5(ProxyCacheUtils.computeMD5(videoUrl));
        VideoProxyCacheManager.getInstance().startRequestVideoInfo(videoUrl, cacheMode, albumId, headers, extraParams);
    }

    public void startRequestVideoInfo(String videoUrl, Map<String, String> headers, Map<String, Object> extraParams) {
        mVideoUrl = videoUrl;
        VideoProxyCacheManager.getInstance().addCacheListener(videoUrl, mListener);
        VideoProxyCacheManager.getInstance().setPlayingUrlMd5(ProxyCacheUtils.computeMD5(videoUrl));
        VideoProxyCacheManager.getInstance().startRequestVideoInfo(videoUrl, CacheType.UNKNOWN, null, headers, extraParams);
    }

    public void pauseLocalProxyTask() {
        LogUtils.i(TAG, "pauseLocalProxyTask");
        VideoProxyCacheManager.getInstance().pauseCacheTask(mVideoUrl);
    }

    public void resumeLocalProxyTask() {
        LogUtils.i(TAG, "resumeLocalProxyTask");
        VideoProxyCacheManager.getInstance().resumeCacheTask(mVideoUrl);
    }

    public void seekToCachePosition(long position) {
        long totalDuration = mPlayer.getDuration();
        if (totalDuration > 0) {
            float percent = position * 1.0f / totalDuration;
            VideoProxyCacheManager.getInstance().seekToCacheTaskFromClient(mVideoUrl, percent);
        }
    }

    public void releaseLocalProxyResources() {
        VideoProxyCacheManager.getInstance().stopCacheTask(mVideoUrl);   //停止视频缓存任务
        VideoProxyCacheManager.getInstance().releaseProxyReleases(mVideoUrl);
    }
}
