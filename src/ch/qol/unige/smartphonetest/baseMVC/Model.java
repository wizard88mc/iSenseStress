package ch.qol.unige.smartphonetest.baseMVC;

public class Model {

	protected final static String MODEL_LOG_STRING = "MODEL";
	protected long availableTime = 0; // Available time to submit an answer in milliseconds
	
	/**
	 * Retrieves the total amount of time available to the user
	 * @return the initial amount of time
	 */
	public long getAvailableTime() 
	{
		return this.availableTime;
	}
}
