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

import no.digipost.api.client.delivery.MessageDeliveryApi;
import no.digipost.api.client.delivery.OngoingDelivery;
import no.digipost.api.client.document.DocumentApi;
import no.digipost.api.client.errorhandling.DigipostClientException;
import no.digipost.api.client.errorhandling.ErrorCode;
import no.digipost.api.client.inbox.InboxApi;
import no.digipost.api.client.internal.ApiServiceImpl;
import no.digipost.api.client.internal.delivery.MessageDeliverer;
import no.digipost.api.client.representations.AddDataLink;
import no.digipost.api.client.representations.AdditionalData;
import no.digipost.api.client.representations.Autocomplete;
import no.digipost.api.client.representations.DocumentEvents;
import no.digipost.api.client.representations.DocumentStatus;
import no.digipost.api.client.representations.Identification;
import no.digipost.api.client.representations.IdentificationResult;
import no.digipost.api.client.representations.Link;
import no.digipost.api.client.representations.Message;
import no.digipost.api.client.representations.Recipients;
import no.digipost.api.client.representations.accounts.UserAccount;
import no.digipost.api.client.representations.accounts.UserInformation;
import no.digipost.api.client.representations.inbox.Inbox;
import no.digipost.api.client.representations.inbox.InboxDocument;
import no.digipost.api.client.representations.sender.SenderInformation;
import no.digipost.api.client.security.CryptoUtil;
import no.digipost.api.client.security.Signer;
import no.digipost.api.client.util.JAXBContextUtils;
import no.digipost.http.client3.DigipostHttpClientFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.UUID;

import static no.digipost.api.client.internal.http.response.HttpResponseUtils.checkResponse;
import static no.digipost.api.client.util.JAXBContextUtils.jaxbContext;


/**
 * En klient for å sende brev gjennom Digipost. Hvis et objekt av denne klassen
 * er opprettet med et fungerende sertifikat og tilhørende passord, kan man
 * gjøre søk og sende brev gjennom Digipost.
 */
public class DigipostClient {

    static {
        CryptoUtil.addBouncyCastleProviderAndVerify_AES256_CBC_Support();
    }

    private static final Logger LOG = LoggerFactory.getLogger(DigipostClient.class);

    private final EventLogger eventLogger;
    private final MessageDeliveryApi messageApi;
    private final MessageDeliverer messageSender;
    private final InboxApi inboxApiService;
    private final DocumentApi documentApi;



    public DigipostClient(DigipostClientConfig config, BrokerId brokerId, Signer signer) {
        this(config, brokerId, signer, DigipostHttpClientFactory.createDefaultBuilder());
    }

    public DigipostClient(DigipostClientConfig config, BrokerId brokerId, Signer signer, HttpClientBuilder clientBuilder) {
        this(config, new ApiServiceImpl(config, clientBuilder, brokerId, signer));
    }

    private DigipostClient(DigipostClientConfig config, ApiServiceImpl apiService) {
        this(config, apiService, apiService, apiService);
    }

    public DigipostClient(DigipostClientConfig config, MessageDeliveryApi apiService, InboxApi inboxApiService, DocumentApi documentApi) {
        this.messageApi = apiService;
        this.inboxApiService = inboxApiService;
        this.documentApi = documentApi;

        this.messageSender = new MessageDeliverer(config, apiService);

        this.eventLogger = config.eventLogger.withDebugLogTo(LOG);
    }


    /**
     * Oppretter en forsendelse for sending gjennom Digipost. Dersom mottaker ikke er
     * digipostbruker og det ligger printdetaljer på forsendelsen bestiller vi
     * print av brevet til vanlig postgang. (Krever at avsender har fått tilgang
     * til print.)
     */
    public OngoingDelivery.WithPrintFallback createMessage(final Message message) {
        return messageSender.createMessage(message);
    }

    /**
     * Opprette forsendelse som skal gå direkte til print og videre til utsending
     * gjennom vanlig postgang. Krever at avsender har tilgang til å sende direkte
     * til print.
     */
    public OngoingDelivery.ForPrintOnly createPrintOnlyMessage(final Message printMessage) {
        return messageSender.createPrintOnlyMessage(printMessage);
    }

    public IdentificationResult identifyRecipient(final Identification identification) {
        try (CloseableHttpResponse response = messageApi.identifyRecipient(identification)) {
            checkResponse(response, eventLogger);
            return JAXBContextUtils.unmarshal(jaxbContext, response.getEntity().getContent(), IdentificationResult.class);
        } catch (IOException e) {
            throw new DigipostClientException(ErrorCode.GENERAL_ERROR, e);
        }
    }

