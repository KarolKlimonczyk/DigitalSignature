package com.jvmfy.digitalsignature.signature;

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.Attributes;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.tsp.TSPException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for deal with Time Stamps.
 * Time Stamp can be added when Time Stamp Authority URL is available.
 */
class TimeStampManager {
    private TSAClient tsaClient;

    /**
     * @param tsaUrl The url where request for Time Stamp will be done.
     * @throws NoSuchAlgorithmException
     * @throws MalformedURLException
     */
    TimeStampManager(String tsaUrl) throws NoSuchAlgorithmException, MalformedURLException {
        if (tsaUrl != null) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            this.tsaClient = new TSAClient(new URL(tsaUrl), null, null, digest);
        }
    }

    /**
     * Extend cms signed data with TimeStamp first or to all signers
     *
     * @param signedData Generated CMS signed data
     * @return CMSSignedData Extended CMS signed data
     * @throws IOException
     */
    CMSSignedData addSignedTimeStamp(CMSSignedData signedData) throws IOException, TSPException {
        SignerInformationStore signerStore = signedData.getSignerInfos();
        List<SignerInformation> signersWithTimeStamp = new ArrayList<>();

        for (SignerInformation signer : signerStore.getSigners()) {
            // This adds a timestamp to every signer (into his unsigned attributes) in the signature.
            signersWithTimeStamp.add(signTimeStamp(signer));
        }

        // new SignerInformationStore have to be created cause new SignerInformation instance
        // also SignerInformationStore have to be replaced in a signedData
        return CMSSignedData.replaceSigners(signedData, new SignerInformationStore(signersWithTimeStamp));
    }

    /**
     * Extend CMS Signer Information with the TimeStampToken into the unsigned Attributes.
     *
     * @param signer information about signer
     * @return information about SignerInformation
     * @throws IOException
     */
    private SignerInformation signTimeStamp(SignerInformation signer) throws IOException, TSPException {
        AttributeTable unsignedAttributes = signer.getUnsignedAttributes();

        ASN1EncodableVector vector = new ASN1EncodableVector();
        if (unsignedAttributes != null) {
            vector = unsignedAttributes.toASN1EncodableVector();
        }

        byte[] token = this.tsaClient.getTimeStampToken(signer.getSignature());
        ASN1ObjectIdentifier oid = PKCSObjectIdentifiers.id_aa_signatureTimeStampToken;
        ASN1Encodable signatureTimeStamp = new Attribute(oid, new DERSet(ASN1Primitive.fromByteArray(token)));
        vector.add(signatureTimeStamp);
        Attributes signedAttributes = new Attributes(vector);

        // replace unsignedAttributes with the signed once
        return SignerInformation.replaceUnsignedAttributes(signer, new AttributeTable(signedAttributes));
    }
}
