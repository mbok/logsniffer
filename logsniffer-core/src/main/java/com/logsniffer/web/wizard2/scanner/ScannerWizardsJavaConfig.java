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
package com.logsniffer.web.wizard2.scanner;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.logsniffer.event.support.RegexPatternScanner;
import com.logsniffer.web.wizard2.ConfigBeanWizard;
import com.logsniffer.web.wizard2.SimpleBeanWizard;

/**
 * Exposes simple scanner wizards.
 *
 * @author mbok
 *
 */
@Configuration
public class ScannerWizardsJavaConfig {

	@Bean
	public ConfigBeanWizard<RegexPatternScanner> grokPatternScannerWizard() {
		RegexPatternScanner template = new RegexPatternScanner();
		template.getGrokBean().setSubStringSearch(true);
		return new SimpleBeanWizard<RegexPatternScanner>("logsniffer.wizard.scanner.regexPattern",
				"/ng/wizards/scanner/regexPattern.html", RegexPatternScanner.class, template);
	}

}
