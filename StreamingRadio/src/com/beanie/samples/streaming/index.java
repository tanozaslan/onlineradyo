package com.beanie.samples.streaming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.greystripe.android.sdk.BannerListener;
import com.greystripe.android.sdk.BannerView;
import com.greystripe.android.sdk.GSSDK;
import com.spoledge.aacdecoder.AACPlayer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

/*FACEBOOK*/
import com.facebook.android.*;
import com.facebook.android.Facebook.*;

public class index extends Activity implements OnClickListener{
	
	Button btnYerli,btnYabanci,btnFvrKanal,btnTags,btnRecs;
	LinearLayout contentLayout;
	private static AACPlayer aacPlayer;
	private static MediaPlayer mPlayer ; 
	private static int mediaType; 
	private int bannerWidth=48;
	private static boolean isPlaying=false;
	private static String artistName="no artist info";
	private static String songName="no song info";
	private static String channelName="choose a radio channel";
	private static String radioURL;
	private static boolean metaDataThreadStop;
	
	private static Thread getMetaData;
	
	/*GREYSTRIPE VARIABLE*/
    private static final int THIRTY_SECONDS = 30000;
    private static final String TAG = "HelloGreystripeBanner";
    private static final String appGreyStripeID = "b5e7b985-bdfe-4cd0-a443-71c94fa7f67b";
    private GSSDK sdk;
    private BannerView myBanner;
    private Handler greyHandler;
    private Runnable refreshBannerTask;
	
