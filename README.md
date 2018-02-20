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
    compile 'net.scarlettsystems.android:widget:0.0.1'
}
```

## EasyViewPager

A wrapper library for the ViewPager widget to simplify instantiation.

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

## Versioning

Current version: 0.0.1 

## Authors

* **Shane Scarlett** - *core development* - [Scarlett Systems](https://scarlettsystems.net)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the Apache 2.0 License
