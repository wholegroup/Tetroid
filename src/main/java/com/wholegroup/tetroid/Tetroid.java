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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.json.*;

public class Tetroid
{
	/** Генератор случайных чисел */
	private static final Random RND = new Random(); 

	/** Количество фигур */
	public static final int FIGURECOUNT = 7;

	/** Размер очереди очередных фигур */
	public static final int FIGUREQUEUE = 4;
	
	/** ▀▀▀▀ */
	public static final int FIGURE_I = 1;
	
	/** █▀▀ */
	public static final int FIGURE_L = 2; 
	
	/** ▀▀█ */
	public static final int FIGURE_J = 3;
	
	/** ▀█▄ */
	public static final int FIGURE_Z = 4; 

	/** ▄█▀ */
	public static final int FIGURE_S = 5; 

	/** ▀█▀ */
	public static final int FIGURE_T = 6; 

	/** ██ */
	public static final int FIGURE_O = 7; 

	/** */
	public static final int[][][] FIGURE_ARR = {
		{
			{FIGURE_I, FIGURE_I, FIGURE_I, FIGURE_I},
			{0       , 0       , 0       , 0       },
			{0       , 0       , 0       , 0       },
			{0       , 0       , 0       , 0       }},

		{
			{FIGURE_L, FIGURE_L, FIGURE_L, 0       },
			{FIGURE_L, 0       , 0       , 0       },
			{0       , 0       , 0       , 0       },
			{0       , 0       , 0       , 0       }},

		{
			{FIGURE_J, FIGURE_J, FIGURE_J, 0       },
			{0       , 0       , FIGURE_J, 0       },
			{0       , 0       , 0       , 0       },
			{0       , 0       , 0       , 0       }},

		{
			{FIGURE_Z, FIGURE_Z, 0       , 0       },
			{0       , FIGURE_Z, FIGURE_Z, 0       },
			{0       , 0       , 0       , 0       },
			{0       , 0       , 0       , 0       }},

		{
			{0       , FIGURE_S, FIGURE_S, 0       },
			{FIGURE_S, FIGURE_S, 0       , 0       },
			{0       , 0       , 0       , 0       },
			{0       , 0       , 0       , 0       }},

		{
			{FIGURE_T, FIGURE_T, FIGURE_T, 0       },
			{0       , FIGURE_T, 0       , 0       },
			{0       , 0       , 0       , 0       },
			{0       , 0       , 0       , 0       }},

		{
			{FIGURE_O, FIGURE_O, 0       , 0       }, 
			{FIGURE_O, FIGURE_O, 0       , 0       },
			{0       , 0       , 0       , 0       },
			{0       , 0       , 0       , 0       }}};
	

	/** Ширина игрового поля */
	public static final int PLAYGRIDX = 10;
	
	/** Высота игрового поля */
	public static final int PLAYGRIDY = 20;

	/** Игровое поле */
	public int[][] m_gridPlay; 
	
	/** Размер поля для падающей фигуры */
	public static final int FIGUREGRID = 4;

	/** Поле фигуры */
	public int[][] m_gridFigure;
	
	/** Поле фигуры - временное, используется при различных расчетах */
	public int[][] m_gridFigureTemp;
	
	/** Координата X падающей фигуры относительно поля */
	public int m_iFigurePosX;

	/** Координата Y падающей фигуры относительно поля */
	public int m_iFigurePosY;
	
	/** Координата X фигуры-подсказки относительно поля */
	public int m_iFigureHelpPosX;

	/** Координата Y фигуры-подсказки относительно поля */
	public int m_iFigureHelpPosY;
	
	/** Список очередных фигур для планирования */
	public int[] m_queueFigure;
	
	/** Статистика по фигурам */
	public int[] m_statFigure;
	
	/** Общая статистика по линиям */
	public int[] m_statLines;

	/** Количество очков */
	public int m_iScore = 0;
	
	/** Количество линий */
	public int m_iLines = 0;
	
	/** Количество линий для перехода на следующий уровень */
	public int m_iLinesNext = 0;

	/** Текущий уровень */
	public int m_iLevel = 0;
	
	/** Количество уровней */
	public final static int LEVELCOUNT = 10;
	
	/** Тайминги уровней (в мс) */
	public static final int[] LEVELTIMING = {800, 650, 500, 450, 400, 350, 300, 250, 200, 150};

	/** Количество линий для перехода на следующий уровень */
	public static final int[] LEVELLINES = {30, 30, 30, 30, 30, 50, 50, 50, 50, 50};

	/** Бонусные очки */
	public final static int[] BONUSSCORE = {100, 400, 900, 2500};
	
