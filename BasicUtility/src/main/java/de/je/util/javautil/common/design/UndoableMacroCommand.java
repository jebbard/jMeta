/**
 *
 * {@link UndoableMacroCommand}.java
 *
 * @author Jens Ebert
 *
 * @date 25.04.2010
 *
 */
package de.je.util.javautil.common.design;

/**
 * {@link UndoableMacroCommand} can undo actions of a set of commands.
 */
public class UndoableMacroCommand extends MacroCommand implements UndoableCommand {

   /**
    * Creates a new {@link UndoableMacroCommand}.
    * 
    * @param commands
    *           The {@link UndoableCommand} performed by this {@link UndoableMacroCommand} in the given order.
    */
   public UndoableMacroCommand(UndoableCommand[] commands) {
      super(commands);
   }

   /**
    * @see UndoableCommand#undo()
    */
   @Override
   public void undo() {
      for (int i = 0; i < getCommands().length; i++) {
         ((UndoableCommand) getCommands()[i]).undo();
      }
   }
}
