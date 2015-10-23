package com.logsniffer.util.grok;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.logsniffer.config.BeanConfigFactoryManager;
import com.logsniffer.config.BeanPostConstructor;
import com.logsniffer.config.ConfigException;
import com.logsniffer.util.grok.GrokConsumerConstructor.GrokConsumer;

/**
 * Constructor to inject {@link GroksRegistry} to consumers.
 * 
 * @author mbok
 *
 */
@Component
public class GrokConsumerConstructor implements BeanPostConstructor<GrokConsumer> {
	@Autowired
	private GroksRegistry groksRegistry;

	/**
	 * Grok consumer with dependency to the {@link GroksRegistry}.
	 * 
	 * @author mbok
	 *
	 */
	public static interface GrokConsumer {
		/**
		 * Inits the consumer with the registry instance.
		 * 
		 * @param groksRegistry
		 */
		void initGrokFactory(GroksRegistry groksRegistry);
	}

	@Override
	public void postConstruct(GrokConsumer bean, BeanConfigFactoryManager configManager) throws ConfigException {
		bean.initGrokFactory(groksRegistry);
	}
}
