package com.parserbox.vendors;

import java.util.List;

import com.parserbox.model.sso.OAuthAccessToken;
import com.parserbox.model.sso.OAuthAdapter;


public abstract class ImporterAdapter extends OAuthAdapter {


	/**
	 * Flag that tells system to use the full debtor name as the Id.
	 */
	protected boolean useDebtorNameAsId = false;
	/**
	 * Flag that tells system to use the full vendor name as the Id.
	 */
	protected boolean useVendorNameAsId = false;
	
	/**
	 * This switch will be used by the uploader to prevent processing in a thread.
	 * This should always be set to true to allow sequential processing of imports.
	 */
	protected boolean blockSwitch = true;
	
	/**
	 * Constructor
	 *
	 * @param authCallBackUrl
	 * @throws Exception
	 */
	public ImporterAdapter(OAuthAccessToken tokenObject, String authCallBackUrl) throws Exception {
		super(tokenObject, authCallBackUrl);
	}

	/**
	 * Imports data and transfers to our API for processing.
	 * 
	 * @param loanid
	 * @throws Exception
	 */
	public void importData(String loanid) throws Exception {
        (new Thread() {
              public void run() {
              	try {
          			doImport(loanid);
              	} 
              	catch (Exception e) {
              		log.info(e);
              	}
              }
          }).start();

	}
	private void doImport(String loanid) throws Exception {
		try {

			try {
				importDebtors(loanid);
			} catch (Exception e) { log.info(e);}

			try {
				importAgingReceivablesReport(loanid);
			} catch (Exception e) { log.info(e);}

			try {
				importVendors(loanid);
			} catch (Exception e) { log.info(e);}

			try {
				importAgingPayablesReport(loanid);
			} catch (Exception e) { log.info(e);}

		} catch (Exception e) {
			throw e;
		} finally {
		}
	}



	/**
	 * Quick check to see if we are authenticated.
	 * @return
	 * @throws Exception
	 */
	public abstract boolean isOkToProceed() throws Exception;
	

	/**
	 * Imports debtor records.
	 * 
	 * @throws Exception
	 */
	public abstract void importDebtors(String loanid) throws Exception;
	
	/**
	 * Imports the aging receivables.
	 * 
	 * @throws Exception
	 */
	public abstract void importAgingReceivablesReport(String loanid) throws Exception;
	
	/**
	 * Imports vendor records.
	 * 
	 * @throws Exception
	 */
	public abstract void importVendors(String loanid) throws Exception;

	/**
	 * Imports the aging receivables.
	 * 
	 * @throws Exception
	 */
	public abstract void importAgingPayablesReport(String loanid) throws Exception;

	private void uploadData(List records, String type, String loanid) throws Exception {
		/*
		StringWriter writer = new StringWriter();
		CSV csvWriter = new CSVWriter(writer);

		Thread.sleep(1000);
		csvWriter.writeAll(records);
		new UploadHelper().importFromSource(
				userid, loanid, writer.toString().getBytes(), type, "CSV",
				this.blockSwitch, DateHelper.getCurrentDate().getTime(), appid);
		*/
	}
	

}
