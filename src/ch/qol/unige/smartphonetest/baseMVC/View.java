package ch.qol.unige.smartphonetest.baseMVC;

import ch.qol.unige.smartphonetest.R;
import ch.qol.unige.network.ConnectionManager;
import ch.qol.unige.smartphonetest.MainActivity;
import ch.qol.unige.smartphonetest.listeners.RotationSensorManager;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ProgressBar;
import android.util.Log;

public abstract class View extends Activity 
{
	protected static final String LOG_STRING = "VIEW";
	protected ProgressBar progressBar = null;
	protected boolean stress = false;
	protected int availableTime = 0;
	protected static ConnectionManager mConnectionManager = null;
	
	private MediaPlayer playerTimer = null;
	private MediaPlayer playerSecondTimer = null;
	
	private RotationSensorManager mRotationSensorManager = null;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	protected void manageRotationSensor()
	{
		mRotationSensorManager = new RotationSensorManager(this);
		mRotationSensorManager.startRecordSensor();
	}
	/**
	 * Sets the value of the progress bar
	 * @param value the new value of the progress bar
	 */
	public void setValueProgressBar(int value)
	{
		progressBar.setProgress(value);
	}
	
	/**
	 * Starts all the additional stressors (tick tock audio)
	 */
	protected void startAudioVideoStressors()
	{
		new Thread() 
		{
			@Override
			public void run() 
			{
				setupTickTockSoundPlayer();
				playerTimer.start();
			}
		}.start();
	}
	
	/**
	 * Initialize the player to reproduce a tick tock sound to stress people more
	 */
	private void setupTickTockSoundPlayer()
	{
		playerTimer = MediaPlayer.create(getApplicationContext(), R.raw.ticktocktimersoundeffect);
		playerTimer.setLooping(true);
	}
	
	/**
	 * Stops all the additional stressors used (tick tock audio)
	 */
	protected void stopAudioVideoStressors()
	{
		try
		{
			if (stress)
			{
				playerTimer.stop();
				playerTimer.reset();
				playerTimer.release();
				playerTimer = null;
			}
		}
		catch(Exception exc)
		{
			Log.d(LOG_STRING + " ViewBaseClass", exc.toString());
		}
	}
	
	public abstract void submitAnswer(android.view.View view);
	
	/**
	 * Resets the value of the ProgressBar to 100 at the end of each repetition
	 */
	protected void resetWidgets()
	{
		if (stress)
		{
			progressBar.setProgress(progressBar.getMax());
		}
	}
	
	/**
	 * The exercise is completed, closes the View and goes back to the MainActivity
	 */
	public void everythingCompletedGoBackToMainActivity()
	{
		if (this.getParent() == null)
		{
			setResult(RESULT_OK);
		}
		else 
		{
			this.getParent().setResult(RESULT_OK);
		}
		mRotationSensorManager.stopRecordSensor();
		finish();
	}
	
	/**
	 * Retrieves the parent layout of all the view to save the information 
	 * about the children of the view
	 */
	protected void saveTaskElementsSizeAndPosition()
	{
		final ViewGroup parentLayout = (ViewGroup) findViewById(R.id.parentLayout);
		
		ViewTreeObserver vto = parentLayout.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				parentLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				Log.d(LOG_STRING, "Writing View Layout");
				MainActivity.followTreeLayoutToSaveElements(parentLayout);
			}
		});
	}
	
	public void onResume()
	{
		super.onResume();
		Log.d(LOG_STRING, "onResume");
	}
	
	public void onDestroy()
	{
		super.onDestroy();
		Log.d(LOG_STRING, "onDestroy");
	}
	
	public void onRestart()
	{
		Log.d(LOG_STRING, "onRestart");
		super.onRestart();
	}
	
	public void onPause()
	{
		Log.d(LOG_STRING, "onPause");
		super.onPause();
	}

	public static void setConnectionManager(ConnectionManager mConnectionManager)
	{
		View.mConnectionManager = mConnectionManager;
	}
}
