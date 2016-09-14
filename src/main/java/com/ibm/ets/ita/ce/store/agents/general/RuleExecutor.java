package com.ibm.ets.ita.ce.store.agents.general;

//ALL DONE (not messages)

/*******************************************************************************
 * (C) Copyright IBM Corporation  2011, 2016
 * All Rights Reserved
 *******************************************************************************/

import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_GENRAT;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_ITER;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_MAXITS;
import static com.ibm.ets.ita.ce.store.names.CeNames.PROP_RULENAME;
import static com.ibm.ets.ita.ce.store.names.MiscNames.DEFAULT_MAXITS;
import static com.ibm.ets.ita.ce.store.names.ParseNames.TOKEN_TRUE;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.getBooleanValueFrom;
import static com.ibm.ets.ita.ce.store.utilities.GeneralUtilities.getIntValueFrom;

import java.util.ArrayList;
import java.util.HashSet;

import com.ibm.ets.ita.ce.store.agents.CeAgent;
import com.ibm.ets.ita.ce.store.model.CeRule;
import com.ibm.ets.ita.ce.store.model.container.ContainerCeResult;
import com.ibm.ets.ita.ce.store.query.QueryExecutionManager;

public class RuleExecutor extends CeAgent {
	public static final String copyrightNotice = "(C) Copyright IBM Corporation  2011, 2016";

	private static final String AGENT_NAME = "RuleExecutor";
	private static final String AGENT_VERSION = "1.0.0";

	private boolean iterate = true;
	private int maxIterations = 0;
	private ArrayList<String> ruleNames = null;
	private HashSet<String> allSentences = null;

	public RuleExecutor() {
		super();

		this.allSentences = new HashSet<String>();
	}

	@Override
	public String getAgentName() {
		return AGENT_NAME;
	}

	@Override
	public String getAgentVersion() {
		return AGENT_VERSION;
	}

	@Override
	protected void loadAgentParameters() {
		this.ruleNames = getConfigValueListNamed(PROP_RULENAME);
		this.iterate = getBooleanValueFrom(getConfigOptionalSingleValueNamed(PROP_ITER, TOKEN_TRUE));

		if (this.iterate) {
			this.maxIterations = getIntValueFrom(getConfigOptionalSingleValueNamed(PROP_MAXITS, DEFAULT_MAXITS));
		} else {
			this.maxIterations = 1;
		}

		if (!isPropertySpecified(PROP_GENRAT)) {
			// If there is no explicit 'generate rationale' property specified
			// then default to true for this agent
			this.generateRationale = true;
		}
	}

	@Override
	protected void executeAgentProcessing() {
		int newSentences = -1;
		int totalSentences = 0;
		int iterationCounter = 0;

		// Iterate through the rules and only stop if the max iterations is
		// reached
		// or there are no new sentences generated
		while ((iterationCounter < this.maxIterations) && (newSentences != 0)) {
			newSentences = executeSpecifiedRules(iterationCounter);

			totalSentences += newSentences;
			++iterationCounter;
		}

		reportDebug("Total of " + Integer.toString(totalSentences) + " new sentences generated by "
				+ Integer.toString(iterationCounter) + " iterations of the rules");
	}

	private int executeSpecifiedRules(int pIterationCounter) {
		ArrayList<String> genSentences = new ArrayList<String>();
		int newOverallSentences = 0;
		QueryExecutionManager qm = QueryExecutionManager.createUsing(getActionContext(), false, null, null);

		for (String thisRuleName : this.ruleNames) {
			int newSentencesForThisRule = 0;
			CeRule thisRule = getActionContext().getModelBuilder().getRuleNamed(thisRuleName);

			if (thisRule != null) {
				if (!thisRule.hasErrors()) {
					// Initialise the sentence cache (huge performance
					// improvement)
					thisRule.createSentenceLookup(getActionContext());

					reportDebug("[Start] Executing rule " + thisRuleName + " (iteration=" + pIterationCounter + ")");

					ContainerCeResult result = qm.executeRule(thisRule, generateRationale(),
							this.doubleRationaleSentences);
					ArrayList<String> allResults = result.getCeResults();

					for (String thisSen : allResults) {
						if (isNewSentence(thisSen)
								&& !thisRule.doesSentenceTextAlreadyExist(getActionContext(), thisSen)) {
							genSentences.add(thisSen);
							++newSentencesForThisRule;
						}

					}

					newOverallSentences += newSentencesForThisRule;

					reportDebug("[End] Executing rule " + thisRuleName + " (results=" + allResults.size() + ", new="
							+ newSentencesForThisRule + ")");
				} else {
					reportError("Rule '" + thisRule.getRuleName() + "' has errors and was not executed");
				}

				// Clear the sentence cache
				thisRule.clearSentenceLookup();
			} else {
				reportWarning("Unable to locate rule named '" + thisRuleName + "'");
			}
		}

		// Save each new sentence
		for (String thisGenSen : genSentences) {
			if (generateRationale()) {
				addRationaleSentenceCeWithNoAgentName(thisGenSen);
			} else {
				addSentenceCe(thisGenSen);
			}
		}
		genSentences = null;
		flushSentences();

		return newOverallSentences;
	}

	private boolean isNewSentence(String pSen) {
		int count = this.allSentences.size();

		this.allSentences.add(pSen);

		return (count < this.allSentences.size());
	}

}