    public void addData(AddDataLink addDataLink, AdditionalData data) {
        messageSender.addData(addDataLink, data);
    }

    public Recipients search(final String searchString) {
        return messageApi.search(searchString);
    }

    public Autocomplete getAutocompleteSuggestions(final String searchString) {
        return messageApi.searchSuggest(searchString);
    }

    public DocumentEvents getDocumentEvents(ZonedDateTime from, ZonedDateTime to, int offset, int maxResults) {
        return getDocumentEvents(null, null, from, to, offset, maxResults);
    }

    public DocumentEvents getDocumentEvents(String organisation, String partId, ZonedDateTime from, ZonedDateTime to, int offset, int maxResults) {
        return documentApi.getDocumentEvents(organisation, partId, from, to, offset, maxResults);
    }

    /**
     * Hent informasjon om en gitt avsender. Kan enten be om informasjon om
     * "deg selv", eller en avsender du har fullmakt til å sende post for.
     *
     * @param senderId avsender-IDen til avsenderen du vil ha informasjon om.
     *
     * @return informasjon om avsenderen, dersom den finnes, og du har tilgang
     *         til å informasjon om avsenderen. Det er ikke mulig å skille på
     *         om avsenderen ikke finnes, eller man ikke har tilgang til
     *         informasjonen.
     *
     * @see SenderInformation
     */
    public SenderInformation getSenderInformation(SenderId senderId) {
        return messageApi.getSenderInformation(senderId);
    }

    /**
     * Hent informasjon om en gitt avsender. Kan enten be om informasjon om
     * "deg selv", eller en avsender du har fullmakt til å sende post for.
     *
     * @param orgnr organisasjonsnummeret til avsenderen (påkrevet).
     * @param avsenderenhet underenhet for gitt <code>orgnr</code>, dersom
     *                      aktuelt, eller <code>null</code>.
     *
     *
     * @return informasjon om avsenderen, dersom den finnes, og du har tilgang
     *         til å informasjon om avsenderen. Det er ikke mulig å skille på
     *         om avsenderen ikke finnes, eller man ikke har tilgang til
     *         informasjonen.
     *
     * @see SenderInformation
     */
    public SenderInformation getSenderInformation(String orgnr, String avsenderenhet) {
        return messageApi.getSenderInformation(orgnr, avsenderenhet);
    }

    public DocumentStatus getDocumentStatus(Link linkToDocumentStatus) {
        return documentApi.getDocumentStatus(linkToDocumentStatus);
    }

    public DocumentStatus getDocumentStatus(SenderId senderId, UUID uuid) {
        return documentApi.getDocumentStatus(senderId, uuid);
    }

    public InputStream getContent(String path) {
        return documentApi.getDocumentContent(path);
    }

    /**
     * Get the first 100 documents in the inbox for the organisation represented by senderId.
     *
     * @param senderId Either an organisation that you operate on behalf of or your brokerId
     * @return Inbox element with the 100 first documents
     */
    public Inbox getInbox(SenderId senderId) {
        return getInbox(senderId, 0, 100);
    }

    /**
     * Get documents from the inbox for the organisation represented by senderId.
     *
     * @param senderId Either an organisation that you operate on behalf of or your brokerId
     * @param offset Number of documents to skip. For pagination
     * @param limit Maximum number of documents to retrieve (max 1000)
     * @return Inbox element with the n=limit first documents
     */
    public Inbox getInbox(SenderId senderId, int offset, int limit) {
        return inboxApiService.getInbox(senderId, offset, limit);
    }

    /**
     * Get the content of a document as a stream. The content is streamed from the server so remember to
     * close the stream to prevent connection leaks.
     *
     * @param inboxDocument The document to get content for
     * @return Entire content of the document as a stream
     */
    public InputStream getInboxDocumentContent(InboxDocument inboxDocument) {
        return inboxApiService.getInboxDocumentContentStream(inboxDocument);
    }

    /**
     * Delets the given document from the server
     *
     * @param inboxDocument The document to delete
     */
    public void deleteInboxDocument(InboxDocument inboxDocument) {
        inboxApiService.deleteInboxDocument(inboxDocument);
    }

    public UserAccount createOrActivateUserAccount(SenderId senderId, UserInformation user) {
        return messageApi.createOrActivateUserAccount(senderId, user);
    }

}
