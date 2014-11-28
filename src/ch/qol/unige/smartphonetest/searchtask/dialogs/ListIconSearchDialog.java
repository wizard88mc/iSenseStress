package ch.qol.unige.smartphonetest.searchtask.dialogs;

import java.util.ArrayList;

import ch.qol.unige.smartphonetest.R;
import ch.qol.unige.smartphonetest.searchtask.SearchTaskView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

public class ListIconSearchDialog extends DialogFragment 
{
	private static ArrayList<Integer> iconsToSearch = null;
	private int imageSize;
	
	public interface PresentationDialogListener
	{
		public void onDialogPositiveClick(DialogFragment dialog);
	}
	
	private PresentationDialogListener listener;
	
	public ListIconSearchDialog(int imageSize)
	{
		this.imageSize = imageSize;
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		this.setCancelable(false);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		View viewDialog = inflater.inflate(R.layout.dialog_icons_to_search, null);
		
		TableLayout layout = (TableLayout) viewDialog.findViewById(R.id.tableLayoutListIcons);
		layout.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 
				TableLayout.LayoutParams.WRAP_CONTENT));
		
		for (int i = 0; i < iconsToSearch.size(); i = i+2)
		{
			TableRow row = new TableRow(layout.getContext());
			
			TableRow.LayoutParams paramsRow = 
					new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 
							TableRow.LayoutParams.WRAP_CONTENT);
			
			row.setLayoutParams(paramsRow);
			row.setGravity(Gravity.CENTER_HORIZONTAL);
			
			for (int j = i; j < iconsToSearch.size() && j < i+2; j++)
			{
				
				/*LinearLayout.LayoutParams paramsImage = 
						new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
								LinearLayout.LayoutParams.WRAP_CONTENT, 1);*/
				
				TableRow.LayoutParams paramsImage = 
						new TableRow.LayoutParams(imageSize, imageSize);
				
				ImageView imageView = new ImageView(getActivity());
				imageView.setLayoutParams(paramsImage);
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
				imageView.setImageResource(iconsToSearch.get(j));
				
				int marginPixel = getResources().getDimensionPixelSize(R.dimen.margin_between_widgets);
				paramsImage.setMargins(marginPixel,marginPixel, marginPixel, marginPixel);
				
				row.addView(imageView, paramsImage);
			}
			layout.addView(row, paramsRow);
		}
		
		builder.setView(viewDialog);
		builder.setPositiveButton(R.string.ready_to_start, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				listener.onDialogPositiveClick(ListIconSearchDialog.this);
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		
		return dialog;
	}
	
	public static void setIconsToSearch(ArrayList<Integer> iconsToSearch)
	{
		ListIconSearchDialog.iconsToSearch = iconsToSearch;
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		((SearchTaskView) activity).closeWaitingDialog();
		try 
		{
			listener = (PresentationDialogListener) activity;
		}
		catch(ClassCastException exc)
		{
			throw new ClassCastException(activity.toString() + 
					" has to implement PresentationDialogListener");
		}
	}
}
