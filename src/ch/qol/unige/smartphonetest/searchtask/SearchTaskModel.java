package ch.qol.unige.smartphonetest.searchtask;

import ch.qol.unige.smartphonetest.baseMVC.Model;

public class SearchTaskModel extends Model {

	private static final int INCREMENT_BONUS = 500;
	private static final int DECREMENT_PENALTY = 500;
	
	// Defines the number of different icons that the user has to remember and search
	private int totalNumberOfIconsToRemember = 3;
	// Total amount of correct icons to spread all over the other icons
	private int totalNumberCorrectIcons = 30;
	private int wrongAnswersGiven = 0;
	private int correctAnswersGiven = 0;
	private int numberNoClickedIcons = 0;
	
	public SearchTaskModel(boolean stress)
	{
		/**
		 * If we are in stress exercise, increase the number of icons to remember
		 * and the number of icons to find
		 */
		if (stress)
		{
			//totalNumberOfIconsToRemember *= 2;
			//totalNumberCorrectIcons *= 4;
			availableTime = totalNumberCorrectIcons * 1000 + totalNumberCorrectIcons / 3 * 1000;
		}
	}
	
	public void resetModel()
	{
		wrongAnswersGiven = 0;
		correctAnswersGiven = 0;
		numberNoClickedIcons = 0;
	}
	
	/**
	 * Number of different icons the user has to remember
	 * @return the number of different correct icons
	 */
	public int getTotalNumberOfIconsToRemember()
	{
		return totalNumberOfIconsToRemember;
	}
	
	/**
	 * Number of correct icons all over the possible icons
	 * @return the number of correct icons to create
	 */
	public int getTotalNumberCorrectIcons()
	{
		return totalNumberCorrectIcons;
	}
	
	/**
	 * Registers a wrong answer submitted by the user
	 */
	public void wrongIconSelected()
	{
		availableTime -= DECREMENT_PENALTY;
		wrongAnswersGiven++;
	}
	
	/**
	 * Registers a correct answer submitted by the user
	 */
	public void correctAnswerGiven()
	{
		availableTime += INCREMENT_BONUS;
		correctAnswersGiven++;
	}
	
	public void calculateForgottenIcons()
	{
		numberNoClickedIcons = totalNumberCorrectIcons - correctAnswersGiven;
	}
	
	public int getForgottenIcons()
	{
		calculateForgottenIcons();
		return numberNoClickedIcons;
	}
	
	public int getCorrectAnswerGiven()
	{
		return correctAnswersGiven;
	}
	
	public int getWrongAnswersGiven()
	{
		return wrongAnswersGiven;
	}
}
