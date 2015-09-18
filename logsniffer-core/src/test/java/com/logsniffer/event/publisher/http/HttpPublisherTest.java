/*******************************************************************************
 * logsniffer, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2015 Scaleborn UG, www.scaleborn.com
 *
 * logsniffer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * logsniffer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.logsniffer.event.publisher.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.options;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.trace;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.logsniffer.event.Event;
import com.logsniffer.event.Publisher.PublishException;
import com.logsniffer.event.publisher.VelocityEventRenderer;

/**
 * Test for {@link HttpPublisher}.
 * 
 * @author mbok
 * 
 */
public class HttpPublisherTest {
	private static int port = 8099;
	static {
		ServerSocket s = null;
		try {
			s = new ServerSocket(0);
			port = s.getLocalPort();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(port);

	private HttpPublisher publisher;
	private VelocityEventRenderer renderer;
	private HttpClient client;

	@Before
	public void setUp() {
		publisher = new HttpPublisher();
		renderer = Mockito.mock(VelocityEventRenderer.class);
		Mockito.when(
				renderer.render(Mockito.anyString(),
						Mockito.any(VelocityContext.class))).thenAnswer(
				new Answer<String>() {

					@Override
					public String answer(final InvocationOnMock invocation)
							throws Throwable {
						return invocation.getArguments()[0].toString();
					}
				});
		client = HttpClientBuilder.create().build();
		publisher.init(renderer, client);
	}

	@Test
	public void testGet() throws PublishException {
		stubFor(get(urlEqualTo("/eventId/123")).willReturn(
				aResponse().withStatus(201)
						.withHeader("Content-Type", "text/xml")
						.withBody("<response>Some content</response>")));

		publisher.setUrl("http://localhost:" + port + "/eventId/123");
		Event event = new Event();
		event.setId("123");
		publisher.publish(event);
	}

	@Test(expected = PublishException.class)
	public void testGet404() throws PublishException {
		stubFor(get(urlEqualTo("/eventId/123")).willReturn(
				aResponse().withStatus(404)
						.withHeader("Content-Type", "text/xml")
						.withBody("<response>Some content</response>")));
		publisher.setUrl("http://localhost:" + port + "/eventId/123");
		Event event = new Event();
		event.setId("123");
		publisher.publish(event);
	}

	@Test
	public void testPost() throws PublishException {
		stubFor(post(urlEqualTo("/eventId/12345")).withRequestBody(
				equalTo("eventbody")).willReturn(
				aResponse().withStatus(HttpStatus.NO_CONTENT.value())
						.withHeader("Content-Type", "text/xml")
						.withBody("<response>Some content</response>")));
		publisher.setMethod(HttpMethod.POST);
		publisher.setUrl("http://localhost:" + port + "/eventId/12345");
		publisher.setBody("eventbody");
		Event event = new Event();
		event.setId("123");
		publisher.publish(event);
	}

	@Test
	public void testPut() throws PublishException {
		stubFor(put(urlEqualTo("/eventId/1234567"))
				.withHeader("Content-Type",
						equalTo("application/json; charset=UTF-8"))
				.withRequestBody(equalTo("{}"))
				.willReturn(
						aResponse().withStatus(HttpStatus.OK.value())
								.withHeader("Content-Type", "text/xml")
								.withBody("<response>Some content</response>")));
		publisher.setMethod(HttpMethod.PUT);
		publisher.setBodyMimeType("application/json");
		publisher.setUrl("http://localhost:" + port + "/eventId/1234567");
		publisher.setBody("{}");
		Event event = new Event();
		event.setId("123");
		publisher.publish(event);
	}

	@Test
	public void testDelete() throws PublishException {
		stubFor(delete(urlEqualTo("/eventId/123456789")).willReturn(
				aResponse().withStatus(201)));
		publisher.setMethod(HttpMethod.DELETE);
		publisher.setUrl("http://localhost:" + port + "/eventId/123456789");
		Event event = new Event();
		event.setId("123");
		publisher.publish(event);
	}

	@Test
	public void testOptions() throws PublishException {
		stubFor(options(urlEqualTo("/eventId/1234567890")).willReturn(
				aResponse().withStatus(201)));
		publisher.setMethod(HttpMethod.OPTIONS);
		publisher.setUrl("http://localhost:" + port + "/eventId/1234567890");
		Event event = new Event();
		event.setId("123");
		publisher.publish(event);
	}

	@Test
	public void testHead() throws PublishException {
		stubFor(head(urlEqualTo("/eventId/12345678901")).willReturn(
				aResponse().withStatus(201)));
		publisher.setMethod(HttpMethod.HEAD);
		publisher.setUrl("http://localhost:" + port + "/eventId/12345678901");
		Event event = new Event();
		event.setId("123");
		publisher.publish(event);
	}

	@Test
	public void testPatch() throws PublishException {
		stubFor(patch(urlEqualTo("/eventId/123456789012")).willReturn(
				aResponse().withStatus(201)));
		publisher.setMethod(HttpMethod.PATCH);
		publisher.setUrl("http://localhost:" + port + "/eventId/123456789012");
		Event event = new Event();
		event.setId("123");
		publisher.publish(event);
	}

	@Test
	public void testTrace() throws PublishException {
		stubFor(trace(urlEqualTo("/eventId/12345678901")).willReturn(
				aResponse().withStatus(201)));
		publisher.setMethod(HttpMethod.TRACE);
		publisher.setUrl("http://localhost:" + port + "/eventId/12345678901");
		Event event = new Event();
		event.setId("123");
		publisher.publish(event);
	}
}
