package net.scarlettsystems.android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class NetPicView extends AppCompatImageView
{
	private RelativeLayout mView;
	private ProgressBar mLoader, mProgress;
	private OkHttpClient mClient;
	private ProgressManager.UIonProgressListener mProgressListener;
	private RequestListener<Drawable> mRequestListener;
	private String mUrl;
	private ArrayList<OnLoadListener> mOnLoadListeners = new ArrayList<>();
	private int mCounter = 0;

	private int mLoaderHeight;

	private boolean mAnimationEnabled = true;

	private ValueAnimator mLoaderAnimator;
	private int mLoaderAnimationDuration = 200;

	public interface OnLoadListener
	{
		void OnLoadStart();
		void OnProgress(float progress);
		void OnLoadFinish();
	}

	public NetPicView(Context context)
	{
		super(context);
		initialise();
	}

	public NetPicView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialise();
		applyAttributes(context, attrs);
	}

	public NetPicView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initialise();
		applyAttributes(context, attrs);
	}

	private void initialise()
	{
		mView = (RelativeLayout)LayoutInflater.from(getContext()).inflate(R.layout.net_scarlettsystems_android_widget_netpicview,null);
		mLoaderHeight = Helpers.Dp2Pix(56, getContext());
		configureLoader();
		configureProgress();
		configureClient();
		configureProgressListener();
		configureRequestListener();
	}

	private void applyAttributes(Context context, AttributeSet attrs)
	{

	}

	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		//Intercept when being attached externally
		if(getParent() != mView)
		{
			//Configure wrapper view
			ViewGroup parent = (ViewGroup)getParent();
			mView.setLayoutParams(getLayoutParams());
			this.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			int index = parent.indexOfChild(this);
			//Switch out views
			parent.removeViewAt(index);
			mView.addView(this, 1);
			parent.addView(mView, index);
		}
	}

	private void configureLoader()
	{
		mLoader = mView
				.findViewById(R.id.net_scarlettsystems_android_widget_netpicview_loader);
	}

	private void configureProgress()
	{
		mProgress = mView
				.findViewById(R.id.net_scarlettsystems_android_widget_netpicview_progress);
	}

	private void configureClient()
	{
		mClient = new OkHttpClient.Builder()
			.addNetworkInterceptor(new Interceptor()
			{
				@Override
				public Response intercept(Chain chain) throws IOException
				{
					Request request = chain.request();
					Response response = chain.proceed(request);
					ProgressManager.ResponseProgressListener listener = new ProgressManager.DispatchingProgressListener();
					return response.newBuilder()
							.body(new ProgressManager.OkHttpProgressResponseBody(request.url(), response.body(), listener))
							.build();
				}
			})
			.build();
	}

	private void configureProgressListener()
	{
		mProgressListener = new ProgressManager.UIonProgressListener()
		{
			@Override
			public void onProgress(long bytesRead, long expectedLength)
			{
				float loadedFraction = (float)bytesRead / (float)expectedLength;
				mProgress.setProgress(Math.round(100f * loadedFraction));
				runOnProgress(loadedFraction);
			}

			@Override
			public float getGranularityPercent()
			{
				return 1.0f;
			}
		};
	}

	private void configureRequestListener()
	{
		mRequestListener = new RequestListener<Drawable>()
		{
			@Override
			public boolean onLoadFailed(
					@Nullable GlideException e,
					Object model,
					Target<Drawable> target,
					boolean isFirstResource)
			{
				ProgressManager.forget(mUrl);
				onFinished();
				return false;
			}

			@Override
			public boolean onResourceReady(
					Drawable resource,
					Object model,
					Target<Drawable> target,
					DataSource dataSource,
					boolean isFirstResource)
			{
				ProgressManager.forget(mUrl);
				onFinished();
				return false;
			}
		};
	}

	private void showLoader()
	{
		mLoader.setVisibility(View.VISIBLE);
		if(mAnimationEnabled)
		{
			if(mLoaderAnimator!=null){mLoaderAnimator.cancel();}
			mLoaderAnimator = ValueAnimator.ofInt(mLoader.getHeight(), mLoaderHeight);
			mLoaderAnimator.setDuration(mLoaderAnimationDuration);
			mLoaderAnimator.setInterpolator(new DecelerateInterpolator());
			mLoaderAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
			{
				@Override
				public void onAnimationUpdate(ValueAnimator animation)
				{
					Helpers.setViewHeight(mLoader, (int)animation.getAnimatedValue());
				}
			});
			mLoaderAnimator.start();
		}
		else
		{
			Helpers.setViewHeight(mLoader, mLoaderHeight);
		}
	}

	private void hideLoader()
	{
		if(mAnimationEnabled)
		{
			if(mLoaderAnimator!=null){mLoaderAnimator.cancel();}
			mLoaderAnimator = ValueAnimator.ofInt(mLoader.getHeight(), 0);
			mLoaderAnimator.setDuration(mLoaderAnimationDuration);
			mLoaderAnimator.setInterpolator(new AccelerateInterpolator());
			mLoaderAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
			{
				@Override
				public void onAnimationUpdate(ValueAnimator animation)
				{
					Helpers.setViewHeight(mLoader, (int)animation.getAnimatedValue());
				}
			});
			mLoaderAnimator.addListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					mLoader.setVisibility(View.GONE);
				}
			});
			mLoaderAnimator.start();
		}
		else
		{
			Helpers.setViewHeight(mLoader, 0);
			mLoader.setVisibility(View.GONE);
		}
	}

	private void showProgress()
	{
		mProgress.setVisibility(View.VISIBLE);
		if(mAnimationEnabled)
		{
			mProgress.animate().alpha(1.0f).start();
		}
	}

	private void hideProgress()
	{
		if(mAnimationEnabled)
		{
			mProgress.animate().alpha(0.0f).setListener(new AnimatorListenerAdapter()
			{
				@Override
				public void onAnimationEnd(Animator animation)
				{
					mProgress.setVisibility(View.GONE);
					mProgress.setProgress(0);
				}
			}).start();
		}
		else
		{
			mProgress.setVisibility(View.GONE);
			mProgress.setProgress(0);
		}
	}

	private void runOnStart()
	{
		for(OnLoadListener l : mOnLoadListeners)
		{
			l.OnLoadStart();
		}
	}

	private void runOnProgress(float progress)
	{
		for(OnLoadListener l : mOnLoadListeners)
		{
			l.OnProgress(progress);
		}
	}

	private void runOnFinish()
	{
		for(OnLoadListener l : mOnLoadListeners)
		{
			l.OnLoadFinish();
		}
	}

	private RequestBuilder<Drawable> glideBuilder(String url)
	{
		mUrl = url;
		onConnecting();
		ProgressManager.expect(url, mProgressListener);
		Glide glide = Glide.get(getContext());
		glide.getRegistry().replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(mClient));
		return Glide.with(getContext())
				.load(url)
				.transition(withCrossFade())
				.listener(mRequestListener);
	}

	private void onConnecting()
	{
		if(mLoader != null)
		{
			showLoader();
			showProgress();
		}
		runOnStart();
	}

	private void onFinished()
	{
		if(mLoader != null)
		{
			hideLoader();
			hideProgress();
		}
		runOnFinish();
	}

	public void addOnLoadListener(OnLoadListener l)
	{
		mOnLoadListeners.add(l);
	}

	public void removeOnLoadListener(OnLoadListener l)
	{
		mOnLoadListeners.remove(l);
	}

	public ArrayList<OnLoadListener> getOnLoadListeners()
	{
		return mOnLoadListeners;
	}

	public void setLoaderAnimationDuration(int duration)
	{
		mLoaderAnimationDuration = duration;
	}

	public void setLoaderColour(@ColorInt int colour)
	{
		mLoader.getIndeterminateDrawable().setColorFilter(colour, PorterDuff.Mode.SRC_IN);
	}

	public void setProgressColour(@ColorInt int colour)
	{
		mProgress.getProgressDrawable().setColorFilter(colour, PorterDuff.Mode.SRC_IN);
	}

	public void load(String url)
	{
		if (url == null) return;
		glideBuilder(url).into(this);
	}

	public void load(String url, RequestOptions options)
	{
		if (url == null) return;
		if (options == null) return;
		glideBuilder(url)
				.apply(options)
				.into(this);
	}

	public void forceReload(String url)
	{
		if (url == null) return;
		glideBuilder(url)
				.apply(new RequestOptions().signature(new ObjectKey(String.valueOf(System.currentTimeMillis() + mCounter++))))
				.into(this);
	}

	public void forceReload(String url, RequestOptions options)
	{
		if (url == null) return;
		if (options == null) return;
		glideBuilder(url)
				.apply(options.skipMemoryCache(true).signature(new ObjectKey(String.valueOf(System.currentTimeMillis() + mCounter++))))
				.into(this);
	}
}

