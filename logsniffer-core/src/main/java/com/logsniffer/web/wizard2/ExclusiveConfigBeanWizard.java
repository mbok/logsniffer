package com.logsniffer.web.wizard2;

import com.logsniffer.config.ConfiguredBean;

/**
 * Support wizards with exclusive typing.
 * 
 * @author mbok
 *
 * @param <BeanType>
 */
public interface ExclusiveConfigBeanWizard<BeanType extends ConfiguredBean> extends ConfigBeanWizard<BeanType> {
	Class<? super BeanType> getExclusiveType();
}
