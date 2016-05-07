/*******************************************************************************
 * Copyright (c) 2016, Erik Österlund
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ERIK ÖSTERLUND BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
