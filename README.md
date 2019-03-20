# 嵌套滚动
自从Android 5.0开始，谷歌引进了嵌套滚动的机制，来更好跟简单实现嵌套滚动，那什么是嵌套滚到呢，举个例子：

* 场景一，滚到视图里还包含一个滚动视图，即是ScrollView里又包含了一个ScrollView, 如果滚动的范围在里面ScrollView时，我们先让里面的ScrollView滚动，当里面的ScrollView滚动底部或者顶部时在允许外层的ScrollView滚动。
* 场景二，有三个视图从上往下布局，又给顶部试图，中间的是列表的标题视图，最底部的视图是RecyclerView, 要实现效果，点击RecylerView的范围往上滑动，隐藏顶部视图，但是最多只能滑动到标题视图，然后把事件交给RecyclerView,到RecylerView的内部滑动。隐藏顶部视图后如果往下滑时，如果RecyclerView内部可以往下滑时，直到滑动RecyclerView内部不能往下画时，这时整个视图再向下滚动，逐渐显示顶部视图。

简单来说，嵌套滚动就是滚动视图里面还有一个内部滚动视图。这两种场景如果要Android5.0在之前实现这场效果，就要自己去分发事件，拦截事件。非常繁琐。有了嵌套滑动的机制实现着两种效果，可以说信手拈来，拿来就可以加点代码就可以实现了，此刻是不是有点小激动呢。

## 嵌套滚动机制介绍
谷歌霸霸主要用了两个接口来实现嵌套滚动，NestedScrollingParent,NestedScrollingChild。通过着两个接口来管理父View和子View滚动事件的分发。过程大致是这样的，由子View发起, 每次滑动之前都会询问父View，如果父View不消耗，则由子View处理滑动的事件，如果父View消耗了部分或者全部滑动事件，则会告诉子View消耗了多少滑动事件，子view处理剩下的滑动事件。

## NestedScrollingParent 和 NestedScrollingChild

Android5.0以上所有的View都实现了NestedScrollingChild的方法，ViewGroup实现了NestedScrollingParent的方法，如果要支持到之前的版本要使用v4包的这两个类。

NestedScrollingParent有以下的方法

接口方法 | 说明
---|---
boolean onStartNestedScroll(View child, View target, int axes); | 子View开始滚动时，请求父View是否开始接受嵌套滚动，返回true表示接受，false反之，child是直接的子View,target是产生嵌套滚动的View,axes表示方向，垂直或者左右
void onNestedScrollAccepted( View child,  View target, int axes); | 表示子View接受其嵌套滚动的操作，参数说明同上
void onStopNestedScroll(View target); | 停止嵌套滚动的操作的回调方法，target是产生嵌套滚动的View
void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed); | 目标视图嵌套滚到后回调的方法，dxConsumed，dyConsumed表示已经消耗的滚动距离，dxUnconsumed, dyUnconsumed表示未消耗的滚动视图
void onNestedPreScroll(View target, int dx, int dy, int[] consumed); | 目标视图执行嵌套滚动前的回调，dx,dy 为产生的滚动距离，consumed 为父View消耗的滚动距离
boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed); | 目标视图执行fling事件的回调
boolean onNestedPreFling(View target, float velocityX, float velocityY); | 目标视图执行fling事件的回调前的回调，返回true表示父View消耗了fling事件
 int getNestedScrollAxes(); | 返回嵌套滚动的方向 ViewCompat.SCROLL_AXIS_HORIZONTAL,ViewCompat.SCROLL_AXIS_VERTICAL,ViewCompat.SCROLL_AXIS_NONE


NestedScrollingChild有以下的方法

接口方法 | 说明
---|---
void setNestedScrollingEnabled(boolean enabled) | 设置视图是否允许嵌套滚动
boolean isNestedScrollingEnabled(); | 返回是否允许嵌套滚动
boolean startNestedScroll(int axes); | 表示开始嵌套滚动的操作，axes 为滚动的方向
void stopNestedScroll(); | 停止嵌套滚动
boolean hasNestedScrollingParent(); | 返回是否存在嵌套滚动的父View
boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow); | 分发嵌套滚动的事件
boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, int[] offsetInWindow); | 分发执行嵌套滚动前的事件
boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed); | 分发fling事件
boolean dispatchNestedPreFling(float velocityX, float velocityY); | 分发执行fling前的事件

