package ch.qol.unige.smartphonetest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.util.Log;

public class AsyncUserRegister extends AsyncTask<String, Void, Boolean> {

	private static final String HOST = "http://trainutri.unige.ch/matteo/php/save_new_tester.php";

	@Override
	protected Boolean doInBackground(String... elements) {
		// elements[0]: name
		// elements[1]: email
		// elements[2]: imei
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(HOST);
		
		try {
			List<NameValuePair> data = new ArrayList<NameValuePair>(2);
			data.add(new BasicNameValuePair("name", elements[0]));
			data.add(new BasicNameValuePair("email", elements[1]));
			data.add(new BasicNameValuePair("imei", elements[2]));
			httpPost.setEntity(new UrlEncodedFormEntity(data));
			
			HttpResponse response = httpClient.execute(httpPost);
			Log.d("RESPONSE", response.toString());
			return true;
		}
		catch(ClientProtocolException e) {
			return false;
		}
		catch(IOException exc) 
		{
			return false;
		}
	}
	
	
}
