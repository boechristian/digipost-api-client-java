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
package no.digipost.api.client.security;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.RSAPrivateCrtKey;

import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class CryptoUtil {
	public static PrivateKey loadKeyFromP12(final InputStream certificateStream, final String passord) {
		RSAPrivateCrtKey key;
		try {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(certificateStream, passord.toCharArray());
			String onlyKeyAlias = keyStore.aliases().nextElement();
			key = (RSAPrivateCrtKey) keyStore.getKey(onlyKeyAlias, passord.toCharArray());
		} catch (Exception e) {
			throw new RuntimeException("Det skjedde en feil ved lasting av nøkkelen", e);
		}
		if (key == null) {
			throw new RuntimeException("Nøkkelen som ble lastet, var null");
		}
		return key;
	}

	public static byte[] sign(final PrivateKey privateKey, final String messageToSign) {
		Signature instance;
		try {
			instance = Signature.getInstance("SHA256WithRSAEncryption");
			instance.initSign(privateKey);
			instance.update(messageToSign.getBytes());
			return instance.sign();
		} catch (Exception e) {
			throw new RuntimeException("Det skjedde en feil ved signeringen", e);
		}
	}

	public static void verifyJCE() {
		try {
			if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
				Security.addProvider(new BouncyCastleProvider());
			}
			new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC).setProvider(BouncyCastleProvider.PROVIDER_NAME).build();
		} catch (CMSException e) {
			throw new RuntimeException("Feil under initialisering av algoritmer. Er Java Cryptographic Excetsions (JCE) installert?", e);
		}
	}

}