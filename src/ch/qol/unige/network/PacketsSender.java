package ch.qol.unige.network;

import android.os.AsyncTask;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

public class PacketsSender extends AsyncTask<String, Void, Void> 
{	
	private static final String LOG_STRING = "PacketSender";
	
	private WebSocketClient mWebSocketClient = null;
	
	public PacketsSender(WebSocketClient client)
	{
		this.mWebSocketClient = client;
	}
	
	@Override
	protected Void doInBackground(String... params) 
	{
		
		/**
		 * params[0] is the array of the data to send, params[0][0] is the 
		 * type of the packet to send
		 */
		try
		{
			JSONObject packetToSend = new JSONObject();
			
			/**
			 * TYPE: IDENTIFICATION
			 * DATA: DEVICE
			 */
			if (params[0].equals(ConnectionManager.PACKET_STRING_TYPE_IDENTIFICATION))
			{
				packetToSend.put(ConnectionManager.PACKET_STRING_TYPE, 
						ConnectionManager.PACKET_STRING_TYPE_IDENTIFICATION);
				packetToSend.put(ConnectionManager.PACKET_STRING_DATA
						, ConnectionManager.PACKET_STRING_DEVICE);
			}
			/**
			 * TYPE: NAME_AVATAR
			 * NAME: name (params[1])
			 * AVATAR: avatar (params[2])
			 */
			else if (params[0].equals(ConnectionManager.PACKET_STRING_TYPE_NAME_AVATAR))
			{
				packetToSend.put(ConnectionManager.PACKET_STRING_TYPE, 
						ConnectionManager.PACKET_STRING_TYPE_NAME_AVATAR);
				packetToSend.put(ConnectionManager.PACKET_STRING_NAME, params[1]);
				packetToSend.put(ConnectionManager.PACKET_STRING_AVATAR, params[2]);
			}
			/**
			 * TYPE: BACK_HERE
			 * NAME: name (params[1])
			 */
			else if (params[0].equals(ConnectionManager.PACKET_STRING_TYPE_BACK_HERE))
			{
				packetToSend.put(ConnectionManager.PACKET_STRING_TYPE, 
						ConnectionManager.PACKET_STRING_TYPE_BACK_HERE);
				packetToSend.put(ConnectionManager.PACKET_STRING_NAME, params[1]);
			}
			/**
			 * TYPE: FIRST_STEP_COMPLETED
			 */
			else if (params[0].equals(ConnectionManager.PACKET_STRING_TYPE_FIRST_STEP_COMPLETED))
			{
				packetToSend.put(ConnectionManager.PACKET_STRING_TYPE, 
						ConnectionManager.PACKET_STRING_TYPE_FIRST_STEP_COMPLETED);
			}
			/**
			 * TYPE: SEARCH_RESPONSE
			 */
			else if (params[0].equals(ConnectionManager.PACKET_STRING_TYPE_SEARCH_RESPONSE))
			{
				/**
				 * DATA: PLAYING_CLICK (params[1])
				 * STRESS: true/false (params[2])
				 * SUB_DATA: true/false (params[3])
				 */
				if (params[1].equals(ConnectionManager.PACKET_STRING_TYPE_SEARCH_CLICK_DURING_PLAYING))
				{
					packetToSend.put(ConnectionManager.PACKET_STRING_TYPE, 
							ConnectionManager.PACKET_STRING_TYPE_SEARCH_RESPONSE);
					packetToSend.put(ConnectionManager.PACKET_STRING_DATA, 
							ConnectionManager.PACKET_STRING_TYPE_SEARCH_CLICK_DURING_PLAYING);
					packetToSend.put(ConnectionManager.PACKET_STRING_STRESS, 
							Boolean.valueOf(params[2]));
					packetToSend.put(ConnectionManager.PACKET_STRING_SUB_DATA, 
							Boolean.valueOf(params[3]));
				}
				/**
				 * DATA: FINAL_RESPONSE (params[1])
				 * STRESS: true/false (params[2])
				 * CORRECT: correct icons (params[3])
				 * WRONG: wrong icons clicked (params[4])
				 * MISSING: number missing icons (params[5])
				 */
				else 
				{
					packetToSend.put(ConnectionManager.PACKET_STRING_TYPE, 
							ConnectionManager.PACKET_STRING_TYPE_SEARCH_RESPONSE);
					packetToSend.put(ConnectionManager.PACKET_STRING_DATA, 
							ConnectionManager.PACKET_STRING_SEARCH_FINAL_RESPONSE);
					packetToSend.put(ConnectionManager.PACKET_STRING_STRESS, 
							Boolean.valueOf(params[2]));
					packetToSend.put(ConnectionManager.PACKET_STRING_SEARCH_CORRECT_ICONS, 
							Integer.valueOf(params[3]));
					packetToSend.put(ConnectionManager.PACKET_STRING_SEARCH_WRONG_ICONS, 
							Integer.valueOf(params[4]));
					packetToSend.put(ConnectionManager.PACKET_STRING_SEARCH_MISSING_ICONS, 
							Integer.valueOf(params[5]));
				}
			}
			/**
			 * TYPE: WRITING_RESPONSE
			 */
			else if (params[0].equals(ConnectionManager.PACKET_STRING_TYPE_WRITING_RESPONSE))
			{
				/**
				 * DATA: BACK_PRESSED: (params[1])
				 * STRESS: true/false (params[2])
				 */
				if (params[1].equals(ConnectionManager.PACKET_STRING_TYPE_WRITING_BACK_PRESSED))
				{
					packetToSend.put(ConnectionManager.PACKET_STRING_TYPE, 
							ConnectionManager.PACKET_STRING_TYPE_WRITING_RESPONSE);
					packetToSend.put(ConnectionManager.PACKET_STRING_DATA, 
							ConnectionManager.PACKET_STRING_TYPE_WRITING_BACK_PRESSED);
					packetToSend.put(ConnectionManager.PACKET_STRING_STRESS, 
							Boolean.valueOf(params[2]));
				}
				/**
				 * DATA: FINAL_RESPONSE (params[1])
				 * STRESS: true/false (params[2])
				 * CORRECT_WORDS: number correct words (params[3])
				 * WRONG_WORDS: number wrong words (params[4])
				 */
				else 
				{
					packetToSend.put(ConnectionManager.PACKET_STRING_TYPE, 
							ConnectionManager.PACKET_STRING_TYPE_WRITING_RESPONSE);
					packetToSend.put(ConnectionManager.PACKET_STRING_DATA, 
							ConnectionManager.PACKET_STRING_WRITING_FINAL_RESPONSE);
					packetToSend.put(ConnectionManager.PACKET_STRING_STRESS, 
							Boolean.valueOf(params[2]));
					packetToSend.put(ConnectionManager.PACKET_STRING_WRITING_CORRECT_WORDS, 
							Integer.valueOf(params[3]));
					packetToSend.put(ConnectionManager.PACKET_STRING_WRITING_WRONG_WORDS, 
							Integer.valueOf(params[4]));
				}
			}
			/**
			 * TYPE: STRESSOR_RESPONSE
			 */
			else if (params[0].equals(ConnectionManager.PACKET_STRING_TYPE_STRESSOR_RESPONSE))
			{
				packetToSend.put(ConnectionManager.PACKET_STRING_TYPE, 
						ConnectionManager.PACKET_STRING_TYPE_STRESSOR_RESPONSE);
				packetToSend.put(ConnectionManager.PACKET_STRING_DATA, 
						Boolean.valueOf(params[1]));
			}
			else if (params[0].equals(ConnectionManager.PACKET_STRING_TYPE_GAME_COMPLETED))
			{
				packetToSend.put(ConnectionManager.PACKET_STRING_TYPE, 
						ConnectionManager.PACKET_STRING_TYPE_GAME_COMPLETED);
			}
			
			mWebSocketClient.send(packetToSend.toString());
		}
		catch(JSONException exc)
		{
			Log.e(LOG_STRING, "Problem sending packet: " + exc.toString());
		}
		return null;
	}
}
