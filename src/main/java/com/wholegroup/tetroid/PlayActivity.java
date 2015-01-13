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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;

public class PlayActivity extends Activity
{
	/** Класс игры */
	private Tetroid m_cTetroid;

	/** Класс для отрисовки игрового поля */
	private PlayView m_viewPlay;
	
	/** Слой "ПАУЗА" */
	private View m_viewPause;
	
	/** Слой "КОНЕЦ ИГРЫ" */
	private View m_viewStop;
	
	/** Уровень игры */
	private int m_iLevel = 0;
	
	/** Статистика фигур */
	private int[] m_statLinesLast;
	
	/** Ширина экрана */
	public int m_iWidth = 0;
	
	/** Высота экрана */
	public int m_iHeight = 0;
	
	/** Не выводит слой "ПАУЗА (используется для вывода диалога подсчета бонусов) */
	boolean m_bNotVisiblePause = false;

	/** Код возврата активности "БОНУС" */
	private static int ACTIVITY_RESULT_BONUS = 0; 
	
	/** Звук сброса фигуры */
	private MediaPlayer m_mpDrop;
	
	/** Буфер для конвертирования чисел */
	public StringBuffer m_sbNumber;
	
	/** Класс для создания анимации */
	private RefreshHandler m_RedrawHandler = new RefreshHandler();
	
