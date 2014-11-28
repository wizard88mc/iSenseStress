package ch.qol.unige.smartphonetest.writetask;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.qol.unige.smartphonetest.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class ReadyWritingTaskDialog extends DialogFragment 
{
	public interface ReadyWritingTaskDialogListener {
		public void onCloseDialogPresentation(DialogFragment dialog);
	}

	private ReadyWritingTaskDialogListener listener;
	private boolean stress = false;
	
	private ScheduledThreadPoolExecutor maximumTimeChecker = null;
	private int maximumSecondsOpened = 2;
	
	public ReadyWritingTaskDialog(boolean stress)
	{
		this.stress = stress;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		this.setCancelable(false);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		View viewDialog = inflater.inflate(R.layout.dialog_writing_task, null);
		
		if (this.stress)
		{
			viewDialog.findViewById(R.id.beforeTimeExpiresText).setVisibility(View.VISIBLE);
		}
		
		builder.setView(viewDialog);
		builder.setPositiveButton(R.string.start, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (maximumTimeChecker != null)
				{
					maximumTimeChecker.shutdown();
					maximumTimeChecker = null;
				}
				listener.onCloseDialogPresentation(ReadyWritingTaskDialog.this);
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
			listener = (ReadyWritingTaskDialogListener) activity;
			if (stress)
			{
				maximumTimeChecker = new ScheduledThreadPoolExecutor(1);
				maximumTimeChecker.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
				maximumTimeChecker.schedule(new Runnable() {
					
					@Override
					public void run() {
						ReadyWritingTaskDialog.this.maximumTimeChecker.shutdown();
						listener.onCloseDialogPresentation(ReadyWritingTaskDialog.this);
					}
				}, maximumSecondsOpened, TimeUnit.SECONDS);
			}
		}
		catch(ClassCastException exc) {
			throw new ClassCastException(activity.toString() + "has to implement " 
					+ "ReadyWritingTaskDialogListener");
		}
	}
}
