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
//	 private static final String APPLICATION_NAME = "GeotagAdd2Image";
	// private static final Uri IMAGE_URI =
	// MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	// private static final String PATH =
	// Environment.getExternalStorageDirectory().toString() + "/" +
	// APPLICATION_NAME;
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
	// private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	// private static final String uri =
	// "http://road2win.plala.jp/e-velo/report/exif/exif_regi.php";
	// private static final String uri =
	// "http://ec2.masanoriyono.net/report/exif/exif_regi.php";

	// private static final int UPLOAD_COMPLETE = -1;
	// private static final int EDIT_TEXT_HEIGHT = 72;
	private static final int TEXT_VIEW_HEIGHT = 36;
//	private float[] lat_lng;
//	private double[] lat_lng;
	// original
	private double o_lat = 0.0D;
	private double o_lng = 0.0D;
	// returned map center
	private double c_lat = 0.0D;
	private double c_lng = 0.0D;
//	private ExifInterface exifInterface;
	
	private boolean f_geotag; 
//	private Degrees2DMS d2d;

	private String image_file_path = "";
	private File image_from_gallery;
	public FrameLayout layout;
	// public EditText image_memo_text;
	public TextView photo_text;
	public ImageView imgView;
	public Menu mMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		layout = new FrameLayout(this);
		// layout = new LinearLayout(this);

		LayoutParams lp0 = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		setContentView(layout, lp0);
		
		photo_text = new TextView(this);
        LayoutParams lp_photo_text = new LayoutParams(LayoutParams.MATCH_PARENT,
				TEXT_VIEW_HEIGHT);
//        lp_photo_text.gravity = Gravity.TOP;
//        lp_photo_text.topMargin = EDIT_TEXT_HEIGHT;
        layout.addView(photo_text, lp_photo_text);
        
		imgView = new ImageView(this);
		LayoutParams lp_imgView = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		// //FrameLayoutではeditText.getHeight=0が返ってきた。まだ生成前なので。
		lp_imgView.gravity = Gravity.TOP;
		lp_imgView.topMargin = TEXT_VIEW_HEIGHT;
		// imgView.setFocusableInTouchMode(true);
		// imgView.setFocusable(true);
		// imgView.requestFocusFromTouch();
		layout.addView(imgView, lp_imgView);

		// // ギャラリー呼び出し
		// Intent intent = new Intent();
		// intent.setType("image/*");
		// intent.setAction(Intent.ACTION_GET_CONTENT);
		// startActivityForResult(intent, REQUEST_GALLERY);

		Toast.makeText(this, START_MESSAGE, Toast.LENGTH_LONG).show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Hold on to this
		mMenu = menu;

		// Inflate the currently selected menu XML resource.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.title_only, menu);

		// Disable the spinner since we've already created the menu and the user
		// can no longer pick a different menu XML.
		// mSpinner.setEnabled(false);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		
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
		// For "Title only": Examples of matching an ID with one assigned in
		// the XML
		case R.id.menu_upper_left:

			// ギャラリー呼び出し
			intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, REQUEST_GALLERY);

			Log.v("menu_upper_left", "gallery");

			// webView.reload();

			return true;

		case R.id.menu_upper_right:
			// if (webView.canGoForward()) {
			// webView.goForward();
			// }
			//ここでジオタグの有無をチェック。そうでないと過去に開いたものをそのまま投げてしまう。
			
			Intent intent2Map = new Intent(GeotagAdd2ImageActivity.this,
					MapViewActivity.class);

			// この書き方でエラーにはなってないけれど、調べてみるとBitmapの画像が粗いらしい。
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

			// Intent intent_search = new Intent(Intent.ACTION_MAIN, null);
			// // デスクトップから可能なIntent(つまり通常のアプリケーション)
			// intent_search.addCategory(Intent.CATEGORY_LAUNCHER);
			// // 通常のアプリケーションのリストを取得
			// PackageManager manager = getPackageManager();
			// List<ResolveInfo> infoes = manager.queryIntentActivities(
			// intent_search, 0);
			// Boolean f_exist = false;
			// for (int i = 0; i < infoes.size(); i++) {
			// ResolveInfo a_info = infoes.get(i);
			// // Log.v("menu_lower_left", "name:" +
			// // a_info.nonLocalizedLabel.toString());
			// Log.v("menu_upper_right",
			// "judge:"
			// + a_info.toString()
			// .contains(
			// "net.masanoriyono.MapSample.MapSampleActivity"));
			// // if(a_info.loadLabel(manager).equals("Paint")){
			// if (a_info.toString().contains(
			// "net.masanoriyono.Paint.PaintActivity")) {
			// f_exist = true;
			// Log.v("menu_upper_right",
			// "name:" + a_info.loadLabel(manager));
			// }
			// }
			// if (f_exist) {
			// Intent intent2Map = new Intent();
			// intent2Map.setClassName("net.masanoriyono.MapSample",
			// "net.masanoriyono.MapSample.MapSampleActivity");
			//
			// boolean f_geotag = exifInterface.getLatLong(lat_lng);
			// if (f_geotag) {
			// o_lat = lat_lng[0];
			// o_lng = lat_lng[1];
			//
			// }
			// // //この書き方でエラーにはなってないけれど、調べてみるとBitmapの画像が粗いらしい。
			// intent2Map.setAction("TakePicturePosition");
			// intent2Map.putExtra("lat", (double) o_lat);
			// intent2Map.putExtra("lng", (double) o_lng);
			// // なのでファイル名渡しにしようと思う。時間があればデータそのものを渡す方法もいいかも。
			// // startActivityForResult(intent2Paint,MY_PAINT);
			// startActivityForResult(intent2Map, MY_MAP);
			// }

			Log.v("menu_upper_right", "map");

			return true;

		case R.id.menu_lower_left:

			//Degree -> DMS
