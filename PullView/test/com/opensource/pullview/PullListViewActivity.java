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

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

/**
 * Usage 
 * 
 * @author yinglovezhuzhu@gmail.com
 */
public class PullListViewActivity extends Activity {
	
	private static final String TAG = "PullListViewActivity";

	private static final int MSG_REFRESH_DONE = 0x100;
	private static final int MSG_LOAD_DONE = 0x101;

	private PullListView mListView;
	private MainHandler mHandler = new MainHandler();
	
	@SuppressLint("HandlerLeak")
	private class MainHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REFRESH_DONE:
				if(null != mListView) {
					mListView.refreshComplete();
				}
				break;
			case MSG_LOAD_DONE:
				if(null != mListView) {
					mListView.loadMoreComplete(true);
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_pull_list_view);
		
		mListView = (PullListView) findViewById(R.id.pull_list_view);
		
		List<String> items = new ArrayList<String>();
		for(int i = 0; i < 30; i++) {
			items.add("Item " + i);
		}
		
		ImageView iv = new ImageView(this);
		iv.setImageResource(R.drawable.ic_launcher);
		mListView.addHeaderView(iv);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		mListView.setAdapter(adapter);
		
		mListView.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				mHandler.sendEmptyMessageDelayed(MSG_REFRESH_DONE, 50000);
				Log.e(TAG, "Start refresh+=====================^_^");
			}

			@Override
			public void onInterrupt() {
				Log.e(TAG, "Interrupt refresh+=====================-_-");
			}
		});
		
		mListView.setOnLoadMoreListener(new OnLoadMoreListener() {
			
			@Override
			public void onLoadMore() {
				mHandler.sendEmptyMessageDelayed(MSG_LOAD_DONE, 50000);
				Log.e(TAG, "Start load more+=====================^_^");
			}

			@Override
			public void onInterrupt() {
				Log.e(TAG, "Interrupt load more+=====================-_-");
			}
		});
	}
}
