package net.scarlettsystems.android.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.ArrayList;

class ScarlettRecyclerAdapter extends RecyclerView.Adapter
{
	private ArrayList<Object> mDataset;
	private OnItemClickListener mItemClickListener = null;
	private ItemViewListener mItemViewListener = null;
	private RecyclerView mRecyclerView;
	private LoaderHolder mLoaderHolder;

	private int mDuration;
	private float mInterpFactor;
	private int mLoaderHeight = 100;

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
			if(mItemClickListener == null)
			{
				return;
			}
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
		private Context mContext;
		private RelativeLayout loaderContainer, view;
		private boolean isOpen = false;

		LoaderHolder(View itemView)
		{
			super(itemView);
			mContext = itemView.getContext();
			view = itemView.findViewById(R.id.padding);
			loaderContainer = itemView.findViewById(R.id.loader_container);
			Helpers.setViewHeight(loaderContainer, 0);
			StaggeredGridLayoutManager.LayoutParams layoutParams = new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.setFullSpan(true);
			itemView.setLayoutParams(layoutParams);
			configureLoader();
			configurePadding();
		}

		private void configurePadding()
		{
			if(Helpers.softButtonsExist(mContext))
			{
				Helpers.setViewHeight(view, Helpers.getNavBarHeight(mContext));
			}
			else
			{
				Helpers.setViewHeight(view, 0);
			}
		}

		private void configureLoader()
		{
			ProgressBar loader = loaderContainer.findViewById(R.id.loader);
			//loader.setIndeterminateTintList();
		}

		void openLoading()
		{
			if(!isOpen)
			{
				ValueAnimator animator = ValueAnimator.ofInt(0, mLoaderHeight);
				animator.setDuration(mDuration);
				animator.setInterpolator(new DecelerateInterpolator(mInterpFactor));
				animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
				{
					@Override
					public void onAnimationUpdate(ValueAnimator valueAnimator)
					{
						Helpers.setViewHeight(loaderContainer, (int) valueAnimator.getAnimatedValue());
						//ScarlettHelpers.setViewHeight(root, view.getHeight() + (int)valueAnimator.getAnimatedValue());
						//mRecyclerView.scrollToPosition(mRecyclerView.getLayoutManager().getItemCount() - 1);
					}
				});
				animator.start();
				isOpen = true;
			}
		}

		void closeLoading()
		{
			if(isOpen)
			{
				int size = loaderContainer.getHeight();
				ValueAnimator animator = ValueAnimator.ofInt(size, 0);
				animator.setDuration(mDuration);
				animator.setInterpolator(new AccelerateInterpolator(mInterpFactor));
				animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
				{
					@Override
					public void onAnimationUpdate(ValueAnimator valueAnimator)
					{
						Helpers.setViewHeight(loaderContainer, (int) valueAnimator.getAnimatedValue());
						//ScarlettHelpers.setViewHeight(root, view.getHeight() + (int)valueAnimator.getAnimatedValue());
					}
				});
				animator.start();
				isOpen = false;
			}
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
				View view = LayoutInflater
						.from(parent.getContext())
						.inflate(R.layout.card_loader, parent, false);
				LoaderHolder holder = new LoaderHolder(view);
				mLoaderHolder = holder;
				return holder;
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
		if(index >= getItemCount())
		{
			throw new IndexOutOfBoundsException("Index is out of bounds of EasyRecyclerView's items.");
		}
		return mDataset.get(index);
	}

	ArrayList<Object> getItems(int startIndex, int count)
	{
		return new ArrayList<>(mDataset.subList(startIndex, startIndex + count));
	}

	ArrayList<Object> getItems()
	{
		return new ArrayList<>(mDataset.subList(0, getItemCount()));
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public int getItemCount()
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
		int size = mDataset.size();
		mDataset.add(size - 1, item);
		notifyItemInserted(getItemCount() - 1);
		if(size==1)
		{
			mRecyclerView.scrollToPosition(0);
		}
	}

	void addItems(ArrayList<?> items)
	{
		for(int c = 0; c < items.size(); c++)
		{
			addItem(items.get(c));
		}
		notifyItemRangeInserted(getItemCount() - items.size(), items.size());
	}

	void addItemAt(Object item, int index)
	{
		if(index >= getItemCount())
		{
			throw new IndexOutOfBoundsException("Index is out of bounds of EasyRecyclerView's items.");
		}
		mDataset.add(index, item);
		notifyItemInserted(index);
	}

	void removeItem(int index)
	{
		if(index >= getItemCount())
		{
			throw new IndexOutOfBoundsException("Index is out of bounds of EasyRecyclerView's items.");
		}
		mDataset.remove(index);
		notifyItemRemoved(index);
	}

	void removeAll()
	{
		int originalItemCount = getItemCount();
		Object loader = mDataset.get(originalItemCount);
		mDataset.clear();
		mDataset.add(loader);
		notifyItemRangeRemoved(0, originalItemCount);
	}

	int indexOf(Object item)
	{
		return mDataset.indexOf(item);
	}

	void openLoading()
	{
		if(mLoaderHolder == null){return;}
		mLoaderHolder.openLoading();
	}

	void closeLoading()
	{
		if(mLoaderHolder == null){return;}
		mLoaderHolder.closeLoading();
	}
}
