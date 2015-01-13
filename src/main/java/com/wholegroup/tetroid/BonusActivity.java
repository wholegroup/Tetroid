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
import android.view.KeyEvent;
import android.view.Window;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BonusActivity extends Activity
{
	/** Период обновления в мс */
	private final static int UPDATEPERIOD = 10;
	
	/** Шаг обновления очков (100 должно быть кратно этому числу) */
	private final static int SCOREADD = 20;
	
	/** Номер бонуса */
	private int m_iBonus = 0;

	/** Текущее количество очков одного вида бонуса */
	private int m_iSum = 0;

	/** Максимальное количество очков за один вид бонуса*/
	private int m_iSumMax = 0;

	/** Количество убранных линий */
	private int[] m_iLines;
	
	/** Ссылки на контролы для вывода очков */
	private TextView[] m_tvLinesSum;
	
	/** Сумма бонусов */
	private int m_iTotal = 0;

	/** Ссылка на контрол вывода суммы бонусов */
	private TextView m_tvTotal;
	
	/** Количество очков */
	private int m_iScore = 0;
	
	/** Ссылка на контрол вывода общего количества очков */
	private TextView m_tvScore;
	
	/** Медиаплеер */
	private MediaPlayer m_mPlayer;

	/** Для вывода статистики на экран */
	public StringBuffer m_sbNumber;
	
	/** Класс для создания анимации */
	private RefreshHandler m_RedrawHandler = new RefreshHandler();
	
	class RefreshHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			BonusActivity.this.onUpdate();
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
	
	/** Создание активности. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// убираем заголовок приложения
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// установка видимого слоя
		setContentView(R.layout.bonus);

		// буффер для вывода числовой статистики
		m_sbNumber = new StringBuffer();
		
		// создание массивов
		m_iLines     = new int[Tetroid.FIGUREGRID];
		m_tvLinesSum = new TextView[Tetroid.FIGUREGRID];

		// получение параметров
 		Intent intent = getIntent();
 		
 		m_iScore    = intent.getIntExtra(getString(R.string.levelup_parameter_score), 0);
 		m_iLines[0] = intent.getIntExtra(getString(R.string.levelup_parameter_lines_1x), 0);
		m_iLines[1] = intent.getIntExtra(getString(R.string.levelup_parameter_lines_2x), 0);
		m_iLines[2] = intent.getIntExtra(getString(R.string.levelup_parameter_lines_3x), 0);
		m_iLines[3] = intent.getIntExtra(getString(R.string.levelup_parameter_lines_4x), 0);

		// инициализация переменных
		m_iSum    = 0;
		m_iBonus  = 0;
		m_iSumMax = m_iLines[0] * Tetroid.BONUSSCORE[m_iBonus];
		m_iTotal  = 0;
		
		// вычитаем сумму бонуса из полученных очков
		for (int i = 0; i < Tetroid.FIGUREGRID; i++)
		{
			m_iScore -= m_iLines[i] * Tetroid.BONUSSCORE[i];
		}

		// инициализируем контролы
		((TextView)findViewById(R.id.levelup_singles)).setText(
			PlayView.insertInt(m_sbNumber, m_iLines[0]));
		((TextView)findViewById(R.id.levelup_doubles)).setText(
			PlayView.insertInt(m_sbNumber, m_iLines[1]));
		((TextView)findViewById(R.id.levelup_triples)).setText(
			PlayView.insertInt(m_sbNumber, m_iLines[2]));
		((TextView)findViewById(R.id.levelup_tetroid)).setText(
			PlayView.insertInt(m_sbNumber, m_iLines[3]));

		m_tvLinesSum[0] = (TextView)findViewById(R.id.levelup_singles_sum);
		m_tvLinesSum[1] = (TextView)findViewById(R.id.levelup_doubles_sum);
		m_tvLinesSum[2] = (TextView)findViewById(R.id.levelup_triples_sum);
		m_tvLinesSum[3] = (TextView)findViewById(R.id.levelup_tetroid_sum);
		
		for (int i = 0; i < Tetroid.FIGUREGRID; i++)
		{
			m_tvLinesSum[i].setText("0");
		}

		m_tvTotal = (TextView)findViewById(R.id.levelup_total);
		m_tvTotal.setText("0");

		m_tvScore = (TextView)findViewById(R.id.levelup_score);
		m_tvScore.setText(PlayView.insertInt(m_sbNumber, m_iScore)); 
		
		// установка обработчика на нажатие кнопки
		findViewById(R.id.levelup).setOnClickListener(m_ClickListener);
		
		// инициализация звука
   	SharedPreferences settings = getSharedPreferences(getString(R.string.preferences_id), 0);

   	if (settings.getBoolean(getString(R.string.preferences_settings_sound),
			Boolean.parseBoolean(getString(R.string.preferences_settings_sound_default))))
   	{
   		m_mPlayer = MediaPlayer.create(this, R.raw.bonus);
   	}
	}
	
	/**
	 *  Запуск активности.
	 *  - старт проигрывания музыки
	 */
	@Override
	public void onResume()
	{
		super.onResume();

		if (null != m_mPlayer)
		{
			m_mPlayer.seekTo(0);
			m_mPlayer.start();
		}
		
		onUpdate();
	}
	
	/**
	 * Остановка активности.
	 * - медиаплеер на паузу
	 */
	@Override
	public void onPause()
	{
		if ((null != m_mPlayer) && m_mPlayer.isPlaying())
		{
			m_mPlayer.pause();
		}
		
		super.onPause();
	}
	
	/**
	 * Уничтожение активности.
	 * - удаление медиаплеера
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

	/**
	 * Обновление данных.
	 */
 	public void onUpdate()
	{
		if (m_iSum >= m_iSumMax)
		{
			m_iBonus++;
			
			if (Tetroid.FIGUREGRID > m_iBonus)
			{
				m_iSum    = 0;
				m_iSumMax = m_iLines[m_iBonus] * Tetroid.BONUSSCORE[m_iBonus];
			}
		}
		else
		{
			m_iSum += SCOREADD;
			m_tvLinesSum[m_iBonus].setText(PlayView.insertInt(m_sbNumber, m_iSum));

			m_iTotal += SCOREADD;
			m_tvTotal.setText(PlayView.insertInt(m_sbNumber, m_iTotal));

			m_tvScore.setText(PlayView.insertInt(m_sbNumber, m_iScore + m_iTotal));
		}
		
		if (Tetroid.FIGUREGRID > m_iBonus)
		{
			m_RedrawHandler.sleep(UPDATEPERIOD);
		}
	}

	/** 
	 * Завершение начисления бонусов и возврат результата.
	 */
	public void onFinish()
	{
		finish();
	}

	/**
	 * Обработка нажатия на экран.
	 * - если нажали во время подсчета, то выполняется мгновенный расчет бонуса 
	 * - завершение активности
	 */
	OnClickListener m_ClickListener = new OnClickListener()
	{	
		public void onClick(View v)
		{
			if (Tetroid.FIGUREGRID > m_iBonus)
			{
				m_RedrawHandler.stop();
				
				// подсчитываем весь бонус
				m_iTotal = 0;
				
				for (int i = 0; i < Tetroid.FIGUREGRID; i++)
				{
					m_iSumMax = m_iLines[i] * Tetroid.BONUSSCORE[i];
					m_tvLinesSum[i].setText(PlayView.insertInt(m_sbNumber, m_iSumMax));

					m_iTotal += m_iSumMax;
				}

				// обновляем значения
				m_tvTotal.setText(PlayView.insertInt(m_sbNumber, m_iTotal));
				m_tvScore.setText(PlayView.insertInt(m_sbNumber, m_iScore + m_iTotal));

				m_iBonus = Tetroid.FIGUREGRID;
			}
			else
			{
				onFinish();
			}
		}
	};
	
	/** 
	 * Обработчик нажатия клавиш.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_ENTER:
			{
				m_ClickListener.onClick(null);

				return true;
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}

	
	/**
	 * Обработка изменения фокуса.
	 * - получение размеров рабочего окна
	 * - изменение положения таблицы результатов
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) 
	{
		super.onWindowFocusChanged(hasFocus);

		View viewLayout = findViewById(R.id.levelup);

		if (viewLayout.getWidth() < viewLayout.getHeight())
		{
			((LinearLayout)findViewById(R.id.levelup_table)).setOrientation(LinearLayout.VERTICAL);
		}
		else
		{
			((LinearLayout)findViewById(R.id.levelup_table)).setOrientation(LinearLayout.HORIZONTAL);
		}
	}
}
