/**
 *
 */
package com.gmail.takshi4.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Widget本体クラス
 */
public class SwitchWidget extends AppWidgetProvider {
	private static final String TAG = "SwitchWidgetDebug";

	private static final String TOGGLE_ACTION = "TOGGLE_ACTION";
	private static final String ICON_GOING_OUT = "ICON_GOING_OUT";
	private static final String ICON_RETURN_HOME = "ICON_RETURN_HOME";
	private static final String ICON_TRANSITION1 = "ICON_TRANSITION1";
	private static final String ICON_TRANSITION2 = "ICON_TRANSITION2";

	/**
	 * Wifi状態遷移状態 true 遷移中
	 */
	private static boolean running = false;

	/**
	 * Wifiイベント処理サービス起動状態 true 起動中
	 */
	private static boolean startService = false;

	/**
	 * 更新処理
	 * @param	context	コンテキスト
	 * @param	appWidgetManager	ウィジェットマネージャ
	 * @param	appWidgetIds	ウィジェットを識別するID
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// Widget更新処理を呼ぶ
		for(int i = 0; i < appWidgetIds.length; i++) {
			int appWidgetId = appWidgetIds[i];
			updateWidget(context, appWidgetManager, appWidgetId);
		}
	}

	/**
	 * 削除処理
	 * @param	context	コンテキスト
	 * @param	appWidgetIds	ウィジェットを識別するID
	 */
	@Override
	public void onDeleted(Context context, int appWidgetIds[]) {
	}

	/**
	 * Enable処理
	 * @param	context	コンテキスト
	 */
	@Override
	public void onEnabled(Context context){
	}

	/**
	 * Disable処理
	 * @param	context	コンテキスト
	 */
	@Override
	public void onDisabled(Context context){
		// アイコン更新用スレッド停止 スレッド動作中にウィジェット削除された場合の対応
		running = false;
		// サービス停止
		if(startService){
			context.stopService(new Intent(context, WifiStateService.class));
		}
	}

	/**
	 * ウィジェットのEvent発生時に呼び出されToastを表示する
	 * @param	context	コンテキスト
	 * @param	intent	Intent
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		super.onReceive(context, intent);
		if(action.equals(AppWidgetManager.ACTION_APPWIDGET_DELETED)){
			//			Toast.makeText(context, "Delete", Toast.LENGTH_LONG).show();

		}else if(action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)){
			//			Toast.makeText(context, "Udpate", Toast.LENGTH_LONG).show();

		}else if(action.equals(AppWidgetManager.ACTION_APPWIDGET_ENABLED)){
			//			Toast.makeText(context, "Enable", Toast.LENGTH_LONG).show();

		}else if(action.equals(AppWidgetManager.ACTION_APPWIDGET_DISABLED)){
			//			Toast.makeText(context, "Disabled", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * ホームスクリーンウィジェットの更新
	 */
	private void updateWidget(Context context, AppWidgetManager manager,int id) {
		// リモートビューの生成
		final RemoteViews remoteView = new RemoteViews(context.getPackageName(),R.layout.switch_widget_layout);
		// アイコン画像
		if(isHomeMode(context)){
			remoteView.setImageViewResource(R.id.toggleButton, R.drawable.idle);
		}
		else {
			remoteView.setImageViewResource(R.id.toggleButton, R.drawable.awake);
		}

		// ボタン押下時に呼び出されるクラス
		final Intent intentPast = new Intent(context, ButtonReceiver.class);
		intentPast.setAction(TOGGLE_ACTION);
		intentPast.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);

		// ボタン押下時に実行されるPendingIntentを取得
		// リクエストコードに必ずウィジェットのIDを指定すること
		PendingIntent pendingIntentPast = PendingIntent.getBroadcast(context, id,intentPast, 0);

		// ボタン押下時にButtonReceiverクラスを呼び出す設定
		remoteView.setOnClickPendingIntent(R.id.toggleButton, pendingIntentPast);

