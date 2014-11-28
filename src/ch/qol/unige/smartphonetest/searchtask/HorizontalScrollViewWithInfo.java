package ch.qol.unige.smartphonetest.searchtask;

import ch.qol.unige.smartphonetest.MainActivity;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

public class HorizontalScrollViewWithInfo extends HorizontalScrollView {
	
	public static boolean scrolling = false;

	public HorizontalScrollViewWithInfo(Context context)
	{
		super(context);
	}
	
	public HorizontalScrollViewWithInfo(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	
	public HorizontalScrollViewWithInfo(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onScrollChanged(int left, int top, int oldLeft, int oldTop)
	{
		super.onScrollChanged(left, top, oldLeft, oldTop);
		
		if (VerticalScrollViewWithInfo.timestampTouchDown != -1 && 
				!HorizontalScrollViewWithInfo.scrolling)
		{
			HorizontalScrollViewWithInfo.scrolling = true;
			MainActivity.mLoggers.writeOnLayoutLogger("H_SCROLL_START," + 
					VerticalScrollViewWithInfo.timestampTouchDown);
		}
		
		MainActivity.mLoggers.writeOnLayoutLogger("H_SCROLL," + left + "," + 
				top + "," + oldLeft + "," + oldTop);
	}
}
