package net.scarlettsystems.android.widget;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * A wrapper widget for Android's RecyclerView that simplifies instantiation and configuration
 * of dynamic lists. EasyRecyclerView eliminates the the need to specify a RecyclerView Adapter.
 * A generic instance of the adapter is created and managed internally within the EasyRecyclerView
 * instance.
 *
 * Binding of views for EasyRecyclerView's elements are specified through callbacks and should be
 * configured upon instantiation.
 *
 * @author Shane Scarlett
 * @version 1.0.0
 * @see RecyclerView
 */
public class EasyRecyclerView extends RecyclerView
{
	//Members
	private Context mContext;
	private ScarlettRecyclerAdapter mAdapter;
	private CardAnimator mAnimator;
	private @LayoutRes Integer mItemLayoutResource = null;

	//Callbacks
	private OnItemClickListener mItemClickListener = null;
	private OnCreateItemViewListener mOnCreateItemViewListener = null;
	private OnBindItemViewListener mOnBindItemViewListener = null;
	private OnLoadRequestListener mOnLoadRequestListener = null;

	private int mAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
	private float mInterpolationFactor = 1.0f;
	private boolean mEnabled = true;

	@SuppressWarnings("WeakerAccess")
	@IntDef({HORIZONTAL, VERTICAL})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Orientation {}

	public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;
	public static final int VERTICAL = OrientationHelper.VERTICAL;

	@SuppressWarnings("WeakerAccess")
	@IntDef({NORTH, SOUTH, EAST, WEST})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Direction {}

	public static final int NORTH = CardAnimator.NORTH;
	public static final int SOUTH = CardAnimator.SOUTH;
	public static final int EAST = CardAnimator.EAST;
	public static final int WEST = CardAnimator.WEST;

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
	/**
	 * Interface definition for a callback to be invoked when an item within the EasyRecyclerView
	 * is clicked.
	 *
	 */
	public interface OnItemClickListener
	{
		/**
		 * Called when an item view is clicked.
		 *
		 * @param v view of the object that was clicked
		 * @param object data object associated with the item
		 */
		void OnItemClick(View v, Object object);
	}

	/**
	 * Interface definition for a callback to be invoked when a View for an item needs to be
	 * created.
	 *
	 */
	public interface OnCreateItemViewListener
	{
		/**
		 * Called when the view for an item within the EasyRecyclerView needs to be created.
		 *
		 * @return the item view to be displayed
		 */
		View OnCreateItemView();
	}

	/**
	 * Interface definition for a callback to be invoked when a data object is associated with
	 * the item's View within the EasyRecyclerView.
	 *
	 */
	public interface OnBindItemViewListener
	{
		/**
		 * Called when the data needs to be bound to the item's view.
		 * The necessary data should be read from
		 * {@code item} and bound to {@code view}.
		 *
		 * @param view instantiated view of the item
		 * @param item data object of the item
		 */
		void OnBindItemView(View view, Object item);
	}

