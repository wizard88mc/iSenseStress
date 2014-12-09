package ch.qol.unige.smartphonetest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DialogPersonalInformation extends DialogFragment {
	
	private View personalInfoDialog = null;
	private String currentValueSpinner = null;
	
	public interface DialogPersonalInformationInterface {
		public void saveUserInformation(String gender, String age, 
				String email, String nickname);
	}
	
	private DialogPersonalInformationInterface listener;
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		super.onCreateDialog(savedInstanceState);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		personalInfoDialog = inflater.inflate(R.layout.dialog_user_details, null);
		builder.setView(personalInfoDialog);
		
		Spinner spinner = (Spinner) personalInfoDialog.findViewById(R.id.spinnerGender);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.genders, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				currentValueSpinner = parent.getItemAtPosition(pos).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				currentValueSpinner = null;
			}
		});
		
		builder.setPositiveButton(R.string.proceed, null);
		builder.setNegativeButton(R.string.discard, null);
		
		final AlertDialog finalDialog = builder.create();
		finalDialog.setCancelable(false);
		finalDialog.setCanceledOnTouchOutside(false);
		
		finalDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(final DialogInterface dialog) {
				Button positiveButton = finalDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				positiveButton.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						String age = ((TextView) personalInfoDialog.findViewById(R.id.userAge)).getText().toString();
						String email = ((TextView) personalInfoDialog.findViewById(R.id.emailUser)).getText().toString();
						String nickname = ((TextView) personalInfoDialog.findViewById(R.id.nickname)).getText().toString();
						
						if (!age.equals("") && currentValueSpinner != null && 
								!email.equals("") && !nickname.equals("")) {
						
							boolean correct = checkInput(age, email, nickname);
							if (correct) {
								listener.saveUserInformation(currentValueSpinner, age, 
										email, nickname);
							}
							else {
								Toast.makeText(getActivity(), 
										R.string.input_user_info_not_correct, 
										Toast.LENGTH_LONG).show();
							}
						}
						else
						{
							Toast.makeText(getActivity(), R.string.error_user_info, Toast.LENGTH_LONG).show();
						}
					}
				});
				
				Button negativeButton = finalDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
				negativeButton.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				
			}
		});
		
		return finalDialog;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (DialogPersonalInformationInterface) activity;
		}
		catch(ClassCastException exc) {
			throw new ClassCastException(activity.toString() + " has to " + 
					"implement DialogPersonalInformationInterface");
		}
	}
	
	/**
	 * Performs an easy check of user input before sending it to the 
	 * Main Activity
	 * @param age the age inserted by the user
	 * @param email the email inserted by the user
	 * @param nickname the nickname inserted by the user
	 * @return if the inserted input is correct or not
	 */
	private boolean checkInput(String age, String email, String nickname) {
		
		boolean correct = true;
		
		if ((age.trim()).length() > 3 && age.trim().length() < 1) {
			correct = false;
		}
		if (!email.trim().contains("@") || email.trim().length() < 5 || 
				!email.trim().contains(".")) {
			correct = false;
		}
		
		if (nickname.trim().length() < 3) {
			correct = false;
		}
		
		return correct;
	}

}
