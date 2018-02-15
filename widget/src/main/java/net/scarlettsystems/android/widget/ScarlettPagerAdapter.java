package net.scarlettsystems.android.widget;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

class ScarlettPagerAdapter extends PagerAdapter
{
	private ArrayList<View> mViews = new ArrayList<View>();

	@Override
	public int getItemPosition (Object object)
	{
		int index = mViews.indexOf (object);
		if (index == -1)
			return POSITION_NONE;
		else
			return index;
	}

	@Override
	public Object instantiateItem (ViewGroup container, int position)
	{
		View v = mViews.get (position);
		container.addView (v);
		return v;
	}

	@Override
	public int getCount ()
	{
		return mViews.size();
	}

	@Override
	public boolean isViewFromObject (View view, Object object)
	{
		return view == object;
	}

	@Override
	public void destroyItem (ViewGroup container, int position, Object object)
	{
		container.removeView (mViews.get (position));
	}

	public int addView (View view)
	{
		return addView (view, mViews.size());
	}

	public int addView (View v, int position)
	{
		mViews.add (position, v);
		notifyDataSetChanged();
		return position;
	}

	public void removeView(int position)
	{
		mViews.remove(position);
		notifyDataSetChanged();
	}

	public int removeView (ViewPager pager, View view)
	{
		return removeView (pager, mViews.indexOf (view));
	}

	public int removeView (ViewPager pager, int position)
	{
		pager.setAdapter (null);
		mViews.remove (position);
		pager.setAdapter (this);
		notifyDataSetChanged();
		return position;
	}

	public View getView (int position)
	{
		return mViews.get (position);
	}
}