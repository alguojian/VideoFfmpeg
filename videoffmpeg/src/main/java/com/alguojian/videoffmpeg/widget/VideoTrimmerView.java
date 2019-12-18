package com.alguojian.videoffmpeg.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alguojian.videoffmpeg.LogUtils;
import com.alguojian.videoffmpeg.R;
import com.alguojian.videoffmpeg.VfMessageNotification;
import com.alguojian.videoffmpeg.VideoFfmpeg;
import com.alguojian.videoffmpeg.VideoUtils;
import com.alguojian.videoffmpeg.trim.IVideoTrimmerView;
import com.alguojian.videoffmpeg.trim.VfVideoTrimmerAdapter;
import com.alguojian.videoffmpeg.trim.VfVideoTrimmerUtil;
import com.alguojian.videoffmpeg.trim.VideoTrimmerActivity;

import io.reactivex.disposables.Disposable;

import static com.alguojian.videoffmpeg.trim.VfVideoTrimmerUtil.VIDEO_FRAMES_WIDTH;


public class VideoTrimmerView extends FrameLayout implements IVideoTrimmerView {

    private int mMaxWidth = VIDEO_FRAMES_WIDTH;
    private Context mContext;
    private RelativeLayout mLinearVideo;
    private ZVideoView mVideoView;
    private ProgressBar progressBar;
    private ImageView mPlayView;
    private RecyclerView mVideoThumbRecyclerView;
    private RangeSeekBarView mRangeSeekBarView;
    private LinearLayout mSeekBarLayout;
    private ImageView mRedProgressIcon;
    private TextView mVideoShootTipTv;
    private float mAverageMsPx;//每毫秒所占的px
    private float averagePxMs;//每px所占用的ms毫秒
    private Uri mSourceUri;
    private int mDuration = 0;
    private VfVideoTrimmerAdapter mVideoThumbAdapter;
    private boolean isFromRestore = false;
    //new
    private long mLeftProgressPos, mRightProgressPos;
    private long mRedProgressBarPos = 0;
    private long scrollPos = 0;
    private int mScaledTouchSlop;
    private int lastScrollX;
    private boolean isSeeking;
    private boolean isOverScaledTouchSlop;
    private int mThumbsTotalCount;
    private ValueAnimator mRedProgressAnimator;
    private Handler mAnimationHandler = new Handler();
    private Disposable disposable;


