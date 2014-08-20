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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sms-notification-failed-metadata")
public class SmsNotificationFailedMetadata extends EventMetadata {

	@XmlAttribute(name = "mobile-number")
	public final String mobileNumber;
	@XmlAttribute(name = "error-code")
	public final String errorCode;

	public SmsNotificationFailedMetadata() {
		this(null, null);
	}

	public SmsNotificationFailedMetadata(String mobileNumber, String errorCode) {
		this.mobileNumber = mobileNumber;
		this.errorCode = errorCode;
	}
}
