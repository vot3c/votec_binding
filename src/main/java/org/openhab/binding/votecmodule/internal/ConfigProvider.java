package org.openhab.binding.votecmodule.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
@Component
public class ConfigProvider implements ConfigOptionProvider {

    private final Logger logger = LoggerFactory.getLogger(ConfigProvider.class);

    static ArrayList<Integer> ADDED_NODE_IDS = new ArrayList<Integer>();

    /**
     * @param addedNodeId the addedNodeId to set
     */
    public static void setAddedNodeId(ArrayList<Integer> addedNodeId) {
        ADDED_NODE_IDS = addedNodeId;
    }

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        return ConfigOptionProvider.super.getParameterOptions(uri, param, context, locale);
    }

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable Locale locale) {

        switch (param) {
            case "nodeId":
                List<ParameterOption> options = new ArrayList<ParameterOption>();
                for (int i = 0; i < 255; i++) {

                    if (!ADDED_NODE_IDS.contains(i)) {
                        options.add(new ParameterOption(Integer.toString(i), Integer.toString(i)));
                    }

                }

                return options;
            default:
                break;
        }

        return null;
    }
}
