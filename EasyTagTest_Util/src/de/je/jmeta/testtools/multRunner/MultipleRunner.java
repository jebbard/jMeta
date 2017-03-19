
package de.je.jmeta.testtools.multRunner;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class MultipleRunner extends BlockJUnit4ClassRunner {

   private static final int RUN_COUNT = 100;

   public MultipleRunner(Class<?> testClass) throws InitializationError {
      super(testClass);
   }

   @Override
   public int testCount() {

      return super.testCount() * RUN_COUNT;
   }

   @Override
   public void run(RunNotifier notifier) {

      for (int i = 0; i < RUN_COUNT; i++) {
         System.out.println("###################");
         System.out.println("Test run no. " + i);
         System.out.println("###################");

         super.run(notifier);

         System.out.println("###################");
      }
   }
}
