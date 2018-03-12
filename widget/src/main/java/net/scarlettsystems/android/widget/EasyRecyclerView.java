package net.scarlettsystems.android.widget;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class EasyRecyclerView extends RecyclerView
{

	//Public constants

	public static final int VERTICAL = OrientationHelper.VERTICAL;
	public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;

	//Members

	private Context mContext;
	private ScarlettRecyclerAdapter mAdapter;
	private LayoutManager mLayoutManager;
	private CardAnimator mAnimator;
	private @LayoutRes Integer mItemLayoutResource = null;
	private OnItemClickListener mItemClickListener = null;
	private OnCreateItemViewListener mOnCreateItemViewListener = null;
	private OnBindItemViewListener mOnBindItemViewListener = null;
	private int mAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
	private float mInterpolationFactor = 1.0f;

	//Constructors

	public EasyRecyclerView(Context context)
	{
		super(context);
		init(context);
	}

	public EasyRecyclerView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	public EasyRecyclerView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context);
	}

	//Interfaces

	public interface OnItemClickListener
	{
		void OnItemClick(View v, Object object);
	}

	public interface OnCreateItemViewListener
	{
		View OnCreateItemView();
	}

	public interface OnBindItemViewListener
	{
		void OnBindItemView(View view, Object item);
	}

	public interface OnInteractListener
	{

	}

	//Internal Configuration Methods

	private void init(Context context)
	{
		mContext = context;
		configureRecyclerView();
		configureAdapter();
		configureLayoutManager();
		configureAnimator();
	}

	private void configureRecyclerView()
	{
		setHasFixedSize(true);
		setItemViewCacheSize(20);
		setDrawingCacheEnabled(true);
		setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
	}

	private void configureAdapter()
	{
		mAdapter = new ScarlettRecyclerAdapter();
		mAdapter.setAnimationDuration(mAnimationDuration);
		mAdapter.setInterpolationFactor(mInterpolationFactor);
		mAdapter.setItemViewListener(new ScarlettRecyclerAdapter.ItemViewListener()
		{
			@Override
			public View OnCreateItemView(ViewGroup parent)
			{
				if(mItemLayoutResource != null)
				{
					return LayoutInflater.from(mContext).inflate(mItemLayoutResource, parent, false);
				}
				if(mOnCreateItemViewListener == null)
				{
					throw new IllegalStateException("OnCreateItemViewListener must be set.");
				}
				return mOnCreateItemViewListener.OnCreateItemView();
			}

			@Override
			public void OnBindItemView(View v, Object item)
			{
				mOnBindItemViewListener.OnBindItemView(v, item);
			}
		});
		mAdapter.setOnItemClickListener(new ScarlettRecyclerAdapter.OnItemClickListener()
		{
			@Override
			public void OnItemClick(View v, Object object)
			{
				if(mItemClickListener == null)
				{
					return;
				}
				mItemClickListener.OnItemClick(v, object);
			}
		});

		setAdapter(mAdapter);
	}

	private void configureLayoutManager()
	{
		mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
		super.setLayoutManager(mLayoutManager);
	}

	private void configureAnimator()
	{
		mAnimator = new CardAnimator(mContext, this);
		setItemAnimator(mAnimator);
	}

	//External Settings Methods

	public void setOnItemClickListener(OnItemClickListener l)
	{
		mItemClickListener = l;
	}

	public void setOnCreateItemViewListener(OnCreateItemViewListener l)
	{
		mOnCreateItemViewListener = l;
	}

	public void setOnBindItemViewListener(OnBindItemViewListener l)
	{
		mOnBindItemViewListener = l;
	}

	public void setItemLayoutResource(@LayoutRes int res)
	{
		mItemLayoutResource = res;
	}

	@Override
	public void setLayoutManager(RecyclerView.LayoutManager lm)
	{
		super.setLayoutManager(lm);
	}

	public void setLayoutManager(int spanCount, int orientation, boolean staggered, boolean reverseLayout)
	{
		LayoutManager lm;
		//Input validation
		if(spanCount < 1)
		{
			throw new IllegalArgumentException("Span count should be larger than 1.");
		}
		if(orientation != VERTICAL && orientation != HORIZONTAL)
		{
			throw new IllegalArgumentException("Unrecognised orientation. Required: VERTICAL or HORIZONTAL.");
		}
		//Create manager
		if(spanCount == 1)
		{
			lm = new LinearLayoutManager(mContext, orientation, reverseLayout);
		}
		else if(!staggered)
		{
			lm = new GridLayoutManager(mContext, spanCount, orientation, reverseLayout);
		}
		else
		{
			lm = new StaggeredGridLayoutManager(spanCount, orientation);
		}
		//Set
		lm.setAutoMeasureEnabled(true);
		super.setLayoutManager(lm);
	}

	public void setCardEnterDirection(int value)
	{
		mAnimator.setDirection(value);
	}

	public void setCardAnimationAmount(int value)
	{
		mAnimator.setTranslationAmount(value);
	}

	public void setAnimationDuration(int value)
	{
		mAdapter.setAnimationDuration(value);
		mAnimator.setAnimationDuration(value);
	}

	public void setAnimationStagger(int value)
	{
		mAnimator.setStaggerDelay(value);
	}

	public void setInterpolationFactor(float value)
	{
		mAdapter.setInterpolationFactor(value);
		mAnimator.setInterpolationFactor(value);
	}

	public void setLoaderHeight(int value)
	{
		mAdapter.setLoaderHeight(value);
	}

	public void addItem(Object item)
	{
		mAdapter.addItem(item);
	}

	public void addItems(ArrayList<Object> items)
	{
		mAdapter.addItems(items);
	}

	public void addItemAt(Object item, int index)
	{
		mAdapter.addItemAt(item, index);
	}

	public void removeItem(int index)
	{
		mAdapter.removeItem(index);
	}

	public void removeAll()
	{
		mAdapter.removeAll();
	}
}
