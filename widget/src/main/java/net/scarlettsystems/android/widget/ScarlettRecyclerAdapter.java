package net.scarlettsystems.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.ArrayList;

class ScarlettRecyclerAdapter extends RecyclerView.Adapter
{
	private boolean mAnimationEnabled = true;
	private ArrayList<Object> mDataset;
	private OnItemClickListener mItemClickListener = null;
	private ItemViewListener mItemViewListener = null;
	private RecyclerView mRecyclerView;
	private LoaderHolder mLoaderHolder;

	private int mDuration;
	private float mInterpFactor;
	private int mLoaderHeight = 100;
	private int[] mLoaderPadding = {0, 0, 0, 0};

	private static final int TYPE_ITEM = 0;
	private static final int TYPE_FOOTER = -1;
	private static final int TYPE_LOADER = -2;

	ScarlettRecyclerAdapter()
	{
		mDataset = new ArrayList<>();
		mDataset.add(new LoaderObject());
	}

	//Holder Classes

	private class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		private View mView;
		private Object mItem;

		ItemHolder(View view)
		{
			super(view);
			mView = view;
			view.setOnClickListener(this);
		}

		@Override
		public void onClick(View v)
		{
			if(mItemClickListener == null){return;}
			mItemClickListener.OnItemClick(v, mItem);
		}

		void setItem(Object item)
		{
			this.mItem = item;
		}

		Object getItem()
		{
			return mItem;
		}