### 怎么实现这两个接口？
对于这两个接口的实现，其实谷歌已经提供了两个方法NestedScrollingParentHelper和NestedScrollingChildHelper实现。

NestedScrollingParentHelper已经帮你实现了onNestedScrollAccepted，getNestedScrollAxes，onStopNestedScroll三个方法，其他方法则需要自己根据需求来实现。

对于要想实现NestedScrollingChild的View，可以完全使用NestedScrollingChildHelper，参考RecyclerView的源码，如下
```
 // NestedScrollingChild

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        getScrollingChildHelper().setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return getScrollingChildHelper().isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return getScrollingChildHelper().startNestedScroll(axes);
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return getScrollingChildHelper().startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll() {
        getScrollingChildHelper().stopNestedScroll();
    }

    @Override
    public void stopNestedScroll(int type) {
        getScrollingChildHelper().stopNestedScroll(type);
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return getScrollingChildHelper().hasNestedScrollingParent();
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return getScrollingChildHelper().hasNestedScrollingParent(type);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
            int dyUnconsumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
            int dyUnconsumed, int[] offsetInWindow, int type) {
        return getScrollingChildHelper().dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow,
            int type) {
        return getScrollingChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow,
                type);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return getScrollingChildHelper().dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return getScrollingChildHelper().dispatchNestedPreFling(velocityX, velocityY);
    }
```
而我们自己要做什么事呢，恰当的时候调用对应的方法即可，比如开始嵌套滚动时，调用startNestedScroll(int axes)。即是在onTouchEvent的DOWN事件中调用，开始产生滑动时，先调用
dispatchNestedPreScroll方法。可以参考RecyclerView中的onTouchEvent方法
```
 public boolean onTouchEvent(MotionEvent e) {
        ···
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                startNestedScroll(nestedScrollAxis, TYPE_TOUCH);
            } break;

        ···
            case MotionEvent.ACTION_MOVE: {

                if (dispatchNestedPreScroll(dx, dy, mScrollConsumed, mScrollOffset, TYPE_TOUCH)) {
                  ···
                }

        ···

        return true;
    }
```
省略很多无用代码，第二个参数是触摸类型，稍后会说到，这里先认为等同没有这个参数的方法就可以了。

### NestedScrollingParent2 和 NestedScrollingChild2
由于上面两个类有局限性，如子View的fling事件如果没有被子View完全消耗时，不能通知父View，使父View不能继续处理剩余的fling事件。会造成滑动的不顺畅。谷歌在Andorid8.0 引入了新增这两个类，对几个方法加了滑动类型的参数。用来解决上面说的局限性，具体可以参考
https://blog.csdn.net/humorousz/article/details/79552635
这篇文章，写得非常详细。

## 场景一的实现
嵌套滚动里面又包含了一个嵌套滚动。

Android的support包中提供了一个支持嵌套滚动的View，使用NestedScrollView很容易实现这个效果，因为NestedScrollView同时实现了NestedScrollingParent, NestedScrollingChild2，所以只需NestedScrollView中嵌套一个NestedScrollView子View即可实现，代码如下：

```
<?xml version="1.0" encoding="utf-8"?>
<android.support.v4.widget.NestedScrollView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MainActivity">

    <LinearLayout
        android:id="@+id/layout_top"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <TextView
            android:id="@+id/textView1"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:text="@string/text1"
            android:gravity="center"
            android:textSize="18sp"
            android:lineSpacingExtra="20dp"
            android:textColor="#9C27B0" />

        <android.support.v4.widget.NestedScrollView
            android:layout_width="match_parent"
            android:layout_height="200dp"
            android:layout_margin="12dp"
            android:background="#7003A9F4"
            android:orientation="vertical"
            tools:context=".MainActivity">

            <LinearLayout
                android:id="@+id/layout_top2"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"
                    android:text="@string/poem"
                    android:gravity="center"
                    android:textSize="18sp"
                    android:lineSpacingExtra="16dp"
                    android:textColor="#161217"  />

            </LinearLayout>

        </android.support.v4.widget.NestedScrollView>

        <TextView
            android:id="@+id/textView2"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:layout_marginTop="18dp"
            android:text="@string/text2"
            android:gravity="center"
            android:textSize="18sp"
            android:lineSpacingExtra="20dp"
            android:textColor="#161217" />

    </LinearLayout>

</android.support.v4.widget.NestedScrollView>
```
效果如下：

