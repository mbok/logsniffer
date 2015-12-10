package com.logsniffer.fields.path;

import com.logsniffer.fields.FieldsMap;

public interface ExpandedAccessBuilder {
	<T> ExpandedAccess<T> buildAccess(FieldsMap fields, String path, Class<T> desiredClass);
}
