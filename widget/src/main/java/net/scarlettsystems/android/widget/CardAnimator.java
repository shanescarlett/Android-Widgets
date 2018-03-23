package net.scarlettsystems.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.view.ViewPropertyAnimator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Augmentation of Android's DefaultItemAnimator, adding fly-in directions, staggering, and
 * other functionality.
 *
 * @see android.support.v7.widget.DefaultItemAnimator
 */
@SuppressWarnings("WeakerAccess")
public class CardAnimator extends SimpleItemAnimator
{
	private static final boolean DEBUG = false;

	private static TimeInterpolator sDefaultInterpolator;

	private float mInterpolationFactor = 1.0f;
	private int mStaggerDelay = 0;
	private int mDuration;
	private int mDirection;
	private int mTranslationAmount = 100;

	private ArrayList<ViewHolder> mPendingRemovals = new ArrayList<>();
	private ArrayList<ViewHolder> mPendingAdditions = new ArrayList<>();
	private ArrayList<MoveInfo> mPendingMoves = new ArrayList<>();
	private ArrayList<ChangeInfo> mPendingChanges = new ArrayList<>();

	private ArrayList<ArrayList<ViewHolder>> mAdditionsList = new ArrayList<>();
	private ArrayList<ArrayList<MoveInfo>> mMovesList = new ArrayList<>();
	private ArrayList<ArrayList<ChangeInfo>> mChangesList = new ArrayList<>();

	private ArrayList<ViewHolder> mAddAnimations = new ArrayList<>();
	private ArrayList<ViewHolder> mMoveAnimations = new ArrayList<>();
	private ArrayList<ViewHolder> mRemoveAnimations = new ArrayList<>();
	private ArrayList<ViewHolder> mChangeAnimations = new ArrayList<>();

	@IntDef({EAST, NORTH, WEST, SOUTH})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Direction
	{
	}

	public static final int EAST = 0;
	public static final int NORTH = 2;
	public static final int WEST = 4;
	public static final int SOUTH = 6;

	public static final int DIRECTION_IN = 0;
	public static final int DIRECTION_OUT = 1;

	private static class MoveInfo
	{
		public ViewHolder holder;
		public int fromX, fromY, toX, toY;

		MoveInfo(ViewHolder holder, int fromX, int fromY, int toX, int toY)
		{
			this.holder = holder;
			this.fromX = fromX;
			this.fromY = fromY;
			this.toX = toX;
			this.toY = toY;
		}
	}

	private static class ChangeInfo
	{
		public ViewHolder oldHolder, newHolder;
		public int fromX, fromY, toX, toY;

		private ChangeInfo(ViewHolder oldHolder, ViewHolder newHolder)
		{
			this.oldHolder = oldHolder;
			this.newHolder = newHolder;
		}

		ChangeInfo(ViewHolder oldHolder, ViewHolder newHolder,
				   int fromX, int fromY, int toX, int toY)
		{
			this(oldHolder, newHolder);
			this.fromX = fromX;
			this.fromY = fromY;
			this.toX = toX;
			this.toY = toY;
		}

		@Override
		public String toString()
		{
			return "ChangeInfo{"
					+ "oldHolder=" + oldHolder
					+ ", newHolder=" + newHolder
					+ ", fromX=" + fromX
					+ ", fromY=" + fromY
					+ ", toX=" + toX
					+ ", toY=" + toY
					+ '}';
		}
	}

