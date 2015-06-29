function formatPayload()
{
	receivedMessage.setPayload("<tag>" + receivedMessage.getPayload() + "- modified :)</tag>");
	
	return true;
}

formatPayload();