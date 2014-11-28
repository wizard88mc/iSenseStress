package ch.qol.unige.smartphonetest.writetask;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import ch.qol.unige.smartphonetest.R;
import ch.qol.unige.smartphonetest.MainActivity;
import ch.qol.unige.smartphonetest.listeners.TouchData;
import ch.qol.unige.smartphonetest.writetask.customkeyboard.CustomKeyboard;

public class WritingTaskView extends ch.qol.unige.smartphonetest.baseMVC.View 
	implements //TextView.OnEditorActionListener, 
		ReadyWritingTaskDialog.ReadyWritingTaskDialogListener, 
		ResumeWritingTaskDialog.ResumeWritingTaskDialogInterface {
	
	private TextView textViewToWrite = null;
	private EditText editText = null;
	
	private WritingTaskController controller = null;
	
	private CustomKeyboard mCustomKeyboard = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.writing_task_layout);
		
		progressBar = (ProgressBar) findViewById(R.id.progressTimingBar);
		textViewToWrite = (TextView) findViewById(R.id.textViewParagraph);
		editText = (EditText) findViewById(R.id.editTextSubmitted);
		
		controller = new WritingTaskController(this);
		
		mCustomKeyboard = new CustomKeyboard(this, R.id.keyboardView, controller);
		mCustomKeyboard.registerEditText(R.id.editTextSubmitted);
		
		stress = controller.stress();
		
		if (!stress)
		{
			((LinearLayout) findViewById(R.id.layoutProgressBar)).setVisibility(View.GONE);
		}
		
		super.manageRotationSensor();
		
		openDialogReady();
		startANewExerciseOrDie(true);
	}
	
	/**
	 * Depending on the number of repetition or if the time is expired, starts
	 * a new exercise
	 */
	private void startANewExerciseOrDie(boolean firstTime)
	{
		if (controller.getContinuePlaying())
		{
			mCustomKeyboard.showCustomKeyboard(null);
			
			saveTaskElementsSizeAndPosition();
			
			setupExercise();
			if (!firstTime && stress)
			{		
				startAudioVideoStressors();
				controller.exerciseStarted();
			}
		}
		else
		{
			this.everythingCompletedGoBackToMainActivity();
		}
	}
	
	/**
	 * Sets up the exercise, retrieving the text to write and showing it
	 */
	private void setupExercise()
	{	
		// Asking to the controller for the exercise to instantiate
		textViewToWrite.setText(controller.instantiateExercise());
		((ScrollView) findViewById(R.id.scrollWrite)).setScrollY(0);
	}
	
	/**
	 * Opens the dialog to prepare the user to start, waiting from him to start
	 */
	private void openDialogReady()
	{
		ReadyWritingTaskDialog dialog = new ReadyWritingTaskDialog(stress);
		dialog.show(getFragmentManager(), null);
	}
	
	/**
	 * Takes the text the user has to write and instantiate the TextView.
	 * 
	 * @param textToWrite: the text to write
	 * @param mixFonts: if inside the text it is necessary to mix fonts, size etc.
	 */
	public void setTextToWrite(String textToWrite, boolean mixFonts)
	{
		if (mixFonts)
		{
			// Make some changes to the text
		}
		textViewToWrite.setText(textToWrite);
	}

	/**
	 * Called when the user clicks on the submit button or when the time expires
	 */
	@Override
	public void submitAnswer(final View view) {
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run()
			{
				if (view != null)
				{
					controller.stopExercise();
				}
				String writtenText = editText.getText().toString();
				((WritingTaskController) controller).submittedText(writtenText);
			}
		});
	}

	/**
	 * Called when the presentation dialog is closed. It has to start the exercise
	 */
	@Override
	public void onCloseDialogPresentation(DialogFragment dialog) 
	{
		dialog.dismiss();
		
		if (stress)
		{
			startAudioVideoStressors();
			controller.exerciseStarted();
		}
	}
	
	/**
	 * Called when the exercise is completed, to show a small resume about user
	 * performances
	 * @param totalWords the total words to write
	 * @param correctWords the number of correct words
	 * @param wrongWords the number of wrong written words
	 */
	public void showResumeDialog(int totalWords, int correctWords, 
			int wrongWords) 
	{	
		
		if (mConnectionManager != null)
		{
			mConnectionManager.sendMessageWritingTaskFinalResult(stress, 
					correctWords, wrongWords);
		}
		
		ResumeWritingTaskDialog dialog = new ResumeWritingTaskDialog(totalWords, 
				correctWords, wrongWords, this.stress, this);
		dialog.show(getFragmentManager(), null);
	}
	
	@Override
	protected void resetWidgets()
	{
		super.resetWidgets();
		textViewToWrite.setText("");
		editText.setText("");
	}

	/**
	 * Called when the resume dialog is closed to dismiss it and decide how to
	 * continue
	 */
	@Override
	public void onCloseDialogResumeWritingTask(DialogFragment dialog) 
	{
		dialog.dismiss();
	
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		executor.allowCoreThreadTimeOut(false);
		executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		executor.schedule(new Runnable() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						resetWidgets();
						startANewExerciseOrDie(false);
					}
				});
			}
		}, 200, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Takes a MotionEvent and saves the relevant data
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) 
	{	
		boolean result = super.dispatchTouchEvent(event);
		
		TouchData data = new TouchData(event);
		MainActivity.mLoggers.writeOnTouchLogger(data.toString());
		
		if (data.getAction() == 1)
		{
			mCustomKeyboard.writeDigit();
		}
		return result;
	}
	
	/**
	 * When the BACK is pressed on the UI, we hide the custom keyboard if 
	 * it was Visible
	 */
	@Override
	public void onBackPressed()
	{
		if (mCustomKeyboard.isCustomKeyboardVisible())
		{
			mCustomKeyboard.hideCustomKeyboard();
		}
	}
	
	/**
	 * Method used to notify the view that the BACK Button has been clicked and
	 * if we are performing a real-time game we use the ConnectionManager to 
	 * notify the server
	 */
	public void backButtonClicked()
	{
		if (mConnectionManager != null)
		{
			mConnectionManager.sendMessageBackButtonClickedWritingTask(stress);
		}
	}
	
	@Override
	protected void saveTaskElementsSizeAndPosition()
	{
		final ViewGroup parentLayout = (ViewGroup) findViewById(R.id.parentLayout);
		
		ViewTreeObserver vto = parentLayout.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				parentLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				MainActivity.followTreeLayoutToSaveElements(parentLayout);
				mCustomKeyboard.writeKeyboardLayoutSpect();
			}
		});
	}
}
