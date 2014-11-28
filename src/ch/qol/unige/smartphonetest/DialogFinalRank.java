package ch.qol.unige.smartphonetest;

import ch.qol.unige.smartphonetest.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class DialogFinalRank extends DialogFragment 
{
	private int finalPosition;
	
	public interface DialogFinalRankInterface {
		public void finalRankDialogClosed(DialogFragment dialog);
	}
	
	private DialogFinalRankInterface listener = null;
	
	public DialogFinalRank(int finalPosition)
	{
		this.finalPosition = finalPosition;
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		View view = inflater.inflate(R.layout.dialog_final_rank, null);
		builder.setView(view);
		
		((TextView) view.findViewById(R.id.finalPosition)).setText(String.valueOf(finalPosition));
		
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) 
			{
				listener.finalRankDialogClosed(DialogFinalRank.this);				
			}
		});
		
		return builder.create();
	}
	
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		
		try 
		{
			listener = (DialogFinalRankInterface) activity;
		}
		catch(ClassCastException exc)
		{
			throw new ClassCastException(activity.toString() + "has to implement " + 
					"DialogFinalRankInterface interface");
		}
	}
	
}
