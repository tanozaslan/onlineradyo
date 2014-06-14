package com.beanie.samples.streaming;

//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.beanie.samples.streaming.R.color;
import com.greystripe.android.sdk.BannerListener;
import com.greystripe.android.sdk.BannerView;
import com.greystripe.android.sdk.GSSDK;

//import java.io.InputStream;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLConnection;

//import android.app.Activity;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.graphics.Color;

import android.media.AudioManager;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Handler;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class radioPlayer extends ListActivity implements OnClickListener, OnGesturePerformedListener{
    
    private String[] radioNames;
    private String[] radioUrls; 
    private String[] favoriteRadioNames;
    private String[] favoriteRadioUrls;
    private String[] favoriteArtists;
    private String[] favoriteSongsList;
    private String selectedRadioUrl="http://46.20.4.58:8040/";
    private String selectedRadioName="Select a radio station";
    private String stopMsg;
    private String welcomeMsg;
    private String noServerConnectionMsg;
    private String channelListButton;
    private String channels;
    private static String artistString;
	private static String songString;
    private int mediaType=1;
    private int channelList=1;
    private int bannerWidth=48;
    private int listIndex,listTop;
    private static boolean getInfo;
    
    private String[] notWorkingRadios=new String[]{"SuperFM","KralTŸrk","JoyTŸrk","Mydonose TŸrk","Radyo ON","JoyFM"};
      
    private String JSONUrl="http://wcomp.rpfusion.com/yerli.json"; 
    private JSONObject JSONStations;
       
    private SeekBar volumeBar;
    private Button buttonChannelList;
    private Button buttonStopPlay; 
    private Button buttonBack; 
    private Button buttonFvrSong;
    private Button buttonTags;
    private Button buttonGuncelle;
    private TextView stationView;
	private static TextView infoView;
	private ListView listView;
	private LinearLayout llInfo,llList,contentLayout;
	
	
	/*static Integer images;
	private LayoutInflater layoutx;
	private Vector<RowData> listValue;
	RowData rd;
	Integer favIMG = R.drawable.icon;
	Integer noFavIMG = R.drawable.channeldummy2;
	Integer[] favChannelIMG;*/
	
	/*ANIMATION VARIABLE*/
	private static final int ANIMATION_DURATION = 1000;
    private View mSlidingLayout;
    private View mLeftView;
    private View mRightView;
    private boolean mAnimating = false;
    private boolean mLeftExpand = true;
    private float mLeftStartWeight;
    private float mLayoutWeightSum;
    private Handler mAnimationHandler = new Handler();
    private long mAnimationTime;
    
    /*GREYSTRIPE VARIABLE*/
    private static final int THIRTY_SECONDS = 30000;
    private static final String TAG = "HelloGreystripeBanner";
    private static final String appGreyStripeID = "b5e7b985-bdfe-4cd0-a443-71c94fa7f67b";
    private GSSDK sdk;
    private BannerView myBanner;
    private Handler greyHandler;
    private Runnable refreshBannerTask;

    //private Button buttonRecord;
    //private Button buttonStopRecord;
   
    private radioChannel selectedChannel;
    
    private Map<String, ?> favoriteChannelsMap;
    private Map<String, ?> favoriteSongsMap;
    private GestureLibrary mLibrary;
        
    private SharedPreferences favoriteChannels;
    private SharedPreferences favoriteSongs;
    private SharedPreferences yerliKanallar; 
    private SharedPreferences yabanciKanallar;
    
    
    
    //private InputStream recordingStream;
    //private RecorderThread recorderThread;
    //private boolean isRecording = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {

    	favoriteChannels = getSharedPreferences("favorite_channels_list", 0);
    	favoriteSongs = getSharedPreferences("favorite_songs_list", 0);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);  
        initializeUIElements();
        setChannelChoice();
        
        if(index.isRadioPlaying()){
   	 		setInfoFields();
   	 		toggleExpand(false);
   	 	}
   	 	else
   	        toggleExpand(true);
             
        if(isChannelPrefEmpty()) refreshChannelList();
        
        getChannelList();
        
        mLibrary = GestureLibraries.fromRawResource(this, R.raw.radiogestures);
        if (!mLibrary.load()) {
        	finish();
        }

        //GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
        //gestures.addOnGesturePerformedListener(this);
        //gestures.setGestureColor(Color.TRANSPARENT);
                        
        //initializeMediaPlayer();
        
        stationView.setText(index.getRadioName());
        Log.d("ChooseChannel List 1", String.valueOf(channelList));
        chooseChannelDisplay();     		 	
    }

    private void initializeUIElements() {
	
    	stopMsg = getResources().getString(R.string.stopMsg);
        welcomeMsg=getResources().getString(R.string.welcomeMsg);
        
        noServerConnectionMsg=getResources().getString(R.string.noServerConnection);
        
        
        contentLayout = (LinearLayout) findViewById(R.id.contentLayout);
        
        llInfo =(LinearLayout) findViewById(R.id.llInfo);
        llList =(LinearLayout) findViewById(R.id.llList);
        
        mSlidingLayout = findViewById(R.id.slide_layout);

        mLeftStartWeight = ((LinearLayout.LayoutParams)
        		llList.getLayoutParams()).weight;
        mLayoutWeightSum = ((LinearLayout) mSlidingLayout).getWeightSum();
              
    	stationView = (TextView) findViewById(R.id.stationView);
    	
    	infoView = (TextView) findViewById(R.id.infoView);
    	
    	//channelList = (TextView) findViewById(R.id.channelList);
    	//channelList.setOnClickListener(this);
    	
    	//likeCheckBox =(CheckBox) findViewById(R.id.likeCheckBox);
    	//likeCheckBox.setOnClickListener(this);
    	
    	/*VOLUME BAR*/
        volumeBar = (SeekBar) findViewById(R.id.volumeBar);      
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeBar.setMax(maxVolume);
        volumeBar.setProgress(curVolume);
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            public void onStartTrackingTouch(SeekBar arg0) {
            }

            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, arg1, 0);
            }
        });
        
        buttonChannelList = (Button) findViewById(R.id.buttonChannelList);
        buttonChannelList.setOnClickListener(this);

        buttonBack =(Button) findViewById(R.id.buttonBack); 
        buttonBack.setOnClickListener(this);
        
        buttonFvrSong =(Button) findViewById(R.id.buttonFvrSong);
        buttonFvrSong.setOnClickListener(this);
        
        buttonTags =(Button) findViewById(R.id.buttonTags);
        buttonTags.setOnClickListener(this);
        
        buttonGuncelle =(Button) findViewById(R.id.buttonGuncelle);
        buttonGuncelle.setOnClickListener(this);

        getFavoritChannels();
        if (favoriteChannelsMap.isEmpty()){
        	buttonChannelList.setEnabled(false);
        }else{
        	buttonChannelList.setEnabled(true);
        }

        buttonStopPlay = (Button) findViewById(R.id.buttonStopPlay);
        if (index.isRadioPlaying())
        	buttonStopPlay.setEnabled(true);
        else
        	buttonStopPlay.setEnabled(false);

        buttonStopPlay.setOnClickListener(this);
        
        
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
                myBanner.refresh();
                greyHandler.postDelayed(refreshBannerTask, THIRTY_SECONDS);
            }
        };

        
        /*buttonRecord = (Button) findViewById(R.id.buttonRecord);
        buttonRecord.setOnClickListener(this);

        buttonStopRecord = (Button) findViewById(R.id.buttonStopRecord);
        buttonStopRecord.setOnClickListener(this);*/
    }

    @Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//super.onBackPressed();
    	//Toast t = Toast.makeText(this,"Back Button",8000);
	    //t.show();
    	Intent info = new Intent(getBaseContext(),index.class);
	    //moveTaskToBack(true);
	    info.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    radioPlayer.this.startActivity(info);
	}
    
	private void setChannelChoice(){
    	Bundle extras = getIntent().getExtras();
        if(extras !=null) {
        	channels = extras.getString("kanalList");  
        	if(channels.compareTo("yerli")==0){
                JSONUrl = getResources().getString(R.string.JSONUrlYerli);
                channelList = 1;
                channelListButton="normalList";
        	}
        	else if (channels.compareTo("yabanci")==0){
                JSONUrl = getResources().getString(R.string.JSONUrlYabanci);
                channelList = 1;
                channelListButton="normalList";
        	}
        	else if(channels.compareTo("favori")==0){
        		channelList = 2;  
                channelListButton="favoriteList";
        	}

        }else{
        	
        }
    }
    
    private void chooseChannelDisplay(){
    	switch(channelList){
    	case 1:
    		showChannelsList();
    		//channelListButton="favoriteList";
    	break;
    	case 2:
    		showFavoriteChannelList();    
    		//channelListButton="normalList";
    	break;
    	}  	
    }
    
    private void parseJSONtoArray(JSONObject JSONText){
        List<String> radioNamesArrayList = new ArrayList<String>();
        List<String> radioURLsArrayList = new ArrayList<String>();      
        try {
        	JSONObject KanallarObject = JSONText.getJSONObject("Kanallar");
	        //String versionString = KanallarObject.getString("Version");
	        JSONArray kanalArray = KanallarObject.getJSONArray("Kanal");
			for(int i=0;i < kanalArray.length();i++){  
				radioNamesArrayList.add(kanalArray.getJSONObject(i).getString("Isim").toString());
				radioURLsArrayList.add(kanalArray.getJSONObject(i).getString("Adres").toString());
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e("log_tag", "Error parsing data "+e.toString());		
		}
    	radioNames = radioNamesArrayList.toArray(new String[radioNamesArrayList.size()]);
    	radioUrls = radioURLsArrayList.toArray(new String[radioURLsArrayList.size()]);
    }
    
    public void onClick(View v) {
    	switch (v.getId()){
    	case R.id.buttonBack:    
		    Intent info = new Intent(getBaseContext(),index.class);
		    info.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		    startActivityForResult(info,0);
		    break;
    	case R.id.buttonChannelList:
    		if (channelListButton.compareTo("normalList")==0){
    			showFavoriteChannelList();
    			channelListButton="favoriteList";
    		}else if(channelListButton.compareTo("favoriteList")==0){
    			showChannelsList();
    			channelListButton="normalList";
    			Log.d("ChannelListButton Fav",channelListButton);
    		}
    		break;
    	case R.id.buttonStopPlay:
            stopPlaying();
            break; 
    	case R.id.buttonFvrSong:
    		addFvrSong();
    		break;
    	case R.id.buttonTags:
    		Intent fvrTagIntent = new Intent(radioPlayer.this,fvrTags.class);
    		startActivity(fvrTagIntent);
    		break;
    	}
    	    	
    	/*else if (v == buttonRecord) {
            recorderThread = new RecorderThread();
            recorderThread.start();

            buttonRecord.setEnabled(false);
            buttonStopRecord.setEnabled(true);
        } else if (v == buttonStopRecord) {
            stopRecording();
        }*/
    }
    
	private void startPlaying() { 	
        buttonStopPlay.setEnabled(true);

        toggleExpand(false);
        if(index.isRadioPlaying()){
        	index.stopPlayingRadio();
        }
        
        index.startPlayingRadio(selectedChannel);
    	stationView.setText(index.getRadioName()); 
    	//chooseChannelList();
    }

    private void stopPlaying() {
    	//stationView.setText(stopMsg);
        index.stopPlayingRadio();     
      
        toggleExpand(true);
       
    	buttonChannelList.setEnabled(true);
        buttonStopPlay.setEnabled(false);        
        //buttonRecord.setEnabled(false);
        //buttonStopRecord.setEnabled(false);
        /*stopRecording();*/
    }
        
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		//super.onListItemClick(l, v, position, id);
    	String selection = l.getItemAtPosition(position).toString();
    	//Toast.makeText(this, selection, Toast.LENGTH_LONG).show();
    	    
    	
    	infoView.setText("looking for info");
    	getInfo=false;
    	Log.d("channelListButton 1",channelListButton);
		if (channelListButton.compareTo("normalList")==0){
			selectedRadioUrl = radioUrls[position];
			selectedRadioName = radioNames[position];
		}else if(channelListButton.compareTo("favoriteList")==0){
			selectedRadioUrl = favoriteRadioUrls[position];
			selectedRadioName = favoriteRadioNames[position];
		}	
		for(int i=0;i<notWorkingRadios.length;i++)
	    {
			Log.d("notWorkingRadio",notWorkingRadios[i]);
			if(notWorkingRadios[i].equalsIgnoreCase(selectedRadioName)){
				mediaType=2;
				Log.d("Not Working",String.valueOf(notWorkingRadios[i].indexOf("Ÿ")));
				Log.d("Not Working",notWorkingRadios[i]);
				break;
			}else{
				mediaType=1;
			}
	    }
		selectedChannel = new radioChannel(position,mediaType,selectedRadioName,selectedRadioUrl);
		
		//v.setSelected(true);	
		
		if (channelListButton.compareTo("normalList")==0){
			listIndex = getListView().getFirstVisiblePosition();
			View v2 = getListView().getChildAt(0);
	        listTop = (v2 == null) ? 0 : v2.getTop();
			showChannelsList();		
		}else if(channelListButton.compareTo("favoriteList")==0){
			showFavoriteChannelList();
		}
		
		stopPlaying();
		startPlaying();	
		
	}
  
    private void refreshChannelList(){
   	  	
    	JSONStations = getJSONfromURL(JSONUrl);
        
        if(JSONStations == null){
			Toast t = Toast.makeText(this,noServerConnectionMsg,8000);
		    t.show();
		    if(isChannelPrefEmpty()){
	            radioNames = getResources().getStringArray(R.array.radioName);    
	            radioUrls = getResources().getStringArray(R.array.radioURL); 
            } 
        }else{
        	parseJSONtoArray(JSONStations);
        	if(channels.compareTo("yerli")==0){
        	    SharedPreferences yerliKanallar = getSharedPreferences("yerli_channels_list", 0);
                SharedPreferences.Editor yerliKanallarEditor = yerliKanallar.edit();
                yerliKanallarEditor.clear();
                yerliKanallarEditor.commit();
                for(int i=0;i < radioNames.length;i++){          	
                	yerliKanallarEditor.putString(radioNames[i], radioUrls[i]);
        		}
                yerliKanallarEditor.commit();

        	}
        	else if (channels.compareTo("yabanci")==0){
        	    SharedPreferences yabanciKanallar = getSharedPreferences("yabanci_channels_list", 0);
                SharedPreferences.Editor yabanciKanallarEditor = yabanciKanallar.edit();
                yabanciKanallarEditor.clear();
                yabanciKanallarEditor.commit();
                for(int i=0;i < radioNames.length;i++){  
                	yabanciKanallarEditor.putString(radioNames[i], radioUrls[i]);
        		}
                yabanciKanallarEditor.commit();
        	}
        }
                        
    }

    private void getChannelList(){
    	Map<String, ?> channelsMap;
    	
    	if(channels.compareTo("yerli")==0){
    	    SharedPreferences yerliKanallar = getSharedPreferences("yerli_channels_list", 0);
    	    channelsMap=yerliKanallar.getAll();
    	    radioNames = new String[channelsMap.size()];
    	    radioUrls = new String[channelsMap.size()];
    	    int i=0;
        	for(String s : channelsMap.keySet()){
        		radioNames[i]=s;
        		radioUrls[i]=channelsMap.get(s).toString();
        	    i++;
        	    }
    	}
    	else if (channels.compareTo("yabanci")==0){
    	    SharedPreferences yabanciKanallar = getSharedPreferences("yabanci_channels_list", 0);
    	    channelsMap=yabanciKanallar.getAll();
    	    radioNames = new String[channelsMap.size()];
    	    radioUrls = new String[channelsMap.size()];
    	    int i=0;
        	for(String s : channelsMap.keySet()){
        		radioNames[i]=s;
        		radioUrls[i]=channelsMap.get(s).toString();
        	    i++;
        	    }
    	}
    	
    }
    
    private boolean isChannelPrefEmpty(){
    	Map<String, ?> channelsMap;
    	boolean isempty=true;
    	if(channels.compareTo("yerli")==0){
    	    SharedPreferences yerliKanallar = getSharedPreferences("yerli_channels_list", 0);
    	    channelsMap=yerliKanallar.getAll();
    	    if(channelsMap.isEmpty()) isempty=true;
    	    else isempty=false;
    	}
    	else if (channels.compareTo("yabanci")==0){
    		SharedPreferences yabanciKanallar = getSharedPreferences("yabanci_channels_list", 0);
    	    channelsMap=yabanciKanallar.getAll();
    	    if(channelsMap.isEmpty()) isempty=true;
    	    else isempty=false;
    		}
    	return isempty;
    }
    
	public static JSONObject getJSONfromURL(String url){

		//initialize
		InputStream is = null;
		String result = "";
		JSONObject jArray = null;

		//http post
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

		}catch(Exception e){
			Log.e("log_tag", "Error in http connection "+e.toString());
		}

		//convert response to string
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8000);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result=sb.toString();
		}catch(Exception e){
			Log.e("log_tag", "Error converting result "+e.toString());
		}

		//try parse the string to a JSON object
		try{
	        	jArray = new JSONObject(result);
		}catch(JSONException e){
			Log.e("log_tag", "Error parsing data "+e.toString());
		}

		return jArray;
	}

	private void runLongClick(int clickId){
		SharedPreferences favoriteChannels = getSharedPreferences("favorite_channels_list", 0);
        SharedPreferences.Editor favoriteChannelsEditor = favoriteChannels.edit();
		if (channelListButton.compareTo("normalList")==0){
			if(favoriteChannels.contains(radioNames[clickId]))
		    	Toast.makeText(this, radioNames[clickId]+" has already in favorite channels", Toast.LENGTH_LONG).show();
			else{
		    	favoriteChannelsEditor.putString(radioNames[clickId], radioUrls[clickId]);
		    	Toast.makeText(this, radioNames[clickId]+" added to favorites", Toast.LENGTH_LONG).show();
		    	buttonChannelList.setEnabled(true);
		    	favoriteChannelsEditor.commit();
		    	
		    	listIndex = getListView().getFirstVisiblePosition();
		        View v = getListView().getChildAt(0);
		        listTop = (v == null) ? 0 : v.getTop();
		    	showChannelsList();
			}
		}else if(channelListButton.compareTo("favoriteList")==0){
	    	Toast.makeText(this, favoriteRadioNames[clickId]+" removed From favorites", Toast.LENGTH_LONG).show();
			favoriteChannelsEditor.remove(favoriteRadioNames[clickId]);  	
	    	favoriteChannelsEditor.commit();
	    	
	    	listIndex = getListView().getFirstVisiblePosition();
	        View v = getListView().getChildAt(0);
	        listTop = (v == null) ? 0 : v.getTop();
	    	showFavoriteChannelList();			
		}       
	}
	
	private void getFavoritChannels(){
	    SharedPreferences favoriteChannels = getSharedPreferences("favorite_channels_list", 0);
    	favoriteChannelsMap=favoriteChannels.getAll();
    	int i=0;
    	favoriteRadioNames = new String[favoriteChannelsMap.size()];
    	favoriteRadioUrls = new String[favoriteChannelsMap.size()];
    	Log.d("String Arrays",String.valueOf(favoriteRadioNames.length));
    	for(String s : favoriteChannelsMap.keySet()){
    	    favoriteRadioNames[i]=s;
    	    favoriteRadioUrls[i]=favoriteChannelsMap.get(s).toString();
    	    i++;
    	    }
	}

	private void showChannelsList(){
		buttonChannelList.setText(getResources().getString(R.string.favButtonStr));
        //ArrayAdapter<String> adapter;
        //adapter = new ArrayAdapter<String>(this,R.layout.channels,R.id.channelList,radioNames);
        setListAdapter(new IconicAdapter());
        //ListView lv = getListView();
        getListView().setOnItemLongClickListener(new OnItemLongClickListener(){
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int row, long arg3) {
        	runLongClick(row);
        	return true;
           }});
        //channelListButton="favoriteList";
        getListView().setSelectionFromTop(listIndex, listTop);
	}
	
	private void showFavoriteChannelList(){
		buttonChannelList.setText(getResources().getString(R.string.normalButtonStr));
		getFavoritChannels();
		//ArrayAdapter<String> adapterFavorits;
		//adapterFavorits = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,favoriteRadioNames);
		//setListAdapter(adapterFavorits);
        setListAdapter(new FavoritIconicAdapter());
		ListView lv = getListView();
        lv.setOnItemLongClickListener(new OnItemLongClickListener(){
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int row, long arg3) {
        	runLongClick(row);
        	return true;
           }});
        //channelListButton="normalList";
        getListView().setSelectionFromTop(listIndex, listTop);

	}
		
	class IconicAdapter extends ArrayAdapter<String> {
	    IconicAdapter() {
	      super(radioPlayer.this, R.layout.channels, R.id.channelList, radioNames);
	    }
	    public View getView(int position, View convertView,ViewGroup parent) {
	      View row=super.getView(position, convertView, parent);
	      ImageView icon=(ImageView)row.findViewById(R.id.icon);
	      ImageView playIcon=(ImageView)row.findViewById(R.id.playIcon);
	      Log.d("iconicAdapter",String.valueOf(channelListButton));
		      if (favoriteChannels.contains(radioNames[position])){
		        icon.setImageResource(R.drawable.star);
		        icon.setVisibility(View.VISIBLE);
		      }
		      else {
		        icon.setImageResource(R.drawable.black);
		        icon.setVisibility(View.INVISIBLE);
		      }
		      if(radioNames[position].equals(selectedRadioName)){
			        playIcon.setImageResource(R.drawable.speaker);
			        playIcon.setVisibility(View.VISIBLE);
		      }
		      else{
		    	  playIcon.setImageResource(R.drawable.black);
		    	  playIcon.setVisibility(View.INVISIBLE);
		      }
	      
	return(row);
	    }
	}
	
	class FavoritIconicAdapter extends ArrayAdapter<String> {
		FavoritIconicAdapter() {
	      super(radioPlayer.this, R.layout.channels, R.id.channelList, favoriteRadioNames);
	    }
	    public View getView(int position, View convertView,ViewGroup parent) {
	      View row=super.getView(position, convertView, parent);
	      ImageView icon=(ImageView)row.findViewById(R.id.icon);
	      ImageView playIcon=(ImageView)row.findViewById(R.id.playIcon);
	      Log.d("iconicAdapter",String.valueOf(channelListButton));
	      if(favoriteRadioNames[position].equals(selectedRadioName)){
			   playIcon.setImageResource(R.drawable.speaker);
			   playIcon.setVisibility(View.VISIBLE);
		   }
		   else{
			   playIcon.setImageResource(R.drawable.black);
			   playIcon.setVisibility(View.INVISIBLE);
		      }
	return(row);
	    }
	}
  	
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		Log.d("Gesture","Gesture Aldi");
		// TODO Auto-generated method stub
		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
		// We want at least one prediction
		if (predictions.size() > 0) {
			Prediction prediction = predictions.get(0);
			// We want at least some confidence in the result
			if (prediction.score > 1.0) {
				// Show the spell
				Toast.makeText(this, String.valueOf(prediction.score), Toast.LENGTH_SHORT).show();
				stopPlaying();
			}
		}
		
	}
 	
	static void setInfoFields(){
		boolean getArtistInfo;
		boolean getSongInfo;

		if(index.getArtistName().compareTo("")==0 || index.getArtistName().compareTo("-")==0){
         	artistString="no artist info";
			getArtistInfo=false;
		}
		else{
        	 artistString=index.getArtistName();
 			 getArtistInfo=true;

        }
    	 
         if(index.getSongName().compareTo("")==0 || index.getSongName().compareTo("-")==0){
         	songString="no song info";
         	getSongInfo=false;
         }
         else{
        	 songString=index.getSongName();
        	 getSongInfo=true;
         	}
         getInfo=getSongInfo&&getArtistInfo;
         infoView.setText(artistString+" - "+songString);
	}
	
	private Runnable mAnimationStep = new Runnable() {
        
        public void run() {
            long currentTime = System.currentTimeMillis();
            float animationStep = (currentTime - mAnimationTime) * 1f / ANIMATION_DURATION;
            float weightOffset = animationStep * (mLayoutWeightSum - mLeftStartWeight);

            LinearLayout.LayoutParams llListParams = (LinearLayout.LayoutParams)
            		llList.getLayoutParams();
            LinearLayout.LayoutParams llInfoParams = (LinearLayout.LayoutParams)
                    llInfo.getLayoutParams();

            llListParams.weight += mLeftExpand ? weightOffset : -weightOffset;
            llInfoParams.weight += mLeftExpand ? -weightOffset : weightOffset;

            if (llListParams.weight >= mLayoutWeightSum) {
                mAnimating = false;
                llListParams.weight = mLayoutWeightSum;
                llInfoParams.weight = 0;
            } else if (llListParams.weight <= mLeftStartWeight) {
                mAnimating = false;
                llListParams.weight = mLeftStartWeight;
                llInfoParams.weight = mLayoutWeightSum - mLeftStartWeight;
            }

            mSlidingLayout.requestLayout();

            mAnimationTime = currentTime;

            if (mAnimating) {
                mAnimationHandler.postDelayed(mAnimationStep, 30);
            }
        }
    };

    private void toggleExpand(boolean expand) {
        mLeftExpand = expand;

        if (!mAnimating) {
            mAnimating = true;
            mAnimationTime = System.currentTimeMillis();
            mAnimationHandler.postDelayed(mAnimationStep, 30);
        }
    }
	    
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	     super.onSaveInstanceState(savedInstanceState);
	     // your stuff or nothing
	 }
	
	 @Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	     super.onRestoreInstanceState(savedInstanceState);
	     // your stuff or nothing
	 }
	    
	 @Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Log.d("onReStart","^^^^^^^^^RESTART^^^^^^");

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d("onResume","^^^^^^^^^RESUME^^^^^^");
	}
		
	@Override	
	protected void onStart() {			
		// TODO Auto-generated method stub			
		super.onStart();
		myBanner.refresh();
        greyHandler.postDelayed(refreshBannerTask, THIRTY_SECONDS);
		Log.d("onStart","^^^^^^^^^START^^^^^^");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
        greyHandler.removeCallbacks(refreshBannerTask);
		Log.d("onStop","^^^^^^^^^STOP^^^^^^");
	}
			
	@Override
	protected void onPause() {
		super.onPause();
		Log.d("onPause","^^^^^^^^^PAUSE^^^^^^");
	        /*if (player.isPlaying()) {
	            Log.d("onPause","2");
	            player.stop();
	            Log.d("onPause","3");
	        }*/
	}

	
	
	private void addFvrSong(){
		if(getInfo){
			getFavoritSongs();
			SharedPreferences favoriteSongs = getSharedPreferences("favorite_songs_list", 0);
	        if(favoriteSongs.contains(index.getArtistName().trim()+index.getSongName().trim())){  	
		    	Toast.makeText(this, index.getArtistName() +"-"+ index.getSongName()+" has already in favorites.", Toast.LENGTH_LONG).show();
	        }
			else{
		        SharedPreferences.Editor favoriteSongsEditor = favoriteSongs.edit(); 
				favoriteSongsEditor.putString(index.getArtistName().trim()+index.getSongName().trim(), index.getArtistName()+"?&"+index.getSongName());			
				Toast.makeText(this, index.getArtistName() +"-"+ index.getSongName()+" added to favorites", Toast.LENGTH_LONG).show();
				favoriteSongsEditor.commit();
			}
		}else
	    	Toast.makeText(this,"no info", Toast.LENGTH_LONG).show();

			
	}
	private void getFavoritSongs(){
	    SharedPreferences favoriteSongs = getSharedPreferences("favorite_songs_list", 0);
    	favoriteSongsMap=favoriteSongs.getAll();
    	int i=0;    	
    	favoriteArtists = new String[favoriteSongsMap.size()];	    
    	favoriteSongsList = new String[favoriteSongsMap.size()];   
    	for(String s : favoriteSongsMap.keySet()){
    		favoriteArtists[i]=s;
    		favoriteSongsList[i]=favoriteSongsMap.get(s).toString();    	    
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
	 
	 
	 /*   private void startRecording() {

        BufferedOutputStream writer = null;
        try {
            URL url = new URL(RADIO_STATION_URL);
            URLConnection connection = url.openConnection();
            final String FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + "Songs";

            File folder = new File(FOLDER_PATH);
            if (!folder.exists()) {
                folder.mkdir();
            }

            writer = new BufferedOutputStream(new FileOutputStream(new File(FOLDER_PATH
                    + File.separator + "sample.mp3")));
            recordingStream = connection.getInputStream();

            final int BUFFER_SIZE = 100;

            byte[] buffer = new byte[BUFFER_SIZE];

            while (recordingStream.read(buffer, 0, BUFFER_SIZE) != -1 && isRecording) {
                writer.write(buffer, 0, BUFFER_SIZE);
                writer.flush();
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                recordingStream.close();
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording() {
        buttonStopRecord.setEnabled(false);
        buttonRecord.setEnabled(true);
        try {
            isRecording = false;
            if (recordingStream != null) {
                recordingStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class RecorderThread extends Thread {
        @Override
        public void run() {
            isRecording = true;
            startRecording();
        } 

    };*/

}
