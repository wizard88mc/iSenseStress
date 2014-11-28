package ch.qol.unige.smartphonetest;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.io.File;

import android.os.AsyncTask;
import android.util.Log;

public class AsyncFileSender extends AsyncTask<File[], Void, Integer> {

	private static final String SERVER = "trainutri.unige.ch";
	private static final String USERNAME = "matteo";
	private static final String PASSWORD = "mcIman47!";
	private static final String FOLDER = "iSenseStress";
	
	protected long totalSizeToSend = 0;
	protected long totalSizeSent = 0;
	
	protected int totalFileToSent = 0;
	protected int sentFileCounter = 0;
	
	public interface AsyncFileSenderInterface
	{
		public void sentFileCompleted(boolean allFilesSent);
		public void updatePercentage(int percentageValue);
	}
	
	private AsyncFileSenderInterface delegate = null;
	
	public AsyncFileSender(AsyncFileSenderInterface delegate)
	{
		this.delegate = delegate;
	}
	
	@Override
	protected Integer doInBackground(File[]... params) 
	{
		totalFileToSent = params[0].length;
		for (int i = 0; i < params[0].length; i++)
		{
			totalSizeToSend += params[0][i].length();
		}
		
		FTPClient client = new FTPClient();
		
		try {	
			client.connect(SERVER, 21);
			client.login(USERNAME, PASSWORD);
			client.changeDirectory(FOLDER);
			
			for (int i = 0; i < params[0].length; i++)
			{	
				client.upload(params[0][i], new FileTransferListener(delegate, this));
			}
		}
		catch(Exception exc)
		{
			exc.printStackTrace();
			try
			{
				client.disconnect(true);
			}
			catch(Exception exc2)
			{
				exc.printStackTrace();
			}
			return -1;
		}
		try
		{
			client.disconnect(true);
		}
		catch(Exception exc)
		{
			exc.printStackTrace();
		}
		return sentFileCounter;
	}
	
	@Override
	protected void onPostExecute(Integer sentFiles)
	{
		delegate.sentFileCompleted(totalFileToSent == sentFiles);	
	}
	
	private class FileTransferListener implements FTPDataTransferListener
	{
		private AsyncFileSenderInterface delegate = null;
		private AsyncFileSender sender = null;
		
		public FileTransferListener(AsyncFileSenderInterface delegate, 
				AsyncFileSender sender)
		{
			this.delegate = delegate; this.sender = sender;
		}

		@Override
		public void aborted() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void completed() {
			sentFileCounter++;
		}

		@Override
		public void failed() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void started() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void transferred(int dataSent) {
			sender.totalSizeSent += dataSent;
			Log.d("TOTAL_SIZE_SENT", String.valueOf(sender.totalSizeSent));
			Log.d("PERCENTAGE", String.valueOf(sender.totalSizeSent * 100 / sender.totalSizeToSend));
			delegate.updatePercentage((int)(sender.totalSizeSent * 100 / sender.totalSizeToSend));
		}
	}
}
