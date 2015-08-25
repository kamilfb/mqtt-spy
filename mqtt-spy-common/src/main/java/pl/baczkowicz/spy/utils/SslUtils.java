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
package pl.baczkowicz.spy.utils;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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

import pl.baczkowicz.spy.exceptions.SpyException;

/**
 * Utility class for handling SSL/TLS connections.
 */
public class SslUtils
{
	/**
	 * Loads a PEM file from the specified location.
	 * 
	 * @param file Location of the file to load
	 * 
	 * @return Content of the PEM file
	 * 
	 * @throws IOException Thrown when cannot read the file
	 */
    public static byte[] loadPemFile(final String file) throws IOException 
    {
        final PemReader pemReader = new PemReader(new FileReader(file));
        final byte[] content = pemReader.readPemObject().getContent();
        pemReader.close();
        return content;        
    }

    /**
     * Loads a Private Key from the specified location and algorithm.
     */
    public static PrivateKey loadPrivateKeyFromPemFile(final String keyPemFile, final String algorithm) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException 
    {
        final PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(loadPemFile(keyPemFile));
        final PrivateKey privateKey = KeyFactory.getInstance(algorithm).generatePrivate(privateKeySpec);
        return privateKey;
    }
    
    /**
     * Loads an X509 certificate from the given location.
     */
    public static X509Certificate loadX509Certificate(final String certificateFile) throws IOException, CertificateException 
    {
        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        final InputStream inputStream = FileUtils.loadFileByName(certificateFile);
        final X509Certificate certificate = (X509Certificate) cf.generateCertificate(inputStream);
        inputStream.close();
        return certificate;
    }
	
    /**
     * Creates a trust manager factory.
     */
	public static TrustManagerFactory getTrustManagerFactory(final String caCertificateFile) 
			throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException
	{
		// Load CA certificate
		final X509Certificate caCertificate = (X509Certificate) loadX509Certificate(caCertificateFile);
		
		// CA certificate is used to authenticate server
		final KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
		caKs.load(null, null);
		caKs.setCertificateEntry("ca-certificate", caCertificate);
		
		final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(caKs);
		
		return tmf;
	}
	
	/**
	 * Creates a key manager factory.
	 */
	public static KeyManagerFactory getKeyManagerFactory(final String clientCertificateFile, final String clientKeyFile, final String clientKeyPassword) 
			throws IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, InvalidKeySpecException
	{
		// Load client certificate
		final X509Certificate clientCertificate = loadX509Certificate(clientCertificateFile);			

		// Load private client key
		final PrivateKey privateKey = loadPrivateKeyFromPemFile(clientKeyFile, "RSA");

		// Client key and certificate are sent to server
		final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		ks.setCertificateEntry("certificate", clientCertificate);
		ks.setKeyEntry("private-key", privateKey, clientKeyPassword.toCharArray(), new Certificate[] { clientCertificate });
		
		final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, clientKeyPassword.toCharArray());
		
		return kmf;
	}
	
	/**
	 * Creates an SSL/TLS socket factory with the given CA certificate file and protocol version.
	 */
	public static SSLSocketFactory getSocketFactory(final String caCrtFile, final String protocolVersion) throws SpyException
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
			throw new SpyException("Cannot create SSL/TLS connection", e);
		}
	}
	
	/**
	 * Creates an SSL/TLS socket factory with the given CA certificate file, client certificate, key&password and protocol version.
	 */
	public static SSLSocketFactory getSocketFactory(final String caCrtFile,
			final String crtFile, final String keyFile, final String password,
			final String protocolVersion) throws SpyException
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
			throw new SpyException("Cannot create SSL/TLS connection", e);
		}			
	}
}
