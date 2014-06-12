/*
 * Copyright (C) 2014  The Android Open Source Project.
 *
 *		yinglovezhuzhu@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.opensource.pullview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Scroller;

/**
 * Usage A Custom ListView can be pull to refresh and load more<br>
 * <p>Off by default pull-to-refresh and load more, but turn them on when<br>
 * call {@link #setOnRefreshListener(OnRefreshListener)} and {@link #setOnLoadMoreListener(OnLoadMoreListener)}<br><br>
 * 
 * <p>Pull-to-refresh and load-more can not doing at the same time.<br>
 * it would be stop Pull-to-refresh when start load-more.<br>
 * Similarly, it would be stop load-more when start Pull-to-refresh.<br><br>
 * 
 * <p>You need to call {@link #refreshComplete()} when refresh thread finished,<br>
 * Similarly, You also need to call {@link #loadMoreComplete(boolean)} when load thread finished.<br>
 * 
 * @author yinglovezhuzhu@gmail.com
 */
public class PullListView extends ListView implements
		AbsListView.OnScrollListener {

	/** The Constant SCROLLBACK_HEADER. */
	private final static int SCROLLBACK_HEADER = 0;

	/** The Constant SCROLLBACK_FOOTER. */
	private final static int SCROLLBACK_FOOTER = 1;

	/** The Constant SCROLL_DURATION. */
	private final static int SCROLL_DURATION = 200;

	/** The Constant OFFSET_RADIO. */
	private final static float OFFSET_RADIO = 1.8f;

	/** The last y position. */
	private float mLastY = -1;

	/** The scroller. */
	private Scroller mScroller;

	/** The listener to listen refresh action */
	private OnRefreshListener mRefreshListener;

	/** The listener to listen load more action */
	private OnLoadMoreListener mLoadMoreListener;

	/** The header view. */
	private PullHeaderView mHeaderView;

	/** The footer view. */
	private PullFooterView mFooterView;

	/** The height of header view. */
	private int mHeaderViewHeight;

	/** The height of footer view. */
	private int mFooterViewHeight;

	/** The flag enable pull refresh. */
	private boolean mEnablePullRefresh = false;

	/** The flag enable pull load. */
	private boolean mEnablePullLoad = false;

	/** The flag pull refreshing. */
	private boolean mPullRefreshing = false;

	/** The flag pull loading. */
	private boolean mPullLoading = false;

	/** The flag is footer ready. */
	private boolean mIsFooterReady = false;

	/** The item count */
	private int mTotalItemCount;

	/** The m scroll back. */
	private int mScrollBack;

	/**
	 * Constructor
	 * 
	 * @param context the context
	 */
	public PullListView(Context context) {
		super(context);
		initView(context);
	}

	/**
	 * Constructor
	 * 
	 * @param context the context
	 * @param attrs the attrs
	 */
	public PullListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mTotalItemCount = totalItemCount;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (mIsFooterReady == false) {
			mIsFooterReady = true;
			mFooterView.setGravity(Gravity.TOP);
			addFooterView(mFooterView);
		}
		super.setAdapter(adapter);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float deltaY = ev.getRawY() - mLastY;
			mLastY = ev.getRawY();
			if (mEnablePullRefresh && getFirstVisiblePosition() == 0
					&& (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
				updateHeaderView(deltaY / OFFSET_RADIO);
			} else if (mEnablePullLoad && !mPullLoading
					&& getLastVisiblePosition() == mTotalItemCount - 1
					&& deltaY < 0) {
				startLoadMore();
			}
			break;
		case MotionEvent.ACTION_UP:
			mLastY = -1;
			if (getFirstVisiblePosition() == 0) {
				if (mEnablePullRefresh
						&& mHeaderView.getVisiableHeight() >= mHeaderViewHeight) {
					startRefresh();
				}

				if (mEnablePullRefresh) {
					refreshHeaderViewState();
				}
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (mScrollBack == SCROLLBACK_HEADER) {
				mHeaderView.setVisiableHeight(mScroller.getCurrY());
			}
			postInvalidate();
		}
		super.computeScroll();
	}
	
	/**
	 * Stop refresh and update header view.
	 */
	public void refreshComplete() {
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			refreshHeaderViewState();
		}

		int count = getCount() - getHeaderViewsCount() - getFooterViewsCount();
		if (count > 0) {
			mFooterView.setState(PullFooterView.STATE_READY);
		} else {
			mFooterView.setState(PullFooterView.STATE_EMPTY);
		}
	}

	/**
	 * Start to load more
	 */
	private void startLoadMore() {
		Log.d("TAG", "startLoadMore");
		mFooterView.show();
		if(mPullLoading) {
			//In the process of preventing load again when it was loading. 
			return;
		}
		interruptPull(); //Stop refresh
		mFooterView.setState(PullFooterView.STATE_LOADING);
		if (null != mLoadMoreListener) {
			mLoadMoreListener.onLoadMore();
		}
		mPullLoading = true;
	}

	/**
	 * Stop load more and rest footer view.<br>
	 * <p>You can set the flag to control whether it can load more<br>
	 * 
	 * @param hasMore Whether it can load more.
	 */
	public void loadMoreComplete(boolean hasMore) {
		mFooterView.hide();
		mPullLoading = false;
		if (hasMore) {
			mFooterView.setState(PullFooterView.STATE_READY);
		} else {
			mFooterView.setState(PullFooterView.STATE_NO);
		}
	}
	
	/**
	 * Interrupt loading data.(include refresh and load more)
	 */
	public void interruptPull() {
		if(mPullRefreshing) {
			if(null != mRefreshListener) {
				mRefreshListener.onInterrupt();
			}
			refreshComplete();
		}
		if(mPullLoading) {
			if(null != mLoadMoreListener) {
				mLoadMoreListener.onInterrupt();
			}
			loadMoreComplete(true);
		}
	}

	/**
	 * Set refresh listener.
	 * 
	 * @param listener
	 */
	public void setOnRefreshListener(OnRefreshListener listener) {
		this.mRefreshListener = listener;
		setPullRefreshEnable(null != listener);
	}

	/**
	 * Set load more listener
	 * 
	 * @param listener
	 */
	public void setOnLoadMoreListener(OnLoadMoreListener listener) {
		this.mLoadMoreListener = listener;
		setPullLoadEnable(null != listener);
	}

	/**
	 * 
	 * Get header view
	 * 
	 * @return
	 * @throws
	 */
	public PullHeaderView getHeaderView() {
		return mHeaderView;
	}

	/**
	 * 
	 * Get footer view
	 * 
	 * @return
	 * @throws
	 */
	public PullFooterView getFooterView() {
		return mFooterView;
	}

	/**
	 * Show header view.
	 */
	public void showHeader() {
		mHeaderView.setVisiableHeight(mHeaderViewHeight);
		if (mEnablePullRefresh) {
			mPullRefreshing = true;
			mHeaderView.setState(PullHeaderView.STATE_REFRESHING);
		}
		setSelection(0);
	}

	/**
	 * 
	 * Get progress in header view.
	 * 
	 * @return
	 * @throws
	 */
	public ProgressBar getHeaderProgress() {
		return mHeaderView.getHeaderProgress();
	}

	/**
	 * 
	 * Get progress in footer view.
	 * 
	 * @return
	 * @throws
	 */
	public ProgressBar getFooterProgress() {
		return mFooterView.getFooterProgress();
	}

	/**
	 * Init views.
	 * 
	 * @param context the context
	 */
	private void initView(Context context) {

		mScroller = new Scroller(context, new DecelerateInterpolator());

		super.setOnScrollListener(this);

		// init header view
		mHeaderView = new PullHeaderView(context);

		// init header height
		mHeaderViewHeight = mHeaderView.getHeaderHeight();
		mHeaderView.setGravity(Gravity.BOTTOM);
		addHeaderView(mHeaderView);

		// init footer view
		mFooterView = new PullFooterView(context);

		mFooterViewHeight = mFooterView.getFooterHeight();

		// Disable pull to refresh and pull to load more default.
		setPullRefreshEnable(false);
		setPullLoadEnable(false);

		mFooterView.hide();
	}

	/**
	 * Update header view
	 * 
	 * @param delta
	 */
	private void updateHeaderView(float delta) {
		int newHeight = (int) delta + mHeaderView.getVisiableHeight();
		mHeaderView.setVisiableHeight(newHeight);
		if (mEnablePullRefresh && !mPullRefreshing) {
			if (mHeaderView.getVisiableHeight() >= mHeaderViewHeight) {
				mHeaderView.setState(PullHeaderView.STATE_READY);
			} else {
				mHeaderView.setState(PullHeaderView.STATE_NORMAL);
			}
		}
		setSelection(0);
	}

	/**
	 * Refresh header view state
	 */
	private void refreshHeaderViewState() {
		int height = mHeaderView.getVisiableHeight();
		if (height < mHeaderViewHeight || !mPullRefreshing) {
			mScrollBack = SCROLLBACK_HEADER;
			mScroller.startScroll(0, height, 0, -1 * height, SCROLL_DURATION);
		} else if (height > mHeaderViewHeight || !mPullRefreshing) {
			mScrollBack = SCROLLBACK_HEADER;
			mScroller.startScroll(0, height, 0, -(height - mHeaderViewHeight),
					SCROLL_DURATION);
		}

		invalidate();
	}

	/**
	 * Start refresh
	 */
	private void startRefresh() {
		mHeaderView.setState(PullHeaderView.STATE_REFRESHING);
		if(mPullRefreshing) {
			//In the process of preventing refresh again when it was refreshing. 
			return;
		}
		interruptPull();	//Stop load more
		if (null != mRefreshListener) {
			mRefreshListener.onRefresh();
		}
		mPullRefreshing = true;
	}

	/**
	 * Set pull-to-refresh enable or disable.
	 * 
	 * @param enable
	 */
	private void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) {
			mHeaderView.setVisibility(View.INVISIBLE);
		} else {
			mHeaderView.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Set pull-to-load-more enable or disable.
	 * 
	 * @param enable
	 */
	private void setPullLoadEnable(boolean enable) {
		mEnablePullLoad = enable;
		if (mEnablePullLoad) {
			mPullLoading = false;
			mFooterView.setState(PullFooterView.STATE_READY);
			mFooterView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startLoadMore();
				}
			});
		} else {
			mFooterView.hide();
			mFooterView.setOnClickListener(null);
		}
	}
}
