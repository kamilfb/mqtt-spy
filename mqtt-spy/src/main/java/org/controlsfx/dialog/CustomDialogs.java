/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - extended class, based on ControlsFx code
 *    
 */
package org.controlsfx.dialog;

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.getString;
import static impl.org.controlsfx.i18n.Localization.localize;
import static org.controlsfx.dialog.Dialog.ACTION_CANCEL;
import static org.controlsfx.dialog.Dialog.ACTION_NO;
import static org.controlsfx.dialog.Dialog.ACTION_OK;
import static org.controlsfx.dialog.Dialog.ACTION_YES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Pair;

import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.ButtonBar.ButtonType;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.textfield.CustomPasswordField;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

@SuppressWarnings("deprecation")
/**
 * This is an extended version of the ControlsFx Dialogs class to add required functionality. 
 * Most of the code has been copied from the ControlsFx Dialogs class.
 */
public class CustomDialogs
{
	/**
	 * USE_DEFAULT can be passed in to {@link #title(String)} and
	 * {@link #masthead(String)} methods to specify that the default text for
	 * the dialog should be used, where the default text is specific to the type
	 * of dialog being shown.
	 */
	public static final String USE_DEFAULT = "$$$";

	private Object owner;
	private String title = USE_DEFAULT;
	private String message;
	private String masthead;
	private Effect backgroundEffect;

	/**
	 * Assigns the owner of the dialog. If an owner is specified, the dialog
	 * will block input to the owner and all parent owners. If no owner is
	 * specified, or if owner is null, the dialog will block input to the entire
	 * application.
	 * 
	 * @param owner
	 *            The dialog owner.
	 * @return dialog instance.
	 */
	public CustomDialogs owner(final Object owner)
	{
		this.owner = owner;		
		return this;
	}

	/**
	 * Assigns dialog's title
	 * 
	 * @param title
	 *            dialog title
	 * @return dialog instance.
	 */
	public CustomDialogs title(final String title)
	{
		this.title = title;
		return this;
	}

	/**
	 * Assigns dialog's masthead
	 * 
	 * @param masthead
	 *            dialog masthead
	 * @return dialog instance.
	 */
	public CustomDialogs masthead(final String masthead)
	{
		this.masthead = masthead;
		return this;
	}

	/** This a replacement for Dialogs.showLogin. */
    public Optional<Pair<String,String>> showLogin( final Pair<String,String> initialUserInfo, final Callback<Pair<String,String>, Void> authenticator ) {
    	
    	final CustomTextField txUserName = (CustomTextField) TextFields.createClearableTextField();
    	txUserName.setLeft(new ImageView( DialogResources.getImage("login.user.icon")) ); //$NON-NLS-1$
    	
    	final CustomPasswordField txPassword = (CustomPasswordField) TextFields.createClearablePasswordField();
    	txPassword.setLeft(new ImageView( DialogResources.getImage("login.password.icon"))); //$NON-NLS-1$
		
		final Label lbMessage= new Label("");  //$NON-NLS-1$
		lbMessage.getStyleClass().addAll("message-banner"); //$NON-NLS-1$
		lbMessage.setVisible(false);
		lbMessage.setManaged(false);
		
		final VBox content = new VBox(10);
		content.getChildren().add(new Label("User name"));
		content.getChildren().add(txUserName);
		content.getChildren().add(txPassword);
		
		final Action actionLogin = new DialogAction("Connect", null, false, false, true) { //$NON-NLS-1$
			{
				ButtonBar.setType(this, ButtonType.OK_DONE);
				setEventHandler(this::handleAction);
			}
			
			protected void handleAction(ActionEvent ae) {
				Dialog dlg = (Dialog) ae.getSource();
				try {
					if ( authenticator != null ) {
						authenticator.call(new Pair<>(txUserName.getText(), txPassword.getText()));
					}
					lbMessage.setVisible(false);
					lbMessage.setManaged(false);
					dlg.hide();
					dlg.setResult(this);
				} catch( Throwable ex ) {
					lbMessage.setVisible(true);
					lbMessage.setManaged(true);
					lbMessage.setText(ex.getMessage());
					dlg.sizeToScene();
					dlg.shake();
					ex.printStackTrace();
				}
			}

			@Override public String toString() {
				return "LOGIN"; //$NON-NLS-1$
			};
		};
		
		final Dialog dlg = buildDialog(Type.LOGIN);
        dlg.setContent(content);
        
        dlg.setResizable(false);
		dlg.setIconifiable(false);
		if ( dlg.getGraphic() == null ) { 
			dlg.setGraphic( new ImageView( DialogResources.getImage("login.icon"))); //$NON-NLS-1$
		}
		dlg.getActions().setAll(actionLogin, ACTION_CANCEL);
		final String userNameCation = getString("login.dlg.user.caption"); //$NON-NLS-1$
		final String passwordCaption = getString("login.dlg.pswd.caption"); //$NON-NLS-1$
		txUserName.setPromptText(userNameCation);
		txUserName.setText( initialUserInfo.getKey());
		txPassword.setPromptText(passwordCaption);
		txPassword.setText(new String(initialUserInfo.getValue()));

		final ValidationSupport validationSupport = new ValidationSupport();
		Platform.runLater(new Runnable()
		{
		@Override
		public void run()
		{
			String requiredFormat = "'%s' is required"; //$NON-NLS-1$
			validationSupport.registerValidator(txUserName, Validator.createEmptyValidator( String.format( requiredFormat, userNameCation )));
			actionLogin.disabledProperty().bind(validationSupport.invalidProperty());
			txUserName.requestFocus();			
		}});

		dlg.sizeToScene();
    	return Optional.ofNullable( 
    			dlg.show() == actionLogin? 
    					new Pair<>(txUserName.getText(), txPassword.getText()): 
    					null);
    }
	
