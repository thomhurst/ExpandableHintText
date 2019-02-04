# ExpandableHintText
A Customisable Pretty EditText Layout for Android

[![](https://jitpack.io/v/thomhurst/ExpandableHintText.svg)](https://jitpack.io/#thomhurst/ExpandableHintText)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/e6f3c4e528114f678c3feb970b140cbc)](https://www.codacy.com/app/thomhurst/ExpandableHintText?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=thomhurst/ExpandableHintText&amp;utm_campaign=Badge_Grade)
[![Android Arsenal]( https://img.shields.io/badge/Android%20Arsenal-Expandable%20Hint%20Texts-green.svg?style=flat )]( https://android-arsenal.com/details/1/7499 )
[![Android Weekly](https://img.shields.io/badge/Android%20Weekly-347-blue.svg)](http://androidweekly.net/issues/issue-347)

## Install

Add Jitpack to your repositories in your `build.gradle` file

```groovy
allprojects {
    repositories {
      // ...
      maven { url 'https://jitpack.io' }
    }
}
```

Add the below to your dependencies, again in your gradle.build file

```groovy
implementation 'com.github.thomhurst:ExpandableHintText:{version}'
```

### On Click Animation
![](https://github.com/thomhurst/ExpandableHintText/blob/master/images/expandable-hinttext-gif.gif)

### Typing
![](https://github.com/thomhurst/ExpandableHintText/blob/master/images/expandable-hinttext-gif2.gif)

## Sample Code
### Light on Dark

<img src="https://github.com/thomhurst/ExpandableHintText/blob/master/images/light-on-dark.png" width="450"/>

```xml
<com.tomlonghurst.expandablehinttext.ExpandableHintText
                android:layout_marginTop="32dp"
                android:layout_width="300dp"
                android:layout_height="wrap_content"
                android:text="Tom"
                android:textColor="@color/colorPrimaryDark"
                android:hint="First Name"
                app:textBoxColor="@android:color/white"
                app:floatingLabelColor="@android:color/white"
                app:imageColor="@color/colorPrimary"
                app:image="@drawable/ic_baseline_sentiment_very_satisfied_24px"
        />
```

### Dark on Light 

<img src="https://github.com/thomhurst/ExpandableHintText/blob/master/images/dark-on-light.png" width="450"/>

```xml
<com.tomlonghurst.expandablehinttext.ExpandableHintText
                android:layout_marginTop="32dp"
                android:layout_width="300dp"
                android:layout_height="wrap_content"
                android:text="Tom"
                android:textColor="@android:color/white"
                android:hint="First Name"
                app:textBoxColor="@color/colorPrimaryDark"
                app:floatingLabelColor="@color/colorPrimaryDark"
                app:imageColor="@color/white"
                app:image="@drawable/ic_baseline_sentiment_very_satisfied_24px"
        />
```

If you enjoy, please buy me a coffee :)

<a href="https://www.buymeacoffee.com/tomhurst" target="_blank"><img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy Me A Coffee" style="height: auto !important;width: auto !important;" ></a>
