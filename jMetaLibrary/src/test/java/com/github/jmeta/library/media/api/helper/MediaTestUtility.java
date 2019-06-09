/**
 *
 * {@link MediaTestUtility}.java
 *
 * @author Jens Ebert
 *
 * @date 17.04.2017
 */
package com.github.jmeta.library.media.api.helper;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link MediaTestUtility} offers general helper methods for testing the media
 * components.
 */
public class MediaTestUtility {

	/**
	 * Reads the content of a file and returns it.
	 * 
	 * @param filePath the file path, must exist
	 * @return contents of the file as byte array.
	 */
	public static byte[] readFileContent(Path filePath) {
		Reject.ifNull(filePath, "filePath");
		Reject.ifFalse(Files.isRegularFile(filePath), "Files.isRegularFile(filePath)");

		try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
			int size = (int) Files.size(filePath);
			byte[] bytesReadBuffer = new byte[size];

			int bytesRead = 0;
			while (bytesRead < size) {
				int readReturn = raf.read(bytesReadBuffer, bytesRead, size - bytesRead);

				if (readReturn == -1) {
					throw new RuntimeException("Unexpected EOF");
				}

				bytesRead += readReturn;
			}

			return bytesReadBuffer;
		} catch (Exception e) {
			throw new RuntimeException("Unexpected exception during reading of file <" + filePath + ">", e);
		}
	}

	private MediaTestUtility() {

	}
}
