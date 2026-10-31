package com.riagraph;

import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Native is a wrapper class for the native library.
 * It is used to load the native library and call the native functions.
 * This class is not intended to be used by end users.
 */
public class Native {
    static {
        try {
            String os_name = "";
            String os_arch;
            String os_name_detect = System.getProperty("os.name").toLowerCase().trim();
            String os_arch_detect = System.getProperty("os.arch").toLowerCase().trim();
            boolean isAndroid = System.getProperty("java.runtime.name", "").toLowerCase().contains("android")
                || System.getProperty("java.vendor", "").toLowerCase().contains("android")
                || System.getProperty("java.vm.name", "").toLowerCase().contains("dalvik");
            switch (os_arch_detect) {
                case "x86_64":
                case "amd64":
                    os_arch = "amd64";
                    break;
                case "aarch64":
                case "arm64":
                    os_arch = "arm64";
                    break;
                case "i386":
                    os_arch = "i386";
                    break;
                default:
                    throw new IllegalStateException("Unsupported system architecture");
            }
            if (isAndroid){
                os_name = "android";
            }
            else if (os_name_detect.startsWith("windows")) {
                os_name = "windows";
            } else if (os_name_detect.startsWith("mac")) {
                os_name = "osx";
            } else if (os_name_detect.startsWith("linux")) {
                os_name = "linux";
            }
            String lib_res_name = "/libriag_java_native.so" + "_" + os_name + "_" + os_arch;

            Path lib_file = Files.createTempFile("libriag_java_native", ".so");
            URL lib_res = Native.class.getResource(lib_res_name);
            if (lib_res == null) {
                throw new IOException(lib_res_name + " not found");
            }
            Files.copy(lib_res.openStream(), lib_file, StandardCopyOption.REPLACE_EXISTING);
            new File(lib_file.toString()).deleteOnExit();
            String lib_path = lib_file.toAbsolutePath().toString();
            System.load(lib_path);
            if (os_name.equals("linux")) {
                riagNativeReloadLibrary(lib_path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Hack: Reload the native library again in JNI bindings to work around the
    // extension loading issue on Linux as System.load() does not set
    // `RTLD_GLOBAL` flag and there is no way to set it in Java.
    protected static native void riagNativeReloadLibrary(String libPath);

    // Database
    protected static native long riagDatabaseInit(String databasePath, long bufferPoolSize,
            boolean enableCompression, boolean readOnly, long maxDbSize, boolean autoCheckpoint,
            long checkpointThreshold, boolean throwOnWalReplayFailure, boolean enableChecksums);

    // TODO: Collapse this back into riagDatabaseInit after published main-repo
    // Java native artifacts have been rebuilt with the extended system_config fields.
    protected static native long riagDatabaseInitExtended(String databasePath, long bufferPoolSize,
            long maxNumThreads,
            boolean enableCompression, boolean readOnly, long maxDbSize, boolean autoCheckpoint,
            long checkpointThreshold, boolean throwOnWalReplayFailure, boolean enableChecksums,
            boolean enableMultiWrites, boolean enableDefaultHashIndex);

    protected static native void riagDatabaseDestroy(Database db);

    protected static native void riagDatabaseSetLoggingLevel(String loggingLevel);

    // Connection
    protected static native long riagConnectionInit(Database database);

    protected static native void riagConnectionDestroy(Connection connection);

    protected static native void riagConnectionSetMaxNumThreadForExec(
            Connection connection, long numThreads);

    protected static native long riagConnectionGetMaxNumThreadForExec(Connection connection);

    protected static native QueryResult riagConnectionQuery(Connection connection, String query);

    protected static native PreparedStatement riagConnectionPrepare(
            Connection connection, String query);

    protected static native QueryResult riagConnectionExecute(
            Connection connection, PreparedStatement preparedStatement, Map<String, Value> param);

    protected static native void riagConnectionInterrupt(Connection connection);

    protected static native void riagConnectionSetQueryTimeout(
            Connection connection, long timeoutInMs);

    protected static native QueryResult riagConnectionCreateArrowTable(Connection connection,
            String tableName, long arrowSchemaAddress, long arrowArraysAddress, long numArrays);

    protected static native QueryResult riagConnectionCreateArrowRelTable(Connection connection,
            String tableName, String srcTableName, String dstTableName, long arrowSchemaAddress,
            long arrowArraysAddress, long numArrays);

    protected static native QueryResult riagConnectionCreateArrowRelTableCSR(Connection connection,
            String tableName, String srcTableName, String dstTableName, long indicesSchemaAddress,
            long indicesArraysAddress, long numIndicesArrays, long indptrSchemaAddress,
            long indptrArraysAddress, long numIndptrArrays, String dstColumnName);

    protected static native QueryResult riagConnectionDropArrowTable(Connection connection,
            String tableName);

    // PreparedStatement
    protected static native void riagPreparedStatementDestroy(PreparedStatement preparedStatement);

    protected static native boolean riagPreparedStatementIsSuccess(PreparedStatement preparedStatement);

    protected static native boolean riagPreparedStatementIsReadOnly(PreparedStatement preparedStatement);

    protected static native String riagPreparedStatementGetErrorMessage(
            PreparedStatement preparedStatement);

    // QueryResult
    protected static native void riagQueryResultDestroy(QueryResult queryResult);

    protected static native boolean riagQueryResultIsSuccess(QueryResult queryResult);

    protected static native String riagQueryResultGetErrorMessage(QueryResult queryResult);

    protected static native long riagQueryResultGetNumColumns(QueryResult queryResult);

    protected static native String riagQueryResultGetColumnName(QueryResult queryResult, long index);

    protected static native DataType riagQueryResultGetColumnDataType(
            QueryResult queryResult, long index);

    protected static native long riagQueryResultGetNumTuples(QueryResult queryResult);

    protected static native QuerySummary riagQueryResultGetQuerySummary(QueryResult queryResult);

    protected static native boolean riagQueryResultHasNext(QueryResult queryResult);

    protected static native FlatTuple riagQueryResultGetNext(QueryResult queryResult);

    protected static native boolean riagQueryResultHasNextQueryResult(QueryResult queryResult);

    protected static native QueryResult riagQueryResultGetNextQueryResult(QueryResult queryResult);

    protected static native String riagQueryResultToString(QueryResult queryResult);

    protected static native void riagQueryResultResetIterator(QueryResult queryResult);

    protected static native void riagQueryResultGetArrowSchema(QueryResult queryResult,
            long arrowSchemaAddress);

    protected static native void riagQueryResultGetNextArrowChunk(QueryResult queryResult,
            long chunkSize, long arrowArrayAddress);

    // FlatTuple
    protected static native void riagFlatTupleDestroy(FlatTuple flatTuple);

    protected static native Value riagFlatTupleGetValue(FlatTuple flatTuple, long index);

    protected static native String riagFlatTupleToString(FlatTuple flatTuple);

    // DataType
    protected static native long riagDataTypeCreate(
            DataTypeID id, DataType childType, long numElementsInArray);

    protected static native DataType riagDataTypeClone(DataType dataType);

    protected static native void riagDataTypeDestroy(DataType dataType);

    protected static native boolean riagDataTypeEquals(DataType dataType1, DataType dataType2);

    protected static native DataTypeID riagDataTypeGetId(DataType dataType);

    protected static native DataType riagDataTypeGetChildType(DataType dataType);

    protected static native long riagDataTypeGetNumElementsInArray(DataType dataType);

    // Value
    protected static native Value riagValueCreateNull();

    protected static native Value riagValueCreateNullWithDataType(DataType dataType);

    protected static native boolean riagValueIsNull(Value value);

    protected static native void riagValueSetNull(Value value, boolean isNull);

    protected static native Value riagValueCreateDefault(DataType dataType);

    protected static native <T> long riagValueCreateValue(T val);

    protected static native Value riagValueClone(Value value);

    protected static native void riagValueCopy(Value value, Value other);

    protected static native void riagValueDestroy(Value value);

    protected static native Value riagCreateMap(Value[] keys, Value[] values);

    protected static native Value riagCreateList(Value[] values);

    protected static native Value riagCreateList(DataType type, long numElements);

    protected static native long riagValueGetListSize(Value value);

    protected static native Value riagValueGetListElement(Value value, long index);

    protected static native long riagValueGetStructNumFields(Value value);

    protected static native Value riagValueGetStructFieldValue(Value value, long index);

    protected static native long riagValueGetMapSize(Value value);

    protected static native Value riagValueGetMapKey(Value value, long index);

    protected static native Value riagValueGetMapValue(Value value, long index);

    protected static native Value riagValueGetRecursiveRelNodeList(Value value);

    protected static native Value riagValueGetRecursiveRelRelList(Value value);

    protected static native DataType riagValueGetDataType(Value value);

    protected static native <T> T riagValueGetValue(Value value);

    protected static native String riagValueToString(Value value);

    protected static native InternalID riagNodeValGetId(Value nodeVal);

    protected static native String riagNodeValGetLabelName(Value nodeVal);

    protected static native long riagNodeValGetPropertySize(Value nodeVal);

    protected static native String riagNodeValGetPropertyNameAt(Value nodeVal, long index);

    protected static native Value riagNodeValGetPropertyValueAt(Value nodeVal, long index);

    protected static native String riagNodeValToString(Value nodeVal);

    protected static native InternalID riagRelValGetId(Value relVal);

    protected static native InternalID riagRelValGetSrcId(Value relVal);

    protected static native InternalID riagRelValGetDstId(Value relVal);

    protected static native String riagRelValGetLabelName(Value relVal);

    protected static native long riagRelValGetPropertySize(Value relVal);

    protected static native String riagRelValGetPropertyNameAt(Value relVal, long index);

    protected static native Value riagRelValGetPropertyValueAt(Value relVal, long index);

    protected static native String riagRelValToString(Value relVal);

    protected static native Value riagCreateStruct(String[] fieldNames, Value[] fieldValues);

    protected static native String riagValueGetStructFieldName(Value structVal, long index);

    protected static native long riagValueGetStructIndex(Value structVal, String fieldName);

    protected static native long riagArrowArrayAllocate(long numArrays);

    protected static native long riagArrowArrayGetAddress(long arrowArraysAddress, long index);

    protected static native void riagArrowArrayFree(long arrowArraysAddress);

    protected static native String riagGetVersion();

    protected static native long riagGetStorageVersion();
}
