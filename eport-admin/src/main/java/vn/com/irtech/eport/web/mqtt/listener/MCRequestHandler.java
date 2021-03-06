package vn.com.irtech.eport.web.mqtt.listener;

import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import vn.com.irtech.eport.framework.web.service.WebSocketService;

@Component
public class MCRequestHandler implements IMqttMessageListener{
	
	private static final Logger logger = LoggerFactory.getLogger(MCRequestHandler.class);
	
	@Autowired
	private WebSocketService webSocketService;

	@Autowired
	@Qualifier("threadPoolTaskExecutor")
	private TaskExecutor executor;

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					processMessage(topic, message);
				} catch (Exception e) {
					logger.error("Error while process mq message", e);
					e.printStackTrace();
				}
			}
		});
	}
	
	private void processMessage(String topic, MqttMessage message) throws Exception {
		
		logger.info("Receive message subject : " + topic);
		String messageContent = new String(message.getPayload());
		logger.info("Receive message content : " + messageContent);
		Map<String, Object> map = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			map = new Gson().fromJson(messageContent, Map.class);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		if (map.get("pickupHistoryId") == null) {
			return;
		}
		
		Long shipmentDetailId = Long.parseLong(map.get("pickupHistoryId").toString());
		webSocketService.sendMessage("/mc/plan/request", shipmentDetailId);
	}

}
