package ch.qol.unige.smartphonetest.searchtask;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.qol.unige.smartphonetest.R;
import ch.qol.unige.smartphonetest.MainActivity;
import ch.qol.unige.smartphonetest.listeners.TouchData;
import ch.qol.unige.smartphonetest.searchtask.dialogs.ListIconSearchDialog;
import ch.qol.unige.smartphonetest.searchtask.dialogs.ResumeDialog;
import ch.qol.unige.smartphonetest.searchtask.dialogs.ListIconSearchDialog.PresentationDialogListener;
import ch.qol.unige.smartphonetest.searchtask.dialogs.ResumeDialog.ResumeDialogListener;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.ProgressBar;

public class SearchTaskView extends ch.qol.unige.smartphonetest.baseMVC.View 
	implements View.OnClickListener, PresentationDialogListener, ResumeDialogListener {

	private SearchTaskController controller = null;
	
	private static int[] availableIcons = {R.drawable.ic_action_attach, R.drawable.ic_action_call,
		R.drawable.ic_action_copy, R.drawable.ic_action_cut, R.drawable.ic_action_delete,
		R.drawable.ic_action_done, R.drawable.ic_action_edit, R.drawable.ic_action_locate,
		R.drawable.ic_action_mail_add, R.drawable.ic_action_mail};
	
	private ArrayList<Integer> iconsToSearch = null;
	private ArrayList<Integer> distractors = null;
	private ArrayList<ArrayList<ImageView>> icons = null;
	
	private ProgressDialog progressDialogWriting = null;
	
	private int imageSize;
	
	private Animation animationArrows = null;
	private ImageView arrowsUp = null;
	private ImageView arrowsDown = null;
	private ImageView arrowsLeft = null;
	private ImageView arrowsRight = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_layout);
		
		arrowsUp = (ImageView) findViewById(R.id.arrowsUp);
		arrowsLeft = (ImageView) findViewById(R.id.arrowsLeft);
		arrowsRight = (ImageView) findViewById(R.id.arrowsRight);
		arrowsDown = (ImageView) findViewById(R.id.arrowsDown);
		
		progressDialogWriting = new ProgressDialog(SearchTaskView.this);
		progressDialogWriting.setIndeterminate(true);
		progressDialogWriting.setTitle(R.string.wait);
		progressDialogWriting.setCanceledOnTouchOutside(false);
		progressDialogWriting.setCancelable(false);
		
		new Thread() {
			public void run() {
				controller = new SearchTaskController(SearchTaskView.this);
				
				SearchTaskView.this.stress = controller.stress();
				
				progressBar = (ProgressBar) findViewById(R.id.progressTimingBar);
				
				if (!stress)
				{ 
					((LinearLayout) findViewById(R.id.layoutProgressBar)).setVisibility(View.INVISIBLE);
				}
				SearchTaskView.this.manageRotationSensor();
				stepsToStartANewExercise();
			}
		}.start();
		
		animationArrows = new AlphaAnimation(0.0f, 0.7f);
		animationArrows.setDuration(500);
		animationArrows.setStartOffset(10);
		animationArrows.setRepeatMode(Animation.REVERSE);
		animationArrows.setRepeatCount(3);
		
		animationArrows.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				arrowsRight.setVisibility(View.VISIBLE);
				arrowsLeft.setVisibility(View.VISIBLE);
				arrowsUp.setVisibility(View.VISIBLE);
				arrowsDown.setVisibility(View.VISIBLE);
				arrowsUp.invalidate(); arrowsDown.invalidate();
				arrowsLeft.invalidate(); arrowsRight.invalidate();
				Log.w("ANIMATION", "Animation started");
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				arrowsLeft.setVisibility(View.GONE);
				arrowsRight.setVisibility(View.GONE);
				arrowsUp.setVisibility(View.GONE);
				arrowsDown.setVisibility(View.GONE);
			}
		});
	}
	
	/**
	 * Shows the dialog to present the icons to remember
	 */
	private void showDialogPresentation()
	{
		DialogFragment dialog = new ListIconSearchDialog(imageSize);
		dialog.show(getFragmentManager(), null);
	}
	
	/**
	 * Depending if we have to proceed with the exercise, it instantiate a new
	 * one or closes the activity
	 */
	private void stepsToStartANewExercise() 
	{
		if (controller.getContinuePlaying())
		{	
			runOnUiThread(new Runnable() {
				public void run() {
					progressDialogWriting.show();
					setupGame();
					ListIconSearchDialog.setIconsToSearch(iconsToSearch);
				}
			});
		}
		else 
		{
			everythingCompletedGoBackToMainActivity();
		}
	}
	
	/**
	 * Setups the current exercise creating the layout with the icons
	 */
	private void setupGame()
	{
		int iconsToRemember = controller.setupExercise();
		
		chooseIcons(iconsToRemember);
		
		if (icons != null)
		{
			icons.clear();
		}
		icons = new ArrayList<ArrayList<ImageView>>();
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		
		int widthScreen = size.x;
		
		TableLayout layout = (TableLayout) findViewById(R.id.tableLayout);
		
		imageSize = widthScreen / (controller.getNumberOfColumnsPerScreen() + 1);
		final int marginsImage = imageSize / (controller.getNumberOfColumnsPerScreen() * 2);
		
		int counterForTag = 0;
		for (int i = 0; i < controller.getTotalNumberOfRows(); i++)
		{
			
			TableRow row = new TableRow(this);
			TableRow.LayoutParams paramsRow = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT
					, TableRow.LayoutParams.WRAP_CONTENT);
			
			icons.add(new ArrayList<ImageView>());
			
			for (int j = 0; j < controller.getTotalNumberOfColumns(); j++) 
			{
				TableRow.LayoutParams paramsImage = new TableRow.LayoutParams(imageSize, 
						imageSize);
				ImageView imageIcon = new ImageView(this);
				imageIcon.setLayoutParams(paramsImage);
				imageIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
				imageIcon.setTag(String.valueOf(counterForTag));
				imageIcon.setOnClickListener(this);
				
				int idSourceImage = -1;
				if (controller.isIconToSearch(i, j))
				{
					/*
					 *  Picking up a random icon to search for in the grid
					 */
					int randomNumberImageSource = (int)(Math.random() * iconsToSearch.size());
					idSourceImage = iconsToSearch.get(randomNumberImageSource);
				}
				else
				{
					/*
					 *  Picking up a random icon from the distractors
					 */
					int randomNumberImageSource = (int)(Math.random() * distractors.size());
					idSourceImage = distractors.get(randomNumberImageSource);
				}
				
				imageIcon.setImageResource(idSourceImage);
				
				paramsImage.setMargins(marginsImage, marginsImage/2, marginsImage, marginsImage/2);
				row.addView(imageIcon, paramsImage);
				
				counterForTag++;
				icons.get(i).add(imageIcon);
			}
			
			layout.addView(row, paramsRow);
		}
		
		final ImageView finalImage = icons.get(controller.getNumberOfRowsPerScreen())
				.get(controller.getNumberOfColumnsPerScreen());
		
		final HorizontalScrollView horizontalScroll = 
				(HorizontalScrollView) findViewById(R.id.scrollHorizontal);
		
		final VerticalScrollViewWithInfo scrollView = (VerticalScrollViewWithInfo) findViewById(R.id.scrollVertical);
		
		ViewTreeObserver vto = findViewById(R.id.scrollHorizontal).getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				
				int[] locations = new int[2];
				finalImage.getLocationOnScreen(locations);
				
				horizontalScroll.getViewTreeObserver()
					.removeOnGlobalLayoutListener(this);
				
				/**
				 * Scrolling both on the X and Y axis to put the scroller in the
				 * middle of the created "screens"
				 */
				scrollView.scrollTo(0, 
						imageSize * controller.getNumberOfRowsPerScreen());
				horizontalScroll.scrollTo(imageSize * controller.getNumberOfColumnsPerScreen()
						, 0);
						
				ViewGroup parentLayout = (ViewGroup) findViewById(R.id.parentLayout);
				MainActivity.followTreeLayoutToSaveElements(parentLayout);
			}
		});
		showDialogPresentation();
	}
	
	/**
	 * Chooses the icons that will be the ones to search inside the grid
	 * @param correctIcons the number of icons to choose
	 */
	private void chooseIcons(int correctIcons)
	{
		iconsToSearch = new ArrayList<Integer>();
		distractors = new ArrayList<Integer>();
		
		for (int i = 0; i < correctIcons;)
		{
			int randomPosition = (int)(Math.random() * availableIcons.length);
			if (!iconsToSearch.contains(availableIcons[randomPosition]))
			{
				iconsToSearch.add(availableIcons[randomPosition]);
				i++;
			}
		}
		
		/**
		 * Populating the distractors ArrayList with the remaining icons not
		 * in the iconsToSearch ArrayList
		 */
		for (int i = 0; i < availableIcons.length; i++)
		{
			if (!iconsToSearch.contains(availableIcons[i]))
			{
				distractors.add(availableIcons[i]);
			}
		}
	}

	/**
	 * Method called when an icon is clicked. It understands if the icon clicked
	 * is correct or not and changes the image accordingly
	 */
	@Override
	public void onClick(View v) {

		String counter = (String)v.getTag();
		int row = Integer.valueOf(counter) / controller.getTotalNumberOfColumns();
		int column = Integer.valueOf(counter) % controller.getTotalNumberOfColumns();

		MainActivity.mLoggers.writeOnTouchLogger("(CLICK, ICON)");
		boolean result = controller.userClickedIcon(row, column);
		
		if (mConnectionManager != null)
		{
			mConnectionManager.sendMessageSearchTaskIconClicked(stress, result);
		}
		
		if (result)
		{
			((ImageView) v).setImageResource(R.drawable.tick_yes);
		}
		else {
			((ImageView) v).setImageResource(R.drawable.x);
			if (stress)
			{
				/**
				 * To stress more the user, further changing the icon, we play the
				 * negative sound and we vibrate the device
				 */
				final MediaPlayer player = MediaPlayer.create(this, R.raw.wrong);
				final Vibrator vibrator = (Vibrator) this.getApplicationContext()
						.getSystemService(Context.VIBRATOR_SERVICE);
				
				runOnUiThread(new Runnable() {
					public void run() 
					{
						vibrator.vibrate(250);
						player.start();
						player.release();
					}
				});
			}
		}
		v.setClickable(false); v.setOnClickListener(null);
	}
	
	/**
	 * Answer submitted by the user or time expired
	 */
	public void submitAnswer(final View view)
	{
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (view != null)
				{
					controller.stopExercise();
				}
				((VerticalScrollViewWithInfo) findViewById(R.id.scrollVertical)).closeScrolling();
				controller.exerciseCompleted();
			}
		});
	}

	/**
	 * Presentation dialog closed, start the exercise and the stressors if we
	 * are in stress conditions
	 */
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) 
	{
		dialog.dismiss();
		closeWaitingDialog();
		
		if (stress)
		{
			startAudioVideoStressors();
			controller.exerciseStarted();
		}
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				arrowsUp.startAnimation(animationArrows);
				arrowsDown.startAnimation(animationArrows);
				arrowsLeft.startAnimation(animationArrows);
				arrowsRight.startAnimation(animationArrows);
			}
		});
		
	}
	
	/**
	 * Shows a dialog with a resume of the performances of the user
	 * @param totalIcons number of all the shown icons
	 * @param totalCorrectIconsFound number of correct icons found by the user
	 * @param totalMissingIcons number of icons not found by the user
	 * @param wrongAnswers number of wrong clicked icons
	 */
	public void showResumeDialog(int totalIcons, int totalCorrectIconsFound, 
			int totalMissingIcons, int wrongAnswers)
	{
		if (mConnectionManager != null)
		{
			mConnectionManager.sendMessageSearchTaskFinalResult(stress, 
					totalCorrectIconsFound, wrongAnswers, totalMissingIcons);
		}
		
		/**
		 * Wrong clicks = totalTimesClick - totalCorrectIconsFound
		 * total possible correct Icons: missing + correct found
		 */
		ResumeDialog dialog = new ResumeDialog(totalIcons, totalCorrectIconsFound, 
				totalMissingIcons, wrongAnswers, stress);
		dialog.show(getFragmentManager(), null);
	}

	/**
	 * Event called when the Resume dialog has to be closed
	 */
	@Override
	public void onClickCloseDialog(DialogFragment dialog) 
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
						SearchTaskView.this.resetWidgets();
						stepsToStartANewExercise();
					}
				});
			}
		}, 200, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Resets the widgets deleting all the icons inserted for the previous exercise
	 */
	@Override
	protected void resetWidgets()
	{
		super.resetWidgets();
		TableLayout tableLayout = (TableLayout)this.findViewById(R.id.tableLayout);
		tableLayout.removeAllViews();
	}
	
	/**
	 * Method overrided to write touch information
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event)
	{
		TouchData data = new TouchData(event);
		MainActivity.mLoggers.writeOnTouchLogger(data.toString());
		
		return super.dispatchTouchEvent(event);
	}
	
	/**
	 * Closes the Dialog used to ask to the user to wait while we build the 
	 * new exercise
	 */
	public void closeWaitingDialog()
	{
		runOnUiThread(new Runnable()
		{
			public void run() {
				progressDialogWriting.dismiss();
			}
		});	
	}
	
	@Override
	public void onBackPressed()
	{
		return;
	}
}
