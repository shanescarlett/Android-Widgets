# Scarlett Android Widgets

A selection of Android widgets (views) to simplify development.

## Preview

<table width="100%" align="center">
    <tr>
        <th width="33%">
            <a href="../master/EASY_VIEW_PAGER.md">EasyViewPager</a>
        </th>
        <th width="33%">
            <a href="../master/EASY_RECYCLER_VIEW.md">EasyRecyclerView</a>
        </th>
        <th width="33%">
            <a href="../master/FLEX_TEXT_VIEW.md">FlexTextView</a>
        </th>
    </tr>
    <tr>
        <td>A wrapper library for the ViewPager widget to simplify instantiation.</td>
        <td>A wrapper library for the RecyclerView widget to simplify instantiation.</td>
        <td>An adaptive text view widget focused on smooth animations between data changes and configurable truncation modes
            for long texts.</td>
    </tr>
    <tr>
        <td>
            <p align="center">
                <img src="https://raw.githubusercontent.com/shanescarlett/Android-Widgets/master/samples/EasyViewPagerDemo.gif" />
            </p>
        </td>
        <td>
            <p align="center">
                <img src="https://raw.githubusercontent.com/shanescarlett/Android-Widgets/master/samples/EasyRecyclerViewDemo.gif" />
            </p>
        </td>
        <td>
            <p align="center">
                <img src="https://raw.githubusercontent.com/shanescarlett/Android-Widgets/master/samples/FlexTextViewDemo.gif" />
            </p>
        </td>
    </tr>
</table>

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
    compile 'net.scarlettsystems.android:widget:0.1.5'
}
```

## EasyViewPager
See more: [EasyViewPager Readme](../master/EASY_VIEW_PAGER.md)

A wrapper library for the ViewPager widget to simplify instantiation.

### Benefits

Create paged views quickly without the need to specify a Pager Adapter. A generic instance of the adapter is created and managed internally within the EasyViewPager instance. Adding and removing pages are facilitated by simple method calls on this instance, by passing in Views.

## EasyRecyclerView
See more: [EasyRecyclerView Readme](../master/EASY_RECYCLER_VIEW.md)

A wrapper library for the RecyclerView widget to simplify instantiation.

### Benefits

Hides the implementation of the RecyclerView Adapter and simplifies the initialisation process.

## FlexTextView
See more: [FlexTextView Readme](../master/FLEX_TEXT_VIEW.md)

An adaptive text view widget focused on smooth animations between data changes and configurable truncation modes for long texts.

### Benefits

FlexTextView offers different modes to truncate long sequences of texts including
* Collapsing mode where the view is collapsed to display a set number of lines and expanded by the user,
* Scrolling mode where the view is sized to display a set number of lines and the user can scroll through.


## Versioning

Current version: 0.1.5

## Authors

* **Shane Scarlett** - *core development* - [Scarlett Systems](https://scarlettsystems.net)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the Apache 2.0 License
