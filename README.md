# Scarlett Android Widgets

A selection of Android widgets (views) to simplify development.

## Getting Started

This library includes the EasyViewPager module, designed to automate implementation of the ViewPagerAdapter, with other functionalities.

### Prerequisites

Android development environment with the jcenter repository added in the project's build.gradle file.
A target SDK of 15 or higher.

```
repositories {
        jcenter()
    }
```

### Installation

Add the following dependency in the app's build.gradle file:

```
dependencies {
    compile 'net.scarlettsystems.android:widget:0.1.0'
}
```

## EasyViewPager

A wrapper library for the ViewPager widget to simplify instantiation.

### Benefits

Create paged views quickly without the need to specify a Pager Adapter. A generic instance of the adapter is created and managed internally within the EasyViewPager instance. Adding and removing pages are facilitated by simple method calls on this instance, by passing in Views.

### Usage

EasyViewPager can be defined in an XML layout file such as:

```
<net.scarlettsystems.android.widget.EasyViewPager
		android:id="@+id/pager"
		android:layout_width="match_parent"
		android:layout_height="match_parent"
		app:swipeable="false"/>
```

Pages are programmatically added and automatically shown in the view:

```
EasyViewPager pager = findViewById(R.id.pager);
View view = new View(this);
pager.addPage(view);
```

## EasyRecyclerView

A wrapper library for the RecyclerView widget to simplify instantiation.

### Benefits

Hides the implementation of the RecyclerView Adapter and simplifies the initialisation process.

### Usage

EasyViewPager can be defined in an XML layout file such as:

```
<net.scarlettsystems.android.widget.EasyRecyclerView
		android:id="@+id/recycler"
		android:layout_width="match_parent"
		android:layout_height="match_parent"/>
```

EasyRecyclerView should be configured by passing in a LayoutManager, a view for the items (cards), and a listener to bind the data items with the views.

```
EasyRecyclerView recycler = findViewById(R.id.recycler);
RecyclerView.LayoutManager lm = new LinearLayoutManager(this);
recycler.setLayoutManager(lm)
```

The view of the item can be set by either setting a layout resource, or dynamically returning a View through the OnCreateItemViewListener. EasyRecyclerView will prefer inflating a layout from the resource set by setItemLayoutResource().

```
recycler.setItemLayoutResource(R.layout.card);

//OR

recycler.setOnCreateItemViewListener(new EasyRecyclerView.OnCreateItemViewListener()
{
	@Override
	public View OnCreateItemView()
	{
		return new View(getContext());
	}
});
```

To actually populate the view, the OnBindItemViewListener must be specified, else nothing will be done for the newly created view.

```
recycler.setOnBindItemViewListener(new EasyRecyclerView.OnBindItemViewListener()
{
	@Override
	public void OnBindItemView(View view, Object item)
	{
		//Find Views
		TextView titleView = view.findViewById(R.id.title);
		TextView messageView = view.findViewById(R.id.message);
		
		//Get Data from Item
		String message = ((MyItem)item).getMessage();
		
		//Set Data to Views
		titleView.setText("Hello!");
		messageView.setText(message);
	}
});
```

## Versioning

Current version: 0.1.0

## Authors

* **Shane Scarlett** - *core development* - [Scarlett Systems](https://scarlettsystems.net)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the Apache 2.0 License
