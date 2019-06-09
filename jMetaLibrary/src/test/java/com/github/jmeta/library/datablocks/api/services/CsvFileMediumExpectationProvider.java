/**
 *
 * {@link CsvFileMediumExpectationProvider}.java
 *
 * @author Jens Ebert
 *
 * @date 29.05.2011
 */
package com.github.jmeta.library.datablocks.api.services;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jmeta.library.dataformats.api.services.DataFormatRepository;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.Flags;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.utility.byteutils.api.exceptions.InvalidArrayStringFormatException;
import com.github.jmeta.utility.byteutils.api.services.ByteArrayUtils;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.csv.api.exceptions.CsvRowFormatException;
import com.github.jmeta.utility.csv.api.services.CsvReader;
import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.namedio.api.services.NamedReader;

/**
 * {@link CsvFileMediumExpectationProvider} reads test data from a single csv
 * file and returns it accordingly.
 */
public class CsvFileMediumExpectationProvider extends AbstractMediumExpectationProvider {

	private static final String FLAG_SEPARATOR = "&&";

	private static final String COL_DATA_FORMAT = "DataFormat";

	private static final String COL_ID = "Id";

	private static final String COL_SEQU_NO = "Sequ_No";

	private static final String COL_PARENT_ROW_INDEX = "Parent_Row_Index";

	private static final String COL_EXP_BLOCK_SIZE = "Exp_Block_Size";

	private static final String COL_EXP_FIELD_INTERPRETED_VALUE = "Exp_Field_Interpreted_Value";

	private static final String COL_EXP_FAILED_FIELD_CONV_CHARSET = "Exp_Failed_Field_Conv_Charset";

	private static final String COL_EXP_FAILED_FIELD_CONV_BYTE_ORDER = "Exp_Failed_Field_Conv_ByteOrder";

	private static final String COL_CONTAINER_BACKWARD_READABLE = "Container_Backward_Readable";

	private static final String COL_COMMENT = "Comment";

	private static final Set<String> CSV_READER_COLUMNS = new LinkedHashSet<>();

	static {
		CsvFileMediumExpectationProvider.CSV_READER_COLUMNS.add(CsvFileMediumExpectationProvider.COL_DATA_FORMAT);
		CsvFileMediumExpectationProvider.CSV_READER_COLUMNS.add(CsvFileMediumExpectationProvider.COL_ID);
		CsvFileMediumExpectationProvider.CSV_READER_COLUMNS.add(CsvFileMediumExpectationProvider.COL_SEQU_NO);
		CsvFileMediumExpectationProvider.CSV_READER_COLUMNS.add(CsvFileMediumExpectationProvider.COL_PARENT_ROW_INDEX);
		CsvFileMediumExpectationProvider.CSV_READER_COLUMNS.add(CsvFileMediumExpectationProvider.COL_EXP_BLOCK_SIZE);
		CsvFileMediumExpectationProvider.CSV_READER_COLUMNS
			.add(CsvFileMediumExpectationProvider.COL_EXP_FIELD_INTERPRETED_VALUE);
		CsvFileMediumExpectationProvider.CSV_READER_COLUMNS
			.add(CsvFileMediumExpectationProvider.COL_EXP_FAILED_FIELD_CONV_CHARSET);
		CsvFileMediumExpectationProvider.CSV_READER_COLUMNS
			.add(CsvFileMediumExpectationProvider.COL_EXP_FAILED_FIELD_CONV_BYTE_ORDER);
		CsvFileMediumExpectationProvider.CSV_READER_COLUMNS
			.add(CsvFileMediumExpectationProvider.COL_CONTAINER_BACKWARD_READABLE);
		CsvFileMediumExpectationProvider.CSV_READER_COLUMNS.add(CsvFileMediumExpectationProvider.COL_COMMENT);
	}

	private static final CsvReader CSV_READER = new CsvReader(CsvFileMediumExpectationProvider.CSV_READER_COLUMNS);

