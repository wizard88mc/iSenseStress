package ch.qol.unige.smartphonetest.dataloggers;

import java.io.File;

import ch.qol.unige.smartphonetest.MainActivity;
import android.content.Intent;
import android.net.Uri;

public class Loggers 
{
	private MainActivity mMainActivity = null;
	
	private SettingsLogger mSettingsLogger = null;
	private TouchLogger mTouchLogger = null;
	private LayoutLogger mLayoutLogger = null;
	private RotationSensorLogger mRotationSensorLogger = null;
	
	public Loggers(MainActivity mainActivity)
	{
		this.mMainActivity = mainActivity;
		
		mSettingsLogger = new SettingsLogger(mainActivity);
		mTouchLogger = new TouchLogger(mainActivity);
		mLayoutLogger = new LayoutLogger(mainActivity);
		mRotationSensorLogger = new RotationSensorLogger(mainActivity);
	}
	
	public void resetLoggers()
	{
		BasicLogger.FILENAME_TIMESTAMP = null;
	}
	
	/**
	 * Calls the settings logger to write the new line
	 * @param string the string to write
	 */
	public void writeOnSettingsLogger(String string)
	{
		mSettingsLogger.writeLog(string);
	}
	
	/**
	 * Calls the touch logger to write the new line
	 * @param string the string to write
	 */
	public void writeOnTouchLogger(String string)
	{
		mTouchLogger.writeLog(string);
	}
	
	/**
	 * Calls the layout logger to write the new line
	 * @param string the string to write
	 */
	public void writeOnLayoutLogger(String string)
	{
		mLayoutLogger.writeLog(string);
	}
	
	/**
	 * Calls the rotation sensor logger to write the new line
	 * @param string the string to write
	 */
	public void writeOnRotationSensorLogger(String string)
	{
		mRotationSensorLogger.writeLog(string);
	}
	
	/**
	 * Writes a new step in all the files logger
	 * @param string the string to identify the new step
	 */
	public void writeNewStep(String string)
	{
		mTouchLogger.writeLog(string);
		mLayoutLogger.writeLog(string);
		mRotationSensorLogger.writeLog(string);
	}
	
	/**
	 * Writes a new repetition of the same exercise in all the files logger
	 * @param string the string to write
	 */
	public void writeRepetition(String string)
	{
		mTouchLogger.writeLog(string);
		mLayoutLogger.writeLog(string);
		mRotationSensorLogger.writeLog(string);
	}
	
	/**
	 * Makes file visible calling the MediaScanner
	 */
	public void makeFilesVisible()
	{
		File[] files = {mSettingsLogger.getFile(), mLayoutLogger.getFile(), 
				mTouchLogger.getFile(), mRotationSensorLogger.getFile()};
		
		for (File file: files)
		{
			mMainActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, 
					Uri.fromFile(file)));
		}
	}
	
	/**
	 * Closes all the used files
	 */
	public void closeFiles()
	{
		mSettingsLogger.close(); mLayoutLogger.close(); mTouchLogger.close();
		mRotationSensorLogger.close();
	}
	
	/**
	 * Returns the file saved for the layout information
	 * @return the layout file
	 */
	public File getFileLayoutLogger()
	{
		return mLayoutLogger.getFile();
	}
	
	/**
	 * Returns the file saved with the rotation sensor data
	 * @return the rotation sensor file
	 */
	public File getFileRotationSensorLogger()
	{
		return mRotationSensorLogger.getFile();
	}
	
	/**
	 * Returns the file saved with the settings and results information
	 * @return the settings + results file
	 */
	public File getFileSettingsLogger()
	{
		return mSettingsLogger.getFile();
	}
	
	/**
	 * Returns the file saved with the touch information
	 * @return the touch file
	 */
	public File getFileTouchLogger()
	{
		return mTouchLogger.getFile();
	}
	
	/**
	 * Returns the total number of files that has to be sent to the server
	 * @return the number of files to send
	 */
	public int getTotalFilesToSend()
	{
		return 4;
	}
}