//			d2d = new Degrees2DMS(c_lat,c_lng);
//			d2d.convert();
//			Log.d("Degrees2DMS", "lat_ref : " + d2d.getLatRef() +
//				" lat : " + d2d.getLat() +
//				" lnt_ref : " + d2d.getLngRef() +
//				" lng : " + d2d.getLng());
//			
//			try {
//				exifInterface = new ExifInterface(image_file_path);
//			} catch (IOException e) {
//				Log.e(TAG, "Error");
//				return false;
//			}
			// exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE,
			// d2d.getLat());
			// exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,
			// d2d.getLatRef());
			// exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,
			// d2d.getLng());
			// exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,
			// d2d.getLngRef());
			//
			// Toast.makeText(this,GEOTAG_SET_COMPLETE,Toast.LENGTH_SHORT).show();

			// try {
			// exifInterface.saveAttributes();
			// Toast.makeText(this,GEOTAG_SET_COMPLETE,Toast.LENGTH_SHORT).show();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
			// }

			// String output_image = "/mnt/sdcard/DCIM/101SHARP/test.JPG";
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

			// copy temp file over original file
//			try {
//				FileUtils.copyFile(f_dest, f_src);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			//Regist ContentsProvider
			
			contentResolver = getContentResolver();

			values = new ContentValues();
			values.put(Images.Media.TITLE, output_image_file);
			values.put(Images.Media.DISPLAY_NAME, output_image_file);
			values.put(Images.Media.DATE_TAKEN, dateTaken);
			values.put(Images.Media.MIME_TYPE, "image/jpg");
			values.put(Images.Media.DATA, output_image_path);
			//一緒に入れたほうがいいみたい。下のようにすると別のファイルとして扱われる様子。
			values.put(MediaStore.Images.ImageColumns.LATITUDE, String.valueOf(c_lat));
			values.put(MediaStore.Images.ImageColumns.LONGITUDE, String.valueOf(c_lng));
			
			contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			
//			ContentResolver contentResolver = getContentResolver();
//			ContentValues values = new ContentValues();
//			values.put(MediaStore.Images.ImageColumns.DATA, output_image_path);
			//どうも採番してくれる様子。
//			values.put(MediaStore.Images.ImageColumns.BUCKET_ID, "1323083777");
			
