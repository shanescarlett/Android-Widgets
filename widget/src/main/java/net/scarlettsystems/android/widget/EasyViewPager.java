package net.scarlettsystems.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

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

	public void addPage(View view)
	{
		mAdapter.addView(view);
	}

	public void addPage(View view, int index)
	{
		mAdapter.addView(view, index);
	}

	public View getPage(int index)
	{
		return mAdapter.getView(index);
	}

	public void removePage(int index)
	{
		mAdapter.removeView(index);
	}

	public void enableSwipe()
	{
		mSwipeable = true;
	}

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
