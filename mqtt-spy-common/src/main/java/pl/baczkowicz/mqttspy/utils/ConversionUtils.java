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
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.mqttspy.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import pl.baczkowicz.mqttspy.exceptions.ConversionException;

/** 
 * String conversion utilities.
 */
public class ConversionUtils
{
	/**
	 * Converts the given string into a HEX string.
	 * 
	 * @param data The string to convert
	 * 
	 * @return Converted string in HEX form
	 */
	public static String stringToHex(final String data)
	{
		return new String(Hex.encodeHex(data.getBytes()));
	}
	
	/**
	 * Converts the given HEX string into a plain string.
	 * 
	 * @param data The HEX string to convert from
	 * 
	 * @return The plain string
	 * 
	 * @throws ConversionException Thrown if the given string is not a valid HEX string
	 */
	public static String hexToString(final String data) throws ConversionException
	{
		try
		{
			return new String(Hex.decodeHex(data.toCharArray()));
		}
		catch (DecoderException e)
		{
			throw new ConversionException("Cannot convert given hex text into plain text", e);
		}
	}
	
	/**
	 * Converts the given HEX string into a plain string.
	 * 
	 * @param data The HEX string to convert from
	 * 
	 * @return The plain string or [invalid hex] if invalid HEX string detected
	 */
	public static String hexToStringNoException(final String data)
	{
		try
		{
			return new String(Hex.decodeHex(data.toCharArray()));
		}
		catch (DecoderException e)
		{
			return "[invalid hex]";
		}
	}

	/**
	 * Converts Base64 string to a plain string.
	 * 
	 * @param data The Base64 string to decode
	 * 
	 * @return Decoded string
	 */
	public static String base64ToString(final String data)
	{
		return new String(Base64.decodeBase64(data));
	}
	
	/**
	 * Converts plain string into Base64 string.
	 *  
	 * @param data The string to encode to Base64
	 * 
	 * @return Encoded string (Base64)
	 */
	public static String stringToBase64(final String data)
	{
		return Base64.encodeBase64String(data.getBytes());
	}
}