//			contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			
			Toast.makeText(this,GEOTAG_SET_COMPLETE + "\n" + output_image_file
					,Toast.LENGTH_SHORT).show();
			
			Log.v("menu_lower_left", "Set GEOTAG");

			return true;

		case R.id.menu_lower_center:

//			try {
//				exifInterface = new ExifInterface(image_file_path);
//			} catch (IOException e) {
//				Log.e(TAG, "Error");
//				return false;
//			}

//			// String t_res =
//			// exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
//			String t_temp = "";
//			// for(int i=0;i<t_res.length();i++){
//			// t_temp.concat(" ");
//			// }
//			exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, t_temp);
//
//			// t_res =
//			// exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
//			// t_temp = "";
//			// for(int i=0;i<t_res.length();i++){
//			// t_temp.concat(" ");
//			// }
//			exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,
//					t_temp);
//
//			// t_res =
//			// exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
//			// t_temp = "";
//			// for(int i=0;i<t_res.length();i++){
//			// t_temp.concat(" ");
//			// }
//			exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, t_temp);
//
//			// t_res =
//			// exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
//			// t_temp = "";
//			// for(int i=0;i<t_res.length();i++){
//			// t_temp.concat(" ");
//			// }
//			exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,
//					t_temp);
//			// exifInterface.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, "");
//			// exifInterface.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, "");
//			// exifInterface.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD,
//			// "");
//			// exifInterface.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, "");
			
			if(f_geotag){
				f_src = new File(image_file_path);
				dateTaken = System.currentTimeMillis();
				output_image_file = "NO_GEO_" + f_src.getName();
				output_image_path = f_src.getParent() + "/" + output_image_file;
				Log.v("menu_lower_left", "output_image_path:" + output_image_path);
				f_dest = new File(output_image_path);
				try {
					//create file by sanselan.
//					ExifRewriter er = new ExifRewriter();
//					OutputStream os = null;
//					os = new FileOutputStream(f_dest);
//					er.removeExifMetadata(f_src, os);
//					os.close();
//				    os = null;
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
				//一緒に入れたほうがいいみたい。下のようにすると別のファイルとして扱われる様子。
	//			values.put(MediaStore.Images.ImageColumns.LATITUDE, String.valueOf(c_lat));
	//			values.put(MediaStore.Images.ImageColumns.LONGITUDE, String.valueOf(c_lng));
				
				contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
				
				Toast.makeText(this, REMOVE_GEOTAG + "\n" + output_image_file
						, Toast.LENGTH_SHORT).show();
				
				Log.v("menu_lower_center", REMOVE_GEOTAG);
			}
			else{
				Toast.makeText(this, NO_TARGET_GEOTAG + NO_GEOTAG, Toast.LENGTH_SHORT).show();
				
				Log.v("menu_lower_center", NO_TARGET_GEOTAG + NO_GEOTAG);
			}
			// try {
			// exifInterface.saveAttributes();
			// Log.e(TAG, "set");
			// Toast.makeText(this,REMOVE_GEOTAG,Toast.LENGTH_SHORT).show();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show();
			// }

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
				// この呼び出し方だと配列には8つデータが入っている。
				// Cursor c = cr.query(data.getData(), null, null, null, null);
				// c.moveToFirst();
				// Log.d("REQUEST_GALLERY","0:"+ c.getString(0) + " 1:"+
				// c.getString(1)
				// + " 2:"+ c.getString(2) + " 3:"+ c.getString(3)
				// + " 4:"+ c.getString(4) + " 5:"+ c.getString(5)
				// + " 6:"+ c.getString(6) + " 7:"+ c.getString(7));
				// 参考元
				// http://d.hatena.ne.jp/hyoromo/20100109/1263046050
				// 0 インデックス 0
				// 1 ファイルパス /sdcard/DCIM/Camera/test.jpg
				// 2 ファイルサイズ 337243
				// 3 ファイル名(拡張子付き) test.jpg
				// 4 MIMEタイプ image/jpeg
				// 5 ファイル名(拡張子なし) test
				// 6 謎データ*1 1256906217
				// 7 謎データ*2 1249145692

				String[] columns = { MediaStore.Images.Media.DATA };
				// この呼び出し方だと配列には1つだけデータが入っている。
				Cursor c = cr.query(data.getData(), columns, null, null, null);
				c.moveToFirst();
				image_file_path = c.getString(0);
//				Log.d("REQUEST_GALLERY", "c:" + c.getCount());
				// 配列には1つしかデータは入っていない？
				// Log.d("REQUEST_GALLERY","0:"+ c.getString(0) + "1:"+
				// c.getString(1));
				// + "1:"+ c.getString(2) + "1:"+ c.getString(3)
				// + "1:"+ c.getString(4) + "1:"+ c.getString(5)
				// + "1:"+ c.getString(6) + "1:"+ c.getString(7));

				BitmapFactory.Options bfOptions = new BitmapFactory.Options();
				// Disable Dithering mode
				bfOptions.inDither = false;
				// Tell to gc that whether it needs free memory, the Bitmap can
				// be cleared
				bfOptions.inPurgeable = true;
				// Which kind of reference will be used to recover the Bitmap
				// data after being clear, when it will be used in the future
				bfOptions.inInputShareable = true;
				// 縮小スケール。2の倍数毎に有効。
				bfOptions.inSampleSize = 4;
				bfOptions.inTempStorage = new byte[32 * 1024];

				// example ->  /mnt/sdcard/DCIM/101SHARP/DSC_0032.JPG
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

				// InputStream in_img =
				// getContentResolver().openInputStream(data.getData());
				// img = BitmapFactory.decodeStream(in_img);
				// in_img.close();

				// 選択した画像を表示
				imgView.setImageBitmap(null);
				imgView.setImageBitmap(img);

				photo_text.setText(image_file_path);
				// photo_text.requestFocusFromTouch();
				image_from_gallery = new File(image_file_path);

//				try {
//					exifInterface = new ExifInterface(image_file_path);
//				} catch (IOException e) {
//					Log.e(TAG, "Error");
//					return;
//				}
				//壊れたデータの場合があるのでデバッグの時のみに。
//				showExif(exifInterface);
//				metadataExample(image_from_gallery);
				
				//ContentProviderに登録されてない場合は例外になってる。
//				lat_lng = new float[2];
				//boolean f_geotag = exifInterface.getLatLong(lat_lng);
//				lat_lng = IsExistGEOTAG(image_from_gallery);
//				if(lat_lng.length>0){
				f_geotag = IsExistGEOTAG(image_from_gallery);
				if(f_geotag){
//					o_lat = lat_lng[0];
//					o_lng = lat_lng[1];

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

				//cr = getContentResolver();
				// この呼び出し方だと配列には8つデータが入っている。
				// Cursor c = cr.query(data.getData(), null, null, null, null);
				String[] columns2 = { MediaStore.Images.Media._ID };
				c = cr.query(data.getData(), columns2, null, null, null);
				//c = managedQuery(data.getData(), null, null, null, null);
				c.moveToFirst();
				Long src_media_id;
				src_media_id = c.getLong(0);
				Log.d("MediaStore.Images.Media._ID","src_media_id:" + src_media_id);
//				Log.d("REQUEST_GALLERY",
//						"0:" + c.getString(0) + " 1:" + c.getString(1) + " 2:"
//								+ c.getString(2) + " 3:" + c.getString(3)
//								+ " 4:" + c.getString(4) + " 5:"
//								+ c.getString(5) + " 6:" + c.getString(6)
//								+ " 7:" + c.getString(7));
				
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
//				int cnt = 0, VolMax = 0;
//				HashMap<Integer, Uri> uriMap = new HashMap<Integer, Uri>(); // URIをMapで管理する
				
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
					
//					Log.d("REQUEST_GALLERY", "BUCKET_ID:" + bucket_id);
					// IDからURIを取得
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

//				VolMax = --cnt;
//				cnt = 0;
				
				// bitmapの解放
				// img.recycle();
				// img = null;
				//
				// file upload.
				// upload(image_from_gallery);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (requestCode == MY_MAP && resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			// これがnullの場合があるのにチェックせずに
			// キーの存在をチェックしていたのでエラーが起きていた。
			if (bundle != null) {
				if ((bundle.containsKey("c_lat"))
						&& (bundle.containsKey("c_lng"))) {
					// どうもgetParcelableではキーが存在しない場合はnullが戻ってくる。
					// Bitmap bitmap_Paint2SS =
					// (Bitmap)bundle.getParcelable("net.masanoriyono.Paint2SS.BITMAP");
					c_lat = data.getDoubleExtra("c_lat", 0);
					c_lng = data.getDoubleExtra("c_lng", 0);
					// String Paint2SS_imagefile =
					// intent.getStringExtra("net.masanoriyono.Paint2SS.BITMAP");
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
	
//	public double[] IsExistGEOTAG(File file) throws ImageReadException,IOException{
	public boolean IsExistGEOTAG(File file) throws ImageReadException,IOException{
		//        get all metadata stored in EXIF format (ie. from JPEG or TIFF).
		//            org.w3c.dom.Node node = Sanselan.getMetadataObsolete(imageBytes);
		IImageMetadata metadata = Sanselan.getMetadata(file);
		
		//System.out.println(metadata);
		
		if (metadata instanceof JpegImageMetadata){
		    JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
		
		    // Jpeg EXIF metadata is stored in a TIFF-based directory structure
		    // and is identified with TIFF tags.
		    // Here we look for the "x resolution" tag, but
		    // we could just as easily search for any other tag.
		    //
		    // see the TiffConstants file for a list of TIFF tags.
		
		//    System.out.println("file: " + file.getPath());
		//
		//    // print out various interesting EXIF tags.
		//    printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_XRESOLUTION);
		//    printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME);
		//    printTagValue(jpegMetadata,
		//            TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
		//    printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_CREATE_DATE);
		//    printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_ISO);
		//    printTagValue(jpegMetadata,
		//            TiffConstants.EXIF_TAG_SHUTTER_SPEED_VALUE);
		//    printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_APERTURE_VALUE);
		//    printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_BRIGHTNESS_VALUE);
		//    printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LATITUDE_REF);
		//    printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LATITUDE);
		//    printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
		//    printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LONGITUDE);
		//
		//    System.out.println();
		
			// simple interface to GPS data
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
			        
//			        lat_lng = new double[2];
//			        lat_lng[0] = o_lat;
//			        lat_lng[1] = o_lng;
//			        return lat_lng;
			        return true;
			        
			    }
			}
		
//		    // more specific example of how to manually access GPS values
//		    TiffField gpsLatitudeRefField = jpegMetadata
//		            .findEXIFValueWithExactMatch(TiffConstants.GPS_TAG_GPS_LATITUDE_REF);
//		    TiffField gpsLatitudeField = jpegMetadata
//		            .findEXIFValueWithExactMatch(TiffConstants.GPS_TAG_GPS_LATITUDE);
//		    TiffField gpsLongitudeRefField = jpegMetadata
//		            .findEXIFValueWithExactMatch(TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
//		    TiffField gpsLongitudeField = jpegMetadata
//		            .findEXIFValueWithExactMatch(TiffConstants.GPS_TAG_GPS_LONGITUDE);
//		    if (gpsLatitudeRefField != null && gpsLatitudeField != null
//		            && gpsLongitudeRefField != null
//		            && gpsLongitudeField != null)
//		    {
//		        // all of these values are strings.
//		        String gpsLatitudeRef = (String) gpsLatitudeRefField.getValue();
//		        RationalNumber gpsLatitude[] = (RationalNumber[]) (gpsLatitudeField
//		                .getValue());
//		        String gpsLongitudeRef = (String) gpsLongitudeRefField
//		                .getValue();
//		        RationalNumber gpsLongitude[] = (RationalNumber[]) gpsLongitudeField
//		                .getValue();
//		
//		        RationalNumber gpsLatitudeDegrees = gpsLatitude[0];
//		        RationalNumber gpsLatitudeMinutes = gpsLatitude[1];
//		        RationalNumber gpsLatitudeSeconds = gpsLatitude[2];
//		
//		        RationalNumber gpsLongitudeDegrees = gpsLongitude[0];
//		        RationalNumber gpsLongitudeMinutes = gpsLongitude[1];
//		        RationalNumber gpsLongitudeSeconds = gpsLongitude[2];
//		
//		        // This will format the gps info like so:
//		        //
//		        // gpsLatitude: 8 degrees, 40 minutes, 42.2 seconds S
//		        // gpsLongitude: 115 degrees, 26 minutes, 21.8 seconds E
//		
//		        System.out.println("    " + "GPS Latitude: "
//		                + gpsLatitudeDegrees.toDisplayString() + " degrees, "
//		                + gpsLatitudeMinutes.toDisplayString() + " minutes, "
//		                + gpsLatitudeSeconds.toDisplayString() + " seconds "
//		                + gpsLatitudeRef);
//		        System.out.println("    " + "GPS Longitude: "
//		                + gpsLongitudeDegrees.toDisplayString() + " degrees, "
//		                + gpsLongitudeMinutes.toDisplayString() + " minutes, "
//		                + gpsLongitudeSeconds.toDisplayString() + " seconds "
//		                + gpsLongitudeRef);
//		
//		    }
		    
		}
		o_lat = 0.0d;
		o_lng = 0.0d;
		return false;
//		lat_lng = new double[0];
//		return lat_lng;
	}
	
	// Exif情報を抜き出す。
	public void showExif(ExifInterface ei) {
		// API 5
		String exifString = getExifString(ei, ExifInterface.TAG_DATETIME)
				+ "\n";
		exifString += getExifString(ei, ExifInterface.TAG_FLASH) + "\n";
		/*
		01-26 15:43:22.999: D/GeotagAdd2Image(14151): GPSLatitudeRef: N
		01-26 15:43:22.999: D/GeotagAdd2Image(14151): GPSLatitude: 35/1,0/1,21/1
		01-26 15:43:22.999: D/GeotagAdd2Image(14151): GPSLongitudeRef: E
		01-26 15:43:22.999: D/GeotagAdd2Image(14151): GPSLongitude: 138/1,23/1,17/1
		*/
		
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
		            // TiffImageMetadata class is immutable (read-only).
		            // TiffOutputSet class represents the Exif data to write.
		            //
		            // Usually, we want to update existing Exif metadata by
		            // changing
		            // the values of a few fields, or adding a field.
		            // In these cases, it is easiest to use getOutputSet() to
		            // start with a "copy" of the fields read from the image.
		            outputSet = exif.getOutputSet();
		        }
		    }
		
//		    if (null == outputSet)
//		    {
//		        // file does not contain any exif metadata. We don't need to
//		        // update the file; just copy it.
//		        IoUtils.copyFileNio(jpegImageFile, dst);
//		        return;
//		    }
		    if (null == outputSet)
		        outputSet = new TiffOutputSet();
		    {
		        // Example of how to remove a single tag/field.
		        // There are two ways to do this.
		
		        // Option 1: brute force
		        // Note that this approach is crude: Exif data is organized in
		        // directories. The same tag/field may appear in more than one
		        // directory, and have different meanings in each.
		    	
		    	// make sure to remove old value if present (this method will
		        // not fail if the tag does not exist).
//		        exifDirectory.removeField(TiffConstants.EXIF_TAG_APERTURE_VALUE);
//		        exifDirectory.removeField(TiffConstants.GPS_TAG_GPS_LATITUDE_REF);
//		        exifDirectory.removeField(TiffConstants.GPS_TAG_GPS_LATITUDE);
//		        exifDirectory.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
//		        exifDirectory.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE);
		        
//		        exifDirectory.add(aperture);
		        
//		    	outputSet.removeField(TiffConstants.EXIF_TAG_APERTURE_VALUE);
		    	outputSet.removeField(TiffConstants.GPS_TAG_GPS_LATITUDE_REF);
		    	outputSet.removeField(TiffConstants.GPS_TAG_GPS_LATITUDE);
		    	outputSet.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
		    	outputSet.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE);
//		    	
//		        // Option 2: precision
//		        // We know the exact directory the tag should appear in, in this
//		        // case the "exif" directory.
//		        // One complicating factor is that in some cases, manufacturers
//		        // will place the same tag in different directories.
//		        // To learn which directory a tag appears in, either refer to
//		        // the constants in ExifTagConstants.java or go to Phil Harvey's
//		        // EXIF website.
//		        TiffOutputDirectory exifDirectory = outputSet
//		                .getGPSDirectory();
//		        if (null != exifDirectory){
//		        	outputSet.removeField(TiffConstants.GPS_TAG_GPS_LATITUDE_REF);
//		        	outputSet.removeField(TiffConstants.GPS_TAG_GPS_LATITUDE);
//			    	outputSet.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
//			    	outputSet.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE);
//		        }
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
		            // TiffImageMetadata class is immutable (read-only).
		            // TiffOutputSet class represents the Exif data to write.
		            //
		            // Usually, we want to update existing Exif metadata by
		            // changing
		            // the values of a few fields, or adding a field.
		            // In these cases, it is easiest to use getOutputSet() to
		            // start with a "copy" of the fields read from the image.
		            outputSet = exif.getOutputSet();
		        }
		    }
		
		    // if file does not contain any exif metadata, we create an empty
		    // set of exif metadata. Otherwise, we keep all of the other
		    // existing tags.
		    if (null == outputSet)
		        outputSet = new TiffOutputSet();
		
		    {
		        // Example of how to add a field/tag to the output set.
		        //
		        // Note that you should first remove the field/tag if it already
		        // exists in this directory, or you may end up with duplicate
		        // tags. See above.
		        //
		        // Certain fields/tags are expected in certain Exif directories;
		        // Others can occur in more than one directory (and often have a
		        // different meaning in different directories).
		        //
		        // TagInfo constants often contain a description of what
		        // directories are associated with a given tag.
		        //
		        // see
		        // org.apache.commons.sanselan.formats.tiff.constants.AllTagConstants
		        //
		        TiffOutputField aperture = TiffOutputField.create(
		                TiffConstants.EXIF_TAG_APERTURE_VALUE,
		                outputSet.byteOrder, new Double(0.3));
		        TiffOutputDirectory exifDirectory = outputSet
		                .getOrCreateExifDirectory();
		        // make sure to remove old value if present (this method will
		        // not fail if the tag does not exist).
		        exifDirectory
		                .removeField(TiffConstants.EXIF_TAG_APERTURE_VALUE);
		        exifDirectory.add(aperture);
		    }
		
		    {
		        // Example of how to add/update GPS info to output set.
		
		        // New York City
//		        double longitude = -74.0; // 74 degrees W (in Degrees East)
//		        double latitude = 40 + 43 / 60.0; // 40 degrees N (in Degrees
//		        // North)
		
		        outputSet.setGPSInDegrees(longitude, latitude);
		    }
		
		    // printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME);
		
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
					// TiffImageMetadata class is immutable (read-only).
					// TiffOutputSet class represents the Exif data to write.
					//
					// Usually, we want to update existing Exif metadata by
					// changing
					// the values of a few fields, or adding a field.
					// In these cases, it is easiest to use getOutputSet() to
					// start with a "copy" of the fields read from the image.
					outputSet = exif.getOutputSet();
				}
			}

			// if file does not contain any exif metadata, we create an empty
			// set of exif metadata. Otherwise, we keep all of the other
			// existing tags.
			if (null == outputSet)
				outputSet = new TiffOutputSet();

			{
				// Example of how to add/update GPS info to output set.

				// New York City
//				double longitude = -74.0; // 74 degrees W (in Degrees East)
//				double latitude = 40 + 43 / 60.0; // 40 degrees N (in Degrees
				// North)

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
		// get all metadata stored in EXIF format (ie. from JPEG or TIFF).
		// org.w3c.dom.Node node = Sanselan.getMetadataObsolete(imageBytes);
		IImageMetadata metadata = Sanselan.getMetadata(file);

		// System.out.println(metadata);

		if (metadata instanceof JpegImageMetadata) {
			JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

			// Jpeg EXIF metadata is stored in a TIFF-based directory structure
			// and is identified with TIFF tags.
			// Here we look for the "x resolution" tag, but
			// we could just as easily search for any other tag.
			//
			// see the TiffConstants file for a list of TIFF tags.

			Log.d("metadataExample", "file: " + file.getPath());

			// print out various interesting EXIF tags.
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
			//GPS Latitude Ref: 'R98' <- what? not 'N'?
			printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LATITUDE);
			//GPS Latitude: 48, 49, 48, 48 <- what?
			printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
			//GPS Longitude Ref: 'E'
			printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LONGITUDE);
			//GPS Longitude: 138, 23, 17
			
