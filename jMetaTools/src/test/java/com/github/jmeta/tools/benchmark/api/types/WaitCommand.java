package com.github.jmeta.tools.benchmark.api.types;

/**
 * {@link WaitCommand} waits for a given time period.
 */
public class WaitCommand implements MeasuredCommand {

   /**
    * Creates a new {@link WaitCommand}.
    * 
    * @param waitTimeMillis
    *           The wait time in milliseconds
    */
   public WaitCommand(long waitTimeMillis) {
      super();
      this.waitTimeMillis = waitTimeMillis;
   }

   /**
    * @see com.github.jmeta.tools.benchmark.api.types.MeasuredCommand#getUniqueName()
    */
   @Override
   public String getUniqueName() {
      return "WaitCommand " + waitTimeMillis + "[" + hashCode() + "]";
   }

   /**
    * @see com.github.jmeta.tools.benchmark.api.types.Command#execute()
    */
   @Override
   public void execute() {
      try {
         Thread.sleep(waitTimeMillis);
      } catch (InterruptedException e) {
         throw new RuntimeException("Couldn't sleep", e);
      }
   }

   private final long waitTimeMillis;
}
