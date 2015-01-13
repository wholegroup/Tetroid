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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class PlayView extends View
{
	/** Ссылка на родительский класс PlayActivity */
	public Context m_context; 
	
	/** Объекты игры Tetroid */
	public Tetroid m_cTetroid;
	
	/** Битмапы фигур */
	private Bitmap[] m_bmpCubes;

	/** Размер стороны квадрата в пикселях */
	private int m_iSideCubeSize;
	
	/** Ширина поля в пикселах */
	public int m_iFieldWidth;
	
	/** Высота поля в пикселах */
	public int m_iFieldHeight;
	
	/** Координаты поля */
	public Point m_ptFieldPosition;
	
	/** Отображать сетку на поле */
	boolean m_bDrawGrid;
	
	/** Отображать подсказку */
	boolean m_bFigureHelp;
	
	/** Отображать очередь фигур */
	boolean m_bFigureNext;
	
	/** Отображать цветное поле */
	boolean m_bColorGrid;
	
	/** Формировать классические фигуры */
	boolean m_bFigureClassic;
	
	/** Кисть для вывода заголовков статистики */ 
	public TextPaint m_tpStatisticHeader;

	/** Кисть для вывода чисел статистики */ 
	public TextPaint m_tpStatisticNumbers;

	/** Текст "SCORE" */
	public String m_strScore;
	
	/** Координаты для вывода заголовка очков */
	public Point m_ptScoreHeader;
	
	/** Координаты для вывода очков */
	public Point m_ptScore;
	
	/** Текст "LINES" */
	public String m_strLines;

	/** Координаты для вывода заголовка линий */
	public Point m_ptLinesHeader;

	/** Координаты для вывода линий */
	public Point m_ptLines;

	/** Текст "LEVEL" */
	public String m_strLevel;
	
	/** Координаты для вывода заголовка линий */
	public Point m_ptLevelHeader;

	/** Координаты для вывода линий */
	public Point m_ptLevel;

	/** Кисть для вывода сетки поля */
	public Paint m_ptGrid;

	/** Кисть для вывода подсказки */
	Paint m_paintFigureHelp;

	/** Буфер для конвертирования чисел */
	public StringBuffer m_sbNumber;
	
	/** Ссылка на ресурсы */
	private Resources m_res;
	
	/** Конструктор */
	@SuppressWarnings("UnusedDeclaration")
	public PlayView(Context context)
	{
		super(context);

		initView();
	}

	/** Конструктор */
	@SuppressWarnings("UnusedDeclaration")
	public PlayView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		initView();
	}

	/** Конструктор */
	@SuppressWarnings("UnusedDeclaration")
	public PlayView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		initView();
	}
	
	/** 
	 * Инициализация.
	 */
	public void initView()
	{
		// Ссылка на родительский класс PlayActivity
		m_context = getContext();
		
		// ссылка на ресурсы
		m_res = m_context.getResources();
		
		// текст статистики игры
		m_strScore = m_context.getString(R.string.play_text_score);
		m_strLines = m_context.getString(R.string.play_text_lines);
		m_strLevel = m_context.getString(R.string.play_text_level);

		// буффер для вывода числовой статистики
		m_sbNumber = new StringBuffer();
		
		// битмапы кубиков
		m_bmpCubes = new Bitmap[Tetroid.FIGURECOUNT + 1];
		
		// позиция поля
		m_ptFieldPosition = new Point(0, 0);

		// кисть для отрисовки сетки
		m_ptGrid = new Paint();

		m_ptGrid.setColor(m_res.getColor(R.color.play_color_grid));
	
		// кисть для отрисовки подсказки
		m_paintFigureHelp = new Paint();
	}

	/**
	 * Отрисовка игрового поля.
	 */
	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		if (null == m_cTetroid)
		{
			return;
		}
		
		// очистка
		canvas.drawColor(m_res.getColor(R.color.play_color_view));
		
		// отрисовка сетки игрового поля
		if (m_bDrawGrid)
		{
			for (int x = 0; x <= Tetroid.PLAYGRIDX; x++)
			{
				canvas.drawLine(m_ptFieldPosition.x + x * m_iSideCubeSize, m_ptFieldPosition.y,
					m_ptFieldPosition.x + x * m_iSideCubeSize,
					m_ptFieldPosition.y + Tetroid.PLAYGRIDY * m_iSideCubeSize, m_ptGrid);
			}
			
			for (int y = 0; y <= Tetroid.PLAYGRIDY; y++)
			{
				canvas.drawLine(m_ptFieldPosition.x, m_ptFieldPosition.y + y * m_iSideCubeSize,
					m_ptFieldPosition.x + Tetroid.PLAYGRIDX * m_iSideCubeSize,
					m_ptFieldPosition.y + y *m_iSideCubeSize, m_ptGrid);
			}
		}
		else
		{
			canvas.drawLine(m_ptFieldPosition.x, m_ptFieldPosition.y, m_ptFieldPosition.x,
				m_ptFieldPosition.y + Tetroid.PLAYGRIDY * m_iSideCubeSize, m_ptGrid);
			canvas.drawLine(m_ptFieldPosition.x + Tetroid.PLAYGRIDX * m_iSideCubeSize,
				m_ptFieldPosition.y, m_ptFieldPosition.x + Tetroid.PLAYGRIDX * m_iSideCubeSize,
				m_ptFieldPosition.y + Tetroid.PLAYGRIDY * m_iSideCubeSize, m_ptGrid);

			canvas.drawLine(m_ptFieldPosition.x, m_ptFieldPosition.y,
				m_ptFieldPosition.x + Tetroid.PLAYGRIDX * m_iSideCubeSize,
				m_ptFieldPosition.y, m_ptGrid);
			canvas.drawLine(m_ptFieldPosition.x,
				m_ptFieldPosition.y + Tetroid.PLAYGRIDY * m_iSideCubeSize,
				m_ptFieldPosition.x + Tetroid.PLAYGRIDX * m_iSideCubeSize,
				m_ptFieldPosition.y + Tetroid.PLAYGRIDY *m_iSideCubeSize, m_ptGrid);
		}
		
		// отрисовка игрового поля
		for (int y = 0; y < Tetroid.PLAYGRIDY; y++)
		{
			for (int x = 0; x < Tetroid.PLAYGRIDX; x++)
			{
				if (0 == m_cTetroid.m_gridPlay[y][x])
				{
					continue;
				}

				canvas.drawBitmap(
					m_bColorGrid ? m_bmpCubes[m_cTetroid.m_gridPlay[y][x]] : m_bmpCubes[0],
					m_ptFieldPosition.x + x * m_iSideCubeSize,
					m_ptFieldPosition.y + y * m_iSideCubeSize,
					null);
			}
		}
		
		// отрисовка подсказки
    	if (m_bFigureHelp && (0 < m_cTetroid.m_iFigureHelpPosY) &&
			(2 <= (m_cTetroid.m_iFigureHelpPosY - m_cTetroid.m_iFigurePosY)))
    	{
    		m_paintFigureHelp.setAlpha(0x50);
    		
    		for (int y = 0; y < Tetroid.FIGUREGRID; y++)
            {
                for (int x = 0; x < Tetroid.FIGUREGRID; x++)
                {
                	// пустые клетки пропускаем
                	if (0 == m_cTetroid.m_gridFigure[y][x])
                	{
                		continue;
                	}

						 canvas.drawBitmap(
							 m_bmpCubes[0],
							 m_ptFieldPosition.x + (x + m_cTetroid.m_iFigureHelpPosX) * m_iSideCubeSize,
							 m_ptFieldPosition.y + (y + m_cTetroid.m_iFigureHelpPosY) * m_iSideCubeSize,
							 m_paintFigureHelp);
					 }
            }
    	}
		
		// отрисовка игровой фигуры
    	for (int y = 0; y < Tetroid.FIGUREGRID; y++)
    	{
    		for (int x = 0; x < Tetroid.FIGUREGRID; x++)
    		{
    			// пустые клетки пропускаем
    			if (0 == m_cTetroid.m_gridFigure[y][x])
    			{
    				continue;
    			}
          	
    			canvas.drawBitmap(
    				m_bmpCubes[m_cTetroid.m_gridFigure[y][x]], 
    				m_ptFieldPosition.x + (x + m_cTetroid.m_iFigurePosX) * m_iSideCubeSize, 
    				m_ptFieldPosition.y + (y + m_cTetroid.m_iFigurePosY) * m_iSideCubeSize, 
    				null);
    		}
    	}

		// отрисовка очереди фигур
    	if (m_bFigureNext)
    	{
    		for (int i = 0; i < Tetroid.FIGUREQUEUE; i++)
    		{
    	 		m_paintFigureHelp.setAlpha(0xFF - 0x40 * i);

       		for (int y = 0; y < Tetroid.FIGUREGRID; y++)
    	        {
    	            for (int x = 0; x < Tetroid.FIGUREGRID; x++)
    	            {
    	            	// пустые клетки пропускаем
    	            	if (0 == Tetroid.FIGURE_ARR[m_cTetroid.m_queueFigure[i] - 1][y][x])
    	            	{
    	            		continue;
    	            	}
    	            	
    	            	canvas.drawBitmap(
    							m_bmpCubes[Tetroid.FIGURE_ARR[m_cTetroid.m_queueFigure[i] - 1][y][x]], 
    							m_ptFieldPosition.x + x * m_iSideCubeSize + (Tetroid.PLAYGRIDX + 1) * m_iSideCubeSize, 
    							m_ptFieldPosition.y + y * m_iSideCubeSize + i * (Tetroid.FIGUREGRID - 1) * m_iSideCubeSize, 
    							m_paintFigureHelp);
    	            }
    	        }
    		}
    	}

    	// вывод количества очков
    	canvas.drawText(m_strScore, m_ptScoreHeader.x, m_ptScoreHeader.y, m_tpStatisticHeader);
    	canvas.drawText(insertInt(m_sbNumber, m_cTetroid.m_iScore), 0, m_sbNumber.length(),
			m_ptScore.x, m_ptScore.y, m_tpStatisticNumbers);
    	
    	// вывод количества линий
    	canvas.drawText(m_strLines, m_ptLinesHeader.x, m_ptLinesHeader.y, m_tpStatisticHeader);
    	canvas.drawText(insertInt(m_sbNumber, m_cTetroid.m_iLines), 0, m_sbNumber.length(),
			m_ptLines.x, m_ptLines.y, m_tpStatisticNumbers);
    	
    	// вывод уровня
    	canvas.drawText(m_strLevel, m_ptLevelHeader.x, m_ptLevelHeader.y, m_tpStatisticHeader);
    	canvas.drawText(insertInt(m_sbNumber, m_cTetroid.m_iLevel), 0, m_sbNumber.length(),
			m_ptLevel.x, m_ptLevel.y, m_tpStatisticNumbers);
	}
	
	/** 
	 * Обработка изменения размеров.
	 */
	@Override
	protected void onSizeChanged(int w, int h, int ow, int oh)
	{
		super.onSizeChanged(w, h, ow, oh);
      
		// размер кубика
		if (w > h)
		{
			m_iSideCubeSize = h / Tetroid.PLAYGRIDY;
		}
		else
		{
			m_iSideCubeSize = w / (2 * Tetroid.PLAYGRIDX);
		}

		// размер поля
		m_iFieldWidth  = m_iSideCubeSize * Tetroid.PLAYGRIDX;
		m_iFieldHeight = m_iSideCubeSize * Tetroid.PLAYGRIDY;
		
		// положение поля
		m_ptFieldPosition.x = (w - m_iFieldWidth) / 2;
		m_ptFieldPosition.y = (h - m_iFieldHeight) / 2;
		
		// инициализация кисти вывода надписей статистики
		m_tpStatisticHeader = new TextPaint();

		m_tpStatisticHeader.setColor(m_res.getColor(R.color.play_color_statistic_name));
		m_tpStatisticHeader.setAntiAlias(true);
		m_tpStatisticHeader.setTextSize(m_iSideCubeSize);
		
		Rect rcTmp     = new Rect();
		Rect rcTextMax = new Rect();
		
		m_tpStatisticHeader.getTextBounds(m_strScore, 0, m_strScore.length(), rcTextMax);
		m_tpStatisticHeader.getTextBounds(m_strLines, 0, m_strLines.length(), rcTmp);

		if (rcTmp.width() > rcTextMax.width())
		{
			rcTextMax.set(rcTmp);
		}

		m_tpStatisticHeader.getTextBounds(m_strLevel, 0, m_strLevel.length(), rcTmp);

		if (rcTmp.width() > rcTextMax.width())
		{
			rcTextMax.set(rcTmp);
		}
		
		float fCoefTmp = (float)rcTextMax.width() * 2 / m_iFieldWidth;

		m_tpStatisticHeader.setTextSize(m_iSideCubeSize / fCoefTmp * 0.9f); 
		
		// инициализация кисти вывода значений статистики
		m_tpStatisticNumbers = new TextPaint();

		m_tpStatisticNumbers.setColor(m_res.getColor(R.color.play_color_statistic_number));
		m_tpStatisticNumbers.setAntiAlias(true);
		m_tpStatisticNumbers.setTextSize(m_iSideCubeSize);
		
		m_tpStatisticNumbers.getTextBounds("9999999", 0, 7, rcTextMax);
		
		fCoefTmp = (float)rcTextMax.width() * 2 / m_iFieldWidth;

		m_tpStatisticNumbers.setTextSize(m_iSideCubeSize / fCoefTmp * 0.9f); 
		m_tpStatisticNumbers.getTextBounds("9999999", 0, 7, rcTextMax);
		
		// инициализация координат для вывода статистики
		int iTextLeft   = m_ptFieldPosition.x - m_iFieldWidth / 2; 
		int iTextHeight = rcTextMax.height();
		int iNumbersPad = iTextHeight / 4;   
		
		m_tpStatisticHeader.getTextBounds(m_strLevel, 0, m_strLevel.length(), rcTmp);

		if (rcTmp.height() > rcTextMax.height())
		{
			iTextHeight = rcTmp.height();
		}
		
		m_ptScoreHeader = new Point(iTextLeft, m_ptFieldPosition.y + iTextHeight);
		m_ptScore = new Point(iTextLeft, m_ptFieldPosition.y + iTextHeight * 2 + iNumbersPad);

		m_ptLinesHeader = new Point(iTextLeft, m_ptFieldPosition.y + iTextHeight * 4);
		m_ptLines = new Point(iTextLeft, m_ptFieldPosition.y + iTextHeight * 5 + iNumbersPad);

		m_ptLevelHeader = new Point(iTextLeft, m_ptFieldPosition.y + iTextHeight * 7);
		m_ptLevel = new Point(iTextLeft, m_ptFieldPosition.y + iTextHeight * 8 + iNumbersPad);
		
		// формирование изображений
		loadBitmaps();
	}
	
	/**
	 * Загрузка изображений.
	 *
	 * - загружаем кубики из PNG
	 * - рисуем классические кубики
	 */
	public void loadBitmaps()
	{
		Drawable cube;
		Bitmap bitmap;
		Canvas canvas;
		
		if (!m_bFigureClassic)
		{
			final int[] arrIDs = {
				R.drawable.cube_0,
				R.drawable.cube_1,
				R.drawable.cube_2,
				R.drawable.cube_3,
				R.drawable.cube_4,
				R.drawable.cube_5,
				R.drawable.cube_6,
				R.drawable.cube_7
			};

			for (int i = 0; i < arrIDs.length; i++)
			{
				bitmap = Bitmap.createBitmap(m_iSideCubeSize, m_iSideCubeSize, Bitmap.Config.ARGB_8888);
				canvas = new Canvas(bitmap);
				
				cube = m_res.getDrawable(arrIDs[i]);

				cube.setBounds(0, 0, m_iSideCubeSize, m_iSideCubeSize);
				cube.draw(canvas);
			
				m_bmpCubes[i] = bitmap;
			}
		}
		else
		{
			final int[][] arrColors = {
				{Color.rgb(0xB2, 0xB2, 0xB2), Color.rgb(0xE2, 0xE2, 0xE2), Color.rgb(0x59, 0x59, 0x59), Color.rgb(0xA0, 0xA0, 0xA0), Color.rgb(0xC5, 0xC5, 0xC5), Color.rgb(0x90, 0x90, 0x90)},
				{Color.rgb(0x00, 0xF0, 0xF0), Color.rgb(0x99, 0xFF, 0xFF), Color.rgb(0x00, 0x78, 0x78), Color.rgb(0x00, 0xD8, 0xD8), Color.rgb(0x65, 0xEB, 0xEB), Color.rgb(0x3F, 0xB0, 0xB0)},
				{Color.rgb(0xF0, 0xA0, 0x00), Color.rgb(0xFF, 0xDD, 0x99), Color.rgb(0x78, 0x50, 0x00), Color.rgb(0xD8, 0x90, 0x00), Color.rgb(0xEB, 0xBF, 0x65), Color.rgb(0xB0, 0x8A, 0x3F)},
				{Color.rgb(0x00, 0x00, 0xF0), Color.rgb(0x99, 0x99, 0xFF), Color.rgb(0x00, 0x00, 0x78), Color.rgb(0x00, 0x00, 0xD8), Color.rgb(0x65, 0x65, 0xEB), Color.rgb(0x00, 0x00, 0xCA)},
				{Color.rgb(0xF0, 0x00, 0x00), Color.rgb(0xFF, 0x99, 0x99), Color.rgb(0x78, 0x00, 0x00), Color.rgb(0xD8, 0x00, 0x00), Color.rgb(0xEB, 0x65, 0x65), Color.rgb(0xB0, 0x3F, 0x3F)},
				{Color.rgb(0x00, 0xF0, 0x00), Color.rgb(0x99, 0xFF, 0x99), Color.rgb(0x00, 0x78, 0x00), Color.rgb(0x00, 0xD8, 0x00), Color.rgb(0x65, 0xEB, 0x65), Color.rgb(0x3F, 0xB0, 0x3F)},
				{Color.rgb(0xA0, 0x00, 0xF0), Color.rgb(0xDD, 0x99, 0xFF), Color.rgb(0x50, 0x00, 0x78), Color.rgb(0x90, 0x00, 0xD8), Color.rgb(0xBF, 0x65, 0xEB), Color.rgb(0x8A, 0x3F, 0xB0)},
				{Color.rgb(0xF0, 0xF0, 0x00), Color.rgb(0xFF, 0xFF, 0x99), Color.rgb(0x78, 0x78, 0x00), Color.rgb(0xD8, 0xD8, 0x00), Color.rgb(0xEB, 0xEB, 0x65), Color.rgb(0xB0, 0xB0, 0x3F)}};

			// вычисляем размер границы квадрата
			int iBoundary = (int)(m_iSideCubeSize / 2.0 * 0.25 + 0.5);
		
			// кисть для рисования
			Paint paintLine = new Paint();

			// отрисовываем кубики
			for (int i = 0; i < arrColors.length; i++)
			{
				bitmap = Bitmap.createBitmap(m_iSideCubeSize, m_iSideCubeSize, Bitmap.Config.ARGB_8888);
				canvas = new Canvas(bitmap);

				// закрашиваем
				canvas.drawColor(arrColors[i][0]);
			
				// рисуем тени
				for (int j = 0; j < iBoundary; j++)
				{
					// рисуем верх
					paintLine.setColor(arrColors[i][1]);
					canvas.drawLine(j + 1, j, m_iSideCubeSize - j - 1, j, paintLine);
				
					// рисуем низ
					paintLine.setColor(arrColors[i][2]);
					canvas.drawLine(j + 1, m_iSideCubeSize - j - 1, m_iSideCubeSize - j - 1,
						m_iSideCubeSize - j - 1, paintLine);

					// рисуем левую сторону
					paintLine.setColor(arrColors[i][3]);
					canvas.drawLine(j, j + 1, j, m_iSideCubeSize - j - 1, paintLine);
				
					// рисуем правую сторону
					canvas.drawLine(m_iSideCubeSize - j - 1, j + 1, m_iSideCubeSize - j - 1,
						m_iSideCubeSize - j - 1, paintLine);
				
					// рисуем диагонали
					paintLine.setColor(arrColors[i][4]);
					canvas.drawPoint(j, j, paintLine);
					canvas.drawPoint(m_iSideCubeSize - j - 1, j, paintLine);

					paintLine.setColor(arrColors[i][5]);
					canvas.drawPoint(j, m_iSideCubeSize - j - 1, paintLine);
					canvas.drawPoint(m_iSideCubeSize - j - 1, m_iSideCubeSize - j - 1, paintLine);
				}
			
				m_bmpCubes[i] = bitmap;
			}
		}
	}

	/** Набор цифр для использования в функции insertInt */
	final static char[] m_cNumber = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

	/**
	 * Функция вставляет положительный int в строкой буфер.
	 * 
	 * @param sbIn строковой буфер
	 * @param iAdd число
	 *
	 * @return строковой буфер 
	 */
	static StringBuffer insertInt(StringBuffer sbIn, int iAdd)
	{
		// очищаем строку
		sbIn.delete(0, sbIn.length());

		// нулевое число сразу возвращаем
		if (0 == iAdd)
		{
			return sbIn.append('0');
		}
		
		// вставляем цифры по очереди
		int iNumber;
		int iCount = 0;

		while (iAdd > 0)
		{
			// разделитель после 3 цифр
			if (0 == (iCount % 3))
			{
				sbIn.insert(0, ' ');
			}
			
			iCount++;
			
			iNumber = iAdd % 10;
			iAdd -= iNumber;
			iAdd /= 10;
			
			sbIn.insert(0, m_cNumber[iNumber]);
		}
		
		return sbIn;
	}
}
