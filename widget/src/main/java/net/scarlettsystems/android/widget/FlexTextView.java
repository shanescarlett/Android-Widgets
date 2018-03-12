package net.scarlettsystems.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * An adaptive text view widget focused on smooth animations between data changes
 * and collapsible functionality for long texts.
 *
 * @author Shane Scarlett
 * @version 1.0.0
 */
public class FlexTextView extends LinearLayout
{
	//Defaults
	private final int DEF_MAX_COL_LIN = 4;
	private final int DEF_BUT_SIZE = Helpers.Dp2Pix(24, this.getContext());
	private final int DEF_BUT_MARGIN = Helpers.Dp2Pix(16, this.getContext());
	private final float DEF_BUT_SPIN = 180;

	private LinearLayout mRoot;
	private TextView mTextView;
	private ImageView mButtonView;
	private int mMaxCollapsedLines = DEF_MAX_COL_LIN;
	private int mAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
	private float mInterpolationFactor = 1.0f;
	private float mButtonSpinAngle = DEF_BUT_SPIN;
	private boolean mCollapsed = true;
	private boolean mEnabled = true;
	private boolean mCollapseEnabled = true;
	private boolean mAnimationsEnabled = true;
	private boolean mLayoutComplete = false;
	private boolean mButtonShown = true;

	private int mButtonSize;

	public FlexTextView(Context context)
	{
		super(context);
		initialise();
	}