    public VideoTrimmerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoTrimmerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.vf_video_trimmer_view, this, true);

        mLinearVideo = findViewById(R.id.layout_surface_view);
        mVideoView = findViewById(R.id.video_loader);
        progressBar = findViewById(R.id.progressBar);
        mPlayView = findViewById(R.id.icon_video_play);
        mSeekBarLayout = findViewById(R.id.seekBarLayout);
        mRedProgressIcon = findViewById(R.id.positionIcon);
        mVideoShootTipTv = findViewById(R.id.video_shoot_tip);
        mVideoThumbRecyclerView = findViewById(R.id.video_frames_recyclerView);
        mVideoThumbRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mVideoThumbAdapter = new VfVideoTrimmerAdapter(mContext);
        mVideoThumbRecyclerView.setAdapter(mVideoThumbAdapter);
        mVideoThumbRecyclerView.addOnScrollListener(mOnScrollListener);
        setUpListeners();
    }

    private void initRangeSeekBarView() {
        if (mRangeSeekBarView != null) return;
        int rangeWidth;
        mLeftProgressPos = 0;
        if (mDuration <= VfVideoTrimmerUtil.MAX_SHOOT_DURATION) {
            mThumbsTotalCount = VfVideoTrimmerUtil.MAX_COUNT_RANGE;
            rangeWidth = mMaxWidth;
            mRightProgressPos = mDuration;
        } else {
            mThumbsTotalCount = (int) (mDuration * 1.0f / (VfVideoTrimmerUtil.MAX_SHOOT_DURATION * 1.0f) * VfVideoTrimmerUtil.MAX_COUNT_RANGE);
            rangeWidth = mMaxWidth / VfVideoTrimmerUtil.MAX_COUNT_RANGE * mThumbsTotalCount;
            mRightProgressPos = VfVideoTrimmerUtil.MAX_SHOOT_DURATION;
        }
        mVideoThumbRecyclerView.addItemDecoration(new SpacesItemDecoration(VfVideoTrimmerUtil.RECYCLER_VIEW_PADDING, mThumbsTotalCount));
        mRangeSeekBarView = new RangeSeekBarView(mContext, mLeftProgressPos, mRightProgressPos);
        mRangeSeekBarView.setSelectedMinValue(mLeftProgressPos);
        mRangeSeekBarView.setSelectedMaxValue(mRightProgressPos);
        mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos);
        mRangeSeekBarView.setMinShootTime(VfVideoTrimmerUtil.MIN_SHOOT_DURATION);
        mRangeSeekBarView.setNotifyWhileDragging(true);
        mRangeSeekBarView.setOnRangeSeekBarChangeListener(mOnRangeSeekBarChangeListener);
        mSeekBarLayout.addView(mRangeSeekBarView);

        mAverageMsPx = mDuration * 1.0f / rangeWidth * 1.0f;
        averagePxMs = (mMaxWidth * 1.0f / (mRightProgressPos - mLeftProgressPos));
    }

    public void initVideoByURI(final Uri videoURI) {
        mSourceUri = videoURI;
        mVideoView.setVideoURI(videoURI);
        mVideoView.requestFocus();
        mVideoShootTipTv.setText(String.format(mContext.getResources().getString(R.string.video_shoot_tip), VfVideoTrimmerUtil.VIDEO_MAX_TIME));
    }

    private void startShootVideoThumbs(final Context context, final Uri videoUri, int totalThumbsCount, long startPosition, long endPosition) {
        disposable = VfVideoTrimmerUtil.shootVideoThumbInBackground(context, videoUri, totalThumbsCount, startPosition, endPosition)
                .subscribe(it -> mVideoThumbAdapter.addBitmaps(it)
                );
    }

    private void onCancelClicked() {
        VideoTrimmerActivity activity = (VideoTrimmerActivity) mContext;
        activity.finish();
    }

    private void videoPrepared(MediaPlayer mp) {
        ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();
        int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();

        float videoProportion = (float) videoWidth / (float) videoHeight;
        int screenWidth = mLinearVideo.getWidth();
        int screenHeight = mLinearVideo.getHeight();

        if (videoHeight > videoWidth) {
            lp.width = screenWidth;
            lp.height = screenHeight;
        } else {
            lp.width = screenWidth;
            float r = videoHeight / (float) videoWidth;
            lp.height = (int) (lp.width * r);
        }
        mVideoView.setLayoutParams(lp);
        mDuration = mVideoView.getDuration();
        seekTo((int) mRedProgressBarPos);
        initRangeSeekBarView();
        startShootVideoThumbs(mContext, mSourceUri, mThumbsTotalCount, 0, mDuration);
    }

    private void videoCompleted() {
        seekTo(mLeftProgressPos);
        setPlayPauseViewIcon(false);
    }

    private void onVideoReset() {
        mVideoView.pause();
        setPlayPauseViewIcon(false);
    }

    private void playVideoOrPause() {
        mRedProgressBarPos = mVideoView.getCurrentPosition();
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
            pauseRedProgressAnimation();
        } else {
            mVideoView.start();
            playingRedProgressAnimation();
        }
        setPlayPauseViewIcon(mVideoView.isPlaying());
    }

    public void onVideoPause() {
        if (mVideoView.isPlaying()) {
            seekTo(mLeftProgressPos);//复位
            mVideoView.pause();
            setPlayPauseViewIcon(false);
            mRedProgressIcon.setVisibility(GONE);
        }
    }

    private void setUpListeners() {
        findViewById(R.id.cancelBtn).setOnClickListener(view -> onCancelClicked());
        findViewById(R.id.finishBtn).setOnClickListener(view -> onSaveClicked());
        mVideoView.setOnPreparedListener(mp -> {
            if (isFromRestore) return;
            mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            videoPrepared(mp);
        });
        mVideoView.setOnCompletionListener(mp -> videoCompleted());
        mPlayView.setOnClickListener(v -> playVideoOrPause());
    }

    private void onSaveClicked() {
        if (mSourceUri == null) {
            VideoFfmpeg.getVfVideoListener().onError();
            return;
        }
        progressBar.setVisibility(VISIBLE);
        mRedProgressBarPos = mVideoView.getCurrentPosition();
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
            pauseRedProgressAnimation();
            setPlayPauseViewIcon(mVideoView.isPlaying());
        }

        if (mRightProgressPos - mLeftProgressPos < VfVideoTrimmerUtil.MIN_SHOOT_DURATION) {
            VideoFfmpeg.startInterceptCover(mSourceUri.getPath(), VideoUtils.INSTANCE.getVideoInterceptImageOutPath(), false);
        } else {
            VideoFfmpeg.startCrop(mSourceUri.getPath(), VideoUtils.INSTANCE.getVideoCropOutPath(), mLeftProgressPos, mRightProgressPos, false);
        }
        if (VideoFfmpeg.INSTANCE.getVfFinishAllActivity())
            VfMessageNotification.INSTANCE().send(VideoUtils.start_video_operating_finish_activity);
    }

    private void seekTo(long msec) {
        if (mVideoView != null) {
            mVideoView.seekTo((int) msec);
            LogUtils.log("seekTo = " + msec);
        }
    }

    @Override
    public void onPause() {
        isFromRestore = true;
        progressBar.setVisibility(GONE);
    }

    @Override
    public void onResume() {
        seekTo(mRedProgressBarPos);
    }

    private void setPlayPauseViewIcon(boolean isPlaying) {
        mPlayView.setImageResource(isPlaying ? R.drawable.ic_video_pause_black : R.drawable.ic_video_play_black);
    }

    private final RangeSeekBarView.OnRangeSeekBarChangeListener mOnRangeSeekBarChangeListener = new RangeSeekBarView.OnRangeSeekBarChangeListener() {
        @Override
        public void onRangeSeekBarValuesChanged(RangeSeekBarView bar, long minValue, long maxValue, int action, boolean isMin,
                                                RangeSeekBarView.Thumb pressedThumb) {
            LogUtils.log("-----minValue----->>>>>>" + minValue);
            LogUtils.log("-----maxValue----->>>>>>" + maxValue);
            mLeftProgressPos = minValue + scrollPos;
            mRedProgressBarPos = mLeftProgressPos;
            mRightProgressPos = maxValue + scrollPos;
            LogUtils.log("-----mLeftProgressPos----->>>>>>" + mLeftProgressPos);
            LogUtils.log("-----mRightProgressPos----->>>>>>" + mRightProgressPos);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    isSeeking = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    isSeeking = true;
                    seekTo((int) (pressedThumb == RangeSeekBarView.Thumb.MIN ? mLeftProgressPos : mRightProgressPos));
                    break;
                case MotionEvent.ACTION_UP:
                    isSeeking = false;
                    seekTo((int) mLeftProgressPos);
                    break;
                default:
                    break;
            }
            mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos);
        }
    };

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            LogUtils.log("newState = " + newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            isSeeking = false;
            int scrollX = calcScrollXDistance();
            //达不到滑动的距离
            if (Math.abs(lastScrollX - scrollX) < mScaledTouchSlop) {
                isOverScaledTouchSlop = false;
                return;
            }
            isOverScaledTouchSlop = true;
            //初始状态,why ? 因为默认的时候有35dp的空白！
            if (scrollX == -VfVideoTrimmerUtil.RECYCLER_VIEW_PADDING) {
                scrollPos = 0;
            } else {
                isSeeking = true;
                scrollPos = (long) (mAverageMsPx * (VfVideoTrimmerUtil.RECYCLER_VIEW_PADDING + scrollX));
                mLeftProgressPos = mRangeSeekBarView.getSelectedMinValue() + scrollPos;
                mRightProgressPos = mRangeSeekBarView.getSelectedMaxValue() + scrollPos;

                LogUtils.log("onScrolled >>>> mLeftProgressPos = " + mLeftProgressPos);
                mRedProgressBarPos = mLeftProgressPos;
                if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    setPlayPauseViewIcon(false);
                }
                mRedProgressIcon.setVisibility(GONE);
                seekTo(mLeftProgressPos);
                mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos);
                mRangeSeekBarView.invalidate();
            }
            lastScrollX = scrollX;
        }
    };

    /**
     * 水平滑动了多少px
     */
    private int calcScrollXDistance() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mVideoThumbRecyclerView.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisibleChildView = layoutManager.findViewByPosition(position);
        int itemWidth = firstVisibleChildView.getWidth();
        return (position) * itemWidth - firstVisibleChildView.getLeft();
    }

    private void playingRedProgressAnimation() {
        pauseRedProgressAnimation();
        playingAnimation();
        mAnimationHandler.post(mAnimationRunnable);
    }

    private void playingAnimation() {
        if (mRedProgressIcon.getVisibility() == View.GONE) {
            mRedProgressIcon.setVisibility(View.VISIBLE);
        }
        final LayoutParams params = (LayoutParams) mRedProgressIcon.getLayoutParams();
        int start = (int) (VfVideoTrimmerUtil.RECYCLER_VIEW_PADDING + (mRedProgressBarPos - scrollPos) * averagePxMs);
        int end = (int) (VfVideoTrimmerUtil.RECYCLER_VIEW_PADDING + (mRightProgressPos - scrollPos) * averagePxMs);
        mRedProgressAnimator = ValueAnimator.ofInt(start, end).setDuration((mRightProgressPos - scrollPos) - (mRedProgressBarPos - scrollPos));
        mRedProgressAnimator.setInterpolator(new LinearInterpolator());
        mRedProgressAnimator.addUpdateListener(animation -> {
            params.leftMargin = (int) animation.getAnimatedValue();
            mRedProgressIcon.setLayoutParams(params);
            LogUtils.log("----onAnimationUpdate--->>>>>>>" + mRedProgressBarPos);
        });
        mRedProgressAnimator.start();
    }

    private void pauseRedProgressAnimation() {
        mRedProgressIcon.clearAnimation();
        if (mRedProgressAnimator != null && mRedProgressAnimator.isRunning()) {
            mAnimationHandler.removeCallbacks(mAnimationRunnable);
            mRedProgressAnimator.cancel();
        }
    }

    private Runnable mAnimationRunnable = this::updateVideoProgress;

    private void updateVideoProgress() {
        long currentPosition = mVideoView.getCurrentPosition();
        LogUtils.log("updateVideoProgress currentPosition = " + currentPosition);
        if (currentPosition >= (mRightProgressPos)) {
            mRedProgressBarPos = mLeftProgressPos;
            pauseRedProgressAnimation();
            onVideoPause();
        } else {
            mAnimationHandler.post(mAnimationRunnable);
        }
    }

    @Override
    public void onDestroy() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
