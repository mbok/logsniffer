package com.logsniffer.system.version.support;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logsniffer.settings.http.HttpSettings;
import com.logsniffer.system.version.UpdatesInfoProvider;
import com.logsniffer.system.version.VersionInfo;
import com.logsniffer.util.value.ConfigValue;
import com.logsniffer.util.value.Configured;

/**
 * Retrieves version info from a HTTP URL with support for following JSON
 * response: { stable: { version: "0.5.3", features: true, bugfixes: true,
 * security: false } }
 * 
 * @author mbok
 *
 */
@Component
public class UrlUpdatesCheckProvider implements UpdatesInfoProvider {
	public static final String PROP_LOGSNIFFER_UPDATES_CHECK_URL = "logsniffer.system.updatesCheckUrl";
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private HttpSettings httpSettings;

	@Autowired
	private ObjectMapper objectMapper;

	@Configured(value = PROP_LOGSNIFFER_UPDATES_CHECK_URL, defaultValue = "http://www.logsniffer.com/versionCheck.php?version={0}")
	private ConfigValue<String> updatesCheckUrlValue;

	/**
	 * {@link VersionInfo} mapped for JSON input.
	 * @author mbok
	 *
	 */
	public static class VersionInfoInputMapped extends VersionInfo {
		@JsonProperty("version")
		@Override
		public String getName() {
			return super.getName();
		}

		@JsonProperty("features")
		@Override
		public boolean isFeatures() {
			return super.isFeatures();
		}

		@JsonProperty("bugfixes")
		@Override
		public boolean isBugfixes() {
			return super.isBugfixes();
		}

		@JsonProperty("security")
		@Override
		public boolean isSecurity() {
			return super.isSecurity();
		}
		
	}
	
	/**
	 * Wrapper for the HTTP JSON response.
	 * 
	 * @author mbok
	 *
	 */
	public static class JsonResponseWrapper {
		private VersionInfoInputMapped stable;

		/**
		 * @return the stable
		 */
		public VersionInfoInputMapped getStable() {
			return stable;
		}

		/**
		 * @param stable
		 *            the stable to set
		 */
		public void setStable(VersionInfoInputMapped stable) {
			this.stable = stable;
		}
	}

	@Override
	public VersionInfo getLatestStableVersion(UpdatesInfoContext context) throws IOException {
		HttpClient client = httpSettings.createHttpClientBuilder().build();
		HttpGet get = new HttpGet(MessageFormat.format(updatesCheckUrlValue.get(), context.getCurrentVersion()));
		logger.debug("Calling '{}' to get updates info for: {}", get, context);
		HttpResponse response = client.execute(get);
		JsonResponseWrapper wrapper = objectMapper.readValue(response.getEntity().getContent(),
				JsonResponseWrapper.class);
		return wrapper.getStable();
	}

}