	/**
	 * Loads the contents of the given csv file into memory.
	 *
	 * @param csvFile The csv file.
	 * @return A {@link List} of csv rows.
	 * @throws IOException           If an I/O error occured during loading.
	 * @throws CsvRowFormatException If the csv format of the file is invalid.
	 */
	private static List<Map<String, String>> loadCsvFileContents(Path csvFile)
		throws IOException, CsvRowFormatException {

		CsvFileMediumExpectationProvider.CSV_READER
			.setNewResource(NamedReader.createFromFile(csvFile.toFile(), Charsets.CHARSET_ASCII));

		List<Map<String, String>> csvRows = new ArrayList<>();
		Map<String, String> row = null;

		// Skip header row
		CsvFileMediumExpectationProvider.CSV_READER.readNextRow(false);

		while ((row = CsvFileMediumExpectationProvider.CSV_READER.readNextRow(false)) != null) {
			csvRows.add(row);
		}

		return csvRows;
	}

	/**
	 * Processes (i.e. reads, validates and optionally stores) the
	 * {@link #COL_SEQU_NO} column of the csv file for the given row.
	 *
	 * @param nextRow The row in the csv file, column contents mapped to column
	 *                names.
	 * @return The sequence number as read from the column.
	 */
	private static int processSequenceNumber(Map<String, String> nextRow) {

		String sequNo = nextRow.get(CsvFileMediumExpectationProvider.COL_SEQU_NO);

		int sequenceNumber = Integer.parseInt(sequNo);
		return sequenceNumber;
	}

	private final List<DataBlockInstanceId> expectedTopLevelContainers = new ArrayList<>();

	private final List<DataBlockInstanceId> expectedTopLevelContainersReverse = new ArrayList<>();

	private final Map<DataBlockInstanceId, Long> expectedDataBlockSizes = new HashMap<>();

	private final Map<DataBlockInstanceId, Object> expectedFieldInterpretedValues = new HashMap<>();

	private final Map<DataBlockInstanceId, Map<PhysicalDataBlockType, List<DataBlockInstanceId>>> expectedParentChildMap = new HashMap<>();

	private final Map<DataBlockInstanceId, ExpectedFailedFieldConversionData> expectedFailedFieldConversionData = new HashMap<>();

	/**
	 * Creates a new {@link CsvFileMediumExpectationProvider}.
	 * 
	 * @param repository          The {@link DataFormatRepository}.
	 * @param testFile            The test file that is read. Necessary for reading
	 *                            the expected bytes.
	 * @param expectedDataCsvFile The csv file with expected test results, must
	 *                            contain specific columns.
	 * @throws InvalidTestDataCsvFormatException if the csv file is incorrect.
	 */
	public CsvFileMediumExpectationProvider(DataFormatRepository repository, Path testFile, Path expectedDataCsvFile)
		throws InvalidTestDataCsvFormatException {
		super(repository, testFile);

		Reject.ifNull(expectedDataCsvFile, "csvFile");
		Reject.ifFalse(Files.isRegularFile(expectedDataCsvFile), "Files.isRegularFile(expectedDataCsvFile)");

		String absolutePath = expectedDataCsvFile.toAbsolutePath().toString();
		try {
			List<Map<String, String>> csvRows = CsvFileMediumExpectationProvider
				.loadCsvFileContents(expectedDataCsvFile);

			processCsvData(csvRows);
		}

		catch (InvalidArrayStringFormatException e) {
			throw new InvalidTestDataCsvFormatException(
				"Invalid array string format detected in csv file <" + absolutePath + ">.", e);
		} catch (CsvRowFormatException e) {
			throw new InvalidTestDataCsvFormatException(
				"Invalid row format detected in csv file <" + absolutePath + ">.", e);
		} catch (IOException e) {
			throw new RuntimeException("Could not read csv file <" + absolutePath + ">.", e);
		}

		finally {
			try {
				CsvFileMediumExpectationProvider.CSV_READER.closeCurrentCsvResource();
			} catch (IOException e) {
				throw new RuntimeException("Could not close csv reader for file <" + absolutePath + ">.", e);
			}
		}
	}

