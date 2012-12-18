package org.jdbcdslog.plugin;

import org.jdbcdslog.ConfigurationParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;

/**
 * The factory creates an instance of EventHandler when it is not yet present in org.jdbcdslog.plugin.EventHandlerAPI
 */
public class EventHandlerFactory {

    private static Logger logger = LoggerFactory.getLogger(EventHandlerFactory.class);

    /**
     * Creates a EventHandler from configurations or a instance of the default StdoutEventHandler in case of any error.
     * @return not null
     */
    public static EventHandler createFromConfig() {
        EventHandler plugin;

        logger.debug("Trying to create an instance of EventHandler from configurations.");

        Class<?> pluginClass = ConfigurationParameters.getPluginClass();
        if (EventHandler.class.isAssignableFrom(pluginClass)) {
            try {
                try {
                    logger.debug("Trying to instantiate an EventHandler with the constructor with" +
                            "exactly one parameter of java.util.Properties.");

                    ClassLoader loader = EventHandlerFactory.class.getClassLoader();
                    InputStream in = null;
                    Properties props;
                    try {
                        in = loader.getResourceAsStream("jdbcdslog.properties");
                        props = new Properties(System.getProperties());
                        if (in != null){
                            props.load(in);
                        }
                    } catch (IOException e) {
                        logger.error("No jdbcdslog.properties found in classpath.", e);
                        props = new Properties();
                    } finally {
                        assert in != null;
                        in.close();
                    }

                    Constructor<?> ctor = pluginClass.getConstructor(Properties.class);
                    plugin = (EventHandler)ctor.newInstance(props);
                } catch (NoSuchMethodException e2) {
                    logger.debug("Trying to instantiate an EventHandler with the default constructor.");
                    plugin = (EventHandler)pluginClass.newInstance();
                }
            } catch (Exception e) {
                logger.warn("Couldn't instantiate an instance of the plugin class." +
                    "Falling back to the default 'org.jdbcdslog.plugin.StdoutEventHandler'.", e);
                plugin = new StdoutEventHandler();
            }
        } else {
            logger.warn("Didn't instantiate an instance of the plugin class because the class '" +
                    pluginClass.getSimpleName() + "' is not a subclass of org.jdbcdslog.plugin.EventHandler.");
            plugin = new StdoutEventHandler();
        }

        return plugin;
    }
}
