package com.dcy.psychology.fragment;

import java.util.ArrayList;

import com.dcy.psychology.MyApplication;
import com.dcy.psychology.PlamPictureDetailActivity;
import com.dcy.psychology.QuestionThemeChooseActivity;
import com.dcy.psychology.R;
import com.dcy.psychology.SeaGameActivity;
import com.dcy.psychology.ThoughtReadingActivity;
import com.dcy.psychology.adapter.HomeListAdapter;
import com.dcy.psychology.adapter.HomeShowAllListAdapter;
import com.dcy.psychology.gsonbean.GrowPictureBean;
import com.dcy.psychology.gsonbean.GrowQuestionBean;
import com.dcy.psychology.util.Constants;
import com.dcy.psychology.util.ThoughtReadingUtils;
import com.dcy.psychology.util.Utils;
import com.dcy.psychology.view.PullRefreshListView;
import com.dcy.psychology.view.PullRefreshListView.OnRefreshListener;
import com.google.gson.reflect.TypeToken;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class StyleTwoBoxFragment extends Fragment implements 
		OnClickListener,OnItemClickListener{
	private Context mContext;
	private ListView mListView;
	private ArrayList<GrowPictureBean> dataList;
	private ArrayList<GrowQuestionBean> questionList;
	private HomeShowAllListAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		dataList = MyApplication.mGson.fromJson(Utils.loadRawString(mContext, R.raw.homepage_pic_text_lib), new TypeToken<ArrayList<GrowPictureBean>>(){}.getType());
		questionList = MyApplication.mGson.fromJson(Utils.loadRawString(mContext, R.raw.homepage_growquestionlib), new TypeToken<ArrayList<GrowQuestionBean>>(){}.getType());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_style2_box_layout, null);
		view.findViewById(R.id.game_one_tv).setOnClickListener(this);
		view.findViewById(R.id.game_two_tv).setOnClickListener(this);
		mAdapter = new HomeShowAllListAdapter(mContext, dataList);
		mListView = (ListView) view.findViewById(R.id.pull_refresh_lv);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		return view;
	}
	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(position >= dataList.size()){
			Intent mIntent = new Intent(mContext, ThoughtReadingActivity.class);
			mIntent.putExtra(ThoughtReadingUtils.GrowBeanData, questionList.get(position - dataList.size()));
			mIntent.putExtra(ThoughtReadingUtils.ThemeTitle, Constants.HomePageTestTitle[position - dataList.size()]);
			startActivity(mIntent);
		}else {
			Intent mIntent = new Intent(mContext, PlamPictureDetailActivity.class);
			mIntent.putExtra(Constants.PictureBean, dataList.get(position));
			startActivity(mIntent);
		}
	}
	
	@Override
	public void onClick(View v) {
		Intent mIntent = null;
		switch (v.getId()) {
		case R.id.game_one_tv:
			mIntent = new Intent(mContext, SeaGameActivity.class);
			break;
		case R.id.game_two_tv:
			mIntent = new Intent(mContext, QuestionThemeChooseActivity.class);
			break;
		default:
			break;
		}
		if(mIntent != null)
			startActivity(mIntent);
	}
}