package ch.qol.unige.gestureanalysis;

import java.util.ArrayList;

import ch.qol.unige.smartphonetest.listeners.TouchData;

public class ScrollSwipeDataAnalysis {
	
	public static int SCREEN_WIDTH_PIXEL;
	public static int SCREEN_HEIGHT_PIXEL;

	private ArrayList<TouchData> touchDataCollection;
	private MinMaxAverage pressure;
	private MinMaxAverage size;
	private MinMaxAverage speed;
	private float timeLength;
	private float movementLength = 0;
	private float startToEndDistance;
	private float startToEndDistanceTotalLengthRatio;
	private float distanceStartPointCenterScreen;
	private float distanceEndPointCenterScreen;
	
	/**
	 * X positive: right side of the screen
	 * Y positive: bottom side of the screen
	 */
	private float deltaStartPointCenterScreenX;
	private float deltaStartPointCenterScreenY;
	
	private float deltaEndPointCenterScreenX;
	private float deltaEndPointCenterScreenY;
	
	public ScrollSwipeDataAnalysis(ArrayList<TouchData> touchDataCollection)
	{
		this.touchDataCollection = touchDataCollection;
		timeLength = touchDataCollection.get(touchDataCollection.size() - 1).getTimestamp()
				- touchDataCollection.get(0).getTimestamp();
		
		float minPressure = Float.MAX_VALUE, maxPressure = Float.MIN_VALUE,
				minSize = Float.MAX_VALUE, maxSize = Float.MIN_VALUE, 
				sumPressure = 0, sumSize = 0;
		
		// Finding min and max value for pressure and size data
		for (TouchData touchData: touchDataCollection)
		{
			if (touchData.getPressure() > maxPressure)
				maxPressure = touchData.getPressure();
			
			if (touchData.getPressure() < minPressure)
				minPressure = touchData.getPressure();
			
			sumPressure += touchData.getPressure();
			
			if (touchData.getSize() > maxSize)
				maxSize = touchData.getSize();
			
			if (touchData.getSize() < minSize)
				minSize = touchData.getSize();
			
			sumSize += touchData.getSize();
		}
		
		pressure = new MinMaxAverage(minPressure, maxPressure, 
				sumPressure / touchDataCollection.size());
		size = new MinMaxAverage(minSize, maxSize, 
				sumSize / touchDataCollection.size());
		
		calculateSpeedData();
		
		calculateTotalLength();
		
		startToEndDistance = euclideanDistance(touchDataCollection.get(touchDataCollection.size() - 1), 
				touchDataCollection.get(0));
		
		startToEndDistanceTotalLengthRatio = startToEndDistance / movementLength;
		
		workWithCenterOfTheScreen();
	}
	
	/**
	 * Calculates the total length of the scroll/swipe as sum of the distance
	 * between the points of the stroke
	 */
	private void calculateTotalLength()
	{
		movementLength = 0;
		for (int i = 0; i < touchDataCollection.size() - 1; i++)
		{
			movementLength += euclideanDistance(touchDataCollection.get(i), 
					touchDataCollection.get(i+1));
		}
	}
	
	/**
	 * Calculates the euclidean distance between two TouchData point
	 * @param data1 first point
	 * @param data2 second point
	 * @return the euclidean distance
	 */
	private float euclideanDistance(TouchData data1, TouchData data2)
	{
		return (float)Math.sqrt(Math.pow(data1.getX() - data2.getX(), 2) + 
				Math.pow(data1.getY() - data2.getY(), 2));
	}
	
	/**
	 * Calculate the euclidean distance between a TouchData point and a general 
	 * point on the screen
	 * @param data the TouchData point
	 * @param x the x-coord of the general point
	 * @param y the y-coord of the general point
	 * @return the euclidean distance between the two points
	 */
	private float euclideanDistance(TouchData data, int x, int y)
	{
		return (float)Math.sqrt(Math.pow(data.getX() - x, 2) + 
				Math.pow(data.getY() - y, 2));
	}
	
	/**
	 * Calculates min, max and average of speed data
	 */
	private void calculateSpeedData() 
	{
		
		float minSpeed = Float.MAX_VALUE, maxSpeed = Float.MIN_VALUE, 
				sumSpeed = 0;
		int count = 0;
		
		for (int i = 0; i < touchDataCollection.size() - 1; i++)
		{
			float distance = euclideanDistance(touchDataCollection.get(i), 
					touchDataCollection.get(i+1));
			float deltaTime = touchDataCollection.get(i+1).getTimestamp() - 
					touchDataCollection.get(i).getTimestamp();
			float speed = distance / deltaTime;
			
			if (speed > maxSpeed)
				maxSpeed = speed;
			
			if (speed < minSpeed)
				minSpeed = speed;
			
			sumSpeed += speed;
			count++;
		}
		
		speed = new MinMaxAverage(minSpeed, maxSpeed, sumSpeed / count);
	}
	
	private void workWithCenterOfTheScreen() 
	{
		deltaStartPointCenterScreenX = touchDataCollection.get(0).getX() - 
				SCREEN_WIDTH_PIXEL / 2;
		deltaStartPointCenterScreenY = touchDataCollection.get(0).getY() - 
				SCREEN_HEIGHT_PIXEL / 2;
		
		deltaEndPointCenterScreenX = touchDataCollection.get(touchDataCollection.size() - 1).getX() - 
				SCREEN_WIDTH_PIXEL / 2;
		deltaEndPointCenterScreenY = touchDataCollection.get(touchDataCollection.size() - 1).getY() - 
				SCREEN_HEIGHT_PIXEL / 2;
				
		
	}
}
