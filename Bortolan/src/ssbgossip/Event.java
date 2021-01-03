package ssbgossip;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Event {
	private final String id;
	private final String previous;
	private final int index;
	private final Map<String, String> content;
	private final String signature;

	public Event(String id, String previous, int index, Map<String, String> content, String signature) {
		this.id = id;
		this.previous = previous;
		this.index = index;
		this.content = content;
		this.signature = signature;
	}

	public String getId() {
		return id;
	}

	public String getPrevious() {
		return previous;
	}

	public int getIndex() {
		return index;
	}

	public Map<String,String> getContent() {
		return content;
	}

	public String getSignature() {
		return signature;
	}

	public String toJSON() {
		GsonBuilder builder = new GsonBuilder();
		builder.serializeNulls();
		builder.disableHtmlEscaping();
		Gson gson = builder.setPrettyPrinting().create();
		return gson.toJson(this);
	}

}
