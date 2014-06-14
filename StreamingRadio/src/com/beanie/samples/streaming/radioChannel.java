package com.beanie.samples.streaming;

public class radioChannel {
	private int id;
	private int mediaType;
	private String name;
	private String URL;
	
	radioChannel(int channelId, int radioMediaType, String channelName, String channelURL){
		id=channelId;
		mediaType = radioMediaType;
		name=channelName;
		URL=channelURL;
	}
	
	public int getId(){
		return id;
	}
	public int getMediaType(){
		return mediaType;
	}	
	public String getName(){
		return name;
	}
	public String getURL(){
		return URL;		
	}

	
	
	public void setId(int channelId){
		id=channelId;
	}
	public void setMediaType(int radioMediaType){
		mediaType = radioMediaType;
	}
	public void setName(String channelName){
		name=channelName;
	}
	public void setURL(String channelURL){
		name=channelURL;
	}
	
}
