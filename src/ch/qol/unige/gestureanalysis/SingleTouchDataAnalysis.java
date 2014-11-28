package ch.qol.unige.gestureanalysis;

import java.util.ArrayList;

import ch.qol.unige.smartphonetest.listeners.TouchData;

public class SingleTouchDataAnalysis {

	private MinMaxAverage pressure;
	private MinMaxAverage size;
	private float timeLength;
	
	public SingleTouchDataAnalysis(ArrayList<TouchData> touchDataCollection)
	{
		/**
		 * Really a touch interaction
		 */
		if (touchDataCollection.size() == 2)
		{
			// Calculating length of the touch
			timeLength = touchDataCollection.get(1).getTimestamp() - 
					touchDataCollection.get(0).getTimestamp();
			
			// Saving min,max and average value for pressure data
			if (touchDataCollection.get(0).getPressure() > touchDataCollection.get(1).getPressure())
			{
				pressure = new MinMaxAverage(touchDataCollection.get(1).getPressure(), 
						touchDataCollection.get(0).getPressure(), 
						(touchDataCollection.get(0).getPressure() + 
						touchDataCollection.get(1).getPressure()) / 2);
			}
			else 
			{
				pressure = new MinMaxAverage(touchDataCollection.get(0).getPressure(), 
						touchDataCollection.get(1).getPressure(), 
						(touchDataCollection.get(0).getPressure() + 
								touchDataCollection.get(1).getPressure()) / 2);
			}
			
			
			// Saving min, max and average value for size data
			if (touchDataCollection.get(0).getSize() > touchDataCollection.get(1).getSize())
			{
				size = new MinMaxAverage(touchDataCollection.get(1).getSize(), 
						touchDataCollection.get(0).getSize(), 
						(touchDataCollection.get(0).getSize() + 
								touchDataCollection.get(1).getSize()) / 2);
			}
			else 
			{
				size = new MinMaxAverage(touchDataCollection.get(0).getSize(), 
						touchDataCollection.get(1).getSize(), 
						(touchDataCollection.get(0).getSize() + 
								touchDataCollection.get(1).getSize()) / 2);
			}
		}
	}
}