	public FlexTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialise();
		applyAttributes(context, attrs);
	}

	public FlexTextView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initialise();
		applyAttributes(context, attrs);
	}

	private void applyAttributes(Context context, AttributeSet attrs)
	{
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FlexTextView, 0, 0);
		try
		{
			setText(ta.getString(R.styleable.FlexTextView_ftv_text));
			setMaxCollapsedLines(ta.getInt(R.styleable.FlexTextView_ftv_maxCollapsedLines, DEF_MAX_COL_LIN));
			setButtonSize(ta.getDimensionPixelSize(R.styleable.FlexTextView_ftv_buttonSize, DEF_BUT_SIZE));
			setButtonMargin(ta.getDimensionPixelSize(R.styleable.FlexTextView_ftv_buttonMargin, DEF_BUT_MARGIN));
		}
		finally
		{
			ta.recycle();
		}
	}

	private void initialise()
	{
		inflate(getContext(), R.layout.flex_text_view, this);
		configureTextView();
		configureButton();
		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				mLayoutComplete = true;
				refresh();
				if(Build.VERSION.SDK_INT >= 16)
				{
					getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
				else
				{
					getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		});
	}

	private void configureTextView()
	{
		mTextView = this.findViewById(R.id.text);
		mTextView.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				onTextClick();
			}
		});
	}

	private void configureButton()
	{
		mButtonView = this.findViewById(R.id.button);
		mButtonView.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				onButtonClick();
			}
		});
	}

	//Callback Actions
	private void onTextClick()
	{
		clickAction();
	}

	private void onButtonClick()
	{
		clickAction();
	}

	private void clickAction()
	{
		if(!mEnabled){return;}
		if(!mCollapseEnabled){return;}

		if(mCollapsed)
		{
			expand();
			mCollapsed = false;
		}
		else
		{
			collapse();
			mCollapsed = true;
		}
	}

	private void refresh()
	{
		if(!mLayoutComplete){return;}

		if(mCollapsed)
		{
			collapse();
		}
		else
		{
			expand();
		}
		if(buttonNeeded())
		{
			showButton();
		}
		else
		{
			hideButton();
		}
	}

	//Animation Methods

	private int getCollapsedTextHeight()
	{
		int collapsedHeight = (getTextLineHeight() * mMaxCollapsedLines)
				+ mTextView.getPaddingTop()
				+ mTextView.getPaddingBottom();
		return Math.min(collapsedHeight, getExpandedTextHeight());
	}

	private int getTextLineHeight()
	{
		if(Build.VERSION.SDK_INT >= 16)
		{
			return Math.round(mTextView.getLineHeight()
					+ mTextView.getLineSpacingExtra());
		}
		else
		{
			return mTextView.getLineHeight();
		}
	}

	private int getExpandedTextHeight()
	{
		return mTextView.getLineCount() * getTextLineHeight();
	}

	private void collapse()
	{
		if(mAnimationsEnabled)
		{
			int startHeight = mTextView.getHeight();
			int endHeight = getCollapsedTextHeight();
			final float startAngle = mButtonView.getRotation();
			final float endAngle = 0f;
			final float deltaAngle = endAngle - startAngle;
			ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
			animator.setDuration(mAnimationDuration);
			animator.setInterpolator(new DecelerateInterpolator(mInterpolationFactor));
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
			{
				@Override
				public void onAnimationUpdate(ValueAnimator va)
				{
					setHeight(mTextView, (int)va.getAnimatedValue());
					mButtonView.setRotation(startAngle + (va.getAnimatedFraction() * deltaAngle));
				}
			});
			animator.start();
		}
		else
		{
			setHeight(mTextView, getCollapsedTextHeight());
		}
	}

	private void expand()
	{
		if(mAnimationsEnabled)
		{
			int startHeight = mTextView.getHeight();
			int endHeight = getExpandedTextHeight();
			final float startAngle = mButtonView.getRotation();
			final float endAngle = -mButtonSpinAngle;
			final float deltaAngle = endAngle - startAngle;
			ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
			animator.setDuration(mAnimationDuration);
			animator.setInterpolator(new DecelerateInterpolator(mInterpolationFactor));
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
			{
				@Override
				public void onAnimationUpdate(ValueAnimator va)
				{
					setHeight(mTextView, (int)va.getAnimatedValue());
					mButtonView.setRotation(startAngle + (va.getAnimatedFraction() * deltaAngle));
				}
			});
			animator.start();
		}
		else
		{
			setHeight(mTextView, getExpandedTextHeight());
		}
	}

	private void showButton()
	{
		if(mAnimationsEnabled)
		{
			int startHeight = mButtonView.getHeight();
			int endHeight = mButtonSize;
			ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
			animator.setDuration(mAnimationDuration);
			animator.setInterpolator(new DecelerateInterpolator(mInterpolationFactor));
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
			{
				@Override
				public void onAnimationUpdate(ValueAnimator va)
				{
					setHeight(mButtonView, (int)va.getAnimatedValue());
				}
			});
			animator.start();
		}
		else
		{
			setHeight(mButtonView, mButtonSize);
		}
	}

	private void hideButton()
	{
		if(mAnimationsEnabled)
		{
			int startHeight = mButtonView.getHeight();
			int endHeight = 0;
			ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
			animator.setDuration(mAnimationDuration);
			animator.setInterpolator(new AccelerateInterpolator(mInterpolationFactor));
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
			{
				@Override
				public void onAnimationUpdate(ValueAnimator va)
				{
					setHeight(mButtonView, (int)va.getAnimatedValue());
				}
			});
			animator.start();
		}
		else
		{
			setHeight(mButtonView, 0);
		}
	}

	private boolean buttonNeeded()
	{
		boolean collapsedIsSmaller = getCollapsedTextHeight() < getExpandedTextHeight();
		return collapsedIsSmaller && mCollapseEnabled;
	}

	private void setHeight(View v, int h)
	{
		ViewGroup.LayoutParams lp = v.getLayoutParams();
		lp.height = h;
		v.setLayoutParams(lp);
	}

	private void animateTextChange(Runnable changeAction)
	{
		if(!mAnimationsEnabled)
		{
			new Handler().post(changeAction);
			return;
		}
		fadeOutText(changeAction);
	}

	private void fadeOutText(final Runnable changeAction)
	{
		mTextView.clearAnimation();
		mTextView.animate().alpha(0.0f)
				.setDuration(mAnimationDuration)
				.setInterpolator(new AccelerateInterpolator(mInterpolationFactor))
				.setListener(new AnimatorListenerAdapter()
				{
					@Override
					public void onAnimationCancel(Animator animation)
					{
						new Handler().post(changeAction);
						mTextView.setAlpha(1.0f);
					}
					@Override
					public void onAnimationEnd(Animator animation)
					{
						new Handler().post(changeAction);
						fadeInText();
					}
				})
				.start();
	}

	private void fadeInText()
	{
		mTextView.animate().alpha(1.0f)
				.setDuration(mAnimationDuration)
				.setInterpolator(new DecelerateInterpolator(mInterpolationFactor))
				.setListener(new AnimatorListenerAdapter()
				{
					@Override
					public void onAnimationCancel(Animator animation)
					{
						mTextView.setAlpha(1.0f);
					}
					@Override
					public void onAnimationEnd(Animator animation)
					{
					}
				})
				.start();
	}

	//Pubilc Attribute Getters & Setters

	//Behaviour
	/**
	 * Enables user interaction and inputs to the view.
	 *
	 */
	public void enable()
	{
		mEnabled = true;
	}

	/**
	 * Disables user interaction and inputs to the view.
	 *
	 */
	public void disable()
	{
		mEnabled = false;
	}

	//Animation
	/**
	 * Set the enabled state for the view's animations. Include scaling from text content changes
	 * and expand, collapse actions.
	 *
	 * @param enabled true if animations are enabled, false otherwise
	 */
	public void setAnimationsEnabled(boolean enabled)
	{
		mAnimationsEnabled = enabled;
	}

	/**
	 * Set the duration of all animations associated with the view.
	 *
	 * @param duration duration of animations in milliseconds
	 */
	public void setAnimationDuration(int duration)
	{
		mAnimationDuration = duration;
	}

	/**
	 * Set the interpolation factor for all animations associated with the view.
	 *
	 * @param factor Degree to which the animation should be eased.
	 *               Setting factor to 1.0f produces an upside-down y=x^2 parabola.
	 *               Increasing factor above 1.0f makes exaggerates the ease-out effect
	 *               (i.e., it starts even faster and ends evens slower)
	 */
	public void setInterpolationFactor(float factor)
	{
		mInterpolationFactor = factor;
	}

	/**
	 * Set the angle of rotation of the collapse/expand button when animating.
	 * By default the button rotates 180 degrees counter-clockwise. Positive angles
	 * denote counter-clockwise rotation.
	 *
	 * @param angle angle of rotation in degrees
	 */
	public void setButtonRotationAmount(float angle)
	{
		mButtonSpinAngle = angle;
	}

	//Text
	/**
	 * Set the number of lines the view will display when collapsed. If the actual number of text
	 * lines needed is smaller than this number, the collapse button will be hidden.
	 *
	 * @param value number of lines to display when collapsed
	 */
	public void setMaxCollapsedLines(int value)
	{
		if(value < 1)
		{
			throw new IllegalArgumentException("Max collapsed lines cannot be fewer than 1.");
		}
		mMaxCollapsedLines = value;
	}

	/**
	 * Return the text that FlexTextView is displaying.
	 *
	 * @return CharSequence
	 * @see TextView#getText()
	 */
	public CharSequence getText()
	{
		return mTextView.getText();
	}

	/**
	 * Set the text that FlexTextView is displaying.
	 *
	 * @param text text to be displayed
	 * @see TextView#setText(CharSequence)
	 */
	public void setText(final CharSequence text)
	{
		animateTextChange(new Runnable()
		{
			@Override
			public void run()
			{
				mTextView.setText(text);
				refresh();
			}
		});
	}

	/**
	 * Set the text that FlexTextView is displaying.
	 *
	 * @param text text to be displayed
	 * @param type a {@link TextView.BufferType} which defines whether the text is stored as a static text, styleable/spannable text, or editable text
	 * @see TextView#setText(CharSequence, TextView.BufferType)
	 */
	public void setText(final CharSequence text, final TextView.BufferType type)
	{
		animateTextChange(new Runnable()
		{
			@Override
			public void run()
			{
				mTextView.setText(text, type);
				refresh();
			}
		});
	}

	/**
	 * Set the text that FlexTextView is displaying.
	 *
	 * @param text char array to be displayed
	 * @param start start index in the char array
	 * @param len length of char count after start
	 * @see TextView#setText(char[], int, int)
	 */
	public void setText(final char[] text, final int start, final int len)
	{
		animateTextChange(new Runnable()
		{
			@Override
			public void run()
			{
				mTextView.setText(text, start, len);
				refresh();
			}
		});
	}

	/**
	 * Set the text that FlexTextView is displaying.
	 *
	 * @param resId the resource identifier of the string resource to be displayed
	 * @see TextView#setText(int)
	 */
	public void setText(@StringRes final int resId)
	{
		animateTextChange(new Runnable()
		{
			@Override
			public void run()
			{
				mTextView.setText(resId);
				refresh();
			}
		});
	}

	/**
	 * Set the text that FlexTextView is displaying.
	 *
	 * @param resId the resource identifier of the string resource to be displayed
	 * @param type a {@link TextView.BufferType} which defines whether the text is stored as a static text, styleable/spannable text, or editable text
	 * @see TextView#setText(int, TextView.BufferType)
	 */
	public void setText(@StringRes final int resId, final TextView.BufferType type)
	{
		animateTextChange(new Runnable()
		{
			@Override
			public void run()
			{
				mTextView.setText(resId, type);
				refresh();
			}
		});
	}

	/**
	 * Get the size of the text displayed in the view.
	 *
	 * @return size of text in pixels
	 * @see TextView#getTextSize()
	 */
	public float getTextSize()
	{
		return mTextView.getTextSize();
	}

	/**
	 * Set the default text size to the given value, interpreted as "scaled pixel" units.
	 *
	 * @param size scaled pixel size
	 * @see TextView#setTextSize(float)
	 */
	public void setTextSize(float size)
	{
		mTextView.setTextSize(size);
		refresh();
	}

	/**
	 * Set the default text size to a given unit and value.
	 * See {@link android.util.TypedValue} for the possible dimension units.
	 *
	 * @param unit desired dimension unit
	 * @param size size in the given units
	 * @see TextView#setTextSize(int, float)
	 */
	public void setTextSize(int unit, float size)
	{
		mTextView.setTextSize(unit, size);
		refresh();
	}

	/**
	 * Set the typeface and style in which the text should be displayed.
	 *
	 * @param tf desired typeface
	 * @see TextView#setTypeface(Typeface)
	 */
	public void setTypeface(Typeface tf)
	{
		mTextView.setTypeface(tf);
		refresh();
	}

	/**
	 * Sets the typeface and style in which the text should be displayed,
	 * and turns on the fake bold and italic bits in the Paint if the Typeface
	 * that you provided does not have all the bits in the style that you specified.
	 *
	 * @param tf desired typeface
	 * @param style
	 * @see TextView#setTypeface(Typeface, int)
	 */
	public void setTypeface(Typeface tf, int style)
	{
		mTextView.setTypeface(tf, style);
		refresh();
	}

	/**
	 * Leave enough room for ascenders and descenders instead of using the font ascent
	 * and descent strictly. (Normally true).
	 *
	 * @param includeFontPadding
	 * @see TextView#setIncludeFontPadding(boolean)
	 */
	public void setIncludeFontPadding(boolean includeFontPadding)
	{
		mTextView.setIncludeFontPadding(includeFontPadding);
		refresh();
	}

	/**
	 * Gets the text colours for the different states (normal, selected, focused) of the TextView.
	 *
	 * @return ColorStateList
	 * @see TextView#getTextColors()
	 */
	public ColorStateList getTextColors()
	{
		return mTextView.getTextColors();
	}

	/**
	 * Set the text colour for all the states (normal, selected, focused) to be this color.
	 *
	 * @param colour color value in the form 0xAARRGGBB. Do not pass a resource ID.
	 *               To get a color value from a resource ID,
	 *               call {@link android.support.v4.content.ContextCompat#getColor(Context, int)}
	 * @see TextView#setTextColor(int)
	 */
	public void setTextColor(@ColorInt int colour)
	{
		mTextView.setTextColor(colour);
	}

	/**
	 * Set the text colour
	 *
	 * @param colours
	 * @see TextView#setTextColor(ColorStateList)
	 */
	public void setTextColor(ColorStateList colours)
	{
		mTextView.setTextColor(colours);
	}

	//Button
	/**
	 * Set the size of the collapse/expand button icon. Button aspect ratio is kept square.
	 *
	 * @param size size of button in pixels
	 */
	public void setButtonSize(int size)
	{
		mButtonSize = size;
		refresh();
	}

	/**
	 * Set the margin between the bottom of the text area and the collapse/expand button.
	 *
	 * @param margin size of margin in pixels
	 */
	public void setButtonMargin(int margin)
	{
		((LayoutParams)mButtonView.getLayoutParams()).topMargin = margin;
	}

	/**
	 * Set a Bitmap as the button image of this FlexTextView.
	 *
	 * @param bm the bitmap to set
	 */
	public void setButtonBitmap(Bitmap bm)
	{
		mButtonView.setImageBitmap(bm);
	}

	/**
	 * Set a drawable as the button image of this FlexTextView.
	 *
	 * @param drawable the drawable to set, or null to clear the content
	 */
	public void setButtonDrawable(Drawable drawable)
	{
		mButtonView.setImageDrawable(drawable);
	}

	/**
	 * Set a drawable resource as the button image of this FlexTextView.
	 *
	 * @param resId  the resource identifier of the drawable
	 */
	public void setButtonResource(@DrawableRes int resId)
	{
		mButtonView.setImageResource(resId);
	}

	/**
	 * Set the tint for the button image of this FlexTextView.
	 *
	 * @param colour colour tint to apply
	 */
	public void setButtonTint(@ColorInt int colour)
	{
		if (Build.VERSION.SDK_INT >= 21)
		{
			mButtonView.setImageTintList(ColorStateList.valueOf(colour));
		}
		else
		{
			mButtonView.setColorFilter(colour);
		}
	}

	/**
	 * Set the alpha for the button image of this FlexTextView.
	 *
	 * @param alpha transparency from 0 to 1
	 */
	public void setButtonAlpha(float alpha)
	{
		mButtonView.setAlpha(alpha);
	}
}
