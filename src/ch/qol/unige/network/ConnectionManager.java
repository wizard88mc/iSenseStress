package ch.qol.unige.network;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import ch.qol.unige.smartphonetest.MainActivity;
import android.util.Log;

public class ConnectionManager extends WebSocketClient
{
	
	public static final String PACKET_STRING_TYPE = "TYPE";
	public static final String PACKET_STRING_DATA = "DATA";
	public static final String PACKET_STRING_SUB_DATA = "SUB_DATA";
	public static final String PACKET_STRING_DEVICE = "DEVICE";
	public static final String PACKET_STRING_NAME = "NAME";
	public static final String PACKET_STRING_AVATAR = "AVATAR";
	public static final String PACKET_STRING_RANK = "RANK";
	
	public static final String PACKET_STRING_TYPE_IDENTIFICATION = 
			"IDENTIFICATION";
	public static final String PACKET_STRING_TYPE_IDENTIFICATION_COMPLETE = 
			"IDENTIFICATION_COMPLETE";
	public static final String PACKET_STRING_TYPE_NAME_AVATAR = "NAME_AVATAR";
	public static final String PACKET_STRING_TYPE_DEVICE_ACCEPTED = "DEVICE_ACCEPTED";
	public static final String PACKET_STRING_TYPE_START_FIRST_STEP = 
			"START_FIRST_STEP";
	public static final String PACKET_STRING_TYPE_FIRST_STEP_COMPLETED = 
			"END_FIRST_STEP";
	public static final String PACKET_STRING_TYPE_START_SECOND_STEP = 
			"START_SECOND_STEP";
	public static final String PACKET_STRING_TYPE_GAME_COMPLETED = 
			"GAME_COMPLETED";
	public static final String PACKET_STRING_TYPE_FINAL_RANK = 
			"FINAL_RANK";
	public static final String PACKET_STRING_TYPE_BACK_HERE = "BACK_HERE";
	
	public static final String PACKET_STRING_STRESS = "STRESS";
	
	/**
	 * SEARCH TASK MESSAGE PACKETS IDENTIFICATION
	 */
	public static final String PACKET_STRING_TYPE_SEARCH_RESPONSE = 
			"SEARCH_RESPONSE";
	public static final String PACKET_STRING_TYPE_SEARCH_CLICK_DURING_PLAYING = 
			"PLAYING_CLICK";
	public static final String PACKET_STRING_SEARCH_FINAL_RESPONSE = "FINAL_RESPONSE";
	public static final String PACKET_STRING_SEARCH_CORRECT_ICONS = "CORRECT";
	public static final String PACKET_STRING_SEARCH_WRONG_ICONS = "WRONG";
	public static final String PACKET_STRING_SEARCH_MISSING_ICONS = "MISSING";
	
	/**
	 * WRITING TASK MESSAGE PACKETS IDENTIFICATION
	 */
	public static final String PACKET_STRING_TYPE_WRITING_RESPONSE = 
			"WRITING_RESPONSE";
	public static final String PACKET_STRING_TYPE_WRITING_BACK_PRESSED = 
			"BACK_PRESSED";
	public static final String PACKET_STRING_WRITING_FINAL_RESPONSE = "FINAL_RESPONSE";
	public static final String PACKET_STRING_WRITING_CORRECT_WORDS = 
			"CORRECT_WORDS";
	public static final String PACKET_STRING_WRITING_WRONG_WORDS = 
			"WRONG_WORDS";
	
	/**
	 * STRESSOR TASK MESSAGE PACKETS IDENTIFICATION
	 */
	public static final String PACKET_STRING_TYPE_STRESSOR_RESPONSE =
			"STRESSOR_RESPONSE";
	
	private static final String LOG_STRING = "Websocket";
	private static final String SERVER = "ws://trainutri.unige.ch:8081";
	private MainActivity activity = null;
	private PacketsSender mPacketsSender = null;
	
	/**
	 * This boolean indicates if the ConnectionManager object is build 
	 * after a reconnection or for the first stime
	 */
	private boolean reconnected;

