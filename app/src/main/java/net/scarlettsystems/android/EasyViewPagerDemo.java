package net.scarlettsystems.android;

import android.content.pm.Signature;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;

import net.scarlettsystems.android.widget.EasyViewPager;

import java.util.Random;

public class EasyViewPagerDemo extends AppCompatActivity
{

	TabLayout tabLayout;
	EasyViewPager easyViewPager;
	int counter = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_easy_view_pager_demo);
		setTitle("EasyViewPager Demo");
		easyViewPager = findViewById(R.id.easy_view_pager);
		easyViewPager.addPage(getPageView());
		easyViewPager.addPage(getPageView());
		easyViewPager.addPage(getPageView());
		tabLayout = findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(easyViewPager);
		for(int c = 0; c < tabLayout.getTabCount(); c++)
		{
			tabLayout.getTabAt(c).setText("Tab " + Integer.toString(c + 1));
		}
	}

	private View getPageView()
	{
		Random rnd = new Random();
		int colour = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
		View view = LayoutInflater.from(this).inflate(R.layout.page_sample, null);
		view.findViewById(R.id.root).setBackgroundColor(colour);
		Glide.with(this)
				.load("https://picsum.photos/512/?random")
				.apply(new RequestOptions().signature(new ObjectKey(String.valueOf(System.currentTimeMillis() + counter++))))
				.into((ImageView)view.findViewById(R.id.picture));
		return view;
	}
}
