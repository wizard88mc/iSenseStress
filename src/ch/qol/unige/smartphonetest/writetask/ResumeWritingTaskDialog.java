package ch.qol.unige.smartphonetest.writetask;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.qol.unige.smartphonetest.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ResumeWritingTaskDialog extends DialogFragment 
{
	private Context context = null;
	private int totalCountWords = 0;
	private int totalWrongWords = 0;
	private int totalCorrectWords = 0;
	private boolean stress;
	private MediaPlayer player = null;
	
	public interface ResumeWritingTaskDialogInterface {
		public void onCloseDialogResumeWritingTask(DialogFragment dialog);
	}

	private ResumeWritingTaskDialogInterface listener = null;
	
	private int maxSecondsOpenend = 4;
	private ScheduledThreadPoolExecutor checkerMaximumTimeOpen = null;
	
	public ResumeWritingTaskDialog(int totalCountWords, int totalCorrectWords, 
			int totalWrongWords, boolean stress, Context context)
	{
		this.totalCountWords = totalCountWords; this.totalWrongWords = totalWrongWords;
		this.totalCorrectWords = totalCorrectWords; this.context = context; 
		this.stress = stress;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{	
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_resume_writing_task, null);
		
		((TextView) view.findViewById(R.id.totalNumberWords)).setText(String.valueOf(totalCountWords));
		
		
		builder.setView(view);
		
		int stringButtonID = R.string.try_another_time;
		if (!stress)
		{
			view.findViewById(R.id.layout_wrong_words).setVisibility(View.GONE);
			
			((TextView) view.findViewById(R.id.totalCorrectWords))
				.setText(String.valueOf(totalCorrectWords));
		}
		else 
		{
			view.findViewById(R.id.layout_correct_words).setVisibility(View.GONE);
			
			((TextView) view.findViewById(R.id.totalWrongWords))
				.setText(String.valueOf(totalWrongWords));
			
			if (totalWrongWords != 0)
			{
				stringButtonID = R.string.yeah_i_was_bad;
				((TextView) view.findViewById(R.id.totalWrongWords)).setTextColor(Color.RED);
			}
			else if (totalWrongWords == 0 && stress) 
			{
				stringButtonID = R.string.yeah_i_know_harder;
			}
		}
		builder.setPositiveButton(stringButtonID, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				Log.d("RESUME_WRITING", "DENTRO BUTTON");
				if (player != null)
				{
					player.stop(); player.release();
				}
				if (checkerMaximumTimeOpen != null)
				{
					checkerMaximumTimeOpen.shutdown(); 
					checkerMaximumTimeOpen = null;
				}
				listener.onCloseDialogResumeWritingTask(ResumeWritingTaskDialog.this);
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		return dialog;
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try 
		{
			listener = (ResumeWritingTaskDialogInterface) activity;
			if (stress)
			{
				checkerMaximumTimeOpen = new ScheduledThreadPoolExecutor(1);
				checkerMaximumTimeOpen.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
				checkerMaximumTimeOpen.scheduleWithFixedDelay(new Runnable() {
					public void run() {
						listener.onCloseDialogResumeWritingTask(ResumeWritingTaskDialog.this);
						checkerMaximumTimeOpen.shutdown(); 
						checkerMaximumTimeOpen = null;
					}
				}, maxSecondsOpenend, maxSecondsOpenend, TimeUnit.SECONDS);
			}
		}
		catch(ClassCastException exc)
		{
			throw new ClassCastException(activity.toString() + " has to implement " +
					"ResumeWritingTaskDialogInterface.");
		}
		
		if (stress && this.totalWrongWords > 0)
		{
			player = MediaPlayer.create(activity.getApplicationContext(), R.raw.lost);
			player.start();
		}
		if (!stress && this.totalCorrectWords == this.totalCountWords)
		{
			player = MediaPlayer.create(activity.getApplicationContext(), R.raw.won);
			player.start();
		}
	}
}
