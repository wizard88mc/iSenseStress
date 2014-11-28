package ch.qol.unige.smartphonetest.relax;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ch.qol.unige.smartphonetest.R;
import ch.qol.unige.smartphonetest.MainActivity;
import ch.qol.unige.smartphonetest.StepSettings;
import android.app.Activity;
import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

public class Relax extends Activity {

	private int timeDuration = -1;
	private MediaPlayer player = null;
	
	private int[] imagesSource = {R.drawable.p044, R.drawable.p045, R.drawable.p046,
			R.drawable.p056, R.drawable.p077, R.drawable.p081, R.drawable.p085, 
			R.drawable.p113, R.drawable.p114, R.drawable.p122, R.drawable.p127, 
			R.drawable.p128};
	
	ScheduledThreadPoolExecutor executorRandomImages = null;
	ScheduledExecutorService executor = null;
	ScheduledThreadPoolExecutor executorCloseDialog = null;
	AlertDialog dialogWelcomeMessage = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.relaxing_layout);
		
		Bundle extras = getIntent().getExtras();
		timeDuration = extras.getInt(StepSettings.MINUTE_DURATION);
		
		player = MediaPlayer.create(this, R.raw.relaxing);
		player.start();
		
		((ImageView) findViewById(R.id.imageRelax))
			.setImageResource(imagesSource[(int)(Math.random() * imagesSource.length)]);
		
		executor = new ScheduledThreadPoolExecutor(1);
		executor.scheduleWithFixedDelay(new Runnable() {
			
			@Override
			public void run() {
				
				player.stop(); player.release(); player = null;
				
				if (Relax.this.getParent() == null)
				{
					setResult(RESULT_OK);
					executorRandomImages.shutdownNow();
					executor.shutdown();
				}
				else 
				{
					getParent().setResult(RESULT_OK);
				}
				Relax.this.finish();
			}
		}, timeDuration, timeDuration, TimeUnit.MINUTES);
		
		executorRandomImages = new ScheduledThreadPoolExecutor(1);
		executorRandomImages.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						((ImageView) Relax.this.findViewById(R.id.imageRelax))
						.setImageResource(imagesSource[(int)(Math.random() * imagesSource.length)]);
					}
				});
			}
		}, timeDuration * 60 / imagesSource.length, timeDuration * 60 / imagesSource.length, 
				TimeUnit.SECONDS);
		
		AlertDialog.Builder builderDialogWelcomeMessage = new AlertDialog.Builder(Relax.this);
		builderDialogWelcomeMessage.setTitle(R.string.relax);
		builderDialogWelcomeMessage.setMessage(R.string.relax_text);
		builderDialogWelcomeMessage.setCancelable(false);
	
		dialogWelcomeMessage = builderDialogWelcomeMessage.create();
		dialogWelcomeMessage.setCancelable(false);
		dialogWelcomeMessage.setCanceledOnTouchOutside(false);
		
		executorCloseDialog = new ScheduledThreadPoolExecutor(1);
		executorCloseDialog.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
		executorCloseDialog.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Relax.this.dialogWelcomeMessage.dismiss();
						Relax.this.executorCloseDialog.shutdown();
						Relax.this.executorCloseDialog = null;
					}
				});
			}
		}, 5, 5, TimeUnit.SECONDS);
		
		dialogWelcomeMessage.show();
		
	}
}
