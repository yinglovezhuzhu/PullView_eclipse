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

import com.opensource.pullview.utils.ViewUtil;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Usage The footer view of pull view
 * 
 * @author yinglovezhuzhu@gmail.com
 */
public class PullFooterView2 extends LinearLayout {
	
	/** The Constant STATE_READY. */
	public final static int STATE_READY = 1;
	
	/** The Constant STATE_LOADING. */
	public final static int STATE_LOADING = 2;
	
	/** The Constant STATE_NO. */
	public final static int STATE_NO = 3;
	
	/** The Constant STATE_EMPTY. */
	public final static int STATE_EMPTY = 4;

	/** The footer view. */
	private LinearLayout footerView;
	
	/** The footer progress bar. */
	private ProgressBar mFooterProgress;
	
	/** The footer text view. */
	private TextView footerTextView;
	
	/** The footer content height. */
	private int footerHeight;

	/**
	 * Instantiates a new ab list view footer.
	 *
	 * @param context the context
	 */
	public PullFooterView2(Context context) {
		super(context);
		initView(context);
	}

	/**
	 * Instantiates a new ab list view footer.
	 *
	 * @param context the context
	 * @param attrs the attrs
	 */
	public PullFooterView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
		setState(STATE_READY);
	}
	
	/**
	 * Inits the view.
	 *
	 * @param context the context
	 */
	private void initView(Context context) {
		footerView  = new LinearLayout(context);  
		footerView.setOrientation(LinearLayout.HORIZONTAL);
		footerView.setGravity(Gravity.CENTER); 
		//setBackgroundColor(Color.rgb(225, 225,225));
		footerTextView = new TextView(context);  
		footerTextView.setGravity(Gravity.CENTER_VERTICAL);
		setTextColor(Color.rgb(107, 107, 107));
		footerTextView.setTextSize(15);
		footerTextView.setMinimumHeight(50);
		footerView.setPadding(0, 10, 0, 10);
		
		mFooterProgress = new ProgressBar(context,null,android.R.attr.progressBarStyle);
		mFooterProgress.setVisibility(View.GONE);
		
		LinearLayout.LayoutParams layoutParamsWW = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParamsWW.gravity = Gravity.CENTER;
		layoutParamsWW.width = 50;
		layoutParamsWW.height = 50;
		layoutParamsWW.rightMargin = 10;
		footerView.addView(mFooterProgress,layoutParamsWW);
		
		LinearLayout.LayoutParams layoutParamsWW1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		footerView.addView(footerTextView,layoutParamsWW1);
		
		LinearLayout.LayoutParams layoutParamsFW = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		addView(footerView,layoutParamsFW);
		
		ViewUtil.measureView(this);
		footerHeight = this.getMeasuredHeight();
	}

	/**
	 * Set state
	 *
	 * @param state the new state
	 */
	public void setState(int state) {
		
		if (state == STATE_READY) {
			footerView.setVisibility(View.VISIBLE);
			footerTextView.setVisibility(View.VISIBLE);
			mFooterProgress.setVisibility(View.GONE);
			footerTextView.setText("载入更多");
		} else if (state == STATE_LOADING) {
			footerView.setVisibility(View.VISIBLE);
			footerTextView.setVisibility(View.VISIBLE);
			mFooterProgress.setVisibility(View.VISIBLE);
			footerTextView.setText("正在加载...");
		}else if(state == STATE_NO){
			footerView.setVisibility(View.GONE);
			footerTextView.setVisibility(View.VISIBLE);
			mFooterProgress.setVisibility(View.GONE);
			footerTextView.setText("已是全部");
		}else if(state == STATE_EMPTY){
			footerView.setVisibility(View.GONE);
			footerTextView.setVisibility(View.GONE);
			mFooterProgress.setVisibility(View.GONE);
			footerTextView.setText("没有数据");
		}
	}
	
	/**
	 * Gets the visiable height.
	 * @return the visiable height
	 */
	public int getVisiableHeight() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)footerView.getLayoutParams();
		return lp.height;
	}

	/**
	 * 隐藏footerView.
	 */
	public void hide() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) footerView.getLayoutParams();
		lp.height = 0;
		footerView.setLayoutParams(lp);
		footerView.setVisibility(View.GONE);
	}

	/**
	 * 显示footerView.
	 */
	public void show() {
		footerView.setVisibility(View.VISIBLE);
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) footerView.getLayoutParams();
		lp.height = LayoutParams.WRAP_CONTENT;
		footerView.setLayoutParams(lp);
	}

	
	/**
	 * 
	 * 描述：设置字体颜色
	 * @param color
	 * @throws 
	 */
	public void setTextColor(int color){
		footerTextView.setTextColor(color);
	}
	
	/**
	 * 
	 * 描述：设置背景颜色
	 * @param color
	 * @throws 
	 */
	public void setBackgroundColor(int color){
		footerView.setBackgroundColor(color);
	}

	/**
	 * 
	 * 描述：获取Footer ProgressBar，用于设置自定义样式
	 * @return
	 * @throws 
	 */
	public ProgressBar getFooterProgress() {
		return mFooterProgress;
	}

	/**
	 * 
	 * Set custom drawable to the footer view progress.
	 * @return
	 * @throws 
	 */
	public void setFooterProgressDrawable(Drawable indeterminateDrawable) {
		mFooterProgress.setIndeterminateDrawable(indeterminateDrawable);
	}

	/**
	 * 
	 * Get the height of footer view.
	 * @return
	 * @throws 
	 */
	public int getFooterHeight() {
		return footerHeight;
	}
	
	/**
	 * 设置高度.
	 *
	 * @param height 新的高度
	 */
	public void setVisiableHeight(int height) {
		if (height < 0) height = 0;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) footerView.getLayoutParams();
		lp.height = height;
		footerView.setLayoutParams(lp);
	}
}
