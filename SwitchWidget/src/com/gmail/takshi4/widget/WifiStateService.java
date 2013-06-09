/**
 *
 */
package com.gmail.takshi4.widget;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.IBinder;

/**
 * Wifi状態変化イベントリスナーを登録するサービス
 */
public class WifiStateService extends Service {

	private SwitchWidget.WifiListener wifiListener = new SwitchWidget.WifiListener();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		getApplicationContext().registerReceiver(wifiListener, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getApplicationContext().unregisterReceiver(wifiListener);
	}
}
