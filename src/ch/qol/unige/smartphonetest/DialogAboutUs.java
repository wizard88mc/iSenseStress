package ch.qol.unige.smartphonetest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class DialogAboutUs extends DialogFragment {

	private static final String WEBSITE = "<a href='http://www.qol.unige.ch/index.html'>QoL home page</a>";
	private static final String EMAIL = "<a href=\"mailto:mciman@math.unipd.it\">Matteo Ciman</a>";
	
	private View dialogAboutUsView = null;
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		super.onCreateDialog(savedInstanceState);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		dialogAboutUsView = inflater.inflate(R.layout.dialog_about, null);
		builder.setView(dialogAboutUsView); 
		
		TextView websiteRef = (TextView) dialogAboutUsView.findViewById(R.id.websiteLink);
		websiteRef.setText(Html.fromHtml(WEBSITE));
		websiteRef.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView emailRef = (TextView) dialogAboutUsView.findViewById(R.id.developerMail);
		emailRef.setText(Html.fromHtml(EMAIL));
		emailRef.setMovementMethod(LinkMovementMethod.getInstance());
		
		builder.setPositiveButton(R.string.ok, null);
		
		return builder.create();
	}
}
