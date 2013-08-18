package net.masanoriyono.GeotagAdd2Image;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
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
		mapView = new MapView(this, getResources().getString(R.string.map_key));
		mapView.setEnabled(true);
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		center_point = new GeoPoint(
				new Double(34.9719439293057 * 1E6).intValue(), new Double(
						138.38911074672706 * 1E6).intValue());

		intent = getIntent();
		if (intent != null) {
			Log.i("intent", "not null");
			if ("TakePicturePosition".equals(intent.getAction())) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					if ((bundle.containsKey("lat"))
							&& (bundle.containsKey("lng"))) {
						double p_lat = intent.getDoubleExtra("lat",
								34.9719439293057);
						double p_lng = intent.getDoubleExtra("lng",
								138.38911074672706);
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
		mapControl.setZoom(17);
		mapControl.setCenter(center_point);
		mapView.getMapCenter();

		Drawable mark = getResources().getDrawable(R.drawable.pin);
		mark.setBounds(0, 0, mark.getMinimumWidth(), mark.getMinimumHeight()); // これを行わないと表示されない
		MyItemizedOverlay overlay = new MyItemizedOverlay(mark);
		mapView.getOverlays().add(overlay);
		Bitmap mBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.center);

		MapOverlay center_view = new MapOverlay(mBitmap);
		mapView.getOverlays().add(center_view);
		setContentView(mapView);
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
