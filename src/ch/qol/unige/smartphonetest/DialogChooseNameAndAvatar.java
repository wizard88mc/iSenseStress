package ch.qol.unige.smartphonetest;

import java.util.ArrayList;

import ch.qol.unige.smartphonetest.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DialogChooseNameAndAvatar extends DialogFragment 
{
	
	private ArrayList<ImageView> avatarsView = new ArrayList<>();
	private String currentSelectedAvatar = null;
	private View nameAvatarDialog = null;
	
	public interface DialogChooseNameAndAvatarInterface {
		public void setupPlayerSettingsCompleted(DialogFragment dialog, 
				String name, String avatar);
	}
	private DialogChooseNameAndAvatarInterface listener;

	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		nameAvatarDialog = inflater.inflate(R.layout.dialog_name_avatar, null);
		builder.setView(nameAvatarDialog);
		
		avatarsView = getImageViews((TableLayout) nameAvatarDialog.findViewById(R.id.tableAvatars));
		
		for (ImageView image: avatarsView)
		{
			image.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if (!v.isSelected())
					{
						if (currentSelectedAvatar != null)
						{
							ImageView oldImage = findImageViewByTag(currentSelectedAvatar);
							oldImage.performClick();
						}
						v.setBackgroundColor(Color.parseColor("#33B5E5"));
						v.setSelected(true);
						currentSelectedAvatar = v.getTag().toString();
					}
					else
					{
						v.setBackgroundColor(Color.parseColor("#00000000"));
						v.setSelected(false);
						currentSelectedAvatar = null;
					}
				}
			});
		}
		
		builder.setPositiveButton(R.string.ready, null);
		
		final AlertDialog d = builder.create();
		d.setCancelable(false);
		d.setCanceledOnTouchOutside(false);
		
		d.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) 
			{
				Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) 
					{	
						String name = ((TextView) DialogChooseNameAndAvatar.this
								.nameAvatarDialog.findViewById(R.id.nickname)).getText().toString();
						
						if (name.trim().length() < 3)
						{
							Toast.makeText(getActivity(), R.string.please_provide_nickname, 
									Toast.LENGTH_SHORT).show();
						}
						else if (currentSelectedAvatar == null)
						{
							Toast.makeText(getActivity(), R.string.please_choose_avatar, 
									Toast.LENGTH_SHORT).show();
						}
						else if (currentSelectedAvatar != null && name.trim().length() >= 3)
						{
							listener.setupPlayerSettingsCompleted(DialogChooseNameAndAvatar.this, name, currentSelectedAvatar);
						}
					}
				});
				
			}
		});
		
		return d;
	}
	
	/**
	 * Retrieves the list of all the ImageView avatars
	 * @param layout the TableLayout to start from to search
	 * @return all the ImageView in the TableLayout
	 */
	private ArrayList<ImageView> getImageViews(ViewGroup layout)
	{
		ArrayList<ImageView> toReturn = new ArrayList<ImageView>();
		
		for (int i = 0; i < layout.getChildCount(); i++)
		{
			if (layout.getChildAt(i) instanceof ImageView)
			{
				toReturn.add((ImageView) layout.getChildAt(i));
			}
			else if (layout.getChildAt(i) instanceof ViewGroup)
			{
				toReturn.addAll(getImageViews((ViewGroup) layout.getChildAt(i)));
			}
		}
		
		return toReturn;
	}
	
	/**
	 * Retrieves the ImageView identified by a particular tag
	 * @param tag the tag to search
	 * @return the ImageView with the particular tag
	 */
	private ImageView findImageViewByTag(String tag)
	{
		for (ImageView image: avatarsView)
		{
			if (image.getTag().toString().equals(tag))
			{
				return image;
			}
		}
		return null;
	}
	
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try
		{
			listener = (DialogChooseNameAndAvatarInterface) activity;
		}
		catch(ClassCastException exc)
		{
			throw new ClassCastException(activity.toString() + " has to implement "
					+ "SetupPlayerSettingsCompleted interface");
		}
	}
}
