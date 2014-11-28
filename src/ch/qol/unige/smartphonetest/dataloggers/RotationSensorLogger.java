package ch.qol.unige.smartphonetest.dataloggers;

import java.io.IOException;

import android.content.Context;
import android.util.Log;

public class RotationSensorLogger extends BasicLogger 
{
	public RotationSensorLogger(Context context)
	{
		super(context, "log_rotation_sensor_data");
	}
	
	/*public void writeLog(String line)
	{
		
	}*/
}
