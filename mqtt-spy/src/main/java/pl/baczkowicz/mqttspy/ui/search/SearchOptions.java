/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.mqttspy.ui.search;

/**
 * This class defines message search options.
 */
public class SearchOptions
{
	/** The string to search in payload of the message. */
	private String searchValue;
	
	/** Whether to match case. */
	private boolean matchCase;
	
	/**
	 * Creates SearchOptions with the supplied parameters.
	 * 
	 * @param searchValue
	 * @param matchCase
	 */
	public SearchOptions(String searchValue, boolean matchCase)
	{
		this.searchValue = searchValue;
		this.matchCase = matchCase;
	}

	/**
	 * Gets the search value.
	 * 
	 * @return The search value (in lowercase if match case set to false)
	 */
	public String getSearchValue()
	{
		return matchCase ? searchValue : searchValue.toLowerCase();
	}

	/**
	 * Sets the search value.
	 * 
	 * @param searchValue The new search value to set
	 */
	public void setSearchValue(final String searchValue)
	{
		this.searchValue = searchValue;
	}

	/**
	 * Returns the match case flag.
	 * 
	 * @return True if set to match case
	 */
	public boolean isMatchCase()
	{
		return matchCase;
	}

	/**
	 * Sets the match case flag.
	 * 
	 * @param matchCase The new flag value to set
	 */
	public void setMatchCase(final boolean matchCase)
	{
		this.matchCase = matchCase;
	}
}	
