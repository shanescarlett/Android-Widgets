package net.scarlettsystems.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.ArrayList;

class ScarlettRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
	private boolean mAnimationEnabled = true;
	private ArrayList<Object> mDataset = new ArrayList<>();
	private ArrayList<Integer> mTypeset = new ArrayList<>();
	private OnItemClickListener mItemClickListener = null;
	private OnItemLongClickListener mItemLongClickListener = null;
	private SparseArray<ItemViewListener> mItemViewListeners = new SparseArray<>();
	private ItemViewListener mErrorListener;
	private RecyclerView mRecyclerView;
	private LoaderHolder mLoaderHolder;
	private EmptyPromptHolder mEmptyPromptHolder;
	private View mEmptyPromptView, mDummyView;

	private Interpolator mLoaderShowInterpolator = new LinearInterpolator();
	private Interpolator mLoaderHideInterpolator = new LinearInterpolator();

	private int mDuration;
	private int mLoaderHeight = 100;
	private int[] mLoaderPadding = {0, 0, 0, 0};

	private static final int TYPE_LOADER = -1;
	private static final int TYPE_EMPTY_PROMPT = -2;

	ScarlettRecyclerAdapter()
	{
		setHasStableIds(true);
		mDataset.add(new LoaderObject());
		mDataset.add(new EmptyPromptObject());
		mErrorListener = new ItemViewListener()
		{
			@Override
			public View OnCreateItemView(ViewGroup parent)
			{
				Log.e("EasyRecyclerView","View request for unrecognised item. Set OnCreateItemView or layout resource for this item type.");
				return new View(parent.getContext());
			}

			@Override
			public void OnBindItemView(View v, SparseArray<View> cache, Object item)
			{
				Log.e("EasyRecyclerView","Bind request for unrecognised item. Set OnBindItemView for this item type.");
			}
		};
	}

	//Holder Classes

	private abstract class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
	{
		ViewHolder(@NonNull View itemView)
		{
			super(itemView);
		}
	}

	private class ItemHolder extends ViewHolder
	{
		private View mView;
		private Object mItem;
		private SparseArray<View> mChildViewCache = new SparseArray<>();

		ItemHolder(View view)
		{
			super(view);
			mView = view;
			view.setOnClickListener(this);
			view.setOnLongClickListener(this);
			cacheChildren(mView);
		}

		@Override
		public void onClick(View v)
		{
			if(mItemClickListener == null){return;}
			mItemClickListener.OnItemClick(v, mItem);
		}

		@Override
		public boolean onLongClick(View v)
		{
			if(mItemLongClickListener == null){return false;}
			mItemLongClickListener.OnItemLongClick(v, mItem);
			return true;
		}

		void cacheChildren(View view)
		{
			mChildViewCache.append(view.getId(), view);
			if(view instanceof ViewGroup)
			{
				for(int c = 0; c < ((ViewGroup) view).getChildCount(); c++)
				{
					cacheChildren(((ViewGroup) view).getChildAt(c));
				}
			}
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

		SparseArray<View> getViewCache()
		{
			return mChildViewCache;
		}
	}

	private class EmptyPromptHolder extends ViewHolder
	{
		private View mView;
		private boolean isShown;

		EmptyPromptHolder(View itemView)
		{
			super(itemView);
			mView = itemView;
		}

		@Override
		public void onClick(View view){}

		@Override
		public boolean onLongClick(View view)
		{
			return false;
		}

		void showEmptyPrompt()
		{
			//Log.e("SRA", "showing empty" + mView.hashCode());
			mView.setVisibility(View.VISIBLE);
			if(mAnimationEnabled)
			{
				ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
				animator.setDuration(mDuration);
				animator.setInterpolator(mLoaderShowInterpolator);
				animator.setStartDelay(mDuration);
				animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
				{
					@Override
					public void onAnimationUpdate(ValueAnimator animator)
					{
						mView.setAlpha(animator.getAnimatedFraction());
					}
				});
				animator.start();
			}
			else
			{
				mView.setAlpha(1.0f);
			}
			isShown = true;
		}

		void hideEmptyPrompt()
		{
			//Log.e("SRA", "hiding empty " + mView.hashCode());
			if(mAnimationEnabled)
			{
				ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
				animator.setDuration(mDuration);
				animator.setInterpolator(mLoaderShowInterpolator);
				animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
				{
					@Override
					public void onAnimationUpdate(ValueAnimator animator)
					{
						mView.setAlpha(1f - animator.getAnimatedFraction());
						if(animator.getAnimatedFraction() == 1f)
						{
							//mView.setVisibility(View.GONE);
						}
					}
				});
				animator.start();
			}
			else
			{
				//mView.setVisibility(View.GONE);
			}
			isShown = false;
		}
	}

	private class LoaderHolder extends ViewHolder
	{
		private RelativeLayout loaderContainer, paddingView;
		private ProgressBar loader;
		private boolean isShown = false;

		LoaderHolder(View itemView)
		{
			super(itemView);
			paddingView = itemView.findViewById(R.id.net_scarlettsystems_android_widget_cardloader_padding);
			loaderContainer = itemView.findViewById(R.id.net_scarlettsystems_android_widget_cardloader_loader_container);
			loader = itemView.findViewById(R.id.net_scarlettsystems_android_widget_cardloader_loader);
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
				animator.setInterpolator(mLoaderShowInterpolator);
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
			isShown = true;
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
				animator.setInterpolator(mLoaderHideInterpolator);
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
			}
			else
			{
				loaderContainer.setPadding(0,0,0,0);
				Helpers.setViewHeight(loader, 0);
				loaderContainer.setVisibility(View.GONE);
			}
			isShown = false;
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

		@Override
		public void onClick(View view){}

		@Override
		public boolean onLongClick(View view)
		{
			return false;
		}
	}

	private class LoaderObject
	{

	}

	private class EmptyPromptObject
	{

	}

	//Interfaces

	public interface OnItemClickListener
	{
		void OnItemClick(View v, Object object);
	}

	public interface OnItemLongClickListener
	{
		void OnItemLongClick(View v, Object object);
	}

	public interface ItemViewListener
	{
		View OnCreateItemView(ViewGroup parent);
		void OnBindItemView(View v, SparseArray<View> cache, Object item);
	}

	//Callback Methods
	@Override
	public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
	{
		super.onAttachedToRecyclerView(recyclerView);
		mRecyclerView = recyclerView;
//		@SuppressLint("InflateParams") View view = LayoutInflater
//				.from(recyclerView.getContext())
//				.inflate(R.layout.net_scarlettsystems_android_widget_cardloader, recyclerView, false);
//		mLoaderHolder = new LoaderHolder(view);
		mDummyView = new View(recyclerView.getContext());
		if(mEmptyPromptView == null)
		{
			mEmptyPromptView = mDummyView;
		}
		mEmptyPromptHolder = new EmptyPromptHolder(mEmptyPromptView);
	}

	@Override
	public @NonNull
	ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		if(viewType == TYPE_LOADER)
		{
			View view = LayoutInflater
					.from(parent.getContext())
					.inflate(R.layout.net_scarlettsystems_android_widget_cardloader, parent, false);
			mLoaderHolder = new LoaderHolder(view);
			return mLoaderHolder;
		}
		else if(viewType == TYPE_EMPTY_PROMPT)
		{
			return mEmptyPromptHolder;
		}
		else if(viewType >= 0)
		{
			View view = mItemViewListeners
					.get(viewType, mErrorListener)
					.OnCreateItemView(parent);
			if(view == null)
			{
				throw new IllegalArgumentException("OnCreateItemView must return a view.");
			}
			return new ItemHolder(view);
		}
		else
		{
			throw new IllegalArgumentException();
		}

	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position)
	{
		if(vh instanceof ItemHolder)
		{
			ItemHolder h = (ItemHolder)vh;
			h.setItem(mDataset.get(position));
			mItemViewListeners
					.get(mTypeset.get(position), mErrorListener)
					.OnBindItemView(h.getView(), h.getViewCache(), h.getItem());
		}
	}

	//Getters & Setters
	void setEmptyPromptView(View v)
	{
		if(v == null)
		{
			mEmptyPromptHolder = new EmptyPromptHolder(mDummyView);
		}
		else
		{

			mEmptyPromptHolder = new EmptyPromptHolder(v);
		}
	}

	void setOnItemClickListener(OnItemClickListener l)
	{
		mItemClickListener = l;
	}

	void setOnItemLongClickListener(OnItemLongClickListener l)
	{
		mItemLongClickListener = l;
	}

	void addOnItemViewListener(ItemViewListener l, int typeCode)
	{
		mItemViewListeners.append(typeCode, l);
	}

	void removeOnItemViewListener(int typeCode)
	{
		mItemViewListeners.remove(typeCode);
	}

	void clearOnItemViewListeners()
	{
		mItemViewListeners.clear();
	}

	void setAnimationDuration(int value)
	{
		mDuration = value;
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
		return mDataset.size() - 2;
	}

	@Override
	public int getItemViewType(int position)
	{
		if(mDataset.get(position) instanceof LoaderObject)
		{
			return TYPE_LOADER;
		}
		else if(mDataset.get(position) instanceof EmptyPromptObject)
		{
			return TYPE_EMPTY_PROMPT;
		}
		else
		{
			int index = mTypeset.get(position);
			if(index == -1){throw new IllegalStateException();}
			return index;
		}
	}

	//Control Methods

	void addItem(Object item, int typeCode)
	{
		mDataset.add(getItemCountProtected(), item);
		mTypeset.add(typeCode);
		notifyInternal();
		notifyItemInserted(getItemCountProtected());
	}

	void addItems(ArrayList<?> items, int typeCode)
	{
		mDataset.addAll(getItemCountProtected(), items);
		for(int c = 0; c < items.size(); c++){mTypeset.add(typeCode);}
		notifyInternal();
		notifyItemRangeInserted(getItemCountProtected(), items.size());
	}

	void addItemAt(Object item, int index, int typeCode)
	{
		mDataset.add(index, item);
		mTypeset.add(index, typeCode);
		notifyInternal();
		notifyItemInserted(index);
	}

	void removeItem(int index)
	{
		if(index >= getItemCountProtected())
		{
			Log.e("SRA", "nope");
		}
		mDataset.remove(index);
		mTypeset.remove(index);
		notifyInternal();
		notifyItemRemoved(index);
	}

	void replaceItem(Object item, int index, int typeCode)
	{
		mDataset.set(index, item);
		mTypeset.set(index, typeCode);
		notifyInternal();
		notifyItemChanged(index);
	}

	void removeAll()
	{
		int originalItemCount = getItemCountProtected();
		Object loader = mDataset.get(originalItemCount);
		Object emptyPrompt = mDataset.get(originalItemCount + 1);
		mDataset.clear();
		mTypeset.clear();
		mDataset.add(loader);
		mDataset.add(emptyPrompt);
		notifyDataSetChanged();
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
		return mLoaderHolder != null && mLoaderHolder.isShown();
	}

	void showEmptyPrompt()
	{
		if(mEmptyPromptHolder == null){return;}
		mEmptyPromptHolder.showEmptyPrompt();
	}

	void hideEmptyPrompt()
	{
		if(mEmptyPromptHolder == null){return;}
		mEmptyPromptHolder.hideEmptyPrompt();
	}

	void notifyInternal()
	{
		if(getItemCountProtected() == 0)
		{
			showEmptyPrompt();
		}
		else
		{
			hideEmptyPrompt();
		}
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

	void setLoaderShowInterpolator(Interpolator interpolator)
	{
		mLoaderShowInterpolator = interpolator;
	}

	void setLoaderHideInterpolator(Interpolator interpolator)
	{
		mLoaderHideInterpolator = interpolator;
	}

	void setBottomPadding(int padding)
	{
		mLoaderHolder.setBottomPadding(padding);
	}
}
