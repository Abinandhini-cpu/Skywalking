package com.a.eye.skywalking.api.plugin;

import com.a.eye.skywalking.api.logging.api.ILog;
import com.a.eye.skywalking.api.logging.api.LogManager;
import net.bytebuddy.pool.TypePool;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Plugins finder.
 * Use {@link PluginResourcesResolver} to find all plugins,
 * and ask {@link PluginCfg} to load all plugin definitions.
 *
 * @author wusheng
 */
public class PluginBootstrap {
    private static ILog logger = LogManager.getLogger(PluginBootstrap.class);

    public static TypePool CLASS_TYPE_POOL = null;

    /**
     * load all plugins.
     * @return plugin definition list.
     */
    public List<AbstractClassEnhancePluginDefine> loadPlugins() {
        CLASS_TYPE_POOL = TypePool.Default.ofClassPath();

        PluginResourcesResolver resolver = new PluginResourcesResolver();
        List<URL> resources = resolver.getResources();

        if (resources == null || resources.size() == 0) {
            logger.info("no plugin files (skywalking-plugin.properties) found, continue to start application.");
            return new ArrayList<AbstractClassEnhancePluginDefine>();
        }

        for (URL pluginUrl : resources) {
            try {
                PluginCfg.CFG.load(pluginUrl.openStream());
            } catch (Throwable t) {
                logger.error("plugin [{}] init failure.", new Object[] {pluginUrl}, t);
            }
        }

        List<String> pluginClassList = PluginCfg.CFG.getPluginClassList();

        List<AbstractClassEnhancePluginDefine> plugins = new ArrayList<AbstractClassEnhancePluginDefine>();
        for (String pluginClassName : pluginClassList) {
            try {
                logger.debug("loading plugin class {}.", pluginClassName);
                AbstractClassEnhancePluginDefine plugin =
                        (AbstractClassEnhancePluginDefine) Class.forName(pluginClassName).newInstance();
                plugins.add(plugin);
            } catch (Throwable t) {
                logger.error("loade plugin [{}] failure.", new Object[] {pluginClassName}, t);
            }
        }

        return plugins;

    }


}
