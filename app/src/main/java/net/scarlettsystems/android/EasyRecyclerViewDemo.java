package net.scarlettsystems.android;

import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;

import net.scarlettsystems.android.widget.EasyRecyclerView;

import java.util.ArrayList;
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
		configureAdvancedBehaviour();
		configureButtons();
	}

	private void configureEasyRecyclerView()
	{
		/* Find the view */
		easyRecyclerView = findViewById(R.id.easy_recycler_view);

		/* Set the view of items via layout resource. It is also possible to specify the view
		through	configuring the OnCreateItemViewListener and programmatically creating a view
		each time it is requested. */
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
		An explicit set of a custom LayoutManager is also possible. */
		easyRecyclerView.setLayoutManager(1, EasyRecyclerView.VERTICAL, false, false);

		/* Set up some animation parameters.*/
		easyRecyclerView.setCardDirection(EasyRecyclerView.EAST);
		easyRecyclerView.setAnimationDuration(400);
		easyRecyclerView.setCardAddInterpolator(new DecelerateInterpolator(2));
		easyRecyclerView.setCardRemoveInterpolator(new AccelerateInterpolator(2));
		easyRecyclerView.setCardMoveInterpolator(new AccelerateDecelerateInterpolator());
		easyRecyclerView.setAnimationStagger(64);
		easyRecyclerView.setLoaderPaddingTop(50);
		easyRecyclerView.setLoaderPaddingBottom(50);
		easyRecyclerView.setLoaderColour(Color.GREEN);

		/* Do something when user clicks on an item. */
		easyRecyclerView.setOnItemClickListener(new EasyRecyclerView.OnItemClickListener()
		{
			@Override
			public void OnItemClick(View v, Object object)
			{
				Toast
					.makeText(getBaseContext(),
							"You clicked item #"
									+ Integer.toString(easyRecyclerView.indexOf(object) + 1),
							Toast.LENGTH_SHORT)
					.show();
			}
		});

		/* Create sample item and add to EasyRecyclerView */
		easyRecyclerView.addItem(new SampleObject());
	}

	private void configureAdvancedBehaviour()
	{
		/* Configure the EasyRecyclerView to load more items upon reaching the end. */
		easyRecyclerView.setOnLoadRequestListener(new EasyRecyclerView.OnLoadRequestListener()
		{
			@Override
			public void OnLoadRequest()
			{
				easyRecyclerView.showLoader();
				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						addItems(5);
						easyRecyclerView.hideLoader();
					}
				}, 1000);
			}
		});
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

		findViewById(R.id.add_five).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				addItems(5);
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

		findViewById(R.id.clear).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				easyRecyclerView.removeAll();
			}
		});
	}

	private void addItems(int numberOfItems)
	{
		ArrayList<SampleObject> objects = new ArrayList<>();
		for(int c = 0; c < numberOfItems; c++)
		{
			SampleObject item = new SampleObject();
			item.setText(Integer.toString(c) +". "+ item.getText());
			objects.add(item);
		}
		easyRecyclerView.addItems(objects);
	}
}

class SampleObject
{
	private String imageUrl;
	private String text;
	private int colour;

	SampleObject()
	{
		Lorem lorem = LoremIpsum.getInstance();
		imageUrl = "https://picsum.photos/512/?random";
		text = lorem.getWords(8, 16);
		Random rnd = new Random();
		colour = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
	}

	String getImageUrl()
	{
		return imageUrl;
	}

	String getText()
	{
		return text;
	}

	void setText(String text){this.text = text;}

	int getColour()
	{
		return colour;
	}
}