	/** Бонус за прохождение уровня */
	public int m_iBonus = 0;

	/** Массив для функции очистки линий */
	private ArrayList<Integer> m_arrLinesClear;

	/**
	 * Конструктор.
	 */
	public Tetroid()
	{
		// поле
		m_gridPlay = new int[PLAYGRIDY][PLAYGRIDX];
		
		// фигура
		m_gridFigure = new int[FIGUREGRID][FIGUREGRID];
		m_gridFigureTemp = new int[FIGUREGRID][FIGUREGRID];

		// очередь
		m_queueFigure = new int[FIGUREQUEUE];
		
		// статистика фигур
		m_statFigure = new int[FIGURECOUNT];

		// общая статистика линий
		m_statLines = new int[FIGUREGRID];
		
		// массив для очистки линий
		m_arrLinesClear = new ArrayList<Integer>();
	}
	
	/**
	 * Инициализация игрового поля.
	 * 
	 * @return Возвращает TRUE, если инициализация прошла без ошибок
	 */
	public boolean init()
	{
		// инициализация игрового поля
		for (int y = 0; y < PLAYGRIDY; y++)
		{
			for (int x = 0; x < PLAYGRIDX; x++)
			{
				m_gridPlay[y][x] = 0; 
			}
		}
		
		// инициализация очереди фигур
		for (int i = 0; i < FIGUREQUEUE; i++)
		{
			m_queueFigure[i] = getRandomFigure();
		}

		// инициализация статистики фигур
		for (int i = 0; i < FIGURECOUNT; i++)
		{
			m_statFigure[i] = 0;
		}

		// инициализация статистики линий
		for (int i = 0; i < FIGUREGRID; i++)
		{
			m_statLines[i] = 0;
		}
		
		// установка 0 уровня
		m_iLevel = 0;
		
		// установка 0 очков
		m_iScore = 0;
		
		// установка 0 линий
		m_iLines = 0;
		
		// количество линий для перехода на следующий уровень
		m_iLinesNext = LEVELLINES[0];
		
		// установка бонуса за прохождение уровня
		m_iBonus = 0;

		// формирование фигуры
		newFigure();

		// формирование позиции фигуры
		m_iFigurePosX = (PLAYGRIDX - FIGUREGRID) / 2;
		m_iFigurePosY = 0;
		
		// рассчитать координат подсказки
		calcHelpPosition();

		return true;
	}
	
	/**
	 * Генерация случайной фигуры.
	 */
	public int getRandomFigure()
	{
		return RND.nextInt(FIGURECOUNT) + 1;
	}
	
	/**
	 * Проверяет возможность установки фигуры в указанную позицию.
	 *
	 * @param gridFigure массив фигуры (передается, т.к. нужна функциям поворота)
	 * @param iNewPosX   координата X новой позиции
	 * @param iNewPosY   координата Y новой позиции
	 *
	 * @return Возвращает TRUE, если фигуру можно установить в новую позицию, иначе FALSE.
	 */
	public boolean checkMove(int[][] gridFigure, int iNewPosX, int iNewPosY)
	{
		int iCheckX;
		int iCheckY;

		for (int y = 0; y < FIGUREGRID; y++)
		{
			for (int x = 0; x < FIGUREGRID; x++)
			{
				// пустые клетки пропускаем
				if (0 == gridFigure[y][x])
				{
					continue;
				}

				// позиция клетки на игровом поле для проверки
				iCheckX = iNewPosX + x;
				iCheckY = iNewPosY + y;

				// выходим, если клетка выходит за рамки поля
				if ((0 > iCheckX) || (0 > iCheckY) || (PLAYGRIDX <= iCheckX) || (PLAYGRIDY <= iCheckY))
				{
					return false;
				}

				// проверки накладки
				if (0 < m_gridPlay[iCheckY][iCheckX])
				{
					return false;
				}
			}
		}

		return true;
	}
	
