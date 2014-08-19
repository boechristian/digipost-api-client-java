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
package no.digipost.api.client;

import no.digipost.api.client.delivery.DeliveryMethod;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.representations.DocumentEvents;
import no.digipost.api.client.security.Signer;
import org.xml.sax.ContentHandler;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * Instansierer en DigipostClient som ikke går mot faktiskt Digipost REST-api endepunkt og
 * som kan brukes av tester o.l for å framprovosere ulike feilsituasjoner.
 */
public class DigipostClientMock {

	private final DigipostClient client;
	private final ApiServiceMock apiService;

	public DigipostClientMock(DeliveryMethod deliveryMethod) {
		if (deliveryMethod == DeliveryMethod.STEPWISE_REST) {
			throw new RuntimeException("Stepwise REST is not yet supported by " + DigipostClientMock.class.getName());
		}
		apiService = new ApiServiceMock(initMarshaller());
		client = new DigipostClient(deliveryMethod, "digipostmock-url", 1, new Signer() {

			@Override
			public byte[] sign(String dataToSign) {
				return new byte[0];
			}
		}, apiService);
	}

	public DigipostClient getClient() {
		return client;
	}

	public Map<String, ApiServiceMock.DigipostRequest> getAllRequests() {
		return apiService.getAllRequests();
	}

	public ApiServiceMock.DigipostRequest getRequest(String messageId) {
		return apiService.getRequest(messageId);
	}

	public void addExpectedDocumentEvents(DocumentEvents documentEvents) {
		apiService.addExpectedDocumentEvents(documentEvents);
	}

	public void addExpectedContent(byte[] content) {
		apiService.addExpectedContent(content);
	}

	public void reset() {
		apiService.reset();
	}

	/**
	 * Threadsafe instance for marshalling and validating.
	 */
	public static class ValidatingMarshaller {
		private final JAXBContext jaxbContext;
		private final Schema schema;

		public ValidatingMarshaller(JAXBContext jaxbContext, Schema schema) {
			this.jaxbContext = jaxbContext;
			this.schema = schema;
		}

		public void marshal(Object jaxbElement, ContentHandler handler) {
			Marshaller marshaller;
            try {
	            marshaller = jaxbContext.createMarshaller();
	            marshaller.setSchema(schema);
	            marshaller.marshal(jaxbElement, handler);
            } catch (JAXBException e) {
            	StringWriter w = new StringWriter();
    			PrintWriter printWriter = new PrintWriter(w);
    			e.printStackTrace(printWriter);
    			throw new DigipostClientException(ErrorCode.PROBLEM_WITH_REQUEST, "DigipostClientMock failed to marshall the " + jaxbElement.getClass().getSimpleName() + " to xml.\n\n" + w.toString());
            }
        }

	}

	/**
	 * Used to validate that requests are according to XSD
	 */
	private static ValidatingMarshaller initMarshaller() {
		try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(DigipostClient.class.getResource("/xsd/api_v6.xsd"));
			return new ValidatingMarshaller(JAXBContext.newInstance("no.digipost.api.client.representations"), schema);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
