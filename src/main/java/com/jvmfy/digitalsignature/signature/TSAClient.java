package com.jvmfy.digitalsignature.signature;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.pdfbox.io.IOUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.tsp.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.SecureRandom;

@Slf4j
class TSAClient {
    private final URL url;
    private final String username;
    private final String password;
    private final MessageDigest digest;

    /**
     * @param url      the URL of the TSA service
     * @param username user name of TSA - pass if the tsaURL need sign in
     * @param password password of TSA - pass if the tsaURL need sign in
     * @param digest   the message digest to use
     */
    TSAClient(URL url, String username, String password, MessageDigest digest) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.digest = digest;
    }

    /**
     * @param messageImprint imprint of message contents
     * @return the encoded time stamp token
     * @throws IOException if there was an error with the connection or data from the TSA server,
     *                     or if the time stamp response could not be validated
     */
    byte[] getTimeStampToken(byte[] messageImprint) throws IOException, TSPException {
        this.digest.reset();
        byte[] hash = this.digest.digest(messageImprint);

        // generate cryptographic nonce
        SecureRandom random = new SecureRandom();
        int nonce = random.nextInt();

        // generate TSA request
        TimeStampRequestGenerator tsaGenerator = new TimeStampRequestGenerator();
        tsaGenerator.setCertReq(true);
        ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha256.getId());
        TimeStampRequest request = tsaGenerator.generate(oid, hash, BigInteger.valueOf(nonce));

        // get TSA response
        byte[] tsaResponse = getTSAResponse(request.getEncoded());

        TimeStampResponse response = new TimeStampResponse(tsaResponse);
        response.validate(request);

        TimeStampToken token = response.getTimeStampToken();
        if (token == null) {
            throw new IOException("Response does not have a time stamp token");
        }

        return token.getEncoded();
    }

    private byte[] getTSAResponse(byte[] request) throws IOException {
        log.debug("Opening connection to TSA server");

        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/timestamp-query");

        log.debug("Established connection to TSA server");

        if (Strings.isNotBlank(this.username) && Strings.isNotBlank(this.password)) {
            connection.setRequestProperty(this.username, this.password);
        }

        // read response
        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            output.write(request);
        } finally {
            IOUtils.closeQuietly(output);
        }

        log.debug("Waiting for response from TSA server");

        InputStream input = null;
        byte[] response;
        try {
            input = connection.getInputStream();
            response = IOUtils.toByteArray(input);
        } finally {
            IOUtils.closeQuietly(input);
        }

        log.debug("Received response from TSA server");

        return response;
    }
}