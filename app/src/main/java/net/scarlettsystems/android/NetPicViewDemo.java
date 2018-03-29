package net.scarlettsystems.android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import net.scarlettsystems.android.widget.NetPicView;

public class NetPicViewDemo extends AppCompatActivity
{
	private NetPicView netPicView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_net_pic_view_demo);
		netPicView = findViewById(R.id.image);
		netPicView.setLoaderAnimationDuration(400);
		findViewById(R.id.load).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				netPicView.forceReload("https://i.imgur.com/0Wbrj06.jpg", new RequestOptions());
			}
		});
	}
}
