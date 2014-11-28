package ch.qol.unige.smartphonetest.stressor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import ch.qol.unige.smartphonetest.MainActivity;
import ch.qol.unige.smartphonetest.StepSettings;
import ch.qol.unige.smartphonetest.baseMVC.Controller;

public class StressorController extends Controller 
{	
	private int subRepetition = 1;
	private static final int POINTS_TO_REACH = 100;
	private static final int POINTS_FOR_CORRECT_ANSWER = 4;
	private int currentPointsForCorrectAnswer = POINTS_FOR_CORRECT_ANSWER;
	private static final int POINTS_REDUCTION_FOR_WRONG_ANSWER = 2;
	private int currentPointsEarned = 0;
	
	private int maxTimeAllowed = 10; // minutes
	private ScheduledThreadPoolExecutor maxTimeAllowedScheduler = null;
	private boolean mandatoryStop = false;
	
	public StressorController(StressorView view, Bundle savedInstanceState)
	{
		super(view.getIntent().getBooleanExtra(StepSettings.STRESS, true),
				view.getIntent().getIntExtra(StepSettings.REPETITIONS, 1),
				view.getIntent().getIntExtra(StepSettings.MINUTE_DURATION, 1));
		
		this.view = view;
		this.model = new StressorModel(stress);
	}
	
	/**
	 * Starts a new exercise, increasing the number of the repetition, writing
	 * it on the log file and calling the model to initialize a new exercise
	 */
	public void newExercise()
	{
		currentRepetition++;
		
		writeRepetition();
		
		((StressorModel) model).initializeExercise();
	}
	
	/**
	 * Starts the ScheduledThreadPoolExecutor to check the max time allowed
	 */
	@Override
	public void startTimeCheckIfNecessary()
	{
		maxTimeAllowedScheduler = new ScheduledThreadPoolExecutor(1);
		maxTimeAllowedScheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
		maxTimeAllowedScheduler.scheduleAtFixedRate(new Runnable() {
			public void run()
			{
				maxTimeAllowedScheduler.shutdown();
				StressorController.this.mandatoryStop = true;
			}
		}, maxTimeAllowed, maxTimeAllowed, TimeUnit.MINUTES);
		
		super.startTimeCheckIfNecessary();
	}
	
	/**
	 * Checks if the controller has to stop immediately due to maximum time 
	 * allowed expired
	 * @return
	 */
	public boolean mandatoryStop()
	{
		return mandatoryStop;
	}
	
	/**
	 * The user has submitted and answer. 
	 * @param answer
	 */
	public void submittedAnswer(final int answer)
	{
		startTimeRepetition = 0;
		
		final Boolean[] digits = new Boolean[4];
		StressorModel cModel = (StressorModel) model;
		boolean correct = cModel.checkAnswer(answer, digits);
		
		String[] report = {String.valueOf(currentRepetition), String.valueOf(subRepetition),
				String.valueOf(cModel.getStartingValue()), 
				String.valueOf(cModel.getDecreaseValue()),
				String.valueOf(answer)};
		
		MainActivity.writeResumeOfExercise(report);
		
		if (correct)
		{
			// Updating points for correct answer
			currentPointsEarned += currentPointsForCorrectAnswer;
			if (currentPointsEarned > getPointsToReach())
			{
				currentPointsEarned = getPointsToReach();
			}
			currentPointsForCorrectAnswer++;
			
			// view has to return to the normal state:
			subRepetition++;
			view.runOnUiThread(new Runnable() {
				@Override
				public void run() 
				{
					((StressorView) view).correctAnswerSubmitted();
				}
			});
		}
		else 
		{
			// Updating point for wrongAnswer
			currentPointsEarned -= POINTS_REDUCTION_FOR_WRONG_ANSWER;
			if (currentPointsEarned < 0)
			{
				currentPointsEarned = 0;
			}
			currentPointsForCorrectAnswer--;
			if (currentPointsForCorrectAnswer < POINTS_FOR_CORRECT_ANSWER)
			{
				currentPointsForCorrectAnswer = POINTS_FOR_CORRECT_ANSWER;
			}
			
			// View goes back to initial state
			subRepetition = 1;
			new Thread() {
				@Override
				public void run()
				{
					int startingValue = ((StressorModel) model).getCurrentValue();
					int decreaseVaue = ((StressorModel) model).getDecreaseValue();
					((StressorView) view).wrongAnswerSubmitted(digits, startingValue, 
							decreaseVaue, answer);
				}
			}.start();
			
		}
	}
	
	/**
	 * Retrieves the value to reduce the current value
	 * @return the down value from the model
	 */
	public int getDownValue()
	{
		return ((StressorModel) model).chooseDownValue();
	}
	
	/**
	 * Retrieves the starting value for the exercise
	 * @return the starting value from the model
	 */
	public long getStartingValue()
	{
		return ((StressorModel) model).getStartingValue();
	}
	
	/**
	 * Retrieves the current value of the exercise, which is the one to 
	 * start to go down
	 * @return the current value from the model
	 */
	public long getCurrentValue()
	{
		return ((StressorModel) model).getCurrentValue();
	}
	
	public int getPointsToReach() 
	{
		return POINTS_TO_REACH;
	}
	
	public void trollIsHungry()
	{
		int reduction = currentPointsEarned / 4;
		if (Math.random() < 0.5)
		{
			reduction = currentPointsEarned / 3;
		}
		currentPointsEarned -= reduction;
	}
	
	/**
	 * Returns the percentage of points reached by the player 
	 * @return 100 if the points to reach has been reached, the percentage 
	 * otherwise
	 */
	public int getCompletionPercentage()
	{
		if (currentPointsEarned >= POINTS_TO_REACH)
		{
			return 100;
		}
		else 
		{
			return (currentPointsEarned * 100) / POINTS_TO_REACH;
		}
	}

	/**
	 * Called when the task is completed to stop the maxTimeAllowedScheduler
	 */
	public void exerciseCompleted()
	{
		if (maxTimeAllowedScheduler != null)
		{
			maxTimeAllowedScheduler.shutdown(); maxTimeAllowedScheduler = null;
		}
	}
}
