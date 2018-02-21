package com.parserbox.vendors.intuit.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.parserbox.model.sso.OAuthAccessToken;
import com.parserbox.vendors.ImporterAdapter;
import org.apache.commons.codec.net.URLCodec;


import au.com.bytecode.opencsv.CSVWriter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import oauth.signpost.OAuthConsumer;
import org.apache.commons.lang3.StringUtils;

public class Quickbooks extends ImporterAdapter {

	/**
	 * API id we use to differentiate from other external systems.
	 */
	public static final String appid = "QuickBooksOnline";
	
	/**
	 * The maximum number of records (for lists) to return per call. (Radar
	 * max is 1000)
	 */
	long MAX_QUERY_RESULTS = 100;

	
	/**
	 * Radar OAuth constructor
	 *
	 * @param authCallBackUrl
	 * @throws Exception
	 */
	public Quickbooks(OAuthAccessToken tokenObject, String authCallBackUrl) throws Exception {
		super(tokenObject, authCallBackUrl);
	}

	/**
	 * Starts the import process.
	 * @param loanid
	 * @throws Exception
	 */
	public void importData(String loanid) throws Exception {
       super.importData(loanid);
	}
	
	/**
	 * Quick check to see if we are authenticated.
	 * @return
	 * @throws Exception
	 */
	public boolean isOkToProceed() throws Exception {
		if (this.getTokenObject() == null) return false;
		
		// Quick API call - throws an exception if not authenticated.
		getRecordCount("SELECT COUNT(*) FROM INVOICE");
		
		return true;
	}
	

