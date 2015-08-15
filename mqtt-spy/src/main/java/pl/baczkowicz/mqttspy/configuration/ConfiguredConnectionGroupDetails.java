/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.mqttspy.configuration;

import pl.baczkowicz.mqttspy.configuration.generated.ConnectionGroup;

public class ConfiguredConnectionGroupDetails extends ConnectionGroup
{
	private boolean modified;
	
	private boolean newConnection;
	
	private ConnectionGroup lastSavedValues;

	public ConfiguredConnectionGroupDetails(final ConnectionGroup group, final boolean newConnection)
	{
		this.modified = newConnection;
		this.newConnection = newConnection;
		setGroupDetails(group);
		setLastSavedValues(new ConnectionGroup(group.getID(), group.getName(), 
				group.getGroup(), group.getSubgroups(), group.getConnections()));
	}
	
	public void setGroupDetails(final ConnectionGroup groupDetails)
	{
		if (groupDetails != null)
		{
			groupDetails.copyTo(this);
		}
	}
	
	/**
	 * @return the modified
	 */
	public boolean isModified()
	{
		return modified;
	}

	/**
	 * @param modified the modified to set
	 */
	public void setModified(boolean modified)
	{
		this.modified = modified;
	}

	/**
	 * @return the lastSavedValues
	 */
	public ConnectionGroup getLastSavedValues()
	{
		return lastSavedValues;
	}

	/**
	 * @param lastSavedValues the lastSavedValues to set
	 */
	public void setLastSavedValues(final ConnectionGroup lastSavedValues)
	{
		this.lastSavedValues = lastSavedValues;
	}
	
	public void undo()
	{
		setID(lastSavedValues.getID());
		setName(lastSavedValues.getName());
		setGroup(lastSavedValues.getGroup());
		modified = newConnection;
	}

	public void apply()
	{
		setLastSavedValues(new ConnectionGroup(getID(), getName(), getGroup(), getSubgroups(), getConnections()));
		modified = false;
		newConnection = false;
	}
}
