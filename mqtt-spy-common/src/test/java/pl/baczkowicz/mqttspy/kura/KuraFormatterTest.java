package pl.baczkowicz.mqttspy.kura;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.kura.KuraInvalidMessageException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KuraFormatterTest
{
	/** Diagnostic logger. */
	private static final Logger logger = LoggerFactory.getLogger(KuraFormatterTest.class);
	
	@Test
	public void testKuraPayloadFormatting() throws KuraInvalidMessageException, IOException
	{
		final Path path = Paths.get("src/test/resources/kura/kura.birth");
		byte[] data = Files.readAllBytes(path);
		
		logger.info(KuraPayloadFormatter.format(data));
	}

}
