package net.scarlettsystems.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

class CardAnimator extends SimpleItemAnimator
{

	private Context mContext;
	private RecyclerView mRecyclerView;
	private float mInterpFactor = 2.0f;
	private int mStaggerDelay = 64;
	private int mDuration = 200;
	private int mDirection = 0;
	private int mTranslationAmount = 100;

	public static final int SLIDE_FROM_BOTTOM = 0;
	public static final int SLIDE_FROM_TOP = 1;
	public static final int SLIDE_FROM_LEFT = 2;
	public static final int SLIDE_FROM_RIGHT = 3;
	public static final int DIRECTION_IN = 0;
	public static final int DIRECTION_OUT = 1;

	public CardAnimator(Context context, RecyclerView recyclerView)
	{
		mContext = context;
		mRecyclerView = recyclerView;
	}

	@Override
	public boolean animateRemove(final RecyclerView.ViewHolder holder)
	{
		int[] firstVisibleItems = ((StaggeredGridLayoutManager)mRecyclerView.getLayoutManager()).findFirstVisibleItemPositions(null);
		int firstVisibleItem = firstVisibleItems[firstVisibleItems.length - 1];
		holder.itemView.setAlpha(1.0f);
		Animator animator = getAnimator(mDirection, DIRECTION_OUT);
		animator.setDuration(mDuration);
		animator.setInterpolator(new AccelerateInterpolator(mInterpFactor));
		animator.setTarget(holder.itemView);
		animator.setStartDelay(mStaggerDelay * Math.max(0, holder.getLayoutPosition() - firstVisibleItem));
		animator.addListener(new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationEnd(Animator animation)
			{
				dispatchRemoveFinished(holder);
				super.onAnimationEnd(animation);
			}
		});
		animator.start();
		return true;
	}

	@Override
	public boolean animateAdd(final RecyclerView.ViewHolder holder)
	{
		int[] firstVisibleItems = ((StaggeredGridLayoutManager)mRecyclerView.getLayoutManager()).findFirstVisibleItemPositions(null);
		int firstVisibleItem = firstVisibleItems[firstVisibleItems.length - 1];
		holder.itemView.setAlpha(0.0f);
		Animator animator = getAnimator(mDirection, DIRECTION_IN);
		animator.setDuration(mDuration);
		animator.setInterpolator(new DecelerateInterpolator(mInterpFactor));
		animator.setTarget(holder.itemView);
		animator.setStartDelay(mStaggerDelay * Math.max(0, holder.getLayoutPosition() - firstVisibleItem));
		animator.addListener(new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationEnd(Animator animation)
			{
				dispatchAddFinished(holder);
				super.onAnimationEnd(animation);
			}
		});
		animator.start();
		return true;
	}

	@Override
	public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY)
	{
		float deltaX = toX - fromX;
		holder.itemView.animate().translationX(deltaX).setDuration(mDuration).start();
		return false;
	}

	@Override
	public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromLeft, int fromTop, int toLeft, int toTop)
	{
		return false;
	}

	@Override
	public void runPendingAnimations()
	{

	}

	@Override
	public void endAnimation(RecyclerView.ViewHolder item)
	{

	}

	@Override
	public void endAnimations()
	{

	}

	@Override
	public boolean isRunning()
	{
		return false;
	}

	private Animator getAnimator(int from, int direction)
	{
		int startPos, finalPos, multiplier;
		String axisName;

		switch(direction)
		{
			case DIRECTION_IN:
			{
				startPos = mTranslationAmount;
				finalPos = 0;
				break;
			}
			case DIRECTION_OUT:
			{
				startPos = 0;
				finalPos = mTranslationAmount;
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Invalid direction.");
			}
		}

		switch(from)
		{
			case SLIDE_FROM_BOTTOM:
			case SLIDE_FROM_TOP:
			{
				axisName = "translationY";
				break;
			}
			case SLIDE_FROM_LEFT:
			case SLIDE_FROM_RIGHT:
			{
				axisName = "translationX";
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Invalid direction.");
			}
		}

		switch(from)
		{
			case SLIDE_FROM_BOTTOM:
			case SLIDE_FROM_RIGHT:
			{
				multiplier = 1;
				break;
			}
			case SLIDE_FROM_TOP:
			case SLIDE_FROM_LEFT:
			{
				multiplier = -1;
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Invalid direction.");
			}
		}

		startPos *= multiplier;
		finalPos *= multiplier;
		ObjectAnimator animator = new ObjectAnimator();
		animator.setPropertyName(axisName);
		animator.setIntValues(startPos, finalPos);
		return animator;
	}

	public float getInterpolationFactor()
	{
		return mInterpFactor;
	}

	public void setInterpolationFactor(float value)
	{
		this.mInterpFactor = value;
	}

	public int getStaggerDelay()
	{
		return mStaggerDelay;
	}

	public void setStaggerDelay(int value)
	{
		this.mStaggerDelay = value;
	}

	public int getAnimationDuration()
	{
		return mDuration;
	}

	public void setAnimationDuration(int value)
	{
		this.mDuration = value;
	}

	public int getDirection()
	{
		return mDirection;
	}

	public void setDirection(int value)
	{
		this.mDirection = value;
	}

	public int getTranslationAmount()
	{
		return mTranslationAmount;
	}

	public void setTranslationAmount(int value)
	{
		this.mTranslationAmount = value;
	}
}
