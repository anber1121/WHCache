package com.jeffmony.sample.tiktok;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

import com.jeffmony.playersdk.common.PlayerSettings;
import com.jeffmony.sample.R;
import com.tencent.videoplayer.player.VideoView;

import java.util.ArrayList;
import java.util.List;


/**
 * 模仿抖音短视频，使用VerticalViewPager实现，实现了预加载功能
 * Created by abner on 2019/10/13.
 */

public class TikTok2Activity extends BaseActivity<IjkVideoView> {

    /**
     * 当前播放位置
     */
    private int mCurPos;
    private List<TiktokBean> mVideoList = new ArrayList<>();
    private Tiktok2Adapter mTiktok2Adapter;
    private VerticalViewPager mViewPager;

    private TikTokController mController;

    private static final String KEY_INDEX = "index";

    public static void start(Context context, int index) {
        Intent i = new Intent(context, TikTok2Activity.class);
        i.putExtra(KEY_INDEX, index);
        context.startActivity(i);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_tiktok2;
    }

    @Override
    protected int getTitleResId() {
        return R.string.str_tiktok;
    }

    @Override
    protected void initView() {
        super.initView();
        setStatusBarTransparent();
        initViewPager();
        initVideoView();

        addData(null);
        Intent extras = getIntent();
        int index = extras.getIntExtra(KEY_INDEX, 0);
        mViewPager.setCurrentItem(index);

        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                startPlay(index);
            }
        });
    }

    private void initVideoView() {
        mVideoView = new IjkVideoView(this);
//        PlayerSettings playerSettings = new PlayerSettings();
//        playerSettings.setLocalProxyEnable(true);
//        mVideoView.initPlayerSettings(playerSettings);
        mVideoView.setLooping(true);
        mVideoView.setScreenScaleType(VideoView.SCREEN_SCALE_CENTER_CROP);

        mController = new TikTokController(this);
        mVideoView.setVideoController(mController);
    }

    private void initViewPager() {
        mViewPager = findViewById(R.id.vvp);
        mViewPager.setOffscreenPageLimit(4);
        mTiktok2Adapter = new Tiktok2Adapter(mVideoList);
        mViewPager.setAdapter(mTiktok2Adapter);
        mViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            private int mCurItem;

            /**
             * VerticalViewPager是否反向滑动
             */
            private boolean mIsReverseScroll;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (position == mCurItem) {
                    return;
                }
                mIsReverseScroll = position < mCurItem;
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == mCurPos) return;
                startPlay(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == VerticalViewPager.SCROLL_STATE_DRAGGING) {
                    mCurItem = mViewPager.getCurrentItem();
                }
            }
        });
    }

    private void startPlay(int position) {
        int count = mViewPager.getChildCount();
        for (int i = 0; i < count; i ++) {
            View itemView = mViewPager.getChildAt(i);
            Tiktok2Adapter.ViewHolder viewHolder = (Tiktok2Adapter.ViewHolder) itemView.getTag();
            if (viewHolder.mPosition == position) {
                mVideoView.release();
                Utils.removeViewFormParent(mVideoView);

                TiktokBean tiktokBean = mVideoList.get(position);
                mVideoView.setUrl(tiktokBean.videoDownloadUrl);
                mController.addControlComponent(viewHolder.mTikTokView, true);
                viewHolder.mPlayerContainer.addView(mVideoView, 0);
                mVideoView.start();
                mCurPos = position;
                break;
            }
        }
    }

    public void addData(View view) {
        mVideoList.addAll(DataUtil.getTiktokDataFromAssets(this));
        mTiktok2Adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
