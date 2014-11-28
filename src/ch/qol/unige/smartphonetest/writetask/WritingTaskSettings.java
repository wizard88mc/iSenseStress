package ch.qol.unige.smartphonetest.writetask;

/**
 * This class contains all the settings necessary to setup a level of the 
 * writing task. The settings are: text to write, the amount of time available 
 * and if the text to write has to be presented using different fonts and size
 * 
 * @author Matteo Ciman
 *
 */
public class WritingTaskSettings {

	private String textToWrite;
	private long availableTime;
	private boolean mixTextFont;
	private boolean stressed;
	
	public WritingTaskSettings(String textToWrite, long availableTime, 
			boolean mixTextFont, boolean stressed)
	{
		this.textToWrite = textToWrite; this.availableTime = availableTime;
		this.mixTextFont = mixTextFont; this.stressed = stressed;
	}
	
	public String getTextToWrite()
	{
		return this.textToWrite;
	}
	
	public long getAvailableTime()
	{
		return this.availableTime;
	}
	
	public boolean mixTextFont()
	{
		return this.mixTextFont;
	}
	
	public boolean isStressed()
	{
		return this.stressed;
	}
}
