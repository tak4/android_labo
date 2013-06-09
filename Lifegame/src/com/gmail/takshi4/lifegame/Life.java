package com.gmail.takshi4.lifegame;

public class Life {
	private final int map_x;
	private final int map_y;

	public static final int MAP_LIFE	= 1;
	public static final int MAP_DEAD	= 0;
	public static final int C_SIZE		= 15;

	private int m_Map[];
	private boolean m_Switch;

	/**
	 * コンストラクタ
	 */
	public Life(final float xSize, final float ySize) {

		// 	生命活動 ON OFF
		m_Switch = true;

		// マップサイズ決定
		map_x = (int)(xSize / C_SIZE);
		map_y = (int)(ySize / C_SIZE);

		// ゲームマップ
		m_Map = new int [map_x * map_y];

		// 初期化
		for(int y = 0; y < map_y; y++) {
			for(int x = 0; x < map_x; x++) {
					m_Map[ (y * map_x) + x] = MAP_DEAD;
			}
		}
	}


	/**
	 * ゲーム処理
	 */
	public void execute() {
		if(m_Switch) {
			stepLife();
		}
	}

	/**
	 *
	 */
	public int[] getMap() {
		return m_Map;
	}

	public int getX()
	{
		return map_x;
	}

	public int getY()
	{
		return map_y;
	}

	/**
	 * 生命配置
	 */
	public void setLife(final float xPoint, final float yPoint) {
		int x = (int)(xPoint / C_SIZE);
		int y = (int)(yPoint / C_SIZE);

		for(int i = 0; i < 10; i++) {
			int tx = (int)(Math.random() * 10) - 5;
			int ty = (int)(Math.random() * 10) - 5;

			int lifeSet = (y+ty) * map_x + (x+tx);
			if( lifeSet >= (map_x * map_y) ) {
				lifeSet = map_x * map_y - 1;
			}
			else if( lifeSet < 0 ) {
				lifeSet = 0;
			}
			m_Map[lifeSet] = MAP_LIFE;
		}
//		m_Map[y * map_x + x] = MAP_LIFE;
	}

	/**
	 * 生命活動
	 */
	public void stepLife() {
		int count[] = new int[map_x * map_y];
		for(int y = 0; y < map_y; y++) {
			for(int x = 0; x < map_x; x++) {
				count[y * map_x + x] = LifeAroundCk(x, y);
			}
		}
		for(int y = 0; y < map_y; y++) {
			for(int x = 0; x < map_x; x++) {
				if(m_Map[y * map_x + x] == MAP_DEAD) {
					if(count[y * map_x + x] == 3) {
						m_Map[y * map_x + x] = MAP_LIFE;
					}
				}
				else
				{
					if(count[y * map_x + x] <= 1) {
						m_Map[y * map_x + x] = MAP_DEAD;
					}
					if(count[y * map_x + x] >= 4) {
						m_Map[y * map_x + x] = MAP_DEAD;
					}
				}
			}
		}
	}

	/**
	 * 周囲のライフカウント
	 */
	private int LifeAroundCk(int x, int y) {
		int count = 0;
		if(y+1<map_y					&& m_Map[(y+1)*map_x+x]		!= MAP_DEAD) count++;
		if(y+1<map_y	&& x+1<map_x	&& m_Map[(y+1)*map_x+(x+1)]	!= MAP_DEAD) count++;
		if(x+1<map_x					&& m_Map[y*map_x+(x+1)]		!= MAP_DEAD) count++;
		if(y-1>=0		&& x+1<map_x	&& m_Map[(y-1)*map_x+(x+1)]	!= MAP_DEAD) count++;

		if(y-1>=0 				 		&& m_Map[(y-1)*map_x+x]		!= MAP_DEAD) count++;
		if(y-1>=0		&& x-1>=0		&& m_Map[(y-1)*map_x+(x-1)]	!= MAP_DEAD) count++;
		if(x-1>=0 				 		&& m_Map[y*map_x+(x-1)]		!= MAP_DEAD) count++;
		if(y+1<map_y	&& x-1>=0		&& m_Map[(y+1)*map_x+(x-1)]	!= MAP_DEAD) count++;

		return count;
	}

	/**
	 * 値の反転
	 */
	public void changeSwitch() {
		m_Switch = !m_Switch;
	}

	/**
	 *
	 */
	public void urawaza() {
		for(int y = 0; y < map_y; y++) {
			for(int x = 0; x < map_x; x++) {
				m_Map[(y*map_x)+x] = (int)(Math.random()*2);
			}
		}
	}

}
