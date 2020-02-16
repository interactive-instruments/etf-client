/**
 * Copyright 2017-2019 European Union, interactive instruments GmbH
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This work was supported by the EU Interoperability Solutions for
 * European Public Administrations Programme (http://ec.europa.eu/isa)
 * through Action 1.17: A Reusable INSPIRE Reference Platform (ARE3NA).
 */
package de.interactive_instruments.etf.client.internal;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import de.interactive_instruments.etf.client.RemoteInvocationException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
abstract class Request {

    private final static String USER_AGENT_HEADER = "ETF Client";
    private final static String ACCEPT_HEADER = "application/json";

    final HttpRequest.Builder requestBuilder;
    final HttpClient httpClient;

    Request(final URI url, final InstanceCtx ctx) {
        this.requestBuilder = HttpRequest.newBuilder(url)
                .timeout(Duration.ofMinutes(2))
                .header("Accept", ACCEPT_HEADER)
                .header("Accept-Language", ctx.locale.getLanguage())
                .header("User-Agent", USER_AGENT_HEADER)
                .header("ETF-Client-Session-ID", ctx.sessionId);
        if (ctx.auth != null) {
            this.httpClient = HttpClient.newBuilder().authenticator(ctx.auth).build();
        } else {
            this.httpClient = HttpClient.newBuilder().build();
        }
    }

    final void checkResponse(final HttpResponse response, final int... expectedCodes)
            throws RemoteInvocationException {
        for (final int code : expectedCodes) {
            if (response.statusCode() == code) {
                return;
            }
        }
        throw new RemoteInvocationException(response);
    }
}