package net.scarlettsystems.android.widget;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

class ScarlettPagerAdapter extends FragmentPagerAdapter
{
	private ArrayList<Fragment> mFragments = new ArrayList<>();

	ScarlettPagerAdapter(FragmentManager fm)
	{
		super(fm);
	}

	@Override
	public Fragment getItem(int position)
	{
		return mFragments.get(position);
	}

	@Override
	public int getItemPosition (@NonNull Object object)
	{
		int index = mFragments.indexOf(object);
		if (index == -1)
			return POSITION_NONE;
		else
			return index;
	}

//	@Override
//	public Object instantiateItem (ViewGroup container, int position)
//	{
//		View v = mFragments.get (position);
//		container.addView (v);
//		return v;
//	}

	@Override
	public int getCount ()
	{
		return mFragments.size();
	}

//	@Override
//	public boolean isViewFromObject (View view, Object object)
//	{
//		return view == object;
//	}
//
//	@Override
//	public void destroyItem (ViewGroup container, int position, Object object)
//	{
//		container.removeView (mFragments.get (position));
//	}

	public void addFragment (Fragment fragment)
	{
		addFragment(fragment, mFragments.size());
	}

	public void addFragment (Fragment fragment, int position)
	{
		mFragments.add (position, fragment);
		notifyDataSetChanged();
	}

	public void removeFragment(int position)
	{
		mFragments.remove(position);
		notifyDataSetChanged();
	}

	public int removeFragment (ViewPager pager, Fragment view)
	{
		return removeFragment (pager, mFragments.indexOf (view));
	}

	public int removeFragment (ViewPager pager, int position)
	{
		pager.setAdapter (null);
		mFragments.remove (position);
		pager.setAdapter (this);
		notifyDataSetChanged();
		return position;
	}

	public Fragment getFragment (int position)
	{
		return mFragments.get (position);
	}
}