	/**
	 * Interface definition for a callback to be invoked when user reaches the end of the list of
	 * items and more items should be loaded.
	 *
	 */
	public interface OnLoadRequestListener
	{
		void OnLoadRequest();
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
		addOnScrollListener(new OnScrollListener()
		{
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState)
			{
				if (!recyclerView.canScrollVertically(1) && newState == SCROLL_STATE_IDLE)
				{
					if(mOnLoadRequestListener == null){return;}
					if(mAdapter.isLoaderShown()){return;}
					mOnLoadRequestListener.OnLoadRequest();
				}
			}
		});
	}

	private void configureAdapter()
	{
		mAdapter = new ScarlettRecyclerAdapter();
		setAdapter(mAdapter);
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
				if(mItemClickListener == null){return;}
				if(!mEnabled){return;}
				mItemClickListener.OnItemClick(v, object);
			}
		});
	}

	private void configureLayoutManager()
	{
		super.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
	}

	private void configureAnimator()
	{
		mAnimator = new CardAnimator();
		setItemAnimator(mAnimator);
	}

	//Public Settings Methods

	//Callbacks
	/**
	 * Set the callback to be invoked when an item within the EasyRecylclerView is clicked.
	 *
	 * @param l {@link OnItemClickListener}
	 */
	@SuppressWarnings("unused")
	public void setOnItemClickListener(OnItemClickListener l)
	{
		mItemClickListener = l;
	}

	/**
	 * Set the callback to be invoked when an item's view is to be created.
	 *
	 * <p>Note: This callback does not need to be set if a layout is already configured through
	 * {@link EasyRecyclerView#setItemLayoutResource(int)}. EasyRecyclerVIew will prefer inflating
	 * the view from the specified resource.
	 *
	 * @param l {@link OnCreateItemViewListener}
	 */
	@SuppressWarnings("unused")
	public void setOnCreateItemViewListener(OnCreateItemViewListener l)
	{
		mOnCreateItemViewListener = l;
	}

	/**
	 * Set the callback to be invoked when an item's data is to be bound to its associated view.
	 *
	 * @param l {@link OnBindItemViewListener}
	 */
	@SuppressWarnings("unused")
	public void setOnBindItemViewListener(OnBindItemViewListener l)
	{
		mOnBindItemViewListener = l;
	}

	/**
	 * Set the callback to be invoked when user reaches the end of the list of items and more
	 * items should be loaded.
	 *
	 * @param l {@link OnLoadRequestListener}
	 */
	@SuppressWarnings("unused")
	public void setOnLoadRequestListener(OnLoadRequestListener l)
	{
		mOnLoadRequestListener = l;
	}

	//Behaviour
	/**
	 * Enables user interaction with EasyRecyclerView.
	 *
	 */
	@SuppressWarnings("unused")
	public void enable()
	{
		mEnabled = true;
	}

	/**
	 * Disables all user input to EasyRecyclerView.
	 *
	 */
	@SuppressWarnings("unused")
	public void disable()
	{
		mEnabled = false;
	}

	/**
	 * Set enabled state for EasyRecyclerView.
	 *
	 * @param enabled desired enabled state
	 */
	@SuppressWarnings("unused")
	public void setEnabled(boolean enabled)
	{
		mEnabled = enabled;
	}

	//Appearance
	/**
	 * Set the layout resource to be used to inflate each item's view.
	 *
	 * <p>Note: This callback does not need to be set if view creating is already configured through
	 * {@link EasyRecyclerView#setOnCreateItemViewListener(OnCreateItemViewListener)}.
	 * EasyRecyclerVIew will prefer inflating the view from the specified resource.
	 *
	 * @param resId layout resource to be used for item view creation
	 */
	@SuppressWarnings("unused")
	public void setItemLayoutResource(@LayoutRes int resId)
	{
		mItemLayoutResource = resId;
	}

	/**
	 * Set the layout manager for the EasyRecyclerView.
	 *
	 * @param lm layout manager to be used
	 */
	@Override
	public void setLayoutManager(RecyclerView.LayoutManager lm)
	{
		super.setLayoutManager(lm);
	}

	/**
	 * Automatically create and set the EasyRecyclerView's layout manager with specified configuration.
	 *
	 * @param spanCount the number of columns (or rows if horizontal) the list should be presented in
	 * @param orientation orientation of list in {@link OrientationHelper#HORIZONTAL} or {@link OrientationHelper#VERTICAL}
	 * @param staggered specification of whether views in list should be staggered
	 * @param reverseLayout specification of whether list should be reversed
	 */
	@SuppressWarnings("unused")
	public void setLayoutManager(int spanCount, @Orientation int orientation, boolean staggered, boolean reverseLayout)
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

	/**
	 * Set the height of the loader that appears at the end of the list while additional items
	 * are being loaded.
	 *
	 * @param height height in pixels
	 */
	@SuppressWarnings("unused")
	public void setLoaderHeight(int height)
	{
		mAdapter.setLoaderHeight(height);
	}

	/**
	 * Set the amount of padding around the loader.
	 *
	 * @param padding size in pixels
	 */
	@SuppressWarnings("unused")
	public void setLoaderPadding(int padding)
	{
		mAdapter.setLoaderPadding(padding);
	}

	/**
	 * Set the amount of padding above the loader.
	 *
	 * @param padding size in pixels
	 */
	@SuppressWarnings("unused")
	public void setLoaderPaddingTop(int padding)
	{
		mAdapter.setLoaderPaddingTop(padding);
	}

	/**
	 * Set the amount of padding below the loader.
	 *
	 * @param padding size in pixels
	 */
	@SuppressWarnings("unused")
	public void setLoaderPaddingBottom(int padding)
	{
		mAdapter.setLoaderPaddingBottom(padding);
	}

	/**
	 * Set the colour of the loader.
	 *
	 * @param colour colour as {@link android.support.annotation.ColorInt}
	 */
	@SuppressWarnings("unused")
	public void setLoaderColour(int colour)
	{
		mAdapter.setLoaderColour(colour);
	}

	/**
	 * Show the loader that appears at the end of the list.
	 *
	 */
	@SuppressWarnings("unused")
	public void showLoader()
	{
		mAdapter.showLoader();
	}

	/**
	 * Hide the loader that appears at the end of the list.
	 *
	 */
	@SuppressWarnings("unused")
	public void hideLoader()
	{
		mAdapter.hideLoader();
	}

	/**
	 * Set size of padding on the very bottom of the list. Useful when transparent soft navigation
	 * buttons overlay EasyRecyclerView. The padding prevents the last item from being displayed
	 * under the soft navigation buttons.
	 *
	 * @param padding size in pixels
	 */
	@SuppressWarnings("unused")
	public void setBottomPadding(int padding)
	{
		mAdapter.setBottomPadding(padding);
	}

	//Animation
	/**
	 * Enable animations associated with EasyRecyclerView.
	 *
	 */
	@SuppressWarnings("unused")
	public void enableAnimation()
	{
		setItemAnimator(mAnimator);
		mAdapter.setAnimationEnabled(true);
	}

	/**
	 * Disable animations associated with EasyRecyclerView.
	 *
	 */
	@SuppressWarnings("unused")
	public void disableAnimation()
	{
		setItemAnimator(null);
		mAdapter.setAnimationEnabled(false);
	}

	/**
	 * Set enabled state of animations associated with EasyRecyclerView.
	 *
	 * @param enabled enabled state
	 */
	@SuppressWarnings("unused")
	public void setAnimationEnabled(boolean enabled)
	{
		if(enabled)
			enableAnimation();
		else
			disableAnimation();
	}

	/**
	 * Set direction the card should enter from when being added.
	 *
	 * @param direction animation direction
	 */
	@SuppressWarnings("unused")
	public void setCardAddDirection(@Direction int direction)
	{
		mAnimator.setAddDirection(direction);
	}

	/**
	 * Set direction the card should exit towards when being removed.
	 *
	 * @param direction animation direction
	 */
	@SuppressWarnings("unused")
	public void setCardRemoveDirection(@Direction int direction)
	{
		mAnimator.setRemoveDirection(direction);
	}

	/**
	 * Set a single direction for both cards entering and exiting the view.
	 *
	 * @param direction animation direction
	 */
	@SuppressWarnings("unused")
	public void setCardDirection(@Direction int direction)
	{
		mAnimator.setDirection(direction);
	}

	/**
	 * Set the distance item views should travel when animating entrance.
	 *
	 * @param distance translation distance in pixels
	 */
	@SuppressWarnings("unused")
	public void setCardAnimationAmount(int distance)
	{
		mAnimator.setTranslationAmount(distance);
	}

	/**
	 * Set duration of animations associated with EasyRecyclerView.
	 *
	 * @param duration duration in milliseconds
	 */
	@SuppressWarnings("unused")
	public void setAnimationDuration(int duration)
	{
		mAdapter.setAnimationDuration(duration);
		mAnimator.setRemoveDuration(duration);
		mAnimator.setAddDuration(duration);
		mAnimator.setMoveDuration(duration);
		mAnimator.setChangeDuration(duration);
	}

	/**
	 * Set the interpolator to be used when animating cards as they enter EasyRecyclerView.
	 *
	 * @param interpolator {@link Interpolator}
	 */
	@SuppressWarnings("unused")
	public void setCardAddInterpolator(Interpolator interpolator)
	{
		mAnimator.setAddInterpolator(interpolator);
	}

	/**
	 * Set the interpolator to be used when animating cards as they leave EasyRecyclerView.
	 *
	 * @param interpolator {@link Interpolator}
	 */
	@SuppressWarnings("unused")
	public void setCardRemoveInterpolator(Interpolator interpolator)
	{
		mAnimator.setRemoveInterpolator(interpolator);
	}

	/**
	 * Set the interpolator to be used when animating cards as they move within EasyRecyclerView.
	 *
	 * @param interpolator {@link Interpolator}
	 */
	@SuppressWarnings("unused")
	public void setCardMoveInterpolator(Interpolator interpolator)
	{
		mAnimator.setMoveInterpolator(interpolator);
	}

	/**
	 * Set duration of stagger (delay) between animating entrance of each item view.
	 * Item animations are staggered sequentially from the first item being added.
	 *
	 * @param duration stagger duration in milliseconds
	 */
	@SuppressWarnings("unused")
	public void setAnimationStagger(int duration)
	{
		mAnimator.setStaggerDelay(duration);
	}

	/**
	 * Set the interpolation factor of animations associated with EasyRecyclerVIew.
	 *
	 * @param factor degree to which the animation should be eased.
	 *               Setting factor to 1.0f produces an upside-down y=x^2 parabola.
	 *               Increasing factor above 1.0f makes exaggerates the ease-out effect
	 *               (i.e., it starts even faster and ends evens slower)
	 */
	@SuppressWarnings("unused")
	public void setInterpolationFactor(float factor)
	{
		mAdapter.setInterpolationFactor(factor);
		mAnimator.setInterpolationFactor(factor);
	}

	//State

	//Data
	/**
	 * Add item to the end of the dataset displayed in EasyRecyclerView.
	 *
	 * @param item item to add
	 */
	@SuppressWarnings("unused")
	public void addItem(Object item)
	{
		mAdapter.addItem(item);
	}

	/**
	 * Add a set of items to the end of the dataset displayed in EasyRecyclerView.
	 *
	 * @param items list of items to add
	 */
	@SuppressWarnings("unused")
	public void addItems(ArrayList<?> items)
	{
		mAdapter.addItems(items);
	}

	/**
	 * Add item at specified index of the dataset displayed in EasyRecyclerView.
	 *
	 * @param item item to add
	 * @param index destination index
	 */
	@SuppressWarnings("unused")
	public void addItemAt(Object item, int index)
	{
		if(index < 0){throw new IndexOutOfBoundsException();}
		if(index >= mAdapter.getItemCountProtected()){throw new IndexOutOfBoundsException();}
		mAdapter.addItemAt(item, index);
	}

	/**
	 * Remove item at specified index from the dataset displayed in EasyRecyclerVIew.
	 *
	 * @param index target index
	 */
	@SuppressWarnings("unused")
	public void removeItem(int index)
	{
		if(index < 0){throw new IndexOutOfBoundsException();}
		if(index >= mAdapter.getItemCountProtected()){throw new IndexOutOfBoundsException();}
		mAdapter.removeItem(index);
	}

	/**
	 * Clears all data from the dataset.
	 *
	 */
	@SuppressWarnings("unused")
	public void removeAll()
	{
		mAdapter.removeAll();
	}

	/**
	 * Get item at specified index in EasyRecyclerView.
	 *
	 * @param index index in list
	 * @return item
	 */
	@SuppressWarnings("unused")
	public Object getItem(int index)
	{
		if(index < 0){throw new IndexOutOfBoundsException();}
		if(index >= mAdapter.getItemCountProtected()){throw new IndexOutOfBoundsException();}
		return mAdapter.getItem(index);
	}

	/**
	 * Get count number of items starting at a specified index.
	 *
	 * @param startIndex start index of items to get
	 * @param count number of items to return
	 * @return list of items
	 */
	@SuppressWarnings("unused")
	public ArrayList<Object> getItems(int startIndex, int count)
	{
		if(startIndex < 0){throw new IndexOutOfBoundsException();}
		if((startIndex + count) >= mAdapter.getItemCountProtected()){throw new IndexOutOfBoundsException();}
		return mAdapter.getItems(startIndex, count);
	}

	/**
	 * Get all items in EasyRecyclerView.
	 * Returned list may be empty if there are no items in the view.
	 *
	 * @return list of all items
	 */
	@SuppressWarnings("unused")
	public ArrayList<Object> getItems()
	{
		return mAdapter.getItems();
	}

	/**
	 * Get the number of items currently in EasyRecyclerView.
	 *
	 * @return number of items
	 */
	@SuppressWarnings("unused")
	public int getItemCount()
	{
		return mAdapter.getItemCountProtected();
	}

	/**
	 * Get the index of an item if it is within EasyRecyclerView's data set.
	 *
	 * @return number of items
	 */
	@SuppressWarnings("unused")
	public int indexOf(Object item)
	{
		return mAdapter.indexOf(item);
	}
}