	@Override
	public void runPendingAnimations()
	{
		boolean removalsPending = !mPendingRemovals.isEmpty();
		boolean movesPending = !mPendingMoves.isEmpty();
		boolean changesPending = !mPendingChanges.isEmpty();
		boolean additionsPending = !mPendingAdditions.isEmpty();
		if (!removalsPending && !movesPending && !additionsPending && !changesPending)
		{
			// nothing to animate
			return;
		}
		// First, remove stuff
		for (ViewHolder holder : mPendingRemovals)
		{
			animateRemoveImpl(holder);
		}
		mPendingRemovals.clear();
		// Next, move stuff
		if (movesPending)
		{
			final ArrayList<MoveInfo> moves = new ArrayList<>();
			moves.addAll(mPendingMoves);
			mMovesList.add(moves);
			mPendingMoves.clear();
			Runnable mover = new Runnable()
			{
				@Override
				public void run()
				{
					for (MoveInfo moveInfo : moves)
					{
						animateMoveImpl(moveInfo.holder, moveInfo.fromX, moveInfo.fromY,
								moveInfo.toX, moveInfo.toY);
					}
					moves.clear();
					mMovesList.remove(moves);
				}
			};
			if (removalsPending)
			{
				View view = moves.get(0).holder.itemView;
				ViewCompat.postOnAnimationDelayed(view, mover, getRemoveDuration());
			} else
			{
				mover.run();
			}
		}
		// Next, change stuff, to run in parallel with move animations
		if (changesPending)
		{
			final ArrayList<ChangeInfo> changes = new ArrayList<>();
			changes.addAll(mPendingChanges);
			mChangesList.add(changes);
			mPendingChanges.clear();
			Runnable changer = new Runnable()
			{
				@Override
				public void run()
				{
					for (ChangeInfo change : changes)
					{
						animateChangeImpl(change);
					}
					changes.clear();
					mChangesList.remove(changes);
				}
			};
			if (removalsPending)
			{
				ViewHolder holder = changes.get(0).oldHolder;
				ViewCompat.postOnAnimationDelayed(holder.itemView, changer, getRemoveDuration());
			} else
			{
				changer.run();
			}
		}
		// Next, add stuff
		if (additionsPending)
		{
			final ArrayList<ViewHolder> additions = new ArrayList<>();
			additions.addAll(mPendingAdditions);
			mAdditionsList.add(additions);
			mPendingAdditions.clear();
			Runnable adder = new Runnable()
			{
				@Override
				public void run()
				{
					int delay;
					int firstAddingItemIndex = getFirstHolder(additions);
					for (ViewHolder holder : additions)
					{
						delay = (holder.getLayoutPosition() - firstAddingItemIndex) * mStaggerDelay;
						animateAddImpl(holder, delay);
					}
					additions.clear();
					mAdditionsList.remove(additions);
				}
			};
			if (removalsPending || movesPending || changesPending)
			{
				long removeDuration = removalsPending ? getRemoveDuration() : 0;
				long moveDuration = movesPending ? getMoveDuration() : 0;
				long changeDuration = changesPending ? getChangeDuration() : 0;
				long totalDelay = removeDuration + Math.max(moveDuration, changeDuration);
				View view = additions.get(0).itemView;
				ViewCompat.postOnAnimationDelayed(view, adder, totalDelay);
			} else
			{
				adder.run();
			}
		}
	}

	private int getFirstHolder(ArrayList<ViewHolder> additions)
	{
		int firstIndex = Integer.MAX_VALUE;
		for (ViewHolder holder : additions)
		{
			firstIndex = Math.min(holder.getLayoutPosition(), firstIndex);
		}
		return firstIndex;
	}

	private void configureAnimator(View view, @Direction int from, int direction)
	{
		int xMovement, yMovement;
		int xStart, xEnd, yStart, yEnd;
		switch (from)
		{
			case NORTH:
				xMovement = 0;
				yMovement = -mTranslationAmount;
				break;
			case SOUTH:
				xMovement = 0;
				yMovement = mTranslationAmount;
				break;
			case EAST:
				xMovement = mTranslationAmount;
				yMovement = 0;
				break;
			case WEST:
				xMovement = -mTranslationAmount;
				yMovement = 0;
				break;
			default:
				throw new IllegalArgumentException("Invalid direction.");
		}
		switch (direction)
		{
			case DIRECTION_IN:
			{
				xStart = xMovement;
				xEnd = 0;
				yStart = yMovement;
				yEnd = 0;
				break;
			}
			case DIRECTION_OUT:
			{
				xStart = 0;
				xEnd = xMovement;
				yStart = 0;
				yEnd = yMovement;
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Invalid direction.");
			}
		}
		view.setTranslationX(xStart);
		view.setTranslationY(yStart);
		view.animate().translationX(xEnd).translationY(yEnd);
	}

	@Override
	public boolean animateRemove(final ViewHolder holder)
	{
		resetAnimation(holder);
		mPendingRemovals.add(holder);
		return true;
	}

