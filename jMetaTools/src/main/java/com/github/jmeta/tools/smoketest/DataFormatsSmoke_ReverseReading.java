
package com.github.jmeta.tools.smoketest;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.jmeta.defaultextensions.apev2.impl.APEv2Extension;
import com.github.jmeta.defaultextensions.id3v1.impl.ID3v1Extension;
import com.github.jmeta.defaultextensions.id3v23.impl.ID3v23Extension;
import com.github.jmeta.defaultextensions.lyrics3v2.impl.Lyrics3v2Extension;
import com.github.jmeta.defaultextensions.mp3.impl.MP3Extension;
import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.services.ContainerIterator;
import com.github.jmeta.library.datablocks.api.services.DataBlockAccessor;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerBasedPayload;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.FieldBasedPayload;
import com.github.jmeta.library.datablocks.api.types.Footer;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.services.DataFormatRepository;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.startup.api.services.LibraryJMeta;

/**
 * {@link DataFormatsSmoke_ReverseReading} is a very basic smoke test for
 * testing the jMeta library with specific data formats, but in reverse reading
 * mode.
 */
public class DataFormatsSmoke_ReverseReading {

	private final static Map<File, ContainerDataFormat> TEST_DATA_FORMATS = new LinkedHashMap<>();

	static {
		DataFormatsSmoke_ReverseReading.TEST_DATA_FORMATS.put(new File("./data/smoke/ID3v1.txt"), ID3v1Extension.ID3v1);
		DataFormatsSmoke_ReverseReading.TEST_DATA_FORMATS.put(new File("./data/smoke/ID3v23.txt"),
			ID3v23Extension.ID3v23);
		DataFormatsSmoke_ReverseReading.TEST_DATA_FORMATS.put(new File("./data/smoke/MP3_01.txt"), MP3Extension.MP3);
		DataFormatsSmoke_ReverseReading.TEST_DATA_FORMATS.put(new File("./data/smoke/MP3_02.txt"), MP3Extension.MP3);
		DataFormatsSmoke_ReverseReading.TEST_DATA_FORMATS.put(new File("./data/smoke/MP3_03.txt"), MP3Extension.MP3);
		DataFormatsSmoke_ReverseReading.TEST_DATA_FORMATS.put(new File("./data/smoke/APEv2.txt"), APEv2Extension.APEv2);
		DataFormatsSmoke_ReverseReading.TEST_DATA_FORMATS.put(new File("./data/smoke/Lyrics3v2.txt"),
			Lyrics3v2Extension.LYRICS3v2);
		DataFormatsSmoke_ReverseReading.TEST_DATA_FORMATS.put(new File("./data/smoke/ID3v1.txt"), ID3v1Extension.ID3v1);
	}

	/**
	 * Program entry point.
	 *
	 * @param args No arguments
	 */
	public static void main(String[] args) {

		System.out.println("###################### Starting Data Format [REVERSE] smoke-Test ##################");

		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss z");
		System.out.println(DATE_FORMAT.format(new Date(System.currentTimeMillis())));

		DataFormatsSmoke_ReverseReading dataFormatSmokeTest = new DataFormatsSmoke_ReverseReading();

		dataFormatSmokeTest.runSmokeTest();

		System.out.println("###################### Shutting Down Data Format [REVERSE] smoke-Test ##################");
	}

	private LibraryJMeta m_context;

	private void configureDataFormats() {

		DataFormatRepository repo = m_context.getDataFormatRepository();

		int index = 0;

		for (Iterator<ContainerDataFormat> iterator = repo.getSupportedDataFormats().iterator(); iterator.hasNext();) {
			ContainerDataFormat dataFormat = iterator.next();

			System.out.println("TEST Data format " + index + ": " + dataFormat);

			index++;
		}
	}

	private void printContainers(ContainerIterator containerIterator, Integer level) {

		String prependWith = "";

		for (int i = 0; i < level; i++) {
			prependWith += "\t";
		}

		while (containerIterator.hasNext()) {
			Container nextBlock = containerIterator.next();

			System.out.println(prependWith + "Read Container: " + nextBlock.toString());

			for (int i = 0; i < nextBlock.getHeaders().size(); i++) {
				Header header = nextBlock.getHeaders().get(i);
				System.out.println(prependWith + "\tHeader: " + header);
				printFields(header.getFields(), level + 2);
			}

			for (int i = 0; i < nextBlock.getFooters().size(); i++) {
				Footer footer = nextBlock.getFooters().get(i);
				System.out.println(prependWith + "\tFooter: " + footer);
				printFields(footer.getFields(), level + 2);
			}

			final Payload payload = nextBlock.getPayload();

			System.out.println(prependWith + "\tPayload: " + payload);

			if (payload instanceof ContainerBasedPayload) {
				printContainers(((ContainerBasedPayload) payload).getContainerIterator(), level + 2);
			} else {
				printFields(((FieldBasedPayload) payload).getFields(), level + 2);
			}
		}
	}

	private void printFields(List<Field<?>> fieldList, Integer level) {

		String prependWith = "";

		for (int i = 0; i < level; i++) {
			prependWith += "\t";
		}

		final Iterator<Field<?>> fieldIterator = fieldList.iterator();
		while (fieldIterator.hasNext()) {
			final Field<?> nextField = fieldIterator.next();

			// Ensure that the conversion has been done
			try {
				nextField.getInterpretedValue();
			} catch (BinaryValueConversionException e) {
				e.printStackTrace();
			}

			System.out.println(prependWith + "Read Field: " + nextField);
		}
	}

	private void readAllTopLevelDataBlocks() {

		DataBlockAccessor accessor = m_context.getDataBlockAccessor();

		for (Iterator<File> iterator = DataFormatsSmoke_ReverseReading.TEST_DATA_FORMATS.keySet().iterator(); iterator
			.hasNext();) {
			File currentFile = iterator.next();

			final AbstractMedium<Path> medium = new FileMedium(currentFile.toPath(), true);

			System.out.println("***********************************************************************");
			System.out.println("All data blocks in the AbstractMedium: " + medium);
			System.out.println("***********************************************************************");

			printContainers(accessor.getReverseContainerIterator(medium), Integer.valueOf(1));
		}
	}

	private void runSmokeTest() {

		m_context = LibraryJMeta.getLibrary();

		configureDataFormats();
		readAllTopLevelDataBlocks();
	}
}
