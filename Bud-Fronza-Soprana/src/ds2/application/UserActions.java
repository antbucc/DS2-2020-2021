package ds2.application;

import java.security.PublicKey;

import ds2.utility.logging.Logger;

public class UserActions extends Id{

	public static enum Action {
			FOLLOW,
			UNFOLLOW,
			BLOCK,
			UNBLOCK
	}
	
	private Action action;
	private PublicKey target;
	
	public UserActions(Action action, PublicKey target) {
		super();
		this.action = action;
		this.target = target;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public PublicKey getTarget() {
		return target;
	}

	public void setTarget(PublicKey target) {
		this.target = target;
	}
	
	public String toString() {
		String res = super.toString();
		res += " Action: ";
		switch (action) {
			case FOLLOW: 
				res += "FOLLOW ";
				break;
			case UNFOLLOW:
				res += "UNFOLLOW ";
				break;
			case BLOCK:
				res += "BLOCK ";
				break;
			case UNBLOCK:
				res += "UNBLOCK ";
				break;
			default: 
				res += "UNKNOWN ACTION ";
				break;
		}
		res += "Target: " + Logger.formatPort(target);
		return res;
	}
	
}