	private void animateRemoveImpl(final ViewHolder holder)
	{
		final View view = holder.itemView;
		final ViewPropertyAnimator animation = view.animate();
		mRemoveAnimations.add(holder);
		configureAnimator(view, mDirection, DIRECTION_OUT);
		animation.alpha(0).setDuration(getRemoveDuration()).setListener(
				new AnimatorListenerAdapter()
				{
					@Override
					public void onAnimationStart(Animator animator)
					{
						dispatchRemoveStarting(holder);
					}

					@Override
					public void onAnimationEnd(Animator animator)
					{
						animation.setListener(null);
						/* View must be ready to be added again in case a removed view is added back
						off screen */
						view.setAlpha(1);
						view.setTranslationX(0);
						view.setTranslationY(0);
						dispatchRemoveFinished(holder);
						mRemoveAnimations.remove(holder);
						dispatchFinishedWhenDone();
					}
				}).start();
	}

	@Override
	public boolean animateAdd(final ViewHolder holder)
	{
		resetAnimation(holder);
		holder.itemView.setAlpha(0);
		mPendingAdditions.add(holder);
		return true;
	}

	private void animateAddImpl(final ViewHolder holder, int delay)
	{
		final View view = holder.itemView;
		final ViewPropertyAnimator animation = view.animate();
		//int firstVisibleItem = getFirstItemPosition(mRecyclerView.getLayoutManager());
		//int startDelay = mStaggerDelay * Math.max(0, holder.getLayoutPosition() - firstVisibleItem);
		mAddAnimations.add(holder);
		configureAnimator(view, mDirection, DIRECTION_IN);
		animation.alpha(1).setStartDelay(delay).setDuration(getAddDuration())
				.setListener(new AnimatorListenerAdapter()
				{
					@Override
					public void onAnimationStart(Animator animator)
					{
						dispatchAddStarting(holder);
					}

					@Override
					public void onAnimationCancel(Animator animator)
					{
						view.setAlpha(1);
						view.setTranslationX(0);
						view.setTranslationY(0);
					}

					@Override
					public void onAnimationEnd(Animator animator)
					{
						animation.setListener(null);
						dispatchAddFinished(holder);
						mAddAnimations.remove(holder);
						dispatchFinishedWhenDone();
					}
				}).start();
	}

	@Override
	public boolean animateMove(final ViewHolder holder, int fromX, int fromY,
							   int toX, int toY)
	{
		final View view = holder.itemView;
		fromX += (int) holder.itemView.getTranslationX();
		fromY += (int) holder.itemView.getTranslationY();
		resetAnimation(holder);
		int deltaX = toX - fromX;
		int deltaY = toY - fromY;
		if (deltaX == 0 && deltaY == 0)
		{
			dispatchMoveFinished(holder);
			return false;
		}
		if (deltaX != 0)
		{
			view.setTranslationX(-deltaX);
		}
		if (deltaY != 0)
		{
			view.setTranslationY(-deltaY);
		}
		mPendingMoves.add(new MoveInfo(holder, fromX, fromY, toX, toY));
		return true;
	}

	void animateMoveImpl(final ViewHolder holder, int fromX, int fromY, int toX, int toY)
	{
		final View view = holder.itemView;
		final int deltaX = toX - fromX;
		final int deltaY = toY - fromY;
		if (deltaX != 0)
		{
			view.animate().translationX(0);
		}
		if (deltaY != 0)
		{
			view.animate().translationY(0);
		}
		final ViewPropertyAnimator animation = view.animate();
		mMoveAnimations.add(holder);
		animation.setDuration(getMoveDuration()).setListener(new AnimatorListenerAdapter()
		{
			@Override
			public void onAnimationStart(Animator animator)
			{
				dispatchMoveStarting(holder);
			}

			@Override
			public void onAnimationCancel(Animator animator)
			{
				if (deltaX != 0)
				{
					view.setTranslationX(0);
				}
				if (deltaY != 0)
				{
					view.setTranslationY(0);
				}
			}

			@Override
			public void onAnimationEnd(Animator animator)
			{
				animation.setListener(null);
				dispatchMoveFinished(holder);
				mMoveAnimations.remove(holder);
				dispatchFinishedWhenDone();
			}
		}).start();
	}

