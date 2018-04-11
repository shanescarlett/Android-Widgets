package net.scarlettsystems.android.widget;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;

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

	@Override
	public int getCount ()
	{
		return mFragments.size();
	}

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

	public Fragment getFragment (int position)
	{
		return mFragments.get (position);
	}
}