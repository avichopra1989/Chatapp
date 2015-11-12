package com.chatt.demo.model;

import java.util.Date;

import com.chatt.demo.UserList;
import com.parse.ParseFile;

/**
 * The Class Conversation is a Java Bean class that represents a single chat
 * conversation message.
 */
public class Conversation {

	/** The Constant STATUS_SENDING. */
	public static final int STATUS_SENDING = 0;

	/** The Constant STATUS_SENT. */
	public static final int STATUS_SENT = 1;

	/** The Constant STATUS_FAILED. */
	public static final int STATUS_FAILED = 2;
	/** The msg. */
	private String msg;

	/** The status. */
	private int status = STATUS_SENT;

	/** The date. */
	private Date date;

	/** The sender. */
	private String sender;

	/** The image if transferred */
	private ParseFile file;
	
	/** IsImage boolean*/
	private boolean isImage;
	
	/** isMap boolean*/
	private boolean isMap;

	/**
	 * Instantiates a new conversation.
	 * 
	 * @param msg
	 *            the msg
	 * @param date
	 *            the date
	 * @param sender
	 *            the sender
	 */
	public Conversation(String msg, Date date, String sender) {
		this.msg = msg;
		this.date = date;
		this.sender = sender;
	}

	/**
	 * Instantiates a new conversation.
	 */
	public Conversation() {
	}

	/**
	 * Gets the msg.
	 * 
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * Sets the msg.
	 * 
	 * @param msg
	 *            the new msg
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

	/**
	 * Sets the parse image file
	 * 
	 * @param file
	 */
	public void setImage(ParseFile file) {
		this.file = file;
	}

	/**
	 * Gets the parseImage
	 * 
	 * @return the parseImage
	 */
	public ParseFile getImage() {
		return file;
	}

	public void setIsImage(boolean isImage){
		this.isImage = isImage;
	}
	
	public boolean getIsImage(){
		return isImage;
	}
	
	public void setIsMap(boolean isMap){
		this.isMap = isMap;
	}
	
	public boolean getIsMap(){
		return isMap;
	}
	
	/**
	 * Checks if is sent.
	 * 
	 * @return true, if is sent
	 */
	public boolean isSent() {
		return UserList.user.getUsername().equals(sender);
	}

	/**
	 * Gets the date.
	 * 
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Sets the date.
	 * 
	 * @param date
	 *            the new date
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Gets the sender.
	 * 
	 * @return the sender
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * Sets the sender.
	 * 
	 * @param sender
	 *            the new sender
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}

	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 * 
	 * @param status
	 *            the new status
	 */
	public void setStatus(int status) {
		this.status = status;
	}

}
