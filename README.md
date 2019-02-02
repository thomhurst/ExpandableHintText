# ExpandableHintText
A Customisable Pretty EditText Layout for Android

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
