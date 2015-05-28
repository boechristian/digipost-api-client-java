/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.api.client.representations;

import no.digipost.api.client.representations.xml.DateTimeXmlAdapter;
import org.joda.time.DateTime;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "event")
public class DocumentEvent {
	@XmlAttribute(name = "uuid", required = true)
	private String uuid;
	@XmlAttribute(name = "type", required = true)
	private DocumentEventType type;
	@XmlAttribute(name = "created", required = true)
	@XmlJavaTypeAdapter(DateTimeXmlAdapter.class)
	@XmlSchemaType(name = "dateTime")
	private DateTime created;
	@XmlElement(name = "metadata")
	private EventMetadata metadata;

	public DocumentEvent() {
	}

	public DocumentEvent(String uuid, DocumentEventType type, DateTime created) {
		this(uuid, type, created, null);
	}

	public DocumentEvent(String uuid, DocumentEventType type, DateTime created, EventMetadata metadata) {
		this.uuid = uuid;
		this.type = type;
		this.created = created;
		this.metadata = metadata;
	}

	public String getUuid() {
		return uuid;
	}

	public DocumentEventType getType() {
		return type;
	}

	public DateTime getCreated() {
		return created;
	}

	public EventMetadata getMetadata() {
		return metadata;
	}
}