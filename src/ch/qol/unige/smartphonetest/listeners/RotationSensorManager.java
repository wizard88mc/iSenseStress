package ch.qol.unige.smartphonetest.listeners;

import ch.qol.unige.smartphonetest.MainActivity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RotationSensorManager implements SensorEventListener {

	private SensorManager mSensorManager = null;
	private Sensor mRotationSensor = null;
	
	public RotationSensorManager(Context context)
	{
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
	}
	
	/**
	 * Registers the listener (this) to receive data from the rotation sensor
	 */
	public void startRecordSensor()
	{
		mSensorManager.registerListener(this, mRotationSensor, SensorManager.SENSOR_DELAY_UI);
	}
	
	/**
	 * Unregisters the listener (this) and stops receive data from the rotation
	 * sensor
	 */
	public void stopRecordSensor()
	{
		mSensorManager.unregisterListener(this, mRotationSensor);
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) 
	{
		MainActivity.mLoggers.writeOnRotationSensorLogger("(" + event.timestamp + 
				"," + event.values[0] + "," + event.values[1] + "," + 
				event.values[2] + ")");
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	

}
