package net.scarlettsystems.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
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
 * <p>Note: For very large amounts of pages, you should manually use {@link ViewPager} with a
 * {@link android.support.v4.app.FragmentStatePagerAdapter}, for performance concerns.
 *
 * @author Shane Scarlett
 * @version 1.1.0
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
		mAdapter = new ScarlettPagerAdapter(((AppCompatActivity)getContext()).getSupportFragmentManager());
		this.setAdapter(mAdapter);
	}

	/**
	 * Add a view as a page to EasyViewPager. Page is automatically added to the end.
	 *
	 * @param view view to add as page
	 */
	@SuppressWarnings("unused")
	public void addPage(View view)
	{
		ViewWrapperFragment fragment = new ViewWrapperFragment();
		fragment.setView(view);
		mAdapter.addFragment(fragment);
	}

	/**
	 * Add a view as a page to EasyViewPager at a specified index.
	 *
	 * @param view view to add as page
	 * @param index zero-based index at which to add page
	 */
	@SuppressWarnings("unused")
	public void addPage(View view, int index)
	{
		ViewWrapperFragment fragment = new ViewWrapperFragment();
		fragment.setView(view);
		mAdapter.addFragment(fragment, index);
	}

	/**
	 * Add a view as a page to EasyViewPager. Page is automatically added to the end.
	 *
	 * @param fragment fragment to add as page
	 */
	@SuppressWarnings("unused")
	public void addPage(Fragment fragment)
	{
		mAdapter.addFragment(fragment);
	}

	/**
	 * Add a view as a page to EasyViewPager at a specified index.
	 *
	 * @param fragment fragment to add as page
	 * @param index zero-based index at which to add page
	 */
	@SuppressWarnings("unused")
	public void addPage(Fragment fragment, int index)
	{
		mAdapter.addFragment(fragment, index);
	}

	/**
	 * Return the View of a page in EasyViewPager
	 *
	 * @param index zero-based index of page to return
	 * @return view of requested page
	 */
	@SuppressWarnings("unused")
	public Object getPage(int index)
	{
		return mAdapter.getFragment(index);
	}

	/**
	 * Remove a page in EasyViewPager
	 *
	 * @param index zero-based index of page to remove
	 */
	@SuppressWarnings("unused")
	public void removePage(int index)
	{
		mAdapter.removeFragment(index);
	}

	/**
	 * Enable manual swiping action between pages from the user.
	 *
	 */
	@SuppressWarnings("unused")
	public void enableSwipe()
	{
		mSwipeable = true;
	}

	/**
	 * Disable manual swiping action between pages from the user. When disabled,
	 * pages can only be changed programmatically.
	 *
	 */
	@SuppressWarnings("unused")
	public void disableSwipe()
	{
		mSwipeable = false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event)
	{
		return mSwipeable && super.onInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		return mSwipeable && super.onTouchEvent(event);
	}
}
