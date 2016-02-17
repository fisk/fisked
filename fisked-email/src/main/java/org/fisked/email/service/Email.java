package org.fisked.email.service;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Email implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 484428815949063662L;
	private final List<String> _receivers = new ArrayList<>();
	private final List<String> _ccList = new ArrayList<>();
	private String _from;
	private final List<File> _attachments = new ArrayList<>();
	private String _body = "";
	private String _subject = "";

	public void setFrom(String from) {
		_from = from;
	}

	public String getFrom() {
		return _from;
	}

	public void setBody(String body) {
		_body = body;
	}

	public String getBody() {
		return _body;
	}

	public void addReceiver(String receiver) {
		_receivers.add(receiver);
	}

	public void addCc(String cc) {
		_ccList.add(cc);
	}

	public void addAttachment(File file) {
		_attachments.add(file);
	}

	public List<File> getAttachments() {
		return _attachments;
	}

	public List<String> getReceivers() {
		return _receivers;
	}

	public List<String> getCcList() {
		return _ccList;
	}

	public String getSubject() {
		return _subject;
	}

	public void setSubject(String subject) {
		_subject = subject;
	}
}
