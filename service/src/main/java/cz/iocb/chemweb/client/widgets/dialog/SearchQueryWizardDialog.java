package cz.iocb.chemweb.client.widgets.dialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;




/**
 * Wizard generating a SPARQL query that combines a Sachem structure search of compounds, a full-text search of
 * bioassays and a full-text search of the bioassay participants (proteins and genes).
 */
public class SearchQueryWizardDialog extends DialogBox
{
    /**
     * Binder of the dialog template.
     */
    private static SearchQueryWizardDialogUiBinder uiBinder = GWT.create(SearchQueryWizardDialogUiBinder.class);

    /**
     * Receiver of the query generated in the open dialog.
     */
    private Callback callback;

    /**
     * Binder of the dialog template {@code SearchQueryWizardDialog.ui.xml}.
     */
    interface SearchQueryWizardDialogUiBinder extends UiBinder<Widget, SearchQueryWizardDialog>
    {
    }

    /**
     * Receiver of the generated query.
     */
    public static abstract class Callback
    {
        /**
         * Creates the receiver.
         */
        protected Callback()
        {
        }


        /**
         * Called with the generated query.
         *
         * @param code the SPARQL query
         */
        abstract public void onSelection(String code);
    }

    /**
     * Methods of the structure search.
     */
    private static enum Methods
    {
        /**
         * Substructure search.
         */
        SUBSEARCH,

        /**
         * Similarity search.
         */
        SIMSEARCH,

        /**
         * Exact search.
         */
        EXACTSEARCH;
    }


    /**
     * Check box enabling the structure search of compounds.
     */
    @UiField
    CheckBox searchCompoundsCheckBox;

    /**
     * Check box enabling the full-text search of bioassays.
     */
    @UiField
    CheckBox searchBioassaysCheckBox;

    /**
     * Check box enabling the full-text search of the bioassay participants.
     */
    @UiField
    CheckBox searchParticipantsCheckBox;

    /**
     * List of the methods of the structure search.
     */
    @UiField
    ListBox queryMethodListBox;

    /**
     * Text area with the query structure.
     */
    @UiField
    TextArea queryTextArea;

    /**
     * File input loading the query structure from a file.
     */
    @UiField
    FileUpload queryFileUpload;

    /**
     * Button lying under the transparent file input, so that the file input looks like a button.
     */
    @UiField
    Button queryUploadButton;

    /**
     * Text box with the maximum number of the found compounds.
     */
    @UiField
    TextBox resultLimitTextBox;

    /**
     * Label of the similarity threshold, shown for the similarity search.
     */
    @UiField
    Label resultThresholdLabel;

    /**
     * Text box with the similarity threshold, shown for the similarity search.
     */
    @UiField
    TextBox resultThresholdTextBox;

    /**
     * Label of the tautomer mode, shown for the substructure and exact searches.
     */
    @UiField
    Label useTautomersLabel;

    /**
     * List of the tautomer modes, shown for the substructure and exact searches.
     */
    @UiField
    ListBox useTautomersListBox;

    /**
     * Text area with the full-text query of the bioassays.
     */
    @UiField
    TextArea keywordsTextArea;

    /**
     * Check box admitting the active outcomes of the compounds in the bioassays.
     */
    @UiField
    CheckBox activeStatusCheckBox;

    /**
     * Check box admitting the inactive outcomes of the compounds in the bioassays.
     */
    @UiField
    CheckBox inactiveStatusCheckBox;

    /**
     * Check box admitting the inconclusive outcomes of the compounds in the bioassays.
     */
    @UiField
    CheckBox inconclusiveStatusCheckBox;

    /**
     * Check box admitting the probe outcomes of the compounds in the bioassays.
     */
    @UiField
    CheckBox probeStatusCheckBox;

    /**
     * Check box admitting the unspecified outcomes of the compounds in the bioassays.
     */
    @UiField
    CheckBox unspecifiedStatusCheckBox;

    /**
     * Text box with the full-text query of the names of the participants.
     */
    @UiField
    TextBox participantTextBox;

    /**
     * Check box admitting the proteins as the participants.
     */
    @UiField
    CheckBox proteinCheckBox;

    /**
     * Check box admitting the genes as the participants.
     */
    @UiField
    CheckBox geneCheckBox;

