package android.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class VerticalSeekBar extends ProgressBar 
{
	public VerticalSeekBar(Context context) 
	{
		super(context);
		setIndeterminate(false);
	}

	public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		setIndeterminate(false);
	}

	public VerticalSeekBar(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		setIndeterminate(false);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) 
	{
		super.onSizeChanged(h, w, oldh, oldw);
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	@Override
	protected void onDraw(Canvas c) 
	{
		c.rotate(-90);
		c.translate(-getHeight(), 10);
		super.onDraw(c);
	}

	/*
	 * added onSizeChanged to solve thumb image not updated
	 * @see android.widget.ProgressBar#setProgress(int)
	 */
	@Override
	public synchronized void setProgress(int progress) {
		super.setProgress(progress);
		onSizeChanged(getWidth(), getHeight(), 0, 0);
	}

	/*@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled()) {
			return false;
		}
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
			case MotionEvent.ACTION_UP:
				int i = 0;
				i = getMax() - (int) (getMax() * event.getY() / getHeight());
				setProgress(i);
				onSizeChanged(getWidth(), getHeight(), 0, 0);
				break;
			case MotionEvent.ACTION_CANCEL:
				break;
		}
		return true;
	}*/
}

