package com.logsniffer.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST resource for I18N stuff.
 * 
 * @author mbok
 *
 */
@RestController
public class I18NResource {
	/**
	 * Wrapper for locales and timezones.
	 * 
	 * @author mbok
	 *
	 */
	public static class LocalesAndTimezonesWrapper {
		private final List<Locale> locales;
		private final List<String> timezones;

		public LocalesAndTimezonesWrapper(final List<Locale> locales, final List<String> timezones) {
			super();
			this.locales = new ArrayList<Locale>();
			for (final Locale l : locales) {
				if (l.toString().length() > 0) {
					this.locales.add(l);
				}
			}
			this.timezones = timezones;
		}

		/**
		 * @return the locales
		 */
		public List<Locale> getLocales() {
			return locales;
		}

		/**
		 * @return the timezones
		 */
		public List<String> getTimezones() {
			return timezones;
		}

	}

	/**
	 * Exposes available locales.
	 * 
	 * @return available locales
	 */
	@RequestMapping(path = "utils/i18n/localesAndTimezones", method = RequestMethod.GET)
	LocalesAndTimezonesWrapper availableLocalesAndTimezones() {
		return new LocalesAndTimezonesWrapper(Arrays.asList(Locale.getAvailableLocales()),
				Arrays.asList(TimeZone.getAvailableIDs()));
	}
}
