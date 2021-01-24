package com.jeffmony.videocache.proxy;

import com.jeffmony.videocache.common.VideoCacheConfig;
import com.jeffmony.videocache.socket.SocketProcessTask;
import com.jeffmony.videocache.utils.LogUtils;
import com.jeffmony.videocache.utils.ProxyCacheUtils;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author jeffmony
 * 本地代理服务端类
 */

public class VideoLocalProxyServer {

    private static final String TAG = "LocalProxyCacheServer";

    private final ExecutorService mSocketPool = Executors.newFixedThreadPool(8);
    private final VideoCacheConfig mConfig;

    private ServerSocket mLocalServer;
    private Thread mRequestThread;
    private int mPort;

    public VideoLocalProxyServer(VideoCacheConfig config) {
        mConfig = config;
        try {
            InetAddress address = InetAddress.getByName(ProxyCacheUtils.LOCAL_PROXY_HOST);
            mLocalServer = new ServerSocket(0, 8, address);
            this.mPort = mLocalServer.getLocalPort();
            mConfig.setPort(mPort);
            CountDownLatch startSignal = new CountDownLatch(1);
            WaitSocketRequestsTask task = new WaitSocketRequestsTask(startSignal);
            mRequestThread = new Thread(task);
            mRequestThread.setName("LocalProxyServerThread");
            mRequestThread.start();
            startSignal.await();
        } catch (Exception e) {
            shutdown();
            LogUtils.w(TAG,"Cannot create serverSocket, exception=" + e);
        }
    }

    private class WaitSocketRequestsTask implements Runnable {

        private CountDownLatch mLatch;

        public WaitSocketRequestsTask(CountDownLatch latch) {
            mLatch = latch;
        }

        @Override
        public void run() {
            mLatch.countDown();
            initSocketProcessor();
        }
    }

    private void initSocketProcessor() {
        do {
            try {
                Socket socket = mLocalServer.accept();
                if (mConfig.getConnTimeOut() > 0)
                    socket.setSoTimeout(mConfig.getConnTimeOut());
                mSocketPool.submit(new SocketProcessTask(socket, mConfig));
            } catch (Exception e) {
                LogUtils.w(TAG, "WaitRequestsRun ServerSocket accept failed, exception=" + e);
            }
        } while (!mLocalServer.isClosed());
    }

    private void shutdown() {
        if (mLocalServer != null) {
            try {
                mLocalServer.close();
            } catch (Exception e) {
                LogUtils.w(TAG,"ServerSocket close failed, exception=" + e);
            } finally {
                mSocketPool.shutdown();
                if (mRequestThread != null && mRequestThread.isAlive()) {
                    mRequestThread.interrupt();
                }
            }
        }
    }
}