    Facebook facebook = new Facebook("180882685356992");
    String FILENAME = "AndroidSSO_data";
    private SharedPreferences mPrefs;
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.index);
		
		initializeUI();	
		//initializeFacebook();
       
        //facebook.dialog(getApplicationContext(), "feed", new PostDialogListener());
        
		
	}
	
	/*FACEBOOK*/
	private void initializeFacebook(){
		/*
         * Get existing access_token if any
         */
		
		initializeFacebook();
        mPrefs = getPreferences(MODE_PRIVATE);
        String access_token = mPrefs.getString("access_token", null);
        long expires = mPrefs.getLong("access_expires", 0);
        if(access_token != null) {
            facebook.setAccessToken(access_token);
        }
        if(expires != 0) {
            facebook.setAccessExpires(expires);
        }
        
        /*
         * Only call authorize if the access_token has expired.
         */
        if(!facebook.isSessionValid()) {

            facebook.authorize(this, new String[] {}, new DialogListener() {
                public void onComplete(Bundle values) {
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("access_token", facebook.getAccessToken());
                    editor.putLong("access_expires", facebook.getAccessExpires());
                    editor.commit();
                }
    
                public void onFacebookError(FacebookError error) {}
    
                public void onError(DialogError e) {}
    
                public void onCancel() {}
            });
        }
	}
	public abstract class BaseDialogListener implements DialogListener {
	    public void onFacebookError(FacebookError e) {
	        e.printStackTrace();
	    }
	    
	    public void onError(DialogError e) {
	        e.printStackTrace();
	    }
	    
	    public void onCancel() {
	    }
	}
	public class PostDialogListener extends BaseDialogListener {
	    
	    public void onComplete(Bundle values) {
	        final String postId = values.getString("post_id");
	        if (postId != null) {
	            Log.d("","Message posted on the wall.");
	        } else {
	            Log.d("","No message posted on the wall.");
	        }
	    }
	}	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebook.authorizeCallback(requestCode, resultCode, data);
    }	
	/*public void sendtoFacebook(){
        facebookClient = new Facebook("<Your_APP_ID");
        facebookClient.authorize(index.this, new AuthorizeListener());
    }
	
	class AuthorizeListener implements DialogListener {
	    public void onComplete(Bundle values) {
	        Bundle parameters = new Bundle();
	            parameters.putString("message", "<Message_you_want_to_send>");// the message to post to the wall
	            facebookClient.dialog(index.this, "stream.publish", parameters, this);// "stream.publish" is an API call
	    }
	    public void onFacebookError(FacebookError e) {
	    }
	    public void onError(DialogError e) {
	    }
	    public void onCancel() {
	    }
	}*/	
	/*FACEBOOK*/
	
	
	
	
	public void initializeUI(){
		contentLayout =(LinearLayout) findViewById(R.id.contentLayout);
		
		btnYerli = (Button) findViewById(R.id.btnYerli);
		btnYerli.setOnClickListener(this);
		
		btnYabanci = (Button) findViewById(R.id.btnYabanci);
		btnYabanci.setOnClickListener(this);
		
		btnFvrKanal = (Button) findViewById(R.id.btnKanalFvr);
		btnFvrKanal.setOnClickListener(this);
		
		btnTags = (Button) findViewById(R.id.btnTags);
		btnTags.setOnClickListener(this);
		
		btnRecs = (Button) findViewById(R.id.btnRecs);
		btnRecs.setOnClickListener(this);
		
		Log.d("Grey","1");
		/*GREYSTRIPE*/
        sdk = GSSDK.initialize(this.getApplicationContext(), appGreyStripeID);
        Log.d("Grey","2");
      //Retrieve the banner and add a listener
        myBanner = (BannerView) findViewById(R.id.gsBanner);
        myBanner.addListener(new HelloBannerListener());
        Log.d("Grey","3");
        //Initialize the handler
        greyHandler = new Handler();
        Log.d("Grey","4");
        //Initialize Runnable task to refresh banner every 30 seconds
        refreshBannerTask = new Runnable() {
            public void run() {
        		Log.d("Grey","5");
                myBanner.refresh();
                greyHandler.postDelayed(refreshBannerTask, THIRTY_SECONDS);
            }
        };
		
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId()){
		case R.id.btnYerli:
			Intent openHomeActivityYerli = new Intent(index.this,radioPlayer.class);		
			openHomeActivityYerli.putExtra("kanalList","yerli");
			startActivity(openHomeActivityYerli);
		break;
		case R.id.btnYabanci:
			Intent openHomeActivityYabanci = new Intent(index.this,radioPlayer.class);
			openHomeActivityYabanci.putExtra("kanalList","yabanci");
			startActivity(openHomeActivityYabanci);
			break;
		case R.id.btnKanalFvr:
			Intent openHomeActivityFavoriKanal = new Intent(index.this,radioPlayer.class);
			openHomeActivityFavoriKanal.putExtra("kanalList","favori");
			startActivity(openHomeActivityFavoriKanal);
			break;
		case R.id.btnTags:
			Intent fvrTagIntent = new Intent(index.this,fvrTags.class);
    		startActivity(fvrTagIntent);
			break;
		}
	}
	
	private static void initializeMediaPlayer() {
    	switch(mediaType){
    	case 1:
    		aacPlayer = new AACPlayer();
    		break;
    	case 2:
            mPlayer = new MediaPlayer();       
    		break;
    	}
    }
	
	static void startPlayingRadio(radioChannel radioChannel){  
        mediaType = radioChannel.getMediaType();
        initializeMediaPlayer();
        channelName = radioChannel.getName();
        radioURL = radioChannel.getURL();
        
        isPlaying=true;
        
        switch(mediaType){
        case 1:
        	aacPlayer.playAsync(radioURL);    	
        	break;
        case 2:
        	try {
                mPlayer.setDataSource(radioURL);      
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mPlayer.setAudioStreamType(AudioManager.STREAM_RING);     
            mPlayer.prepareAsync();                
            mPlayer.setOnPreparedListener(new OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {  
                	mPlayer.start();     
                    //buttonRecord.setEnabled(true);
                }
            });
        	break;
        }
        
        getMetaData = new Thread(){
			public void run(){     				
				getMeta();			    					
			}	
	};
		getMetaData.start();
        
        
        //metaDataThreadStop=true;      
        //This Supposed to be, but changed for testing;
        metaDataThreadStop=false;
	}
	
	static void stopPlayingRadio(){		
		switch(mediaType){
    	case 1:
            aacPlayer.stop();    
    		break;
    	case 2:
            if (mPlayer.isPlaying()){
            	mPlayer.stop();
            	mPlayer.release();
            }
    		break;
    	}
		isPlaying=false;
		metaDataThreadStop=true;
	}
	
	static boolean isRadioPlaying(){
		return isPlaying;
	}

	public static void getMeta(){
        final Timer timer;
	    timer = new Timer();
    	Log.d("getMeta()-MetaData",String.valueOf(metaDataThreadStop));
	    
	    timer.schedule(new TimerTask() {
	        public void run() {
	            URL url;
	            Log.d("getMeta()-run2",String.valueOf(metaDataThreadStop));
	            if(!metaDataThreadStop){
		            try {
		            	Log.d("getMeta()-inTry",String.valueOf(metaDataThreadStop));
		                Log.d("Metadata","MetaData Thread is Working");
		                url = new URL(radioURL);
		                IcyStreamMeta icy = new IcyStreamMeta(url);

		                index.setArtistName(icy.getArtist());		                
		                index.setSongName(icy.getTitle());
		                handler.sendEmptyMessage(0);
		                
		            } catch (MalformedURLException e) {
		                // TODO Auto-generated catch block
		                e.printStackTrace();
		            }catch (IOException e) {
		                // TODO Auto-generated catch block
		                e.printStackTrace();
		            }          	
	            }else{
	            	Log.d("getMeta()-End","Timer Canceled");
	            	timer.cancel();	            	
	            }
	        }
	    }, 0, 30000);
	} 
	
	private static Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {    	 
        	 radioPlayer.setInfoFields();
         }
	};
	
	
	@Override
    public void onStart() {
        super.onStart();
        myBanner.refresh();
        greyHandler.postDelayed(refreshBannerTask, THIRTY_SECONDS);
    }

    
    @Override
    public void onStop() {
        super.onStop();
        greyHandler.removeCallbacks(refreshBannerTask);
    }
	
	
	static void setSongName(String song){
		songName=song;
	}
	static void setArtistName(String artist){
		artistName=artist;
	}
	
	static String getSongName(){
		return songName;
	}
	static String getArtistName(){
		return artistName;
	}
	static String getRadioName(){
		return channelName;
	}

	public static void getMime() {
     
    }


	public void displayAdWrapper(View v) {
        if(sdk.isAdReady()) {
            sdk.displayAd(this);
            Log.v(TAG, "*****SUCCESSFULLY retrieved FULLSCREEN ad");
        } else {
            Log.v(TAG, "*****FAILED to retrieve FULLSCREEN ad");
        }
    }

    /**
     * HelloBannerListener is an inner class that logs
     * successful and failed banner ad retrieval.
     */
    private class HelloBannerListener implements BannerListener {
        public void onFailedToReceiveAd(BannerView v) {
            Log.v(TAG, "*****FAILED to receive BANNER ad");
        }

        public void onReceivedAd(BannerView v) {	        	
        	/// Converts 14 dip into its equivalent px
        	Resources r = getResources();
        	float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bannerWidth, r.getDisplayMetrics());
        	contentLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,getWindowManager().getDefaultDisplay().getHeight()-(int)px*2));
            Log.v(TAG, "*****SUCCESSFULLY received BANNER ad");
        }

    }
}


