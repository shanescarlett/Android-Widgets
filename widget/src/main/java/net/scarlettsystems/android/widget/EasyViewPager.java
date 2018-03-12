package net.scarlettsystems.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * A wrapper widget for Android's ViewPager that simplifies instantiation and configuration
 * of paged views. EasyViewPager eliminates the the need to specify a Pager Adapter. A generic
 * instance of the adapter is created and managed internally within the EasyViewPager instance.
 * Adding and removing pages are facilitated by simple method calls on this instance,
 * by passing in Views.
 *
 * @author Shane Scarlett
 * @version 1.0.0
 * @see ViewPager
 */
public class EasyViewPager extends ViewPager
{
	private ScarlettPagerAdapter mAdapter;
	private boolean mSwipeable = true;

	public EasyViewPager(Context context)
	{
		super(context);
		initialise();
	}

	public EasyViewPager(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		applyAttributes(context, attrs);
		initialise();
	}

	private void applyAttributes(Context context, AttributeSet attrs)
	{
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EasyViewPager, 0, 0);
		try
		{
			mSwipeable = ta.getBoolean(R.styleable.EasyViewPager_swipeable, true);
		}
		finally
		{
			ta.recycle();
		}
	}

	private void initialise()
	{
		mAdapter = new ScarlettPagerAdapter();
		this.setAdapter(mAdapter);
	}

	/**
	 * Add a view as a page to EasyViewPager. Page is automatically added to the end.
	 *
	 * @param view view to add as page
	 */
	public void addPage(View view)
	{
		mAdapter.addView(view);
	}

	/**
	 * Add a view as a page to EasyViewPager at a specified index.
	 *
	 * @param view view to add as page
	 * @param index zero-based index at which to add page
	 */
	public void addPage(View view, int index)
	{
		mAdapter.addView(view, index);
	}

	/**
	 * Return the View of a page in EasyViewPager
	 *
	 * @param index zero-based index of page to return
	 * @return view of requested page
	 */
	public View getPage(int index)
	{
		return mAdapter.getView(index);
	}

	/**
	 * Remove a page in EasyViewPager
	 *
	 * @param index zero-based index of page to remove
	 */
	public void removePage(int index)
	{
		mAdapter.removeView(index);
	}

	/**
	 * Enable manual swiping action between pages from the user.
	 *
	 */
	public void enableSwipe()
	{
		mSwipeable = true;
	}

	/**
	 * Disable manual swiping action between pages from the user. When disabled,
	 * pages can only be changed programmatically.
	 *
	 */
	public void disableSwipe()
	{
		mSwipeable = false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event)
	{
		if(mSwipeable)
		{
			return super.onInterceptTouchEvent(event);
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if(mSwipeable)
		{
			return super.onTouchEvent(event);
		}
		else
		{
			return false;
		}
	}
}
