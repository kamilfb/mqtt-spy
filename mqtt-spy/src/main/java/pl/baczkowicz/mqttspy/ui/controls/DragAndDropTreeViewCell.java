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

package pl.baczkowicz.mqttspy.ui.controls;

import javafx.event.EventHandler;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.ui.EditConnectionsController;
import pl.baczkowicz.mqttspy.ui.properties.ConnectionListItemProperties;

public class DragAndDropTreeViewCell extends TreeCell<ConnectionListItemProperties>
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(DragAndDropTreeViewCell.class);

	private ConnectionListItemProperties item;
	
	public DragAndDropTreeViewCell(final TreeView<ConnectionListItemProperties> treeView)
	{	
		setOnDragDetected(new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(final MouseEvent event)
			{
				if (item == null)
				{
					return;
				}

				logger.debug("Drag detected on item = " + item);
				final Dragboard dragBoard = startDragAndDrop(TransferMode.MOVE);
				final ClipboardContent content = new ClipboardContent();
				content.put(DataFormat.PLAIN_TEXT, item.getId());
				dragBoard.setContent(content);
				event.consume();
			}
		});
		
		setOnDragDone(new EventHandler<DragEvent>()
		{
			@Override
			public void handle(final DragEvent dragEvent)
			{
				logger.debug("Drag done on item = " + item);
				dragEvent.consume();
			}
		});

		setOnDragOver(new EventHandler<DragEvent>()
		{
			@Override
			public void handle(final DragEvent dragEvent)
			{
				logger.debug("Drag over on item = " + item);
				if (dragEvent.getDragboard().hasString())
				{
					final String idToMove = dragEvent.getDragboard().getString();
					if (!idToMove.equals(item.getId()))
					{
						dragEvent.acceptTransferModes(TransferMode.MOVE);
					}
				}
				dragEvent.consume();
			}
		});

		setOnDragDropped(new EventHandler<DragEvent>()
		{
			@Override
			public void handle(DragEvent dragEvent)
			{
				logger.debug("Drag dropped on item = " + item);
				final String idToMove = dragEvent.getDragboard().getString();
				
				// Only move if the new parent is not the current item and the new item is a group
				if (!idToMove.equals(item.getId()) && item.isGroup())
				{
					final TreeItem<ConnectionListItemProperties> treeItemToMove = findNode(treeView.getRoot(), idToMove);
					final ConnectionListItemProperties itemToMove = treeItemToMove.getValue();
					final TreeItem<ConnectionListItemProperties> newParent = findNode(treeView.getRoot(), item.getId());
					
					// Remove from the old parent
					treeItemToMove.getParent().getChildren().remove(treeItemToMove);
					
					// Re-map helper refs
					itemToMove.getParent().getChildren().remove(itemToMove);
					
					// Add to the new parent
					newParent.getChildren().add(treeItemToMove);
					
					// Re-map helper refs
					newParent.getValue().getChildren().add(itemToMove);
					itemToMove.setParent(newParent.getValue());
					
					// Re-map connections and groups
					if (itemToMove.isGroup())
					{
						itemToMove.getConnectionGroup().setParent(item.getConnectionGroup());
					}
					else
					{
						itemToMove.getConnection().setConnectionGroup(item.getConnectionGroup());
					}

					newParent.setExpanded(true);
				}
				dragEvent.consume();
			}
		});
	}

	private TreeItem<ConnectionListItemProperties> findNode(
			final TreeItem<ConnectionListItemProperties> currentNode,
			final String idToSearch)
	{
		TreeItem<ConnectionListItemProperties> result = null;
		if (currentNode.getValue().getId().equals(idToSearch))
		{
			result = currentNode;
		}
		else if (!currentNode.isLeaf())
		{
			for (TreeItem<ConnectionListItemProperties> child : currentNode.getChildren())
			{
				result = findNode(child, idToSearch);
				if (result != null)
				{
					break;
				}
			}
		}
		return result;
	}

	@Override
	protected void updateItem(final ConnectionListItemProperties item, final boolean empty)
	{
		super.updateItem(item, empty);
		this.item = item;
		
		if (item == null)
		{
			setText(null);
			setGraphic(null);
		}
		else
		{
			ImageView image;
			
			if (!item.isGroup())
			{
				image = new ImageView(new Image(EditConnectionsController.class.getResource("/images/mqtt-icon.png").toString()));
			}		
			else
			{
				if (item.getChildren().isEmpty())
				{
					image = new ImageView(new Image(EditConnectionsController.class.getResource("/images/folder-grey.png").toString()));
				}
				else
				{
					image = new ImageView(new Image(EditConnectionsController.class.getResource("/images/folder-yellow.png").toString()));
				}
			}
			
			image.setFitHeight(18);
			image.setFitWidth(18);
			
			setText(item.getName());
			setGraphic(image);
		}
	}
}
