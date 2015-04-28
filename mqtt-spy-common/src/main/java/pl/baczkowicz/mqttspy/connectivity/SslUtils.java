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

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemReader;

import pl.baczkowicz.mqttspy.exceptions.MqttSpyException;

public class SslUtils
{
    public static byte[] loadPemFile(final String file) throws IOException 
    {
        final PemReader pemReader = new PemReader(new FileReader(file));
        final byte[] content = pemReader.readPemObject().getContent();
        pemReader.close();
        return content;        
    }
    
    public static PrivateKey loadPrivateKeyFromPemFile(final String keyPemFile, final String algorithm) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException 
    {
        final PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(loadPemFile(keyPemFile));
        final PrivateKey privateKey = KeyFactory.getInstance(algorithm).generatePrivate(privateKeySpec);
        return privateKey;
    }
    
    public static X509Certificate loadX509CertificatePem(String crtFile) throws CertificateException, IOException 
    {
        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final FileInputStream fileStream = new FileInputStream(crtFile);
        final X509Certificate certificate = (X509Certificate) cf.generateCertificate(fileStream);
        fileStream.close();
        return certificate;
    }
	
	public static TrustManagerFactory getTrustManagerFactory(final String caCertificateFile) 
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException
	{
		// Load CA certificate
		final X509Certificate caCertificate = (X509Certificate) loadX509CertificatePem(caCertificateFile);
		
		// CA certificate is used to authenticate server
		final KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
		caKs.load(null, null);
		caKs.setCertificateEntry("ca-certificate", caCertificate);
		
		final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(caKs);
		
		return tmf;
	}
	
	public static KeyManagerFactory getKeyManagerFactory(final String clientCertificateFile, final String clientKeyFile, final String clientKeyPassword) 
			throws NoSuchAlgorithmException, IOException, KeyStoreException, CertificateException, UnrecoverableKeyException, InvalidKeySpecException
	{
		// Load client certificate
		final X509Certificate clientCertificate = loadX509CertificatePem(clientCertificateFile);			

		// Load client private key
		final PrivateKey privateKey = loadPrivateKeyFromPemFile(clientKeyFile, "RSA");

		// Client key and certificates are sent to server
		final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		ks.setCertificateEntry("certificate", clientCertificate);
		ks.setKeyEntry("private-key", privateKey, clientKeyPassword.toCharArray(), new Certificate[] { clientCertificate });
		
		final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, clientKeyPassword.toCharArray());
		
		return kmf;
	}
	

	public static SSLSocketFactory getSocketFactory(final String caCrtFile, final String protocolVersion) throws MqttSpyException
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
			final String protocolVersion) throws MqttSpyException
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
