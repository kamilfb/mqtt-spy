/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
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

import java.util.List;

import pl.baczkowicz.mqttspy.configuration.generated.ConnectionGroup;
import pl.baczkowicz.mqttspy.configuration.generated.ConnectionGroupReference;
import pl.baczkowicz.mqttspy.configuration.generated.ConnectionReference;
import pl.baczkowicz.mqttspy.configuration.generated.UserInterfaceMqttConnectionDetails;

public class ConfiguredConnectionDetails extends UserInterfaceMqttConnectionDetails
{
	private boolean modified;

	private boolean begingCreated;
	
	private boolean deleted;

	private boolean newConnection;
	
	private boolean valid;

	private ConfiguredConnectionDetails lastSavedValues;	

	private ConnectionGroupReference group;

	private boolean groupingModified;

	public ConfiguredConnectionDetails()
	{
		// Default constructor
	}
	
	 /**
     * Initialising value constructor
     */
	public ConfiguredConnectionDetails(
			final ConnectionGroupReference group,
			final UserInterfaceMqttConnectionDetails connection)
	{
		this.group = group;
		connection.copyTo(this);
    }
	
	public ConfiguredConnectionDetails(final boolean created, final boolean newConnection,
			final UserInterfaceMqttConnectionDetails connection)
	{
		//this.id = id;
		this.modified = newConnection;
		this.begingCreated = created;
		this.newConnection = newConnection;		
		
		final ConfiguredConnectionDetails connectionDetails = new ConfiguredConnectionDetails(null, connection);

		setConnectionDetails(connectionDetails);
		setLastSavedValues(connectionDetails);
	}

	public void setConnectionDetails(final ConfiguredConnectionDetails connectionDetails)
	{
		// Take a copy and null it, so that copyTo can work...
		final ConnectionGroup group = connectionDetails.getGroup() != null ? (ConnectionGroup) connectionDetails.getGroup().getReference() : null;
		connectionDetails.setGroup(null);
		
		if (connectionDetails != null)
		{
			connectionDetails.copyTo(this);
		}
		
		// Restore the group value
		connectionDetails.setGroup(new ConnectionGroupReference(group));
		setGroup(new ConnectionGroupReference(group));
	}

	public boolean isModified()
	{
		return modified;
	}

	public void setModified(boolean modified)
	{
		this.modified = modified;
	}

	public boolean isBeingCreated()
	{
		return begingCreated;
	}

	public void setBeingCreated(boolean created)
	{
		this.begingCreated = created;
	}

	public UserInterfaceMqttConnectionDetails getSavedValues()
	{
		return lastSavedValues;
	}

	private void setLastSavedValues(final ConfiguredConnectionDetails savedValues)
	{
		this.lastSavedValues = savedValues;
	}

	public boolean isValid()
	{
		return valid;
	}

	public void setValid(boolean valid)
	{
		this.valid = valid;
	}
	
	public void undo()
	{
		// Make sure we won't revert any grouping changes here
		final ConfiguredConnectionGroupDetails group = (ConfiguredConnectionGroupDetails) getGroup().getReference();
		setConnectionDetails(lastSavedValues);
		setGroup(new ConnectionGroupReference(group));
		
		modified = newConnection;
	}
	
	/**
	 * This method undoes all changes, including those about grouping.
	 */
	public void undoAll()
	{
		setConnectionDetails(lastSavedValues);
		modified = newConnection;
		groupingModified = false;
	}

	public void apply()
	{
		final ConfiguredConnectionDetails valuesToSave = new ConfiguredConnectionDetails(false, false, this);
		
		setLastSavedValues(valuesToSave);
		begingCreated = false;
		newConnection = false;
		modified = false;
		groupingModified = false;
	}

	public boolean isDeleted()
	{
		return deleted;
	}

	public void setDeleted(boolean deleted)
	{
		this.deleted = deleted;
	}
	
	public boolean isNew()
	{
		return newConnection;
	}
	
	public void removeFromGroup()
	{
		removeFromGroup(this, (ConnectionGroup) getGroup().getReference());
	}
	
	public static void removeFromGroup(final ConfiguredConnectionDetails connectionToRemove, final ConnectionGroup groupToRemoveFrom)
	{
		ConnectionReference refToDelete = null;
		final List<ConnectionReference> connections = groupToRemoveFrom.getConnections(); 
		for (ConnectionReference connectionRef : connections)
		{
			if (connectionRef.getReference().equals(connectionToRemove))
			{
				refToDelete = connectionRef;
				break;
			}
		}
		connections.remove(refToDelete);
	}
	
	public void setGroupingModified(boolean modified)
	{
		this.groupingModified = modified;		
	}
	
	public boolean isGroupingModified()
	{
		return groupingModified;
	}

	/**
	 * @return the group
	 */
	public ConnectionGroupReference getGroup()
	{
		return group;
	}

	/**
	 * @param group the group to set
	 */
	public void setGroup(ConnectionGroupReference group)
	{
		this.group = group;
	}
}
