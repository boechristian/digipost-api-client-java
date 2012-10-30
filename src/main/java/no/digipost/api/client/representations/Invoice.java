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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;

import no.digipost.api.client.representations.xml.DateXmlAdapter;
import org.joda.time.LocalDate;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "invoice", propOrder = {
    "kid",
    "amount",
    "account",
    "dueDate"
})
@XmlRootElement(name = "invoice")
public class Invoice
    extends Message
{

    @XmlElement(required = true)
    protected String kid;
    @XmlElement(required = true)
    protected BigDecimal amount;
    @XmlElement(required = true)
    protected String account;
    @XmlElement(name = "due-date", required = true, type = String.class)
    @XmlJavaTypeAdapter(DateXmlAdapter.class)
    @XmlSchemaType(name = "date")
    protected LocalDate dueDate;

	public Invoice() {
	}

	public Invoice(final String messageId, final String subject, final PersonalIdentificationNumber id, final SmsNotification smsVarsling, final AuthenticationLevel authenticationLevel, final SensitivityLevel sensitivityLevel, final String kid, final BigDecimal amount, final String account, final LocalDate dueDate, final Link... links) {
		super(messageId, subject, id, smsVarsling, authenticationLevel, sensitivityLevel, links);
		this.kid = kid;
		this.amount = amount;
		this.account = account;
		this.dueDate = dueDate;
	}

	public Invoice(final String messageId, final String subject, final DigipostAddress digipostAdress, final SmsNotification smsVarsling, final AuthenticationLevel authenticationLevel, final SensitivityLevel sensitivityLevel, final String kid, final BigDecimal amount, final String account, final LocalDate dueDate, final Link... links) {
		super(messageId, subject, digipostAdress, smsVarsling, authenticationLevel, sensitivityLevel, links);
		this.kid = kid;
		this.amount = amount;
		this.account = account;
		this.dueDate = dueDate;
	}

	/**
     * Gets the value of the kid property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getKid() {
        return kid;
    }

    /**
     * Sets the value of the kid property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setKid(String value) {
        this.kid = value;
    }

    /**
     * Gets the value of the amount property.
     *
     * @return
     *     possible object is
     *     {@link java.math.BigDecimal }
     *
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Sets the value of the amount property.
     *
     * @param value
     *     allowed object is
     *     {@link java.math.BigDecimal }
     *     
     */
    public void setAmount(BigDecimal value) {
        this.amount = value;
    }

    /**
     * Gets the value of the account property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccount() {
        return account;
    }

    /**
     * Sets the value of the account property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccount(String value) {
        this.account = value;
    }

    /**
     * Gets the value of the dueDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Sets the value of the dueDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDueDate(LocalDate value) {
        this.dueDate = value;
    }

}
