package net.masanoriyono.GeotagAdd2Image;

//import android.util.Log;

public class Degrees2DMS {
	private double lat;
	private double lng;
	
	private double abs_lat;
	private double abs_lng;
	
	private String lat_ref;
	
	private int lat_degree;
	private double temp_minute_lat;
	private int lat_minute;
	private double temp_second_lat;
	private long lat_second;
	private long lat_reduced_second_Numerator;
	private long lat_reduced_second_Denominator;
	
	private String lng_ref;
	
	private int lng_degree;
	private double temp_minute_lng;
	private int lng_minute;
	private double temp_second_lng;
	private long lng_second;
	
	private long lng_reduced_second_Numerator;
	private long lng_reduced_second_Denominator;
	
	private static final long RATIO = (long)1E5;
	
	public Degrees2DMS(double in_lat,double in_lng) {
		this.lat = in_lat;
		this.lng = in_lng;
	}
	
	public void convert() {
		//latitude
		if(lat >0){
			lat_ref = "N";
		}
		else{
			lat_ref = "S";
		}
		
		abs_lat = Math.abs(lat);
		
		lat_degree = (int)Math.floor(abs_lat);
		temp_minute_lat = abs_lat - Math.floor(abs_lat);
		lat_minute = (int)Math.floor(temp_minute_lat * 60);
		temp_second_lat = (temp_minute_lat * 60) - Math.floor(temp_minute_lat * 60);
		lat_second = (long)Math.floor(temp_second_lat * 60 * RATIO);
		
		Fraction f_lat = new Fraction(lat_second,RATIO);
		f_lat.reduction();
		lat_reduced_second_Numerator = f_lat.getNumerator();
		lat_reduced_second_Denominator = f_lat.getDenominator();
//		Log.d("Degrees2DMS","約数後: " + lat_reduced_second_Numerator + "/" + lat_reduced_second_Denominator);
		
		//longtitude
		if(lng >0){
			lng_ref = "E";
		}
		else{
			lng_ref = "W";
		}
		
		abs_lng = Math.abs(lng);
		
		lng_degree = (int)Math.floor(abs_lng);
		temp_minute_lng = abs_lng - Math.floor(abs_lng);
		lng_minute = (int)Math.floor(temp_minute_lng * 60);
		temp_second_lng = (temp_minute_lng * 60) - Math.floor(temp_minute_lng * 60);
		lng_second = (long)Math.floor(temp_second_lng * 60 * RATIO);
		
		Fraction f_lng = new Fraction(lng_second,RATIO);
		f_lng.reduction();
		lng_reduced_second_Numerator = f_lng.getNumerator();
		lng_reduced_second_Denominator = f_lng.getDenominator();
//		Log.d("Degrees2DMS","約数後: " + lng_reduced_second_Numerator + "/" + lng_reduced_second_Denominator);
	}
	
	public String getLatRef(){
		return lat_ref;
	}
	
	public String getLngRef(){
		return lng_ref;
	}
	
	public String getLat(){
		return String.valueOf(lat_degree) + "/1," 
			+ String.valueOf(lat_minute) + "/1,"
			+ String.valueOf(lat_reduced_second_Numerator) + "/" + String.valueOf(lat_reduced_second_Denominator);
	}
	
	public String getLng(){
		return String.valueOf(lng_degree) + "/1," 
			+ String.valueOf(lng_minute) + "/1,"
			+ String.valueOf(lng_reduced_second_Numerator) + "/" + String.valueOf(lng_reduced_second_Denominator);
	}
}