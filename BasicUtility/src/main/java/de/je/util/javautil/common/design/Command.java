/**
 *
 * {@link Command}.java
 *
 * @author Jens Ebert
 *
 * @date 25.04.2010
 *
 */
package de.je.util.javautil.common.design;

/**
 * {@link Command} represents a command corresponding to the command pattern having an execute method.
 */
public interface Command {

   /**
    * Executes the command.
    */
   public void execute();
}
