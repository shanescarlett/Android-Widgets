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

	public ScarlettRecyclerAdapter()
	{
		mDataset = new ArrayList<>();
		mDataset.add(new LoaderObject());
	}

	//Holder Classes

	private class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		private View mView;
		private Object mItem;

		public ItemHolder(View view)
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

		public void setItem(Object item)
		{
			this.mItem = item;
		}

		public Object getItem()
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

		public LoaderHolder(View itemView)
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

		public void openLoading()
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

		public void closeLoading()
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
				ItemHolder holder = new ItemHolder(view);
				return holder;
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

	public void setOnItemClickListener(OnItemClickListener l)
	{
		mItemClickListener = l;
	}

	public void setItemViewListener(ItemViewListener l)
	{
		mItemViewListener = l;
	}

	public void setAnimationDuration(int value)
	{
		mDuration = value;
	}

	public void setInterpolationFactor(float value)
	{
		mInterpFactor = value;
	}

	public void setLoaderHeight(int value)
	{
		mLoaderHeight = value;
	}

	public ArrayList<Object> getItems()
	{
		return mDataset;
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

	public void addItem(Object item)
	{
		int size = mDataset.size();
		mDataset.add(size - 1, item);
		notifyItemInserted(mDataset.size() - 2);
		if(size==1)
		{
			mRecyclerView.scrollToPosition(0);
		}
	}

	public void addItems(ArrayList<Object> items)
	{
		for(int c = 0; c < items.size(); c++)
		{
			addItem(items.get(c));
		}
		notifyItemRangeInserted(mDataset.size() - items.size() - 1, items.size());
	}

	public void addItemAt(Object item, int index)
	{
		if(index >= mDataset.size() - 1)
		{
			throw new IndexOutOfBoundsException("Index is out of bounds on EasyRecyclerView's items.");
		}
		mDataset.add(index, item);
		notifyItemInserted(index);
	}

	public void removeItem(int index)
	{
		if(index >= mDataset.size() - 1)
		{
			throw new IndexOutOfBoundsException("Index is out of bounds on EasyRecyclerView's items.");
		}
		mDataset.remove(index);
		notifyItemRemoved(index);
		//notifyItemRangeChanged(index, getItemCount()-index);
	}

	public void removeAll()
	{
		Object loader = mDataset.get(mDataset.size() - 1);
		mDataset.clear();
		mDataset.add(loader);
		notifyDataSetChanged();
	}

	public void openLoading()
	{
		if(mLoaderHolder == null){return;}
		mLoaderHolder.openLoading();
	}

	public void closeLoading()
	{
		if(mLoaderHolder == null){return;}
		mLoaderHolder.closeLoading();
	}
}
