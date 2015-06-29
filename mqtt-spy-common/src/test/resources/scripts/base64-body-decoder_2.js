function format()
{
	receivedMessage.setPayload("<tag>" + receivedMessage.getPayload() + "- modified :)</tag>");
	
	return true;
}