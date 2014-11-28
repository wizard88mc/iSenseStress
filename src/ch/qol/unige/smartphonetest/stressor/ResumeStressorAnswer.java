package ch.qol.unige.smartphonetest.stressor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.qol.unige.smartphonetest.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class ResumeStressorAnswer extends DialogFragment {

	private int startValue = 0;
	private int decreaseValue = 0;
	private int answerGiven = 0;
	private boolean stress;
	
	private MediaPlayer playerLost = null;
	
	public interface ResumeStressorAnswerListener {
		public void onCloseStressorAnswerDialog(DialogFragment dialog);
	}
	
	private ResumeStressorAnswerListener listener;
	
	private ScheduledThreadPoolExecutor checkerMaximumTimeOpen = null;
	
	public ResumeStressorAnswer(boolean stress, int startValue, 
			int decreaseValue, int answerGiven)
	{
		this.startValue = startValue; this.decreaseValue = decreaseValue; 
		this.answerGiven = answerGiven; this.stress = stress;
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		this.setCancelable(false);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		View view = inflater.inflate(R.layout.dialog_resume_stressor, null);
		((TextView) view.findViewById(R.id.firstValue)).setText(String.valueOf(startValue));
		((TextView) view.findViewById(R.id.secondValue)).setText(String.valueOf(decreaseValue));
		((TextView) view.findViewById(R.id.answer)).setText(String.valueOf(answerGiven));
		
		builder.setView(view);
		builder.setPositiveButton(R.string.bit_wrong, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				if (playerLost != null)
				{
					playerLost.release();
				}
				if (checkerMaximumTimeOpen != null)
				{
					checkerMaximumTimeOpen.shutdown(); 
					checkerMaximumTimeOpen = null;
				}
				listener.onCloseStressorAnswerDialog(ResumeStressorAnswer.this);
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		
		return dialog;
	}
	
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try 
		{
			listener = (ResumeStressorAnswerListener) activity;
			
			if (stress)
			{
				checkerMaximumTimeOpen = new ScheduledThreadPoolExecutor(1);
				checkerMaximumTimeOpen.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
				checkerMaximumTimeOpen.scheduleAtFixedRate(new Runnable() {
					public void run() {
						listener.onCloseStressorAnswerDialog(ResumeStressorAnswer.this);
						checkerMaximumTimeOpen.shutdown();
					}
				}, 2500, 2500, TimeUnit.MILLISECONDS);
			}
			
		}
		catch(ClassCastException exc)
		{
			throw new ClassCastException(activity.toString() + " has to implement " +
					"ResumeStressorAnswerListener interface");
		}
		
		if (stress)
		{
			playerLost = MediaPlayer.create(activity.getApplicationContext(), 
					R.raw.lost);
			playerLost.start();
		}
	}
}
