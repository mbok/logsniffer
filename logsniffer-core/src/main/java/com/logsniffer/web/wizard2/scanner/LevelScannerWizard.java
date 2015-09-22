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

import org.springframework.stereotype.Component;

import com.logsniffer.event.support.LevelScanner;
import com.logsniffer.web.wizard2.ConfigBeanWizard;

/**
 * Scanner for the {@link LevelScanner}.
 * 
 * @author mbok
 * 
 */
@Component
public class LevelScannerWizard implements ConfigBeanWizard<LevelScanner> {

	@Override
	public String getWizardView() {
		return "wizards/scanner/level";
	}

	@Override
	public String getNameKey() {
		return "logsniffer.wizard.scanner.level";
	}

	@Override
	public Class<LevelScanner> getBeanType() {
		return LevelScanner.class;
	}

	@Override
	public LevelScanner getTemplate() {
		return new LevelScanner();
	}

}