	/**
	 * Imports debtor records.
	 * 
	 * @throws Exception
	 */
	public void importDebtors(String loanid) throws Exception {
		long totalRecords = getRecordCount("SELECT COUNT(*) FROM CUSTOMER");
		List<JSONObject> objects = getListFromService("Customer", "SELECT * FROM CUSTOMER ", totalRecords);
		if (objects.size() == 0)
			return;
		try {
			List<String[]> records = new ArrayList<>();
			for (JSONObject jObject : objects) {

				String Id = " ";
				String CompanyName = " ";
				String GivenName = " ";
				String FamilyName = " ";
				String Line1 = " ";
				String City = " ";
				String CountrySubDivisionCode = " ";
				String PostalCode = " ";
				String Country = " ";
				String Address = " ";
				String FreeFormNumber = " ";

				Id = getValue(jObject, "Id");
				CompanyName = getValue(jObject, "CompanyName", "FullyQualifiedName");
				GivenName = getValue(jObject, "GivenName");
				FamilyName = getValue(jObject, "FamilyName");
				if (jObject.containsKey("BillAddr")) {
					JSONObject BillAddr = jObject.getJSONObject("BillAddr");
					Line1 = getValue(BillAddr, "Line1");
					City = getValue(BillAddr, "City");
					CountrySubDivisionCode = getValue(BillAddr, "CountrySubDivisionCode");
					PostalCode = getValue(BillAddr, "PostalCode");
					Country = getValue(BillAddr, "Country");
				}
				if (jObject.containsKey("PrimaryEmailAddr")) {
					JSONObject PrimaryEmailAddr = jObject.getJSONObject("PrimaryEmailAddr");
					Address = PrimaryEmailAddr.getString("Address");
				}
				if (jObject.containsKey("PrimaryPhone")) {
					JSONObject PrimaryPhone = jObject.getJSONObject("PrimaryPhone");
					FreeFormNumber = PrimaryPhone.getString("FreeFormNumber");
				}
				
				if (useDebtorNameAsId) Id = CompanyName;
				
				String[] values = new String[] {
						Id, CompanyName, Line1, " ", City, CountrySubDivisionCode, PostalCode, Country,
						GivenName + " " + FamilyName, FreeFormNumber, Address, " ", " ", " "
				};
				records.add(values);
				
			}
			if (records.size() > 0) {
				uploadData(records, "debtors", loanid);
			}
		} catch (Exception e) {
			throw e;
		}
	}
	

	
	
	
	/**
	 * Imports the aging receivables.
	 * 
	 * @throws Exception
	 */
	public void importAgingReceivablesReport(String loanid) throws Exception {
		
		List<String[]> records = new ArrayList<>();
		
		SimpleDateFormat destDateFormatter = new SimpleDateFormat("MM-dd-yyyy");
		SimpleDateFormat srceDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String queryString = "report_date=" + df.format(new Date());
		//queryString += "&columns=tx_date,doc_num,cust_name,due_date,subt_amount";
		JSONObject jsonObject = getReportObjectFromService("AgedReceivableDetail?" + queryString);
		if (jsonObject == null) return;
		if (! jsonObject.containsKey("Rows")) return;
		if (! jsonObject.getJSONObject("Rows").containsKey("Row")) return;

		JSONArray rowsArray = jsonObject.getJSONObject("Rows").getJSONArray("Row");
		try {
			for (Iterator<JSONObject> iterator1 = rowsArray.iterator(); iterator1.hasNext();) {
				JSONObject obj = iterator1.next();

				if (! obj.containsKey("Rows")) continue;
				JSONObject rows1 = obj.getJSONObject("Rows");
				if (! rows1.containsKey("Row")) continue;

				JSONArray colsArray = rows1.getJSONArray("Row");
				for (Iterator<JSONObject> iterator2 = colsArray.iterator(); iterator2.hasNext();) {

					JSONObject obj2 = iterator2.next();
					if (! obj2.containsKey("ColData")) continue;

					JSONArray ColData = obj2.getJSONArray("ColData");

					String  date = ColData.getJSONObject(0).getString("value");
					String  trantype = ColData.getJSONObject(1).getString("value");
					String  docid = ColData.getJSONObject(2).getString("value");
					String 	CompanyName = ColData.getJSONObject(3).getString("value");
					String 	Id = ColData.getJSONObject(3).getString("id");
					String  duedate = ColData.getJSONObject(4).getString("value");
					String  amount = ColData.getJSONObject(5).getString("value");
					String  balance = ColData.getJSONObject(6).getString("value");

					if (StringUtils.isBlank(date)) continue;
					if (StringUtils.isBlank(Id)) continue;
					if (StringUtils.isBlank(balance)) continue;

					// Determine if this is an unapplied payment / credit
					if (StringUtils.isBlank(docid)) {
						docid = "Unapplied CR - " + new Date().getTime();

					}
					// Convert dates to our format

					if (org.apache.commons.lang3.StringUtils.isNotBlank(date)) {
						Date dt = srceDateFormatter.parse(date);
						if (dt != null) {
							date = destDateFormatter.format(dt);
						}
					}
					if (org.apache.commons.lang3.StringUtils.isNotBlank(duedate)) {
						Date dt = srceDateFormatter.parse(duedate);
						if (dt != null) {
							duedate = destDateFormatter.format(dt);
						}
					}
					if (this.useDebtorNameAsId) Id = CompanyName;


					if(StringUtils.isNotBlank(trantype)) {
						if (trantype.length() > 2) {
							trantype = trantype.substring(0, 2).toUpperCase();
						}
					}


					if(StringUtils.isNotBlank(trantype)) {
						if (trantype.length() > 2) {
							trantype = trantype.substring(0, 2).toUpperCase();
						}
					}

	            	String debtorid_alt = StringUtils.isEmpty(Id) ? " " : Id;
                	String debtor_name 	= StringUtils.isEmpty(CompanyName) ? " " : CompanyName;
                	String address1 	= " ";
                	String address2 	= " ";
                	String city 		= " ";
                	String state 		= " ";
                	String zip 			= " ";
                	String country 		= " ";
                	String inv_nm 		= StringUtils.isEmpty(docid) ? " " : docid;
                	String inv_dt 		= StringUtils.isEmpty(date) ? " " : date;
                	String terms 		= " ";
                	String trancode 	= " ";
                	String inv_am 		= StringUtils.isEmpty(balance) ? " " : balance;
                    String po 			= " ";
                    String inel 		= " ";
                    String inel_am 		= " ";
                    String due_dt 		= StringUtils.isEmpty(duedate) ? " " : duedate;

					String[] values = new String[] { debtorid_alt, debtor_name, address1, address2, city, state, zip,
							country, inv_nm, inv_dt, terms, trancode, inv_am, po, inel, inel_am, due_dt
					};
					records.add(values);
				}
			}
			if (records.size() > 0) {
				uploadData(records, "transactions", loanid);
			}
		}
		catch (Exception e) {
			throw e;
		}
	}


