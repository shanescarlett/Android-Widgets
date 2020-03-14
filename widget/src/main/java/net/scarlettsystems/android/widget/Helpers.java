package net.scarlettsystems.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

class Helpers
{
	public static void setViewHeight(View view, int height)
	{
		ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
		layoutParams.height = height;
		view.setLayoutParams(layoutParams);
	}

	public static void setViewWidth(View view, int width)
	{
		ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
		layoutParams.width = width;
		view.setLayoutParams(layoutParams);
	}

	public static boolean softButtonsExist(Context context)
	{
		int id = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
		return id > 0 && context.getResources().getBoolean(id);
	}

	public static int getNavBarHeight(Context context)
	{
		Resources resources = context.getResources();
		int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
		if (resourceId > 0) {
			return resources.getDimensionPixelSize(resourceId);
		}
		return 0;
	}

	public static int Dp2Pix(int dp, Context context)
	{
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
		return px;
	}

}
