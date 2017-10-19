package de.je.util.javautil.common.configparams;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

/**
 * {@link CalledConfigChangeListener} is a dummy test configuration change listener that is expected to be really
 * called, with a given list of parameters and expected values.
 */
public class CalledConfigChangeListener implements IConfigChangeListener {

   private Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> expectedParams = new HashMap<>();
   private Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> notifiedParams = new HashMap<>();
   private int callCounter = 0;

   /**
    * @return the current number of calls to the notification method
    */
   public int getCallCounter() {
      return callCounter;
   }

   /**
    * Resets the listener as if it were never called and all its attributes are cleared / set to 0.
    */
   public void reset() {
      expectedParams.clear();
      notifiedParams.clear();
      callCounter = 0;
   }

   /**
    * @param expectedParams
    *           the expected {@link AbstractConfigParam}s with their values
    */
   public void setExpectedParams(Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> expectedParams) {
      this.expectedParams = expectedParams;
   }

   /**
    * @return the Map of parameters that the listener was really notified about
    */
   public Map<AbstractConfigParam<? extends Comparable<?>>, Comparable<?>> getNotifiedParams() {
      return notifiedParams;
   }

   /**
    * @see de.je.util.javautil.common.configparams.IConfigChangeListener#configurationParameterValueChanged(de.je.util.javautil.common.configparams.AbstractConfigParam,
    *      java.lang.Comparable)
    */
   @Override
   public void configurationParameterValueChanged(AbstractConfigParam<? extends Comparable<?>> param,
      Comparable<?> value) {
      callCounter++;
      Assert.assertTrue(expectedParams.containsKey(param));
      Assert.assertEquals(expectedParams.get(param), value);

      notifiedParams.put(param, value);
   }
}
