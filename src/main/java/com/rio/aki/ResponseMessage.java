package com.rio.aki;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

public class ResponseMessage {
	public final String KEY_MESSAGE = "message";
	public Map<String, Object> mResponseMap;
	public MessageSource mMsgSource;
	
	public ResponseMessage() {
		this.mResponseMap = new HashMap<>();
	}
	
	public ResponseMessage(MessageSource msgSource) {
		this();
		this.mMsgSource = msgSource;
	}

	public ResponseMessage setMessage(String value) {
		return this.setData(KEY_MESSAGE, value);
	}
	
	public ResponseMessage setData(String key, Object value) {
		this.mResponseMap.put(key, value);
		return this;
	}
	
	public ResponseMessage setMessageWithProp(String propKey, String resolvableKey) {
		return this.setDataWithProp(KEY_MESSAGE, propKey, resolvableKey);
	}
	
	public ResponseMessage setDataWithProp(String key, String propKey, String resolvableKey) {
		MessageSourceResolvable msgSourceResolvable = new DefaultMessageSourceResolvable(resolvableKey);
		String value = mMsgSource.getMessage(propKey, new MessageSourceResolvable[] {msgSourceResolvable}, Locale.JAPAN);
		this.mResponseMap.put(key, value);
		return this;
	}
	
	public String getResponse() {
		return CommonUtils.objectToJson(mResponseMap);
	}
	
}