	/**
     * Show a dialog filled with provided command links. Command links are used instead of button bar and represent 
     * a set of available 'radio' buttons
     * @param defaultCommandLink command is set to be default. Null means no default
     * @param links list of command links presented in specified sequence 
     * @return action used to close dialog (it is either one of command links or CANCEL) 
     */
    public Action showCommandLinks(DialogAction defaultCommandLink, List<DialogAction> links, final int minWidth, final int longMessageMinHeight, double maxHeight) 
    {
        final CustomDialog dlg = buildDialog(Type.INFORMATION);
        dlg.setContentWithNoMaxWidth(message);             
        
        Node messageNode = dlg.getContent();
        messageNode.getStyleClass().add("command-link-message");
        
        final int gapSize = 10;
        final List<Button> buttons = new ArrayList<>(links.size());
        
		GridPane content = new GridPane()
		{
			@Override
			protected double computePrefWidth(double height)
			{
				double pw = 0;

				for (int i = 0; i < buttons.size(); i++)
				{
					Button btn = buttons.get(i);
					pw = Math.min(pw, btn.prefWidth(-1));
				}
				return pw + gapSize;
			}

			@Override
			protected double computePrefHeight(double width)
			{
				double ph = masthead == null || masthead.isEmpty() ? 0 : 10;

				for (int i = 0; i < buttons.size(); i++)
				{
					Button btn = buttons.get(i);
					ph += btn.prefHeight(width) + gapSize;
				}
				return ph * 1.5;
			}
		};
		content.setMinWidth(minWidth);
        content.setHgap(gapSize);
        content.setVgap(gapSize);
        
        int row = 0;
        // Node message = dlg.getContent();
		if (message != null)
		{
			content.add(messageNode, 0, row++);
		}
        
		for (final DialogAction commandLink : links)
		{
			if (commandLink == null)
				continue;

			final Button button = buildCommandLinkButton(commandLink, longMessageMinHeight, maxHeight);
			button.setDefaultButton(commandLink == defaultCommandLink);
			button.setOnAction(new EventHandler<ActionEvent>()
			{
				@Override
				public void handle(ActionEvent ae)
				{
					commandLink.handle(new ActionEvent(dlg, ae.getTarget()));
				}
			});

			GridPane.setHgrow(button, Priority.ALWAYS);
			GridPane.setVgrow(button, Priority.ALWAYS);
			content.add(button, 0, row++);
			buttons.add(button);
		}
        
        // last button gets some extra padding (hacky)
        GridPane.setMargin(buttons.get(buttons.size() - 1), new Insets(0,0,10,0));
        
        dlg.setContent(content);
        dlg.getActions().clear();
        
        return dlg.show();
    }
    
    private Button buildCommandLinkButton(DialogAction commandLink, final int longMessageMinHeight, double maxHeight) 
    {
        // put the content inside a button
        final Button button = new Button();
        button.getStyleClass().addAll("command-link-button");
        button.setMaxHeight(maxHeight);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        
        final Label titleLabel = new Label(commandLink.getText() );
        titleLabel.minWidthProperty().bind(new DoubleBinding() {
            {
                bind(titleLabel.prefWidthProperty());
            }
            
            @Override protected double computeValue() {
                return titleLabel.getPrefWidth() + 400;
            }
        });
        titleLabel.getStyleClass().addAll("line-1");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.TOP_LEFT);
        GridPane.setVgrow(titleLabel, Priority.NEVER);