	class RefreshHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			PlayActivity.this.updateTimer();
		}

		public void sleep(long delayMillis)
		{
			this.removeMessages(0);

			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
		
		public void stop()
		{
			this.removeMessages(0);
		}
	}
	
	/**
	 * Конструктор.
	 */
	public PlayActivity()
	{
		m_cTetroid = new Tetroid();

		m_cTetroid.init();

		m_statLinesLast = new int[Tetroid.FIGUREGRID];
		
		// буффер для вывода числовой статистики
		m_sbNumber = new StringBuffer();
	}
	
	/**
	 * Создание активности.
	 */
	@Override 	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// установка контента
		setContentView(R.layout.play);
		
		// получаем класс игрового окна
		m_viewPlay = (PlayView)findViewById(R.id.playview);

		m_viewPlay.m_cTetroid = m_cTetroid;

		// слой "ПАУЗА"
		m_viewPause = findViewById(R.id.playview_layoutpause);

		// слой "КОНЕЦ ИГРЫ"
		m_viewStop= findViewById(R.id.play_gameover);
		
		// установка обработчика на нажатие кнопки возобновления игры
		findViewById(R.id.playview_layoutpause_button).setOnClickListener(m_ResumeListener);
		
		// установка обработчика на нажатие кнопки на экране Game Over
		findViewById(R.id.play_gameover_btn_next).setOnClickListener(m_EndListener);
		
		// загрузка настроек игры
   	SharedPreferences settings = getSharedPreferences(getString(R.string.preferences_id), 0);

   	m_viewPlay.m_bDrawGrid = settings.getBoolean(
			getString(R.string.preferences_settings_grid),
			Boolean.parseBoolean(getString(R.string.preferences_settings_grid_default)));
   	m_viewPlay.m_bFigureHelp = settings.getBoolean(
			getString(R.string.preferences_settings_promt),
			Boolean.parseBoolean(getString(R.string.preferences_settings_promt_default)));
   	m_viewPlay.m_bFigureNext = settings.getBoolean(
			getString(R.string.preferences_settings_next),
			Boolean.parseBoolean(getString(R.string.preferences_settings_next_default)));
   	m_viewPlay.m_bColorGrid = settings.getBoolean(
			getString(R.string.preferences_settings_colorgrid),
			Boolean.parseBoolean(getString(R.string.preferences_settings_colorgrid_default)));
   	m_viewPlay.m_bFigureClassic = settings.getBoolean(
			getString(R.string.preferences_settings_figure_classic),
			Boolean.parseBoolean(getString(R.string.preferences_settings_figure_classic_default)));
   	
		// восстанавливаем состояние игры при необходимости
		if (null == savedInstanceState)
		{
			startTimer();
		}
		else
		{
			m_cTetroid.fromJSON(savedInstanceState.getString(
				getString(R.string.play_parameter_instance)));

			m_iLevel = m_cTetroid.m_iLevel;
			
			try
			{
				JSONArray jsonArray = new JSONArray(savedInstanceState.getString(
					getString(R.string.play_parameter_statlines)));

				for (int i = 0; i < jsonArray.length(); i++)
				{
					if (Tetroid.FIGUREGRID <= i)
					{
						continue;
					}
					
					m_statLinesLast[i] = jsonArray.getInt(i);
				}
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			
			if (View.INVISIBLE == m_viewStop.getVisibility())
			{
				m_viewPause.setVisibility(View.VISIBLE);

				findViewById(R.id.playview_layoutpause_button).requestFocus();
			}
		}
		
		// обработчики нажатия клавиш
		ImageButton btnImage;

		btnImage = (ImageButton)findViewById(R.id.play_btn_left);
		btnImage.setOnClickListener(m_btnLeftListener);

		btnImage = (ImageButton)findViewById(R.id.play_btn_right);
		btnImage.setOnClickListener(m_btnRightListener);

		btnImage = (ImageButton)findViewById(R.id.play_btn_rotate_left);
		btnImage.setOnClickListener(m_btnRotateLeftListener);

		btnImage = (ImageButton)findViewById(R.id.play_btn_rotate_right);
		btnImage.setOnClickListener(m_btnRotateRightListener);

		btnImage = (ImageButton)findViewById(R.id.play_btn_down);
		btnImage.setOnClickListener(m_btnDownListener);
		
		// загрузка звуков
		if (settings.getBoolean(getString(R.string.preferences_settings_sound),
			Boolean.parseBoolean(getString(R.string.preferences_settings_sound_default))))
		{
			m_mpDrop = MediaPlayer.create(this, R.raw.drop);
		}
	}
	
	/**
	 * Возобновление состояния игры.
	 */
	@Override
	protected void onResume()
	{
		super.onResume();
	}
	
	/**
	 * Установка паузы.
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
		
		// останавливаем игру
		stopTimer();
		
		// выводим слой паузы
		if (!isFinishing() && !m_bNotVisiblePause && (View.INVISIBLE == m_viewStop.getVisibility()))
		{
			m_viewPause.setVisibility(View.VISIBLE);

			findViewById(R.id.playview_layoutpause_button).requestFocus();
		}
		else
		{
			m_bNotVisiblePause = false;
		}
	}
	
	/**
	 * Сохранение состояния игры.
	 */
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		String strStatLines = "";

		try
		{
			strStatLines = new JSONArray(Arrays.toString(m_statLinesLast)).toString();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		
		outState.putString(getString(R.string.play_parameter_instance), m_cTetroid.toJSON());
		outState.putString(getString(R.string.play_parameter_statlines), strStatLines);

		super.onSaveInstanceState(outState);
	}

	/**
	 *  Обработка нажатия кнопки возобновления игры.
	 */
	OnClickListener m_ResumeListener = new OnClickListener()
	{	
        public void onClick(View v)
        {
      	  m_viewPause.setVisibility(View.INVISIBLE);

      	  startTimer();
        }
	};
	
	/**
	 * Обработка нажатия кнопки на экране Game Over.
	 * - переход к таблице рекордов 
	 */
	OnClickListener m_EndListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			Intent intent = new Intent(PlayActivity.this, ScoreActivity.class);

			intent.putExtra(getString(R.string.score_parameter_score), m_cTetroid.m_iScore);
			intent.putExtra(getString(R.string.score_parameter_lines), m_cTetroid.m_iLines);

			startActivity(intent);
			
			finish();
		}
	};
	
	/** 
	 * Обработчик нажатия клавиш.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (View.VISIBLE == m_viewPause.getVisibility())
		{
			return super.onKeyDown(keyCode, event);
		}
		
		switch (keyCode)
		{
			// сбросить фигуру вниз
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_SPACE:
			{
				if (m_cTetroid.drop())
				{
					updateTimer();

					m_viewPlay.invalidate();
				}

				return true;
			}

			// сдвинуть фигуру влево
			case KeyEvent.KEYCODE_DPAD_LEFT:
			case KeyEvent.KEYCODE_A:
			{
				if (m_cTetroid.moveLeft())
				{
					m_viewPlay.invalidate();
				}

				return true;
			}

			// сдвинуть фигуру вправо
			case KeyEvent.KEYCODE_DPAD_RIGHT:
			case KeyEvent.KEYCODE_L:
			{
				if (m_cTetroid.moveRight())
				{
					m_viewPlay.invalidate();
				}

				return true;
			}

			// поворот фигуры влево
			case KeyEvent.KEYCODE_DPAD_UP:
			case KeyEvent.KEYCODE_Q:
			{
				if (m_cTetroid.rotateLeft())
				{
					m_viewPlay.invalidate();
				}

				return true;
			}

			// поворот фигуры вправо
			case KeyEvent.KEYCODE_P:
			{
				if (m_cTetroid.rotateRight())
				{
					m_viewPlay.invalidate();
				}

				return true;
			}
			
			// опустить фигуру вниз на одно деление
			case KeyEvent.KEYCODE_DPAD_DOWN:
			{
				if (m_cTetroid.moveDown())
				{
					m_viewPlay.invalidate();
				}
				
				return true;
			}

			// Пауза
//			case KeyEvent.KEYCODE_P:
//			{
//				onPause();
//
//				return true;
//			}

			default:
				break;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * Обновление игры по таймеру.
	 */
	public void updateTimer()
	{
		// следующий ход
		if (!m_cTetroid.step())
		{
			// если игра закончена выводим слой окончания игры
			TextView textTmp;
			
			textTmp = (TextView)findViewById(R.id.play_gameover_score);
			textTmp.setText(getString(R.string.play_gameover_score) + ": " +
				PlayView.insertInt(m_sbNumber, m_cTetroid.m_iScore));
			
			textTmp = (TextView)findViewById(R.id.play_gameover_lines);
			textTmp.setText(getString(R.string.play_gameover_lines) + ": " +
				PlayView.insertInt(m_sbNumber, m_cTetroid.m_iLines));
			
			m_viewStop.setVisibility(View.VISIBLE);

			findViewById(R.id.play_gameover_btn_next).requestFocus();
			
			// включение вибры на 1 секунду
			Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

			vibrator.vibrate(1000);

			return;
		}

		// если позиция нулевая => значит пред фигура сброшена
		if ((null != m_mpDrop) && (0 == m_cTetroid.m_iFigurePosY))
		{
			m_mpDrop.seekTo(0);
			m_mpDrop.start();
		}
		
		// проверка увеличения уровня
		if (m_iLevel != m_cTetroid.m_iLevel)
		{
			android.util.Log.d("PLAYACTIVITY.JAVA", "m_iLevel:" + Integer.toString(m_iLevel) + ", " +
				"TetroidLevel:" + Integer.toString(m_cTetroid.m_iLevel));
			m_iLevel = m_cTetroid.m_iLevel;

			m_bNotVisiblePause = true;
			
			Intent m_intentBonus = new Intent(PlayActivity.this, BonusActivity.class);

			m_intentBonus.putExtra(getString(R.string.levelup_parameter_score), m_cTetroid.m_iScore);
	 		m_intentBonus.putExtra(getString(R.string.levelup_parameter_lines_1x),
				m_cTetroid.m_statLines[0] - m_statLinesLast[0]);
	 		m_intentBonus.putExtra(getString(R.string.levelup_parameter_lines_2x),
				m_cTetroid.m_statLines[1] - m_statLinesLast[1]);
	 		m_intentBonus.putExtra(getString(R.string.levelup_parameter_lines_3x),
				m_cTetroid.m_statLines[2] - m_statLinesLast[2]);
	 		m_intentBonus.putExtra(getString(R.string.levelup_parameter_lines_4x),
				m_cTetroid.m_statLines[3] - m_statLinesLast[3]);
	 		
	 		m_statLinesLast[0] = m_cTetroid.m_statLines[0];
	 		m_statLinesLast[1] = m_cTetroid.m_statLines[1];
	 		m_statLinesLast[2] = m_cTetroid.m_statLines[2];
	 		m_statLinesLast[3] = m_cTetroid.m_statLines[3];

	 		startActivityForResult(m_intentBonus, ACTIVITY_RESULT_BONUS);
		}
		else
		{
			m_viewPlay.invalidate();

			startTimer();
		}
	}
	
	/**
	 * Обработка завершения дочерних активностей.
	 * - после вывода бонуса запускаем игру
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (ACTIVITY_RESULT_BONUS == requestCode)
		{
			m_bNotVisiblePause = false;

			m_viewPause.setVisibility(View.INVISIBLE);

			System.gc();
			
			startTimer();
		}
	}

	/**
	 * Запуск итерации по таймеру.
	 */
	public void startTimer()
	{
		if (m_iLevel < Tetroid.LEVELCOUNT)
		{
			m_RedrawHandler.sleep(Tetroid.LEVELTIMING[m_cTetroid.m_iLevel]);
		}
		else
		{
			m_RedrawHandler.sleep(Tetroid.LEVELTIMING[Tetroid.LEVELCOUNT - 1]);
		}
	}
	
	/**
	 * Остановка таймера.
	 */
	public void stopTimer()
	{
		m_RedrawHandler.stop();
	}
	
	/**
	 * Обработчик нажатия клавиши влево.
	 */
	OnClickListener m_btnLeftListener = new OnClickListener() 
	{
		public void onClick(View v)
		{
			if (View.VISIBLE == m_viewPause.getVisibility())
			{
				return;
			}
			
			if (m_cTetroid.moveLeft())
			{
				m_viewPlay.invalidate();
			}
		}
	};

	/**
	 * Обработчик нажатия клавиши вправо.
	 */
	OnClickListener m_btnRightListener = new OnClickListener() 
	{
		public void onClick(View v)
		{
			if (View.VISIBLE == m_viewPause.getVisibility())
			{
				return;
			}

			if (m_cTetroid.moveRight())
			{
				m_viewPlay.invalidate();
			}
		}
	};

	/**
	 * Обработчик нажатия клавиши поворота влево.
	 */
	OnClickListener m_btnRotateLeftListener = new OnClickListener() 
	{
		public void onClick(View v)
		{
			if (View.VISIBLE == m_viewPause.getVisibility())
			{
				return;
			}

			if (m_cTetroid.rotateLeft())
			{
				m_viewPlay.invalidate();
			}
		}
	};

	/**
	 * Обработчик нажатия клавиши поворота вправо.
	 */
	OnClickListener m_btnRotateRightListener = new OnClickListener() 
	{
		public void onClick(View v)
		{
			if (View.VISIBLE == m_viewPause.getVisibility())
			{
				return;
			}

			if (m_cTetroid.rotateRight())
			{
				m_viewPlay.invalidate();
			}
		}
	};

	/**
	 * Обработчик нажатия клавиши поворота вправо.
	 */
	OnClickListener m_btnDownListener = new OnClickListener() 
	{
		public void onClick(View v)
		{
			if (View.VISIBLE == m_viewPause.getVisibility())
			{
				return;
			}

			if (m_cTetroid.drop())
			{
				m_viewPlay.invalidate();
			}
		}
	};
	
	/**
	 * Обработка изменения фокуса.
	 * - получение размеров рабочего окна
	 * - расчет координат расположения кнопок
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) 
	{
		super.onWindowFocusChanged(hasFocus);

		// расчет координат расположения кнопок
		if ((0 == m_iWidth) || (0 == m_iHeight))
		{
			m_iWidth  = m_viewPlay.getWidth();
			m_iHeight = m_viewPlay.getHeight();
			
			setButtonPosition();
		}

		// ставим фокус на кнопку Resume
		if (View.VISIBLE == m_viewPause.getVisibility())
		{
			findViewById(R.id.playview_layoutpause_button).requestFocus();
		}
	}

	/** 
	 * Установка размера и позиции кнопок.
	 */
	public void setButtonPosition()
	{
		View viewTmp;
		int m_iButtonSize = m_viewPlay.m_iFieldWidth / 2;
		
		// кнопка влево
		LinearLayout.LayoutParams lpLeft = new LinearLayout.LayoutParams(m_iButtonSize, m_iButtonSize);

		lpLeft.topMargin  = m_iHeight - m_iButtonSize;
		lpLeft.leftMargin = 0; 

		viewTmp = findViewById(R.id.play_btn_left);

		viewTmp.setLayoutParams(lpLeft);
		
		// кнопка поворот влево
		LinearLayout.LayoutParams lpRotateLeft = new LinearLayout.LayoutParams(
			m_iButtonSize, m_iButtonSize);

		lpRotateLeft.topMargin  = m_iHeight - m_iButtonSize * 2;
		lpRotateLeft.leftMargin = 0;

		viewTmp = findViewById(R.id.play_btn_rotate_left);

		viewTmp.setLayoutParams(lpRotateLeft);

		// кнопка вправо
		LinearLayout.LayoutParams lpRight = new LinearLayout.LayoutParams(
			m_iButtonSize, m_iButtonSize);

		lpRight.topMargin  = m_iHeight - m_iButtonSize;
		lpRight.leftMargin = m_iWidth - m_iButtonSize;

		viewTmp = findViewById(R.id.play_btn_right);

		viewTmp.setLayoutParams(lpRight);
		
		// кнопка поворот вправо
		LinearLayout.LayoutParams lpRotateRight = new LinearLayout.LayoutParams(
			m_iButtonSize, m_iButtonSize);

		lpRotateRight.topMargin  = m_iHeight - m_iButtonSize * 2;
		lpRotateRight.leftMargin = m_iWidth - m_iButtonSize;

		viewTmp = findViewById(R.id.play_btn_rotate_right);

		viewTmp.setLayoutParams(lpRotateRight);
		
		// кнопка сброса вниз
		viewTmp = findViewById(R.id.play_btn_down);
		
		if (m_iWidth < m_iHeight)
		{
			int iDownSize = m_iHeight - m_viewPlay.m_ptFieldPosition.y - m_viewPlay.m_iFieldHeight;

			if (iDownSize > m_iButtonSize)
			{
				iDownSize = m_iButtonSize;
			}
			
			LinearLayout.LayoutParams lpDown = new LinearLayout.LayoutParams(iDownSize * 2, iDownSize);

			lpDown.topMargin  = m_viewPlay.m_ptFieldPosition.y + m_viewPlay.m_iFieldHeight;
			lpDown.leftMargin = m_viewPlay.m_ptFieldPosition.x + m_viewPlay.m_iFieldWidth / 2 - iDownSize;
			
			viewTmp.setLayoutParams(lpDown);
		}
		else
		{
			viewTmp.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 *  Обработка нажатия на экран.
	 *  - сброс фигуры
	 */
	@Override 
	public boolean onTouchEvent(MotionEvent event)
	{
		if (View.VISIBLE == m_viewPause.getVisibility())
		{
			return super.onTouchEvent(event);
		}
		
		if (MotionEvent.ACTION_DOWN == event.getAction())
		{
         float fX = event.getX();
         float fY = event.getY();
         
        	if ((fX >= m_viewPlay.m_ptFieldPosition.x) &&
        		(fY >= m_viewPlay.m_ptFieldPosition.y) &&
        		(fX <= (m_viewPlay.m_ptFieldPosition.x + m_viewPlay.m_iFieldWidth)) &&
        		(fY <= (m_viewPlay.m_ptFieldPosition.y + m_viewPlay.m_iFieldHeight)))
        	{
   			if (m_cTetroid.drop())
   			{
   				updateTimer();

   				m_viewPlay.invalidate();
   			}
        	}
		}
		
		return super.onTouchEvent(event);
	}
	
	/**
	 * Уничтожение активности.
	 * - удаление MediaPlayer'ов
	 */
	@Override
	public void onDestroy()
	{
		if (null != m_mpDrop)
		{
			m_mpDrop.release();
			m_mpDrop = null;
		}
		
		super.onDestroy();
	}
}