		// IDに指定したウィジェットを描画
		manager.updateAppWidget(id,remoteView);
	}

	/**
	 * ウィジェットのボタン押下時に呼び出されるレシーバー
	 * TODO staticクラスでないとExceptionになる
	 */
	public static class ButtonReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(final Context context, Intent intent) {
			//			Toast.makeText(context, "ButtonReceiver::onReceive", Toast.LENGTH_SHORT).show();

			// Wifi設定チェック
			// 状態遷移中であれば、設定更新を処理を行わない
			final WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
			boolean wifiSkip = false;
			int wifiState = wifiManager.getWifiState();
			switch (wifiState) {
			case WifiManager.WIFI_STATE_ENABLING:
			case WifiManager.WIFI_STATE_DISABLING:
			case WifiManager.WIFI_STATE_UNKNOWN:
				wifiSkip = true;
				break;
			default:
				wifiSkip = false;
				break;
			}
			if(wifiSkip == true){
				Log.i(TAG, "wifi skip : " + wifiState);
				return;
			}

			// Wifiイベントを受ける為のサービス起動
			// サービスの停止はしない。本Widget以外でのWifi設定の変化を検出する為
			// TODO 本当はonEnabled()で起動したいが、そこで起動するとボタンイベントが受けられなくなる。(エミュレータだと受けられるのに・・・)
			if(!startService){
				//				Toast.makeText(context, "startService", Toast.LENGTH_LONG).show();

				// registerReceiverはmain thread(ActivityとかServiceのスレッド？)以外では呼べないらしい
				// ここで呼ぶとReceiverCallNotAllowedExceptionが発生する。
				// よってServiceを起動し、ServiceでregisterReceiverを呼び出す。
//				context.registerReceiver(new WifiListener(), new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
				context.startService(new Intent(context, WifiStateService.class));
				startService = true;
			}

			// Wifi設定変更
			// android.permission.CHANGE_WIFI_STATE が必要
			if(wifiState == WifiManager.WIFI_STATE_ENABLED){
				wifiManager.setWifiEnabled(false);	// true or false
			}
			else if(wifiState == WifiManager.WIFI_STATE_DISABLED) {
				wifiManager.setWifiEnabled(true);	// true or false
			}
			else {
				// ありえない
			}
		}
	}

	/**
	 * Wifi状態変化イベントを受けるリスナー
	 * @author takashi
	 *
	 */
	public static class WifiListener extends BroadcastReceiver {

		/**
		 * Wifi状態イベントを受ける
		 */
		@Override
		public void onReceive(final Context context, Intent intent) {
			Log.i(TAG, "WIFI has changed start");
			final int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
			Log.i(TAG, "WIFI State = " + wifiState);

			switch (wifiState) {
			case WifiManager.WIFI_STATE_ENABLING:
			case WifiManager.WIFI_STATE_DISABLING:
				running = true;
				break;
			default:
				running = false;
				break;
			}

			if(running){
				//
				// Wifi状態遷移中 Widget更新スレッド起動
				//
				Thread thread = new Thread(){
					@Override
					public void run() {
						boolean toggle = false;
						while(running){
							Log.i(TAG, "WIFI ...");
							if(toggle){
								updateIcon(context, ICON_TRANSITION1);
								toggle = false;
							} else {
								updateIcon(context, ICON_TRANSITION2);
								toggle = true;
							}
							try {
								Thread.sleep(500);
							} catch (Exception e) {
							}
						}

					}
				};
				thread.start();
			}
			else {
				Log.i(TAG, "WIFI end wifiState=" + wifiState);
				switch (wifiState) {
				case WifiManager.WIFI_STATE_ENABLED:
					updateIcon(context, ICON_RETURN_HOME);
					break;
				case WifiManager.WIFI_STATE_DISABLED:
					updateIcon(context, ICON_GOING_OUT);
					break;
				default:
					break;
				}
			}
		}
	}

	/**
	 * Wifi設定を見て家モードかをチェックする
	 * @param context
	 * @return
	 */
	private boolean isHomeMode(final Context context) {
		// Wifi設定変更
		final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		boolean isHome = false;
		int wifiState = wifiManager.getWifiState();
		switch (wifiState) {
		case WifiManager.WIFI_STATE_ENABLED:
			isHome = true;
			break;
		default:
			isHome = false;
			break;
		}
		return isHome;
	}

	/**
	 * アイコンの更新
	 * @param context
	 * @param status
	 */
	private static void updateIcon(final Context context, final String status) {
		//リモートビューの生成
		RemoteViews remoteView = new RemoteViews(context.getPackageName(),R.layout.switch_widget_layout);
		Log.i(TAG, "updateIcon : " + context.getPackageName());

		// アイコン画像
		if(status.equals(ICON_GOING_OUT)){
			remoteView.setImageViewResource(R.id.toggleButton, R.drawable.awake);
		} else if(status.equals(ICON_RETURN_HOME)) {
			remoteView.setImageViewResource(R.id.toggleButton, R.drawable.idle);
		} else if(status.equals(ICON_TRANSITION1)) {
			remoteView.setImageViewResource(R.id.toggleButton, R.drawable.flap1);
		} else if(status.equals(ICON_TRANSITION2)) {
			remoteView.setImageViewResource(R.id.toggleButton, R.drawable.flap2);
		} else {

		}
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		ComponentName thisWidget = new ComponentName(context, SwitchWidget.class);

		//設定した内容を同じウィジェットの全てに描画
		manager.updateAppWidget(thisWidget, remoteView);

//		//IDに指定したウィジェットを描画
//		Bundle extras = intent.getExtras();
//
//		//Intentに指定した値を取得できない場合下記の処理を実行することで取得できる場合がある
//		//			if(extras != null){
//		//				extras.getInt(
//		//			            AppWidgetManager.EXTRA_APPWIDGET_ID,
//		//			            AppWidgetManager.INVALID_APPWIDGET_ID);
//		//			}
//		int id = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
//		manager.updateAppWidget(id,remoteView);
	}
}