	public ConnectionManager(MainActivity activity, boolean reconnected) throws URISyntaxException
	{
		super(new URI(SERVER));
		this.activity = activity;
		this.mPacketsSender = new PacketsSender(this);
		this.reconnected = reconnected;
		this.connect();
	}

	@Override
	public void onOpen(ServerHandshake arg0) 
	{
		Log.d(LOG_STRING, "WebSocket Connected");
	}

	@Override
	public void onMessage(String message) 
	{
		try 
		{
			JSONObject packet = new JSONObject(message);
			
			if (packet.get(ConnectionManager.PACKET_STRING_TYPE)
					.equals(ConnectionManager.PACKET_STRING_TYPE_IDENTIFICATION))
			{
				if (!reconnected)
				{
					mPacketsSender.doInBackground(ConnectionManager.PACKET_STRING_TYPE_IDENTIFICATION);
				}
				else
				{
					mPacketsSender.doInBackground(ConnectionManager.PACKET_STRING_TYPE_BACK_HERE, 
							activity.getNameOfPlayer());
					ch.qol.unige.smartphonetest.baseMVC.View.setConnectionManager(this);
				}
			}
			/**
			 * Packet to inform that identification was completed and the 
			 * MainActivity has to ask to the player name and avatar
			 */
			else if (packet.getString(ConnectionManager.PACKET_STRING_TYPE)
					.equals(ConnectionManager.PACKET_STRING_TYPE_IDENTIFICATION_COMPLETE))
			{
				activity.onConnectionToWebsocketCompletedOpenDialogNameAvatar();
				ch.qol.unige.smartphonetest.baseMVC.View.setConnectionManager(this);
			}
			else if (packet.getString(ConnectionManager.PACKET_STRING_TYPE)
					.equals(ConnectionManager.PACKET_STRING_TYPE_NAME_AVATAR))
			{
				activity.responseAvatarAndName(packet.getBoolean(ConnectionManager.PACKET_STRING_DATA));
			}
			else if (packet.getString(ConnectionManager.PACKET_STRING_TYPE)
					.equals(ConnectionManager.PACKET_STRING_TYPE_DEVICE_ACCEPTED))
			{
				if (!packet.getBoolean(ConnectionManager.PACKET_STRING_DATA))
				{
					this.close();
					activity.showDialogNotThisTime();
				}
			}
			/**
			 * Packet to start the first set of tasks
			 */
			else if (packet.getString(ConnectionManager.PACKET_STRING_TYPE)
					.equals(ConnectionManager.PACKET_STRING_TYPE_START_FIRST_STEP))
			{
				activity.commandToStartFirstStepArrived();	
			}
			else if (packet.getString(ConnectionManager.PACKET_STRING_TYPE)
					.equals(ConnectionManager.PACKET_STRING_TYPE_START_SECOND_STEP))
			{
				activity.startSecondSetOfTaskExercises();
			}
			else if (packet.getString(ConnectionManager.PACKET_STRING_TYPE)
					.equals(ConnectionManager.PACKET_STRING_TYPE_FINAL_RANK))
			{
				int finalPosition = packet.getInt(ConnectionManager.PACKET_STRING_RANK);
				activity.finalRankIsArrived(finalPosition);
			}
		}
		catch(JSONException exc)
		{
			Log.e(LOG_STRING, "Error parsing message: " + message);
			Log.e(LOG_STRING, "Exception: " + exc.toString());
		}
	}

	@Override
	public void onClose(int arg0, String arg1, boolean arg2) 
	{
		activity.websocketConnectionClosed();
	}

	@Override
	public void onError(Exception exc) 
	{
		Log.e(LOG_STRING, exc.toString());
		activity.websocketConnectionClosed();
	}
	
	/**
	 * Sends a message to the server with the chosen name and avatar for the 
	 * player
	 * @param name: the name chosen by the player
	 * @param avatar: the selected avatar (name)
	 */
	public void sendMessageNameAndAvatar(String name, String avatar)
	{
		mPacketsSender.doInBackground(ConnectionManager.PACKET_STRING_TYPE_NAME_AVATAR, 
				name, avatar);
	}
	