//			System.out.println();

			// simple interface to GPS data
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
/*
GPS Description: [GPS. Latitude: 35 degrees, 0 minutes, 21 seconds N??, Longitude: 138 degrees, 23 minutes, 17 seconds E??]
GPS Longitude (Degrees East): 138.38805555555555
GPS Latitude (Degrees North): 35.005833333333335					
*/				}
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
				// all of these values are strings.
//				String gpsLatitudeRef = (String) gpsLatitudeRefField.getValue();
//				RationalNumber gpsLatitude[] = (RationalNumber[]) (gpsLatitudeField
//						.getValue());
//				String gpsLongitudeRef = (String) gpsLongitudeRefField
//						.getValue();
//				RationalNumber gpsLongitude[] = (RationalNumber[]) gpsLongitudeField
//						.getValue();
//
//				RationalNumber gpsLatitudeDegrees = gpsLatitude[0];
//				RationalNumber gpsLatitudeMinutes = gpsLatitude[1];
//				RationalNumber gpsLatitudeSeconds = gpsLatitude[2];
//
//				RationalNumber gpsLongitudeDegrees = gpsLongitude[0];
//				RationalNumber gpsLongitudeMinutes = gpsLongitude[1];
//				RationalNumber gpsLongitudeSeconds = gpsLongitude[2];
//
//				// This will format the gps info like so:
//				//
//				// gpsLatitude: 8 degrees, 40 minutes, 42.2 seconds S
//				// gpsLongitude: 115 degrees, 26 minutes, 21.8 seconds E
//
//				Log.d("metadataExample", "	" + "GPS Latitude: "
//						+ gpsLatitudeDegrees.toDisplayString() + " degrees, "
//						+ gpsLatitudeMinutes.toDisplayString() + " minutes, "
//						+ gpsLatitudeSeconds.toDisplayString() + " seconds "
//						+ gpsLatitudeRef);
//				Log.d("metadataExample", "	" + "GPS Longitude: "
//						+ gpsLongitudeDegrees.toDisplayString() + " degrees, "
//						+ gpsLongitudeMinutes.toDisplayString() + " minutes, "
//						+ gpsLongitudeSeconds.toDisplayString() + " seconds "
//						+ gpsLongitudeRef);

			}

//			System.out.println();

			List items = jpegMetadata.getItems();
			for (int i = 0; i < items.size(); i++) {
				Object item = items.get(i);
				System.out.println("	" + "item: " + item);
			}

			System.out.println();
/*
item: Interop Index: 'R98'
item: Interop Version: 48, 49, 48, 48
item: Interop Index: 'N'
item: Interop Version: 35, 0, 21
item: Unknown Tag (0x3): 'E'
item: Unknown Tag (0x4): 138, 23, 17
item: Unknown Tag (0xe): 'T'
item: Unknown Tag (0xf): 0
item: Unknown Tag (0x12): 'WGS-84'
item: Unknown Tag (0x1b): 65, 83, 67, 73, 73, 0, 0, 0, 71, 80, 83, 45, 70, 73, 88
*/
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
		// ファイルが存在しない場合にgetAttributeを呼び出したらNULLが返る
		// ファイルにEXIFが存在しない場合も同様にNULLが返る
		return tag + ": " + ei.getAttribute(tag);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}
}