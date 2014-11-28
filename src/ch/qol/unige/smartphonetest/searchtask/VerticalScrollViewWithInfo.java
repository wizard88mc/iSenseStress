package ch.qol.unige.smartphonetest.searchtask;

import ch.qol.unige.smartphonetest.MainActivity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class VerticalScrollViewWithInfo extends ScrollView {
	
	public static long timestampTouchDown = -1;
	public static long timestampTouchUp = -1;
	public static boolean scrolling = false;

	public VerticalScrollViewWithInfo(Context context) {
		super(context);
	}
	
	public VerticalScrollViewWithInfo(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public VerticalScrollViewWithInfo(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);

		if (timestampTouchDown != -1 && !scrolling)
		{
			scrolling = true;
			MainActivity.mLoggers.writeOnLayoutLogger("V_SCROLL_START," 
					+ timestampTouchDown);
		}
		MainActivity.mLoggers.writeOnLayoutLogger("V_SCROLL," + l + "," + 
				t + "," + oldl + "," + oldt);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) 
	{
		
		if (event.getAction() == MotionEvent.ACTION_UP)
		{
			timestampTouchUp = event.getEventTime();
		}
		else if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			if (timestampTouchUp != -1 && 
					(scrolling || HorizontalScrollViewWithInfo.scrolling)) 
			{
				if (scrolling)
				{
					MainActivity.mLoggers.writeOnLayoutLogger("V_SCROLL_END," + 
							timestampTouchUp);
					scrolling = false;
				}
				else 
				{
					MainActivity.mLoggers.writeOnLayoutLogger("H_SCROLL_END," + 
							timestampTouchUp);
					HorizontalScrollViewWithInfo.scrolling = false;
				}
				
				timestampTouchUp = -1;
			}
			
			timestampTouchDown = event.getEventTime();
		}
		
		return super.dispatchTouchEvent(event);
	}
	
	public void closeScrolling()
	{
		if (VerticalScrollViewWithInfo.scrolling)
		{
			MainActivity.mLoggers.writeOnLayoutLogger("V_SCROLL_END," + 
					timestampTouchUp);
			scrolling = false;
		}
		else if (HorizontalScrollViewWithInfo.scrolling)
		{
			MainActivity.mLoggers.writeOnLayoutLogger("H_SCROLL_END," + 
					timestampTouchUp);
			HorizontalScrollViewWithInfo.scrolling = false;
		}
	}
}