	/**
	 * Функция выполняет очередной ход. При необходимости убирает заполненные линии, 
	 * генерирует новую фигуру, считает очки, осуществляет переход на новый уровень.
	 *
	 * @return Возвращает FALSE, если следующий ход не возможен (игра закончена).
	 */
	public boolean step()
	{
		// проверка на случай завершения игры 
		if (!checkMove(m_gridFigure, m_iFigurePosX, m_iFigurePosY))
		{
			return false;
		}

		// сдвинуть фигуру вниз
		if (moveDown())
		{
			return true;
		}
		
		// вставляем фигуру в игровое поле
		int iToX;
		int iToY;
		int iToMaxY = 0;

		for (int y = 0; y < FIGUREGRID; y++)
		{
			for (int x = 0; x < FIGUREGRID; x++)
			{
				if (0 == m_gridFigure[y][x])
				{
					continue;
				}

				iToX = x + m_iFigurePosX;
				iToY = y + m_iFigurePosY;

				if ((0 > iToX) || (0 > iToY) || (PLAYGRIDX <= iToX) || (PLAYGRIDY <= iToY))
				{
					continue;
				}

				if (iToY > iToMaxY)
				{
					iToMaxY = iToY;
				}
				
				m_gridPlay[iToY][iToX] = m_gridFigure[y][x];
			}
		}

		// расчет очков
		m_iScore += 4 * m_iLevel + 2 * (m_iLevel + 1) * (PLAYGRIDY - iToMaxY);
		
		// очистить заполненные строки
		clearLines();
		
		// переход на следующий уровень
		if (m_iLines >= m_iLinesNext)
		{
			android.util.Log.d("TETROID.JAVA",
				"Level:" + Integer.toString(m_iLevel) + ", " +
				"Lines:" + Integer.toString(m_iLines) +  ", " +
				"LinesNext: " + Integer.toString(m_iLinesNext));
			
			m_iLevel++;

			m_iScore += m_iBonus;
			m_iBonus = 0;
			
			// расчет количества линий для перехода на следующий уровень
			if (m_iLevel < LEVELCOUNT)
			{
				m_iLinesNext += LEVELLINES[m_iLevel];
			}
			else
			{
				m_iLinesNext += LEVELLINES[LEVELCOUNT - 1];
			}
		}
		
		// сформировать следующую фигуру
		newFigure();

		m_iFigurePosX = (PLAYGRIDX - FIGUREGRID) / 2;
		m_iFigurePosY = 0;
		
		// если фигуру на поле поставить нельзя, то заканчиваем игру
		if (!checkMove(m_gridFigure, m_iFigurePosX, m_iFigurePosY))
		{
			return false;
		}
		
		// рассчитать координаты подсказки
		calcHelpPosition();
		
		return true;
	}

	/**
	 * Функция сдвигает фигуру в указанные координаты.
	 *
	 * @return TRUE, если сдвиг выполнен успешно, иначе FALSE
	 */
	public boolean move(int iNewPosX, int iNewPosY)
	{
		// проверка возможности сдвига
		if (!checkMove(m_gridFigure, iNewPosX, iNewPosY))
		{
			return false;
		}
		
		// установка новых координат
		m_iFigurePosX = iNewPosX;
		m_iFigurePosY = iNewPosY;

		// пересчет позиции подсказки
		calcHelpPosition();
		
		return true;
	}
	
	/**
	 * Функция сдвигает фигуру влево.
	 *
	 * @return Возвращает TRUE, если фигуру сдвинули, иначе FALSE. 
	 */
	public boolean moveLeft()
	{
		return move(m_iFigurePosX - 1, m_iFigurePosY);
	}
	
	/**
	 * Функция сдвигает фигуру вправо.
	 *
	 * @return Возвращает TRUE, если фигуру сдвинули, иначе FALSE.
	 */
	public boolean moveRight()
	{
		return move(m_iFigurePosX + 1, m_iFigurePosY);
	}

	/**
	 * Сдвигает фигуру вниз.
	 *
	 * @return Возвращает TRUE, если фигуру сдвинули, иначе FALSE.
	 */
	public boolean moveDown()
	{
		return move(m_iFigurePosX, m_iFigurePosY + 1);
	}
	
	/**
	 * Выполняет сброс фигуры.
	 *
	 * @return Возвращает TRUE, если фигура сброшена, иначе FALSE.
	 */
	public boolean drop()
	{
		// сдвигаем вниз насколько это возможно
		for (int y = m_iFigurePosY; y < PLAYGRIDY; y++)
		{
			if (!moveDown())
			{
				break;
			}
		}

		return true;
	}
	
	/**
	 * Выполняет поворот фигуры вправо.
	 *
	 * @return Возвращает TRUE, если фигуру повернули, иначе FALSE.
	 */
	public boolean rotateRight()
	{
		return rotate(false);
	}
	
	/**
	 * Выполняет поворот фигуры влево.
	 *
	 * @return Возвращает TRUE, если фигуру повернули, иначе FALSE.
	 */
	public boolean rotateLeft()
	{
		return rotate(true);
	}

