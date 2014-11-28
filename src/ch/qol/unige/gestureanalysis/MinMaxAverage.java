package ch.qol.unige.gestureanalysis;

public class MinMaxAverage {

	private float minValue;
	private float maxValue;
	private float averageValue;
	
	public MinMaxAverage(float minValue, float maxValue, float averageValue)
	{
		this.minValue = minValue; this.maxValue = maxValue; 
		this.averageValue = averageValue;
	}
	
	public float getMinValue()
	{
		return this.minValue;
	}
	
	public float getMaxValue()
	{
		return this.maxValue;
	}
	
	public float getAverageValue()
	{
		return this.averageValue;
	}
}
