# EasyViewPager

A wrapper library for the ViewPager widget to simplify instantiation.

## Preview

<p align="center">
    <img src="https://raw.githubusercontent.com/shanescarlett/Android-Widgets/master/samples/EasyViewPagerDemo.gif"/>
</p>

## Benefits

Create paged views quickly without the need to specify a Pager Adapter. A generic instance of the adapter is created and managed internally within the EasyViewPager instance. Adding and removing pages are facilitated by simple method calls on this instance, by passing in Views.

## Usage

EasyViewPager can be defined in an XML layout file such as:

```XML
<net.scarlettsystems.android.widget.EasyViewPager
		android:id="@+id/pager"
		android:layout_width="match_parent"
		android:layout_height="match_parent"
		app:swipeable="false"/>
```

Pages are programmatically added and automatically shown in the view:

```Java
EasyViewPager pager = findViewById(R.id.pager);
View view = new View(this);
pager.addPage(view);
```