	/**
	 * Выполняет поворот фигуры в указанную сторону.
	 *
	 * @param bLeft Направление поворота: TRUE - влево, FALSE - вправо.

	 * @return TRUE, если поворот выполнен успешно, иначе FALSE.
	 */
	@SuppressWarnings("ConstantConditions")
	public boolean rotate(boolean bLeft)
	{
		// получаем размеры прямоугольника описывающего фигуру
		int iRectX = FIGUREGRID - 1;
		int iRectY = FIGUREGRID - 1;
		
		boolean bBreak = false;

		for (; iRectX > 0; iRectX--)
		{
			for (int y = 0; y < FIGUREGRID; y++)
			{
				if (0 < m_gridFigure[y][iRectX])
				{
					bBreak = true;

					break;
				}
			}

			if (bBreak)
			{
				break;
			}
		}

		bBreak = false;

		for (; iRectY > 0; iRectY--)
		{
			for (int x = 0; x < FIGUREGRID; x++)
			{
				if (0 < m_gridFigure[iRectY][x])
				{
					bBreak = true;

					break;
				}
			}

			if (bBreak)
			{
				break;
			}
		}
		
		// поворот
		for (int x = 0; x < FIGUREGRID; x++)
		{
			for (int y = 0; y < FIGUREGRID; y++)
			{
				m_gridFigureTemp[y][x] = 0;
			}
		}

		for (int x = 0; x <= iRectY; x++)
		{
			for (int y = 0; y <= iRectX; y++)
			{
				if (bLeft)
				{
					m_gridFigureTemp[y][x] = m_gridFigure[x][iRectX - y];
				}
				else
				{
					m_gridFigureTemp[y][x] = m_gridFigure[iRectY - x][y];
				}
			}
		}
		
		// проверка выхода за границы стакана. при необходимости сдвиг.
		int iFigurePosX = m_iFigurePosX;
		int iFigurePosY = m_iFigurePosY;

		if ((m_iFigurePosX + iRectY) >= PLAYGRIDX)
		{
			iFigurePosX = PLAYGRIDX - iRectY - 1;
		}

		if (!checkMove(m_gridFigureTemp, iFigurePosX, iFigurePosY))
		{
			return false;
		}

		// установка новых координат
		m_iFigurePosX = iFigurePosX;
		m_iFigurePosY = iFigurePosY;

		// копирование в массив фигуры
		for (int x = 0; x < FIGUREGRID; x++)
		{
			for (int y = 0; y < FIGUREGRID; y++)
			{
				m_gridFigure[y][x] = m_gridFigureTemp[y][x];
			}
		}

		// вычисление позиции подсказки
		calcHelpPosition();

		return true;
	}
	
	
	/**
	 * Вычисляет координаты подсказки.
	 *
	 * @return TRUE, если координаты успешно вычислены
	 */
	public boolean calcHelpPosition()
	{
		m_iFigureHelpPosX = 0;
		m_iFigureHelpPosY = 0;
		
		for (int y = (m_iFigurePosY + 1); y <= PLAYGRIDY; y++)
		{
			if (checkMove(m_gridFigure, m_iFigurePosX, y))
			{
				continue;
			}

			m_iFigureHelpPosX = m_iFigurePosX;
			m_iFigureHelpPosY = y - 1;

			break;
		}
		
		return true;
	}
	
	/**
	 * Очищает заполненные строки
	 *
	 * @return result
	 */
	public boolean clearLines()
	{
		// поиск линий для очистки
		m_arrLinesClear.clear();

		boolean bClear;

		for (int y = (PLAYGRIDY - 1); y >= 0; y--)
		{
			bClear = true;

			for (int x = 0; x < PLAYGRIDX; x++)
			{
				if (0 == m_gridPlay[y][x])
				{
					bClear = false;

					break;
				}
			}

			if (bClear)
			{
				m_arrLinesClear.add(y);
			}
		}
		
		// убираем заполненные линии
		if (m_arrLinesClear.isEmpty())
		{
			return false;
		}

		int iPostStep = 1;

		for (int y = (m_arrLinesClear.get(0) - 1); y >= 0; y--)
		{
			if (0 <= m_arrLinesClear.indexOf(y))
			{
				iPostStep++;
				continue;
			}

			for (int x = 0; x < PLAYGRIDX; x++)
			{
				m_gridPlay[y + iPostStep][x] = m_gridPlay[y][x];
				m_gridPlay[y][x] = 0;
			}
		}

		// подсчет статистики
		m_iLines += m_arrLinesClear.size();
		m_statLines[m_arrLinesClear.size() - 1]++;
		
		// прибавление бонусных очков
		m_iBonus += BONUSSCORE[m_arrLinesClear.size() - 1];
		
		return true;
	}
	
