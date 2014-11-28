package ch.qol.unige.smartphonetest.searchtask.dialogs;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.qol.unige.smartphonetest.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ResumeDialog extends DialogFragment {

	private int totalIcons = 0;
	private int correctAnswers = 0; 
	private int missingIcons = 0; 
	private int wrongAnswer = 0; 
	
	private boolean stress = false;
	private MediaPlayer player = null;
	
	public interface ResumeDialogListener
	{
		public void onClickCloseDialog(DialogFragment dialog);
	}
	
	private ResumeDialogListener listener = null;
	
	private ScheduledThreadPoolExecutor checkerMaximumTimeOpen = null;
	
	public ResumeDialog(int totalIcons, int correctAnswers, int missingIcons, 
			int wrongAnswers, boolean stress)
	{
		this.totalIcons = totalIcons; this.correctAnswers = correctAnswers; 
		this.missingIcons = missingIcons; this.wrongAnswer = wrongAnswers;
		this.stress = stress;
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		this.setCancelable(false);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		View viewDialog = inflater.inflate(R.layout.dialog_resume_search, null);
		
		((TextView) viewDialog.findViewById(R.id.totalIconsOnScreen))
			.setText(String.valueOf(totalIcons));
		((TextView) viewDialog.findViewById(R.id.totalIconsToFind))
			.setText(String.valueOf(correctAnswers + missingIcons));
		((TextView) viewDialog.findViewById(R.id.totalIconsFound))
			.setText(String.valueOf(correctAnswers));
		((TextView) viewDialog.findViewById(R.id.totalMissingIcons))
			.setText(String.valueOf(missingIcons));

		if (!stress) 
		{
			viewDialog.findViewById(R.id.layout_randomly_clicked).setVisibility(View.GONE);
		}
		else
		{
			((TextView) viewDialog.findViewById(R.id.timesRandomlyClicked))
			.setText(String.valueOf(wrongAnswer));
			if (wrongAnswer > 0 && stress)
			{
				((TextView) viewDialog.findViewById(R.id.timesRandomlyClicked))
					.setTextColor(Color.RED);
			}
		}
		if (missingIcons == 0)
		{
			((TextView) viewDialog.findViewById(R.id.totalMissingIcons)).setTextColor(Color.GREEN);
			((TextView) viewDialog.findViewById(R.id.totalIconsFound)).setTextColor(Color.GREEN);
		}
		else if (missingIcons != 0 && stress)
		{
			((TextView) viewDialog.findViewById(R.id.totalMissingIcons)).setTextColor(Color.RED);
			((TextView) viewDialog.findViewById(R.id.totalIconsFound)).setTextColor(Color.RED);
		}
		
		int stringForButton = R.string.try_another_time;
		if (missingIcons == 0 && stress)
		{
			stringForButton = R.string.yeah_i_know_harder;
		}
		else if (missingIcons != 0 && stress)
		{
			stringForButton = R.string.yeah_i_was_bad;
		}
		
		builder.setView(viewDialog);
		builder.setPositiveButton(stringForButton, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				if (player != null)
				{
					player.stop(); player.release();
				}
				if (checkerMaximumTimeOpen != null)
				{
					checkerMaximumTimeOpen.shutdown();
					checkerMaximumTimeOpen = null;
				}
				listener.onClickCloseDialog(ResumeDialog.this);
			}
		});
		
		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		
		return dialog;
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		
		try 
		{
			listener = (ResumeDialogListener) activity;
			
			if (stress) {
				checkerMaximumTimeOpen = new ScheduledThreadPoolExecutor(1);
				checkerMaximumTimeOpen.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
				checkerMaximumTimeOpen.scheduleWithFixedDelay(new Runnable() {
					public void run() {
						listener.onClickCloseDialog(ResumeDialog.this);
						checkerMaximumTimeOpen.shutdown();
					}
				}, 3000, 3000, TimeUnit.MILLISECONDS);
			}
		}
		catch(ClassCastException exc)
		{
			throw new ClassCastException(activity.toString() + 
					" has to implement ResumeDialogListener");
		}
		if (stress && missingIcons > 0)
		{
			player = MediaPlayer.create(activity.getApplicationContext(), 
					R.raw.lost);
			player.start();
		}
		if (!stress && missingIcons == 0)
		{
			player = MediaPlayer.create(activity.getApplicationContext(), R.raw.won);
			player.start();
		}
	}
}
