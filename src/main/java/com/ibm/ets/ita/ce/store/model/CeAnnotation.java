package com.ibm.ets.ita.ce.store.model;

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2017
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.MiscNames.ES;
import static com.ibm.ets.ita.ce.store.names.MiscNames.NO_TS;
import static com.ibm.ets.ita.ce.store.names.ParseNames.ANNO_TOKEN_MODEL;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_COLON;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.timestampNow;

import java.io.Serializable;

import com.ibm.ets.ita.ce.store.core.ActionContext;

public class CeAnnotation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2017";

//	private static AtomicLong annotationIdVal = new AtomicLong(0);

//	private String id = null;
	private long creationDate = NO_TS;
	private boolean metaModelGenerated = false;
	private String label = null;
	private String text = null;
	private CeSentence annotationSentence = null;

	private CeAnnotation() {
		this.creationDate = timestampNow();
	}

	public static CeAnnotation createAnnotationFrom(ActionContext pAc, String pAnnoLabel, String pAnnoText,
			CeSentence pAnnoSen) {
		CeAnnotation newAnno = new CeAnnotation();

//		newAnno.id = PREFIX_ANNO + nextAnnotationId();
		newAnno.label = pAc.getModelBuilder().getCachedStringValueLevel3(pAnnoLabel);
		newAnno.text = pAc.getModelBuilder().getCachedStringValueLevel3(pAnnoText);

		if (pAc.getCeConfig().isSavingCeSentences()) {
			newAnno.annotationSentence = pAnnoSen;
		}

		return newAnno;
	}

//	public static void resetCounter() {
//		annotationIdVal = new AtomicLong(0);
//	}

//	private static long nextAnnotationId() {
//		return annotationIdVal.incrementAndGet();
//	}

//	public String getId() {
//		return this.id;
//	}

	public long getCreationDate() {
		return this.creationDate;
	}

	public String getLabel() {
		return this.label;
	}

	public String trimmedLabel() {
		return this.label.replace(TOKEN_COLON, ES);
	}

	public String getText() {
		return this.text;
	}

	public CeSentence getAnnotationSentence() {
		return this.annotationSentence;
	}

	public boolean metaModelHasBeenGenerated() {
		return this.metaModelGenerated;
	}

	public void markAsMetaModelGenerated() {
		this.metaModelGenerated = true;
	}

	public boolean isModelAnnotation() {
		return this.label.equals(ANNO_TOKEN_MODEL);
	}

	@Override
	public String toString() {
		return "CeAnnotation - " + this.text + " (" + this.label + ")";
	}

}
