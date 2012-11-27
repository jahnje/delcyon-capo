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
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DHParameterSpec;
import javax.xml.bind.DatatypeConverter;

import org.w3c.dom.Element;

import com.delcyon.capo.CapoApplication;
import com.delcyon.capo.protocol.client.CapoConnection;
import com.delcyon.capo.protocol.client.XMLRequest;
import com.delcyon.capo.xml.XPath;

/**
 * @author jeremiah
 *
 */
public class CertificateRequest extends XMLRequest
{

	@SuppressWarnings("unchecked")
	private HashMap<Enum, String> parameterHashMap = new HashMap<Enum, String>();
	
	public enum CertificateRequestType
	{
		DH
	}
	public enum Attributes
	{
		TYPE,
		SERVER_PUBLIC_KEY,
		CLIENT_PUBLIC_KEY,
		CLIENT_ID,
		SERVER_ID,
		DH_GENERATOR,
		DH_PRIME,
		DH_LENGTH,
		PHASE,
		PAYLOAD
	}
	
	private CertificateRequestType certificateRequestType = null;
	private CapoConnection capoConnection;
	private KeyAgreement keyAgreement1;
	private SecretKey secretKey1;
	private String payloadString;
	
	
	
	
	public CertificateRequest(CapoConnection capoConnection) throws Exception
	{
		super();
		this.capoConnection = capoConnection;
	}

	@Override
	public void init() throws Exception
	{
		setInputStream(capoConnection.getInputStream());
		setOutputStream(capoConnection.getOutputStream());
		super.init();
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Element getChildRootElement() throws Exception
	{
		Element updaterRequestElement =  CapoApplication.getDefaultDocument("certificate_request.xml").getDocumentElement();
		updaterRequestElement.setAttribute(Attributes.TYPE.toString(), certificateRequestType.toString());
		Set<Entry<Enum, String>> parameterSet = parameterHashMap.entrySet();
		for (Entry<Enum, String> entry : parameterSet)
		{
			updaterRequestElement.setAttribute(entry.getKey().toString(), entry.getValue());
		}
		return updaterRequestElement;
	}
	
	public CertificateRequestType getCertificateRequestType()
	{
		return certificateRequestType;
	}
	
	public void setCertificateRequestType(CertificateRequestType certificateRequestType)
	{
		this.certificateRequestType = certificateRequestType;
	}

	@SuppressWarnings("unchecked")
	public void setParameter(Enum parameterName,String parameterValue)
	{
		parameterHashMap.put(parameterName, parameterValue);
	}
	
	@SuppressWarnings("unchecked")
	public String getParameter(Enum parameterName)
	{
		return parameterHashMap.get(parameterName);
	}
	
	public void clearParameters()
	{
		parameterHashMap.clear();
	}
	
	public void loadDHPhase1() throws Exception
	{
		//create dh stuff
        //generate key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
        keyPairGenerator.initialize(1024);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        //get public key
        byte[] encodedPublicKey1 = keyPair.getPublic().getEncoded();
        
        keyAgreement1 = KeyAgreement.getInstance("DH");
        keyAgreement1.init(keyPair.getPrivate());
        
        //get the key speces for remote
        DHParameterSpec dhParameterSpec = ((DHPublicKey) keyPair.getPublic()).getParams();
        BigInteger dhParameterSpecGenerator = dhParameterSpec.getG();
        BigInteger dhParameterSpecPrime = dhParameterSpec.getP();
        int dhParameterSpecLength = dhParameterSpec.getL();
        
        
        setParameter(Attributes.CLIENT_PUBLIC_KEY, DatatypeConverter.printBase64Binary(encodedPublicKey1));
        setParameter(Attributes.DH_GENERATOR, dhParameterSpecGenerator.toString(16));
        setParameter(Attributes.DH_PRIME, dhParameterSpecPrime.toString(16));
        setParameter(Attributes.DH_LENGTH, dhParameterSpecLength+"");
	}

	public void parseResponse() throws Exception
	{
		Element responseElement = readResponse().getDocumentElement(); 
		Element certificateRequestElement = (Element) XPath.selectSingleNode(responseElement, "//CertificateRequestResponse");
		String publicKeyString = XPath.selectSingleNodeValue(certificateRequestElement, "//CertificateRequestResponse/@"+CertificateRequest.Attributes.SERVER_PUBLIC_KEY);
		payloadString = XPath.selectSingleNodeValue(certificateRequestElement, "//CertificateRequestResponse/@"+CertificateRequest.Attributes.PAYLOAD);
		String serverID = XPath.selectSingleNodeValue(certificateRequestElement, "//CertificateRequestResponse/@"+CertificateRequest.Attributes.SERVER_ID);
		setParameter(Attributes.SERVER_ID, serverID);
		String clientID = XPath.selectSingleNodeValue(certificateRequestElement, "//CertificateRequestResponse/@"+CertificateRequest.Attributes.CLIENT_ID);
		setParameter(Attributes.CLIENT_ID, clientID);
		KeyFactory keyFactory1 = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509Spec = new X509EncodedKeySpec(DatatypeConverter.parseBase64Binary(publicKeyString));
        PublicKey publicKey2 = keyFactory1.generatePublic(x509Spec);
        
        //finish up key agreement with remote public key
        keyAgreement1.doPhase(publicKey2,true);
        
        //get local secret
        byte secret1[] = keyAgreement1.generateSecret();

        //create secret key to encrypt things with
        SecretKeyFactory secretKeyFactory1 = SecretKeyFactory.getInstance("DES");
        DESKeySpec desKeySpec1 = new DESKeySpec(secret1);
        secretKey1 = secretKeyFactory1.generateSecret(desKeySpec1);
        
       
        
	}

	public byte[] getDecryptedPayload() throws Exception
	{
		Cipher cipher1 = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher1.init(Cipher.DECRYPT_MODE, secretKey1);	        
		return cipher1.doFinal(DatatypeConverter.parseBase64Binary(payloadString));
	}

	public void setPayload(String decryptedPayload) throws Exception
	{
		Cipher cipher1 = Cipher.getInstance("DES/ECB/PKCS5Padding");
		cipher1.init(Cipher.ENCRYPT_MODE, secretKey1);
		byte[] encryptedPayload = cipher1.doFinal(decryptedPayload.getBytes());
		setParameter(Attributes.PAYLOAD, DatatypeConverter.printBase64Binary(encryptedPayload));
	}

	@SuppressWarnings("unchecked")
	public void resend() throws Exception
	{
		getImportedChildRootElement().setAttribute(Attributes.TYPE.toString(), certificateRequestType.toString());
		Set<Entry<Enum, String>> parameterSet = parameterHashMap.entrySet();
		for (Entry<Enum, String> entry : parameterSet)
		{
			getImportedChildRootElement().setAttribute(entry.getKey().toString(), entry.getValue());
		}
		send();
	}
	
	
}
