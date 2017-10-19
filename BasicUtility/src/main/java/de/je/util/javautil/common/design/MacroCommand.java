/**
 *
 * {@link MacroCommand}.java
 *
 * @author Jens Ebert
 *
 * @date 25.04.2010
 *
 */
package de.je.util.javautil.common.design;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link MacroCommand} can execute multiple commands in order.
 */
public class MacroCommand implements Command {

   /**
    * Creates a new {@link MacroCommand}.
    * 
    * @param commands
    *           the commands to execute.
    */
   public MacroCommand(Command[] commands) {
      Reject.ifNull(commands, "commands");

      m_commands = commands;
   }

   /**
    * @see Command#execute()
    */
   @Override
   public void execute() {
      for (int i = 0; i < m_commands.length; i++) {
         m_commands[i].execute();
      }
   }

   /**
    * Returns the commands executed by this {@link MacroCommand}.
    *
    * @return the commands executed by this {@link MacroCommand}.
    */
   public Command[] getCommands() {
      return m_commands;
   }

   private Command[] m_commands;
}
