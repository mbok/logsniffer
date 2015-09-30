package com.logsniffer.web.advice;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.logsniffer.app.CoreAppConfig;

/**
 * General advice for common model attributes.
 * 
 * @author mbok
 *
 */
@ControllerAdvice
public class CommonControllerAdvice {
	@Autowired
	@Qualifier(CoreAppConfig.BEAN_LOGSNIFFER_PROPS)
	private Properties logsnifferProps;

	/**
	 * Exposes the CoreAppConfig#BEAN_LOGSNIFFER_PROPS as general modell
	 * attribute under the name "logsnifferProps".
	 * 
	 * @return the CoreAppConfig#BEAN_LOGSNIFFER_PROPS properties object
	 */
	@ModelAttribute("logsnifferProps")
	public Properties logsnifferProps() {
		return logsnifferProps;
	}
}
