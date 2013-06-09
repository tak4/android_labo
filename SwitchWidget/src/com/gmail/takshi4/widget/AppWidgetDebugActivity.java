package com.gmail.takshi4.widget;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * ウィジェットデバッグ用のアクティビティ
 * これが無いとデバッグ出来ない！？
 * ブレークポイントを利用するには、onCreate() で、
 * android.os.Debug.waitForDebugger();という記述を入れる必要があるとの情報をみかけたが
 * 入れてもブレーク出来ない
 */
public class AppWidgetDebugActivity extends Activity {
    /**
     * デバッグ用Activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	//レイアウトの生成
    	final LinearLayout layout = new LinearLayout(this);
    	layout.setOrientation(LinearLayout.VERTICAL);
    	setContentView(layout);

    	//テキストビューの生成
    	final TextView textView = new TextView(this);
    	textView.setText("AppWidgetDebugActivity：戻るボタンで画面を消去してください。");
    	layout.addView(textView);

    	// デバッグ用
    	android.os.Debug.waitForDebugger();

    	super.onCreate(savedInstanceState);
    }
}