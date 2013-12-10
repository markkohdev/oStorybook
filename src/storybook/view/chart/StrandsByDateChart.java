/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package storybook.view.chart;

import storybook.model.DocumentModel;
import storybook.model.hbn.dao.SceneDAOImpl;
import storybook.model.hbn.dao.StrandDAOImpl;
import storybook.model.hbn.entity.Part;
import storybook.model.hbn.entity.Strand;
import storybook.toolkit.swing.ColorUtil;
import storybook.view.MainFrame;
import storybook.view.chart.jfreechart.ChartUtil;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import org.hibernate.Session;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.Layer;

public class StrandsByDateChart extends AbstractChartPanel
{
  private ChartPanel chartPanel;
  private double average;

  public StrandsByDateChart(MainFrame paramMainFrame)
  {
    super(paramMainFrame, "msg.menu.tools.charts.overall.character.date");
    this.partRelated = true;
    this.needsFullRefresh = true;
  }

  public void actionPerformed(ActionEvent paramActionEvent)
  {
  }

  protected void initChart()
  {
    CategoryDataset localCategoryDataset = createDataset();
    JFreeChart localJFreeChart = createChart(localCategoryDataset);
    this.chartPanel = new ChartPanel(localJFreeChart);
  }

  protected void initChartUi()
  {
    this.panel.add(this.chartPanel, "grow");
  }

  protected void initOptionsUi()
  {
  }

  private JFreeChart createChart(CategoryDataset paramCategoryDataset)
  {
    JFreeChart localJFreeChart = ChartFactory.createBarChart(this.chartTitle, "", "", paramCategoryDataset, PlotOrientation.VERTICAL, true, true, false);
    CategoryPlot localCategoryPlot = (CategoryPlot)localJFreeChart.getPlot();
    localCategoryPlot.addRangeMarker(ChartUtil.getAverageMarker(this.average), Layer.FOREGROUND);
    DocumentModel localDocumentModel = this.mainFrame.getDocumentModel();
    Session localSession = localDocumentModel.beginTransaction();
    StrandDAOImpl localStrandDAOImpl = new StrandDAOImpl(localSession);
    List localList = localStrandDAOImpl.findAll();
    localDocumentModel.commit();
    Color[] arrayOfColor = new Color[localList.size()];
    int i = 0;
    Object localObject = localList.iterator();
    while (((Iterator)localObject).hasNext())
    {
      Strand localStrand = (Strand)((Iterator)localObject).next();
      arrayOfColor[i] = ColorUtil.darker(localStrand.getJColor(), 0.25D);
      i++;
    }
    localObject = (BarRenderer)localCategoryPlot.getRenderer();
    for (int j = 0; j < paramCategoryDataset.getRowCount(); j++)
    {
      Color localColor = arrayOfColor[(j % arrayOfColor.length)];
      ((BarRenderer)localObject).setSeriesPaint(j, localColor);
    }
    return localJFreeChart;
  }

  private CategoryDataset createDataset()
  {
    DefaultCategoryDataset localDefaultCategoryDataset = new DefaultCategoryDataset();
    try
    {
      Part localPart = this.mainFrame.getCurrentPart();
      DocumentModel localDocumentModel = this.mainFrame.getDocumentModel();
      Session localSession = localDocumentModel.beginTransaction();
      StrandDAOImpl localStrandDAOImpl = new StrandDAOImpl(localSession);
      List localList1 = localStrandDAOImpl.findAll();
      SceneDAOImpl localSceneDAOImpl = new SceneDAOImpl(localSession);
      List localList2 = localSceneDAOImpl.findDistinctDates(localPart);
      double d = 0.0D;
      Iterator localIterator1 = localList1.iterator();
      while (localIterator1.hasNext())
      {
        Strand localStrand = (Strand)localIterator1.next();
        Iterator localIterator2 = localList2.iterator();
        while (localIterator2.hasNext())
        {
          Date localDate = (Date)localIterator2.next();
          long l = localStrandDAOImpl.countByDate(localDate, localStrand);
          localDefaultCategoryDataset.addValue(l, localStrand, localDate);
          d += l;
        }
      }
      localDocumentModel.commit();
      this.average = (d / (localList1.size() + localList2.size()));
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    return localDefaultCategoryDataset;
  }
}