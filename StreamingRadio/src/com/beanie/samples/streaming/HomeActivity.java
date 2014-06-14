package com.beanie.samples.streaming;

//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.spoledge.aacdecoder.AACPlayer;

//import java.io.InputStream;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLConnection;

//import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class HomeActivity extends ListActivity implements OnClickListener, OnGesturePerformedListener{
    
    private String[] radioNames;
    private String[] radioUrls; 
    private String[] favoriteRadioNames;
    private String[] favoriteRadioUrls;
    private String selectedRadioUrl="http://46.20.4.58:8040/";
    private String selectedRadioName="Select a radio station";
    private String artist;
    private String song;
    private String stopMsg;
    private String welcomeMsg;
    private String noServerConnectionMsg;
    private boolean favoriteButton=true;
    private boolean metaDataThreadStop=false;
    private int mediaType=1;
    private int channelList=1;
      
    private String JSONUrl; 
    private JSONObject JSONStations;
       
    private ProgressBar playSeekBar;
    private Button buttonChannelList;
    private Button buttonStopPlay; 
    private Button buttonBack; 
    private TextView stationView,artistView,songView;

    //private Button buttonRecord;
    //private Button buttonStopRecord;

    private MediaPlayer player;
    private AACPlayer aacPlayer;
    
    private Map<String, ?> favoriteChannelsMap;
    private GestureLibrary mLibrary;
    
    private Thread getMetaData;

    //private InputStream recordingStream;
    //private RecorderThread recorderThread;
    //private boolean isRecording = false;

    /** Called when the activity is first created. */
    
    /*public HomeActivity(String channels){
    	setChannelChoice(channels);
    }*/
    
    @Override
    public void onCreate(Bundle savedInstanceState) {

    	SharedPreferences favoriteChannels = getSharedPreferences("favorite_channels_list", 0);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);  

        initializeUIElements();
        setChannelChoice();

        JSONStations = getJSONfromURL(JSONUrl);
        if(JSONStations == null){
			Toast t = Toast.makeText(this,noServerConnectionMsg,8000);
		    t.show();
            radioNames = getResources().getStringArray(R.array.radioName);    
            radioUrls = getResources().getStringArray(R.array.radioURL);  
        }else{
        	parseJSONtoArray(JSONStations);
        }
        
        mLibrary = GestureLibraries.fromRawResource(this, R.raw.radiogestures);
        if (!mLibrary.load()) {
        	finish();
        }

        //GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
        //gestures.addOnGesturePerformedListener(this);
        //gestures.setGestureColor(Color.TRANSPARENT);
                        
        initializeMediaPlayer();        
        stationView.setText(welcomeMsg);

        chooseChannelList();     
    }

    private void initializeUIElements() {
	
    	stopMsg = getResources().getString(R.string.stopMsg);
        welcomeMsg=getResources().getString(R.string.welcomeMsg);
        noServerConnectionMsg=getResources().getString(R.string.noServerConnection);

    	stationView = (TextView) findViewById(R.id.stationView);
    	//artistView = (TextView) findViewById(R.id.artistView);
    	//songView = (TextView) findViewById(R.id.songView);
    	
    	//channelList = (TextView) findViewById(R.id.channelList);
    	//channelList.setOnClickListener(this);
    	
    	//likeCheckBox =(CheckBox) findViewById(R.id.likeCheckBox);
    	//likeCheckBox.setOnClickListener(this);
    	
        playSeekBar = (ProgressBar) findViewById(R.id.volumeBar);
        playSeekBar.setMax(100);
        playSeekBar.setVisibility(View.INVISIBLE);

        buttonChannelList = (Button) findViewById(R.id.buttonChannelList);
        buttonChannelList.setOnClickListener(this);

        buttonBack =(Button) findViewById(R.id.buttonBack); 
        buttonBack.setOnClickListener(this);

        getFavoritChannels();
        if (favoriteChannelsMap.isEmpty()){
        	buttonChannelList.setEnabled(false);
        }else{
        	buttonChannelList.setEnabled(true);
        }

        buttonStopPlay = (Button) findViewById(R.id.buttonStopPlay);
        buttonStopPlay.setEnabled(false);
        buttonStopPlay.setOnClickListener(this);

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
	    HomeActivity.this.startActivity(info);
	}

    /*public void setChannelChoice(String channels){
    	if(channels.compareTo("yerli")==0){
            JSONUrl = getResources().getString(R.string.JSONUrlYerli);
            channelList = 1;
    	}
    	else if (channels.compareTo("yabanci")==0){
            JSONUrl = getResources().getString(R.string.JSONUrlYabanci);
            channelList = 1;
    	}
    	else if(channels.compareTo("favori")==0){
    		channelList = 2;    
    	}
    }*/
    
	private void setChannelChoice(){
    	Bundle extras = getIntent().getExtras();
        if(extras !=null) {
        	String channels = extras.getString("kanalList");  
        	if(channels.compareTo("yerli")==0){
                JSONUrl = getResources().getString(R.string.JSONUrlYerli);
                channelList = 1;
        	}
        	else if (channels.compareTo("yabanci")==0){
                JSONUrl = getResources().getString(R.string.JSONUrlYabanci);
                channelList = 1;
        	}
        	else if(channels.compareTo("favori")==0){
        		channelList = 2;    
        	}

        }else{
        	
        }
    }
    
    private void chooseChannelList(){
    	switch(channelList){
    	case 1:
    		showChannelsList();
    	break;
    	case 2:
    		showFavoriteChannelList();    	
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
		    Log.d("1","1");
		    //moveTaskToBack(true);
		    Log.d("2","2");
		    info.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		    Log.d("3","3");
		    //HomeActivity.this.startActivity(info);
		    startActivityForResult(info,0);
    	break;
    	case R.id.buttonChannelList:
    		if (favoriteButton){
    			showFavoriteChannelList();
    		}else{
    			showChannelsList();
    		}
        break;
    	case R.id.buttonStopPlay:
            stopPlaying();
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
		Log.d("onStart","^^^^^^^^^START^^^^^^");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
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
    
	
	private void startPlaying() {
    	
    	stationView.setText(selectedRadioName);
        buttonStopPlay.setEnabled(true);
        playSeekBar.setVisibility(View.VISIBLE);
        initializeMediaPlayer();

		switch(mediaType){
        case 1:
        	aacPlayer.playAsync(selectedRadioUrl);
        	metaDataThreadStop=false;
        	getMetaData = new Thread(){
        			public void run(){     				
        				getMeta();			    					
        			}	
        	};
        	getMetaData.start();
        	break;
        case 2:
            player.setAudioStreamType(AudioManager.STREAM_RING);     
            player.prepareAsync();                
            player.setOnPreparedListener(new OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {  
                    player.start();     
                    //buttonRecord.setEnabled(true);
                }
            });
        	break;
        }

    }

    private void stopPlaying() {
    	int i=0;
    	stationView.setText(stopMsg);
    	switch(mediaType){
    	case 1:
            aacPlayer.stop();
            //aacPlayer.
            metaDataThreadStop=true;
            /*if (getMetaData!=null){
            	Log.d("Stop","threadNULL");
            	getMetaData.interrupt();
            	Log.d("threadInterrupted",String.valueOf(getMetaData.isInterrupted()));          	
            	try{
            		Log.d("Stop","threadJoin");
            		getMetaData.join();
            	}catch(InterruptedException e){
            		Thread.currentThread().interrupt();
            	}
            }*/
    		break;
    	case 2:
            if (player.isPlaying()){
            player.stop();
             //aacPlayer.stop();
             player.release();
            }
    		break;
    	}
    	buttonChannelList.setEnabled(true);
        buttonStopPlay.setEnabled(false);
        playSeekBar.setVisibility(View.INVISIBLE);
        //buttonRecord.setEnabled(false);
        //buttonStopRecord.setEnabled(false);
        /*stopRecording();*/
    }

    

    private void initializeMediaPlayer() {
    	switch(mediaType){
    	case 1:
    		aacPlayer = new AACPlayer();
    		break;
    	case 2:
            player = new MediaPlayer();       
            try {
                player.setDataSource(selectedRadioUrl);      
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    playSeekBar.setSecondaryProgress(percent);
                    Log.i("Buffering", "" + percent);
                }
            });
    		break;
    	}
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		//super.onListItemClick(l, v, position, id);
    	String selection = l.getItemAtPosition(position).toString();
    	Toast.makeText(this, selection, Toast.LENGTH_LONG).show();
    	artistView.setText("looking for artist info");
    	songView.setText("looking for song info");
		if (favoriteButton){
			selectedRadioUrl = radioUrls[position];
			selectedRadioName = radioNames[position];
		}else{
			selectedRadioUrl = favoriteRadioUrls[position];
			selectedRadioName = favoriteRadioNames[position];
		}
		switch(mediaType){
		case 1:
			stopPlaying();
			startPlaying();
			break;
		case 2:
			if (player.isPlaying()) {
			stopPlaying();
			startPlaying();
    		}
    		else{      
    			startPlaying();    
    			}
			break;
		}
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
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"),8000);
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
		if (favoriteButton){	    
	    	favoriteChannelsEditor.putString(radioNames[clickId], radioUrls[clickId]);
	    	Toast.makeText(this, radioNames[clickId]+" added to favorites", Toast.LENGTH_LONG).show();
	    	buttonChannelList.setEnabled(true);
	    	favoriteChannelsEditor.commit();
		}else{
	    	Toast.makeText(this, favoriteRadioNames[clickId]+" removed From favorites", Toast.LENGTH_LONG).show();
			favoriteChannelsEditor.remove(favoriteRadioNames[clickId]);  	
	    	favoriteChannelsEditor.commit();
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
		buttonChannelList.setText("Favorite Channels");
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this,R.layout.channels,R.id.channelList,radioNames);
        setListAdapter(adapter);
        ListView lv = getListView();
        lv.setOnItemLongClickListener(new OnItemLongClickListener(){
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int row, long arg3) {
        	runLongClick(row);
        	return true;
           }});
        favoriteButton=!favoriteButton;
	}
	private void showFavoriteChannelList(){
		buttonChannelList.setText("Channel List");
		getFavoritChannels();
		ArrayAdapter<String> adapterFavorits;
		adapterFavorits = new ArrayAdapter<String>(this,R.layout.channels,R.id.channelList,favoriteRadioNames);
		setListAdapter(adapterFavorits);
		ListView lv = getListView();
        lv.setOnItemLongClickListener(new OnItemLongClickListener(){
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1,int row, long arg3) {
        	runLongClick(row);
        	return true;
           }});
		favoriteButton=!favoriteButton;

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
 	
	public void getMeta(){
        Timer timer;
	    timer = new Timer();
	    while(!metaDataThreadStop){
	    timer.schedule(new TimerTask() {
	        public void run() {
	            URL url;
	            //Message msg = handler.obtainMessage();
	            try {
	                Log.d("Metadatos","Dentro del try para obtener los metadatos");
	                url = new URL(selectedRadioUrl);
	                IcyStreamMeta icy = new IcyStreamMeta(url);

	                artist=icy.getArtist();
	                Log.d("artist",artist);
	                song=icy.getTitle();
	                Log.d("song",song);
	                handler.sendEmptyMessage(0);

	                
	            } catch (MalformedURLException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }

	        }
	    }, 0, 30000);}
	    timer.cancel();
	} 
	
	private Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
        	 if(artist.compareTo("")==0 || artist.compareTo("-")==0)
             	artistView.setText("no artist info");
             else{
             	artistView.setText(artist);
             	Log.d("handler_artist",artist);
             }
        	 
             if(song.compareTo("")==0 || song.compareTo("-")==0)
             	songView.setText("no song info");
             else{
            	 Log.d("handler_song",song);
             	songView.setText(song);
             	}

         }
	};
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