	/**
	 * Imports vendor records.
	 *
	 * @throws Exception
	 */
	public void importVendors(String loanid) throws Exception {
		long totalRecords = getRecordCount("SELECT COUNT(*) FROM VENDOR");
		List<JSONObject> objects = getListFromService("Vendor", "SELECT * FROM VENDOR ", totalRecords);
		if (objects.size() == 0)
			return;
		try {

			List<String[]> records = new ArrayList<>();
			for (JSONObject jObject : objects) {

				String Id = " ";
				String CompanyName = " ";
				String GivenName = " ";
				String FamilyName = " ";
				String Line1 = " ";
				String City = " ";
				String CountrySubDivisionCode = " ";
				String PostalCode = " ";
				String Country = " ";
				String Address = " ";
				String FreeFormNumber = " ";

				Id = getValue(jObject, "Id");
				CompanyName = getValue(jObject, "CompanyName", "FullyQualifiedName");

				if (StringUtils.isBlank(Id) || StringUtils.isBlank(CompanyName)) {
					continue;
				}

				GivenName = getValue(jObject, "GivenName");
				FamilyName = getValue(jObject, "FamilyName");
				if (jObject.containsKey("BillAddr")) {
					JSONObject BillAddr = jObject.getJSONObject("BillAddr");
					Line1 = getValue(BillAddr, "Line1");
					City = getValue(BillAddr, "City");
					CountrySubDivisionCode = getValue(BillAddr, "CountrySubDivisionCode");
					PostalCode = getValue(BillAddr, "PostalCode");
					Country = getValue(BillAddr, "Country");
				}
				if (jObject.containsKey("PrimaryEmailAddr")) {
					JSONObject PrimaryEmailAddr = jObject.getJSONObject("PrimaryEmailAddr");
					Address = PrimaryEmailAddr.getString("Address");
				}
				if (jObject.containsKey("PrimaryPhone")) {
					JSONObject PrimaryPhone = jObject.getJSONObject("PrimaryPhone");
					FreeFormNumber = PrimaryPhone.getString("FreeFormNumber");
				}

				if (this.useVendorNameAsId) Id = CompanyName;

				String[] values = new String[] {
						Id, CompanyName, Line1, " ", City, CountrySubDivisionCode, PostalCode,
						GivenName + " " + FamilyName, FreeFormNumber, Address, " ", " "
				};
				records.add(values);
			}
			if (records.size() > 0) {
				uploadData(records, "vendors", loanid);
			}
		} catch (Exception e) {
			throw e;
		}
	}



