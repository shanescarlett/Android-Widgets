## EasyRecyclerView

A wrapper library for the RecyclerView widget to simplify instantiation.

### Benefits

Hides the implementation of the RecyclerView Adapter and simplifies the initialisation process.

### Usage

EasyViewPager can be defined in an XML layout file such as:

```XML
<net.scarlettsystems.android.widget.EasyRecyclerView
		android:id="@+id/recycler"
		android:layout_width="match_parent"
		android:layout_height="match_parent"/>
```

EasyRecyclerView should be configured by passing in a LayoutManager, a view for the items (cards), and a listener to bind the data items with the views.

```Java
EasyRecyclerView recycler = findViewById(R.id.recycler);
RecyclerView.LayoutManager lm = new LinearLayoutManager(this);
recycler.setLayoutManager(lm)
```

The view of the item can be set by either setting a layout resource, or dynamically returning a View through the OnCreateItemViewListener. EasyRecyclerView will prefer inflating a layout from the resource set by setItemLayoutResource().

```Java
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

```Java
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

Click actions on the items can be obtained through the click listener inteface:
```Java
recycler.setOnItemClickListener(new EasyRecyclerView.OnItemClickListener()
{
	@Override
	public void OnItemClick(View v, Object object)
	{
		//TODO: Do something with object and view.
	}
});
```
