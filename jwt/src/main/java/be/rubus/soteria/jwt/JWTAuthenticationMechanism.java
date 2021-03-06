/*
 * Copyright 2016 Rudy De Busscher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package be.rubus.soteria.jwt;

import javax.inject.Inject;
import javax.security.auth.message.AuthException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;


/**
 *
 */
public class JWTAuthenticationMechanism implements HttpAuthenticationMechanism {

    private static final String BEARER = "Bearer ";

    @Inject
    private IdentityStore identityStore;

    @Inject
    private JWTTokenHandler tokenHandler;

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthException {

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            String token = authorizationHeader.substring(BEARER.length());

            JWTCredential credential = tokenHandler.retrieveCredential(token);

            CredentialValidationResult result = identityStore.validate(credential);

            if (result.getStatus() == VALID) {
                // Communicate the details of the authenticated user to the
                // container. In many cases the underlying handler will just store the details
                // and the container will actually handle the login after we return from
                // this method.
                return httpMessageContext.notifyContainerAboutLogin(
                        result.getCallerPrincipal(), result.getCallerGroups());
            } else {
                throw new AuthException("Login failed");
            }
        }

        return httpMessageContext.doNothing();
    }
}
