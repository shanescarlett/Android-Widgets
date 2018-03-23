package net.scarlettsystems.android;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import net.scarlettsystems.android.widget.FlexTextView;

public class FlexTextViewDemo extends AppCompatActivity
{

	FlexTextView flexTextView;
	TextView enabledButton, animationsButton, addButton, removeButton, collapsingButton, scrollingButton, resizingButton;
	int primaryColour;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flex_text_view_demo);
		setTitle("FlexTextView Demo");
		primaryColour = ContextCompat.getColor(getBaseContext(), R.color.colorPrimary);
		flexTextView = findViewById(R.id.ftv);

		enabledButton = findViewById(R.id.enable);
		animationsButton = findViewById(R.id.animation);
		addButton = findViewById(R.id.add);
		removeButton = findViewById(R.id.remove);
		collapsingButton = findViewById(R.id.collapsing);
		scrollingButton = findViewById(R.id.scrolling);
		resizingButton = findViewById(R.id.resizing);
		configButtons();
	}

	private void configButtons()
	{
		enabledButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(flexTextView.isEnabled())
				{
					flexTextView.disable();
					enabledButton.setText("Disabled");
					enabledButton.setBackgroundColor(Color.GRAY);
				}
				else
				{
					flexTextView.enable();
					enabledButton.setText("Enabled");
					enabledButton.setBackgroundColor(primaryColour);
				}
			}
		});

		animationsButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(flexTextView.isAnimationEnabled())
				{
					flexTextView.setAnimationsEnabled(false);
					animationsButton.setText("Animations: Off");
					animationsButton.setBackgroundColor(Color.GRAY);
				}
				else
				{
					flexTextView.setAnimationsEnabled(true);
					animationsButton.setText("Animations: On");
					animationsButton.setBackgroundColor(primaryColour);
				}
			}
		});

		addButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				flexTextView.setText(flexTextView.getText() + "lorem ipsum dolor sit amet, consectetur adipiscing elit, ");
			}
		});

		removeButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(flexTextView.getText().length()<57)
				{
					flexTextView.setText("");
				}
				else
				{
					flexTextView.setText(flexTextView.getText().toString().substring(0, flexTextView.getText().length() - 57));
				}
			}
		});

		collapsingButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				flexTextView.setMode(FlexTextView.COLLAPSING);
				collapsingButton.setBackgroundColor(primaryColour);
				scrollingButton.setBackgroundColor(Color.GRAY);
				resizingButton.setBackgroundColor(Color.GRAY);
			}
		});

		scrollingButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				flexTextView.setMode(FlexTextView.SCROLLING);
				collapsingButton.setBackgroundColor(Color.GRAY);
				scrollingButton.setBackgroundColor(primaryColour);
				resizingButton.setBackgroundColor(Color.GRAY);
			}
		});

		resizingButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				flexTextView.setMode(FlexTextView.RESIZING);
				collapsingButton.setBackgroundColor(Color.GRAY);
				scrollingButton.setBackgroundColor(Color.GRAY);
				resizingButton.setBackgroundColor(primaryColour);
			}
		});
	}
}
