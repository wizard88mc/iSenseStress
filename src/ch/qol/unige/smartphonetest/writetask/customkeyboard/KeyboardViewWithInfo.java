package ch.qol.unige.smartphonetest.writetask.customkeyboard;

import android.content.Context;
import android.graphics.Color;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

public class KeyboardViewWithInfo extends KeyboardView 
{
	public KeyboardViewWithInfo(Context context, AttributeSet attr)
	{
		super(context, attr);
		//this.setBackgroundColor(Color.RED);
	}
	
	public KeyboardViewWithInfo(Context context, AttributeSet attr, int defStyle)
	{
		super(context, attr, defStyle);
		//this.setBackgroundColor(Color.RED);
	}
}