	@Override
	public boolean animateChange(ViewHolder oldHolder, ViewHolder newHolder,
								 int fromX, int fromY, int toX, int toY)
	{
		if (oldHolder == newHolder)
		{
			// Don't know how to run change animations when the same view holder is re-used.
			// run a move animation to handle position changes.
			return animateMove(oldHolder, fromX, fromY, toX, toY);
		}
		final float prevTranslationX = oldHolder.itemView.getTranslationX();
		final float prevTranslationY = oldHolder.itemView.getTranslationY();
		final float prevAlpha = oldHolder.itemView.getAlpha();
		resetAnimation(oldHolder);
		int deltaX = (int) (toX - fromX - prevTranslationX);
		int deltaY = (int) (toY - fromY - prevTranslationY);
		// recover prev translation state after ending animation
		oldHolder.itemView.setTranslationX(prevTranslationX);
		oldHolder.itemView.setTranslationY(prevTranslationY);
		oldHolder.itemView.setAlpha(prevAlpha);
		if (newHolder != null)
		{
			// carry over translation values
			resetAnimation(newHolder);
			newHolder.itemView.setTranslationX(-deltaX);
			newHolder.itemView.setTranslationY(-deltaY);
			newHolder.itemView.setAlpha(0);
		}
		mPendingChanges.add(new ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY));
		return true;
	}

	void animateChangeImpl(final ChangeInfo changeInfo)
	{
		final ViewHolder holder = changeInfo.oldHolder;
		final View view = holder == null ? null : holder.itemView;
		final ViewHolder newHolder = changeInfo.newHolder;
		final View newView = newHolder != null ? newHolder.itemView : null;
		if (view != null)
		{
			final ViewPropertyAnimator oldViewAnim = view.animate().setDuration(
					getChangeDuration());
			mChangeAnimations.add(changeInfo.oldHolder);
			oldViewAnim.translationX(changeInfo.toX - changeInfo.fromX);
			oldViewAnim.translationY(changeInfo.toY - changeInfo.fromY);
			oldViewAnim.alpha(0).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationStart(Animator animator)
				{
					dispatchChangeStarting(changeInfo.oldHolder, true);
				}

				@Override
				public void onAnimationEnd(Animator animator)
				{
					oldViewAnim.setListener(null);
					view.setAlpha(1);
					view.setTranslationX(0);
					view.setTranslationY(0);
					dispatchChangeFinished(changeInfo.oldHolder, true);
					mChangeAnimations.remove(changeInfo.oldHolder);
					dispatchFinishedWhenDone();
				}
			}).start();
		}
		if (newView != null)
		{
			final ViewPropertyAnimator newViewAnimation = newView.animate();
			mChangeAnimations.add(changeInfo.newHolder);
			newViewAnimation.translationX(0).translationY(0).setDuration(getChangeDuration())
					.alpha(1).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationStart(Animator animator)
				{
					dispatchChangeStarting(changeInfo.newHolder, false);
				}

				@Override
				public void onAnimationEnd(Animator animator)
				{
					newViewAnimation.setListener(null);
					newView.setAlpha(1);
					newView.setTranslationX(0);
					newView.setTranslationY(0);
					dispatchChangeFinished(changeInfo.newHolder, false);
					mChangeAnimations.remove(changeInfo.newHolder);
					dispatchFinishedWhenDone();
				}
			}).start();
		}
	}

	private void endChangeAnimation(List<ChangeInfo> infoList, ViewHolder item)
	{
		for (int i = infoList.size() - 1; i >= 0; i--)
		{
			ChangeInfo changeInfo = infoList.get(i);
			if (endChangeAnimationIfNecessary(changeInfo, item))
			{
				if (changeInfo.oldHolder == null && changeInfo.newHolder == null)
				{
					infoList.remove(changeInfo);
				}
			}
		}
	}

	private void endChangeAnimationIfNecessary(ChangeInfo changeInfo)
	{
		if (changeInfo.oldHolder != null)
		{
			endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder);
		}
		if (changeInfo.newHolder != null)
		{
			endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder);
		}
	}

	private boolean endChangeAnimationIfNecessary(ChangeInfo changeInfo, ViewHolder item)
	{
		boolean oldItem = false;
		if (changeInfo.newHolder == item)
		{
			changeInfo.newHolder = null;
		} else if (changeInfo.oldHolder == item)
		{
			changeInfo.oldHolder = null;
			oldItem = true;
		} else
		{
			return false;
		}
		item.itemView.setAlpha(1);
		item.itemView.setTranslationX(0);
		item.itemView.setTranslationY(0);
		dispatchChangeFinished(item, oldItem);
		return true;
	}

	@Override
	public void endAnimation(ViewHolder item)
	{
		final View view = item.itemView;
		// this will trigger end callback which should set properties to their target values.
		view.animate().cancel();
		for (int i = mPendingMoves.size() - 1; i >= 0; i--)
		{
			MoveInfo moveInfo = mPendingMoves.get(i);
			if (moveInfo.holder == item)
			{
				view.setTranslationY(0);
				view.setTranslationX(0);
				dispatchMoveFinished(item);
				mPendingMoves.remove(i);
			}
		}
		endChangeAnimation(mPendingChanges, item);
		if (mPendingRemovals.remove(item))
		{
			view.setAlpha(1);
			dispatchRemoveFinished(item);
		}
		if (mPendingAdditions.remove(item))
		{
			view.setAlpha(1);
			dispatchAddFinished(item);
		}

		for (int i = mChangesList.size() - 1; i >= 0; i--)
		{
			ArrayList<ChangeInfo> changes = mChangesList.get(i);
			endChangeAnimation(changes, item);
			if (changes.isEmpty())
			{
				mChangesList.remove(i);
			}
		}
		for (int i = mMovesList.size() - 1; i >= 0; i--)
		{
			ArrayList<MoveInfo> moves = mMovesList.get(i);
			for (int j = moves.size() - 1; j >= 0; j--)
			{
				MoveInfo moveInfo = moves.get(j);
				if (moveInfo.holder == item)
				{
					view.setTranslationY(0);
					view.setTranslationX(0);
					dispatchMoveFinished(item);
					moves.remove(j);
					if (moves.isEmpty())
					{
						mMovesList.remove(i);
					}
					break;
				}
			}
		}
		for (int i = mAdditionsList.size() - 1; i >= 0; i--)
		{
			ArrayList<ViewHolder> additions = mAdditionsList.get(i);
			if (additions.remove(item))
			{
				view.setAlpha(1);
				dispatchAddFinished(item);
				if (additions.isEmpty())
				{
					mAdditionsList.remove(i);
				}
			}
		}

		// animations should be ended by the cancel above.
		//noinspection PointlessBooleanExpression,ConstantConditions
		if (mRemoveAnimations.remove(item) && DEBUG)
		{
			throw new IllegalStateException("after animation is cancelled, item should not be in "
					+ "mRemoveAnimations list");
		}

		//noinspection PointlessBooleanExpression,ConstantConditions
		if (mAddAnimations.remove(item) && DEBUG)
		{
			throw new IllegalStateException("after animation is cancelled, item should not be in "
					+ "mAddAnimations list");
		}

		//noinspection PointlessBooleanExpression,ConstantConditions
		if (mChangeAnimations.remove(item) && DEBUG)
		{
			throw new IllegalStateException("after animation is cancelled, item should not be in "
					+ "mChangeAnimations list");
		}

		//noinspection PointlessBooleanExpression,ConstantConditions
		if (mMoveAnimations.remove(item) && DEBUG)
		{
			throw new IllegalStateException("after animation is cancelled, item should not be in "
					+ "mMoveAnimations list");
		}
		dispatchFinishedWhenDone();
	}

	private void resetAnimation(ViewHolder holder)
	{
		if (sDefaultInterpolator == null)
		{
			sDefaultInterpolator = new ValueAnimator().getInterpolator();
		}
		holder.itemView.animate().setInterpolator(sDefaultInterpolator);
		endAnimation(holder);
	}

	@Override
	public boolean isRunning()
	{
		return (!mPendingAdditions.isEmpty()
				|| !mPendingChanges.isEmpty()
				|| !mPendingMoves.isEmpty()
				|| !mPendingRemovals.isEmpty()
				|| !mMoveAnimations.isEmpty()
				|| !mRemoveAnimations.isEmpty()
				|| !mAddAnimations.isEmpty()
				|| !mChangeAnimations.isEmpty()
				|| !mMovesList.isEmpty()
				|| !mAdditionsList.isEmpty()
				|| !mChangesList.isEmpty());
	}

	/**
	 * Check the state of currently pending and running animations. If there are none
	 * pending/running, call {@link #dispatchAnimationsFinished()} to notify any
	 * listeners.
	 */
	void dispatchFinishedWhenDone()
	{
		if (!isRunning())
		{
			dispatchAnimationsFinished();
		}
	}

	@Override
	public void endAnimations()
	{
		int count = mPendingMoves.size();
		for (int i = count - 1; i >= 0; i--)
		{
			MoveInfo item = mPendingMoves.get(i);
			View view = item.holder.itemView;
			view.setTranslationY(0);
			view.setTranslationX(0);
			dispatchMoveFinished(item.holder);
			mPendingMoves.remove(i);
		}
		count = mPendingRemovals.size();
		for (int i = count - 1; i >= 0; i--)
		{
			ViewHolder item = mPendingRemovals.get(i);
			dispatchRemoveFinished(item);
			mPendingRemovals.remove(i);
		}
		count = mPendingAdditions.size();
		for (int i = count - 1; i >= 0; i--)
		{
			ViewHolder item = mPendingAdditions.get(i);
			item.itemView.setAlpha(1);
			dispatchAddFinished(item);
			mPendingAdditions.remove(i);
		}
		count = mPendingChanges.size();
		for (int i = count - 1; i >= 0; i--)
		{
			endChangeAnimationIfNecessary(mPendingChanges.get(i));
		}
		mPendingChanges.clear();
		if (!isRunning())
		{
			return;
		}

		int listCount = mMovesList.size();
		for (int i = listCount - 1; i >= 0; i--)
		{
			ArrayList<MoveInfo> moves = mMovesList.get(i);
			count = moves.size();
			for (int j = count - 1; j >= 0; j--)
			{
				MoveInfo moveInfo = moves.get(j);
				ViewHolder item = moveInfo.holder;
				View view = item.itemView;
				view.setTranslationY(0);
				view.setTranslationX(0);
				dispatchMoveFinished(moveInfo.holder);
				moves.remove(j);
				if (moves.isEmpty())
				{
					mMovesList.remove(moves);
				}
			}
		}
		listCount = mAdditionsList.size();
		for (int i = listCount - 1; i >= 0; i--)
		{
			ArrayList<ViewHolder> additions = mAdditionsList.get(i);
			count = additions.size();
			for (int j = count - 1; j >= 0; j--)
			{
				ViewHolder item = additions.get(j);
				View view = item.itemView;
				view.setAlpha(1);
				dispatchAddFinished(item);
				additions.remove(j);
				if (additions.isEmpty())
				{
					mAdditionsList.remove(additions);
				}
			}
		}
		listCount = mChangesList.size();
		for (int i = listCount - 1; i >= 0; i--)
		{
			ArrayList<ChangeInfo> changes = mChangesList.get(i);
			count = changes.size();
			for (int j = count - 1; j >= 0; j--)
			{
				endChangeAnimationIfNecessary(changes.get(j));
				if (changes.isEmpty())
				{
					mChangesList.remove(changes);
				}
			}
		}

		cancelAll(mRemoveAnimations);
		cancelAll(mMoveAnimations);
		cancelAll(mAddAnimations);
		cancelAll(mChangeAnimations);

		dispatchAnimationsFinished();
	}

	void cancelAll(List<ViewHolder> viewHolders)
	{
		for (int i = viewHolders.size() - 1; i >= 0; i--)
		{
			viewHolders.get(i).itemView.animate().cancel();
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * If the payload list is not empty, DefaultItemAnimator returns <code>true</code>.
	 * When this is the case:
	 * <ul>
	 * <li>If you override {@link #animateChange(ViewHolder, ViewHolder, int, int, int, int)}, both
	 * ViewHolder arguments will be the same instance.
	 * </li>
	 * <li>
	 * If you are not overriding {@link #animateChange(ViewHolder, ViewHolder, int, int, int, int)},
	 * then DefaultItemAnimator will call {@link #animateMove(ViewHolder, int, int, int, int)} and
	 * run a move animation instead.
	 * </li>
	 * </ul>
	 */
	@Override
	public boolean canReuseUpdatedViewHolder(@NonNull ViewHolder viewHolder,
											 @NonNull List<Object> payloads)
	{
		return !payloads.isEmpty() || super.canReuseUpdatedViewHolder(viewHolder, payloads);
	}

	public float getInterpolationFactor()
	{
		return mInterpolationFactor;
	}

	public void setInterpolationFactor(float value)
	{
		this.mInterpolationFactor = value;
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

	@Direction
	public int getDirection()
	{
		return mDirection;
	}

	public void setDirection(@Direction int direction)
	{
		this.mDirection = direction;
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
