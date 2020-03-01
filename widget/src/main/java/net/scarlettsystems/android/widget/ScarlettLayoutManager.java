package net.scarlettsystems.android.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

public class ScarlettLayoutManager extends LinearLayoutManager
{
	public ScarlettLayoutManager(Context context)
	{
		super(context);
	}

	public ScarlettLayoutManager(Context context, int orientation, boolean reverseLayout)
	{
		super(context, orientation, reverseLayout);
	}

	public ScarlettLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
	{
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	public boolean supportsPredictiveItemAnimations()
	{
		return false;
	}
}
