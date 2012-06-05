/**
Copyright (C) 2012  Delcyon, Inc.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.delcyon.capo.crypto;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DHParameterSpec;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.Configuration;
import com.delcyon.capo.Configuration.PREFERENCE;
import com.delcyon.capo.controller.server.ControllerProcessingException;
import com.delcyon.capo.protocol.server.ClientRequest;
import com.delcyon.capo.protocol.server.ClientRequestProcessor;
import com.delcyon.capo.protocol.server.ClientRequestProcessorProvider;
import com.delcyon.capo.protocol.server.ClientRequestXMLProcessor;
import com.delcyon.capo.server.CapoServer;
import com.delcyon.capo.server.CapoServer.Preferences;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
@ClientRequestProcessorProvider(name="CertificateRequest")
public class CertificateRequestProcessor implements ClientRequestProcessor
{

	private static final String BC = org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;
	
	private String sessionID = null;
	
	@Override
	public String getSessionId()
	{
		return sessionID;
	}
	@Override
	public void init(ClientRequestXMLProcessor clientRequestXMLProcessor, String sessionID, HashMap<String, String> sessionHashMap,String requestName) throws Exception
	{ 
		this.sessionID = sessionID;
	}

	@Override
	public void process(ClientRequest clientRequest) throws Exception
	{
		

		String type = XPath.selectSingleNodeValue(clientRequest.getRequestDocument().getDocumentElement(), "//CertificateRequest/@"+CertificateRequest.Attributes.TYPE);
		
		if (type == null || type.trim().isEmpty())
		{
			throw new ControllerProcessingException("CertificateRequest missing type attribute", clientRequest.getRequestDocument());
		}
		else
		{

			Element certificateRequestElement = (Element) XPath.selectSingleNode(clientRequest.getRequestDocument().getDocumentElement(), "//CertificateRequest");
			String publicKeyString = XPath.selectSingleNodeValue(clientRequest.getRequestDocument().getDocumentElement(), "//CertificateRequest/@"+CertificateRequest.Attributes.CLIENT_PUBLIC_KEY);
			String dhGeneratorString = XPath.selectSingleNodeValue(clientRequest.getRequestDocument().getDocumentElement(), "//CertificateRequest/@"+CertificateRequest.Attributes.DH_GENERATOR);
			String dhLengthString = XPath.selectSingleNodeValue(clientRequest.getRequestDocument().getDocumentElement(), "//CertificateRequest/@"+CertificateRequest.Attributes.DH_LENGTH);
			String dhPrimeString = XPath.selectSingleNodeValue(clientRequest.getRequestDocument().getDocumentElement(), "//CertificateRequest/@"+CertificateRequest.Attributes.DH_PRIME);
			String clientID = XPath.selectSingleNodeValue(clientRequest.getRequestDocument().getDocumentElement(), "//CertificateRequest/@"+CertificateRequest.Attributes.CLIENT_ID);
			
			//process clientID
			//check to see if client ID is a valid client id, if not, we need to create a new one
			if (clientID == null || clientID.matches("capo\\.client\\.0") || clientID.matches("capo\\.client\\.\\d+") == false)
			{
				clientID = "capo.client."+CapoApplication.getDataManager().nextValue("client_id_sequence")+"";
			}
			
			//create parameter specs 
			BigInteger dhParameterSpecGenerator = new BigInteger(dhGeneratorString, 16);
			BigInteger dhParameterSpecPrime = new BigInteger(dhPrimeString, 16);
			int dhParameterSpecLength = Integer.parseInt(dhLengthString);

			//on remote generate a key pair using original specs
			KeyPairGenerator keyPairGenerator2 = KeyPairGenerator.getInstance("DH");
			DHParameterSpec dhParameterSpec2 = new DHParameterSpec(dhParameterSpecPrime, dhParameterSpecGenerator, dhParameterSpecLength);
			keyPairGenerator2.initialize(dhParameterSpec2);

			//generate remote key pair
			KeyPair keyPair2 = keyPairGenerator2.generateKeyPair();
			byte[] encodedPublicKey2 = keyPair2.getPublic().getEncoded();

			//start remote key agreement
			KeyAgreement keyAgreement2 = KeyAgreement.getInstance("DH");
			keyAgreement2.init(keyPair2.getPrivate());

			//read in keyspec
			KeyFactory keyFactory2 = KeyFactory.getInstance("DH");
			X509EncodedKeySpec x509Spec2 = new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(publicKeyString));

			//load keyspec, and finish key agreement
			PublicKey publicKey1 = keyFactory2.generatePublic(x509Spec2);
			keyAgreement2.doPhase(publicKey1, true);

			//get remote secret key
			byte secret2[] = keyAgreement2.generateSecret();


			//use our secret to generate our remote secret key
			SecretKeyFactory secretKeyFactory2 = SecretKeyFactory.getInstance("DES");
			DESKeySpec desKeySpec2 = new DESKeySpec(secret2);
			SecretKey secretKey2 = secretKeyFactory2.generateSecret(desKeySpec2);

			//encrypt message
			Cipher cipher2 = Cipher.getInstance("DES/ECB/PKCS5Padding");
			cipher2.init(Cipher.ENCRYPT_MODE, secretKey2);
			byte encryptedMessage[] = cipher2.doFinal(CapoApplication.getCeritifcate());

			//populate attributes
			certificateRequestElement.setAttribute(CertificateRequest.Attributes.SERVER_PUBLIC_KEY.toString(), DatatypeConverter.printBase64Binary(encodedPublicKey2));
			certificateRequestElement.setAttribute(CertificateRequest.Attributes.PAYLOAD.toString(), DatatypeConverter.printBase64Binary(encryptedMessage));
			certificateRequestElement.setAttribute(CertificateRequest.Attributes.SERVER_ID.toString(), CapoApplication.getConfiguration().getValue(CapoServer.Preferences.SERVER_ID));
			certificateRequestElement.setAttribute(CertificateRequest.Attributes.CLIENT_ID.toString(), clientID);
			
			String oneTimePassword = CapoApplication.getConfiguration().getValue(PREFERENCE.CLIENT_VERIFICATION_PASSWORD);
			if (oneTimePassword.isEmpty())
			{
				oneTimePassword = (new Random().nextInt(Integer.MAX_VALUE))+"";
				CapoApplication.logger.log(Level.INFO, "One time client verification password = '"+oneTimePassword+"'");
			}
			
			clientRequest.getXmlStreamProcessor().writeDocument(clientRequest.getRequestDocument());
			Document reuquestDocument = clientRequest.getXmlStreamProcessor().readNextDocument();
			
			byte[] encryptedPayload = DatatypeConverter.parseBase64Binary(XPath.selectSingleNodeValue(reuquestDocument.getDocumentElement(), "//CertificateRequest/@"+CertificateRequest.Attributes.PAYLOAD));

			//decrypt message

			cipher2.init(Cipher.DECRYPT_MODE, secretKey2);
			String returnedPassword  = new String(cipher2.doFinal(encryptedPayload));
			if (oneTimePassword.equals(returnedPassword))
			{
				//create certificate from public key
				byte[] clientPublicKeyBytes = DatatypeConverter.parseBase64Binary(XPath.selectSingleNodeValue(reuquestDocument.getDocumentElement(), "//CertificateRequest/@"+CertificateRequest.Attributes.CLIENT_PUBLIC_KEY));
				KeyFactory keyFactory3 = KeyFactory.getInstance("RSA");
				X509EncodedKeySpec x509Spec3 = new X509EncodedKeySpec(clientPublicKeyBytes);

				//load keyspec, and finish key agreement
				PublicKey clientPublicKey = keyFactory3.generatePublic(x509Spec3);

				X500NameBuilder x500NameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
				String clientAlias = clientID+".cert"; 
				x500NameBuilder.addRDN(BCStyle.CN,clientAlias); 

				String serverAlias = CapoApplication.getConfiguration().getValue(Preferences.SERVER_ID)+".private";

				RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) CapoApplication.getKeyStore().getKey(serverAlias, CapoApplication.getConfiguration().getValue(Configuration.PREFERENCE.KEYSTORE_PASSWORD).toCharArray());

				ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(BC).build(rsaPrivateKey);

				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MONTH, CapoApplication.getConfiguration().getIntValue(Preferences.KEY_MONTHS_VALID));

				X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(x500NameBuilder.build(), BigInteger.valueOf(System.currentTimeMillis()), new Date(System.currentTimeMillis() - 50000),calendar.getTime(),x500NameBuilder.build(), clientPublicKey);

				X509Certificate certificate = new JcaX509CertificateConverter().setProvider(BC).getCertificate(certificateBuilder.build(contentSigner));
				CapoApplication.getKeyStore().setCertificateEntry(clientAlias, certificate);
				((CapoServer)CapoApplication.getApplication()).writeKeyStore(CapoApplication.getKeyStore());
				Document responseDocument = CapoApplication.getDefaultDocument("default_response.xml");
				responseDocument.getDocumentElement().setAttribute("result", "SUCCESS");
				clientRequest.getXmlStreamProcessor().writeDocument(responseDocument);
			}
			else
			{
				Document responseDocument = CapoApplication.getDefaultDocument("default_response.xml");
				responseDocument.getDocumentElement().setAttribute("result", "WRONG_PASSWORD");
				clientRequest.getXmlStreamProcessor().writeDocument(responseDocument);
			}
				
		}
	}

	@Override
	public Document readNextDocument() throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
