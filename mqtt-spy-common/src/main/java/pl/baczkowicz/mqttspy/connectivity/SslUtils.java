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

package pl.baczkowicz.mqttspy.connectivity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

import pl.baczkowicz.mqttspy.exceptions.MqttSpyException;

public class SslUtils
{
	public static TrustManagerFactory getTrustManagerFactory(final String caCertificateFile) 
		throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException
	{
		// Load CA certificate
		final PEMReader reader = new PEMReader(new InputStreamReader(
				new ByteArrayInputStream(Files.readAllBytes(Paths.get(caCertificateFile)))));
		final X509Certificate caCert = (X509Certificate) reader.readObject();
		reader.close();
		
		// CA certificate is used to authenticate server
		final KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
		caKs.load(null, null);
		caKs.setCertificateEntry("ca-certificate", caCert);
		
		final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(caKs);
		
		return tmf;
	}
	
	public static KeyManagerFactory getKeyManagerFactory(final String certificateFile, final String keyFile, final String password) 
		throws NoSuchAlgorithmException, IOException, KeyStoreException, CertificateException, UnrecoverableKeyException
	{
		// Load client certificate
		PEMReader reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(
				Files.readAllBytes(Paths.get(certificateFile)))));
		X509Certificate cert = (X509Certificate) reader.readObject();
		reader.close();

		// Load client private key
		reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(
				Files.readAllBytes(Paths.get(keyFile)))), new PasswordFinder()
		{
			@Override
			public char[] getPassword()
			{
				return password.toCharArray();
			}
		});
		final KeyPair key = (KeyPair) reader.readObject();
		reader.close();

		// Client key and certificates are sent to server
		final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		ks.setCertificateEntry("certificate", cert);
		ks.setKeyEntry("private-key", key.getPrivate(), password.toCharArray(), new java.security.cert.Certificate[] { cert });
		
		final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, password.toCharArray());
		
		return kmf;
	}
	

	public static SSLSocketFactory getSocketFactory(final String caCrtFile,
			String protocolVersion) throws MqttSpyException
	{
		try
		{
			Security.addProvider(new BouncyCastleProvider());
			
			// Create SSL/TLS socket factory
			final SSLContext context = SSLContext.getInstance(protocolVersion);
			
			context.init(
					null, 
					SslUtils.getTrustManagerFactory(caCrtFile).getTrustManagers(), null);
	
			return context.getSocketFactory();
		}
		catch (Exception e)
		{
			throw new MqttSpyException("Cannot create SSL connection", e);
		}
	}
	
	public static SSLSocketFactory getSocketFactory(final String caCrtFile,
			final String crtFile, final String keyFile, final String password,
			String protocolVersion) throws MqttSpyException
	{
		try
		{
			Security.addProvider(new BouncyCastleProvider());
			
			// Create SSL/TLS socket factory
			final SSLContext context = SSLContext.getInstance(protocolVersion);
			
			context.init(
					SslUtils.getKeyManagerFactory(crtFile, keyFile, password).getKeyManagers(), 
					SslUtils.getTrustManagerFactory(caCrtFile).getTrustManagers(), null);
	
			return context.getSocketFactory();			
		}
		catch (Exception e)
		{
			throw new MqttSpyException("Cannot create SSL connection", e);
		}
			
	}
}
