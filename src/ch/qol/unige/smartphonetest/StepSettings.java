package ch.qol.unige.smartphonetest;

/**
 * This class holds the settings for a particular exercise, i.e. the type, 
 * the duration, if stress or not
 * @author Matteo Ciman
 *
 */
public class StepSettings {

	public enum ExerciseType {SURVEY, RELAX, SEARCH, WRITE, STRESSOR, 
		WAIT_SECOND_STEP};
	
	public static final String STRESS = "stress";
	public static final String REPETITIONS = "repetitions";
	public static final String MINUTE_DURATION = "minute_duration";

	private ExerciseType exerciseType;
	private int exerciseNumber = -1;
	private boolean stress;
	private int repetitions;
	private int minuteDuration;
	
	/**
	 * Constructor used for the SURVEY enum, since no stress, repetition or 
	 * minute duration are required
	 * @param exerciseType is ExerciseType.SURVEY
	 */
	public StepSettings(ExerciseType exerciseType, int exerciseNumber)
	{
		this(exerciseType, exerciseNumber, false, -1, -1);
	}
	
	/**
	 * Constructor used for the RELAX enum, since no stress or repetition are
	 * required, but only time length
	 * @param exerciseType is ExerciseType.REALX
	 * @param minuteDuration the duration in minutes
	 */
	public StepSettings(ExerciseType exerciseType, int exerciseNumber, 
			int minuteDuration)
	{
		this(exerciseType, exerciseNumber, false, -1, minuteDuration);
	}
	
	/**
	 * Constructor to setup an exercise, if repetition != -1 => minute duration = -1
	 * or viceversa
	 * @param exerciseType the type of exercise
	 * @param stress if stressed or not
	 * @param repetitions the number of repetition of the same exercise
	 * @param minuteDuration the total length in minutes (this is the lower bound)
	 */
	public StepSettings(ExerciseType exerciseType, int exerciseNumber, 
			boolean stress, int repetitions, int minuteDuration)
	{
		this.exerciseType = exerciseType; this.stress = stress; 
		this.exerciseNumber = exerciseNumber;
		this.repetitions = repetitions; this.minuteDuration = minuteDuration;
	}
	
	/**
	 * Returns the enum of the current exercise
	 * @return the exercise type
	 */
	public ExerciseType getExerciseType()
	{
		return this.exerciseType;
	}
	
	/**
	 * Returns the number of the current exercise in the global order of the eercises
	 * @return the number in the list of exercises
	 */
	public int getExerciseNumber()
	{
		return this.exerciseNumber;
	}
	
	/**
	 * Returns if the exercise is stressed or not
	 * @return if stress or not
	 */
	public boolean getStress()
	{
		return this.stress;
	}
	
	/**
	 * Returns the number of repetition for this exercise
	 * @return the number of repetition
	 */
	public int getRepetions()
	{
		return this.repetitions;
	}
	
	/**
	 * Returns the length of the exercises if a time duration is used
	 * @return the length in minutes
	 */
	public int getMinuteDuration()
	{
		return this.minuteDuration;
	}
	
	@Override
	public String toString()
	{
		String string = "************************************" 
				+ System.getProperty("line.separator");
		
		string += "Exercise " + exerciseNumber + ": ";
		switch(exerciseType) {
		case RELAX: 
		{
			string += "RELAX";
			break;
		}
		case SEARCH: 
		{
			string += "SEARCH";
			break;
		}
		case STRESSOR: 
		{
			string += "STRESSOR";
			break;
		}
		case SURVEY: 
		{
			string += "SURVEY";
			break;
		}
		case WAIT_SECOND_STEP: 
		{
			string += "WAIT_SECOND_STEP";
			break;
		}
		case WRITE: 
		{
			string += "WRITE";
			break;
		}
		}
		
		string += "(" + String.valueOf(stress) + ") ";
		if (minuteDuration != -1)
		{
			string += "DURATION: " + minuteDuration + "'";
		}
		else if (repetitions != -1)
		{
			string += "DURATION: x" + repetitions;
		}
		
		return string;
	}
	
	public String toStringBasic()
	{
		switch(exerciseType) {
		case RELAX: 
		{
			return "RELAX,"+stress;
		}
		case SEARCH: 
		{
			return "SEARCH,"+stress;
		}
		case STRESSOR: 
		{
			return "STRESSOR,"+stress;
		}
		case SURVEY: 
		{
			return "SURVEY,"+stress;
		}
		case WRITE: 
		{
			return "WRITE,"+stress;
		}
		case WAIT_SECOND_STEP: 
		{
			return "WAIT_SECOND_STEP,false";
		}
		default: 
		{
			return "";
		}
		}
	}
}