	/**
	 * Adds the given instance id to the internal parent child map.
	 *
	 * @param instanceId The {@link DataBlockInstanceId} to add.
	 * @param blockType  The {@link PhysicalDataBlockType} of the corresponding data
	 *                   block.
	 */
	private void addInstanceId(DataBlockInstanceId instanceId, PhysicalDataBlockType blockType) {

		DataBlockInstanceId parentInstanceId = instanceId.getParentInstanceId();
		if (!expectedParentChildMap.containsKey(parentInstanceId)) {
			expectedParentChildMap.put(parentInstanceId,
				new HashMap<PhysicalDataBlockType, List<DataBlockInstanceId>>());
		}

		if (!expectedParentChildMap.get(parentInstanceId).containsKey(blockType)) {
			expectedParentChildMap.get(parentInstanceId).put(blockType, new ArrayList<DataBlockInstanceId>());
		}

		expectedParentChildMap.get(parentInstanceId).get(blockType).add(instanceId);
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.AbstractMediumExpectationProvider#getExpectedChildBlocksOfType(com.github.jmeta.library.datablocks.api.services.DataBlockInstanceId,
	 *      com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType)
	 */
	@Override
	public List<DataBlockInstanceId> getExpectedChildBlocksOfType(DataBlockInstanceId parentBlock,
		PhysicalDataBlockType blockType) {

		if (!expectedParentChildMap.containsKey(parentBlock)) {
			return new ArrayList<>();
		}

		if (!expectedParentChildMap.get(parentBlock).containsKey(blockType)) {
			return new ArrayList<>();
		}

		return expectedParentChildMap.get(parentBlock).get(blockType);
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.AbstractMediumExpectationProvider#getExpectedDataBlockSize(com.github.jmeta.library.datablocks.api.services.DataBlockInstanceId)
	 */
	@Override
	public long getExpectedDataBlockSize(DataBlockInstanceId dataBlockId) {

		return expectedDataBlockSizes.get(dataBlockId);
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.AbstractMediumExpectationProvider#getExpectedFailingFieldConversions(com.github.jmeta.library.datablocks.api.services.DataBlockInstanceId)
	 */
	@Override
	public ExpectedFailedFieldConversionData getExpectedFailingFieldConversions(DataBlockInstanceId fieldInstance) {

		return expectedFailedFieldConversionData.get(fieldInstance);
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.AbstractMediumExpectationProvider#getExpectedFieldInterpretedValue(com.github.jmeta.library.datablocks.api.services.DataBlockInstanceId)
	 */
	@Override
	public Object getExpectedFieldInterpretedValue(DataBlockInstanceId fieldInstanceId) {

		return expectedFieldInterpretedValues.get(fieldInstanceId);
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.AbstractMediumExpectationProvider#getExpectedTopLevelContainers()
	 */
	@Override
	public List<DataBlockInstanceId> getExpectedTopLevelContainers() {

		return expectedTopLevelContainers;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.AbstractMediumExpectationProvider#getExpectedTopLevelContainersReverse()
	 */
	@Override
	public List<DataBlockInstanceId> getExpectedTopLevelContainersReverse() {

		return expectedTopLevelContainersReverse;
	}

	/**
	 * Processes (i.e. reads, validates and optionally stores) the
	 * {@link #COL_CONTAINER_BACKWARD_READABLE} column of the csv file for the given
	 * row.
	 *
	 * @param nextRow    The row in the csv file, column contents mapped to column
	 *                   names.
	 * @param instanceId The {@link DataBlockInstanceId} of the current row.
	 * @param rowPrefix  The row prefix.
	 */
	private void processContainerReverseReadable(Map<String, String> nextRow, DataBlockInstanceId instanceId,
		String rowPrefix) {

		String isContainerBackwardReadbleString = nextRow
			.get(CsvFileMediumExpectationProvider.COL_CONTAINER_BACKWARD_READABLE);

		DataFormatSpecification spec = getDataFormatSpecification(instanceId.getId().getDataFormat());

		if (topLevelDataBlocksToMap(spec).containsKey(instanceId.getId())) {
			boolean isContainerBackwardReadble = Boolean.parseBoolean(isContainerBackwardReadbleString);

			// Each new container must be added AT FRONT of the list because it represents
			// the order in which the containers are expected when backwards reading. And
			// this
			// order is of course exactly the reverse order to the read order of the csv
			// file.
			if (isContainerBackwardReadble) {
				expectedTopLevelContainersReverse.add(0, instanceId);
			}
		}
	}

	/**
	 * Processes csv data in the given rows.
	 * 
	 * @param csvRows The {@link List} of csv rows.
	 *
	 * @throws InvalidTestDataCsvFormatException If test data in the csv file is
	 *                                           invalid.
	 * @throws InvalidArrayStringFormatException If the format of an array string
	 *                                           expression in the csv file is
	 *                                           invalid.
	 */
	private void processCsvData(List<Map<String, String>> csvRows)
		throws InvalidTestDataCsvFormatException, InvalidArrayStringFormatException {

		List<DataBlockInstanceId> instanceIds = new ArrayList<>();

		// For fast error analysis, the correct row numbers must be given in error
		// messages.
		// Row numbers in text editors are 1-based, therefore the row index must be add
		// by 1. Furthermore, the header row is not contained in the csvRows list,
		// therefore the row indices must be add by 1 again.
		int rowOffset = 2;

		for (int rowCounter = 0; rowCounter < csvRows.size(); ++rowCounter) {
			Map<String, String> nextRow = csvRows.get(rowCounter);

			String rowPrefix = "[ROW " + (rowCounter + rowOffset) + " of csv resource <"
				+ CsvFileMediumExpectationProvider.CSV_READER.getCurrentCsvResource().getName() + ">] - ";

			ContainerDataFormat dataFormat = processDataFormat(nextRow, rowPrefix);

			DataBlockId id = processId(dataFormat, nextRow, rowPrefix);

			int sequenceNumber = CsvFileMediumExpectationProvider.processSequenceNumber(nextRow);

			DataBlockInstanceId instanceId = processParentRowIndex(nextRow, id, sequenceNumber, instanceIds, rowPrefix);

			processExpectedBlockSize(nextRow, instanceId);

			processExpectedFieldInterpretedValue(nextRow, id, instanceId, rowPrefix);

			processExpectedFailedFieldConversion(nextRow, instanceId, rowPrefix);

			processContainerReverseReadable(nextRow, instanceId, rowPrefix);
		}
	}

	/**
	 * Processes (i.e. reads, validates and optionally stores) the
	 * {@link #COL_DATA_FORMAT} column of the csv file for the given row.
	 * 
	 * @param row       The row in the csv file, column contents mapped to column
	 *                  names.
	 * @param rowPrefix A string prefix describing the current row for informational
	 *                  output in error messages.
	 *
	 * @return The {@link ContainerDataFormat}.
	 * @throws InvalidTestDataCsvFormatException If test data in the csv file is
	 *                                           invalid.
	 */
	private ContainerDataFormat processDataFormat(Map<String, String> row, String rowPrefix)
		throws InvalidTestDataCsvFormatException {

		Set<ContainerDataFormat> dataFormats = getSupportedDataFormats();

		String dfName = row.get(CsvFileMediumExpectationProvider.COL_DATA_FORMAT);

		for (Iterator<ContainerDataFormat> iterator = dataFormats.iterator(); iterator.hasNext();) {
			ContainerDataFormat dataFormat2 = iterator.next();

			if (dataFormat2.getName().equals(dfName)) {
				return dataFormat2;
			}
		}

		throw new InvalidTestDataCsvFormatException(
			rowPrefix + "No data format found for name <" + dfName + ">. Must be one of <" + dataFormats + ">.", null);
	}

	/**
	 * Processes (i.e. reads, validates and optionally stores) the
	 * {@link #COL_EXP_BLOCK_SIZE} column of the csv file for the given row.
	 *
	 * @param row        The row in the csv file, column contents mapped to column
	 *                   names.
	 * @param instanceId The {@link DataBlockInstanceId} of the current row.
	 */
	private void processExpectedBlockSize(Map<String, String> row, DataBlockInstanceId instanceId) {

		String expBlockSizeString = row.get(CsvFileMediumExpectationProvider.COL_EXP_BLOCK_SIZE);

		expectedDataBlockSizes.put(instanceId, Long.parseLong(expBlockSizeString));
	}

	/**
	 * Processes (i.e. reads, validates and optionally stores) the
	 * {@link #COL_EXP_FAILED_FIELD_CONV_BYTE_ORDER} and
	 * {@link #COL_EXP_FAILED_FIELD_CONV_CHARSET} columns of the csv file for the
	 * given row.
	 *
	 * @param row        The row in the csv file, column contents mapped to column
	 *                   names.
	 * @param instanceId The {@link DataBlockInstanceId} of the current row.
	 * @param rowPrefix  The row prefix.
	 * @throws InvalidTestDataCsvFormatException if the failed field conversion data
	 *                                           could not be parsed correctly.
	 */
	private void processExpectedFailedFieldConversion(Map<String, String> row, DataBlockInstanceId instanceId,
		String rowPrefix) throws InvalidTestDataCsvFormatException {

		String failingConversionCharsetString = row
			.get(CsvFileMediumExpectationProvider.COL_EXP_FAILED_FIELD_CONV_CHARSET);
		String failingConversionByteOrderString = row
			.get(CsvFileMediumExpectationProvider.COL_EXP_FAILED_FIELD_CONV_BYTE_ORDER);

		if ((!failingConversionCharsetString.isEmpty() && failingConversionByteOrderString.isEmpty())
			|| (failingConversionCharsetString.isEmpty() && !failingConversionByteOrderString.isEmpty())) {
			throw new InvalidTestDataCsvFormatException(
				rowPrefix + "If a failing field conversion is expected, both character encoding (column "
					+ CsvFileMediumExpectationProvider.COL_EXP_FAILED_FIELD_CONV_CHARSET + ") and byte order (column "
					+ CsvFileMediumExpectationProvider.COL_EXP_FAILED_FIELD_CONV_BYTE_ORDER + ") must be given",
				null);
		}

		if (!failingConversionByteOrderString.isEmpty()) {
			Charset failingConversionCharset = Charset.forName(failingConversionCharsetString);
			ByteOrder failingConversionByteOrder = null;

			if (failingConversionByteOrderString.equals(ByteOrder.BIG_ENDIAN.toString())) {
				failingConversionByteOrder = ByteOrder.BIG_ENDIAN;
			} else if (failingConversionByteOrderString.equals(ByteOrder.LITTLE_ENDIAN.toString())) {
				failingConversionByteOrder = ByteOrder.LITTLE_ENDIAN;
			} else {
				throw new InvalidTestDataCsvFormatException(rowPrefix
					+ "Failed field conversion requires a byte order as value. Byte Order with name <"
					+ failingConversionByteOrderString + "> is unknown. Only BIG_ENDIAN and LITTLE_ENDIAN are allowed",
					null);
			}

			expectedFailedFieldConversionData.put(instanceId,
				new ExpectedFailedFieldConversionData(failingConversionCharset, failingConversionByteOrder));
		}
	}

	/**
	 * Processes (i.e. reads, validates and optionally stores) the
	 * {@link #COL_EXP_FIELD_INTERPRETED_VALUE} column of the csv file for the given
	 * row.
	 *
	 * @param row        The row in the csv file, column contents mapped to column
	 *                   names.
	 * @param id         The {@link DataBlockId} of the current rows data block for
	 *                   which to determine the expected field interpreted value.
	 * @param instanceId The {@link DataBlockInstanceId} of the current row.
	 * @param rowPrefix  A string prefix describing the current row for
	 *                   informational output in error messages.
	 * @throws InvalidTestDataCsvFormatException If test data in the csv file is
	 *                                           invalid.
	 * @throws InvalidArrayStringFormatException If the format of an array string
	 *                                           expression in the csv file is
	 *                                           invalid.
	 */
	private void processExpectedFieldInterpretedValue(Map<String, String> row, DataBlockId id,
		DataBlockInstanceId instanceId, String rowPrefix)
		throws InvalidTestDataCsvFormatException, InvalidArrayStringFormatException {

		String expInterpretedFieldValueString = row
			.get(CsvFileMediumExpectationProvider.COL_EXP_FIELD_INTERPRETED_VALUE);

		DataFormatSpecification spec = getDataFormatSpecification(id.getDataFormat());

		PhysicalDataBlockType blockType = spec.getDataBlockDescription(id).getPhysicalType();

		if (blockType.equals(PhysicalDataBlockType.FIELD)) {
			FieldProperties<?> fieldProps = spec.getDataBlockDescription(id).getFieldProperties();

			if (expInterpretedFieldValueString.equals(AbstractMediumExpectationProvider.ANY_WILDCARD)) {
				expectedFieldInterpretedValues.put(instanceId, expInterpretedFieldValueString);
			} else if (fieldProps.getFieldType().equals(FieldType.STRING)) {
				expectedFieldInterpretedValues.put(instanceId, expInterpretedFieldValueString);
			} else if (fieldProps.getFieldType().equals(FieldType.UNSIGNED_WHOLE_NUMBER)) {
				expectedFieldInterpretedValues.put(instanceId, Long.parseLong(expInterpretedFieldValueString));
			} else if (fieldProps.getFieldType().equals(FieldType.FLAGS)) {
				FlagSpecification flagSpec = fieldProps.getFlagSpecification();

				Flags expectedFlags = new Flags(flagSpec);

				String[] flagsToSet = expInterpretedFieldValueString
					.split(CsvFileMediumExpectationProvider.FLAG_SEPARATOR);

				for (int i = 0; i < flagsToSet.length; i++) {
					String flagName = flagsToSet[i];

					if (flagName.isEmpty()) {
						continue;
					}

					if (!flagSpec.hasFlag(flagName)) {
						throw new InvalidTestDataCsvFormatException(rowPrefix + "The flag specification <" + flagSpec
							+ "> does not specify a flag named <" + flagName + ">.", null);
					}

					expectedFlags.setFlag(flagName, true);
				}

				expectedFieldInterpretedValues.put(instanceId, expectedFlags);
			}

			// Binary data in the csv file is represented by a Java like array notation and
			// parsed accordingly
			else if (fieldProps.getFieldType().equals(FieldType.BINARY)) {
				byte[] parsedBytes;
				try {
					parsedBytes = ByteArrayUtils.parseArray(expInterpretedFieldValueString);
				} catch (InvalidArrayStringFormatException e) {
					// Throw further, enriched with row index information
					throw new InvalidArrayStringFormatException(rowPrefix + e.getMessage());
				}
				expectedFieldInterpretedValues.put(instanceId, parsedBytes);
			}
		}
	}

	/**
	 * Processes (i.e. reads, validates and optionally stores) the {@link #COL_ID}
	 * column of the csv file for the given row.
	 *
	 * @param dataFormat The {@link ContainerDataFormat} expected in the data.
	 * @param row        The row in the csv file, column contents mapped to column
	 *                   names.
	 * @param rowPrefix  A string prefix describing the current row for
	 *                   informational output in error messages.
	 * @return The {@link DataBlockId} as read from the column.
	 * @throws InvalidTestDataCsvFormatException If test data in the csv file is
	 *                                           invalid.
	 */
	private DataBlockId processId(ContainerDataFormat dataFormat, Map<String, String> row, String rowPrefix)
		throws InvalidTestDataCsvFormatException {

		String idString = row.get(CsvFileMediumExpectationProvider.COL_ID);

		DataFormatSpecification spec = getDataFormatSpecification(dataFormat);

		DataBlockId id = new DataBlockId(dataFormat, idString);

		if (!spec.specifiesBlockWithId(id)) {
			throw new InvalidTestDataCsvFormatException(
				rowPrefix + "Data format <" + dataFormat + "> does not specify a block with id <" + id + ">.", null);
		}
		return id;
	}

	/**
	 * Processes (i.e. reads, validates and optionally stores) the
	 * {@link #COL_PARENT_ROW_INDEX} column of the csv file for the given row.
	 *
	 * @param row            The row in the csv file, column contents mapped to
	 *                       column names.
	 * @param id             The {@link DataBlockId} of the current rows data block
	 *                       for which to determine the {@link DataBlockInstanceId}
	 *                       based on the parent row index.
	 * @param sequenceNumber The sequence number of the current rows data block.
	 * @param instanceIds    A list of all previously read
	 *                       {@link DataBlockInstanceId}s where the created
	 *                       {@link DataBlockInstanceId} for the current row is
	 *                       added.
	 * @param rowPrefix      A string prefix describing the current row for
	 *                       informational output in error messages.
	 * @return The {@link DataBlockInstanceId} as read from the column.
	 * @throws InvalidTestDataCsvFormatException If test data in the csv file is
	 *                                           invalid.
	 */
	private DataBlockInstanceId processParentRowIndex(Map<String, String> row, DataBlockId id, int sequenceNumber,
		List<DataBlockInstanceId> instanceIds, String rowPrefix) throws InvalidTestDataCsvFormatException {

		String parentRow = row.get(CsvFileMediumExpectationProvider.COL_PARENT_ROW_INDEX);

		DataFormatSpecification spec = getDataFormatSpecification(id.getDataFormat());

		PhysicalDataBlockType blockType = spec.getDataBlockDescription(id).getPhysicalType();

		DataBlockInstanceId instanceId = null;

		if (parentRow.isEmpty()) {
			instanceId = new DataBlockInstanceId(id, null, sequenceNumber);
			instanceIds.add(instanceId);
		}

		else {
			int parentRowIndex = Integer.parseInt(parentRow);

			if (parentRowIndex > instanceIds.size()) {
				throw new InvalidTestDataCsvFormatException(rowPrefix + "Specified parent row index <"
					+ (parentRowIndex - 1) + "> must be smaller then the current row index.", null);
			}

			DataBlockInstanceId parentInstanceId = instanceIds.get(parentRowIndex - 1);
			instanceId = new DataBlockInstanceId(id, parentInstanceId, sequenceNumber);
			instanceIds.add(instanceId);
			addInstanceId(instanceId, blockType);
		}

		if (topLevelDataBlocksToMap(spec).containsKey(id)) {
			expectedTopLevelContainers.add(instanceId);
		}

		return instanceId;
	}

	/**
	 * @param spec The {@link DataFormatSpecification}
	 * @return A map representation of top-level data blocks
	 */
	private Map<DataBlockId, DataBlockDescription> topLevelDataBlocksToMap(DataFormatSpecification spec) {
		return spec.getTopLevelDataBlockDescriptions().stream()
			.collect(Collectors.toMap(desc -> desc.getId(), desc -> desc));
	}
}
