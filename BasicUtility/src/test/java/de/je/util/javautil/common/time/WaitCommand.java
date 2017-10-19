package de.je.util.javautil.common.time;

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
    * @see de.je.util.javautil.common.time.MeasuredCommand#getUniqueName()
    */
   @Override
   public String getUniqueName() {
      return "WaitCommand " + waitTimeMillis + "[" + hashCode() + "]";
   }

   /**
    * @see de.je.util.javautil.common.design.Command#execute()
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
