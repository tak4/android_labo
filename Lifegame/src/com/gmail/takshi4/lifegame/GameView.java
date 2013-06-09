package com.gmail.takshi4.lifegame;

import com.gmail.takshi4.lifegame.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
	private Life life = null;

	private SurfaceHolder holder;
	private Thread thread;
	private Paint paint = new Paint();
	private Bitmap bitmap;

	float left;
	float top;

	public GameView(Context context) {
		super(context);
		setFocusable(true); // タッチイベントとトラックボールイベントを使うために必須

		holder = getHolder();
		holder.addCallback(this);

		paint.setColor(Color.BLUE);
		bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.mrc);
	}

	@Override
	public void run() {
		while (true) {
			life.execute();
			doDraw();
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d("TEST", "onTouchEvent");
		if( life != null ) {
			life.setLife(event.getX(), event.getY());
		}
		return true;
	}

	private void doDraw() {
		int[] map;
		Canvas canvas = holder.lockCanvas();
		if (canvas != null) {
			canvas.drawColor(Color.WHITE);
			map = life.getMap();
			for(int y = 0; y < life.getY(); y++) {
				for(int x = 0; x < life.getX(); x++) {
					if( map[y * life.getX() + x] == Life.MAP_LIFE ) {
						canvas.drawBitmap(bitmap, x * Life.C_SIZE, y * Life.C_SIZE, null);
					}
				}
			}
			holder.unlockCanvasAndPost(canvas);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// Surfaceが作成されてからcanvasを取得しないといけない
		Canvas canvas = holder.lockCanvas();
		if (canvas != null) {
			life = new Life(canvas.getWidth(), canvas.getHeight());
			Log.d("TEST", "canvas h=" + canvas.getHeight() + " w=" + canvas.getWidth());
			Log.d("TEST", "bitmap h=" + bitmap.getHeight() + " w=" + bitmap.getWidth());
			holder.unlockCanvasAndPost(canvas);
		}

		thread = new Thread(this);
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		thread = null;
	}

}
