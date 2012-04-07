/**
Copyright (C) 2012  Delcyon, Inc.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.delcyon.capo.util;

import java.util.HashMap;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * @author jeremiah
 *
 */
public class LeveledConsoleHandler extends Handler
{

	public enum Output
	{
		STDOUT,
		STDERR
	}
	
	private HashMap<Level, Output> levelHashMap = new HashMap<Level, Output>();
	private Level errorLevel = Level.WARNING;
	@Override
	public void publish(LogRecord record)
	{
		if (getFormatter() == null)
		{
			setFormatter(new SimpleFormatter());
		}

		try {
			String message = getFormatter().format(record);
			if (levelHashMap.containsKey(record.getLevel()))
			{
				switch (levelHashMap.get(record.getLevel()))
				{
					case STDERR:
						System.err.write(message.getBytes()); 
						break;

					case STDOUT:
						System.out.write(message.getBytes());
						break;
				}
			}
			else if (record.getLevel().intValue() >= errorLevel.intValue())
			{
				System.err.write(message.getBytes()); 
			}
			else
			{
				System.out.write(message.getBytes());
			}
		} catch (Exception exception) {
			reportError(null, exception, ErrorManager.FORMAT_FAILURE);
			return;
		}

	}

	/**
	 * Sets the level at which things are should go to stderr in stead of stdout
	 * default is Level.WARNING
	 * @param errorLevel
	 */
	public void setErrorLevel(Level errorLevel)
	{
		this.errorLevel = errorLevel;
	}
	
	/**
	 * Sets the output type for a specific level
	 * example: STDERR,Level.FINER would cause any Records that are marked FINER to go to stderr, while both FINE, and FINEST would still go to stdout
	 * @param output
	 * @param level
	 */
	public void setOutputForLevel(Output output, Level level)
	{
		levelHashMap.put(level, output);
	}
	
	@Override
	public void close() throws SecurityException {}
	@Override
	public void flush(){}
	
	

}
