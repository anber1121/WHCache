package com.jeffmony.videocache.task;

import com.jeffmony.videocache.m3u8.M3U8;
import com.jeffmony.videocache.model.VideoCacheInfo;

import java.util.Map;

public class M3U8CacheTask extends VideoCacheTask {

    public M3U8CacheTask(VideoCacheInfo cacheInfo, Map<String, String> headers, M3U8 m3u8) {
        super(cacheInfo, headers);
    }

    @Override
    public void startCacheTask() {

    }

    @Override
    public void pauseCacheTask() {

    }

    @Override
    public void resumeCacheTask() {

    }
}
