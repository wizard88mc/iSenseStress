package ch.qol.unige.smartphonetest.stressor;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.qol.unige.smartphonetest.R;
import ch.qol.unige.smartphonetest.MainActivity;
import ch.qol.unige.smartphonetest.listeners.TouchData;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VerticalSeekBar;
import android.widget.ViewFlipper;

public class StressorView extends ch.qol.unige.smartphonetest.baseMVC.View 
	implements ResumeStressorAnswer.ResumeStressorAnswerListener
{	
	private ViewFlipper flipperLayout = null;
	private TextView decreaseValue = null;
	private ArrayList<NumberPicker> numberPickerDigits = null;
	private ArrayList<ImageView> wrongDigitsImageView = null;
	private VerticalSeekBar verticalSeekBarPoints = null;
	private TextView textViewTroll;
	private TextView textViewYes;
	
	private StressorController controller = null;
	
	private boolean firstStep = true;
	private boolean recordTouchData = false;

	private ScheduledThreadPoolExecutor executorThreadTroll = null;
	private Animation animationTextViewTroll = null;
	
	private Animation animationTextViewYes = null;
	
	private AlertDialog welcomeDialog;
	private ScheduledThreadPoolExecutor executorTimerWelcomeDialog = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stressor_layout);
		
		textViewTroll = (TextView) findViewById(R.id.feedTroll);
		
		animationTextViewTroll = new AlphaAnimation(0.0f, 1.0f);
		animationTextViewTroll.setDuration(500);
		animationTextViewTroll.setStartOffset(20);
		animationTextViewTroll.setRepeatMode(Animation.REVERSE);
		animationTextViewTroll.setRepeatCount(5);
		
		animationTextViewTroll.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				textViewTroll.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				textViewTroll.setVisibility(View.INVISIBLE);
			}
		});
		
		textViewYes = (TextView) findViewById(R.id.yes);
		
		animationTextViewYes = new AlphaAnimation(0.0f, 1.0f);
		animationTextViewYes.setDuration(500);
		animationTextViewYes.setStartOffset(10);
		animationTextViewYes.setRepeatMode(Animation.REVERSE);
		animationTextViewYes.setRepeatCount(2);
		
		animationTextViewYes.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				textViewYes.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				textViewYes.setVisibility(View.INVISIBLE);
				StressorView.this.findViewById(R.id.buttonSubmit).setEnabled(true);
			}
		});
		
		controller = new StressorController(this, savedInstanceState);
		
		stress = controller.stress();
		
		flipperLayout = (ViewFlipper) findViewById(R.id.flipperLayout);
		flipperLayout.setInAnimation(getApplicationContext(), R.anim.abc_fade_in);
		flipperLayout.setOutAnimation(getApplicationContext(), R.anim.abc_fade_out);
		
		verticalSeekBarPoints = (VerticalSeekBar) findViewById(R.id.verticalSeekBarPoints);
		verticalSeekBarPoints.setOnTouchListener(new OnTouchListener() {	
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		
		super.manageRotationSensor();
		
		if (!stress)
		{
			((LinearLayout) findViewById(R.id.layoutProgressBar)).setVisibility(View.INVISIBLE);
		}
		
		AlertDialog.Builder builderWelcomeDialog = new AlertDialog.Builder(StressorView.this);
		builderWelcomeDialog.setTitle(R.string.and_now);
		builderWelcomeDialog.setMessage(R.string.some_math_problems);
		welcomeDialog = builderWelcomeDialog.create();
		welcomeDialog.setCancelable(false);
		welcomeDialog.setCanceledOnTouchOutside(false);
		
		executorTimerWelcomeDialog = new ScheduledThreadPoolExecutor(1);
		executorTimerWelcomeDialog.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
		executorTimerWelcomeDialog.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						
						StressorView.this.welcomeDialog.dismiss();
						StressorView.this.executorTimerWelcomeDialog.shutdown();
						StressorView.this.executorTimerWelcomeDialog = null;
						
						firstStep(false);
						
						secondStepExerciseSetup();
						
						controller.startTimeCheckIfNecessary();
						
						if (stress && executorThreadTroll == null)
						{
							executorThreadTroll = new ScheduledThreadPoolExecutor(1);
							executorThreadTroll.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
							executorThreadTroll.scheduleAtFixedRate(new Runnable() {
								
								@Override
								public void run() 
								{
									try 
									{
										if (Math.random() < 0.75)
										{		
											controller.trollIsHungry();
											
											StressorView.this.runOnUiThread(new Runnable() {
												
												@Override
												public void run() {
													StressorView.this.trollEatedSomePoints();
												}
											});
										}
									}
									catch(Exception exc)
									{
										exc.printStackTrace();
									}	
								}
							}, controller.getTotalTimeDuration() - 2, 2, TimeUnit.MINUTES);	
						}
					}
				});
				
			}
		}, 5, 5, TimeUnit.SECONDS);
		
		welcomeDialog.show();
	}
	
	/**
	 * Setup of the view for the first step, retrieves the starting point and 
	 * manages the flipping to the next view
	 */
	private void firstStep(boolean restored) 
	{
		if (!(controller.mandatoryStop()) && (controller.getCompletionPercentage() != 100 || 
				controller.getCompletionPercentage() == 100 && controller.getContinuePlaying()))
		{
			if (!restored)
			{
				controller.newExercise();
			}
			
			flipperLayout.getInAnimation().setAnimationListener(new Animation.AnimationListener() 
			{	
				@Override
				public void onAnimationStart(Animation animation) {	}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					if (firstStep) 
					{
						flipperLayout.getInAnimation().setAnimationListener(null);
						StressorView.this.saveTaskElementsSizeAndPosition();
						StressorView.this.start();
					}
					firstStep = !firstStep;
				}
			});
			
			if (!restored || restored && firstStep)
			{
				ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
				executor.allowCoreThreadTimeOut(false);
				executor.schedule(new Runnable() 
				{
					@Override
					public void run() {
						
						StressorView.this.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								StressorView.this.flipperLayout.showNext();
							}
						});
					}
				}, 3000, TimeUnit.MILLISECONDS);
				
				((TextView) findViewById(R.id.startingValue)).
					setText(String.valueOf(controller.getStartingValue()));
			}
			/**
			 * If in the previous state the second step was already shown, 
			 * immediately show it
			 */
			else if (restored && !firstStep)
			{
				firstStep = !firstStep;
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() 
					{
						StressorView.this.flipperLayout.showNext();
					}
				});
			}
			
		}
		else 
		{
			this.everythingCompletedGoBackToMainActivity();
		}
	}
	
	/**
	 * Start the exercise retrieving the value to go down and informing the 
	 * controller about the start
	 */
	private void start()
	{
		int valueGoDown = controller.getDownValue();
		decreaseValue.setText(" - " + valueGoDown);
		decreaseValue.invalidate();
		
		if (stress)
		{
			startAudioVideoStressors();
			controller.exerciseStarted();
		}
		
		recordTouchData = true;
		
		if (!stress)
		{
			String startingValue = String.valueOf(controller.getCurrentValue());
			for (int i = 0; i < numberPickerDigits.size(); i++)
			{
				String digit = ((Character) startingValue.charAt(i)).toString();
				numberPickerDigits.get(i).setValue(Integer.valueOf(digit));
			}
		}
		
		ScheduledThreadPoolExecutor executorActivateButton = new ScheduledThreadPoolExecutor(1);
		executorActivateButton.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		executorActivateButton.schedule(new Runnable() {
			
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						StressorView.this.findViewById(R.id.buttonSubmit).setEnabled(true);
						
					}
				});
				
			}
		}, 1000, TimeUnit.MILLISECONDS);
		
	}
	
	/**
	 * Setup of the elements that manage the second step of the stressor exercise
	 */
	private void secondStepExerciseSetup()
	{
		progressBar = (ProgressBar) findViewById(R.id.progressTimingBar);
		
		decreaseValue = (TextView) findViewById(R.id.decreaseValue);
		
		if (numberPickerDigits != null)
		{
			numberPickerDigits.clear();
		}
		else
		{
			numberPickerDigits = new ArrayList<NumberPicker>();
		}
		if (wrongDigitsImageView != null) 
		{
			wrongDigitsImageView.clear();
		}
		else 
		{
			wrongDigitsImageView = new ArrayList<ImageView>();
		}
		
		numberPickerDigits.add((NumberPicker) findViewById(R.id.firstDigit));
		numberPickerDigits.add((NumberPicker) findViewById(R.id.secondDigit));
		numberPickerDigits.add((NumberPicker) findViewById(R.id.thirdDigit));
		numberPickerDigits.add((NumberPicker) findViewById(R.id.fourthDigit));
		
		for (NumberPicker digit: numberPickerDigits) 
		{
			digit.setMinValue(0); digit.setMaxValue(9); digit.setValue(0);
		}
		
		wrongDigitsImageView.add((ImageView) findViewById(R.id.firstDigitWrong));
		wrongDigitsImageView.add((ImageView) findViewById(R.id.secondDigitWrong));
		wrongDigitsImageView.add((ImageView) findViewById(R.id.thirdDigitWrong));
		wrongDigitsImageView.add((ImageView) findViewById(R.id.fourthDigitWrong));
	}
	
	/**
	 * Reset to the base state for the widgets
	 */
	public void resetWidgets()
	{
		super.resetWidgets();
		
		//findViewById(R.id.buttonSubmit).setEnabled(true);
		
		if (stress) 
		{
			for (NumberPicker digit: numberPickerDigits) {
				digit.setValue(0);
				digit.setEnabled(true);
			}
		}
		else
		{
			for (NumberPicker digit: numberPickerDigits)
			{
				digit.setEnabled(true);
			}
		}
		
		for (ImageView imgView: wrongDigitsImageView) {
			imgView.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * Wrong answer submitted by the user, show X over the wrong digits 
	 * @param digits contains which digit is wrong and which is correct (NOT USED)
	 */
	public void wrongAnswerSubmitted(Boolean[] digits, int startValue, int reduce, 
			int submittedValue)
	{
		Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
		MediaPlayer player = MediaPlayer.create(getApplicationContext(), R.raw.wrong);
		for (int i = 0; i < digits.length; i++) 
		{
			if (!digits[i] || digits[i]) 
			{
				final int index = i;
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() 
					{
						wrongDigitsImageView.get(index).setVisibility(View.VISIBLE);
						wrongDigitsImageView.get(index).invalidate();
					}
				});
				
				if (stress)
				{
					player.start();
					v.vibrate(250);
					try 
					{
						Thread.sleep(500);
					}
					catch(InterruptedException exc) {
						exc.toString();
						exc.printStackTrace();
					}
				}
			}
		}
		
		if (mConnectionManager != null)
		{
			mConnectionManager.sendMessageStressorTaskAnswerSubmitted(false);
		}
		
		ResumeStressorAnswer dialog = new ResumeStressorAnswer(stress, startValue, 
				reduce, submittedValue);
		dialog.show(getFragmentManager(), null);
	}
	
	/**
	 * The controller notifies the view that a correct answer has been submitted
	 */
	public void correctAnswerSubmitted()
	{
		if (mConnectionManager != null)
		{
			mConnectionManager.sendMessageStressorTaskAnswerSubmitted(true);
		}
		if (controller.mandatoryStop() || 
				(!controller.getContinuePlaying() && 
						controller.getCompletionPercentage() == 100))
		{
			controller.exerciseCompleted();
			this.everythingCompletedGoBackToMainActivity();
		}	
		else
		{
			resetWidgets();
			start();
			findViewById(R.id.buttonSubmit).setEnabled(false);
			textViewYes.startAnimation(animationTextViewYes);
		}
	}
	
	/**
	 * Method called when the user submits an answer clicking on the button
	 */
	@Override
	public void submitAnswer(final View view) 
	{	
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				
				recordTouchData = false;
				if (view != null)
				{
					controller.stopExercise();
				}
				
				findViewById(R.id.buttonSubmit).setEnabled(false);
				
				String answer = "";
				
				for (NumberPicker digit: numberPickerDigits)
				{
					answer += digit.getValue();
					digit.setEnabled(false);
				}

				controller.submittedAnswer(Integer.valueOf(answer));
				
				verticalSeekBarPoints.setProgress(controller.getCompletionPercentage());
			}
		});
	}
	
	/**
	 * Updates the user interface updating the progress bar and showing the
	 * TextView with the message about the hungry troll
	 */
	public void trollEatedSomePoints()
	{
		verticalSeekBarPoints.setProgress(controller.getCompletionPercentage());
		verticalSeekBarPoints.invalidate();

		textViewTroll.startAnimation(animationTextViewTroll);
	}

	/**
	 * Called when the dialog with the answer provided by the user is closed 
	 * (both automatically or by the user)
	 */
	@Override
	public void onCloseStressorAnswerDialog(DialogFragment dialog) 
	{
		dialog.dismiss();
		
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		executor.allowCoreThreadTimeOut(false);
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						flipperLayout.showPrevious();
						resetWidgets();
						firstStep(false);
					}
				});
			}
		}, 200, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		if (recordTouchData)
		{
			TouchData data = new TouchData(ev);
			MainActivity.mLoggers.writeOnTouchLogger(data.toString());
		}
		return super.dispatchTouchEvent(ev);
	}
	
	/**
	 * Overriding the onBackPressed method to avoid the Activity closes 
	 * when the user clicks on it
	 */
	@Override
	public void onBackPressed()
	{
		return;
	}
}
