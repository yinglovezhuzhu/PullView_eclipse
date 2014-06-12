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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Scroller;

/**
 * Usage 
 * 
 * @author yinglovezhuzhu@gmail.com
 */
public class PullGridView extends BaseGridView implements OnScrollListener,
		OnTouchListener {
	
	/** The Constant SCROLLBACK_HEADER. */
	private final static int SCROLLBACK_HEADER = 0;
	
	/** The Constant SCROLL_DURATION. */
	private final static int SCROLL_DURATION = 200;
	
	/** The Constant OFFSET_RADIO. */
	private final static float OFFSET_RADIO = 1.8f;
	
	/** The m last y. */
	private float mLastY = -1; 
	
	/** The m scroller. */
	private Scroller mScroller;
	
	/** 头部刷新View. */
	private PullHeaderView mHeaderView;
	
	/** The m footer view. */
	private PullFooterView mFooterView;
	
	/** 头部View的高度. */
	private int mHeaderViewHeight; 
	
	/** The m enable pull refresh. */
	private boolean mEnablePullRefresh = true;
	
	/** The m enable pull load. */
	private boolean mEnablePullLoad = true;
	
	/** The m pull refreshing. */
	private boolean mPullRefreshing = false;
	
	/** The m pull loading. */
	private boolean mPullLoading;
	
	/** The listener to listen pull-to-refresh */
	private OnRefreshListener mOnRefreshListener = null;
	
	/** The listener to listen load-more */
	private OnLoadMoreListener mOnLoadMoreListener = null;
 
	/** The m scroll back. */
	private int mScrollBack;
	
	/** 数据相关. */
	private BaseAdapter mAdapter = null;
	
	/** 外层是否可滚动. */
	private boolean mGridViewScrollable = false;
	

	
	/**
	 * Constructor
	 *
	 * @param context the context
	 */
	public PullGridView(Context context) {
		super(context);
		initView(context);
	}

	/**
	 * Constructor
	 *
	 * @param context the context
	 * @param attrs the attrs
	 */
	public PullGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (mGridViewScrollable) {
			return false;
		}

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
			if (mEnablePullRefresh
					&& (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
				// 下拉更新高度
				updateHeaderView(deltaY / OFFSET_RADIO);
				mGridView.smoothScrollToPosition(0);
				return true;
			} 
//			else if (mEnablePullLoad
//					&& !mPullLoading
//					&& mGridView.getLastVisiblePosition() == (mGridView
//							.getChildCount() - 1) && deltaY < 0) {
//				startLoadMore();
//			}
			break;
		case MotionEvent.ACTION_UP:
			mLastY = -1;
			// 需要刷新的条件
			if (mEnablePullRefresh
					&& mHeaderView.getVisiableHeight() >= mHeaderViewHeight) {
				startRefresh();
			}
			if (mEnablePullRefresh) {
				// Log.i("TAG", "--弹回--");
				// 弹回
				refreshHeaderViewState();
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

	@Override
	public boolean onTouch(View arg0, MotionEvent ev) {
		return onTouchEvent(ev);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		System.out.println("AAAAAAAAAAAAAAAAAAA " + firstVisibleItem + "<>" + mGridView.getFirstVisiblePosition());
		if (firstVisibleItem == 0) {
			//Scroll to top
			mGridViewScrollable = false;
		} else if (firstVisibleItem + visibleItemCount == totalItemCount) {
			//Scroll to bottom
			mGridViewScrollable = false;
		} else {
			if (!mPullRefreshing) {
				mGridViewScrollable = true;
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) { }

	/**
	 * Set the listener to listen pull-to-refresh
	 * @param listener
	 */
	public void setOnRefreshListener(OnRefreshListener listener) {
		this.mOnRefreshListener = listener;
		setPullRefreshEnable(null != listener);
	}
	
	/**
	 * Set the listener to listen load-more
	 * @param listener
	 */
	public void setOnLoadMoreListener(OnLoadMoreListener listener) {
		this.mOnLoadMoreListener = listener;
		setPullLoadEnable(null != listener);
	}
	
	/**
	 * 描述：设置数据列表的适配器.
	 * @param adapter the new adapter
	 */
	public void setAdapter(BaseAdapter adapter) {
		mAdapter = adapter;
		mGridView.setAdapter(mAdapter);
	}
	
	/**
	 * Get header view.
	 * 
	 * @return
	 * @throws 
	 */
	public PullHeaderView getHeaderView() {
		return mHeaderView;
	}

	/**
	 * Get footer view
	 * 
	 * @return
	 * @throws 
	 */
	public PullFooterView getFooterView() {
		return mFooterView;
	}
	
	/**
	 * Get ProgressBar in header view.
	 * @return
	 * @throws 
	 */
	public ProgressBar getHeaderProgress() {
		return mHeaderView.getHeaderProgress();
	}
	
	
	/**
	 * Get ProgressBar in footer view.
	 * @return
	 * @throws 
	 */
	public ProgressBar getFooterProgress() {
		return mFooterView.getFooterProgress();
	}

	/**
	 * Set pull-to-refresh enable or disable.
	 *
	 * @param enable 
	 */
	public void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) {
			mHeaderView.setVisibility(View.INVISIBLE);
		} else {
			mHeaderView.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * Set load-more enable or disable.
	 *
	 * @param enable 
	 */
	public void setPullLoadEnable(boolean enable) {
		mEnablePullLoad = enable;
		if (!mEnablePullLoad) {
			mFooterView.hide();
			mFooterView.setOnClickListener(null);
		} else {
			mPullLoading = false;
			mFooterView.setState(PullFooterView.STATE_READY);
			mFooterView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startLoadMore();
				}
			});
		}
	}
	
	/**
	 * Stop refresh and update header view.
	 */
	public void refreshComplete() {
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			refreshHeaderViewState();
		}
	}
	
	/**
	 * Stop load more and rest footer view.<br>
	 * <p>You can set the flag to control whether it can load more<br>
	 * 
	 * @param hasMore Whether it can load more.
	 *
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
	 * Inits the with context.
	 *
	 * @param context the context
	 */
	private void initView(Context context) {
		mScroller = new Scroller(context, new DecelerateInterpolator());
		
		// init header view
		mHeaderView = new PullHeaderView(context);
		
		// init header height
		mHeaderViewHeight = mHeaderView.getHeaderHeight();
		mHeaderView.setGravity(Gravity.BOTTOM);
		addHeaderView(mHeaderView);
		
		mGridView.setCacheColorHint(context.getResources().getColor(android.R.color.transparent));
		mGridView.setColumnWidth(150);
		mGridView.setGravity(Gravity.CENTER);
		mGridView.setHorizontalSpacing(5);
		mGridView.setNumColumns(GridView.AUTO_FIT);
		mGridView.setPadding(5, 5, 5, 5);
		mGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
		mGridView.setVerticalSpacing(5);
		mGridView.setOnScrollListener(this);
		mGridView.setOnTouchListener(this);
		
		// init footer view
		mFooterView = new PullFooterView(context);
		addFooterView(mFooterView);
		
		setPullRefreshEnable(false);
		setPullLoadEnable(false);
		
		mFooterView.hide();
		
	}
	
	/**
	 * Start to refresh.
	 */
	private void startRefresh() {
		if(mPullLoading) {
			//In the process of preventing refresh again when it was refreshing. 
			return;
		}
		mHeaderView.setState(PullHeaderView.STATE_REFRESHING);
		if(null != mOnRefreshListener) {
			mOnRefreshListener.onRefresh();
		}
		mPullRefreshing = true;
	}

	/**
	 * Update header view
	 *
	 * @param 
	 */
	private void updateHeaderView(float delta) {
		if(delta > 0 && mGridView.getFirstVisiblePosition() != 0) {
			//If and only if the first record displayed when it can pull-to-refresh
			return;
		}
		int newHeight = (int) delta + mHeaderView.getVisiableHeight();
		mHeaderView.setVisiableHeight(newHeight);
		if (mEnablePullRefresh && !mPullRefreshing) {
			if (mHeaderView.getVisiableHeight() >= mHeaderViewHeight) {
				mHeaderView.setState(PullHeaderView.STATE_READY);
			} else {
				mHeaderView.setState(PullHeaderView.STATE_NORMAL);
			}
		}
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

		mGridViewScrollable = true;

		invalidate();
	}
	
	/**
	 * Start to load more.
	 */
	private void startLoadMore() {
		mFooterView.show();
		if (mPullLoading) {
			// //In the process of preventing load again when it was loading.
			return;
		}
		mFooterView.setState(PullFooterView.STATE_LOADING);
		if (null != mOnLoadMoreListener) {
			mOnLoadMoreListener.onLoadMore();
		}
		mPullLoading = true;
	}
	
}
