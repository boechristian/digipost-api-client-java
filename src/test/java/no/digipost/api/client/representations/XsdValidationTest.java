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


import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static no.digipost.api.client.representations.AuthenticationLevel.IDPORTEN_3;
import static no.digipost.api.client.representations.AuthenticationLevel.PASSWORD;
import static no.digipost.api.client.representations.AuthenticationLevel.TWO_FACTOR;
import static no.digipost.api.client.representations.Channel.DIGIPOST;
import static no.digipost.api.client.representations.DocumentEventType.EMAIL_NOTIFICATION_FAILED;
import static no.digipost.api.client.representations.DocumentEventType.MOVE_FILES_FROM_PUBLIC_SECTOR;
import static no.digipost.api.client.representations.DocumentEventType.OPENED;
import static no.digipost.api.client.representations.DocumentEventType.POSTMARKED;
import static no.digipost.api.client.representations.DocumentEventType.PRINT_FAILED;
import static no.digipost.api.client.representations.DocumentEventType.SHREDDED;
import static no.digipost.api.client.representations.DocumentEventType.SMS_NOTIFICATION_FAILED;
import static no.digipost.api.client.representations.ErrorType.CLIENT_DATA;
import static no.digipost.api.client.representations.FileType.PDF;
import static no.digipost.api.client.representations.Message.MessageBuilder.newMessage;
import static no.digipost.api.client.representations.MessageStatus.DELIVERED;
import static no.digipost.api.client.representations.SensitivityLevel.NORMAL;
import static no.digipost.api.client.representations.XmlTestHelper.marshallValidateAndUnmarshall;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

public class XsdValidationTest {


    private Link link = new Link(Relation.SELF, new DigipostUri("http://localhost/self"), MediaTypes.DIGIPOST_MEDIA_TYPE_V7);


    @Test
    public void validateRecipients() {
        Address address = new Address("Streetn", "houseNumber", "houseLetter", "additionalAddressLine", "zipCode", "city");
        ArrayList<Address> addresses = new ArrayList<Address>();
        addresses.add(address);
        Recipients recipients = new Recipients();
        Recipient recipient = new Recipient("Even", "Emmil", "Beinlaus", "even.beinlaus#1234", addresses, link);
        recipients.add(recipient);
        marshallValidateAndUnmarshall(recipients);
    }

    @Test
    public void validateErrorMessage() {
        marshallValidateAndUnmarshall(new ErrorMessage(CLIENT_DATA, "Error message", link));
    }

    @Test
    public void validateAutocomplete() {
        List<Suggestion> suggestions = new ArrayList<Suggestion>();
        suggestions.add(new Suggestion("even", link));
        marshallValidateAndUnmarshall(new Autocomplete(suggestions, link));
    }

    @Test
    public void validateEntryPoint() {
        marshallValidateAndUnmarshall(new EntryPoint("dummy certificate-PEM", link, link, link));
    }

    @Test
    public void validateMessage() {
        Message messageWithDigipostAddress = newMessage(randomUUID().toString(),
                        new Document(randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, TWO_FACTOR, NORMAL)
                )
                .digipostAddress(new DigipostAddress("even.beinlaus#1234"))
                .senderOrganization(new SenderOrganization("1337", "R&D"))
                .build();

        Message messageWithPersonalIdentificationNumber = newMessage(randomUUID().toString(),
                        new Document(randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, TWO_FACTOR, NORMAL)
                )
                .personalIdentificationNumber(new PersonalIdentificationNumber("12345678901"))
                .build();

        Document primaryDocumentToPreEncrypt = new Document(randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, TWO_FACTOR, NORMAL);
        Message messageWithPreEncryptAndSenderId = newMessage(randomUUID().toString(), primaryDocumentToPreEncrypt)
                .personalIdentificationNumber(new PersonalIdentificationNumber("12345678901"))
                .senderId(10L)
                .build();

        primaryDocumentToPreEncrypt.encrypt();

        Message messageWithTechnicalAttachment = newMessage(randomUUID().toString(),
                    new Document(randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, TWO_FACTOR, NORMAL))
                .personalIdentificationNumber(new PersonalIdentificationNumber("12345678901"))
                .attachments(Collections.singleton(Document.technicalAttachment(PDF, "tech-type")))
                .build();


        marshallValidateAndUnmarshall(messageWithDigipostAddress);
        marshallValidateAndUnmarshall(messageWithPersonalIdentificationNumber);
        marshallValidateAndUnmarshall(messageWithPreEncryptAndSenderId);
        marshallValidateAndUnmarshall(messageWithTechnicalAttachment);
    }

    @Test
    public void validateMessage_invoicingAccount() {
        Document document = new Document(randomUUID().toString(), "subject", PDF, null, null, null, TWO_FACTOR, NORMAL);
        Message message = newMessage(randomUUID().toString(), document)
                .digipostAddress(new DigipostAddress("even.beinlaus#1234"))
                .invoiceReference("ACCOUNT01")
                .deliveryTime(ZonedDateTime.now())
                .build();
        marshallValidateAndUnmarshall(message);
    }

    @Test
    public void validatePrintMessage() {
        PrintRecipient address = new PrintRecipient("name", new NorwegianAddress("1234", "Oslo"));
        Message message = newMessage(randomUUID().toString(),
                        new Document(randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL)
                )
                .recipient(new MessageRecipient(new PrintDetails(address, address)))
                .build();
        marshallValidateAndUnmarshall(message);
    }

    @Test
    public void validatePrintMessageWithForeignRecipiantWihtCountry() {
        PrintRecipient address = new PrintRecipient("name", new ForeignAddress("adresse", "Sverige", null));
        Message message = newMessage(randomUUID().toString(),
                        new Document(randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL)
                )
                .recipient(new MessageRecipient(new PrintDetails(address, address)))
                .build();
        marshallValidateAndUnmarshall(message);
    }

