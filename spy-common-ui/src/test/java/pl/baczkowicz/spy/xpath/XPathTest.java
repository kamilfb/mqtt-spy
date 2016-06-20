package pl.baczkowicz.spy.xpath;

import java.io.StringReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.xml.sax.InputSource;

import junit.framework.TestCase;

public class XPathTest extends TestCase
{

	@Test
	public void test()
	{
		final String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"  +

		"<book category=\"COOKING\"> <year>2005</year> <price>30.00</price> </book>";
		
		assertEquals(evaluateXPath("/book/price", message), 30.0);
	}
	
	protected Double evaluateXPath(final String expression, final String message)
	{
		Double value = 0.0;
		final XPath xpath = XPathFactory.newInstance().newXPath();
		final InputSource inputSource = new InputSource(new StringReader(message));

		try
		{
			final XPathExpression exp = xpath.compile(expression);
			value = (Double) exp.evaluate(inputSource, XPathConstants.NUMBER);
		}
		catch (XPathExpressionException e)
		{
			// TODO
		}

		return value;
	}
}