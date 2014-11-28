package ch.qol.unige.smartphonetest.writetask.customkeyboard;

import java.util.List;

import ch.qol.unige.smartphonetest.R;
import ch.qol.unige.smartphonetest.MainActivity;
import ch.qol.unige.smartphonetest.writetask.WritingTaskController;
import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class CustomKeyboard 
{
	private KeyboardViewWithInfo mKeyboardView = null;
	private Activity activity = null;
	private Keyboard mKeyboard = null;
	private Keyboard mKeyboardShifted = null;
	private boolean shifted = false;
	
	private WritingTaskController mController = null;
	private String lastDigit = "";
	
	private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener() {

        @Override public void onKey(int primaryCode, int[] keyCodes) 
        {
            // NOTE We can say '<Key android:codes="49,50" ... >' in the xml file;
        	// all codes come in keyCodes, the first in this list in primaryCode
            // Get the EditText and its Editable
            View focusCurrent = activity.getWindow().getCurrentFocus();
            if (focusCurrent == null || focusCurrent.getClass() != EditText.class) 
            	return;
            
            EditText edittext = (EditText) focusCurrent;
            Editable editable = edittext.getText();
            int start = edittext.getSelectionStart();
            
            // Apply the key to the EditText
            if (primaryCode == Keyboard.KEYCODE_CANCEL) 
            {
                hideCustomKeyboard();
            } 
            else if (primaryCode == Keyboard.KEYCODE_DELETE) 
            {
                if (editable != null && start > 0)
                {
                	editable.delete(start - 1, start);
                	//MainActivity.mLoggers.writeOnTouchLogger("(DIGIT,DELETE)");
                	lastDigit = "DELETE";
                	mController.backButtonClicked();
                }
            }
            else if (primaryCode == Keyboard.KEYCODE_SHIFT)
            {
            	//MainActivity.mLoggers.writeOnTouchLogger("(DIGIT,SHIFT)");
            	lastDigit = "SHIFT";
            	if (!shifted)
            	{
            		mKeyboardView.setKeyboard(mKeyboardShifted);
            	}
            	else
            	{
            		mKeyboardView.setKeyboard(mKeyboard);
            	}
            	shifted = !shifted;
            }
            else 
            { // insert character
            	Character c = (char) primaryCode;
            	if (shifted) 
            	{
            		c = Character.toUpperCase(c);
            	}
                editable.insert(start, Character.toString(c));
                
                //MainActivity.mLoggers.writeOnTouchLogger("(DIGIT," + 
                //		Character.toString(c) + ")");
                lastDigit = Character.toString(c);
                if (lastDigit.equals(","))
                {
                	lastDigit = "comma";
                }
                
                if (shifted)
                {
                	shifted = false;
                	mKeyboardView.setKeyboard(mKeyboard);
                }
            }
        }

        @Override public void onPress(int arg0) {}

        @Override public void onRelease(int primaryCode) {}

        @Override public void onText(CharSequence text) {}

        @Override public void swipeDown() {}

        @Override public void swipeLeft() {}

        @Override public void swipeRight() {}

        @Override public void swipeUp() {}
    };

    /**
     * Create a custom keyboard, that uses the KeyboardView (with resource id <var>viewid</var>) of the <var>host</var> activity,
     * and load the keyboard layout from xml file <var>layoutid</var> (see {@link Keyboard} for description).
     * Note that the <var>host</var> activity must have a <var>KeyboardView</var> in its layout (typically aligned with the bottom of the activity).
     * Note that the keyboard layout xml file may include key codes for navigation; see the constants in this class for their values.
     * Note that to enable EditText's to use this custom keyboard, call the {@link #registerEditText(int)}.
     *
     * @param host The hosting activity.
     * @param viewid The id of the KeyboardView.
     * @param layoutid The id of the xml file containing the keyboard layout.
     */
    public CustomKeyboard(Activity host, int viewID, 
    		WritingTaskController controller) 
    {
        activity = host;
        this.mController = controller;
        
        mKeyboard = new Keyboard(host, R.xml.layout_softkeyboard);
        mKeyboardShifted = new Keyboard(host, R.xml.layout_softkeyboard_caps);
        mKeyboardView = (KeyboardViewWithInfo) activity.findViewById(viewID);
        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView.setPreviewEnabled(true); // NOTE Do not show the preview balloons
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
        
        // Hide the standard keyboard initially
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /** Returns whether the CustomKeyboard is visible. */
    public boolean isCustomKeyboardVisible() 
    {
        return mKeyboardView.getVisibility() == View.VISIBLE;
    }

    /** Make the CustomKeyboard visible, and hide the system keyboard for view v. */
    public void showCustomKeyboard(View v) 
    {
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);
        if (v!=null) 
        {
        	((InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE))
        		.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        MainActivity.mLoggers.writeOnTouchLogger("(KEYBOARD_SHOW)");
    }

    /** 
     * Make the CustomKeyboard invisible. 
     */
    public void hideCustomKeyboard() 
    {
        mKeyboardView.setVisibility(View.GONE);
        mKeyboardView.setEnabled(false);
        MainActivity.mLoggers.writeOnTouchLogger("(KEYBOARD_HIDE)");
    }

    /**
     * Register <var>EditText<var> with resource id <var>resid</var> 
     * (on the hosting activity) for using this custom keyboard.
     *
     * @param resid The resource id of the EditText that registers to the custom keyboard.
     */
    public void registerEditText(int resid) 
    {    
        final EditText editText = (EditText) activity.findViewById(resid);
        
        /**
         *  Make the custom keyboard appear
         */
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            /**
             * By setting the on focus listener, we can show the custom keyboard 
             * when the edit box gets focus, but also hide it when the edit box 
             * loses focus
             */
            @Override public void onFocusChange(View v, boolean hasFocus) 
            {
                if (hasFocus) 
            	{
                	showCustomKeyboard(v); 
            	}
                else 
            	{
                	hideCustomKeyboard();
            	}
            }
        });
        
        editText.setOnClickListener(new OnClickListener() 
        {
            /**
             *  NOTE By setting the on click listener, we can show the custom 
             *  keyboard again, by tapping on an edit box that already had focus 
             *  (but that had the keyboard hidden).
             */
            @Override public void onClick(View v) 
            {
                showCustomKeyboard(v);
                editText.setSelection(editText.length());
            }
        });
        // Disable standard keyboard hard way
        // NOTE There is also an easy way: 'edittext.setInputType(InputType.TYPE_NULL)' (but you will not have a cursor, and no 'edittext.setCursorVisible(true)' doesn't work )
        editText.setOnTouchListener(new OnTouchListener() {
            @Override 
            public boolean onTouch(View v, MotionEvent event) {
                EditText editText = (EditText) v;
                int inType = editText.getInputType();       // Backup the input type
                editText.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
                editText.onTouchEvent(event);               // Call native handler
                editText.setInputType(inType);              // Restore input type
                return true; // Consume touch event
            }
        });
        // Disable spell check (hex strings look like words to Android)
        editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }
    
    public void writeKeyboardLayoutSpect()
    {
    	List<Keyboard.Key> keys = mKeyboard.getKeys();
    	
    	for (Keyboard.Key key: keys)
    	{
    		String toWrite = "[-1," + key.getClass().toString() + "," + key.x + 
    				"," + key.y + "," + key.width + "," + key.height + ",0,";
    		
    		/**
    		 * Handling special characters
    		 */
    		if (key.codes[0] == -1)
    		{
    			toWrite += "shift";
    		}
    		else if (key.codes[0] == -5)
    		{
    			toWrite += "delete";
    		}
    		else if (key.codes[0] == 32)
    		{
    			toWrite += "space";
    		}
    		else if (key.codes[0] == 44)
    		{
    			toWrite += "comma";
    		}
    		else if (key.codes[0] == 46)
    		{
    			toWrite += "dot";
    		}
    		else 
    		{
    			toWrite += key.label;
    		}
    		toWrite += "]";
    		
    		MainActivity.mLoggers.writeOnLayoutLogger(toWrite);
    	}
    }
    
    public void writeDigit()
    {
    	if (!lastDigit.equals(""))
    	{
    		MainActivity.mLoggers.writeOnTouchLogger("(DIGIT,"+lastDigit + ")");
    		lastDigit = "";
    	}
    }
}
