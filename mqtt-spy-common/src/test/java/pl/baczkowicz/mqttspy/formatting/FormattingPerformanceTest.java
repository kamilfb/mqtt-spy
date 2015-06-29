package pl.baczkowicz.mqttspy.formatting;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Test;

import pl.baczkowicz.mqttspy.common.generated.ConversionMethod;
import pl.baczkowicz.mqttspy.common.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.common.generated.FormatterFunction;
import pl.baczkowicz.mqttspy.common.generated.SubstringConversionFormatterDetails;
import pl.baczkowicz.mqttspy.common.generated.SubstringExtractFormatterDetails;
import pl.baczkowicz.mqttspy.storage.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.utils.FormattingUtils;

public class FormattingPerformanceTest
{
	private FormatterDetails defaultFormatter = FormattingUtils.createBasicFormatter("default", "Plain", ConversionMethod.PLAIN);
	
	@Test
	public void compareFormattingMethods()
	{
		final String payload = "<Body>VGhpcyBpcyBhIHNhbXBsZSBtZXNzYWdlIGVuY29kZWQgaW4gQkFTRTY0Lg==</Body>";
		final FormattedMqttMessage message = new FormattedMqttMessage(0, "test", new MqttMessage(payload.getBytes()), null);
		
		long startTime = 0;
		long totalTime = 0;
		long repeat = 100000;
		
		final FormatterDetails functionBased = new FormatterDetails();
		functionBased.setID("base64-body-decoder");
		functionBased.setName("Base64 body decoder");
		//	  <Function>
		//        <SubstringConversion>
		//            <StartTag>&lt;Body&gt;</StartTag>
		//            <EndTag>&lt;/Body&gt;</EndTag>
		//            <KeepTags>true</KeepTags>
		//            <Format>Base64Decode</Format>
		//        </SubstringConversion>
		//    </Function>
		//    <Function>
		//        <SubstringExtract>
		//            <StartTag>&lt;Body&gt;</StartTag>
		//            <EndTag>&lt;/Body&gt;</EndTag>
		//            <KeepTags>false</KeepTags>
		//        </SubstringExtract>
		//    </Function>

		final SubstringConversionFormatterDetails substringConversion = 
				new SubstringConversionFormatterDetails("&lt;Body&gt;", "&lt;/Body&gt;", true, ConversionMethod.BASE_64_DECODE);
		functionBased.getFunction().add(new FormatterFunction(null, substringConversion, null, null, null, null));
		
        final SubstringExtractFormatterDetails 
        	substringExtract = new SubstringExtractFormatterDetails("&lt;Body&gt;", "&lt;/Body&gt;", false);
		functionBased.getFunction().add(new FormatterFunction(null, null, null, substringExtract, null, null));
		
		startTime = System.nanoTime();
		for (int i = 0; i < repeat; i++)
		{
			message.format(functionBased);
			message.format(defaultFormatter);
			// FormattingUtils.formatText(functionBased, messageToFormat, messageToFormat.getBytes());
		}
		totalTime = System.nanoTime() - startTime;
		
		
		System.out.println("Function-based took " + totalTime + " ns; avg = " + (totalTime / repeat) + " ns");
	}
}
