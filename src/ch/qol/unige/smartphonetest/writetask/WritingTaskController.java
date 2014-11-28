package ch.qol.unige.smartphonetest.writetask;

import ch.qol.unige.smartphonetest.MainActivity;
import ch.qol.unige.smartphonetest.StepSettings;
import ch.qol.unige.smartphonetest.baseMVC.Controller;

public class WritingTaskController extends Controller 
{
	private int totalNumberBackButtonCancelCharacter = 0;
	
	public WritingTaskController(WritingTaskView view)
	{
		super(view.getIntent().getBooleanExtra(StepSettings.STRESS, false),
				view.getIntent().getIntExtra(StepSettings.REPETITIONS, 1),
				view.getIntent().getIntExtra(StepSettings.MINUTE_DURATION, 1));
		
		this.view = view;
		this.model = new WritingTaskModel(stress, view);
	}
	
	/**
	 * Instantiate a new exercise
	 * @return the text to write
	 */
	public String instantiateExercise()
	{	
		currentRepetition++;
		
		writeRepetition();
		
		totalNumberBackButtonCancelCharacter = 0;
		
		((WritingTaskModel) model).instantiateExercise();
		return ((WritingTaskModel) model).getTextToWrite();
	}
	
	/**
	 * Returns the text to write
	 * @return the text to write from the model
	 */
	public String getTextToWrite()
	{
		return ((WritingTaskModel) model).getTextToWrite();
	}
	
	/**
	 * Method invoked when the user submits his answer. It is responsible to ask
	 * to the model the number of errors made and to call a method of the view to
	 * show the resume dialog
	 * @param text the submitted text
	 */
	public void submittedText(final String text)
	{
		new Thread() {
			
			@Override
			public void run() 
			{
				WritingTaskModel cModel = (WritingTaskModel) model;
				cModel.checkSubmittedAnswer(text, 
						totalNumberBackButtonCancelCharacter);
				
				String[] report = {String.valueOf(currentRepetition), String.valueOf(1),
						String.valueOf(cModel.getTotalWords()), 
						String.valueOf(cModel.getTotalCorrect()), 
						String.valueOf(cModel.getTotalErrors()),
						String.valueOf(totalNumberBackButtonCancelCharacter),
						cModel.getTextToWrite().replace(",", ";"), 
						text.replace(",", ";")};
				
				MainActivity.writeResumeOfExercise(report);
				
				((WritingTaskView) view).showResumeDialog( 
						cModel.getTotalWords(),cModel.getTotalCorrect(), 
						cModel.getTotalErrors());
			}
		}.start();
	}
	
	/**
	 * Returns if the view has to mix fonts while presenting the text to write
	 * @return a boolean to say if the view has to mix fonts or not
	 */
	public boolean mixFonts()
	{
		return ((WritingTaskModel) model).mixFonts();
	}
	
	/**
	 * Method used to notify the controller and the view that the BACK button
	 * has been clicked 
	 */
	public void backButtonClicked()
	{
		totalNumberBackButtonCancelCharacter++;
		((WritingTaskView) view).backButtonClicked();
	}
}
