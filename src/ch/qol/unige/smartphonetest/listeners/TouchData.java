package ch.qol.unige.smartphonetest.listeners;

import android.view.MotionEvent;

public class TouchData {
	
	public final static String SEPARATOR = ",";

	private int action;
	private long timestamp;
	private float x;
	private float y;
	private float size;
	private float pressure;
	private long firstEventTimestamp;
	
	public TouchData(MotionEvent event)
	{
		action = event.getAction();
		timestamp = event.getEventTime();
		x = event.getX(); y = event.getY();
		size = event.getSize();
		pressure = event.getPressure();
		firstEventTimestamp = event.getDownTime();
	}
	
	@Override
	public String toString() 
	{
		return "(" + action + SEPARATOR + timestamp + 
				SEPARATOR + x + SEPARATOR + y + 
				SEPARATOR + size + SEPARATOR + 
				pressure + SEPARATOR + firstEventTimestamp + ")";
	}
	
	public int getAction()
	{
		return this.action;
	}
	
	public long getTimestamp()
	{
		return this.timestamp;
	}
	
	public float getPressure()
	{
		return this.pressure;
	}
	
	public float getSize()
	{
		return this.size;
	}
	
	public float getX()
	{
		return this.x;
	}
	
	public float getY()
	{
		return this.y;
	}
}
