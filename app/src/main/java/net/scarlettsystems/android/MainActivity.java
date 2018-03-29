package net.scarlettsystems.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.button_evp).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent intent = new Intent(getBaseContext(), EasyViewPagerDemo.class);
				startActivity(intent);
			}
		});

		findViewById(R.id.button_erv).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent intent = new Intent(getBaseContext(), EasyRecyclerViewDemo.class);
				startActivity(intent);
			}
		});

		findViewById(R.id.button_ftv).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent intent = new Intent(getBaseContext(), FlexTextViewDemo.class);
				startActivity(intent);
			}
		});

		findViewById(R.id.button_npv).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent intent = new Intent(getBaseContext(), NetPicViewDemo.class);
				startActivity(intent);
			}
		});
	}
}
