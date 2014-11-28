package ch.qol.unige.smartphonetest;

import java.io.File;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.qol.unige.smartphonetest.R;
import ch.qol.unige.network.ConnectionManager;
import ch.qol.unige.smartphonetest.dataloggers.Loggers;
import ch.qol.unige.smartphonetest.listeners.TouchData;
import ch.qol.unige.smartphonetest.relax.Relax;
import ch.qol.unige.smartphonetest.searchtask.SearchTaskView;
import ch.qol.unige.smartphonetest.stressor.StressorView;
import ch.qol.unige.smartphonetest.writetask.WritingTaskView;
import ch.qol.unige.survey.Survey;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity 
	implements Survey.SurveyDialogResultInterface,
		AsyncFileSender.AsyncFileSenderInterface, 
		DialogChooseNameAndAvatar.DialogChooseNameAndAvatarInterface, 
		DialogFinalRank.DialogFinalRankInterface 
{	
	private final static String LOG_STRING = "MAIN_ACTIVITY";
	private static final int ACTIVITY_EXECUTION_CODE = 1;
	
	private static ArrayList<ArrayList<StepSettings>> allExercisesToPerform = null;
	private static ArrayList<StepSettings> currentExercise = null;
	private static ArrayList<StepSettings> exerciseOnlineForward = null;
	private static ArrayList<StepSettings> exerciseOnlineBackward = null;
	private static int currentIndexOfTask = -1;
	private static int currentIndexOfExercise = -1;
	private static boolean onlineGame = false;
	
	public static Loggers mLoggers = null;
	
	private ConnectionManager mConnectionManager = null;
	
	private ProgressDialog dialogUploadData = null;
	private ProgressDialog dialogWaitingToStart = null;
	private ProgressDialog dialogWaitingSecondStep = null;
	private ProgressDialog dialogPauseBetweenTwoExercises = null;
	private ProgressDialog dialogWaitingFinalRank = null;
	
	private boolean gameInProgress = false;
	private boolean gameCompleted = false;
	
	private String name = null;
	private String avatar = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_main);
		
		mLoggers = new Loggers(this);
		
		smartphoneSettingsAndSpecsLog();
		
		/**
		 * Creating the set of exercises, in particular: 
		 * 1) Relax - Search - Write - Stressor - Search - Write
		 * 2) Relax - Search - Write - Stressor - Search (Stress) - Write (Stress)
		 * 3) Relax - Search - Write - Stressor (Relaxed) - Search (Stress) - Write (Stress)
		 * For the online game we use a different set of exercises
		 * 1) Relax - Search - Write - STOP - Stressor - Search (Stress) - Write (Stress) 
		 */
		setupExercises();
		
		//exercises = exercisesMovingForward;
		
		/*Intent intent = new Intent(this, StressorView.class);
		intent.putExtra(StepSettings.STRESS, false);
		intent.putExtra(StepSettings.REPETITIONS, 3);
		intent.putExtra(StepSettings.MINUTE_DURATION, -1);
		startActivityForResult(intent, ACTIVITY_EXECUTION_CODE);
		
		/*Survey dialogSurvey = new Survey();
		dialogSurvey.show(getFragmentManager(), null);*/
		
		//currentIndexOfTask = 10;
		
		//finalRankIsArrived(1);
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case (R.id.action_data) :
			{
				sendRecoveredData();
				return true;
			}
			default:
			{
				return super.onOptionsItemSelected(item);
			}
		}
	}
	
	private void sendRecoveredData()
	{
		File dirFiles = getFilesDir();
		ArrayList<File> files = new ArrayList<>();
		for (String fileName: dirFiles.list())
		{
			Log.d(LOG_STRING, fileName);
			File file = new File(getFilesDir(), fileName);
			if (file.exists() && file.isFile())
			{
				files.add(file);
			}
		}
		
		File[] filesArray = new File[files.size()];
		files.toArray(filesArray);
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				dialogUploadData = new ProgressDialog(MainActivity.this);
				dialogUploadData.setTitle(R.string.uploading);
				dialogUploadData.setMax(100); dialogUploadData.setProgress(0);
				dialogUploadData.setIndeterminate(false);
				dialogUploadData.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				dialogUploadData.setCancelable(false);
				dialogUploadData.setCanceledOnTouchOutside(false);
				dialogUploadData.show();
			}
		});
		
		AsyncFileSender sender = new AsyncFileSender(this);
		sender.execute(filesArray);
	}
	
	/**
	 * Overriding the onBackPressed function to avoid that the user exits
	 * from the application
	 */
	@Override
	public void onBackPressed()
	{
		return;
	}
	
	protected void onResume()
	{
		Log.d(LOG_STRING, "onResume");
		super.onResume();
	}
	
	protected void onRestart()
	{
		Log.d(LOG_STRING, "onRestart");
		super.onRestart();
	}
	
	/**
	 * Creates the two list of possible set exercises, one RELAX -> TEST -> STRESS -> TEST
	 * and another one STRESS -> TEST -> RELAX -> TEST
	 */
	private void setupExercises()
	{	
		
		allExercisesToPerform = new ArrayList<ArrayList<StepSettings>>();
		
		ArrayList<StepSettings> firstSet = new ArrayList<StepSettings>();
		// First step is the survey
		firstSet.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 1));
		// We relax the user for 5 minutes
		firstSet.add(new StepSettings(StepSettings.ExerciseType.RELAX, 2, 5));
		// Survey for the base tasks without stress
		firstSet.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 3));
		// Search + writing task
		firstSet.add(new StepSettings(StepSettings.ExerciseType.SEARCH, 4, 
				false, 3, -1));
		firstSet.add(new StepSettings(StepSettings.ExerciseType.WRITE, 5, 
				false, 3, -1));
		// Survey before the beginning of the stressor
		firstSet.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 6));
		// Stressor
		firstSet.add(new StepSettings(StepSettings.ExerciseType.STRESSOR, 7, 
				true, -1, 5));
		// Survey to test if user is effectively stressed
		firstSet.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 8));
		// The two tasks in stressed condition (without timing)
		firstSet.add(new StepSettings(StepSettings.ExerciseType.SEARCH, 9, 
				true, 3, -1));
		firstSet.add(new StepSettings(StepSettings.ExerciseType.WRITE, 10, 
				true, 3, -1));
		// Final survey
		firstSet.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 11));
		
		allExercisesToPerform.add(firstSet);
		
		ArrayList<StepSettings> secondSet = new ArrayList<StepSettings>();
		// Survey
		secondSet.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 1));
		// Relax
		secondSet.add(new StepSettings(StepSettings.ExerciseType.RELAX, 2, 3));
		// Survey
		secondSet.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 3));
		// Search + writing
		secondSet.add(new StepSettings(StepSettings.ExerciseType.SEARCH, 4, 
				false, 3, -1));
		secondSet.add(new StepSettings(StepSettings.ExerciseType.WRITE, 5, 
				false, 3, -1));
		// Survey
		secondSet.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 6));
		// Stressor
		secondSet.add(new StepSettings(StepSettings.ExerciseType.STRESSOR, 7, 
				true, -1, 3));
		// Survey
		secondSet.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 8));
		// Search + writing (timing)
		secondSet.add(new StepSettings(StepSettings.ExerciseType.SEARCH, 9, 
				true, 3, -1));
		secondSet.add(new StepSettings(StepSettings.ExerciseType.WRITE, 10, 
				true, 3, -1));
		// Survey
		secondSet.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 11));
		
		//allExercisesToPerform.add(secondSet);
		
		ArrayList<StepSettings> thirdSet = new ArrayList<StepSettings>();
		// Survey
		thirdSet.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 1));
		// Relax
		thirdSet.add(new StepSettings(StepSettings.ExerciseType.RELAX, 2, 3));
		// Survey
		thirdSet.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 3));
		// Search + write
		thirdSet.add(new StepSettings(StepSettings.ExerciseType.SEARCH, 4, false, 
				3, -1));
		thirdSet.add(new StepSettings(StepSettings.ExerciseType.WRITE, 5, false, 
				3, -1));
		// Survey
		thirdSet.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 6));
		// Stressor (relaxed)
		thirdSet.add(new StepSettings(StepSettings.ExerciseType.STRESSOR, 7, false, 
				-1, 3));
		// search + write (stress)
		thirdSet.add(new StepSettings(StepSettings.ExerciseType.SEARCH, 8, true, 
				3, -1));
		thirdSet.add(new StepSettings(StepSettings.ExerciseType.WRITE, 9, true, 
				3, -1));
		// Survey 
		thirdSet.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 10));
		
		//allExercisesToPerform.add(thirdSet);
		
		exerciseOnlineForward = new ArrayList<>();
		// Survey
		exerciseOnlineForward.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 1));
		// Relax 
		exerciseOnlineForward.add(new StepSettings(StepSettings.ExerciseType.RELAX, 2, 3));
		// Survey
		exerciseOnlineForward.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 3));
		// search + write
		exerciseOnlineForward.add(new StepSettings(StepSettings.ExerciseType.SEARCH, 4, 
				false, 3, -1));
		exerciseOnlineForward.add(new StepSettings(StepSettings.ExerciseType.WRITE, 5, 
				false, 3, -1));
		// Survey
		exerciseOnlineForward.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 6));
		// Wait to proceed for other players
		exerciseOnlineForward.add(new StepSettings(StepSettings.ExerciseType.WAIT_SECOND_STEP, 7));
		// Stressor
		exerciseOnlineForward.add(new StepSettings(StepSettings.ExerciseType.STRESSOR, 8, 
				true, -1, 3));
		// Survey
		exerciseOnlineForward.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 9));
		// Search + write (stress)
		exerciseOnlineForward.add(new StepSettings(StepSettings.ExerciseType.SEARCH, 10, 
				true, 3, -1));
		exerciseOnlineForward.add(new StepSettings(StepSettings.ExerciseType.WRITE, 11, 
				true, 3, -1));
		// Survey
		exerciseOnlineForward.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 12));
		
		exerciseOnlineBackward = new ArrayList<>();
		// Survey
		exerciseOnlineBackward.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 1));
		// Stressor
		exerciseOnlineBackward.add(new StepSettings(StepSettings.ExerciseType.STRESSOR, 
				2, true, -1, 5));
		// Survey
		exerciseOnlineBackward.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 3));
		// search + write stress
		exerciseOnlineBackward.add(new StepSettings(StepSettings.ExerciseType.SEARCH, 
				4, true, 3, -1));
		exerciseOnlineBackward.add(new StepSettings(StepSettings.ExerciseType.WRITE, 
				5, true, 3, -1));
		// Survey
		exerciseOnlineBackward.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 
				6));
		exerciseOnlineBackward.add(new StepSettings(StepSettings.ExerciseType.WAIT_SECOND_STEP, 7));
		// Relax
		exerciseOnlineBackward.add(new StepSettings(StepSettings.ExerciseType.RELAX, 8, 5));
		// Survey
		exerciseOnlineBackward.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 9));
		// search + write relax
		exerciseOnlineBackward.add(new StepSettings(StepSettings.ExerciseType.SEARCH, 
				10, false, 3, -1));
		exerciseOnlineBackward.add(new StepSettings(StepSettings.ExerciseType.WRITE, 
				11, false, 3, -1));
		// Survey
		exerciseOnlineBackward.add(new StepSettings(StepSettings.ExerciseType.SURVEY, 12));
	}
	
	/**
	 * Writes inside the log file preliminary info about the smartphone used 
	 * (Manufacturer and Model) and about the screen dimensions
	 */
	private void smartphoneSettingsAndSpecsLog()
	{
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		DateFormat format = DateFormat.getDateTimeInstance(DateFormat.FULL, 
				DateFormat.FULL);
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		String toWrite = "/*********************************/" + 
				System.getProperty("line.separator") + 
				" * Date: " + format.format(new Date()) + " *" + 
				System.getProperty("line.separator") +
				" * Manufacturer + Model: " + Build.MANUFACTURER + " " + 
					Build.MODEL + " *" + 
				System.getProperty("line.separator") +
				" * Screen Pixels WxH: " + metrics.widthPixels + "x" + 
					metrics.heightPixels + " *" +
				System.getProperty("line.separator") + 
				" * Language: " + Locale.getDefault().getLanguage() + " *" +
				System.getProperty("line.separator") +
				"/*********************************/";
		
		mLoggers.writeOnSettingsLogger(toWrite);
	}
	
	/**
	 * Returns the name chosen by the player. Used when reconnecting to the 
	 * WebServer
	 * @return the name of the player
	 */
	public String getNameOfPlayer()
	{
		return this.name;
	}
	
	/**
	 * Method called when the user click on the button to start the offline 
	 * game. It first randomize the set of exercises and start the game
	 * @param view
	 */
	public void startOfflineGame(View view)
	{
		onlineGame = false;
		if (allExercisesToPerform.size() != 1)
		{
			Collections.shuffle(allExercisesToPerform, new Random(System.nanoTime()));
		}
		currentIndexOfExercise = 0;
		currentExercise = allExercisesToPerform.get(0);
		currentIndexOfTask = -1; 
		gameInProgress = true;
		launchNewStepInsideAnExercise();
	}
	
	/**
	 * Method called when the user clicks on the button to start the online
	 * game, it creates a new ConnectionManager object and starts a new 
	 * WebSocket connection
	 * @param view: the clicked button
	 */
	public void startOnlineGame(View view)
	{
		currentExercise = exerciseOnlineForward;
		onlineGame = true;
		try {
			mConnectionManager = new ConnectionManager(this, false);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		//connectionToWebsocketCompletedOpenDialogNameAvatar();
	}
	
	/**
	 * Called if the connection to the websocket server is fine. 
	 * The second step is to show the dialog where the user has to choose
	 * the name and the avatar for the game
	 */
	public void onConnectionToWebsocketCompletedOpenDialogNameAvatar()
	{
		DialogChooseNameAndAvatar dialog = new DialogChooseNameAndAvatar();
		dialog.show(getFragmentManager(), null);
	}
	
	public void responseAvatarAndName(boolean correct)
	{
		if (!correct)
		{
			Toast.makeText(this, R.string.nickname_used, Toast.LENGTH_SHORT).show();
			dialogWaitingToStart.dismiss(); dialogWaitingToStart = null;
			DialogChooseNameAndAvatar dialog = new DialogChooseNameAndAvatar();
			dialog.show(getFragmentManager(), null);
		}
	}
	
	/**
	 * Launches a new step. It retrieves its settings, writes it on the log files
	 * and starts the correct activity
	 */
	private void launchNewStepInsideAnExercise()
	{	
		currentIndexOfTask++;
		
		if (currentIndexOfTask == 0)
		{
			writeProtocolDescription(currentExercise);
		}
		
		if (currentIndexOfTask < currentExercise.size())
		{
			StepSettings newStep = currentExercise.get(currentIndexOfTask);
			
			mLoggers.writeNewStep(newStep.toString());
			
			Intent intent = null;
			switch(newStep.getExerciseType()) {
			
				case SURVEY: 
				{
					Survey dialogSurvey = new Survey();
					dialogSurvey.show(getFragmentManager(), null);
					break;
				}
				case WAIT_SECOND_STEP:
				{
					// Send the message to the server of first tasks completed
					// and put in waiting
					mConnectionManager.sendMessageFirstStepCompleted();
					waitForSecondStep();
					break;
				}
				case RELAX: 
				{
					intent = new Intent(this, Relax.class);
					intent.putExtra(StepSettings.MINUTE_DURATION, newStep.getMinuteDuration());
					startActivityForResult(intent, ACTIVITY_EXECUTION_CODE);
					break;
				}
				case SEARCH:
				{
					intent = new Intent(this, SearchTaskView.class);
					intent.putExtra(StepSettings.STRESS, newStep.getStress());
					intent.putExtra(StepSettings.REPETITIONS, newStep.getRepetions());
					intent.putExtra(StepSettings.MINUTE_DURATION, newStep.getMinuteDuration());
					startActivityForResult(intent, ACTIVITY_EXECUTION_CODE);
					break;
				}
				case WRITE: 
				{
					intent = new Intent(this, WritingTaskView.class);
					intent.putExtra(StepSettings.STRESS, newStep.getStress());
					intent.putExtra(StepSettings.REPETITIONS, newStep.getRepetions());
					intent.putExtra(StepSettings.MINUTE_DURATION, newStep.getMinuteDuration());
					startActivityForResult(intent, ACTIVITY_EXECUTION_CODE);
					break;
				}
				case STRESSOR:
				{
					intent = new Intent(this, StressorView.class);
					intent.putExtra(StepSettings.STRESS, newStep.getStress());
					intent.putExtra(StepSettings.REPETITIONS, newStep.getRepetions());
					intent.putExtra(StepSettings.MINUTE_DURATION, newStep.getMinuteDuration());
					startActivityForResult(intent, ACTIVITY_EXECUTION_CODE);
					break;
				}
				default:
				{
					Log.d(LOG_STRING, "Exercise type not correct");
				}
			}
		}
		else 
		{
			setOfExercisesCompleted();
		}	
	}
	
	private void writeProtocolDescription(ArrayList<StepSettings> list) 
	{
		String toWrite = "** PROTOCOL: ";
		for (StepSettings step: list)
		{
			toWrite += step.toStringBasic() + " - ";
		}
		toWrite += " **";
		mLoggers.writeOnSettingsLogger(toWrite);
		mLoggers.writeNewStep(toWrite);
	}

	private void setOfExercisesCompleted()
	{
		/**
		 * If we are performing the online game or we have completed all the
		 * offline exercises we have completed everything and we can move to
		 * the final greetings 
		 */
		if (onlineGame ||
				!onlineGame && currentIndexOfExercise == allExercisesToPerform.size() -1)
		{
			mLoggers.closeFiles();
			mLoggers.makeFilesVisible();
			
			if (onlineGame)
			{
				mConnectionManager.sendMessageEverythingCompleted();
				
				dialogWaitingFinalRank = new ProgressDialog(this);
				dialogWaitingFinalRank.setTitle(R.string.wait);
				dialogWaitingFinalRank.setMessage(getResources().getString(R.string.wait_final_results));
				dialogWaitingFinalRank.setIndeterminate(true);
				dialogWaitingFinalRank.setCancelable(false);
				dialogWaitingFinalRank.setCanceledOnTouchOutside(false);
				dialogWaitingFinalRank.show();
			}
			else
			{
				showFinalGreetings();
			}
		}
		else
		{
			/** WAIT ONE MINUTE BETWEEN ONE EXERCISE AND THE NEXT ONE */
			currentIndexOfExercise++;
			currentExercise = allExercisesToPerform.get(currentIndexOfExercise);
			currentIndexOfTask = -1;
			
			dialogPauseBetweenTwoExercises = new ProgressDialog(this);
			dialogPauseBetweenTwoExercises.setTitle(R.string.pause);
			dialogPauseBetweenTwoExercises.setMessage(getResources().getString(R.string.break_one_minute));
			dialogPauseBetweenTwoExercises.setIndeterminate(true);
			dialogPauseBetweenTwoExercises.setCancelable(false);
			dialogPauseBetweenTwoExercises.setCanceledOnTouchOutside(false);
			
			final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
			executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
			executor.allowCoreThreadTimeOut(false);
			executor.schedule(new Runnable() {
				public void run() 
				{
					executor.shutdown();
					MainActivity.this.dialogPauseBetweenTwoExercises.dismiss();
					MainActivity.this.dialogPauseBetweenTwoExercises = null;
					MainActivity.this.launchNewStepInsideAnExercise();
				}
			}, 1, TimeUnit.MINUTES);
			
			dialogPauseBetweenTwoExercises.show();
		}
	}
	
	/**
	 * Called whenever an exercise is completed and a new activity has to start
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == ACTIVITY_EXECUTION_CODE) 
		{
			if (resultCode == RESULT_OK)
			{
				Log.d(LOG_STRING, "activity completed");
				launchNewStepInsideAnExercise();
			}
		}
	}
	
	/**
	 * Called whenever the user has completed the survey. The answers provided
	 * are stored and a new activity is started
	 */
	@Override
	public void onCloseSurveyDialogWithAnswer(DialogFragment dialog, String valence, 
			String energy, String stress) 
	{
		dialog.dismiss();
		
		/**
		 * Writes the answer of the survey to the file
		 */
		String[] values = {"1", "1", valence, energy, stress};
		writeResumeOfExercise(values);
		
		launchNewStepInsideAnExercise();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		TouchData data = new TouchData(ev);
		mLoggers.writeOnTouchLogger(data.toString());
		return super.dispatchTouchEvent(ev);
	}
	
	/**
	 * Recursive function to explore all the tree created by a ViewGroup to 
	 * write layout info about all his child
	 * @param layout
	 */
	public static void followTreeLayoutToSaveElements(ViewGroup layout)
	{
		for (int i = 0; i < layout.getChildCount(); i++)
		{
			View child = layout.getChildAt(i);
			if (!(child instanceof ViewGroup))
			{
				MainActivity.mLoggers.writeOnLayoutLogger(
						MainActivity.createStringForViewElement(child));
			}
			else if (child instanceof NumberPicker)
			{
				MainActivity.mLoggers.writeOnLayoutLogger(
						MainActivity.createStringForViewElement((View)child));
			}
			else if (child instanceof ScrollView)
			{
				MainActivity.mLoggers.writeOnLayoutLogger(
						MainActivity.createStringForViewElement(child));
				MainActivity.followTreeLayoutToSaveElements((ViewGroup) child); 
			}
			else 
			{
				MainActivity.followTreeLayoutToSaveElements((ViewGroup) child); 
			}
		}
	}
	
	/**
	 * Given a view element, builds a string to write on the logger to store
	 * position (x and y), width, visibility and eventually the text
	 * @param view the view to build the string
	 * @return the information string
	 */
	public static String createStringForViewElement(View view)
	{
		int[] location = new int[2];
		//view.getLocationOnScreen(location);
		view.getLocationInWindow(location);
		String text = ",null";
		if (view instanceof TextView)
		{
			text = "," + (((TextView) view).getText().toString().replace(",", ";"));
		}
		
		return "(" + view.getId() + "," + view.getClass().toString() + "," + 
				location[0] + "," + location[1] + "," + view.getWidth() + "," +
				view.getHeight() + "," + view.getVisibility() + text + ")";
	}
	
	/**
	 * Writes a resume specific for a particular exercise: 
	 * TYPE, REPETITION, SUB_REPETITION (for stressor), EVALUATION VALUES
	 * @param exerciseReport the evaluation values specific for each task
	 */
	public static void writeResumeOfExercise(String[] exerciseReport)
	{
		if (currentIndexOfTask >= 0 && currentIndexOfTask < currentExercise.size())
		{
			String toWrite = "(" + currentExercise.get(currentIndexOfTask).getExerciseNumber() 
					+ "," + currentExercise.get(currentIndexOfTask).toStringBasic() 
					+ "," + TextUtils.join(",", exerciseReport) + ")";
			mLoggers.writeOnSettingsLogger(toWrite);
		}
	}
	
	/**
	 * Shows the final greetings to thank the user
	 */
	private void showFinalGreetings()
	{
		setContentView(R.layout.layout_greetings);
	}
	
	/**
	 * Method called to retrieve all the files created by the application and 
	 * to send it to the server that has to store this data
	 * @param view The clicked button
	 */
	public void sendDataToServer(View view)
	{
		NetworkInfo info = (NetworkInfo) 
				((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
					.getActiveNetworkInfo();
		
		if (info == null || !info.isConnected())
		{
			Toast.makeText(this, R.string.no_network, Toast.LENGTH_LONG).show();
			return;
		}
		else {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					dialogUploadData = new ProgressDialog(MainActivity.this);
					dialogUploadData.setTitle(R.string.uploading);
					dialogUploadData.setMax(100); dialogUploadData.setProgress(0);
					dialogUploadData.setIndeterminate(false);
					dialogUploadData.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					dialogUploadData.setCancelable(false);
					dialogUploadData.setCanceledOnTouchOutside(false);
					dialogUploadData.show();
				}
			});
			AsyncFileSender sender = new AsyncFileSender(this);
			sender.execute(new File[]{mLoggers.getFileLayoutLogger(), 
					mLoggers.getFileRotationSensorLogger(),
					mLoggers.getFileSettingsLogger(), 
					mLoggers.getFileTouchLogger()});
		}
	}
	
	/**
	 * The game is completed, it closes the application
	 * @param view The clicked button
	 */
	public void closeApplication(View view)
	{
		mLoggers.closeFiles();
		mLoggers.makeFilesVisible();
		this.finish();
	}
	
	/**
	 * First set of exercises completed, the user decides to restart from 
	 * the beginning
	 * @param view the button clicked
	 */
	public void restartFromBeginning(View view)
	{
		findViewById(R.id.buttonExit).setEnabled(false);
		findViewById(R.id.buttonRestart).setEnabled(false);
		
		mLoggers.resetLoggers();
		mLoggers = new Loggers(this);
		
		setContentView(R.layout.activity_main);
	}
	
	@Override
	protected void onStop()
	{
		mLoggers.makeFilesVisible();
		super.onStop();
	}

	/**
	 * Shows a Toast once all the files are uploaded on the server
	 */
	@Override
	public void sentFileCompleted(boolean allFilesSent) 
	{
		if (allFilesSent)
		{
			dialogUploadData.dismiss();
			Toast.makeText(this, R.string.thanks, Toast.LENGTH_LONG).show();
			if (findViewById(R.id.buttonExit) != null)
			{
				findViewById(R.id.buttonExit).setEnabled(true);
				findViewById(R.id.buttonRestart).setEnabled(true);
			}
		}
		else
		{
			Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Method used to update the value of the progress bar of the progress dialog
	 * while uploading the file on the server
	 * @param percentageValue the value of the progress bar to show
	 */
	@Override
	public void updatePercentage(int percentageValue) 
	{
		dialogUploadData.setProgress(percentageValue);
	}

	/**
	 * Called when the Dialog to choose the name and the avatar is closed and 
	 * we have to send it to the Server
	 */
	@Override
	public void setupPlayerSettingsCompleted(DialogFragment dialog,
			String name, String avatar) 
	{
		if (dialog != null)
		{
			dialog.dismiss();
		}
		this.name = name; this.avatar = avatar;
		/**
		 * Sending data to the webserver and open dialog waiting for start
		 */
		mConnectionManager.sendMessageNameAndAvatar(name, avatar);
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				dialogWaitingToStart = 
						new ProgressDialog(MainActivity.this);
				dialogWaitingToStart.setTitle(R.string.waiting);
				dialogWaitingToStart.setMessage(getResources().getString(R.string.waiting_to_start));
				dialogWaitingToStart.setIndeterminate(true);
				dialogWaitingToStart.setCancelable(false);
				dialogWaitingToStart.setCanceledOnTouchOutside(false);
				
				dialogWaitingToStart.show();
			}
		});
	}
	
	/**
	 * Method called once the first step of the game and has been completed, and 
	 * the user has to wait that all the players complete the first set of 
	 * tasks
	 */
	public void waitForSecondStep()
	{
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				dialogWaitingSecondStep = new ProgressDialog(MainActivity.this);
				dialogWaitingSecondStep.setTitle(R.string.waiting);
				dialogWaitingSecondStep.setMessage(getResources().getString(R.string.wait_next_step));
				dialogWaitingSecondStep.setIndeterminate(true);
				dialogWaitingSecondStep.setCancelable(false);
				dialogWaitingSecondStep.setCanceledOnTouchOutside(false);
				dialogWaitingSecondStep.show();
			}
		});
	}
	
	/**
	 * Method called when the server sends the command to start the second set
	 * of tasks. Simply close the waiting dialog and proceed with the list of
	 * tasks
	 */
	public void startSecondSetOfTaskExercises()
	{
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				dialogWaitingSecondStep.dismiss();
				dialogWaitingSecondStep = null;
				
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle(R.string.message);
				builder.setMessage(R.string.watch_screen);
				builder.setCancelable(false);
				builder.setPositiveButton(R.string.start_battle, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						launchNewStepInsideAnExercise();
					}
				});
				
				AlertDialog dialog = builder.create();
				dialog.setCanceledOnTouchOutside(false);
				dialog.show();
			}
		});
	}
	
	/**
	 * Once all the clients are connected and the server sends the command, 
	 * it's time to start the different tasks
	 */
	public void commandToStartFirstStepArrived()
	{
		gameInProgress = true;
		dialogWaitingToStart.dismiss();
		launchNewStepInsideAnExercise();
	}
	
	/**
	 * The client has not been selected to be part of the game, shows a dialog
	 * with the appropriate message
	 */
	public void showDialogNotThisTime()
	{
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				new AlertDialog.Builder(MainActivity.this).
					setTitle(R.string.next_time)
					.setMessage(R.string.not_selected_game)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) 
						{
							dialog.dismiss();	
						}
					})
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show();
			}
		});
	}
	
	/**
	 * The ConnectionManager has received the packet with the final rank of the 
	 * player, so close the dialogWaitingFinalRank and show the DialogFinalRank
	 * with the final position
	 * @param finalPosition: the final position of the player
	 */
	public void finalRankIsArrived(int finalPosition)
	{
		if (dialogWaitingFinalRank != null)
		{
			dialogWaitingFinalRank.dismiss();
			dialogWaitingFinalRank = null;
		}
		DialogFinalRank dialog = new DialogFinalRank(finalPosition);
		dialog.show(getFragmentManager(), null);
		gameInProgress = false;
		gameCompleted = true;
		mConnectionManager.close();
	}

	@Override
	public void finalRankDialogClosed(DialogFragment dialog) 
	{
		if (dialog != null)
		{
			dialog.dismiss();
		}
		showFinalGreetings();
	}
	
	/**
	 * Method called if the connection is closed or an error occurred
	 * during connection setup
	 */
	public void websocketConnectionClosed()
	{
		ch.qol.unige.smartphonetest.baseMVC.View.setConnectionManager(null);
		if (!gameCompleted)
		{
			if (!gameInProgress)
			{
				errorConnectingToWebSocket();
			}
			else
			{
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() 
					{
						Toast.makeText(MainActivity.this, R.string.connection_closed, 
								Toast.LENGTH_SHORT).show();
						try 
						{
							mConnectionManager = new ConnectionManager(MainActivity.this, true);
						} catch (URISyntaxException e) 
						{
							e.printStackTrace();
						}
					}
				});
			}
		}
	}
	
	/**
	 * Shows a Dialog to ask to the user if he wants to retry to connect to the 
	 * or wants to discard the request
	 */
	private void errorConnectingToWebSocket()
	{
		ch.qol.unige.smartphonetest.baseMVC.View.setConnectionManager(null);
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() 
			{
				AlertDialog.Builder dialogConnectionNotWorkingBuilder = 
						new AlertDialog.Builder(MainActivity.this);
				dialogConnectionNotWorkingBuilder.setTitle(R.string.error);
				dialogConnectionNotWorkingBuilder.setMessage(R.string.network_not_working);
				dialogConnectionNotWorkingBuilder.setPositiveButton(R.string.retry, 
					new OnClickListener() 
					{
					
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Calling again the method to start a new websocket connection
							MainActivity.this.startOnlineGame(null);
						}
					}
				);
				dialogConnectionNotWorkingBuilder.setNegativeButton(R.string.discard, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						if (dialogWaitingToStart != null)
						{
							dialogWaitingToStart.dismiss(); 
							dialogWaitingToStart = null;
						}
					}
				});
				AlertDialog dialogConnectionNotWorking = dialogConnectionNotWorkingBuilder.create();
				dialogConnectionNotWorking.setCancelable(false);
				dialogConnectionNotWorking.setCanceledOnTouchOutside(false);
				dialogConnectionNotWorking.show();
				
			}
		});
	}
}
