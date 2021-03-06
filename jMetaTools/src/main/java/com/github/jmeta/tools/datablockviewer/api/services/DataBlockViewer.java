/**
 *
 * {@link Viewer}.java
 *
 * @author Jens Ebert
 *
 * @date 08.01.2009
 *
 */
package com.github.jmeta.tools.datablockviewer.api.services;

/**
 * {@link DataBlockViewer} tests the usability of the Audio Composition API. It
 * provides a window where users may choose audio files and display all
 * supported meta data within.
 */
public class DataBlockViewer {

	/**
	 * Starts the application.
	 *
	 * @param args Ignored.
	 */
	public static void main(String[] args) {

		DataBlockViewerMainDialog mainDialog = new DataBlockViewerMainDialog();

		mainDialog.init();
	}
}
