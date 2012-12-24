package net.masanoriyono.GeotagAdd2Image;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Point;
//import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
//import android.view.View;
//import android.view.View;
//import android.view.ViewGroup.LayoutParams;
//import android.widget.ImageView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Overlay;
public class MapViewActivity extends MapActivity {
	/** Called when the activity is first created. */
	private GeoPoint center_point;

	class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
		public MyItemizedOverlay(Drawable defaultMarker) {
			super(defaultMarker);
			boundCenterBottom(defaultMarker);
			populate();
		}

		@Override
		protected OverlayItem createItem(int index) {
			OverlayItem item = new OverlayItem(center_point, "撮影位置", "撮影位置");
			return item;
		}

		@Override
		public int size() {
			return 1;
		}
	}

	// class BitmapView extends View {
	// private Bitmap bmp;
	//
	// public BitmapView(Context context) {
	// super(context);
	// setFocusable(true);
	// bmp = BitmapFactory.decodeResource(this.getResources(),
	// R.drawable.center);
	// }
	// protected void onDraw(Canvas canvas) {
	// canvas.drawColor(Color.TRANSPARENT);
	// canvas.drawBitmap(bmp, 100, 150, null);
	// }
	// }

	public class MapOverlay extends Overlay {
		private Bitmap mBitmap;

		public MapOverlay(Bitmap bitmap) {
			this.mBitmap = bitmap;
		}

		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			canvas.drawBitmap(mBitmap,
					mapView.getWidth() / 2 - mBitmap.getWidth() / 2,
					mapView.getHeight() / 2 - mBitmap.getHeight() / 2, null);
		}
	}

	private Intent intent;
	private MapView mapView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// MapView mapView = new MapView(this,
		// getResources().getString(R.string.map_key));
		// mapView.setEnabled(true);
		// mapView.setClickable(true);
		// mapView.setBuiltInZoomControls(true);
		// final MyLocationOverlay overlay = new
		// MyLocationOverlay(getApplicationContext(), mapView);
		// overlay.onProviderEnabled(LocationManager.GPS_PROVIDER);
		// overlay.enableMyLocation();
		// overlay.runOnFirstFix(new Runnable() {
		// @Override
		// public void run() {
		// mapView.getController().animateTo(overlay.getMyLocation());
		// }
		// });
		// mapView.getOverlays().add(overlay);
		// mapView.invalidate();
		// setContentView(mapView);

		//
		// Drawable mark = getResources().getDrawable(R.drawable.center);
		// //mark.setBounds(0, 0, mark.getMinimumWidth(),
		// mark.getMinimumHeight()); // これを行わないと表示されない
		// mark.setBounds(0, 0, 100, 100); // これを行わないと表示されない
		// MyItemizedOverlay overlay = new MyItemizedOverlay(mark);
		// mapView.getOverlays().add(overlay);
		// setContentView(mapView);

		mapView = new MapView(this, getResources().getString(R.string.map_key));
		mapView.setEnabled(true);
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		// 地図のセンターを撮影位置にする。
		center_point = new GeoPoint(
				new Double(34.9719439293057 * 1E6).intValue(), new Double(
						138.38911074672706 * 1E6).intValue());

		// インテントから取得する予定。
		intent = getIntent();
		if (intent != null) {
			Log.i("intent", "not null");
			if ("TakePicturePosition".equals(intent.getAction())) {
				Bundle bundle = intent.getExtras();
				// これがnullの場合があるのにチェックせずに
				// キーの存在をチェックしていたのでエラーが起きていた。
				if (bundle != null) {
					if ((bundle.containsKey("lat"))
							&& (bundle.containsKey("lng"))) {
						// どうもgetParcelableではキーが存在しない場合はnullが戻ってくる。
						// Bitmap bitmap_Paint2SS =
						// (Bitmap)bundle.getParcelable("net.masanoriyono.Paint2SS.BITMAP");
						double p_lat = intent.getDoubleExtra("lat",
								34.9719439293057);
						double p_lng = intent.getDoubleExtra("lng",
								138.38911074672706);
//						if (p_lat < 30) {
//							p_lat = 34.9719439293057;
//						}
//						if (p_lng < 130) {
//							p_lng = 138.38911074672706;
//						}
						// String Paint2SS_imagefile =
						// intent.getStringExtra("net.masanoriyono.Paint2SS.BITMAP");
						Log.i("TakePicturePosition", "pos: " + p_lat + ","
								+ p_lng);

						center_point = new GeoPoint(
								new Double(p_lat * 1E6).intValue(), new Double(
										p_lng * 1E6).intValue());
					}
				}
			}
		}

		MapController mapControl = mapView.getController();
		// 地図の縮尺設定
		mapControl.setZoom(17);

		// 地図の中心座標設定
		mapControl.setCenter(center_point);

		mapView.getMapCenter();

		Drawable mark = getResources().getDrawable(R.drawable.pin);
		mark.setBounds(0, 0, mark.getMinimumWidth(), mark.getMinimumHeight()); // これを行わないと表示されない
		MyItemizedOverlay overlay = new MyItemizedOverlay(mark);
		mapView.getOverlays().add(overlay);

		// Drawable center_mark = getResources().getDrawable(R.drawable.center);
		// center_mark.setBounds(0, 0, center_mark.getMinimumWidth(),
		// center_mark.getMinimumHeight()); // これを行わないと表示されない
		// MyItemizedOverlay center_overlay = new
		// MyItemizedOverlay(center_mark);
		// mapView.getOverlays().add(center_overlay);

		Bitmap mBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.center);

		MapOverlay center_view = new MapOverlay(mBitmap);
		mapView.getOverlays().add(center_view);

		// ImageView center_view = new ImageView(this);
		// center_view.setId(2);
		// //center_view.setBackgroundColor(Color.TRANSPARENT);
		// center_view.setImageResource(R.drawable.center);
		// center_view.setScaleType(ImageView.ScaleType.CENTER);

		// BitmapView center_view = new BitmapView(this.getApplication());
		//
		setContentView(mapView);

		// mapView.addView(center_view);

		// setContentView(R.layout.main);
		// // レイアウトファイルに記述した MapViewオブジェクト取得
		// MapView mapview = (MapView) findViewById(R.id.mapview);
		//
		// // MapViewをコントロールするためのオブジェクト取得
		// MapController mapctrl = mapview.getController();
		//
		// // 地図の縮尺設定
		// mapctrl.setZoom(16);
		//
		// // 地図の中心座標設定
		// mapctrl.setCenter(new GeoPoint((int) (35.45530345132602 * 1E6),
		// (int) (139.6365491316008 * 1E6)));
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		// 戻るボタンが押されたとき
		if (e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			// ボタンが押されたとき
			if (e.getAction() == KeyEvent.ACTION_DOWN) {
				double c_lat = new Double(mapView.getMapCenter()
						.getLatitudeE6()) / 1E6;
				double c_lng = new Double(mapView.getMapCenter()
						.getLongitudeE6()) / 1E6;

				Log.i("end", "map center:" + c_lat + " , " + c_lng);
				// OKの戻り値の指定
				Intent intent = new Intent();
				intent.putExtra("c_lat", c_lat);
				intent.putExtra("c_lng", c_lng);
				setResult(Activity.RESULT_OK, intent);

			}
			// ボタンが離されたとき
			else if (e.getAction() == KeyEvent.ACTION_UP) {

			}

		}

		return super.dispatchKeyEvent(e);
	}
}
