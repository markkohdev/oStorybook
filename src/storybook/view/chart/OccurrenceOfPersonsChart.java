/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storybook.view.chart;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import org.hibernate.Session;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.Layer;
import storybook.model.DocumentModel;
import storybook.model.hbn.dao.PersonDAOImpl;
import storybook.model.hbn.dao.SceneDAOImpl;
import storybook.model.hbn.entity.Person;
import storybook.toolkit.swing.ColorUtil;
import storybook.view.MainFrame;
import storybook.view.chart.jfreechart.ChartUtil;
import storybook.view.chart.jfreechart.DbTableCategoryItemLabelGenerator;

public class OccurrenceOfPersonsChart extends AbstractPersonsChart
{
  private ChartPanel chartPanel;
  private double average;

  public OccurrenceOfPersonsChart(MainFrame paramMainFrame)
  {
    super(paramMainFrame, "msg.report.person.occurrence.title");
  }

	@Override
  protected void initChartUi()
  {
    CategoryDataset localCategoryDataset = createDataset();
    JFreeChart localJFreeChart = createChart(localCategoryDataset);
    this.chartPanel = new ChartPanel(localJFreeChart);
    this.panel.add(this.chartPanel, "grow");
  }

  private JFreeChart createChart(CategoryDataset paramCategoryDataset)
  {
    JFreeChart localJFreeChart = ChartFactory.createBarChart(this.chartTitle, "", "", paramCategoryDataset, PlotOrientation.VERTICAL, true, true, false);
    CategoryPlot localCategoryPlot = (CategoryPlot)localJFreeChart.getPlot();
    ChartUtil.hideDomainAxis(localCategoryPlot);
    localCategoryPlot.addRangeMarker(ChartUtil.getAverageMarker(this.average), Layer.FOREGROUND);
    BarRenderer localBarRenderer = (BarRenderer)localCategoryPlot.getRenderer();
    DbTableCategoryItemLabelGenerator localDbTableCategoryItemLabelGenerator = new DbTableCategoryItemLabelGenerator();
    localBarRenderer.setBaseItemLabelGenerator(localDbTableCategoryItemLabelGenerator);
    localBarRenderer.setBaseItemLabelsVisible(true);
    ItemLabelPosition localItemLabelPosition = ChartUtil.getNiceItemLabelPosition();
    localBarRenderer.setBasePositiveItemLabelPosition(localItemLabelPosition);
    localBarRenderer.setPositiveItemLabelPositionFallback(localItemLabelPosition);
    int i = 0;
    Color[] arrayOfColor = ColorUtil.getDarkColors(ColorUtil.getPastelColors(), 0.35D);
    for (int j = 0; j < paramCategoryDataset.getRowCount(); j++)
    {
      Person localPerson = (Person)paramCategoryDataset.getRowKey(j);
      Color localColor = localPerson.getJColor();
      if (localColor != null) {
			localColor = ColorUtil.darker(localColor, 0.15D);
		}
      else {
			localColor = arrayOfColor[(j % arrayOfColor.length)];
		}
      localBarRenderer.setSeriesPaint(j, localColor);
      if ((localColor != null) && (ColorUtil.isDark(localColor)))
        localBarRenderer.setSeriesItemLabelPaint(i, Color.white);
      i++;
    }
    return localJFreeChart;
  }

  private CategoryDataset createDataset()
  {
    DefaultCategoryDataset localDefaultCategoryDataset = new DefaultCategoryDataset();
    try
    {
      DocumentModel localDocumentModel = this.mainFrame.getDocumentModel();
      Session localSession = localDocumentModel.beginTransaction();
      PersonDAOImpl localPersonDAOImpl = new PersonDAOImpl(localSession);
      List localList = localPersonDAOImpl.findByCategories(this.selectedCategories);
      SceneDAOImpl localSceneDAOImpl = new SceneDAOImpl(localSession);
      double d = 0.0D;
      Iterator localIterator = localList.iterator();
      while (localIterator.hasNext())
      {
        Person localPerson = (Person)localIterator.next();
        long l = localSceneDAOImpl.countByPerson(localPerson);
        localDefaultCategoryDataset.addValue(l, localPerson, new Integer(1));
        d += l;
      }
      localDocumentModel.commit();
      this.average = (d / localList.size());
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    return localDefaultCategoryDataset;
  }
}