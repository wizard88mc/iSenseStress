package ch.qol.unige.smartphonetest.dataloggers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ch.qol.unige.smartphonetest.MainActivity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BasicLogger {

	public static String FILENAME_TIMESTAMP = null;
	protected static final String LOG_STRING = "BASIC LOGGER";
	public static final Boolean DEBUG = true;
	private File file;
	private BufferedWriter buf = null;
	
	public BasicLogger(Context context, String prefix)
	{	
		if (FILENAME_TIMESTAMP == null)
		{
			GregorianCalendar today = new GregorianCalendar();
		
			FILENAME_TIMESTAMP = today.get(Calendar.YEAR) 
				+ String.format("%02d", today.get(Calendar.MONTH))
				+ String.format("%02d", today.get(Calendar.DATE)) 
				+ String.format("%02d",today.get(Calendar.HOUR)) 
				+ String.format("%02d", today.get(Calendar.MINUTE)) 
				+ String.format("%02d", today.get(Calendar.SECOND)) 
				+ today.get(Calendar.MILLISECOND);
		}
		
		MainActivity.IMEI = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		
		String filename =  MainActivity.IMEI + "_" + FILENAME_TIMESTAMP + "_" 
				+ prefix + ".csv";
		
		
		file = new File(context.getFilesDir(), filename);
		
		if (!file.exists()) 
		{
			try 
			{
				file.createNewFile();
			}
			catch(IOException exc) 
			{
				Log.e("LOGGER", "No file created " + filename);
			}
		}
		
		try {
			buf = new BufferedWriter(new FileWriter(file, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes a new line in the log file
	 * @param line the line to write (add a \n at the end of the new line)
	 */
	public void writeLog(String line)
	{
		try 
		{
			if (DEBUG)
			{
				Log.d(LOG_STRING, "Writing string: " + line);
			}
			buf.write(line + "\n");
			buf.flush();
		}
		catch(IOException exc)
		{
			Log.e(LOG_STRING, exc.toString());
			exc.printStackTrace();
		}
	}
	
	public void close()
	{
		try {
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the path of the log file
	 * @return the path of the file
	 */
	public String getPath()
	{
		return file.getPath();
	}
	
	public File getFile()
	{
		return file;
	}
}
