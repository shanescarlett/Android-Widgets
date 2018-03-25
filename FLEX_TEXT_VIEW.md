# FlexTextView

An adaptive text view widget focused on smooth animations between data changes and configurable truncation modes for long texts.

## Preview

<p align="center">
  <img src="https://raw.githubusercontent.com/shanescarlett/Android-Widgets/master/samples/FlexTextViewDemo.gif"/>
</p>
  
## Benefits

FlexTextView offers different modes to truncate long sequences of texts including
* Collapsing mode where the view is collapsed to display a set number of lines and expanded by the user,
* Scrolling mode where the view is sized to display a set number of lines and the user can scroll through.

## Usage

FlexTextView can be defined in an XML layout file such as:

```xml
<net.scarlettsystems.android.widget.FlexTextView
		android:id="@+id/flex_text_view"
		android:layout_width="match_parent"
		android:layout_height="wrap_content"
		app:ftv_text="Hello World!"
		app:ftv_mode="scrolling"
		app:ftv_button_rotation="none"/>
```

FlexTextView wraps most functions involving the setting and appearance of the text such as all the `setText()` methods, and `setTypeface()`, etcetera.
When animations are enabled, FlexTextView aims to transition these changes as smoothly as possible, within the specified animation duration.

```Java
FlexTextView ftv = findViewById(R.id.flex_text_view);
ftv.setAnimationEnabled(true);
ftv.setText("Lorem ipsum dolor sit amet.");
```
