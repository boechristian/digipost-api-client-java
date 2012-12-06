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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;


public class MessageTest {

	@Test
	public void shouldBeDirectPrintWhenMessageContainsOnlyPrintDetails() {
		Message message = new Message("id", new PrintDetails());
		assertTrue(message.isDirectPrint());
	}

	@Test
	public void shouldNotBeDirectPrintWhenMessageContainsDigipostAddress() {
		Message message = new Message("id", "subject", new DigipostAddress("test.testson#1234"), null, AuthenticationLevel.PASSWORD, SensitivityLevel.NORMAL);
		assertFalse(message.isDirectPrint());
	}
	@Test
	public void shouldNotBeDirectPrintWhenMessageContainsNameAndAddress() {
		Message message = new Message("id", "subject", new RecipientIdentification(new NameAndAddress()), null, AuthenticationLevel.PASSWORD,
				SensitivityLevel.NORMAL);
		assertFalse(message.isDirectPrint());
	}
	@Test
	public void shouldNotBeDirectPrintWhenMessageContainsPersonalIdendificationNumber() {
		Message message = new Message("id", "subject", new PersonalIdentificationNumber("12125412435"), null, AuthenticationLevel.PASSWORD,
				SensitivityLevel.NORMAL);
		assertFalse(message.isDirectPrint());
	}
}