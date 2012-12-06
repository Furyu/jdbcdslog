package org.jdbcdslog.plugin;

import org.jdbcdslog.ConfigurationParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventHandlerFactory {

    private static Logger logger = LoggerFactory.getLogger(EventHandlerFactory.class);

    /**
     * Creates a EventHandler from configurations or a instance of the default StdoutEventHandler in case of any error.
     * @return not null
     */
    public static EventHandler createFromConfig() {
        EventHandler plugin = null;

        Class pluginClass = ConfigurationParameters.getPluginClass();
        if (EventHandler.class.isAssignableFrom(pluginClass)) {
            try {
                plugin = (EventHandler)pluginClass.newInstance();
            } catch (Exception e) {
                logger.warn("Couldn't instantiate an instance of the plugin class." +
                    "Falling back to the default 'org.jdbcdslog.plugin.StdoutEventHandler'.", e);
                plugin = new StdoutEventHandler();
            }
        } else {
            logger.warn("Didn't instantiate an instance of the plugin class because the class '" +
                    pluginClass.getSimpleName() + "' is not a subclass of orgjdbcdslog.plugin.EventHandler.");
            plugin = new StdoutEventHandler();
        }

        return plugin;
    }
}