    @Test
    public void validatePrintMessageWithForeignRecipiantWihtCountryCode() {
        PrintRecipient address = new PrintRecipient("name", new ForeignAddress("adresse", null, "SE"));
        Message message = newMessage(randomUUID().toString(),
                        new Document(randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL)
                )
                .recipient(new MessageRecipient(new PrintDetails(address, address)))
                .build();
        marshallValidateAndUnmarshall(message);
    }

    @Test
    public void validate_print_message_with_print_instructions() {
        PrintRecipient address = new PrintRecipient("name", new NorwegianAddress("1234", "Oslo"));

        PrintDetails printDetails = new PrintDetails(address, address);
        printDetails.setInstruction(Arrays.asList(new PrintInstruction("returkonvolutt", "true")));

        Message message = newMessage(randomUUID().toString(),
                new Document(randomUUID().toString(), "subject", PDF, null, new SmsNotification(), null, PASSWORD, NORMAL)
        )
                .recipient(new MessageRecipient(printDetails))
                .build();
        marshallValidateAndUnmarshall(message);
    }

    @Test
    public void validateDocumentEvents() {
        DocumentEvent openedEvent = new DocumentEvent(randomUUID().toString(), OPENED, ZonedDateTime.now(), ZonedDateTime.now());

        DocumentEvent failedEmailNotificationEvent = new DocumentEvent(randomUUID().toString(), EMAIL_NOTIFICATION_FAILED, ZonedDateTime.now(), ZonedDateTime.now(),
                new EmailNotificationFailedMetadata("emailAddress", "ERROR_CODE"));

        DocumentEvent failedSmsNotificationEvent = new DocumentEvent(randomUUID().toString(), SMS_NOTIFICATION_FAILED, ZonedDateTime.now(), ZonedDateTime.now(),
                new SmsNotificationFailedMetadata("12345678", "ERROR_CODE"));

        DocumentEvent printFailedEvent = new DocumentEvent(randomUUID().toString(), PRINT_FAILED, ZonedDateTime.now(), ZonedDateTime.now(),
                new FailedPrintMetadata("Feil dimensjoner"));

        DocumentEvent postmarkedEvent = new DocumentEvent(randomUUID().toString(), POSTMARKED, ZonedDateTime.now(), ZonedDateTime.now(),
                new PostmarkedMetadata(ZonedDateTime.now()));

        DocumentEvent movedFilesEvent = new DocumentEvent(randomUUID().toString(), MOVE_FILES_FROM_PUBLIC_SECTOR, ZonedDateTime.now(), ZonedDateTime.now(),
                new MoveFilesFromPublicSectorMetadata(true, ZonedDateTime.now().minusDays(3), "Subject", NORMAL, IDPORTEN_3, "fake-cert",
                        "dest-mailbox", "dest-mailbox-address", asList(new DocumentMetadata(UUID.randomUUID().toString(), null)))
        );

        DocumentEvent shreddedEvent = new DocumentEvent(randomUUID().toString(), SHREDDED, ZonedDateTime.now(), ZonedDateTime.now());

        DocumentEvents documentEvents = new DocumentEvents(asList(openedEvent, failedEmailNotificationEvent,
                failedSmsNotificationEvent, printFailedEvent, movedFilesEvent, postmarkedEvent, shreddedEvent));
        marshallValidateAndUnmarshall(documentEvents);
    }

    @Test
    public void validate_document_status_simple() {
        DocumentStatus primaryDoc = new DocumentStatus(randomUUID().toString(), DeliveryStatus.NOT_DELIVERED, ZonedDateTime.now(), null, null, Channel.DIGIPOST, true,
                null, HashAlgorithm.NONE, null, null);
        marshallValidateAndUnmarshall(primaryDoc);
    }

    @Test
    public void validate_document_status_with_attachments() {
        DocumentStatus attachment1 = createDocumentStatus(false);
        DocumentStatus attachment2 = createDocumentStatus(false);
        DocumentStatus primaryDoc = createDocumentStatus(true, attachment1, attachment2);

        marshallValidateAndUnmarshall(primaryDoc);
    }

    private DocumentStatus createDocumentStatus(boolean isPrimaryDocument, DocumentStatus ... attachments) {
        return new DocumentStatus(randomUUID().toString(), DeliveryStatus.DELIVERED, ZonedDateTime.now(), ZonedDateTime.now(), Read.Y, Channel.PRINT, isPrimaryDocument,
                "asdf", HashAlgorithm.SHA256, Arrays.asList(attachments), null);
    }

    @Test
    public void validateMessageDelivery() {
        ZonedDateTime deliveryTime = ZonedDateTime.of(LocalDateTime.of(2017, 1, 30, 12, 45), ZoneId.of("GMT+3"));
        MessageDelivery delivery = new MessageDelivery(randomUUID().toString(), DIGIPOST, DELIVERED, deliveryTime);
        delivery.primaryDocument = new Document(randomUUID().toString(), "primary", FileType.PDF);
        MessageDelivery unmarshalled = marshallValidateAndUnmarshall(delivery);
        assertThat(unmarshalled, not(sameInstance(delivery)));
        assertThat(unmarshalled.primaryDocument.uuid, is(delivery.primaryDocument.uuid));
        assertThat(unmarshalled.primaryDocument.subject, is(delivery.primaryDocument.subject));
        assertThat(unmarshalled.messageId, is(delivery.messageId));
        assertThat(unmarshalled.deliveryMethod, is(delivery.deliveryMethod));
        assertThat(unmarshalled.deliveryTime, is(delivery.deliveryTime));
        assertThat(unmarshalled.status, is(delivery.status));
    }

}
