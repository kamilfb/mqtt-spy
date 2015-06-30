package pl.baczkowicz.mqttspy.formatting;

import javax.script.ScriptException;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Test;

import pl.baczkowicz.mqttspy.common.generated.ConversionMethod;
import pl.baczkowicz.mqttspy.common.generated.FormatterDetails;
import pl.baczkowicz.mqttspy.common.generated.FormatterFunction;
import pl.baczkowicz.mqttspy.common.generated.ScriptDetails;
import pl.baczkowicz.mqttspy.common.generated.ScriptExecutionDetails;
import pl.baczkowicz.mqttspy.common.generated.SubstringConversionFormatterDetails;
import pl.baczkowicz.mqttspy.common.generated.SubstringExtractFormatterDetails;
import pl.baczkowicz.mqttspy.scripts.ScriptBasedFormatter;
import pl.baczkowicz.mqttspy.scripts.ScriptManager;
import pl.baczkowicz.mqttspy.storage.FormattedMqttMessage;
import pl.baczkowicz.mqttspy.utils.FormattingUtils;

public class FormattingPerformanceTest
{
	private FormatterDetails defaultFormatter = FormattingUtils.createBasicFormatter("default", "Plain", ConversionMethod.PLAIN);
	
	@Test
	public void compareFormattingMethods() throws NoSuchMethodException, ScriptException
	{
		final String payload = "<Body>VGhpcyBpcyBhIHNhbXBsZSBtZXNzYWdlIGVuY29kZWQgaW4gQkFTRTY0Lg==</Body>";
		final FormattedMqttMessage message = new FormattedMqttMessage(0, "test", new MqttMessage(payload.getBytes()), null);
		
		long startTime = 0;
		long totalTime = 0;
		long repeat = 10000;
		
		// 1. Function-based
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
		}
		totalTime = System.nanoTime() - startTime;
		System.out.println("Function-based took " + totalTime + " ns; avg = " + (totalTime / repeat) + " ns");
		
		// 2. Script file-based		
		ScriptManager scriptManager = new ScriptManager(null, null, null);
		final String scriptFile1 = "/home/kamil/Programming/Git/mqtt-spy-common/src/test/resources/scripts/base64-body-decoder_1.js";
		scriptManager.addScript(new ScriptDetails(false, false, scriptFile1));
		
		startTime = System.nanoTime();
		for (int i = 0; i < repeat; i++)
		{
			message.setPayload(payload);
			scriptManager.runScriptFileWithReceivedMessage(scriptFile1, message);	
		}
		System.out.println("Message payload = " + message.getPayload());
		
		totalTime = System.nanoTime() - startTime;		
		System.out.println("Script file-based took " + totalTime + " ns; avg = " + (totalTime / repeat) + " ns");
		
		// 3. Script function-based
		final String scriptFile2 = "/home/kamil/Programming/Git/mqtt-spy-common/src/test/resources/scripts/base64-body-decoder_2.js";
		final FormatterDetails scriptFunctionBased = new FormatterDetails();
		scriptFunctionBased.setID("script-base64-body-decoder");
		scriptFunctionBased.setName("Script-based BASE64 body decoder");
	
		ScriptExecutionDetails scriptExecution = new ScriptExecutionDetails(scriptFile2);
		scriptFunctionBased.getFunction().add(new FormatterFunction(null, null, null, null, null, scriptExecution));
		
		final ScriptBasedFormatter scriptFormatter = new ScriptBasedFormatter();
		scriptFormatter.setScriptManager(scriptManager);
		//scriptFormatter.addFormatter(scriptFunctionBased);
		startTime = System.nanoTime();
		String result = "";
		for (int i = 0; i < repeat; i++)
		{
			result = scriptFormatter.formatMessage(scriptFunctionBased, message);
		}
		System.out.println("Message payload = " + result);
		
		totalTime = System.nanoTime() - startTime;		
		System.out.println("Script function-based took " + totalTime + " ns; avg = " + (totalTime / repeat) + " ns");
	}
}
