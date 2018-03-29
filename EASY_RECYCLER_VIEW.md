# EasyRecyclerView

A wrapper library for the RecyclerView widget to simplify instantiation.

# Preview

<p align="center">
    <img src="https://raw.githubusercontent.com/shanescarlett/Android-Widgets/master/samples/EasyRecyclerViewDemo.gif"/>
</p>

## Benefits

Hides the implementation of the RecyclerView Adapter and simplifies the initialisation process.

## Usage

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

The view of the item can be set by either setting a layout resource, or dynamically returning a View through the OnCreateItemViewListener. A routine to bind the item's data to the created view must also be set..

```Java
easyRecyclerView.addOnCreateItemViewListener(ITEM_TYPE,
        new EasyRecyclerView.OnCreateItemViewListener()
{
    @Override
    public View OnCreateItemView()
    {
        View view = new View(getContext());
        return view;
    }

    @Override
    public void OnBindItemView(View view, Object item)
    {
        //TODO: Do something to view.
    }
});
```

OR

```Java
easyRecyclerView.addOnBindItemViewListener(ITEM_TYPE,
        R.layout.my_layout,
        new EasyRecyclerView.OnBindItemViewListener()
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

More than one type of item can be specified this way, through custom defined `ITEM_TYPE` codes. Different view layouts and binding routines can be specified for objects of the same type, depending on how they are required to be displayed in the list.
For example, you may want items to be shown one way, but in-line advertisements to have a different set of data and appearance in the list.

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
