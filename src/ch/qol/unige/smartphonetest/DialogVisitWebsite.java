package ch.qol.unige.smartphonetest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class DialogVisitWebsite extends DialogFragment {

	private static final String LINK = "<a href='http://trainutri.unige.ch/matteo/index_lottery.html'>Lottery Website Homepage</a>";
	private View dialogWebsiteView = null;
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		super.onCreateDialog(savedInstanceState);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		dialogWebsiteView = inflater.inflate(R.layout.dialog_final_with_link, null);
		builder.setView(dialogWebsiteView);
		
		TextView text = (TextView) dialogWebsiteView.findViewById(R.id.websiteLink);
		text.setText(Html.fromHtml(LINK));
		text.setMovementMethod(LinkMovementMethod.getInstance());
		
		builder.setPositiveButton(R.string.goodbye, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		AlertDialog dialog = builder.create();
		dialog.setCancelable(false); dialog.setCanceledOnTouchOutside(false);
		
		return dialog;
	}
}