	/**
	 * If the connection is first lost and restored, with this method the server
	 * is informed that the device is back
	 * @param name: the previously selected name
	 * @param avatar: the previously selected avatar
	 */
	public void sendMessageDeviceBack(String name, String avatar)
	{
		mPacketsSender.doInBackground(ConnectionManager.PACKET_STRING_TYPE_BACK_HERE, 
				name, avatar);
	}
	
	/*
	 * Sends a message to the server informing that the player has completed 
	 * the first set of exercises
	 */
	public void sendMessageFirstStepCompleted()
	{
		mPacketsSender.doInBackground(ConnectionManager.PACKET_STRING_TYPE_FIRST_STEP_COMPLETED);
	}
	
	/**
	 * Sends a message each time the user click on an icon on the screen during
	 * the search task
	 * @param stress: if the task is in stress or not stress mode
	 * @param correct: if the provided answer is correct or not
	 */
	public void sendMessageSearchTaskIconClicked(Boolean stress, Boolean correct)
	{
		mPacketsSender.doInBackground(ConnectionManager.PACKET_STRING_TYPE_SEARCH_RESPONSE, 
				ConnectionManager.PACKET_STRING_TYPE_SEARCH_CLICK_DURING_PLAYING,
				stress.toString(), correct.toString());
	}
	
	/**
	 * Sends a message at the end of a search task repetition with the 
	 * final result of the performances of the player
	 * @param stress: if the task is in stress or not stress mode
	 * @param correct: the number of correct icons clicked
	 * @param wrong: the number of wrong icons clicked
	 * @param missing: the number of missing icons
	 */
	public void sendMessageSearchTaskFinalResult(Boolean stress, int correct, 
			int wrong, int missing)
	{
		mPacketsSender.doInBackground(ConnectionManager.PACKET_STRING_TYPE_SEARCH_RESPONSE,
				ConnectionManager.PACKET_STRING_SEARCH_FINAL_RESPONSE,
				stress.toString(), String.valueOf(correct), String.valueOf(wrong), 
				String.valueOf(missing));
	}
	
	/**
	 * Sends a message each time the user clicks the back button while writing
	 * @param stress: if the task is in stress or not stress mode
	 */
	public void sendMessageBackButtonClickedWritingTask(Boolean stress)
	{
		mPacketsSender.doInBackground(ConnectionManager.PACKET_STRING_TYPE_WRITING_RESPONSE, 
				ConnectionManager.PACKET_STRING_TYPE_WRITING_BACK_PRESSED, 
				stress.toString());
	}
	
	/**
	 * Sends a message at the end of the writing task repetition with the 
	 * final result 
	 * @param stress: if the task is in stress or not stress mode
	 * @param correctWords: the number of correct words written
	 * @param wrongWords: the number of wrong words written
	 */
	public void sendMessageWritingTaskFinalResult(Boolean stress, 
			int correctWords, int wrongWords)
	{
		mPacketsSender.doInBackground(ConnectionManager.PACKET_STRING_TYPE_WRITING_RESPONSE, 
				ConnectionManager.PACKET_STRING_WRITING_FINAL_RESPONSE, 
				stress.toString(), String.valueOf(correctWords), 
				String.valueOf(wrongWords));
	}
	
	/**
	 * Sends a message each time the user submits and answer during the stressor
	 * task
	 * @param correct: it the answer is correct or not
	 */
	public void sendMessageStressorTaskAnswerSubmitted(Boolean correct)
	{
		mPacketsSender.doInBackground(ConnectionManager.PACKET_STRING_TYPE_STRESSOR_RESPONSE, 
				correct.toString());
	}
	
	/**
	 * Sends a message when the user completes the online game
	 */
	public void sendMessageEverythingCompleted()
	{
		mPacketsSender.doInBackground(ConnectionManager.PACKET_STRING_TYPE_GAME_COMPLETED);
	}
	
}