	/**
	 * Imports the aging receivables.
	 *
	 * @throws Exception
	 */
	public void importAgingPayablesReport(String loanid) throws Exception {

		StringWriter writer = new StringWriter();
		CSVWriter csvWriter = new CSVWriter(writer);
		List<String[]> records = new ArrayList<>();

		SimpleDateFormat destDateFormatter = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat srceDateFormatter = new SimpleDateFormat("yyyy-MM-dd");

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String queryString = "report_date=" + df.format(new Date());
		JSONObject jsonObject = getReportObjectFromService("AgedPayableDetail?" + queryString);
		if (jsonObject == null) return;
		if (! jsonObject.containsKey("Rows")) return;
		if (! jsonObject.getJSONObject("Rows").containsKey("Row")) return;

		JSONArray rowsArray = jsonObject.getJSONObject("Rows").getJSONArray("Row");
		
		try {
			for (Iterator<JSONObject> iterator1 = rowsArray.iterator(); iterator1.hasNext();) {
				JSONObject obj = iterator1.next();
				
				if (! obj.containsKey("Rows")) continue; 
				JSONObject rows1 = obj.getJSONObject("Rows");
				if (! rows1.containsKey("Row")) continue;
				
				JSONArray colsArray = rows1.getJSONArray("Row");
				for (Iterator<JSONObject> iterator2 = colsArray.iterator(); iterator2.hasNext();) {
					
					JSONObject obj2 = iterator2.next();
					if (! obj2.containsKey("ColData")) continue; 
					
					JSONArray ColData = obj2.getJSONArray("ColData");
					
					String  tx_date = ColData.getJSONObject(0).getString("value");
					
					String  type = ColData.getJSONObject(1).getString("value");
					String  type_id = ColData.getJSONObject(1).getString("id");
					
					String 	doc_num = ColData.getJSONObject(2).getString("value");
					
					String 	vend_name = ColData.getJSONObject(3).getString("value");
					String 	vend_id = ColData.getJSONObject(3).getString("id");
					
					String  due_date = ColData.getJSONObject(4).getString("value");
					
					String  past_due = ColData.getJSONObject(5).getString("value");
				
					String  amount = ColData.getJSONObject(6).getString("value");
					
					String  balance = ColData.getJSONObject(7).getString("value");
									

					if (StringUtils.isBlank(tx_date)) continue;
					if (StringUtils.isBlank(amount)) continue;
					
					if (StringUtils.isBlank(doc_num)) {
						doc_num = type + "-" + type_id;
					}

					// Convert dates to our format
					
					if (org.apache.commons.lang3.StringUtils.isNotBlank(tx_date)) {
						Date dt = srceDateFormatter.parse(tx_date);
						if (dt != null) {
							tx_date = destDateFormatter.format(dt);
						}
					}
					if (org.apache.commons.lang3.StringUtils.isNotBlank(due_date)) {
						Date dt = srceDateFormatter.parse(due_date);
						if (dt != null) {
							due_date = destDateFormatter.format(dt);
						}
					}
					if (this.useVendorNameAsId) vend_id = vend_name;
					
					
	            	String vendorid_alt = StringUtils.isEmpty(vend_id) ? " " : vend_id;
                	String vendor_name 	= StringUtils.isEmpty(vend_name) ? " " : vend_name;
                	String address1 	= " ";
                	String address2 	= " ";
                	String city 		= " ";
                	String state 		= " ";
                	String zip 			= " ";
                	String country 		= " ";
                	String inv_nm 		= StringUtils.isEmpty(doc_num) ? " " : doc_num;
                	String inv_dt 		= StringUtils.isEmpty(tx_date) ? " " : tx_date;
                	String terms 		= " ";
                	String trancode 	= " ";
                	String inv_am 		= StringUtils.isEmpty(balance) ? " " : balance;
                    String due_dt 		= StringUtils.isEmpty(due_date) ? " " : due_date;					
					
					String[] values = new String[] { vendorid_alt, vendor_name, address1, address2, city, state, zip,
							country, inv_nm, inv_dt, terms, trancode, inv_am, due_dt
					};
					records.add(values);
				}
				
			}
			if (records.size() > 0) {
				uploadData(records, "payables", loanid);
			}
		}
		catch (Exception e) {
			throw e;
		}
	}

	private void uploadData(List records, String type, String loanid) throws Exception {
		/*
		StringWriter writer = new StringWriter();
		CSVWriter csvWriter = new CSVWriter(writer);
		
		Thread.sleep(1000);
		csvWriter.writeAll(records);
		new UploadHelper().importFromSource(
				userid, loanid, writer.toString().getBytes(), type, "CSV", 
				this.blockSwitch, DateHelper.getCurrentDate().getTime(), appid);
		*/
	}
	

	/**
	 * The remove REST service is called to receive the JSON string which is
	 * converted to a list of JSON objects. The remote service has a limit to
	 * how many records it will return so we must make multiple calls.
	 * 
	 * @param responseKey
	 * @param sqlString
	 * @param totalRecords
	 * @return The list of JSON objects returned.
	 * @throws Exception
	 */
	private List<JSONObject> getListFromService(String responseKey, String sqlString, long totalRecords)
			throws Exception {

		List<JSONObject> objects = new ArrayList<>();

		if (totalRecords <= 0)
			return objects;

		int startPosition = 1;
		for (long recordsToProcess = totalRecords; recordsToProcess > 0; recordsToProcess -= MAX_QUERY_RESULTS) {
			JSONObject queryResponseObject = getListDataFromService(sqlString, startPosition, MAX_QUERY_RESULTS);
			if (queryResponseObject != null) {
				JSONArray jArray = queryResponseObject.getJSONArray(responseKey);
				for (Iterator<JSONObject> iterator = jArray.iterator(); iterator.hasNext();) {
					JSONObject jObject = iterator.next();
					objects.add(jObject);
				}
			}
			startPosition += MAX_QUERY_RESULTS;
		}
		return objects;
	}

	/**
	 * The total records are returned based on the SQL String.
	 * 
	 * @param sqlString
	 * @return The total records to be returned.
	 * @throws Exception
	 */
	private long getRecordCount(String sqlString) throws Exception {
		long ret = 0;
		JSONObject queryObj = (JSONObject) getListDataFromService(sqlString, 0, 0);
		if (queryObj != null) {
			Long totalCount = queryObj.getLong("totalCount");
			ret = totalCount.longValue();
		}
		return ret;
	}

