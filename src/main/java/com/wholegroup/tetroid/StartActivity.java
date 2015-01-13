/*
 * Copyright (C) 2014 Andrey Rychkov <wholegroup@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.wholegroup.tetroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class StartActivity extends Activity implements View.OnClickListener
{
	/** Идентификатор меню 'старт' */
	private static final int MENU_START_ID = Menu.FIRST + 1;

	/** Идентификатор меню 'очки' */
	private static final int MENU_SCORE_ID = Menu.FIRST + 2;

	/** Идентификатор меню 'настройки' */
	private static final int MENU_SETTINGS_ID = Menu.FIRST + 3;

	/** Идентификатор меню 'выход' */
	private static final int MENU_EXIT_ID = Menu.FIRST + 4;
	
	/** Ширина изображения */
	private final static int STARTIMAGE_WIDTH = 320;
	
	/** Высота изображения */
	private final static int STARTIMAGE_HEIGHT = 440;
	
	/** Коэффицент сторон */
	private final static float STARTIMAGE_KOEF = (float)STARTIMAGE_WIDTH / STARTIMAGE_HEIGHT;
	
	/** Ширина надписи */
	private final static int TEXTIMAGE_WIDTH = 57;
	
	/** Высота надписи */
	private final static int TEXTIMAGE_HEIGHT = 13;
	
	/** Смещение надписи относительно начальных размеров изображения по оси X */
	private final static int TEXTIMAGE_MARGIN_X = 133;

	/** Смещение надписи относительно начальных размеров изображения по оси Y */
	private final static int TEXTIMAGE_MARGIN_Y = 415;

	/** Ширина рабочего окна */
	private int m_iWidth = 0;
	
	/** Высота рабочего окна */
	private int m_iHeight = 0;

	/** Слой для вывода изображения */
	private View m_viewImage;
	
	/** Слой для вывода надписи 'PRESS MENU' */
	private View m_viewText;
	
	/** Время в мс для анимации надписи */
	private final static int TEXT_REFRESH_TIME = 500;
	
	/** Медиаплеер */
	MediaPlayer m_mPlayer;
	
	/** Настройки программы */
	SharedPreferences m_settings;
	
	/** Класс для создания анимации */
	private RefreshHandler m_RedrawHandler = new RefreshHandler();
	
	class RefreshHandler extends Handler
	{
		/** */
		@Override
		public void handleMessage(Message msg)
		{
			if (View.VISIBLE == m_viewText.getVisibility())
			{
				m_viewText.setVisibility(View.INVISIBLE);
			}
			else
			{
				m_viewText.setVisibility(View.VISIBLE);
			}
			
			refresh(TEXT_REFRESH_TIME);
		}

		/** */
		public void refresh(long delayMillis)
		{
			this.removeMessages(0);

			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	}
	
	/**
	 * Создание активности.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		//
		super.onCreate(savedInstanceState);

		// установка видимого слоя
		setContentView(R.layout.start);

		// ссылка на слой с изображением
		m_viewImage = findViewById(R.id.start_layout_image);

		// установка обработчика клика на изображении
		m_viewImage.setOnClickListener(this);
		
		// ссылка на слой с надписью 'PRESS MENU'
		m_viewText = findViewById(R.id.start_layout_text);
		
		// настройки программы
   	m_settings = getSharedPreferences(getString(R.string.preferences_id), 0);
	}

	/**
	 * Создание пунктов меню.
	 * - старт
	 * - очки
	 * - настройки
	 * - выход
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(0, MENU_START_ID, 0, R.string.start_menu_start)
			.setIcon(R.drawable.menu_start);
		menu.add(0, MENU_SCORE_ID, 0, R.string.start_menu_score)
			.setIcon(R.drawable.menu_score);
		menu.add(0, MENU_SETTINGS_ID, 0, R.string.start_menu_settings)
			.setIcon(R.drawable.menu_settings);
		menu.add(0, MENU_EXIT_ID, 0, R.string.start_menu_exit)
			.setIcon(R.drawable.menu_exit);

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Обработка меню.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			// запуск
			case MENU_START_ID:
				startActivity(new Intent(StartActivity.this, PlayActivity.class));
				break;

			// очки
			case MENU_SCORE_ID:
				startActivity(new Intent(StartActivity.this, ScoreActivity.class));
				break;

			// настройки
			case MENU_SETTINGS_ID:
				startActivity(new Intent(StartActivity.this, SettingsActivity.class));
				break;

			// выход
			case MENU_EXIT_ID:
				finish();
				break;

			default:
				return super.onOptionsItemSelected(item);
		}

		return true;
	}

	/**
	 * Обработка изменения фокуса.
	 * - получение размеров рабочего окна
	 * - установка размеров изображения
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) 
	{
		super.onWindowFocusChanged(hasFocus);

		if ((0 == m_iWidth) || (0 == m_iHeight))
		{
			m_iWidth  = m_viewImage.getWidth();
			m_iHeight = m_viewImage.getHeight();
			
			SetImageSize();
		}
	}
	
	/**
	 * Установка размеров изображения в зависимости от разрешения экрана.
	 */
	private void SetImageSize()
	{
		int iImageWidth;
		int iImageHeight;

		float fWork = (float)m_iWidth / m_iHeight;

		if (fWork < STARTIMAGE_KOEF)
		{
			iImageWidth  = m_iWidth;
			iImageHeight = (int)(m_iWidth / STARTIMAGE_KOEF);
		}
		else
		{
			iImageHeight = m_iHeight;  
			iImageWidth  = (int)(m_iHeight * STARTIMAGE_KOEF);
		}

		//
		FrameLayout.LayoutParams lpImage = new FrameLayout.LayoutParams(iImageWidth, iImageHeight);

		lpImage.gravity = Gravity.CENTER;
						
		m_viewImage.setLayoutParams(lpImage);
		
		// установка размеров и положения текста
		LinearLayout.LayoutParams lpText = new LinearLayout.LayoutParams(
				TEXTIMAGE_WIDTH * iImageWidth / STARTIMAGE_WIDTH, 
				TEXTIMAGE_HEIGHT * iImageHeight / STARTIMAGE_HEIGHT);

		lpText.setMargins(
				(m_iWidth - iImageWidth) / 2 + TEXTIMAGE_MARGIN_X * iImageWidth / STARTIMAGE_WIDTH, 
				(m_iHeight - iImageHeight) / 2 + TEXTIMAGE_MARGIN_Y * iImageHeight / STARTIMAGE_HEIGHT, 
				0, 
				0);
		
		m_viewText.setLayoutParams(lpText);
		
		// запуск анимации
		m_RedrawHandler.refresh(TEXT_REFRESH_TIME);
	}
	
	/**
	 * Обработка нажатия на экран.
	 * - показываем меню
	 */
	public void onClick(View v)
	{
		getWindow().openPanel(Window.FEATURE_OPTIONS_PANEL, null);
   }
	
	/**
	 * Запуск активности.
	 * - проигрывание мелодии
	 */
	@Override
	public void onResume()
	{
		super.onResume();

		boolean bSound = m_settings.getBoolean(getString(R.string.preferences_settings_sound),
			Boolean.parseBoolean(getString(R.string.preferences_settings_sound_default)));

		if (null != m_mPlayer)
		{
			m_mPlayer.seekTo(0);
			m_mPlayer.setLooping(true);
		}
		else
		{
	   	// создание медиаплеер
			if (bSound)
			{
				m_mPlayer = MediaPlayer.create(this, R.raw.start);
				m_mPlayer.setLooping(true);
			}
		}

		if (bSound && (null != m_mPlayer))
		{
			m_mPlayer.start();
		}
	}
	
	/**
	 * Остановка активности.
	 * - остановка мелодии
	 */
	@Override
	public void onPause()
	{
		if (null != m_mPlayer)
		{
			m_mPlayer.setLooping(false);
			
			if (m_mPlayer.isPlaying())
			{
				m_mPlayer.pause();
			}
		}
		
		super.onPause();
	}
	
	/**
	 * Уничтожение активности.
	 */
	@Override
	public void onDestroy()
	{
		if (null != m_mPlayer)
		{
			if (m_mPlayer.isPlaying())
			{
				m_mPlayer.stop();
			}
			
			m_mPlayer.release();

			m_mPlayer = null;
		}
		
		super.onDestroy();
	}
}

