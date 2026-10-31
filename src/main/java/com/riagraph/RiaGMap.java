package com.riagraph;

public class RiaGMap implements AutoCloseable {
    private Value mapVal;

    /**
     * @return Gets the underlying Value for the map
     */
    public Value getValue() {
        return mapVal;
    }

    /**
     * Construct a map from a value
     *
     * @param value the value to construct the map from
     */
    public RiaGMap(Value value) {
        mapVal = value == null ? null : value.clone();
    }

    /**
     * Construct a map literal from a given set of keys/values. The length of the
     * key/value arrays must be the same.
     *
     * @param keys:   The keys in the map
     * @param values: The values in the map
     */
    public RiaGMap(Value[] keys, Value[] values) {
        if (keys.length != values.length) {
            mapVal = null;
            return;
        }
        if (keys.length == 0) {
            mapVal = null;
            return;
        }
        mapVal = Native.riagCreateMap(keys, values);
    }

    private Value getMapKeyOrValue(long index, boolean isKey) {
        if (index < 0 || index >= getNumFields()) {
            return null;
        }
        return isKey ? Native.riagValueGetMapKey(mapVal, index) : Native.riagValueGetMapValue(mapVal, index);
    }

    /**
     * @return The number of fields in the map.
     * @throws RuntimeException If the map has been destroyed.
     */
    public long getNumFields() {
        if (mapVal == null) {
            return 0;
        }
        return Native.riagValueGetMapSize(mapVal);
    }

    /**
     * Get the key at the given index.
     *
     * @param index: The index of the key.
     * @return The key.
     * @throws RuntimeException If the map has been destroyed.
     */
    public Value getKey(long index) {
        return getMapKeyOrValue(index, true);
    }

    /**
     * Get the value at the given index.
     *
     * @param index: The index of the value.
     * @return The value.
     * @throws RuntimeException If the map value has been destroyed.
     */
    public Value getValue(long index) {
        return getMapKeyOrValue(index, false);
    }

    /**
     * Closes this object, relinquishing the underlying value
     *
     * @throws RuntimeException
     */
    public void close() {
        if (mapVal != null && !mapVal.destroyed) {
            mapVal.close();
        }
    }
}
