package com.logsniffer.web.controller.settings;

import java.io.IOException;

import javax.validation.Valid;

import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.logsniffer.app.ElasticSearchAppConfig.ClientCallback;
import com.logsniffer.app.ElasticSearchAppConfig.ElasticClientTemplate;
import com.logsniffer.app.ElasticSearchAppConfig.EsSettings;
import com.logsniffer.app.ElasticSearchAppConfig.EsSettingsHolder;
import com.logsniffer.web.controller.settings.ElasticSettingsResource.EsStatusAndSettings.EsStatus;

/**
 * Settings REST source for elasticsearch.
 * 
 * @author mbok
 *
 */
@RestController
public class ElasticSettingsResource {
	@Autowired
	private EsSettingsHolder settingsHolder;

	@Autowired
	private ElasticClientTemplate esClientTpl;

	@RequestMapping(value = "/settings/elastic", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public EsStatusAndSettings EsStatusAndSettings() {
		return getStatus(settingsHolder.getSettings());
	}

	@RequestMapping(value = "/settings/elastic", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public EsStatusAndSettings saveElasticSettings(@RequestBody @Valid final EsSettings settings) throws IOException {
		settingsHolder.storeSettings(settings);
		return getStatus(settings);

	}

	private EsStatusAndSettings getStatus(final EsSettings settings) {
		final EsStatusAndSettings esas = new EsStatusAndSettings();
		esas.settings = settings;
		try {
			esClientTpl.executeWithClient(new ClientCallback<Object>() {
				@Override
				public Boolean execute(final Client client) {
					switch (client.admin().cluster().prepareHealth().get().getStatus()) {
					case GREEN:
					case YELLOW:
						esas.status = EsStatus.GREEN;
						break;
					case RED:
						esas.status = EsStatus.RED;
						break;
					}
					return null;
				}
			});
		} catch (final Exception e) {
			esas.status = EsStatus.RED;
			esas.statusMessage = e.getMessage();
		}
		return esas;
	}

	/**
	 * Composition of status and settings.
	 * 
	 * @author mbok
	 *
	 */
	public static final class EsStatusAndSettings {
		public enum EsStatus {
			GREEN, RED
		}

		private EsSettings settings;

		private EsStatus status;
		private String statusMessage;

		/**
		 * @return the settings
		 */
		public EsSettings getSettings() {
			return settings;
		}

		/**
		 * @return the status
		 */
		public EsStatus getStatus() {
			return status;
		}

		/**
		 * @return the statusMessage
		 */
		public String getStatusMessage() {
			return statusMessage;
		}

	}
}
