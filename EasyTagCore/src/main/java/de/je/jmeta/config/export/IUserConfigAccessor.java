
package de.je.jmeta.config.export;

import java.io.InputStream;
import java.util.List;

import de.je.util.javautil.common.config.issue.ConfigIssue;
import de.je.util.javautil.simpleregistry.IComponentInterface;

/**
 * {@link IUserConfigAccessor} provides read access to user configuration.
 */
public interface IUserConfigAccessor extends IComponentInterface {

   /**
    * Loads data from a configuration {@link InputStream}, thereby discarding previously loaded configuration data.
    * Returns a {@link List} of {@link ConfigIssue}s that occurred while loading the configuration parameters. The
    * {@link List} may be empty which means no errors occurred during configuration loading.
    * 
    * @param inputStream
    *           The configuration {@link InputStream} to load.
    *
    * @return a {@link List} of {@link ConfigIssue}s that occurred while loading the configuration parameters. The
    *         {@link List} may be empty which means no errors occurred during configuration loading.
    */
   public List<ConfigIssue> load(InputStream inputStream);

   /**
    * Returns the currently loaded configuration file or null if none is loaded yet.
    *
    * @return the currently loaded configuration file or null if none is loaded yet.
    */
   public boolean isLoaded();

   /**
    * Returns whether the given {@link UserConfigParam} is maintained by this {@link IUserConfigAccessor}.
    *
    * @param configParam
    *           The {@link UserConfigParam}. Must be defined in the configuration.
    * @param <T>
    *           The concrete type of the {@link UserConfigParam}'s value.
    * @return true if the given {@link UserConfigParam} is maintained by this {@link IUserConfigAccessor}, false
    *         otherwise.
    */
   public <T> boolean hasUserConfigParam(UserConfigParam<T> configParam);

   /**
    * Returns the current value of the given {@link UserConfigParam}.
    *
    * @param configParam
    *           The {@link UserConfigParam}. Must be defined in the configuration.
    * @param <T>
    *           The concrete type of the {@link UserConfigParam}'s value.
    * @return the current value of the given {@link UserConfigParam}.
    *
    * @pre {@link #hasUserConfigParam(UserConfigParam)} == true
    */
   public <T> T getUserConfigParam(UserConfigParam<T> configParam);
}