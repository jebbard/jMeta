package de.je.jmeta.context;

import org.junit.Assert;
import org.junit.Test;

import de.je.jmeta.context.iface.IJMetaContext;
import de.je.jmeta.context.iface.JMetaContext;
import de.je.jmeta.testtools.logChecker.LogChecker;

/**
 * {@link JMetaContextTest} tests the start-up of the {@link JMetaContext}.
 */
public class JMetaContextTest {

   /**
    * Tests {@link JMetaContext#getInstance()}.
    */
   @Test
   public void test_getInstance() {

      System.out.println(System.getProperty("java.class.path"));

      JMetaContext testling = JMetaContext.getInstance();

      IJMetaContext context = testling.get();

      Assert.assertNotNull(context);
      Assert.assertNotNull(context.getDataBlockAccessor());
      Assert.assertNotNull(context.getDataFormatRepository());
      Assert.assertNotNull(context.getLogging());

      LogChecker logChecker = new LogChecker();

      logChecker.logCheck(context.getLogging().getCentralLogFile());

      // TODO deploy001: Why does JAXB not need to be on the classpath?
      // Offenbar sind die verwendeten Klassen ja in rt.jar enthalten, aber welcher Anteil
      // kommt dann von JAXB?
      // TODO deploy002: Why does jMetaCore.jar need to be on the classpath?
      // - document error "Caused by: javax.xml.bind.JAXBException: "de.je.jmeta.extmanager.impl.jaxb.extpoints" doesnt
      // contain ObjectFactory.class or jaxb.index"

      // TODO deploy004: When adding JAR files to a JAR file's class path, the internal
      // classes in those third JAR file CAN be used by the user!!!!
   }
}