class ProgressManager
{
	public static void forget(String url)
	{
		DispatchingProgressListener.forget(url);
	}

	public static void expect(String url, UIonProgressListener listener)
	{
		DispatchingProgressListener.expect(url, listener);
	}

	public interface ResponseProgressListener
	{
		void update(HttpUrl url, long bytesRead, long contentLength);
	}

	public interface UIonProgressListener
	{
		void onProgress(long bytesRead, long expectedLength);

		/**
		 * Control how often the listener needs an update. 0% and 100% will always be dispatched.
		 *
		 * @return in percentage (0.2 = call {@link #onProgress} around every 0.2 percent of progress)
		 */
		float getGranularityPercent();
	}

	public static class DispatchingProgressListener implements ResponseProgressListener
	{
		private static final Map<String, UIonProgressListener> LISTENERS = new HashMap<>();
		private static final Map<String, Long> PROGRESSES = new HashMap<>();

		private final Handler handler;

		DispatchingProgressListener()
		{
			this.handler = new Handler(Looper.getMainLooper());
		}

		static void forget(String url)
		{
			LISTENERS.remove(url);
			PROGRESSES.remove(url);
		}

		static void expect(String url, UIonProgressListener listener)
		{
			LISTENERS.put(url, listener);
		}

		@Override
		public void update(HttpUrl url, final long bytesRead, final long contentLength)
		{
			String key = url.toString();
			final UIonProgressListener listener = LISTENERS.get(key);
			if (listener == null)
			{
				return;
			}
			if (contentLength <= bytesRead)
			{
				forget(key);
			}
			if (needsDispatch(key, bytesRead, contentLength, listener.getGranularityPercent()))
			{
				handler.post(new Runnable()
				{
					@Override
					public void run()
					{
						listener.onProgress(bytesRead, contentLength);
					}
				});
			}
		}