![单独](https://note.youdao.com/yws/api/personal/file/0990AA7F5A5B402FA8155C8A91154340?method=download&shareKey=6a52b4457dac754fdfa5c1a340b2acb9)

## 场景二的实现

这是先看效果

![图片2](https://note.youdao.com/yws/api/personal/file/349BB43C709D4A4B8B60B8D38337EAEF?method=download&shareKey=32fb06617db29c16d461356e18d4b020)

创建一个叫做StickTitleView类，继承ViewGroup，实现NestedScrollingParent2接口
```
public class StickTitleView extends ViewGroup implements NestedScrollingParent2 {

	private static final String TAG = StickTitleView.class.getSimpleName();

	private View topView;
	private View indicationView;
	private View bottomView;

	public StickTitleView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		ensureView();
		final int height = MeasureSpec.getSize(heightMeasureSpec);
		measureChild(topView, widthMeasureSpec, heightMeasureSpec);
		measureChild(indicationView, widthMeasureSpec, heightMeasureSpec);
		measureChild(bottomView, widthMeasureSpec,
					 MeasureSpec.makeMeasureSpec(height - indicationView.getMeasuredHeight(), MeasureSpec.AT_MOST));

		final int desireHeight = topView.getMeasuredHeight() + indicationView.getMeasuredHeight() + bottomView.getMeasuredHeight();
		setMeasuredDimension(widthMeasureSpec, Math.min(desireHeight, height));
	}

	/**
	 * 从上往下布局
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int topOffset = 0;
		topView.layout(0, topOffset, topView.getMeasuredWidth(), topOffset + topView.getMeasuredHeight());
		topOffset += topView.getMeasuredHeight();
		indicationView.layout(0, topOffset, indicationView.getMeasuredWidth(), topOffset + indicationView.getMeasuredHeight());
		topOffset += indicationView.getMeasuredHeight();
		bottomView.layout(0, topOffset, bottomView.getMeasuredWidth(), topOffset + bottomView.getMeasuredHeight());

	}

	private void ensureView() {
		if (getChildCount() < 3) {
			throw new IllegalStateException();
		}
		topView = getChildAt(0);
		indicationView = getChildAt(1);
		bottomView = getChildAt(2);
	}

	@Override
	public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
		//如果是竖直方向就返回true,表示接受竖直方向的滚动
		return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
	}

	@Override
	public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
	}

	@Override
	public void onStopNestedScroll(@NonNull View target, int type) {
	}

	@Override
	public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
	}

	@Override
	public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
		//向下滑动，如果符合bottomView不能往下滑并且mScrollY大于0，就要显示顶部
		boolean showTop = dy < 0 && getScrollY() > 0 && !bottomView.canScrollVertically(-1);
		//向上滑动，如果mScrollY小于顶部的高，就要隐藏顶部
		boolean hideTop = dy > 0 && getScrollY() < topView.getMeasuredHeight();
		if (showTop || hideTop) {
			scrollBy(0, dy);
			consumed[1] = dy;
		}
	}

	@Override
	public void scrollTo(int x, int y) {
		//限制滚动的范围，不能小于0和大于topView的高度
		if (y <= 0) {
			y = 0;
		}
		if (y >= topView.getMeasuredHeight()) {
			y = topView.getMeasuredHeight();
		}

		super.scrollTo(x, y);

	}

}

```
```
<?xml version="1.0" encoding="utf-8"?>
<com.etwge.testnestedscrollapplication.StickTitleView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".Sample2Activity">

    <LinearLayout
        android:id="@+id/layout_top"
        android:layout_width="match_parent"
        android:layout_height="300dp"
        android:background="#90288B"
        android:orientation="vertical"
        android:text="Hello World!" />

    <LinearLayout
        android:id="@+id/layout_indicate"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#0D6A81"
        android:orientation="vertical">

        <ImageView
            android:layout_width="match_parent"
            android:layout_height="60dp"
            android:background="#0de" />
    </LinearLayout>

    <android.support.v7.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</com.etwge.testnestedscrollapplication.StickTitleView>
```


# 总结
通过使用嵌套滚动这套机制，使得很多之前要写很多自定义分发才能做出的效果变得容易起来。如果碰到类似的嵌套滚动效果，首要想到的就是这套嵌套滚动机制，根据需求实现调用即可。

最后附上本编文章的源码


https://www.jianshu.com/p/f09762df81a5