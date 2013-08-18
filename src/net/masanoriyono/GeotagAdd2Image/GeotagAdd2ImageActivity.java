package net.masanoriyono.GeotagAdd2Image;

//import android.R;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.sanselan.ImageReadException;
import org.apache.commons.sanselan.ImageWriteException;
import org.apache.commons.sanselan.Sanselan;
import org.apache.commons.sanselan.common.IImageMetadata;
import org.apache.commons.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.commons.sanselan.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.sanselan.formats.tiff.TiffField;
import org.apache.commons.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.commons.sanselan.formats.tiff.constants.TagInfo;
import org.apache.commons.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.commons.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.commons.sanselan.formats.tiff.write.TiffOutputSet;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GeotagAdd2ImageActivity extends Activity implements
		OnClickListener {
	/** Called when the activity is first created. */
	private static final String TAG = "GeotagAdd2Image";
	private static final int REQUEST_GALLERY = 1;
	private static final int MY_MAP = 2;
	private static final String START_MESSAGE = "Push menu key to open menu,and tap 'gallery'.\n"
			+ "Select photo,and modify 'geotag' of EXIF.";
	private static final String GEOTAG_SET_COMPLETE = "geotag added file make";
	private static final String NO_TARGET_GEOTAG = "no change because ";
	private static final String REMOVE_GEOTAG = "geotag removed file make";
	private static final String NO_GEOTAG = "geotag not found";
	private static final String LOCATION_FROM_MAP = "location from GoogleMap";
	private static final String LOCATION_FROM_PHOTO = "location from photo";
	private static final int TEXT_VIEW_HEIGHT = 36;
	private double o_lat = 0.0D;
	private double o_lng = 0.0D;
	private double c_lat = 0.0D;
	private double c_lng = 0.0D;
	
	private boolean f_geotag; 

	private String image_file_path = "";
	private File image_from_gallery;
	public FrameLayout layout;

	public TextView photo_text;
	public ImageView imgView;
	public Menu mMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		layout = new FrameLayout(this);

		LayoutParams lp0 = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		setContentView(layout, lp0);
		
		photo_text = new TextView(this);
        LayoutParams lp_photo_text = new LayoutParams(LayoutParams.MATCH_PARENT,
				TEXT_VIEW_HEIGHT);
        layout.addView(photo_text, lp_photo_text);
        
		imgView = new ImageView(this);
		LayoutParams lp_imgView = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		lp_imgView.gravity = Gravity.TOP;
		lp_imgView.topMargin = TEXT_VIEW_HEIGHT;
		layout.addView(imgView, lp_imgView);

		Toast.makeText(this, START_MESSAGE, Toast.LENGTH_LONG).show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Hold on to this
		mMenu = menu;

		// Inflate the currently selected menu XML resource.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.title_only, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		Log.v(TAG, "onMenuOpened");
		if (image_file_path.length() == 0) {
			MenuItem item_mapview = mMenu.getItem(1);
			item_mapview.setEnabled(false);
			MenuItem item_add_geotag = mMenu.getItem(2);
			item_add_geotag.setEnabled(false);
			MenuItem item_remove_geotag = mMenu.getItem(3);
			item_remove_geotag.setEnabled(false);
		} else {
			MenuItem item_mapview = mMenu.getItem(1);
			item_mapview.setEnabled(true);
			MenuItem item_add_geotag = mMenu.getItem(2);
			item_add_geotag.setEnabled(true);
			MenuItem item_remove_geotag = mMenu.getItem(3);
			item_remove_geotag.setEnabled(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent intent;
		ContentResolver contentResolver;
		ContentValues values;
		File f_src;
		long dateTaken;
		String output_image_file;
		String output_image_path;
		File f_dest;
		
		switch (item.getItemId()) {
		case R.id.menu_upper_left:
			intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, REQUEST_GALLERY);

			Log.v("menu_upper_left", "gallery");

			return true;

		case R.id.menu_upper_right:
			Intent intent2Map = new Intent(GeotagAdd2ImageActivity.this,
					MapViewActivity.class);

			intent2Map.setAction("TakePicturePosition");
			if(f_geotag){
				intent2Map.putExtra("lat", o_lat);
				intent2Map.putExtra("lng", o_lng);
			}
			else{
				intent2Map.putExtra("lat", "");
				intent2Map.putExtra("lng", "");
			}
			startActivityForResult(intent2Map, MY_MAP);

			Log.v("menu_upper_right", "map");

			return true;

		case R.id.menu_lower_left:

			f_src = new File(image_file_path);
			dateTaken = System.currentTimeMillis();
			output_image_file = "GEOADD_" + f_src.getName();
			output_image_path = f_src.getParent() + "/" + output_image_file;
			Log.v("menu_lower_left", "output_image_path:" + output_image_path);
			f_dest = new File(output_image_path);
			try {
				//create file by sanselan.
				setExifGPSTag(f_src, f_dest,c_lat,c_lng);
			} catch (ImageReadException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ImageWriteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			contentResolver = getContentResolver();

			values = new ContentValues();
			values.put(Images.Media.TITLE, output_image_file);
			values.put(Images.Media.DISPLAY_NAME, output_image_file);
			values.put(Images.Media.DATE_TAKEN, dateTaken);
			values.put(Images.Media.MIME_TYPE, "image/jpg");
			values.put(Images.Media.DATA, output_image_path);
			values.put(MediaStore.Images.ImageColumns.LATITUDE, String.valueOf(c_lat));
			values.put(MediaStore.Images.ImageColumns.LONGITUDE, String.valueOf(c_lng));
			
			contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			Toast.makeText(this,GEOTAG_SET_COMPLETE + "\n" + output_image_file
					,Toast.LENGTH_SHORT).show();
			
			Log.v("menu_lower_left", "Set GEOTAG");

			return true;

		case R.id.menu_lower_center:

			
			if(f_geotag){
				f_src = new File(image_file_path);
				dateTaken = System.currentTimeMillis();
				output_image_file = "NO_GEO_" + f_src.getName();
				output_image_path = f_src.getParent() + "/" + output_image_file;
				Log.v("menu_lower_left", "output_image_path:" + output_image_path);
				f_dest = new File(output_image_path);
				try {
					removeGeoTag(f_src, f_dest);
				} catch (ImageReadException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ImageWriteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				contentResolver = getContentResolver();
	
				values = new ContentValues();
				values.put(Images.Media.TITLE, output_image_file);
				values.put(Images.Media.DISPLAY_NAME, output_image_file);
				values.put(Images.Media.DATE_TAKEN, dateTaken);
				values.put(Images.Media.MIME_TYPE, "image/jpg");
				values.put(Images.Media.DATA, output_image_path);
				
				contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
				
				Toast.makeText(this, REMOVE_GEOTAG + "\n" + output_image_file
						, Toast.LENGTH_SHORT).show();
				
				Log.v("menu_lower_center", REMOVE_GEOTAG);
			}
			else{
				Toast.makeText(this, NO_TARGET_GEOTAG + NO_GEOTAG, Toast.LENGTH_SHORT).show();
				
				Log.v("menu_lower_center", NO_TARGET_GEOTAG + NO_GEOTAG);
			}

			return true;

		case R.id.menu_lower_right:
			finish();
			return true;
		default:
			break;
		}

		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
			try {
				ContentResolver cr = getContentResolver();

				String[] columns = { MediaStore.Images.Media.DATA };

				Cursor c = cr.query(data.getData(), columns, null, null, null);
				c.moveToFirst();
				image_file_path = c.getString(0);

				BitmapFactory.Options bfOptions = new BitmapFactory.Options();
				// Disable Dithering mode
				bfOptions.inDither = false;
				// Tell to gc that whether it needs free memory, the Bitmap can
				// be cleared
				bfOptions.inPurgeable = true;
				// Which kind of reference will be used to recover the Bitmap
				// data after being clear, when it will be used in the future
				bfOptions.inInputShareable = true;

				bfOptions.inSampleSize = 4;
				bfOptions.inTempStorage = new byte[32 * 1024];

				Log.v("FROM GALLERY", "read:" + image_file_path);

				File dest_image_file = new File(image_file_path);
				FileInputStream fs = null;
				fs = new FileInputStream(dest_image_file);

				Bitmap img = null;

				try {
					if (fs != null) {
						img = BitmapFactory.decodeFileDescriptor(fs.getFD(),
								null, bfOptions);
					}
				} catch (IOException e) {
					// TODO do something intelligent
					e.printStackTrace();
				} finally {
					if (fs != null) {
						try {
							fs.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				imgView.setImageBitmap(null);
				imgView.setImageBitmap(img);

				photo_text.setText(image_file_path);
				image_from_gallery = new File(image_file_path);

				f_geotag = IsExistGEOTAG(image_from_gallery);
				if(f_geotag){

					Log.d("IsExistGEOTAG", "true : " + o_lat + " , " + o_lng);
					Toast.makeText(
							this,
							LOCATION_FROM_PHOTO + "\n" + "latitude:" + o_lat
									+ "\n" + "longitude:" + o_lng,
							Toast.LENGTH_SHORT).show();
				} else {
					Log.d("IsExistGEOTAG", "false");
					Toast.makeText(this, NO_GEOTAG, Toast.LENGTH_SHORT).show();
				}

				String[] columns2 = { MediaStore.Images.Media._ID };
				c = cr.query(data.getData(), columns2, null, null, null);
				c.moveToFirst();
				Long src_media_id;
				src_media_id = c.getLong(0);
				Log.d("MediaStore.Images.Media._ID","src_media_id:" + src_media_id);
				
				StringBuilder where = new StringBuilder();
				where.append(MediaStore.Images.Media._ID).append(" == ").append(src_media_id);
				String selection = where.toString();
				
				Cursor cursor = managedQuery(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
						selection, null,null);

				cursor.moveToFirst();
				
				// init for loop
				int fieldIndex;
				String bucket_id;
				String imagecolumns_latitude;
				String imagecolumns_longitude;
				Long media_id;
				
//				do {
					// カラムIDの取得
					fieldIndex = cursor
							.getColumnIndex(MediaStore.Images.Media._ID);
					media_id = cursor.getLong(fieldIndex);

					fieldIndex = cursor
							.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID);
					
					bucket_id = cursor.getString(fieldIndex);
					
					fieldIndex = cursor
						.getColumnIndex(MediaStore.Images.ImageColumns.LATITUDE);
					imagecolumns_latitude = cursor.getString(fieldIndex);
					
					fieldIndex = cursor
						.getColumnIndex(MediaStore.Images.ImageColumns.LONGITUDE);
					imagecolumns_longitude = cursor.getString(fieldIndex);
					
					Uri bmpUri = ContentUris.withAppendedId(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI, media_id);
//					uriMap.put(cnt, bmpUri);
//					if(src_media_id == media_id){
						Log.d("コンテンツプロバイダのGPS情報", "Media._ID:" + media_id + " bmpUri:" + bmpUri
								+ " BUCKET_ID:" + bucket_id
								+ " LATITUDE:" + imagecolumns_latitude
								+ " LONGITUDE:" + imagecolumns_longitude);

//					}
//					cnt++;
//				} while (cursor.moveToNext());

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (requestCode == MY_MAP && resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			if (bundle != null) {
				if ((bundle.containsKey("c_lat"))
						&& (bundle.containsKey("c_lng"))) {
					c_lat = data.getDoubleExtra("c_lat", 0);
					c_lng = data.getDoubleExtra("c_lng", 0);
					Log.i("MY_MAP", "Position: " + c_lat + "," + c_lng);

					Toast.makeText(
							this,
							LOCATION_FROM_MAP + "\n" + "latitude:" + c_lat
									+ "\n" + "longitude:" + c_lng,
							Toast.LENGTH_SHORT).show();

				}
			}
		}
	}
	
	public boolean IsExistGEOTAG(File file) throws ImageReadException,IOException{
		IImageMetadata metadata = Sanselan.getMetadata(file);
		if (metadata instanceof JpegImageMetadata){
		    JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
		
			TiffImageMetadata exifMetadata = jpegMetadata.getExif();
			if (null != exifMetadata){
			    TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
			    if (null != gpsInfo){
			        String gpsDescription = gpsInfo.toString();
			        o_lng = gpsInfo.getLongitudeAsDegreesEast();
			        o_lat = gpsInfo.getLatitudeAsDegreesNorth();
			        
			        System.out.println("    " + "GPS Description: " + gpsDescription);
			        System.out.println("    " + "GPS Longitude (Degrees East): " + o_lng);
			        System.out.println("    " + "GPS Latitude (Degrees North): " + o_lat);
			        
			        return true;
			        
			    }
			}
		
		    
		}
		o_lat = 0.0d;
		o_lng = 0.0d;
		return false;
	}
	
	// Exif情報を抜き出す。
	public void showExif(ExifInterface ei) {
		// API 5
		String exifString = getExifString(ei, ExifInterface.TAG_DATETIME)
				+ "\n";
		exifString += getExifString(ei, ExifInterface.TAG_FLASH) + "\n";
		
		exifString += getExifString(ei, ExifInterface.TAG_GPS_LATITUDE_REF)
				+ "\n";
		exifString += getExifString(ei, ExifInterface.TAG_GPS_LATITUDE) + "\n";
		exifString += getExifString(ei, ExifInterface.TAG_GPS_LONGITUDE_REF)
				+ "\n";
		exifString += getExifString(ei, ExifInterface.TAG_GPS_LONGITUDE) + "\n";
		exifString += getExifString(ei, ExifInterface.TAG_IMAGE_LENGTH) + "\n";
		exifString += getExifString(ei, ExifInterface.TAG_IMAGE_WIDTH) + "\n";
		exifString += getExifString(ei, ExifInterface.TAG_MAKE) + "\n";
		exifString += getExifString(ei, ExifInterface.TAG_MODEL) + "\n";
		exifString += getExifString(ei, ExifInterface.TAG_ORIENTATION) + "\n";
		exifString += getExifString(ei, ExifInterface.TAG_WHITE_BALANCE) + "\n";

		// API 8
		exifString += getExifString(ei, ExifInterface.TAG_FOCAL_LENGTH) + "\n";
		exifString += getExifString(ei, ExifInterface.TAG_GPS_DATESTAMP) + "\n";
		exifString += getExifString(ei, ExifInterface.TAG_GPS_PROCESSING_METHOD)
				+ "\n";
		exifString += getExifString(ei, ExifInterface.TAG_GPS_TIMESTAMP) + "\n";

		// API 9
		// exifString += getExifString(ei, ExifInterface.TAG_GPS_ALTITUDE) +
		// "\n";

		// Honeycomb
		// exifString +=getExifString(ei, ExifInterface.TAG_APERTURE) + "\n";
		// exifString +=getExifString(ei, ExifInterface.TAG_EXPOSURE_TIME) +
		// "\n";
		// exifString +=getExifString(ei, ExifInterface.TAG_ISO) + "\n";

		Log.d(TAG, exifString);
	}
	
    public void removeGeoTag(File jpegImageFile, File dst) throws IOException,
	    ImageReadException, ImageWriteException
		{
		OutputStream os = null;
		try
		{
		    TiffOutputSet outputSet = null;
		
		    // note that metadata might be null if no metadata is found.
		    IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile);
		    JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
		    if (null != jpegMetadata)
		    {
		        // note that exif might be null if no Exif metadata is found.
		        TiffImageMetadata exif = jpegMetadata.getExif();
		
		        if (null != exif)
		        {
		            outputSet = exif.getOutputSet();
		        }
		    }
		
		    if (null == outputSet)
		        outputSet = new TiffOutputSet();
		    {
		    	outputSet.removeField(TiffConstants.GPS_TAG_GPS_LATITUDE_REF);
		    	outputSet.removeField(TiffConstants.GPS_TAG_GPS_LATITUDE);
		    	outputSet.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
		    	outputSet.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE);
		    }
		
		    os = new FileOutputStream(dst);
		    os = new BufferedOutputStream(os);
		
		    new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,
		            outputSet);
		
		    os.close();
		    os = null;
		} finally
		{
		    if (os != null)
		        try
		        {
		            os.close();
		        } catch (IOException e)
		        {
		
		    }
		}
	}
    
    public void changeExifMetadata(File jpegImageFile, File dst,double latitude,double longitude)
    	throws IOException, ImageReadException, ImageWriteException
		{
    	
		OutputStream os = null;
		try
		{
		    TiffOutputSet outputSet = null;
		
		    // note that metadata might be null if no metadata is found.
		    IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile);
		    JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
		    if (null != jpegMetadata)
		    {
		        // note that exif might be null if no Exif metadata is found.
		        TiffImageMetadata exif = jpegMetadata.getExif();
		
		        if (null != exif)
		        {
		            outputSet = exif.getOutputSet();
		        }
		    }
		
		    if (null == outputSet)
		        outputSet = new TiffOutputSet();
		
		    {
		        TiffOutputField aperture = TiffOutputField.create(
		                TiffConstants.EXIF_TAG_APERTURE_VALUE,
		                outputSet.byteOrder, new Double(0.3));
		        TiffOutputDirectory exifDirectory = outputSet
		                .getOrCreateExifDirectory();
		        exifDirectory
		                .removeField(TiffConstants.EXIF_TAG_APERTURE_VALUE);
		        exifDirectory.add(aperture);
		    }
		
		    {
		
		        outputSet.setGPSInDegrees(longitude, latitude);
		    }
		
		
		    os = new FileOutputStream(dst);
		    os = new BufferedOutputStream(os);
		
		    new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,
		            outputSet);
		
		    os.close();
		    os = null;
		} finally
		{
		    if (os != null)
		        try
		        {
		            os.close();
		        } catch (IOException e)
		        {
		
		        }
		}
	}
    
	public void setExifGPSTag(File jpegImageFile, File dst,double latitude,double longitude)
		throws IOException,ImageReadException, ImageWriteException {
		
		OutputStream os = null;
		try {
			TiffOutputSet outputSet = null;

			// note that metadata might be null if no metadata is found.
			IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile);
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
			if (null != jpegMetadata) {
				// note that exif might be null if no Exif metadata is found.
				TiffImageMetadata exif = jpegMetadata.getExif();

				if (null != exif) {
					outputSet = exif.getOutputSet();
				}
			}

			if (null == outputSet)
				outputSet = new TiffOutputSet();

			{

				outputSet.setGPSInDegrees(longitude, latitude);

			}

			os = new FileOutputStream(dst);
			os = new BufferedOutputStream(os);

			new ExifRewriter().updateExifMetadataLossless(jpegImageFile, os,
					outputSet);

			os.close();
			os = null;
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {

				}
		}
	}

	@SuppressWarnings("rawtypes")
	public static void metadataExample(File file) throws ImageReadException,
			IOException {
		IImageMetadata metadata = Sanselan.getMetadata(file);

		if (metadata instanceof JpegImageMetadata) {
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;


			Log.d("metadataExample", "file: " + file.getPath());

			printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_XRESOLUTION);
			printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME);
			printTagValue(jpegMetadata,
					TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
			printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_CREATE_DATE);
			printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_ISO);
			printTagValue(jpegMetadata,
					TiffConstants.EXIF_TAG_SHUTTER_SPEED_VALUE);
			printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_APERTURE_VALUE);
			printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_BRIGHTNESS_VALUE);
				
			printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LATITUDE_REF);
			printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LATITUDE);
			printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
			printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LONGITUDE);
			TiffImageMetadata exifMetadata = jpegMetadata.getExif();
			if (null != exifMetadata) {
				TiffImageMetadata.GPSInfo gpsInfo = exifMetadata.getGPS();
				if (null != gpsInfo) {
					String gpsDescription = gpsInfo.toString();
					double longitude = gpsInfo.getLongitudeAsDegreesEast();
					double latitude = gpsInfo.getLatitudeAsDegreesNorth();

					Log.d("metadataExample","gpsInfo: " + gpsInfo);
					Log.d("metadataExample","GPS Description: " + gpsDescription);
					Log.d("metadataExample","GPS Longitude (Degrees East): "
							+ longitude);
					Log.d("metadataExample","GPS Latitude (Degrees North): "
							+ latitude);
				}
			}

			// more specific example of how to manually access GPS values
			TiffField gpsLatitudeRefField = jpegMetadata
					.findEXIFValue(TiffConstants.GPS_TAG_GPS_LATITUDE_REF);
			TiffField gpsLatitudeField = jpegMetadata
					.findEXIFValue(TiffConstants.GPS_TAG_GPS_LATITUDE);
			TiffField gpsLongitudeRefField = jpegMetadata
					.findEXIFValue(TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
			TiffField gpsLongitudeField = jpegMetadata
					.findEXIFValue(TiffConstants.GPS_TAG_GPS_LONGITUDE);
			if (gpsLatitudeRefField != null && gpsLatitudeField != null
					&& gpsLongitudeRefField != null
					&& gpsLongitudeField != null) {
			}


			List items = jpegMetadata.getItems();
			for (int i = 0; i < items.size(); i++) {
				Object item = items.get(i);
				System.out.println("	" + "item: " + item);
			}

			System.out.println();
		}
	}

	private static void printTagValue(JpegImageMetadata jpegMetadata,
			TagInfo tagInfo) throws ImageReadException, IOException {
		TiffField field = jpegMetadata.findEXIFValue(tagInfo);
		if (field == null)
			Log.d("printTagValue", tagInfo.name + ": " + "Not Found.");
		else
			Log.d("printTagValue",
					tagInfo.name + ": " + field.getValueDescription());
	}

	private String getExifString(ExifInterface ei, String tag) {
		return tag + ": " + ei.getAttribute(tag);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}
}