package com.github.jmeta.utility.testsetup.api.services;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * {@link TestResourceHelper} helps to deal with test resources loaded from
 * external files or streams.
 */
public class TestResourceHelper {

	/**
	 * Tries to convert a given test resource into a {@link File}. The test resource
	 * must be located relative to a given {@link Class} instance. This requires the
	 * resource be bundled with the given .class-File.
	 * 
	 * May throw a runtime {@link InvalidTestDataException} if the given resource
	 * could not be resolved.
	 * 
	 * @param clazz        The {@link Class} instance.
	 * @param resourceName The resource name, may contain relative path parts, as
	 *                     described for the {@link Class#getResource(String)}
	 *                     method.
	 * @return The {@link File} corresponding to the given resource name.
	 * @throws InvalidTestDataException if the given resource could not be resolved.
	 */
	public static Path resourceToFile(Class<?> clazz, String resourceName) {
		Reject.ifNull(resourceName, "resourceName");
		Reject.ifNull(clazz, "clazz");

		URL resourceURL = clazz.getResource(resourceName);

		if (resourceURL == null) {
			throw new InvalidTestDataException(
				"Could not get resource with name: " + resourceName + ", using class: " + clazz.getCanonicalName(),
				null);
		}

		try {
			return Paths.get(resourceURL.toURI());
		} catch (URISyntaxException e) {
			throw new InvalidTestDataException(
				"Could not read resource with name: " + resourceName + ", using class: " + clazz.getCanonicalName(), e);
		}
	}

	/**
	 * Tries to convert a given test resource into an {@link InputStream}. The test
	 * resource must be located relative to a given {@link Class} instance. This
	 * requires the resource be bundled with the given .class-File.
	 * 
	 * @param clazz        The {@link Class} instance.
	 * @param resourceName The resource name, may contain relative path parts, as
	 *                     described for the
	 *                     {@link Class#getResourceAsStream(String)} method.
	 * @return The {@link InputStream} corresponding to the given resource name.
	 * @throws InvalidTestDataException if the given resource could not be resolved.
	 */
	public static InputStream resourceToStream(Class<?> clazz, String resourceName) {
		Reject.ifNull(resourceName, "resourceName");
		Reject.ifNull(clazz, "clazz");

		InputStream resourceAsStream = clazz.getResourceAsStream(resourceName);

		if (resourceAsStream == null) {
			throw new InvalidTestDataException(
				"Could not read resource with name: " + resourceName + ", using class: " + clazz.getCanonicalName(),
				null);
		}

		return resourceAsStream;
	}
}
