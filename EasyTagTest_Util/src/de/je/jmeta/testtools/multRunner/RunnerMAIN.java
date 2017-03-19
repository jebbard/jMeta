package de.je.jmeta.testtools.multRunner;

import java.io.FileOutputStream;
import java.io.PrintStream;

import org.junit.runner.JUnitCore;

/*
 * @RunWith(MultipleRunner.class) Diese Annnotation ist bei der auszuführenden Testklasse notwendig
 */
public class RunnerMAIN {

   public static void main(String[] args) {

      try {
         FileOutputStream outfos;
         outfos = new FileOutputStream("TestResults.txt");

         PrintStream newoutps = new PrintStream(outfos); // create new output stream

         System.setOut(newoutps); // set the output stream

         JUnitCore core = new JUnitCore();

         core.addListener(new FailureArchiverJUnit(System.out));

         // core.run(ID3v11SingleFileTest_02.class);
         // core.run(ID3v23SingleFileTest_01.class);
         // core.run(AllDataBlocksTests.class);
         // org.junit.runner.JUnitCore.main("de.je.jmeta.datablocks.impl.id3v23.ID3v23SingleFileTest_01");
      } catch (Exception e) {
         e.printStackTrace();
      }  // create new //output stream
   }
}
