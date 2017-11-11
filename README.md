# android-logcat-fragment
Display logcat. This library is for your debugging.

## How to add

will be available on `jCenter`

```
dependencies {
   debugImplementation 'com.mokelab:LogcatFragment:1.0.0.27'
   ...
}
```

## Add DebugActivity

Just an Activity for debugging.

```
public class DebugActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
    }
}
```

LayoutXML(`src/debug/res/layout/activity_debug.xml`)

```
<fragment
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    class="com.mokelab.lib.logcat.LogcatFragment">
</fragment>
```

Add this to `AndroidManifest.xml`

```
<manifest package="{package name}"
          xmlns:android="http://schemas.android.com/apk/res/android">

    <application>
        <activity
            android:name=".DebugActivity"
            android:taskAffinity="${applicationId}.debug"
            android:label="@string/debug">
            <intent-filter
                android:label="@string/debug">
                <action android:name="android.intent.action.MAIN"/>

                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
    </application>

</manifest>
```