	/**
	 * Calls the REST Api to return a list based on the start position and max
	 * results.
	 * 
	 * @param sqlString
	 * @param startPosition
	 * @param maxResults
	 * @return The object that represents a list.
	 * @throws Exception
	 */
	private JSONObject getListDataFromService(String sqlString, long startPosition, long maxResults) throws Exception {

		JSONObject returnObject = null;
		try {
			OAuthAccessToken token = this.getTokenObject();
			OAuthConsumer ouathconsumer = this.getAuthConsumer();
			String realmID = token.getSpecialId1__c();
			String urlString = this.baseUrl + "/v3/company/" + realmID + "/query?query=";

			if (startPosition > 0) {
				sqlString += (" STARTPOSITION " + startPosition);
			}
			if (maxResults > 0) {
				sqlString += (" MAXRESULTS " + maxResults);
			}

			String sqlStringEncoded = new URLCodec().encode(sqlString);

			URL url = new URL(urlString + sqlStringEncoded);

			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setUseCaches(false);
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setRequestProperty("Connection", "Keep-Alive");
			urlConnection.setRequestProperty("Content-Type", "application/json");
			urlConnection.setRequestProperty("Accept", "application/json");

			ouathconsumer.sign(urlConnection);

			urlConnection.connect();

			if (urlConnection != null) {
				BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				String json = rd.readLine();
				JSONObject jsonObj = (JSONObject) JSONSerializer.toJSON(json);
				if (jsonObj != null) {
					returnObject = (JSONObject) jsonObj.get("QueryResponse");
				}
				rd.close();
			}
		} catch (Exception e) {
			throw e;
		}
		return returnObject;
	}

	/**
	 * Calls the REST Api to return a report object.
	 * 
	 * @param queryString
	 * @return The report object.
	 * @throws Exception
	 */
	private JSONObject getReportObjectFromService(String queryString) throws Exception {

		JSONObject returnObject = null;
		try {
			OAuthAccessToken token = this.getTokenObject();
			OAuthConsumer ouathconsumer = this.getAuthConsumer();
			String realmID = token.getSpecialId1__c();
			String urlString = this.baseUrl + "/v3/company/" + realmID + "/reports/" + queryString;

			URL url = new URL(urlString);

			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setUseCaches(false);
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			urlConnection.setRequestProperty("Connection", "Keep-Alive");
			urlConnection.setRequestProperty("Content-Type", "application/json");
			urlConnection.setRequestProperty("Accept", "application/json");

			ouathconsumer.sign(urlConnection);

			urlConnection.connect();

			if (urlConnection != null) {

				BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				String json = rd.readLine();
				returnObject = (JSONObject) JSONSerializer.toJSON(json);
				rd.close();
			}

		} catch (Exception e) {
			throw e;
		}
		return returnObject;
	}

	/**
	 * Helper method to retrieve the value from the JSON Object.
	 * 
	 * @param jsonObject
	 * @param primaryKey
	 * @return String value
	 * @throws Exception
	 */
	public String getValue(JSONObject jsonObject, String primaryKey) throws Exception {
		return getValue(jsonObject, primaryKey, null);
	}

	/**
	 * Helper method to retrieve the value from the JSON Object. a secondaryKey
	 * can be specified in case the primaryKey is not sent by the API.
	 * 
	 * @param jsonObject
	 * @param primaryKey
	 * @param secondaryKey
	 * @return
	 * @throws Exception
	 */
	public String getValue(JSONObject jsonObject, String primaryKey, String secondaryKey) throws Exception {
		String v = " ";
		
		if (jsonObject == null || StringUtils.isBlank(primaryKey))
			return v;

		if (jsonObject.containsKey(primaryKey)) {
			v = jsonObject.getString(primaryKey);
		}
		else
		if (StringUtils.isNotBlank(secondaryKey)) {
			if (jsonObject.containsKey(secondaryKey)) {
				v = jsonObject.getString(secondaryKey);
			}
		}
		
		v = StringUtils.remove(v, "'");
		v = StringUtils.remove(v, '"');
		
		if (v.length() == 0) v = " ";
		return v;
	}

}
