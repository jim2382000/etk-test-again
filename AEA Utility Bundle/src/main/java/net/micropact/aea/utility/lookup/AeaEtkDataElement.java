package net.micropact.aea.utility.lookup;

import net.micropact.aea.utility.DataElementType;

public class AeaEtkDataElement {

	Boolean isBoundToLookup;
	String name;
	String mTableName;
	String columnName;
	String businessKey;

	AeaEtkDataObject etkDataObject;
	AeaEtkLookupDefinition etkLookupDefinition;
	DataElementType dataType;


	public Boolean getIsBoundToLookup() {
		return isBoundToLookup;
	}
	public void setIsBoundToLookup(final Boolean isBoundToLookup) {
		this.isBoundToLookup = isBoundToLookup;
	}
	public String getName() {
		return name;
	}
	public void setName(final String name) {
		this.name = name;
	}
	public String getmTableName() {
		return mTableName;
	}
	public void setmTableName(final String mTableName) {
		this.mTableName = mTableName;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(final String columnName) {
		this.columnName = columnName;
	}
	public String getBusinessKey() {
		return businessKey;
	}
	public void setBusinessKey(final String businessKey) {
		this.businessKey = businessKey;
	}
	public AeaEtkDataObject getEtkDataObject() {
		return etkDataObject;
	}
	public void setEtkDataObject(final AeaEtkDataObject etkDataObject) {
		this.etkDataObject = etkDataObject;
	}
	public AeaEtkLookupDefinition getEtkLookupDefinition() {
		return etkLookupDefinition;
	}
	public void setEtkLookupDefinition(final AeaEtkLookupDefinition etkLookupDefinition) {
		this.etkLookupDefinition = etkLookupDefinition;
	}
	public DataElementType getDataType() {
		return dataType;
	}
	public void setDataType(final DataElementType dataType) {
		this.dataType = dataType;
	}
}
