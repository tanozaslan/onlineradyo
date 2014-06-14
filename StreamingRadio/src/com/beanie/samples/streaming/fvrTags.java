package com.beanie.samples.streaming;

import java.util.Map;

import com.greystripe.android.sdk.BannerListener;
import com.greystripe.android.sdk.BannerView;
import com.greystripe.android.sdk.GSSDK;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class fvrTags extends Activity{
	private String[] favoriteArtists;
    private String[] favoriteSongsList;
    private Map<String, ?> favoriteSongsMap;
    
    private ListView list;
    private LinearLayout contentLayout;
    
    private ArrayAdapter ad;
    
    /*GREYSTRIPE VARIABLE*/
    private static final int THIRTY_SECONDS = 30000;
    private static final String TAG = "HelloGreystripeBanner";
    private static final String appGreyStripeID = "b5e7b985-bdfe-4cd0-a443-71c94fa7f67b";
    private GSSDK sdk;
    private BannerView myBanner;
    private Handler greyHandler;
    private Runnable refreshBannerTask;
    private int bannerWidth=48;
    
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fvrtags);
		initializeUI();
		showTags();
	}
	private void initializeUI() {	
		
		contentLayout =(LinearLayout) findViewById(R.id.contentLayout);

		
		getFavoriteTags();
		
		/*GREYSTRIPE*/
        sdk = GSSDK.initialize(this.getApplicationContext(), appGreyStripeID);
      //Retrieve the banner and add a listener
        myBanner = (BannerView) findViewById(R.id.gsBanner);
        myBanner.addListener(new HelloBannerListener());
        //Initialize the handler
        greyHandler = new Handler();
        //Initialize Runnable task to refresh banner every 30 seconds
        refreshBannerTask = new Runnable() {
            public void run() {
        		Log.d("Grey","5");
                myBanner.refresh();
                greyHandler.postDelayed(refreshBannerTask, THIRTY_SECONDS);
            }
        };
		
	}
	
	private void showTags(){
		ad = new ArrayAdapter(this,R.layout.singletag,R.id.label,favoriteSongsList);
        list = (ListView)findViewById(R.id.list);
        list.setAdapter(ad);    
        list.setOnItemClickListener(new OnItemClickListener(){ 
        	public void onItemClick(AdapterView arg0, View arg1, int arg2,long arg3){
        		// TODO Auto-generated method stub
        		Log.d(String.valueOf(arg1),String.valueOf(arg2));
        		deleteTag(arg2);
        		}
        	}
        );
	}
  
	protected void deleteTag(int arg2) {
		SharedPreferences favoriteTags = getSharedPreferences("favorite_songs_list", 0);
        SharedPreferences.Editor favoriteTagsEditor = favoriteTags.edit();

		Toast.makeText(this, favoriteArtists[arg2]+" removed From Tags", Toast.LENGTH_LONG).show();
		favoriteTagsEditor.remove(favoriteArtists[arg2]);  	
		favoriteTagsEditor.commit();
		getFavoriteTags();
		//list.invalidateViews();
		showTags();
	}
	
	private void getFavoriteTags(){
	    SharedPreferences favoriteSongs = getSharedPreferences("favorite_songs_list", 0);
    	favoriteSongsMap=favoriteSongs.getAll();
    	int i=0;    	
    	favoriteArtists = new String[favoriteSongsMap.size()];	    
    	favoriteSongsList = new String[favoriteSongsMap.size()];   
    	for(String s : favoriteSongsMap.keySet()){
    		favoriteArtists[i]=s;
    		favoriteSongsList[i]=favoriteSongsMap.get(s).toString();    
    		favoriteSongsList[i]=favoriteSongsList[i].replace("?&", " - ");
    		
    		
    	    Log.d(favoriteArtists[i],favoriteSongsList[i]);
    	    Log.d("",String.valueOf(favoriteSongsList[i].indexOf("?&")));
    	    i++;
    	    }
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
    
}