        Label messageLabel = new Label(commandLink.getLongText() );
        messageLabel.setMinHeight(longMessageMinHeight);
        messageLabel.setPrefHeight(longMessageMinHeight + 10);
        //messageLabel.setMaxHeight(longMessageMaxHeight);
        messageLabel.getStyleClass().addAll("line-2");
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.TOP_LEFT);
        messageLabel.setMaxHeight(Double.MAX_VALUE);
        // GridPane.setVgrow(messageLabel, Priority.SOMETIMES);
        GridPane.setVgrow(messageLabel, Priority.ALWAYS);
        
        Node graphic = commandLink.getGraphic();
        Node view = graphic == null? new ImageView( DialogResources.getImage("command.link.icon")) : graphic;
        Pane graphicContainer = new Pane(view);
        graphicContainer.getStyleClass().add("graphic-container");
        GridPane.setValignment(graphicContainer, VPos.TOP);
        GridPane.setMargin(graphicContainer, new Insets(0,10,0,0));
        
        GridPane grid = new GridPane();
        grid.minWidthProperty().bind(titleLabel.prefWidthProperty());
        grid.setMaxHeight(Double.MAX_VALUE);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.getStyleClass().add("container");
        grid.add(graphicContainer, 0, 0, 1, 2);
        grid.add(titleLabel, 1, 0);
        grid.add(messageLabel, 1, 1);

        button.setGraphic(grid);
        button.minWidthProperty().bind(titleLabel.prefWidthProperty());
        
        return button;
    }
    
	private CustomDialog buildDialog(final Type dlgType)	
	{
		return buildDialog(dlgType, false);
	}
	
    private CustomDialog buildDialog(final Type dlgType, final boolean resizable)
	{
		String actualTitle = title == null ? null : USE_DEFAULT.equals(title) ? dlgType
				.getDefaultTitle() : title;
		String actualMasthead = masthead == null ? null : (USE_DEFAULT.equals(masthead) ? dlgType
				.getDefaultMasthead() : masthead);
		CustomDialog dlg = new CustomDialog(owner, actualTitle);
		dlg.setResizable(resizable);
		dlg.setIconifiable(false);
		Image image = dlgType.getImage();
		if (image != null)
		{
			dlg.setGraphic(new ImageView(image));
		}
		dlg.setMasthead(actualMasthead);
		dlg.getActions().addAll(dlgType.getActions());
		dlg.setBackgroundEffect(backgroundEffect);
		return dlg;
	}

	 private static enum Type {
	        ERROR("error.image",          asKey("error.dlg.title"),   asKey("error.dlg.header"), ACTION_OK), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	        INFORMATION("info.image",     asKey("info.dlg.title"),    asKey("info.dlg.header"), ACTION_OK), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	        WARNING("warning.image",      asKey("warning.dlg.title"), asKey("warning.dlg.header"), ACTION_OK), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	        CONFIRMATION("confirm.image", asKey("confirm.dlg.title"), asKey("confirm.dlg.header"), ACTION_YES, ACTION_NO, ACTION_CANCEL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	        INPUT("confirm.image",        asKey("input.dlg.title"),   asKey("input.dlg.header"), ACTION_OK, ACTION_CANCEL), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	        FONT( null,                   asKey("font.dlg.title"),    asKey("font.dlg.header"), ACTION_OK, ACTION_CANCEL), //$NON-NLS-1$ //$NON-NLS-2$
	        PROGRESS("info.image",        asKey("progress.dlg.title"), asKey("progress.dlg.header")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	        LOGIN("login.icon",           asKey("login.dlg.title"),    asKey("login.dlg.header"), ACTION_OK, ACTION_CANCEL); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	        private final String defaultTitle;
	        private final String defaultMasthead;
	        private final Collection<Action> actions;
	        private final String imageResource;
	        private Image image;

	        Type(String imageResource, String defaultTitle, String defaultMasthead, Action... actions) {
	            this.actions = Arrays.asList(actions);
	            this.imageResource = imageResource;
	            this.defaultTitle = defaultTitle;
	            this.defaultMasthead = defaultMasthead;
	        }

	        public Image getImage() {
	            if (image == null && imageResource != null ) {
	                image = DialogResources.getImage(imageResource);
	            }
	            return image;
	        }

	        public String getDefaultMasthead() {
	            return localize(defaultMasthead);
	        }

	        public String getDefaultTitle() {
	            return localize(defaultTitle);
	        }

	        public Collection<Action> getActions() {
	            return actions;
	        }
	    }

    /**
     * Assigns dialog's instructions
     * @param message dialog message
     * @return dialog instance.
     */
    public CustomDialogs message(final String message) {
        this.message = message;
        return this;
    }
}