		public View getView()
		{
			return mView;
		}
	}

	private class LoaderHolder extends RecyclerView.ViewHolder
	{
		private View mView;
		private RelativeLayout loaderContainer, paddingView;
		private ProgressBar loader;
		private boolean isShown = false;

		LoaderHolder(View itemView)
		{
			super(itemView);
			mView = itemView;
			paddingView = itemView.findViewById(R.id.padding);
			loaderContainer = itemView.findViewById(R.id.loader_container);
			loader = itemView.findViewById(R.id.loader);
			Helpers.setViewHeight(loader, 0);
			if(itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams)
			{
				StaggeredGridLayoutManager.LayoutParams layoutParams = new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				layoutParams.setFullSpan(true);
				itemView.setLayoutParams(layoutParams);
			}
		}

		void showLoader()
		{
			loaderContainer.setVisibility(View.VISIBLE);
			if(mAnimationEnabled)
			{
				ValueAnimator animator = ValueAnimator.ofInt(0, mLoaderHeight);
				animator.setDuration(mDuration);
				animator.setInterpolator(new DecelerateInterpolator(mInterpFactor));
				animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
				{
					@Override
					public void onAnimationUpdate(ValueAnimator animator)
					{
						int[] pad = {0, 0, 0, 0};
						for (int c = 0; c < mLoaderPadding.length; c++)
						{
							pad[c] = Math.round((float)mLoaderPadding[c]
									* animator.getAnimatedFraction());
						}
						Helpers.setViewHeight(loader, (int) animator.getAnimatedValue());
						loaderContainer.setPadding(pad[0], pad[1], pad[2], pad[3]);
						mRecyclerView.scrollToPosition(mRecyclerView.getLayoutManager().getItemCount() - 1);
					}
				});
				animator.start();
				isShown = true;
			}
			else
			{
				loaderContainer.setPadding(
						mLoaderPadding[0],
						mLoaderPadding[1],
						mLoaderPadding[2],
						mLoaderPadding[3]);
				Helpers.setViewHeight(loader, mLoaderHeight);
			}
		}

		void hideLoader()
		{
			if(mAnimationEnabled)
			{
				final int[] initPad = {
						loaderContainer.getPaddingLeft(),
						loaderContainer.getPaddingTop(),
						loaderContainer.getPaddingRight(),
						loaderContainer.getPaddingBottom()};
				ValueAnimator animator = ValueAnimator.ofInt(loader.getHeight(), 0);
				animator.setDuration(mDuration);
				animator.setInterpolator(new AccelerateInterpolator(mInterpFactor));
				animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
				{
					@Override
					public void onAnimationUpdate(ValueAnimator animator)
					{
						int[] pad = {0,0,0,0};
						for (int c = 0; c < mLoaderPadding.length; c++)
						{
							pad[c] = Math.round((float)initPad[c]
									* (1f - animator.getAnimatedFraction()));
						}
						loaderContainer.setPadding(pad[0], pad[1], pad[2], pad[3]);
						Helpers.setViewHeight(loader, (int) animator.getAnimatedValue());
					}
				});
				animator.addListener(new AnimatorListenerAdapter()
				{
					@Override
					public void onAnimationEnd(Animator animation)
					{
						loaderContainer.setVisibility(View.GONE);
					}
				});
				animator.start();
				isShown = false;
			}
			else
			{
				loaderContainer.setPadding(0,0,0,0);
				Helpers.setViewHeight(loader, 0);
				loaderContainer.setVisibility(View.GONE);
			}
		}

		boolean isShown()
		{
			return isShown;
		}

		void setBottomPadding(int padding)
		{
			Helpers.setViewHeight(paddingView, padding);
		}

		void setLoaderColour(int colour)
		{
			loader.getIndeterminateDrawable().setColorFilter(colour, PorterDuff.Mode.SRC_IN);
		}
	}

	private class LoaderObject
	{

	}

	//Interfaces

	public interface OnItemClickListener
	{
		void OnItemClick(View v, Object object);
	}

	public interface ItemViewListener
	{
		View OnCreateItemView(ViewGroup parent);
		void OnBindItemView(View v, Object item);
	}

	//Callback Methods

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView)
	{
		super.onAttachedToRecyclerView(recyclerView);
		mRecyclerView = recyclerView;
		View view = LayoutInflater
				.from(recyclerView.getContext())
				.inflate(R.layout.card_loader, null, false);
		mLoaderHolder = new LoaderHolder(view);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		switch(viewType)
		{
			case TYPE_ITEM:
			{
				View view;
				if(mItemViewListener == null)
				{
					throw new IllegalStateException("OnCreateItemViewListener must be set.");
				}
				view = mItemViewListener.OnCreateItemView(parent);
				if(view == null)
				{
					throw new IllegalArgumentException("OnCreateItemView must return a view.");
				}
				return new ItemHolder(view);
			}
			case TYPE_LOADER:
			{
				parent.addView(mLoaderHolder.mView);
				return mLoaderHolder;
			}
			default:
			{
				throw new IllegalArgumentException();
			}
		}

	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder vh, int position)
	{
		if(vh instanceof ItemHolder)
		{
			ItemHolder h = (ItemHolder)vh;
			h.setItem(mDataset.get(position));
			mItemViewListener
					.OnBindItemView(h.getView(), h.getItem());
		}
		else if(vh instanceof LoaderHolder)
		{
			LoaderHolder holder = (LoaderHolder)vh;
		}
	}

	//Getters & Setters

	void setOnItemClickListener(OnItemClickListener l)
	{
		mItemClickListener = l;
	}

	void setItemViewListener(ItemViewListener l)
	{
		mItemViewListener = l;
	}

	void setAnimationDuration(int value)
	{
		mDuration = value;
	}

	void setInterpolationFactor(float value)
	{
		mInterpFactor = value;
	}

	void setLoaderHeight(int value)
	{
		mLoaderHeight = value;
	}

	Object getItem(int index)
	{
		if(index >= getItemCountProtected())
		{
			throw new IndexOutOfBoundsException();
		}
		return mDataset.get(index);
	}

	ArrayList<Object> getItems(int startIndex, int count)
	{
		return new ArrayList<>(mDataset.subList(startIndex, startIndex + count));
	}

	ArrayList<Object> getItems()
	{
		return new ArrayList<>(mDataset.subList(0, getItemCountProtected()));
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public int getItemCount()
	{
		return mDataset.size();
	}

	public int getItemCountProtected()
	{
		return mDataset.size() - 1;
	}

	@Override
	public int getItemViewType(int position)
	{
		if(mDataset.get(position) instanceof LoaderObject)
		{
			return TYPE_LOADER;
		}
		else
		{
			return TYPE_ITEM;
		}
	}

	//Control Methods

	void addItem(Object item)
	{
		mDataset.add(getItemCountProtected(), item);
		notifyItemInserted(getItemCountProtected());
	}

	void addItems(ArrayList<?> items)
	{
//		for(int c = 0; c < items.size(); c++)
//		{
//			addItem(items.get(c));
//		}
		mDataset.addAll(getItemCountProtected(), items);
		notifyItemRangeInserted(getItemCountProtected(), items.size());
	}

	void addItemAt(Object item, int index)
	{
		mDataset.add(index, item);
		notifyItemInserted(index);
	}

	void removeItem(int index)
	{
		mDataset.remove(index);
		notifyItemRemoved(index);
	}

	void removeAll()
	{
		int originalItemCount = getItemCountProtected();
		Object loader = mDataset.get(originalItemCount);
		mDataset.clear();
		mDataset.add(loader);
		notifyItemRangeRemoved(0, originalItemCount);
	}

	int indexOf(Object item)
	{
		return mDataset.indexOf(item);
	}

	void showLoader()
	{
		if(mLoaderHolder == null){return;}
		mLoaderHolder.showLoader();
	}

	void hideLoader()
	{
		if(mLoaderHolder == null){return;}
		mLoaderHolder.hideLoader();
	}

	boolean isLoaderShown()
	{
		if(mLoaderHolder == null){return false;}
		return mLoaderHolder.isShown();
	}

	void setAnimationEnabled(boolean enabled)
	{
		mAnimationEnabled = enabled;
	}

	void setLoaderPadding(int padding)
	{
		for(int c = 0; c < mLoaderPadding.length; c++)
		{
			mLoaderPadding[c] = padding;
		}
	}

	void setLoaderPaddingTop(int padding)
	{
		mLoaderPadding[1] = padding;
	}

	void setLoaderPaddingBottom(int padding)
	{
		mLoaderPadding[3] = padding;
	}

	void setLoaderColour(int colour)
	{
		mLoaderHolder.setLoaderColour(colour);
	}

	void setBottomPadding(int padding)
	{
		mLoaderHolder.setBottomPadding(padding);
	}
}
