package ch.qol.unige.smartphonetest.writetask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import ch.qol.unige.smartphonetest.R;
import ch.qol.unige.smartphonetest.baseMVC.Model;
import android.content.Context;

public class WritingTaskModel extends Model {
	
	private static ArrayList<String> possibleParagraphs = null;
	
	private String textToWrite = null;
	private Boolean stress = null;
	private Boolean mixFonts = null;
	
	private Context context = null;
	
	private int totalErrors = 0;
	private int totalCorrect = 0;
	
	public WritingTaskModel(boolean stress, Context context)
	{
		this.context = context;
		if (possibleParagraphs == null)
		{
			retrieveParagraphsForLanguage();
		}
		
		this.stress = stress;
	}
	
	public void instantiateExercise()
	{
		instantiateLevel();
	}
	
	/**
	 * Retrieves paragraphs to use from a resource file
	 */
	private void retrieveParagraphsForLanguage()
	{
		possibleParagraphs = new ArrayList<String>();
		
		String language = Locale.getDefault().getLanguage();
		int fileResourceID = R.raw.paragraphs_en;
		/*if (language.equals("fr"))
		{
			fileResourceID = R.raw.paragraphs_fr;
		}*/
		/*else if (language.equals("it"))
		{
			fileResourceID = R.raw.paragraphs_it;
		}*/
		
		InputStream inputStream = context.getResources().openRawResource(fileResourceID);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		
		try
		{
			while ((line = reader.readLine()) != null)
			{
				possibleParagraphs.add(line);
			}
		}
		catch(IOException exc) 
		{}
	}
	
	/**
	 * Instantiates a new level: decides the text to write and the amount of time
	 * available (used only in case of stress exercise)
	 */
	private void instantiateLevel()
	{	
		if (this.stress)
		{
			mixFonts = Math.random() < 0.5;
		}
		else 
		{
			mixFonts = false;
		}
		
		textToWrite = possibleParagraphs.get((int)(Math.random() * possibleParagraphs.size()));
		
		if (stress)
		{
			availableTime = getTotalWords() * 2000 + getTotalWords() / 2 * 1000;
		}
	}
	
	/**
	 * Returns the text the user has to write
	 * @return the text to write
	 */
	public String getTextToWrite()
	{
		return textToWrite;
	}
	
	/**
	 * Returns if the exercise has to be more difficult mixing fonts
	 * @return if the view has to mix fonts or not
	 */
	public boolean mixFonts()
	{
		return this.mixFonts;
	}
	
	/**
	 * Returns the number of errors made by the user
	 * @param answer the text submitted by the user
	 * @return the number of errors if in stress mode, otherwise the number of 
	 * correct words
	 */
	public void checkSubmittedAnswer(String answer, int totalBackButtons)
	{
		String[] wordsToWrite = textToWrite.split(" ");
		String[] wordsWritten = answer.split(" ");
		totalErrors = 0;
		totalCorrect = 0;
		
		for (int i = 0; i < wordsToWrite.length; i++)
		{
			if (!(i < wordsWritten.length) || 
					!(wordsToWrite[i].equals(wordsWritten[i])))
			{
				totalErrors++;
			}
			if (i < wordsToWrite.length && i < wordsWritten.length && 
					wordsToWrite[i].equals(wordsWritten[i]))
			{
				totalCorrect++;
			}
		}
	}
	
	/**
	 * Returns the total amount of words to write of the chosen text
	 * @return the number of words
	 */
	public int getTotalWords()
	{
		return textToWrite.split(" ").length;
	}
	
	/**
	 * Returns the number of correct words written in the last exercise
	 * @return the number of correct words
	 */
	public int getTotalCorrect()
	{
		return this.totalCorrect;
	}
	
	/**
	 * Returns the number of wrong words written in the last exercise
	 * @return the number of wrong words
	 */
	public int getTotalErrors() 
	{
		return this.totalErrors;
	}
}
