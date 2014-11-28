package ch.qol.unige.smartphonetest.baseMVC;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.util.Log;
import ch.qol.unige.smartphonetest.MainActivity;

public class Controller 
{	
	protected static final String CONTROLLER_LOG_STRING = "CONTROLLER";
	
	protected View view;
	protected Model model;
	
	protected long startTimeRepetition = 0;
	protected TimerManager timeManager = new TimerManager();
	protected ScheduledThreadPoolExecutor executor = null;
	protected ScheduledThreadPoolExecutor executorAvailableTime = null;
	
	protected boolean stress = false;
	protected int numberRepetitions = -1;
	protected int currentRepetition = 0;
	protected int totalTimeDuration = -1;
	protected boolean continuePlaying = true;
	
	public Controller(boolean stress, int numberRepetitions,
			int timeDuration)
	{
		this.stress = stress; this.numberRepetitions = numberRepetitions;
		this.totalTimeDuration = timeDuration;
	}
	
	/**
	 * Start the countdown for a single exercise. Called only in stress mode
	 */
	public void exerciseStarted()
	{
		Calendar cal = new GregorianCalendar();
		if (startTimeRepetition == 0)
		{
			startTimeRepetition = cal.getTime().getTime();
		}
		executor = new ScheduledThreadPoolExecutor(1);
		executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		executor.scheduleAtFixedRate(timeManager, 0, 25, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * It checks if the exercise has a time duration, and in case starts a thread
	 * that waits the defined time before ask to stop the test
	 */
	public void startTimeCheckIfNecessary()
	{
		if (totalTimeDuration != -1)
		{
			/**
			 * The exercise has to last at least the defined time
			 */
			executorAvailableTime = new ScheduledThreadPoolExecutor(1);
			executorAvailableTime.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
			executorAvailableTime.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					
					continuePlaying = false;			
					Controller.this.executorAvailableTime.shutdown();
					Controller.this.executorAvailableTime = null;
				}
			}, totalTimeDuration, totalTimeDuration, TimeUnit.MINUTES);
		}
	}
	
	/**
	 * This class is responsible to work as a timer during a stress task,
	 * to check the duration of the task and decrease the value of the 
	 * progress bar
	 * @author Matteo Ciman
	 *
	 */
	protected class TimerManager implements Runnable 
	{
		@Override
		public void run() {
			Calendar cal = new GregorianCalendar();
			long currentTime = cal.getTime().getTime();
			long elapsedTimeTimerManager = currentTime - startTimeRepetition;
			long remainingTime = model.getAvailableTime() - elapsedTimeTimerManager;
			
			if (remainingTime < 0) {
				view.setValueProgressBar(0);
				// Time expired, behave as submitted answer
				stopExercise();
				
				view.runOnUiThread(new Thread() {
					public void run() {
						view.submitAnswer(null);
					}
				});
			}
			else {
				int percentage = (int)(100 * remainingTime / model.getAvailableTime());
				view.setValueProgressBar(percentage);
			}
		}	
	}
	
	/**
	 * If the user submits a wrong answer, it is possible to reduce the start time, 
	 * increasing the passed time, to penalize the user
	 * @param numberMilliseconds milliseconds to reduce the start time
	 */
	protected void decreaseStartTimeToPenalize(int numberMilliseconds)
	{
		startTimeRepetition -= numberMilliseconds;
	}
	
	/**
	 * Stop the current exercise due to the answer submitted by the user or for 
	 * time expired
	 */
	public void stopExercise()
	{
		view.stopAudioVideoStressors();
		if (stress)
		{
			if (executor != null)
			{
				executor.shutdown();
				executor = null;
			}
		}
		startTimeRepetition = 0;
	}
	
	/**
	 * Returns if the current test has to continue or not, depending on the number
	 * of repetition or if the time is expired
	 * @return if continue playing with the current test or not
	 */
	public boolean getContinuePlaying() 
	{
		Log.d("Controller", "checking continue playing");
		if (numberRepetitions != -1)
		{
			return numberRepetitions != currentRepetition;
		}
		else 
		{
			return this.continuePlaying;
		}
	}
	
	/**
	 * Returns if the current exercise is in stress mode or not
	 * @return true if stress, false otherwise
	 */
	public boolean stress()
	{
		return this.stress;
	}
	
	/**
	 * Writes the current repetition of the exercise on the log files
	 */
	public void writeRepetition()
	{
		MainActivity.mLoggers.writeRepetition("************ REPETITION " + 
				currentRepetition + " ************");
	}
	
	public int getTotalTimeDuration()
	{
		return this.totalTimeDuration;
	}
}
