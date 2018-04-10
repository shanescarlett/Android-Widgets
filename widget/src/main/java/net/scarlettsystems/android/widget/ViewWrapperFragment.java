package net.scarlettsystems.android.widget;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewWrapperFragment extends Fragment
{
	private View mView;

	public void setView(View view)
	{
		mView = view;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return mView;
	}
}
