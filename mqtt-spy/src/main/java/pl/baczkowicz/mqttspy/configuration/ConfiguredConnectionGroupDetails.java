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

import java.util.ArrayList;
import java.util.List;

import pl.baczkowicz.mqttspy.configuration.generated.ConnectionGroup;
import pl.baczkowicz.mqttspy.configuration.generated.ConnectionGroupReference;
import pl.baczkowicz.mqttspy.configuration.generated.ConnectionReference;

public class ConfiguredConnectionGroupDetails extends ConnectionGroup
{
	private boolean modified;
	
	private boolean newGroup;
	
	private ConnectionGroup lastSavedValues;

	private boolean groupingModified;

	public ConfiguredConnectionGroupDetails(final ConnectionGroup group, final boolean newConnection)
	{
		this.modified = newConnection;
		this.newGroup = newConnection;
		setGroupDetails(group);
		setLastSavedValues(new ConnectionGroup(group.getID(), group.getName(), 
				group.getGroup(), group.getSubgroups(), group.getConnections()));
	}
	
	public void setGroupDetails(final ConnectionGroup groupDetails)
	{
		// Take a copy and null it, so that copyTo can work...
		final ConnectionGroup group = groupDetails.getGroup() != null ? (ConnectionGroup) groupDetails.getGroup().getReference() : null;
		groupDetails.setGroup(null);
		
		final List<ConnectionReference> connections = new ArrayList<>(groupDetails.getConnections());
		groupDetails.getConnections().clear();
				
		if (groupDetails != null)
		{
			groupDetails.copyTo(this);
		}
		
		// Restore the group value
		groupDetails.setGroup(new ConnectionGroupReference(group));
		setGroup(new ConnectionGroupReference(group));
		
		groupDetails.getConnections().addAll(connections);
		getConnections().addAll(connections);
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
		modified = newGroup;
	}
	
	/**
	 * This method undoes all changes, including those about grouping.
	 */
	public void undoAll()
	{
		undo();
		
		setGroup(lastSavedValues.getGroup());
		getConnections().clear();
		getConnections().addAll(lastSavedValues.getConnections());
		getSubgroups().clear();
		getSubgroups().addAll(lastSavedValues.getSubgroups());
		groupingModified = false;
	}

	public void apply()
	{
		setLastSavedValues(new ConnectionGroup(getID(), getName(), getGroup(), 
				new ArrayList<>(getSubgroups()), new ArrayList<>(getConnections())));
		modified = false;
		newGroup = false;
		groupingModified = false;
	}

	public boolean isNew()
	{
		return newGroup;
	}
	
	public void removeFromGroup()
	{
		removeFromGroup(this, (ConnectionGroup) getGroup().getReference());
	}
	
	public static void removeFromGroup(final ConnectionGroup groupToRemove, final ConnectionGroup groupToRemoveFrom)
	{
		ConnectionGroupReference refToDelete = null;
		final List<ConnectionGroupReference> subgroups = groupToRemoveFrom.getSubgroups(); 
		for (ConnectionGroupReference subgroupRef : subgroups)
		{
			if (subgroupRef.getReference().equals(groupToRemove))
			{
				refToDelete = subgroupRef;
				break;
			}
		}
		subgroups.remove(refToDelete);	
	}

	public void setGroupingModified(boolean modified)
	{
		this.groupingModified = modified;		
	}
	
	public boolean isGroupingModified()
	{
		return groupingModified;
	}
}