    /**
     * Check box selecting the compounds into the result.
     */
    @UiField
    CheckBox compoundCheckBox;

    /**
     * Check box selecting the similarity scores into the result.
     */
    @UiField
    CheckBox scoreCheckBox;

    /**
     * Check box selecting the bioassays into the result.
     */
    @UiField
    CheckBox bioassayCheckBox;

    /**
     * Check box selecting the outcomes into the result.
     */
    @UiField
    CheckBox statusCheckBox;

    /**
     * Check box selecting the participants into the result.
     */
    @UiField
    CheckBox participantCheckBox;

    /**
     * List of the variables the result can be ordered by.
     */
    @UiField
    ListBox orderByListBox;

    /**
     * Button generating the query, enabled only when the values are valid.
     */
    @UiField
    Button finishButton;



    /**
     * Enables the controls of the structure search, and the outcome statuses when the bioassays are searched too,
     * according to the check box.
     *
     * @param event the value change event
     */
    @UiHandler("searchCompoundsCheckBox")
    void searchCompoundsCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        queryMethodListBox.setEnabled(event.getValue());
        queryTextArea.setEnabled(event.getValue());
        queryFileUpload.setEnabled(event.getValue());
        queryUploadButton.setEnabled(event.getValue());
        resultLimitTextBox.setEnabled(event.getValue());
        resultThresholdTextBox.setEnabled(event.getValue());
        useTautomersListBox.setEnabled(event.getValue());

        compoundCheckBox.setEnabled(event.getValue());
        scoreCheckBox.setEnabled(
                event.getValue() && Methods.valueOf(queryMethodListBox.getSelectedValue()) == Methods.SIMSEARCH);
        statusCheckBox.setEnabled(event.getValue() && searchBioassaysCheckBox.getValue());

        if(searchBioassaysCheckBox.getValue())
        {
            activeStatusCheckBox.setEnabled(event.getValue());
            inactiveStatusCheckBox.setEnabled(event.getValue());
            inconclusiveStatusCheckBox.setEnabled(event.getValue());
            probeStatusCheckBox.setEnabled(event.getValue());
            unspecifiedStatusCheckBox.setEnabled(event.getValue());
        }

        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Enables the controls of the bioassay search, and the outcome statuses when the compounds are searched too,
     * according to the check box.
     *
     * @param event the value change event
     */
    @UiHandler("searchBioassaysCheckBox")
    void searchBioassaysCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        keywordsTextArea.setEnabled(event.getValue());

        bioassayCheckBox.setEnabled(event.getValue());
        statusCheckBox.setEnabled(event.getValue() && searchCompoundsCheckBox.getValue());


        if(searchCompoundsCheckBox.getValue())
        {
            activeStatusCheckBox.setEnabled(event.getValue());
            inactiveStatusCheckBox.setEnabled(event.getValue());
            inconclusiveStatusCheckBox.setEnabled(event.getValue());
            probeStatusCheckBox.setEnabled(event.getValue());
            unspecifiedStatusCheckBox.setEnabled(event.getValue());
        }

        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Enables the controls of the participant search according to the check box.
     *
     * @param event the value change event
     */
    @UiHandler("searchParticipantsCheckBox")
    void searchParticipantsCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        participantTextBox.setEnabled(event.getValue());
        proteinCheckBox.setEnabled(event.getValue());
        geneCheckBox.setEnabled(event.getValue());
        participantCheckBox.setEnabled(event.getValue());

        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Shows the similarity threshold for the similarity search and the tautomer mode for the other methods.
     *
     * @param e the change event
     */
    @UiHandler("queryMethodListBox")
    void queryMethodListBoxChange(ChangeEvent e)
    {
        switch(Methods.valueOf(queryMethodListBox.getSelectedValue()))
        {
            case SUBSEARCH:
            case EXACTSEARCH:
                resultThresholdLabel.setVisible(false);
                resultThresholdTextBox.setVisible(false);
                useTautomersLabel.setVisible(true);
                useTautomersListBox.setVisible(true);
                resultThresholdTextBox.getElement().getStyle().setZIndex(1);
                useTautomersListBox.getElement().getStyle().setZIndex(2);
                scoreCheckBox.setEnabled(false);
                break;

            case SIMSEARCH:
                resultThresholdLabel.setVisible(true);
                resultThresholdTextBox.setVisible(true);
                useTautomersLabel.setVisible(false);
                useTautomersListBox.setVisible(false);
                resultThresholdTextBox.getElement().getStyle().setZIndex(2);
                useTautomersListBox.getElement().getStyle().setZIndex(1);
                scoreCheckBox.setEnabled(true);
                break;
        }

        finishButton.setEnabled(areValuesValid());
    }


