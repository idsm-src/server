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




public class SearchQueryWizardDialog extends DialogBox
{
    private static SearchQueryWizardDialogUiBinder uiBinder = GWT.create(SearchQueryWizardDialogUiBinder.class);

    private Callback callback;

    interface SearchQueryWizardDialogUiBinder extends UiBinder<Widget, SearchQueryWizardDialog>
    {
    }

    public static abstract class Callback
    {
        abstract public void onSelection(String code);
    }

    private static enum Methods
    {
        SUBSEARCH, SIMSEARCH, EXACTSEARCH;
    }


    @UiField CheckBox searchCompoundsCheckBox;
    @UiField CheckBox searchBioassaysCheckBox;
    @UiField CheckBox searchParticipantsCheckBox;

    @UiField ListBox queryMethodListBox;
    @UiField TextArea queryTextArea;
    @UiField FileUpload queryFileUpload;
    @UiField TextBox resultLimitTextBox;
    @UiField Label resultThresholdLabel;
    @UiField TextBox resultThresholdTextBox;
    @UiField Label useTautomersLabel;
    @UiField ListBox useTautomersListBox;

    @UiField TextArea keywordsTextArea;
    @UiField CheckBox activeStatusCheckBox;
    @UiField CheckBox inactiveStatusCheckBox;
    @UiField CheckBox inconclusiveStatusCheckBox;
    @UiField CheckBox probeStatusCheckBox;
    @UiField CheckBox unspecifiedStatusCheckBox;

    @UiField TextBox participantTextBox;
    @UiField CheckBox proteinCheckBox;
    @UiField CheckBox geneCheckBox;

    @UiField CheckBox compoundCheckBox;
    @UiField CheckBox scoreCheckBox;
    @UiField CheckBox bioassayCheckBox;
    @UiField CheckBox statusCheckBox;
    @UiField CheckBox participantCheckBox;

    @UiField ListBox orderByListBox;

    @UiField Button finishButton;



    @UiHandler("searchCompoundsCheckBox")
    void searchCompoundsCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        queryMethodListBox.setEnabled(event.getValue());
        queryTextArea.setEnabled(event.getValue());
        queryTextArea.setEnabled(event.getValue());
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

    @UiHandler("searchParticipantsCheckBox")
    void searchParticipantsCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        participantTextBox.setEnabled(event.getValue());
        proteinCheckBox.setEnabled(event.getValue());
        geneCheckBox.setEnabled(event.getValue());
        participantCheckBox.setEnabled(event.getValue());

        finishButton.setEnabled(areValuesValid());
    }

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


    @UiHandler("resultLimitTextBox")
    void resultLimitTextBoxKeyUp(KeyUpEvent e)
    {
        String color = isResultLimitTextBoxValid() ? null : "red";
        resultLimitTextBox.getElement().getStyle().setColor(color);
        resultLimitTextBox.getElement().getStyle().setBorderColor(color);
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("resultLimitTextBox")
    void resultLimitTextBoxValueChange(ValueChangeEvent<String> e)
    {
        String color = isResultLimitTextBoxValid() ? null : "red";
        resultLimitTextBox.getElement().getStyle().setColor(color);
        resultLimitTextBox.getElement().getStyle().setBorderColor(color);
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("resultThresholdTextBox")
    void resultThresholdTextBoxKeyUp(KeyUpEvent e)
    {
        String color = isResultThresholdTextBoxValid() ? null : "red";
        resultThresholdTextBox.getElement().getStyle().setColor(color);
        resultThresholdTextBox.getElement().getStyle().setBorderColor(color);
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("resultThresholdTextBox")
    void resultThresholdTextBoxValueChange(ValueChangeEvent<String> e)
    {
        String color = isResultThresholdTextBoxValid() ? null : "red";
        resultThresholdTextBox.getElement().getStyle().setColor(color);
        resultThresholdTextBox.getElement().getStyle().setBorderColor(color);
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("queryTextArea")
    void queryTextAreaKeyUp(KeyUpEvent e)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("queryTextArea")
    void queryTextAreaValueChange(ValueChangeEvent<String> e)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("keywordsTextArea")
    void keywordsTextAreaKeyUp(KeyUpEvent e)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("keywordsTextArea")
    void keywordsTextAreaValueChange(ValueChangeEvent<String> e)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("activeStatusCheckBox")
    void activeStatusCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("inactiveStatusCheckBox")
    void inactiveStatusCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("inconclusiveStatusCheckBox")
    void pinconclusiveStatusCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("probeStatusCheckBox")
    void probeStatusCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("unspecifiedStatusCheckBox")
    void unspecifiedStatusCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("participantTextBox")
    void participantTextBoxKeyUp(KeyUpEvent e)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("participantTextBox")
    void participantTextBoxValueChange(ValueChangeEvent<String> e)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("proteinCheckBox")
    void proteinCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("geneCheckBox")
    void geneCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("compoundCheckBox")
    void compoundCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("scoreCheckBox")
    void scoreCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("bioassayCheckBox")
    void bioassayCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("statusCheckBox")
    void statusCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }

    @UiHandler("participantCheckBox")
    void participantCheckBoxValueChange(ValueChangeEvent<Boolean> event)
    {
        finishButton.setEnabled(areValuesValid());
    }



    @UiHandler("finishButton")
    void finishButtonClick(ClickEvent e)
    {
        if(!areValuesValid())
            return;

        hide();
        callback.onSelection(generateQuery());
    }

    @UiHandler("cancelButton")
    void cancelButtonClick(ClickEvent e)
    {
        hide();
    }

    public void open(Callback callback)
    {
        this.callback = callback;
        center();
    }

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
                query.append("\n  ?COMPOUND rdf:type sio:SIO_010004.");
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
                query.append("\n  ?BIOASSAY rdf:type bao:BAO_0000015.");
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
                query.append("\n  ?PARTICIPANT rdf:type sio:SIO_010035.");
            }
            else if(!geneCheckBox.getValue() && proteinCheckBox.getValue())
            {
                //FIXME: se another type instead of bp:Protein
                query.append("\n  ?PARTICIPANT rdf:type bp:Protein.");
            }
            else if(!searchCompounds && !searchBioassays)
            {
                //FIXME: se another type instead of bp:Protein
                query.append("\n  ?PARTICIPANT rdf:type ?PARTICIPANT_TYPE.");
                query.append("\n  FILTER(?PARTICIPANT_TYPE in (sio:SIO_010035, bp:Protein))");
            }

            if(!participantTextBox.getValue().trim().isEmpty())
            {
                String pattern = participantTextBox.getValue().trim().replaceAll("'", "\\\\'");
                query.append("\n  ?PARTICIPANT dcterms:title ?PARTICIPANT_TITLE.");

                if(geneCheckBox.getValue() && !proteinCheckBox.getValue())
                    query.append("\n  ?PARTICIPANT dcterms:description  ?PARTICIPANT_DESCRIPTION.");
                else if(geneCheckBox.getValue() || !proteinCheckBox.getValue())
                    query.append("\n  OPTIONAL { ?PARTICIPANT dcterms:description  ?PARTICIPANT_DESCRIPTION }");

                query.append("\n  FILTER(fulltext:match(?PARTICIPANT_TITLE, '''" + pattern + "''')");

                if(geneCheckBox.getValue() || !proteinCheckBox.getValue())
                    query.append(" || fulltext:match(?PARTICIPANT_DESCRIPTION, '''galectin:*''')");

                query.append(")");
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

    public void handleLoadedFile(String data)
    {
        queryTextArea.setValue(data);
    }


    @UiFactory
    DialogBox instance()
    {
        return this;
    }
}
