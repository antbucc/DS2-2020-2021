package ds2.protocol.ssb;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class acts as a container for the different topologies of messages that
 * can be exchanged by two peers during an update. 
 *
 */
public class ProtocolMessage<T> {
		
	
		/**
		 * Enumeration of the types that the objects of this class can have.
		 */
		public static enum MessageType{
			UPDATE_INIT, // ids sent with this messagE
			FRONTIER,
			NEWS, // news sent with this message
		}
		
		/**
		 * The type of the instantiated ProtocolMessage.
		 */
		private MessageType type;
		
		/**
		 * Generic content that the ProtocolMessage can contain.
		 */
		private T data; 
		
		/**
		 * HahsMap to hold the ids for the UPDATE_INIT message. It is set to 
		 * contain something only for that type of message, else it remains null, or
		 * empty.
		 */
		private HashSet<PublicKey> ids = null;
		
		/**
		 * HashMap for the exchanged frontier, to be set in the FRONTIER type. 
		 * It remains null, or empty, for the other types of message.
		 */
		private HashMap<PublicKey, Integer> frontier = null;
		
		/**
		 * HashMap for the news that are sent during an update. To be set 
		 * to hold some content in the NEWS type, null or empty otherwise.
		 */
		private HashMap<PublicKey, ArrayList<SSBEvent>> news = null;
		
		/**
		 * Constructor, it initializes the message to be of a certain type and
		 * to contain one of among the ids, frontier and news.
		 * @param type
		 * @param data
		 */
		public ProtocolMessage(MessageType type, T data) {
			this.type = type;
			this.data = data;
			if (type == MessageType.UPDATE_INIT) {
				if(data instanceof HashSet<?>) {
					ids = (HashSet<PublicKey>) data;
				} else {
					System.err.println("ProtocolMessage: Unexpected type for UPDATE_INIT.");
				}
			} else if (type == MessageType.FRONTIER) {
				if (data instanceof HashMap<?,?>) {
					frontier = (HashMap<PublicKey, Integer>) data;
				} else {
					System.err.println("ProtocolMessage: Unexpected type for FRONTIER.");
				}
			} else {
				// type =  NEWS
				if (data instanceof HashMap<?,?>) {
					news = (HashMap<PublicKey, ArrayList<SSBEvent>>) data;
				} else {
					System.err.println("ProtocolMessage: Unexpected type for NEWS.");
				}
			}
		}

		/**
		 * Getter for the type of the ProtocolMessage.
		 * @return MessageType, the type of this object.
		 */
		public MessageType getType() {
			return type;
		}
		
		/**
		 * Setter for the type of the Message.
		 * @param type
		 */
		public void setType(MessageType type) {
			this.type = type;
		}

		/**
		 * Getter for the data that this message contains.
		 * @return
		 */
		public T getData() {
			return data;
		}

		/**
		 * Setter for the data.
		 * @param data
		 */
		public void setData(T data) {
			this.data = data;
		}

		/**
		 * Getter for the list of ids. To be called only on ProtocolMessage of type
		 * UPDATE_INIT, otherwise check for ids not null.
		 * @return
		 */
		public HashSet<PublicKey> getIds() {
			return ids;
		}

		/**
		 * Setter for the list of ids.
		 * @param ids
		 */
		public void setIds(HashSet<PublicKey> ids) {
			this.ids = ids;
		}
		
		/**
		 * Setter for the frontier. To be called only on FRONTIER type of 
		 * ProtocolMessage, otherwise it returns null.
		 * @return
		 */
		public HashMap<PublicKey, Integer> getFrontier() {
			return frontier;
		}

		/**
		 * Setter for the frontier.
		 * @param frontier
		 */
		public void setFrontier(HashMap<PublicKey, Integer> frontier) {
			this.frontier = frontier;
		}

		/**
		 * Getter for the news, to be called on NEWS type. Otherwise, it 
		 * returns null.
		 * @return
		 */
		public HashMap<PublicKey, ArrayList<SSBEvent>> getNews() {
			return news;
		}

		/**
		 * Setter for the news.
		 * @param news
		 */
		public void setNews(HashMap<PublicKey, ArrayList<SSBEvent>> news) {
			this.news = news;
		}
		
		/**
		 * Returns a string representation of the ProtocolMessage, where it indicates
		 * only the type of the message.
		 */
		public String toString() {
			String typeStr = "ProtocolMessage";
			if (this.type == MessageType.UPDATE_INIT) {
				typeStr = "ProtocolMessage: UPDATE_INIT";
			} else if (this.type == MessageType.FRONTIER) {
				typeStr = "ProtocolMessage: FRONTIER";
			} else if (this.type == MessageType.NEWS) {
				typeStr = "ProtocolMessage: NEWS";
			}
			return typeStr;
		}	
}
