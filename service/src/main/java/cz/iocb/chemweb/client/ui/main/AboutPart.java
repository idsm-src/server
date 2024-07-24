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



public class AboutPart extends Composite
{
    interface AboutPartUiBinder extends UiBinder<Widget, AboutPart>
    {
    }

    public interface SourceTemplate extends SafeHtmlTemplates
    {
        @Template("<span><a href=\"{0}\" target=\"_blank\">{1}</a>: {2}</span>")
        SafeHtml format(String url, String name, String version);
    }


    private static AboutPartUiBinder uiBinder = GWT.create(AboutPartUiBinder.class);
    private static InfoServiceAsync infoService = (InfoServiceAsync) GWT.create(InfoService.class);
    private static final SourceTemplate sourceTemplate = GWT.create(SourceTemplate.class);

    @UiField FlexTable statisticsTable;
    @UiField DivElement statisticsLoading;
    @UiField UListElement sourcesList;
    @UiField DivElement sourcesLoading;


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
                            liElement.setInnerSafeHtml(sourceTemplate.format(source.url, source.name, source.version));
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
