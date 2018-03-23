package net.scarlettsystems.android;

import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;

import net.scarlettsystems.android.widget.EasyRecyclerView;

import java.util.Random;

public class EasyRecyclerViewDemo extends AppCompatActivity
{
	private EasyRecyclerView easyRecyclerView;
	private int counter = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_easy_recycler_view_demo);
		setTitle("EasyRecyclerView Demo");
		configureEasyRecyclerView();
		configureButtons();
	}

	private void configureEasyRecyclerView()
	{
		/* Find the view */
		easyRecyclerView = findViewById(R.id.easy_recycler_view);

		/* Set the view of items via layout resource. It is also possible to specify the view through
		configuring the OnCreateItemViewListener. */
		easyRecyclerView.setItemLayoutResource(R.layout.card_sample);

		/* Specify the binding procedure for each item. */
		easyRecyclerView.setOnBindItemViewListener(new EasyRecyclerView.OnBindItemViewListener()
		{
			@Override
			public void OnBindItemView(View view, Object item)
			{
				SampleObject object = (SampleObject)item;
				Glide.with(getBaseContext())
						.load(object.getImageUrl())
						.apply(new RequestOptions().signature(new ObjectKey(String.valueOf(System.currentTimeMillis() + counter++))))
						.into((ImageView)view.findViewById(R.id.picture));
				((TextView)view.findViewById(R.id.text)).setText(object.getText());
				view.findViewById(R.id.root).setBackgroundColor(object.getColour());
			}
		});

		/* Configure a layout manager to customise the layout of EasyRecyclerView's items.
		An explicit or custom LayoutManager can be used as well. */
		easyRecyclerView.setLayoutManager(1, EasyRecyclerView.VERTICAL, false, false);

		/* Set up some animation parameters.*/
		easyRecyclerView.setCardEnterDirection(EasyRecyclerView.EAST);
		easyRecyclerView.setAnimationStagger(64);

		/* Do something when user clicks on an item. */
		easyRecyclerView.setOnItemClickListener(new EasyRecyclerView.OnItemClickListener()
		{
			@Override
			public void OnItemClick(View v, Object object)
			{
				Toast.makeText(getBaseContext(), "You clicked on an item.", Toast.LENGTH_SHORT).show();
			}
		});

		/* Create sample items and add them to EasyRecyclerView */
		Handler handler = new Handler();
		handler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				for(int c = 0; c < 10; c++)
				{
					easyRecyclerView.addItem(new SampleObject());
				}
			}
		}, 1000);

	}

	private void configureButtons()
	{
		findViewById(R.id.add).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				easyRecyclerView.addItem(new SampleObject());
			}
		});

		findViewById(R.id.remove).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(easyRecyclerView.getItemCount() > 0)
				{
					easyRecyclerView.removeItem(easyRecyclerView.getItemCount() - 1);
				}
				else
				{
					Toast.makeText(getBaseContext(), "There are no items to remove.", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
}

class SampleObject
{
	private String imageUrl;
	private String text;
	private int colour;

	public SampleObject()
	{
		Lorem lorem = LoremIpsum.getInstance();
		imageUrl = "https://picsum.photos/512/?random";
		text = lorem.getWords(8, 16);
		Random rnd = new Random();
		colour = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
	}

	public String getImageUrl()
	{
		return imageUrl;
	}

	public String getText()
	{
		return text;
	}

	public int getColour()
	{
		return colour;
	}
}
