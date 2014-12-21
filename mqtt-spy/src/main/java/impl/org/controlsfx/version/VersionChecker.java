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
package impl.org.controlsfx.version;

/**
 * Override for the ControlsFX version checker - don't need that.
 */
public class VersionChecker 
{
    private VersionChecker() 
    {
        // no-op
    }

    /** Bug in the ControlxFX version checker, so override. */
    public static void doVersionCheck() 
    {
    	return;
    }
}