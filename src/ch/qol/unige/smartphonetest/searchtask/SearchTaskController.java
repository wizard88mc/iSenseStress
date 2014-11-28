package ch.qol.unige.smartphonetest.searchtask;

import java.util.ArrayList;

import ch.qol.unige.smartphonetest.MainActivity;
import ch.qol.unige.smartphonetest.StepSettings;
import ch.qol.unige.smartphonetest.baseMVC.Controller;

public class SearchTaskController extends Controller 
{
	/**
	 * Default number of columns if 5 and rows is 7, meaning that the lowest
	 * number of possible elements is 35. Since we want to define 9 different
	 * screens, the minimum total amount of icons is 35 * 9 = 315
	 * If we are in stress exercise, the number of columns and rows increase 
	 * by 2, meaning that the number of elements per screen is 7 * 9 = 63, and 
	 * the total amount becomes 63 * 9 = 567  
	 */
	private int numberOfColumnsPerScreen = 5;
	private int numberOfRowsPerScreen = 7;
	private int numberScreensInEachColumnInEachRow = 3;
	private int totalNumberScreens = 9;
	/**
	 * Stores if the (i,j) icon is the correct icon to click or not
	 */
	private ArrayList<ArrayList<Boolean>> icons = new ArrayList<ArrayList<Boolean>>();
	/**
	 * registers which icons the user clicked, independently from the fact that 
	 * it is a correct icon or not
	 */
	private ArrayList<ArrayList<Boolean>> givenAnswers = new ArrayList<ArrayList<Boolean>>();
	
	public SearchTaskController(SearchTaskView view)
	{
		super(view.getIntent().getBooleanExtra(StepSettings.STRESS, false), 
				view.getIntent().getIntExtra(StepSettings.REPETITIONS, 1), 
				view.getIntent().getIntExtra(StepSettings.MINUTE_DURATION, 1));
		
		this.view = view;
		
		model = new SearchTaskModel(stress);
	}
	
	/**
	 * Returns the number of columns per screen
	 * @return columns in each screen
	 */
	public int getNumberOfColumnsPerScreen()
	{
		return numberOfColumnsPerScreen;
	}
	
	/**
	 * Returns the number of rows per screen
	 * @return rows in each screen
	 */
	public int getNumberOfRowsPerScreen()
	{
		return numberOfRowsPerScreen;
	}
	
	/**
	 * Returns the total number of rows of the whole screens
	 * @return rows per screen * number of screens
	 */
	public int getTotalNumberOfRows()
	{
		return numberOfRowsPerScreen * numberScreensInEachColumnInEachRow;
	}
	
	/**
	 * Returns the total numbers of the columns of the whole screens
	 * @return columns per screen * number of screens
	 */
	public int getTotalNumberOfColumns()
	{
		return numberOfColumnsPerScreen * numberScreensInEachColumnInEachRow;
	}
	
	public int setupExercise() 
	{	
		currentRepetition++;
		
		writeRepetition();
		
		icons.clear();
		givenAnswers.clear();
		((SearchTaskModel) model).resetModel();
		/**
		 * Instantiating the matrix of the icons with all boolean set to false
		 */
		for (int i = 0; i < getTotalNumberOfRows(); i++)
		{
			icons.add(new ArrayList<Boolean>());
			givenAnswers.add(new ArrayList<Boolean>());
			for (int j = 0; j < getTotalNumberOfColumns(); j++)
			{
				icons.get(i).add(false);
				givenAnswers.get(i).add(false);
			}
		}
		
		/**
		 * Instantiating the places where the icons will be inside the matrix
		 */
		int correcctIcons = ((SearchTaskModel)model).getTotalNumberCorrectIcons();
		int totalNumberOfElements = numberOfColumnsPerScreen * 
				numberOfRowsPerScreen * totalNumberScreens;
		for (int i = 0; i < correcctIcons;)
		{
			int position = (int)(Math.random() * totalNumberOfElements);
			int indexRow = position / getTotalNumberOfColumns();
			int indexColumn = position % getTotalNumberOfColumns();
			
			if (!icons.get(indexRow).get(indexColumn))
			{
				icons.get(indexRow).set(indexColumn, true);
				i++;
			}
		}
		
		return ((SearchTaskModel) model).getTotalNumberOfIconsToRemember();
	}
	
	/**
	 * Returns if the clicked icon is the right one or not
	 * @param row the row on the screen of the icon
	 * @param column the column on the screen of the icon
	 * @return if the icon clicked is an icon to search or not
	 */
	public boolean isIconToSearch(int row, int column)
	{
		return icons.get(row).get(column);
	}
	
	public boolean userClickedIcon(int row, int column)
	{
		givenAnswers.get(row).set(column, true);
		boolean isCorrectAnswer = isIconToSearch(row, column);
		if (isCorrectAnswer)
		{
			// Correct answer submitted
			((SearchTaskModel)model).correctAnswerGiven();
			return true;
		}
		else 
		{
			((SearchTaskModel)model).wrongIconSelected();
			decreaseStartTimeToPenalize(500);
			return false;
		}
	}
	
	/**
	 * When the user clicks on the Finish button, this method checks if all the
	 * possible correct icons have been clicked
	 */
	/*public void checkIfAllIconsClicked()
	{
		//TODO
		for (int i = 0; i < icons.size(); i++)
		{
			for (int j = 0; j < icons.get(i).size(); j++)
			{
				if (icons.get(i).get(j) && 
						!givenAnswers.get(i).get(j))
				{
					Log.d(CONTROLLER_LOG_STRING, "Found icon not clicked");
					final int row = i, column = j;
					new Thread() {
						public void run() {
							((SearchTaskView) view).showIconNotClicked(row, column);
						}
					}.start();
					try 
					{
						Thread.sleep(1000);
					}
					catch(InterruptedException exc) {}
				}
			}
		}
	}*/
	
	/**
	 * Called when the user submits his answer or when the time expires. 
	 * It collects the data for the report and ask the view to show the 
	 * resume dialog
	 */
	public void exerciseCompleted()
	{
		SearchTaskModel cModel = (SearchTaskModel) model;
		int correctAnswer = cModel.getCorrectAnswerGiven(); 
		int missingIcons = cModel.getForgottenIcons(); 
		int wrongIcons = cModel.getWrongAnswersGiven();
		int totalIcons = getTotalNumberOfRows() * getTotalNumberOfColumns();
		
		String[] report = { String.valueOf(currentRepetition), String.valueOf(1),
				String.valueOf(totalIcons), String.valueOf(correctAnswer + missingIcons),
				String.valueOf(correctAnswer), String.valueOf(missingIcons), 
				String.valueOf(wrongIcons)};
		MainActivity.writeResumeOfExercise(report);
		
		((SearchTaskView) view).showResumeDialog(totalIcons, correctAnswer, 
				missingIcons, wrongIcons);
	}
}
