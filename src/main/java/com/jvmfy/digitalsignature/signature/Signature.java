package com.jvmfy.digitalsignature.signature;


import org.apache.logging.log4j.util.Strings;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TSPException;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Optional;

public class Signature implements SignatureInterface {

    private PrivateKey privateKey;
    private Certificate[] certificateChain;
    private String tsaUrl;

    Signature(KeyStore keyStore, char[] keyStorePassword, String appCertificateAlias, String tsaUrl) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateNotYetValidException, CertificateExpiredException {

        this.certificateChain = Optional.ofNullable(keyStore.getCertificateChain(appCertificateAlias))
                .orElseThrow(() -> (new IOException("Could not find a proper certificate chain")));

        this.privateKey = (PrivateKey) keyStore.getKey(appCertificateAlias, keyStorePassword);

        Certificate certificate = this.certificateChain[0];

        if (certificate instanceof X509Certificate) {
            ((X509Certificate) certificate).checkValidity();
        }

        this.tsaUrl = tsaUrl;
    }

    @Override
    public byte[] sign(InputStream content) throws IOException {
        try {
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            X509Certificate cert = (X509Certificate) this.certificateChain[0];
            ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256WithRSA").build(this.privateKey);
            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build()).build(sha1Signer, cert));
            gen.addCertificates(new JcaCertStore(Arrays.asList(this.certificateChain)));
            CMSProcessableInputStream msg = new CMSProcessableInputStream(content);
            CMSSignedData signedData = gen.generate(msg, false);

            //add timestamp if TSA is available
            if (Strings.isNotBlank(this.tsaUrl)) {
                TimeStampManager timeStampManager = new TimeStampManager(this.tsaUrl);
                signedData = timeStampManager.addSignedTimeStamp(signedData);
            }

            return signedData.getEncoded();
        } catch (GeneralSecurityException | CMSException | OperatorCreationException | TSPException e) {
            //throw new IOException cause a SignatureInterface, but keep the stacktrace
            throw new IOException(e);
        }
    }

}
