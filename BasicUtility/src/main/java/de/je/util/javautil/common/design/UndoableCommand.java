/**
 *
 * {@link UndoableCommand}.java
 *
 * @author Jens Ebert
 *
 * @date 25.04.2010
 *
 */
package de.je.util.javautil.common.design;

/**
 * {@link UndoableCommand} adds the possibility to undo a command.
 */
public interface UndoableCommand extends Command {

   /**
    * Undos the command.
    */
   public void undo();
}
