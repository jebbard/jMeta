/**
 *
 * {@link IUserConfigAccessorTest}.java
 *
 * @author Jens Ebert
 *
 * @date 06.08.2011
 */
package de.je.util.javautil.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.je.util.javautil.common.config.AbstractConfigAccessor;
import de.je.util.javautil.common.config.IUserConfigAccessor;
import de.je.util.javautil.common.config.issue.ConfigIssue;
import de.je.util.javautil.common.configparams.UserConfigParam;

/**
 * {@link IUserConfigAccessorTest} tests the {@link IUserConfigAccessor} interface in a very coarse way, as the detailed
 * testing is already done in the test cases for {@link AbstractConfigAccessor} class.
 */
public abstract class IUserConfigAccessorTest {

   /**
    * Tests {@link IUserConfigAccessor}.
    */
   @Test
   public void test_IUserConfigAccessor() {

      IUserConfigAccessor testling = getUserConfigTestling();

      Assert.assertFalse(testling.isLoaded());

      for (int i = 0; i < getUserConfigStreams().size(); ++i) {
         try (InputStream configStream = getUserConfigStreams().get(i)) {
            List<ConfigIssue> issues = testling.load(configStream);
            Assert.assertNotNull(issues);

            Assert.assertTrue(testling.isLoaded());

            Map<UserConfigParam<?>, Object> expectedParamValues = getExpectedUserConfigValues().get(configStream);

            for (Iterator<UserConfigParam<?>> iterator = expectedParamValues.keySet().iterator(); iterator.hasNext();) {
               UserConfigParam<?> nextParam = iterator.next();
               Object nextValue = expectedParamValues.get(nextParam);

               Assert.assertEquals(nextValue, testling.getUserConfigParam(nextParam));
            }
         } catch (IOException e) {
            Assert.fail("Unexpected exception: " + e);
         }
      }
   }

   /**
    * Returns the {@link IUserConfigAccessor} instance to test.
    *
    * @return the {@link IUserConfigAccessor} instance to test.
    */
   protected abstract IUserConfigAccessor getUserConfigTestling();

   /**
    * Returns the {@link IUserConfigAccessor} configuration files to test.
    *
    * @return the {@link IUserConfigAccessor} configuration files to test.
    */
   protected abstract List<InputStream> getUserConfigStreams();

   /**
    * Returns the {@link UserConfigParam}s expected to be invalid mapped to their configuration file.
    *
    * @return the {@link UserConfigParam}s expected to be invalid mapped to their configuration file.
    */
   protected abstract Map<InputStream, Map<UserConfigParam<?>, Object>> getExpectedUserConfigValues();
}
