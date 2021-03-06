package net.scarlettsystems.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An adaptive text view widget focused on smooth animations between data changes
 * and configurable truncation modes for long texts.
 *
 * @author Shane Scarlett
 * @version 1.0.0
 */
public class FlexTextView extends AppCompatTextView
{
	//Defaults
	private final int DEF_MODE = COLLAPSING;
	private final boolean DEF_ENABLED = true;
	private final boolean DEF_COLLAPSE_ENABLED = true;
	private final int DEF_BUT_RES = R.drawable.net_scarlettsystems_android_ic_chevron_down;
	private final int DEF_BUT_SIZE = Helpers.Dp2Pix(24, this.getContext());
	private final int DEF_BUT_MARGIN = Helpers.Dp2Pix(16, this.getContext());
	private final int DEF_BUT_DIR = ANTI_CLOCKWISE;
	private final int DEF_ANIM_TIME = getResources().getInteger(android.R.integer.config_mediumAnimTime);
	private final float DEF_INTERPOLATION_FACTOR = 1.0f;

	//Elements
	private LinearLayout mView;
	private ImageView mButtonView;
	private ScrollView mScrollView;

	//Interface values
	private int mButtonSize;

	//Settings
	private int mMode = DEF_MODE;
	private int mMaxCollapsedLines;
	private int mAnimationDuration = DEF_ANIM_TIME;
	private float mInterpolationFactor = DEF_INTERPOLATION_FACTOR;
	private int mButtonDirection = DEF_BUT_DIR;

	//Behaviour flags
	private boolean mEnabled = DEF_ENABLED;
	private boolean mCollapseEnabled = DEF_COLLAPSE_ENABLED;
	private boolean mAnimationsEnabled = true;
	private boolean mScrollEnabled = false;

	//State flags
	private boolean mCollapsed = true;
	private boolean mLayoutComplete = false;

	//Executors
	private Handler mHandler = new Handler();
	private ValueAnimator mTextViewAnimator = null;
	private ValueAnimator mButtonAnimator = null;

	@SuppressWarnings("WeakerAccess")
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({COLLAPSING, SCROLLING, RESIZING})
	public @interface Mode {}

	public static final int COLLAPSING = 0;
	public static final int SCROLLING = 1;
	public static final int RESIZING = 2;

	@SuppressWarnings("WeakerAccess")
	@Retention(RetentionPolicy.SOURCE)
	@IntDef({ANTI_CLOCKWISE, NONE, CLOCKWISE})
	public @interface ButtonRotation {}