	/**
	 * Генерирует новую фигуру.
	 *
	 * @return TRUE, если фигура успешно сгенерирована, иначе FALSE 
	 */
	public boolean newFigure()
	{
		// получение кода очередной фигуры
		int iCodeNext = m_queueFigure[0];

		// сдвиг очереди
		System.arraycopy(m_queueFigure, 1, m_queueFigure, 0, FIGUREQUEUE - 1);

		m_queueFigure[FIGUREQUEUE - 1] = getRandomFigure();

		// занесение фигуры в статистику
		m_statFigure[iCodeNext - 1]++;

		// заполнение массива фигуры
		for (int x = 0; x < FIGUREGRID; x++)
		{
			for (int y = 0; y < FIGUREGRID; y++)
			{
				m_gridFigure[y][x] = FIGURE_ARR[iCodeNext - 1][y][x];
			}
		}

		return true;
	}
	
	/**
	 * Возвращает объект в формате JSON.
	 *
	 * @return состояние объекта в формате JSON
	 */
	public String toJSON()
	{
		String sReturn = "";
		
		try
		{
			sReturn = new JSONObject()
				.put("m_gridPlay", new JSONArray(Arrays.deepToString(m_gridPlay)))
				.put("m_gridFigure", new JSONArray(Arrays.deepToString(m_gridFigure)))
				.put("m_queueFigure", new JSONArray(Arrays.toString(m_queueFigure)))
				.put("m_statFigure", new JSONArray(Arrays.toString(m_statFigure)))
				.put("m_statLines", new JSONArray(Arrays.toString(m_statLines)))
				.put("m_iLevel", m_iLevel)
				.put("m_iLines", m_iLines)
				.put("m_iScore", m_iScore)
				.put("m_iFigurePosX", m_iFigurePosX)
				.put("m_iFigurePosY", m_iFigurePosY)
				.put("m_iLinesNext", m_iLinesNext)
				.toString();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		
		return sReturn;
	}
	
	/**
	 * Восстанавливает объект из строки в формате JSON.
	 *
	 * @param strObject string
	 */
	public void fromJSON(String strObject)
	{
   	try
   	{
			JSONObject jsonObject = new JSONObject(strObject);

   		JSONArray  jsonArray;
   		JSONArray  jsonArray2;
   		
   		jsonArray = jsonObject.getJSONArray("m_gridPlay");

      	for (int y = 0; y < jsonArray.length(); y++)
      	{
      		if (PLAYGRIDY <= y )
      		{
      			break;
      		}
      		
      		jsonArray2 = jsonArray.getJSONArray(y);

      		for (int x = 0; x < jsonArray2.length(); x++)
      		{
      			if (PLAYGRIDX <= x)
      			{
      				break;
      			}
      			
      			m_gridPlay[y][x] = jsonArray2.getInt(x);
      		}
      	}

   		jsonArray = jsonObject.getJSONArray("m_gridFigure");

      	for (int y = 0; y < jsonArray.length(); y++)
      	{
      		if (FIGUREGRID <= y )
      		{
      			break;
      		}
      		
      		jsonArray2 = jsonArray.getJSONArray(y);

      		for (int x = 0; x < jsonArray2.length(); x++)
      		{
      			if (FIGUREGRID <= x)
      			{
      				break;
      			}
      			
      			m_gridFigure[y][x] = jsonArray2.getInt(x);
      		}
      	}
      	
   		jsonArray = jsonObject.getJSONArray("m_queueFigure");

      	for (int i = 0; i < jsonArray.length(); i++)
      	{
      		if (i < FIGUREQUEUE)
      		{
      			m_queueFigure[i] = jsonArray.getInt(i);
      		}
      	}
   		
   		jsonArray = jsonObject.getJSONArray("m_statFigure");

      	for (int i = 0; i < jsonArray.length(); i++)
      	{
      		if (i < FIGURECOUNT)
      		{
      			m_statFigure[i] = jsonArray.getInt(i);
      		}
      	}
   		
   		jsonArray = jsonObject.getJSONArray("m_statLines");

      	for (int i = 0; i < jsonArray.length(); i++)
      	{
      		if (i < FIGUREGRID)
      		{
      			m_statLines[i] = jsonArray.getInt(i);
      		}
      	}
      	
   		m_iLevel      = jsonObject.getInt("m_iLevel");
   		m_iLines      = jsonObject.getInt("m_iLines");
   		m_iScore      = jsonObject.getInt("m_iScore");
   		m_iFigurePosX = jsonObject.getInt("m_iFigurePosX");
   		m_iFigurePosY = jsonObject.getInt("m_iFigurePosY");
   		m_iLinesNext  = jsonObject.getInt("m_iLinesNext");
   	}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		
		calcHelpPosition();
	}
}