		private boolean needsDispatch(String key, long current, long total, float granularity)
		{
			if (granularity == 0 || current == 0 || total == current)
			{
				return true;
			}
			float percent = 100f * current / total;
			long currentProgress = (long) (percent / granularity);
			Long lastProgress = PROGRESSES.get(key);
			if (lastProgress == null || currentProgress != lastProgress)
			{
				PROGRESSES.put(key, currentProgress);
				return true;
			} else
			{
				return false;
			}
		}
	}

	public static class OkHttpProgressResponseBody extends ResponseBody
	{
		private final HttpUrl url;
		private final ResponseBody responseBody;
		private final ResponseProgressListener progressListener;
		private BufferedSource bufferedSource;

		OkHttpProgressResponseBody(HttpUrl url, ResponseBody responseBody,
								   ResponseProgressListener progressListener)
		{
			this.url = url;
			this.responseBody = responseBody;
			this.progressListener = progressListener;
		}

		@Override
		public MediaType contentType()
		{
			return responseBody.contentType();
		}

		@Override
		public long contentLength()
		{
			return responseBody.contentLength();
		}

		@Override
		public BufferedSource source()
		{
			if (bufferedSource == null)
			{
				bufferedSource = Okio.buffer(source(responseBody.source()));
			}
			return bufferedSource;
		}

		private Source source(Source source)
		{
			return new ForwardingSource(source)
			{
				long totalBytesRead = 0L;

				@Override
				public long read(Buffer sink, long byteCount) throws IOException
				{
					long bytesRead = super.read(sink, byteCount);
					long fullLength = responseBody.contentLength();
					if (bytesRead == -1)
					{ // this source is exhausted
						totalBytesRead = fullLength;
					} else
					{
						totalBytesRead += bytesRead;
					}
					progressListener.update(url, totalBytesRead, fullLength);
					return bytesRead;
				}
			};
		}
	}
}

