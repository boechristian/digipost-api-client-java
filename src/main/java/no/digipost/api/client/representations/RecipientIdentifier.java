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

import javax.xml.bind.annotation.XmlValue;
import java.util.Objects;

public abstract class RecipientIdentifier {
    @XmlValue
    protected final String identifier;

    public RecipientIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public String asString() {
        return identifier;
    }

    public abstract boolean isPersonalIdentificationNumber();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RecipientIdentifier) {
            RecipientIdentifier that = (RecipientIdentifier) obj;
            return Objects.equals(this.identifier, that.identifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
