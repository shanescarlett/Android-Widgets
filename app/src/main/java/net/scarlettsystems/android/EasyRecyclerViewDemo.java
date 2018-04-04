package net.scarlettsystems.android;

import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
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
	private static final int SAMPLE_TYPE = 0;
	private static final int DIFFERENT_TYPE = 1;
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
		/* Specify the binding procedure for each item and set the view of items via layout
		resource. It is also possible to specify the view through configuring the
		OnCreateItemViewListener and programmatically creating a view each time it is requested. */
		easyRecyclerView.addOnBindItemViewListener(SAMPLE_TYPE,
				R.layout.card_sample,
				new EasyRecyclerView.OnBindItemViewListener()
		{
			@Override
			public void OnBindItemView(View view, SparseArray<View> cache, Object item)
			{
				SampleObject object = (SampleObject)item;
				Glide.with(getBaseContext())
						.load(object.getImageUrl())
						.thumbnail(0.05f)
						.apply(new RequestOptions().signature(new ObjectKey(String.valueOf(System.currentTimeMillis() + counter++))))
						.into((ImageView)cache.get(R.id.picture));
				((TextView)cache.get(R.id.text)).setText(object.getText());
				cache.get(R.id.root).setBackgroundColor(object.getColour());
//				View test;
//				long startTime = System.nanoTime();
//				test = findViewById(R.id.root);
//				long endTime = System.nanoTime();
//				long findViewTime = (endTime - startTime);
//				startTime = System.nanoTime();
//				test = cache.get(R.id.root);
//				endTime = System.nanoTime();
//				long cacheTime = (endTime - startTime);
//				Log.e("findViewById",Long.toString(findViewTime));
//				Log.e("cache",Long.toString(cacheTime));
//				test.setBackgroundColor(object.getColour());
			}
		});

		/* Configure a layout manager to customise the layout of EasyRecyclerView's items.
		An explicit set of a custom LayoutManager is also possible. */
		easyRecyclerView.setLayoutManager(1, EasyRecyclerView.VERTICAL, false, false);

		/* Set up some animation parameters.*/
		easyRecyclerView.setItemDirection(EasyRecyclerView.EAST);
		easyRecyclerView.setAnimationDuration(400);
		easyRecyclerView.setItemAddInterpolator(new DecelerateInterpolator(2));
		easyRecyclerView.setItemRemoveInterpolator(new AccelerateInterpolator(2));
		easyRecyclerView.setItemMoveInterpolator(new AccelerateDecelerateInterpolator());
		easyRecyclerView.setAnimationStagger(64);
		easyRecyclerView.setLoaderPaddingTop(50);
		easyRecyclerView.setLoaderPaddingBottom(50);
		easyRecyclerView.setLoaderColour(Color.GREEN);

		/* Do something when user clicks on an item. */
		easyRecyclerView.addOnItemClickListener(new EasyRecyclerView.OnItemClickListener()
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
		easyRecyclerView.addItem(new SampleObject(), SAMPLE_TYPE);
	}

	private void configureAdvancedBehaviour()
	{
		/* Configure the EasyRecyclerView to load more items upon reaching the end. */
		easyRecyclerView.addOnLoadRequestListener(new EasyRecyclerView.OnLoadRequestListener()
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

		/* Specify a different type of item to be displayed in the list. */
		easyRecyclerView.addOnCreateItemViewListener(DIFFERENT_TYPE, new EasyRecyclerView.OnCreateItemViewListener()
		{
			@Override
			public View OnCreateItemView()
			{
				CardView card = new CardView(getBaseContext());
				TextView text = new TextView(getBaseContext());
				ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				card.setLayoutParams(lp);
				ViewCompat.setElevation(card,10);
				CardView.LayoutParams clp = new CardView.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT);
				text.setPadding(20,20,20,20);
				text.setLayoutParams(clp);
				text.setTag("text");
				card.addView(text);
				return card;
			}

			@Override
			public void OnBindItemView(View view, SparseArray<View> cache, Object item)
			{
				((TextView)view.findViewWithTag("text"))
						.setText(((DifferentObject)item).getText());
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
				easyRecyclerView.addItem(new DifferentObject(), DIFFERENT_TYPE);
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
		for(int c = 0; c < numberOfItems - 1; c++)
		{
			SampleObject item = new SampleObject();
			item.setText(Integer.toString(c) +". "+ item.getText());
			objects.add(item);
		}
		easyRecyclerView.addItems(objects, SAMPLE_TYPE);
		easyRecyclerView.addItem(new DifferentObject(), DIFFERENT_TYPE);
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

		public void setText(String text){this.text = text;}

		public int getColour()
		{
			return colour;
		}
	}

	private class DifferentObject
	{
		private String text;
		private int colour;

		DifferentObject()
		{
			Lorem lorem = LoremIpsum.getInstance();
			text = lorem.getWords(24, 32);
			Random rnd = new Random();
			colour = Color.WHITE;
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
}