	public static final int ANTI_CLOCKWISE = -1;
	public static final int NONE = 0;
	public static final int CLOCKWISE = 1;


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
			setMode(ta.getInt(R.styleable.FlexTextView_ftv_mode, DEF_MODE));
			setEnabled(ta.getBoolean(R.styleable.FlexTextView_ftv_enabled, DEF_ENABLED));
			setCollapseEnabled(ta.getBoolean(R.styleable.FlexTextView_ftv_collapsible, DEF_COLLAPSE_ENABLED));
			setAnimationsEnabled(ta.getBoolean(R.styleable.FlexTextView_ftv_animation_enabled, DEF_ENABLED));
			setButtonResource(ta.getResourceId(R.styleable.FlexTextView_ftv_button_image, DEF_BUT_RES));
			setButtonSize(ta.getDimensionPixelSize(R.styleable.FlexTextView_ftv_button_size, DEF_BUT_SIZE));
			setButtonMargin(ta.getDimensionPixelSize(R.styleable.FlexTextView_ftv_button_margin, DEF_BUT_MARGIN));
			setButtonRotationDirection(ta.getInt(R.styleable.FlexTextView_ftv_button_rotation, DEF_BUT_DIR));
		}
		finally
		{
			ta.recycle();
		}
	}

	/**
	 * Inflates view and configures each element.
	 *
	 */
	private void initialise()
	{
		mView = (LinearLayout) LayoutInflater
				.from(getContext())
				.inflate(R.layout.net_scarlettsystems_android_widget_flextextview, (ViewGroup)getParent(), false);
		configureTextView();
		configureButton();
		configureScrollView();
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

	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		//Intercept when being attached externally
		if(getParent() != mScrollView)
		{
			//Configure wrapper view
			ViewGroup parent = (ViewGroup)getParent();
			mView.setLayoutParams(getLayoutParams());
			this.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			int index = parent.indexOfChild(this);
			//Switch out views
			parent.removeViewAt(index);
			mScrollView.addView(this);
			parent.addView(mView, index);
		}
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		this.post(new Runnable()
		{
			@Override
			public void run()
			{
				refresh();
			}
		});
	}

	@Override
	public void requestLayout()
	{
		super.requestLayout();
	}

	private void configureTextView()
	{
		setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				onTextClick();
			}
		});
	}

	@Override
	public void setText(final CharSequence text, final BufferType type)
	{
		animateTextChange(new Runnable()
		{
			@Override
			public void run()
			{
				FlexTextView.super.setText(text, type);
			}
		});

	}

	private void configureButton()
	{
		mButtonView = mView.findViewById(R.id.net_scarlettsystems_android_widget_flextextview_button);
		mButtonView.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				onButtonClick();
			}
		});
	}

	@SuppressWarnings("all")
	private void configureScrollView()
	{
		mScrollView = mView.findViewById(R.id.net_scarlettsystems_android_widget_flextextview_scroll);
		mScrollView.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				return !(mScrollEnabled&&mEnabled);
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
		if(mMode != COLLAPSING){return;}

		mCollapsed ^= true;
		sizeTextView(getTextHeight());
		sizeButton(getButtonSize(), getButtonAngle());
	}

	/**
	 * Refresh FlexTextView. Should be called each time data is changed.
	 *
	 */
	private void refresh()
	{
		if(!mLayoutComplete){return;}
		scrollToStart();
		switch(mMode)
		{
			case COLLAPSING:
				if(!isButtonNeeded()){mCollapsed = true;}
				sizeTextView(getTextHeight());
				sizeButton(getButtonSize(), getButtonAngle());
				break;
			case SCROLLING:
				mCollapsed = true;
				sizeTextView(getTextHeight());
				sizeButton(0, 0);
				break;
			case RESIZING:
				break;
			default:
				break;
		}

	}

	/**
	 * Scrolls text view to the top.
	 *
	 */
	private void scrollToStart()
	{
		if(mScrollView.getScrollY()!=0)
		{
			if(mAnimationsEnabled)
			{
				mScrollView.smoothScrollTo(0, 0);
			}
			else
			{
				mScrollView.scrollTo(0, 0);
			}
		}
	}

	//Animation Methods

	/**
	 * Get the height the text view should be when collapsed.
	 *
	 * @return height of text view in pixels
	 */
	private int getCollapsedTextHeight()
	{
		int collapsedHeight = (this.getLineHeight() * (mMaxCollapsedLines - 1))
				+ getLastLineHeight()
				+ this.getPaddingTop()
				+ this.getPaddingBottom();
		return Math.min(collapsedHeight, getExpandedTextHeight());
	}

	/**
	 * Get the height the text view should be when expanded.
	 *
	 * @return height of text view in pixels
	 */
	private int getExpandedTextHeight()
	{
		return ((this.getLineCount()-1) * this.getLineHeight()) + getLastLineHeight();
	}

	/**
	 * Get the height of the last line including all ascender and descender heights.
	 *
	 * @return height of line in pixels
	 */
	private int getLastLineHeight()
	{
		return Math.round(this.getPaint().getFontMetrics().bottom - this.getPaint().getFontMetrics().top);
	}

	/**
	 * Get the size of the text view to be displayed, dependent on the current collapsed state.
	 *
	 * @return text view height in pixels
	 */
	private int getTextHeight()
	{
		if(mCollapsed)
		{
			return getCollapsedTextHeight();
		}
		else
		{
			return getExpandedTextHeight();
		}
	}

	/**
	 * Transform the text view to the specified size and rotation.
	 *
	 * @param size size of text view in pixels
	 */
	private void sizeTextView(int size)
	{
		stopTextViewAnimation();
		if(mAnimationsEnabled)
		{
			mTextViewAnimator = ValueAnimator.ofInt(mScrollView.getHeight(), size);
			mTextViewAnimator.setDuration(mAnimationDuration);
			mTextViewAnimator.setInterpolator(new DecelerateInterpolator(mInterpolationFactor));
			mTextViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
			{
				@Override
				public void onAnimationUpdate(ValueAnimator va)
				{
					setHeight(mScrollView, (int)va.getAnimatedValue());
				}
			});
			mTextViewAnimator.start();
		}
		else
		{
			setHeight(mScrollView, size);
		}
	}

	/**
	 * Cancels all animations associated with the text view.
	 *
	 */
	private void stopTextViewAnimation()
	{
		if(mTextViewAnimator == null){return;}
		if(!mTextViewAnimator.isStarted()){return;}
		mTextViewAnimator.cancel();
	}

	/**
	 * Transform the collapse button to the specified size and rotation.
	 *
	 * @param size size of button in pixels
	 * @param angle rotation of button in degrees
	 */
	private void sizeButton(int size, float angle)
	{
		stopButtonAnimation();
		if(mAnimationsEnabled)
		{
			final float startAngle = mButtonView.getRotation();
			final float deltaAngle = angle - startAngle;
			mButtonAnimator = ValueAnimator.ofInt(mButtonView.getHeight(), size);
			mButtonAnimator.setDuration(mAnimationDuration);
			mButtonAnimator.setInterpolator(new DecelerateInterpolator(mInterpolationFactor));
			mButtonAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
			{
				@Override
				public void onAnimationUpdate(ValueAnimator va)
				{
					setHeight(mButtonView, (int)va.getAnimatedValue());
					mButtonView.setRotation(startAngle + (va.getAnimatedFraction() * deltaAngle));
				}
			});
			mButtonAnimator.addListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					super.onAnimationEnd(animation);
					if(Math.abs(mButtonView.getRotation()) == 360){mButtonView.setRotation(0);}
				}
			});
			mButtonAnimator.start();
		}
		else
		{
			setHeight(mButtonView, size);
			if(angle == 360)
			{
				mButtonView.setRotation(0);
			}
			else
			{
				mButtonView.setRotation(angle);
			}
		}
	}

	/**
	 * Cancels all animations associated with the collapse button.
	 *
	 */
	private void stopButtonAnimation()
	{
		if(mButtonAnimator == null){return;}
		if(!mButtonAnimator.isStarted()){return;}
		mButtonAnimator.cancel();
	}

	/**
	 * Check for if the collapse button is required to be displayed. Returns true if collapse is
	 * enabled, and if there is more text than the collapsed size of the view.
	 *
	 * @return is button needed
	 */
	private boolean isButtonNeeded()
	{
		boolean collapsedIsSmaller = getCollapsedTextHeight() < getExpandedTextHeight();
		return collapsedIsSmaller && mCollapseEnabled;
	}

	/**
	 * Get the size of the collapse button to be displayed. Returns a zero size if the button should
	 * not be visible.
	 *
	 * @return button size in pixels
	 */
	private int getButtonSize()
	{
		if(isButtonNeeded())
		{
			return mButtonSize;
		}
		else
		{
			return 0;
		}
	}

	/**
	 * Get the required angle of the collapse button for the current collapsed/expanded state.
	 *
	 * @return button angle in degrees
	 */
	private float getButtonAngle()
	{
		if(mCollapsed)
		{
			if(Math.abs(mButtonView.getRotation())>=180){return 360 * mButtonDirection;}
			else{return 0f;}
		}
		else
		{
			return 180 * mButtonDirection;
		}
	}

	/**
	 * Set the height of a view.
	 *
	 * @param v view to size
	 * @param h height in pixels
	 */
	private void setHeight(View v, int h)
	{
		ViewGroup.LayoutParams lp = v.getLayoutParams();
		lp.height = h;
		v.setLayoutParams(lp);
	}

	/**
	 * Method to change out displayed text. The actual text change must be specified through
	 * the {@link Runnable} parameter. This function only initiates the animation and posts the
	 * specified action in between fade out and fade in.
	 *
	 * @param changeAction text change action runnable
	 */
	private void animateTextChange(Runnable changeAction)
	{
		if(mAnimationsEnabled && mLayoutComplete)
		{
			fadeOutText(changeAction);
		}
		else
		{
			changeAction.run();
			refresh();
		}
	}

	/**
	 * Fade out the text view and post the update runnable.
	 *
	 * @param changeAction action to post upon fade out ending, or being cancelled
	 */
	private void fadeOutText(final Runnable changeAction)
	{
		this.animate().cancel();
		this.animate().alpha(0.0f)
				.setDuration(mAnimationDuration)
				.setInterpolator(new AccelerateInterpolator(mInterpolationFactor))
				.setListener(new AnimatorListenerAdapter()
				{
					@Override
					public void onAnimationCancel(Animator animation)
					{
						mHandler.post(changeAction);
						refresh();
					}
					@Override
					public void onAnimationEnd(Animator animation)
					{
						mHandler.post(changeAction);
						refresh();
						fadeInText();
					}
				})
				.start();
	}

	/**
	 * Fade back in the text view.
	 *
	 */
	private void fadeInText()
	{
		this.animate().alpha(1.0f)
				.setDuration(mAnimationDuration)
				.setInterpolator(new DecelerateInterpolator(mInterpolationFactor))
				.setListener(null)
				.start();
	}

	//Pubilc Attribute Getters & Setters

	//Behaviour
	/**
	 * Enables user interaction and inputs to the view.
	 *
	 */
	@SuppressWarnings("unused")
	public void enable()
	{
		mEnabled = true;
	}

	/**
	 * Disables user interaction and inputs to the view.
	 *
	 */
	@SuppressWarnings("unused")
	public void disable()
	{
		mEnabled = false;
	}

	/**
	 * Set current enabled state of FlexTextView. Setting to false disables user interaction
	 * and inputs to the view.
	 *
	 * @param enabled desired enabled state
	 */
	@SuppressWarnings("unused")
	public void setEnabled(boolean enabled)
	{
		mEnabled = enabled;
	}

	/**
	 * Get current enabled state of FlexTextView.
	 *
	 * @return enabled state of view
	 */
	@SuppressWarnings("unused")
	public boolean isEnabled()
	{
		return mEnabled;
	}

	/**
	 * Set current enabled state of the collapse functionality.
	 *
	 * @param collapseEnabled desired enabled state
	 */
	@SuppressWarnings("unused")
	public void setCollapseEnabled(boolean collapseEnabled)
	{
		mEnabled = collapseEnabled;
	}

	//Animation
	/**
	 * Set the enabled state for the view's animations. Includes scaling from text content changes
	 * and expand, collapse actions.
	 *
	 * @param enabled true if animations are enabled, false otherwise
	 */
	@SuppressWarnings("unused")
	public void setAnimationsEnabled(boolean enabled)
	{
		mAnimationsEnabled = enabled;
	}

	/**
	 * Get the enabled state for the view's animations.
	 *
	 * @return animations enabled state
	 */
	@SuppressWarnings("unused")
	public boolean isAnimationEnabled()
	{
		return mAnimationsEnabled;
	}

	/**
	 * Set the duration of all animations associated with the view.
	 *
	 * @param duration duration of animations in milliseconds
	 */
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
	public void setInterpolationFactor(float factor)
	{
		mInterpolationFactor = factor;
	}

	/**
	 * Set the direction of rotation of the collapse/expand button when animating.
	 * By default the button rotates 180 degrees counter-clockwise.
	 *
	 * @param direction direction of rotation
	 */
	@SuppressWarnings("unused")
	public void setButtonRotationDirection(@ButtonRotation int direction)
	{
		mButtonDirection = direction;
	}

	//Text
	/**
	 * Set the behaviour of FlexTextView.
	 * <p>Collapsing mode displays a collapsible view when the text exceeds the number of specified
	 * lines. Collapse and expansion can be toggled through a provided button.
	 * <p>Scrolling mode sets the height of the view at the number of specified lines and enables
	 * the view to be scrolled.
	 *
	 * @param mode behaviour mode of type {@link Mode}
	 */
	@SuppressWarnings("unused")
	public void setMode(@Mode int mode)
	{
		mMode = mode;
		switch(mMode)
		{
			case COLLAPSING:
				super.setMaxLines(Integer.MAX_VALUE);
				mScrollEnabled = false;
				mScrollView.setVerticalScrollBarEnabled(false);
				break;
			case SCROLLING:
				super.setMaxLines(Integer.MAX_VALUE);
				mScrollEnabled = true;
				mScrollView.setVerticalScrollBarEnabled(true);
				break;
			case RESIZING:
				break;
			default:
				break;
		}
		refresh();
	}

	/**
	 * Set the number of lines of text the view will display.
	 * <p>In collapsing mode, this is the number of lines when the view is collapsed. If the actual
	 * number of text lines needed is smaller than this number, the collapse button will be hidden.
	 * <p>In scrolling mode, this is the number of lines visible within the scrolling window.
	 * <p>In resizing mode, this value has no effect.
	 *
	 * @param value number of lines to display when collapsed
	 */
	@SuppressWarnings("unused")
	@Override
	public void setMaxLines(int value)
	{
		if(value < 1)
		{
			throw new IllegalArgumentException("Max collapsed lines cannot be fewer than 1.");
		}
		mMaxCollapsedLines = value;
	}

	//Button
	/**
	 * Set the size of the collapse/expand button icon. Button aspect ratio is kept square.
	 *
	 * @param size size of button in pixels
	 */
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
	public void setButtonMargin(int margin)
	{
		((LinearLayout.LayoutParams)mButtonView.getLayoutParams()).topMargin = margin;
	}

	/**
	 * Set a Bitmap as the button image of this FlexTextView.
	 *
	 * @param bm the bitmap to set
	 */
	@SuppressWarnings("unused")
	public void setButtonBitmap(Bitmap bm)
	{
		mButtonView.setImageBitmap(bm);
	}

	/**
	 * Set a drawable as the button image of this FlexTextView.
	 *
	 * @param drawable the drawable to set, or null to clear the content
	 */
	@SuppressWarnings("unused")
	public void setButtonDrawable(Drawable drawable)
	{
		mButtonView.setImageDrawable(drawable);
	}

	/**
	 * Set a drawable resource as the button image of this FlexTextView.
	 *
	 * @param resId  the resource identifier of the drawable
	 */
	@SuppressWarnings("unused")
	public void setButtonResource(@DrawableRes int resId)
	{
		mButtonView.setImageResource(resId);
	}

	/**
	 * Set the tint for the button image of this FlexTextView.
	 *
	 * @param colour colour tint to apply
	 */
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
	public void setButtonAlpha(float alpha)
	{
		mButtonView.setAlpha(alpha);
	}
}
