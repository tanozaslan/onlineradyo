package com.beanie.samples.streaming;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class splash extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splah);
	
		if (AppStatus.getInstance(this).isOnline(this)) {
		    
			Toast t = Toast.makeText(this,"You are online!!!!",8000);
		    t.show();		
			Thread timer = new Thread(){
				public void run(){
					try{
						sleep(5000);			
					}catch (InterruptedException e){
						e.printStackTrace();
					}finally{
						Intent openStartingPoint = new Intent("com.beanie.samples.streaming.index");
						startActivity(openStartingPoint);
					}
				}
			};
			timer.start();
			
		   	} else{  
		          Toast t = Toast.makeText(this,"You are not online!!!!",8000);
		          t.show();
		          Log.v("Home", "############################You are not online!!!!");    
		     }		
	}
}