    /**
     * Marks an invalid result limit in red as it is typed.
     *
     * @param e the key up event
     */
    @UiHandler("resultLimitTextBox")
    void resultLimitTextBoxKeyUp(KeyUpEvent e)
    {
        String color = isResultLimitTextBoxValid() ? null : "red";
        resultLimitTextBox.getElement().getStyle().setColor(color);
        resultLimitTextBox.getElement().getStyle().setBorderColor(color);
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Marks an invalid result limit in red when it changes.
     *
     * @param e the value change event
     */
    @UiHandler("resultLimitTextBox")
    void resultLimitTextBoxValueChange(ValueChangeEvent<String> e)
    {
        String color = isResultLimitTextBoxValid() ? null : "red";
        resultLimitTextBox.getElement().getStyle().setColor(color);
        resultLimitTextBox.getElement().getStyle().setBorderColor(color);
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Marks an invalid similarity threshold in red as it is typed.
     *
     * @param e the key up event
     */
    @UiHandler("resultThresholdTextBox")
    void resultThresholdTextBoxKeyUp(KeyUpEvent e)
    {
        String color = isResultThresholdTextBoxValid() ? null : "red";
        resultThresholdTextBox.getElement().getStyle().setColor(color);
        resultThresholdTextBox.getElement().getStyle().setBorderColor(color);
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Marks an invalid similarity threshold in red when it changes.
     *
     * @param e the value change event
     */
    @UiHandler("resultThresholdTextBox")
    void resultThresholdTextBoxValueChange(ValueChangeEvent<String> e)
    {
        String color = isResultThresholdTextBoxValid() ? null : "red";
        resultThresholdTextBox.getElement().getStyle().setColor(color);
        resultThresholdTextBox.getElement().getStyle().setBorderColor(color);
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button as the query structure is typed.
     *
     * @param e the key up event
     */
    @UiHandler("queryTextArea")
    void queryTextAreaKeyUp(KeyUpEvent e)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button when the query structure changes.
     *
     * @param e the value change event
     */
    @UiHandler("queryTextArea")
    void queryTextAreaValueChange(ValueChangeEvent<String> e)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button as the bioassay query is typed.
     *
     * @param e the key up event
     */
    @UiHandler("keywordsTextArea")
    void keywordsTextAreaKeyUp(KeyUpEvent e)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button when the bioassay query changes.
     *
     * @param e the value change event
     */
    @UiHandler("keywordsTextArea")
    void keywordsTextAreaValueChange(ValueChangeEvent<String> e)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button when the active outcomes are admitted or excluded.
     *
     * @param event the value change event
     */
    @UiHandler("activeStatusCheckBox")
    void activeStatusCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button when the inactive outcomes are admitted or excluded.
     *
     * @param event the value change event
     */
    @UiHandler("inactiveStatusCheckBox")
    void inactiveStatusCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button when the inconclusive outcomes are admitted or excluded.
     *
     * @param event the value change event
     */
    @UiHandler("inconclusiveStatusCheckBox")
    void pinconclusiveStatusCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button when the probe outcomes are admitted or excluded.
     *
     * @param event the value change event
     */
    @UiHandler("probeStatusCheckBox")
    void probeStatusCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button when the unspecified outcomes are admitted or excluded.
     *
     * @param event the value change event
     */
    @UiHandler("unspecifiedStatusCheckBox")
    void unspecifiedStatusCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button as the participant query is typed.
     *
     * @param e the key up event
     */
    @UiHandler("participantTextBox")
    void participantTextBoxKeyUp(KeyUpEvent e)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button when the participant query changes.
     *
     * @param e the value change event
     */
    @UiHandler("participantTextBox")
    void participantTextBoxValueChange(ValueChangeEvent<String> e)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button when the proteins are admitted or excluded.
     *
     * @param event the value change event
     */
    @UiHandler("proteinCheckBox")
    void proteinCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button when the genes are admitted or excluded.
     *
     * @param event the value change event
     */
    @UiHandler("geneCheckBox")
    void geneCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button when the selection of the compounds changes.
     *
     * @param event the value change event
     */
    @UiHandler("compoundCheckBox")
    void compoundCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button when the selection of the scores changes.
     *
     * @param event the value change event
     */
    @UiHandler("scoreCheckBox")
    void scoreCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button when the selection of the bioassays changes.
     *
     * @param event the value change event
     */
    @UiHandler("bioassayCheckBox")
    void bioassayCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button when the selection of the outcomes changes.
     *
     * @param event the value change event
     */
    @UiHandler("statusCheckBox")
    void statusCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    /**
     * Updates the finish button when the selection of the participants changes.
     *
     * @param event the value change event
     */
    @UiHandler("participantCheckBox")
    void participantCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }



    /**
     * Closes the dialog and passes the generated query to the callback if the values are valid.
     *
     * @param e the click event
     */
    @UiHandler("finishButton")
    void finishButtonClick(ClickEvent e)
    {
        if(!areValuesValid())
            return;

        hide();
        callback.onSelection(generateQuery());
    }

    /**
     * Closes the dialog without generating a query.
     *
     * @param e the click event
     */
    @UiHandler("cancelButton")
    void cancelButtonClick(ClickEvent e)
    {
        hide();
    }

    /**
     * Opens the dialog in the centre of the page.
     *
     * @param callback the receiver of the generated query
     */
    public void open(Callback callback)
    {
        this.callback = callback;
        center();
    }

    /**
     * Checks that at least one search is enabled and that the limit and the threshold of the structure search are
     * valid.
     *
     * @return true if the query can be generated
     */
    private boolean areValuesValid()
    {
        boolean searchCompounds = searchCompoundsCheckBox.getValue();
        boolean searchBioassays = searchBioassaysCheckBox.getValue();
        boolean searchParticipants = searchParticipantsCheckBox.getValue();


        // at least one search have to be enabled
        if(!searchCompounds && !searchBioassays && !searchParticipants)
            return false;

        /*
        // at least one search have to be restricted
        {
            int restricted = 0;

            if(searchCompounds && !queryTextArea.getValue().trim().isEmpty())
                restricted++;

            if(searchBioassays && !keywordsTextArea.getValue().trim().isEmpty())
                restricted++;

            if(searchParticipants && !participantTextBox.getValue().trim().isEmpty())
                restricted++;

            if(restricted == 0)
                return false;
        }
        */


        // compound search check
        if(searchCompounds)
        {
            if(!isResultLimitTextBoxValid())
                return false;

            if(resultThresholdTextBox.isVisible() && !isResultThresholdTextBoxValid())
                return false;
        }


        // bioassay status check
        if(searchBioassays && searchCompounds)
        {
            /*
            // at least one assay status have to be selected
            int selected = 0;

            for(CheckBox s : new CheckBox[] { activeStatusCheckBox, inactiveStatusCheckBox, inconclusiveStatusCheckBox,
                    probeStatusCheckBox, unspecifiedStatusCheckBox })
                if(s.getValue())
                    selected++;

            if(selected == 0)
                return false;
            */
        }


        // participant search check
        if(searchParticipants)
        {
            /*
            // at least one search have to be enabled
            if(!proteinCheckBox.getValue() && !geneCheckBox.getValue())
                return false;
            */
        }


        /*
        // check selection
        int selections = 0;

        if(compoundCheckBox.getValue() && searchCompounds)
            selections++;

        if(scoreCheckBox.getValue() && searchCompounds
                && Methods.valueOf(queryMethodListBox.getSelectedValue()) == Methods.SIMSEARCH)
            selections++;

        if(bioassayCheckBox.getValue() && searchBioassays)
            selections++;

        if(statusCheckBox.getValue() && searchCompounds && searchBioassays)
            selections++;

        if(participantCheckBox.getValue() && searchParticipants)
            selections++;

        if(selections == 0)
            return false;
        */

        return true;
    }


    /**
     * Checks the similarity threshold: empty or a number between 0 and 1.
     *
     * @return true if the threshold is valid
     */
    private boolean isResultThresholdTextBoxValid()
    {
        try
        {
            String text = resultThresholdTextBox.getValue().trim();

            if(text.isEmpty())
                return true;

            double val = Double.valueOf(text);

            if(val >= 0 && val <= 1)
                return true;
        }
        catch(NumberFormatException exp)
        {

        }

        return false;
    }

    /**
     * Checks the result limit: empty or a positive integer.
     *
     * @return true if the limit is valid
     */
    private boolean isResultLimitTextBoxValid()
    {
        try
        {
            String text = resultLimitTextBox.getValue().trim();

            if(text.isEmpty())
                return true;

            double val = Integer.valueOf(text);

            if(val > 0)
                return true;
        }
        catch(NumberFormatException exp)
        {

        }

        return false;
    }

    /**
     * Generates the query of the enabled searches: the compounds found by the structure search, the bioassays found by
     * the full-text search and the participants found by their names, joined through the substances, endpoints and
     * measure groups of PubChem, restricted to the admitted outcomes and ordered as chosen.
     *
     * @return the SPARQL query
     */
    private String generateQuery()
    {
        Methods method = Methods.valueOf(queryMethodListBox.getSelectedValue());
        boolean searchCompounds = searchCompoundsCheckBox.getValue();
        boolean searchBioassays = searchBioassaysCheckBox.getValue();
        boolean searchParticipants = searchParticipantsCheckBox.getValue();

        StringBuilder query = new StringBuilder();


        int statusCount = 0;

        if(activeStatusCheckBox.getValue())
            statusCount++;

        if(inactiveStatusCheckBox.getValue())
            statusCount++;

        if(inconclusiveStatusCheckBox.getValue())
            statusCount++;

        if(probeStatusCheckBox.getValue())
            statusCount++;

        if(unspecifiedStatusCheckBox.getValue())
            statusCount++;


        boolean useDistinct = false;

        if(!compoundCheckBox.getValue() && searchCompounds)
            useDistinct = true;

        if(!bioassayCheckBox.getValue() && searchBioassays)
            useDistinct = true;

        if(!participantCheckBox.getValue() && searchParticipants)
            useDistinct = true;

        if(!statusCheckBox.getValue() && searchCompounds && searchBioassays && statusCount > 0 && statusCount < 5)
            useDistinct = true;


        boolean useStar = true;

        if(compoundCheckBox.getValue() && searchCompounds)
            useStar = false;

        if(scoreCheckBox.getValue() && searchCompounds && method == Methods.SIMSEARCH)
            useStar = false;

        if(bioassayCheckBox.getValue() && searchBioassays)
            useStar = false;

        if(participantCheckBox.getValue() && searchParticipants)
            useStar = false;

        if(statusCheckBox.getValue() && searchCompounds && searchBioassays)
            useStar = false;


        query.append("SELECT ");

        if(useDistinct && !useStar)
            query.append("DISTINCT ");

        if(compoundCheckBox.getValue() && searchCompounds)
            query.append("?COMPOUND ");

        if(scoreCheckBox.getValue() && searchCompounds && method == Methods.SIMSEARCH)
            query.append("?SCORE ");

        if(bioassayCheckBox.getValue() && searchBioassays)
            query.append("?BIOASSAY ");

        if(participantCheckBox.getValue() && searchParticipants)
            query.append("?PARTICIPANT ");

        if(statusCheckBox.getValue() && searchCompounds && searchBioassays)
            query.append("?STATUS ");

        if(useStar)
            query.append("* ");


        query.append("WHERE \n{");


        if(searchCompounds)
        {
            if(!queryTextArea.getValue().trim().isEmpty())
            {
                if(method == Methods.SIMSEARCH)
                    query.append("\n  [ sachem:compound ?COMPOUND; sachem:score ?SCORE ]\n");
                else
                    query.append("\n  ?COMPOUND ");

                if(method == Methods.EXACTSEARCH || method == Methods.SUBSEARCH)
                    query.append("sachem:substructureSearch");
                else if(method == Methods.SIMSEARCH)
                    query.append("    sachem:similaritySearch");

                query.append(" [\n      sachem:query '''");
                query.append(queryTextArea.getValue());
                query.append("'''");


                String limit = resultLimitTextBox.getValue().trim();

                if(!limit.isEmpty())
                {
                    query.append(";\n      sachem:topn \"");
                    query.append(limit);
                    query.append("\"^^xsd:integer");
                }

                if(method == Methods.EXACTSEARCH)
                {
                    query.append(";\n      sachem:searchMode sachem:exactSearch");
                }

                if(method == Methods.SUBSEARCH || method == Methods.EXACTSEARCH)
                {
                    query.append(";\n      sachem:tautomerMode ");
                    query.append(useTautomersListBox.getSelectedValue());
                }

                String threshold = resultThresholdTextBox.getValue().trim();

                if(method == Methods.SIMSEARCH && !threshold.isEmpty())
                {
                    query.append(";\n      sachem:cutoff \"");
                    query.append(threshold);
                    query.append("\"^^xsd:double");
                }

                query.append(" ].\n");
            }

            if(!searchBioassays && !searchParticipants)
            {
                query.append("\n  ?COMPOUND rdf:type vocab:Compound.");
            }
            else
            {
                query.append("\n  ?SUBSTANCE sio:CHEMINF_000477 ?COMPOUND.");
                query.append("\n  ?ENDPOINT obo:IAO_0000136 ?SUBSTANCE.");
                query.append("\n  ?MEASUREGROUP obo:OBI_0000299 ?ENDPOINT.");
            }
        }


        if(searchBioassays)
        {
            if(searchCompounds)
                query.append("\n");

            if(!keywordsTextArea.getValue().trim().isEmpty())
            {
                query.append("\n\n  ?BIOASSAY fulltext:bioassaySearch [fulltext:query '''");
                query.append(keywordsTextArea.getValue().trim().replaceAll("'", "\\\\'"));
                query.append("'''].\n");
            }

            if(!searchCompounds && !searchParticipants)
                query.append("\n  ?BIOASSAY rdf:type vocab:BioAssay.");
            else
                query.append("\n  ?BIOASSAY bao:BAO_0000209 ?MEASUREGROUP.");
        }



        if(searchCompounds && searchBioassays)
        {
            if(statusCount == 1 && !statusCheckBox.getValue())
            {
                query.append("\n\n  ?ENDPOINT vocab:PubChemAssayOutcome ");

                if(activeStatusCheckBox.getValue())
                    query.append("vocab:active");

                if(inactiveStatusCheckBox.getValue())
                    query.append("vocab:inactive");

                if(inconclusiveStatusCheckBox.getValue())
                    query.append("vocab:inconclusive");

                if(probeStatusCheckBox.getValue())
                    query.append("vocab:probe");

                if(unspecifiedStatusCheckBox.getValue())
                    query.append("vocab:unspecified");

                query.append(".");
            }
            else if(statusCount > 0 && statusCount < 5 || statusCheckBox.getValue())
            {
                query.append("\n\n  ?ENDPOINT vocab:PubChemAssayOutcome ?STATUS.");

                if(statusCount > 0 && statusCount < 5)
                {
                    query.append("\n  FILTER(?STATUS IN (");

                    if(activeStatusCheckBox.getValue())
                    {
                        query.append("vocab:active");

                        if(--statusCount > 0)
                            query.append(" ,");
                    }

                    if(inactiveStatusCheckBox.getValue())
                    {
                        query.append("vocab:inactive");

                        if(--statusCount > 0)
                            query.append(" ,");
                    }

                    if(inconclusiveStatusCheckBox.getValue())
                    {
                        query.append("vocab:inconclusive");

                        if(--statusCount > 0)
                            query.append(" ,");
                    }

                    if(probeStatusCheckBox.getValue())
                    {
                        query.append("vocab:probe");

                        if(--statusCount > 0)
                            query.append(", ");
                    }

                    if(unspecifiedStatusCheckBox.getValue())
                    {
                        query.append("vocab:unspecified");
                    }

                    query.append("))");
                }
            }
        }



        if(searchParticipants)
        {
            if(searchCompounds || searchBioassays)
                query.append("\n\n  ?MEASUREGROUP obo:RO_0000057 ?PARTICIPANT.");

            if(geneCheckBox.getValue() && !proteinCheckBox.getValue())
            {
                query.append("\n  ?PARTICIPANT rdf:type vocab:Gene.");
            }
            else if(!geneCheckBox.getValue() && proteinCheckBox.getValue())
            {
                query.append("\n  ?PARTICIPANT rdf:type vocab:Protein.");
            }
            else if(!searchCompounds && !searchBioassays)
            {
                query.append("\n  ?PARTICIPANT rdf:type ?PARTICIPANT_TYPE.");
                query.append("\n  FILTER(?PARTICIPANT_TYPE in (vocab:Gene, vocab:Protein))");
            }

            if(!participantTextBox.getValue().trim().isEmpty())
            {
                String pattern = participantTextBox.getValue().trim().replaceAll("'", "\\\\'");
                query.append("\n  ?PARTICIPANT dcterms:title ?PARTICIPANT_TITLE.");

                query.append("\n  FILTER(fulltext:match(?PARTICIPANT_TITLE, '''" + pattern + "'''))");
            }
        }


        query.append("\n}\n");


        switch(orderByListBox.getSelectedValue())
        {
            case "COMPOUND":
                query.append("ORDER BY xsd:int(substr(str(?COMPOUND), 49))\n");
                break;

            case "BIOASSAY":
                query.append("ORDER BY xsd:int(substr(str(?BIOASSAY), 49))\n");
                break;

            case "PARTICIPANT":
                query.append("ORDER BY ?PARTICIPANT\n");
                break;

            case "SCORE":
                query.append("ORDER BY ?SCORE\n");
                break;

            case "STATUS":
                query.append("ORDER BY ?STATUS\n");
                break;
        }


        return query.toString();
    }


    /**
     * Creates the dialog and fills its lists.
     */
    public SearchQueryWizardDialog()
    {
        uiBinder.createAndBindUi(this);

        queryMethodListBox.addItem("Substructure search", "SUBSEARCH");
        queryMethodListBox.addItem("Exact search", "EXACTSEARCH");
        queryMethodListBox.addItem("Similarity search", "SIMSEARCH");

        useTautomersListBox.addItem("yes", "sachem:inchiTautomers");
        useTautomersListBox.addItem("no", "sachem:ignoreTautomers");

        orderByListBox.addItem("(none)", "NONE");
        orderByListBox.addItem("Compound ID", "COMPOUND");
        orderByListBox.addItem("Bioassay ID", "BIOASSAY");
        orderByListBox.addItem("Participant ID", "PARTICIPANT");
        orderByListBox.addItem("Score", "SCORE");
        orderByListBox.addItem("Status", "STATUS");

        queryTextArea.setValue("CC(=O)OC1=CC=CC=C1C(=O)O");


        queryFileUpload.addChangeHandler(new ChangeHandler()
        {
            @Override
            public void onChange(ChangeEvent event)
            {
                handleFileSelect(queryFileUpload.getElement());
            }
        });
    }

    /**
     * Reads the file selected in the file input and passes its content to {@link #handleLoadedFile(String)}.
     *
     * @param evt the file input element
     */
    private native void handleFileSelect(@SuppressWarnings("deprecation") com.google.gwt.user.client.Element evt)
    /*-{
        var file = evt.files[0];

        //if (!file.type.match('text.*'))
        //  return;

        if (!$wnd.File || !$wnd.FileReader || !$wnd.FileList || !$wnd.Blob)
            $wnd.alert("The File APIs are not fully supported by your browser.");

        var reader = new $wnd.FileReader();
        var that = this;

        reader.onload = (function(theFile) {
            return function(e) {
                that.@cz.iocb.chemweb.client.widgets.dialog.SearchQueryWizardDialog::handleLoadedFile(Ljava/lang/String;)(e.target.result);
            };
        })(file);

        reader.readAsText(file);
    }-*/;

    /**
     * Puts the content of a loaded file into the query text area.
     *
     * @param data the content of the file
     */
    public void handleLoadedFile(String data)
    {
        queryTextArea.setValue(data);
    }


    /**
     * Provides the dialog itself as the root of the template.
     *
     * @return the dialog
     */
    @UiFactory
    DialogBox instance()
    {
        return this;
    }
}
