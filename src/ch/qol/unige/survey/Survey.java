package ch.qol.unige.survey;

import ch.qol.unige.smartphonetest.R;
import ch.qol.unige.smartphonetest.MainActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class Survey extends DialogFragment {

	private View form = null;
	
	public interface SurveyDialogResultInterface {
		public void onCloseSurveyDialogWithAnswer(DialogFragment dialog, String valence, 
				String energy, String stress);
	}
	
	private SurveyDialogResultInterface listener = null;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{	
		this.setCancelable(false);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		form = inflater.inflate(R.layout.survey_layout, null);
		
		builder.setView(form);
		builder.setPositiveButton(R.string.sure_dialog_survey, null);	
		final AlertDialog finalDialog = builder.create();
		finalDialog.setCanceledOnTouchOutside(false);
		
		finalDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {

				final Button b = finalDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v)
					{
						/**
						 * Retrieving the RadioButton selected by the user for 
						 * the survey
						 */
						int radioValenceId = ((RadioGroup) form.findViewById(R.id.groupValence))
								.getCheckedRadioButtonId();
						int radioEnergyId = ((RadioGroup) form.findViewById(R.id.groupEnergy))
								.getCheckedRadioButtonId();
						int radioStressId = ((RadioGroup) form.findViewById(R.id.groupStress))
								.getCheckedRadioButtonId();
						
						if (radioEnergyId != -1 && radioValenceId != -1 && radioStressId != -1)
						{
							/**
							 * User has completed the survey, save data and close the dialog
							 */
							String valence = (String) ((RadioButton) form.findViewById(radioValenceId))
									.getText();
							String energy = (String) ((RadioButton) form.findViewById(radioEnergyId))
									.getText();
							String stress = (String) ((RadioButton) form.findViewById(radioStressId))
									.getText();
							
							/*if (Integer.valueOf(valence) > 3 && 
									Integer.valueOf(energy) > 3 && 
									Integer.valueOf(stress) > 3)
							{
								Toast.makeText(getActivity(), R.string.check_answer, 
										Toast.LENGTH_SHORT).show();
							}
							else 
							{*/
								listener.onCloseSurveyDialogWithAnswer(Survey.this, valence, 
									energy, stress);
							//}
						}
						else 
						{
							/**
							 * Show a Toast to indicate that the survey is not completed 
							 */
							Toast.makeText(getActivity(), R.string.survey_not_completed, 
									Toast.LENGTH_SHORT).show();
						}
					}
				});
				
				/**
				 * Adding a GlobalLayoutListener to write on the logger the layout
				 * of the Dialog
				 */
				ViewTreeObserver vto = form.getViewTreeObserver();
				vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
					
					@Override
					public void onGlobalLayout() {
						MainActivity.followTreeLayoutToSaveElements((ViewGroup)form);
						form.getViewTreeObserver().removeOnGlobalLayoutListener(this);
						
						/**
						 * Since the button is not part of the layout, 
						 * we manually write the data on the logger
						 */
						MainActivity.mLoggers.writeOnLayoutLogger(
								MainActivity.createStringForViewElement(b));
					}
				});
			}
		});
		
		return finalDialog;
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try 
		{
			listener = (SurveyDialogResultInterface) activity;
		}
		catch(ClassCastException exc) 
		{
			throw new ClassCastException(activity.toString() + " has to implement "
					+ "SurveyDialogResultInterface");
		}
	}

}
