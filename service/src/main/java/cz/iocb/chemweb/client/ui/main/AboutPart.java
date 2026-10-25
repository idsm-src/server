package cz.iocb.chemweb.client.ui.main;

import java.util.List;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;
import cz.iocb.chemweb.shared.services.info.CountItem;
import cz.iocb.chemweb.shared.services.info.InfoService;
import cz.iocb.chemweb.shared.services.info.InfoServiceAsync;
import cz.iocb.chemweb.shared.services.info.SourceItem;



/**
 * Tab about the service, listing the counts of the items of the data collections and the versions of the data sources
 * obtained from the info service.
 */
public class AboutPart extends Composite
{
    /**
     * Binder of the tab template {@code AboutPart.ui.xml}.
     */
    interface AboutPartUiBinder extends UiBinder<Widget, AboutPart>
    {
    }

    /**
     * Template of an item of the list of the data sources.
     */
    public interface SourceTemplate extends SafeHtmlTemplates
    {
        /**
         * Formats a data source as its linked name followed by its version.
         *
         * @param url the URL of the data source
         * @param name the name of the data source
         * @param version the version of the data source
         * @return the item
         */
        @Template("<span><a href=\"{0}\" target=\"_blank\">{1}</a>: {2}</span>")
        SafeHtml format(SafeUri url, String name, String version);
    }


    /**
     * Binder of the tab template.
     */
    private static AboutPartUiBinder uiBinder = GWT.create(AboutPartUiBinder.class);

    /**
     * Asynchronous proxy of the info service.
     */
    private static InfoServiceAsync infoService = (InfoServiceAsync) GWT.create(InfoService.class);

    /**
     * Template of the items of the list of the data sources.
     */
    private static final SourceTemplate sourceTemplate = GWT.create(SourceTemplate.class);

    /**
     * Table of the counts of the items of the data collections.
     */
    @UiField
    FlexTable statisticsTable;

    /**
     * Indicator shown while the counts are loaded.
     */
    @UiField
    DivElement statisticsLoading;

    /**
     * List of the data sources with their versions.
     */
    @UiField
    UListElement sourcesList;

    /**
     * Indicator shown while the data sources are loaded.
     */
    @UiField
    DivElement sourcesLoading;


    /**
     * Creates the tab and starts loading the counts and the data sources, trying each up to three times.
     */
    public AboutPart()
    {
        initWidget(uiBinder.createAndBindUi(this));

        Element element = statisticsTable.getElement();
        TableElement tableElement = (TableElement) element;
        TableSectionElement tHead = tableElement.createTHead();

        TableRowElement row = tHead.insertRow(0);
        row.insertCell(0).setInnerText("Data Collection");
        row.insertCell(1).setInnerText("Count");


        Timer countTimer = new Timer()
        {
            int repeated = 0;

            @Override
            public void run()
            {
                repeated++;

                infoService.getCounts(new AsyncCallback<List<CountItem>>()
                {
                    @Override
                    public void onSuccess(List<CountItem> counts)
                    {
                        NumberFormat format = NumberFormat.getFormat("#,##0");

                        for(int i = 0; i < counts.size(); i++)
                        {
                            CountItem item = counts.get(i);

                            statisticsTable.setText(i, 0, item.name);
                            statisticsTable.setText(i, 1, format.format(item.count));
                        }

                        statisticsTable.setVisible(true);
                        statisticsLoading.getStyle().setDisplay(Display.NONE);
                    }

                    @Override
                    public void onFailure(Throwable caught)
                    {
                        if(repeated < 3)
                            schedule(5000);
                        else
                            statisticsLoading.setInnerHTML("loading error");
                    }
                });
            }
        };

        countTimer.run();


        Timer sourcesTimer = new Timer()
        {
            int repeated = 0;

            @Override
            public void run()
            {
                repeated++;

                infoService.getSources(new AsyncCallback<List<SourceItem>>()
                {
                    @Override
                    public void onSuccess(List<SourceItem> sources)
                    {
                        for(SourceItem source : sources)
                        {
                            LIElement liElement = Document.get().createLIElement();
                            liElement.setInnerSafeHtml(sourceTemplate.format(UriUtils.fromString(source.url),
                                    source.name, source.version));
                            sourcesList.appendChild(liElement);
                        }

                        sourcesList.getStyle().clearDisplay();
                        sourcesLoading.getStyle().setDisplay(Display.NONE);
                    }

                    @Override
                    public void onFailure(Throwable caught)
                    {
                        if(repeated < 3)
                            schedule(5000);
                        else
                            sourcesLoading.setInnerHTML("loading error");
                    }
                });
            }
        };

        sourcesTimer.run();
    }
}
