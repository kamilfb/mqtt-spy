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
package pl.baczkowicz.spy.security;

import java.security.KeyStoreException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

import pl.baczkowicz.spy.common.generated.KeyStoreTypeEnum;

public class SecureSocketUtilsTest
{

	@Test
	public void testGetKeyStoreInstance() throws KeyStoreException
	{
		SecureSocketUtils.getKeyStoreInstance(KeyStoreTypeEnum.DEFAULT);
	}

	@Test
	public void testGetKeyStoreInstanceBaseTypes() throws KeyStoreException
	{
		SecureSocketUtils.getKeyStoreInstance(KeyStoreTypeEnum.JKS);
		
		SecureSocketUtils.getKeyStoreInstance(KeyStoreTypeEnum.JCEKS);
		
		SecureSocketUtils.getKeyStoreInstance(KeyStoreTypeEnum.PKCS_12);		
	}

	@Test
	public void testGetKeyStoreInstanceBKSType() throws KeyStoreException
	{
		// Try by supplying a provider
		SecureSocketUtils.getKeyStoreInstance(KeyStoreTypeEnum.BKS, new BouncyCastleProvider());
		
		// Try be registering a provider
		Security.addProvider(new BouncyCastleProvider());
		SecureSocketUtils.getKeyStoreInstance(KeyStoreTypeEnum.BKS);			
	}
}
