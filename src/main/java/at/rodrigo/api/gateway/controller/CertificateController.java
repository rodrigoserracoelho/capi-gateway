/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *          http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package at.rodrigo.api.gateway.controller;

import at.rodrigo.api.gateway.schema.AliasInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@RestController
@RequestMapping("/certificate")
@Api(value = "Certificate Management", tags = {"Certificate Management"}, description = "Use these services to manage your API's SSL certificates.")
@Slf4j
public class CertificateController {

    @Value("${server.ssl.trust-store}")
    private String trustCertificatePath;

    @Value("${server.ssl.trust-store-password}")
    private String trustCertificatePassword;

    @ApiOperation(value = "Get all certificates")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All certificates trusted by CAPI Gateway")
    })
    @GetMapping
    public ResponseEntity<List<AliasInfo>> getAll() {
        List<AliasInfo> theAlias = new ArrayList<>();
        try {
            FileInputStream is = new FileInputStream(trustCertificatePath);

            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, trustCertificatePassword.toCharArray());
            Enumeration<String> aliases = keystore.aliases();
            while(aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                AliasInfo aliasInfo = new AliasInfo();
                aliasInfo.setAlias(alias);
                X509Certificate certificate = (X509Certificate) keystore.getCertificate(alias);
                aliasInfo.setIssuerDN(certificate.getIssuerDN().getName());
                aliasInfo.setSubjectDN(certificate.getSubjectDN().getName());
                theAlias.add(aliasInfo);
            }
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(theAlias, HttpStatus.OK);
    }

    @ApiOperation(value = "Add a certificate to CAPI Gateway trusted store.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Certificate trusted")
    })
    @PostMapping(path = "/{alias}")
    public ResponseEntity<AliasInfo> certificateUpload(@PathVariable String alias, @RequestParam("file") MultipartFile file) {
        AliasInfo aliasInfo = new AliasInfo();
        try {
            FileInputStream is = new FileInputStream(trustCertificatePath);
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, trustCertificatePassword.toCharArray());

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Certificate newTrusted = certificateFactory.generateCertificate(file.getInputStream());

            X509Certificate x509Object = (X509Certificate) newTrusted;
            aliasInfo.setSubjectDN(x509Object.getSubjectDN().getName());
            aliasInfo.setIssuerDN(x509Object.getIssuerDN().getName());

            keystore.setCertificateEntry(alias, newTrusted);

            FileOutputStream storeOutputStream = new FileOutputStream(trustCertificatePath);
            keystore.store(storeOutputStream, trustCertificatePassword.toCharArray());


        } catch (CertificateException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        }
        return new ResponseEntity<>(aliasInfo, HttpStatus.OK);
    }

    @ApiOperation(value = "Remove a certificate from CAPI Gateway trusted store.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Certificate removed")
    })
    @DeleteMapping(path = "/{alias}")
    public ResponseEntity<AliasInfo> removeFromTrust(@PathVariable String alias) {
        AliasInfo aliasInfo = new AliasInfo();
        try {
            FileInputStream is = new FileInputStream(trustCertificatePath);
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(is, trustCertificatePassword.toCharArray());

            keystore.deleteEntry(alias);

            FileOutputStream storeOutputStream = new FileOutputStream(trustCertificatePath);
            keystore.store(storeOutputStream, trustCertificatePassword.toCharArray());


        } catch (CertificateException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(aliasInfo, HttpStatus.OK);
    }
}