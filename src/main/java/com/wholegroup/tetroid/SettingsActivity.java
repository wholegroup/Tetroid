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

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Window;
import android.widget.TextView;

public class SettingsActivity extends PreferenceActivity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
   	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
   	
      // вызов родительской функции
		super.onCreate(savedInstanceState);
		
   	// установка собственного заголовка окна
   	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);

      ((TextView)findViewById(R.id.custom_title_left_text)).setText(R.string.settings_activity_title);
      ((TextView)findViewById(R.id.custom_title_right_text)).setText(R.string.application_upper);

      // установка ключа для хранения настроек
		getPreferenceManager().setSharedPreferencesName(getString(R.string.preferences_id));

		// установка слоя с настройками 
		addPreferencesFromResource(R.xml.settings);
	}
}
