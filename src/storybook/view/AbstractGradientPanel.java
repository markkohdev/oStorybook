package storybook.view;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;

import storybook.toolkit.swing.ColorUtil;

@SuppressWarnings("serial")
public abstract class AbstractGradientPanel extends AbstractPanel {
	private Color startBgColor = Color.white;
	private Color endBgColor = Color.black;
	private boolean showBgGradient = true;

	protected MainFrame mainFrame;

	public AbstractGradientPanel() {
		showBgGradient = false;
	}

	public AbstractGradientPanel(MainFrame mainFrame) {
		this();
		this.mainFrame = mainFrame;
	}

	public AbstractGradientPanel(MainFrame mainFrame, boolean showBgGradient,
			Color startBgColor, Color endBgColor) {
		this(mainFrame);
		this.showBgGradient = showBgGradient;
		this.startBgColor = startBgColor;
		this.endBgColor = endBgColor;
	}

	public abstract void modelPropertyChange(PropertyChangeEvent evt);

	@Override
	public void refresh() {
		removeAll();
		init();
		initUi();
		invalidate();
		validate();
		repaint();
	}

	@Override
	public void paintComponent(Graphics g) {
		if (showBgGradient) {
			Graphics2D g2d = (Graphics2D) g;
			GradientPaint gradient = new GradientPaint(0, 0, startBgColor,
					this.getWidth(), this.getHeight(), ColorUtil.blend(
							Color.white, endBgColor));
			g2d.setPaint(gradient);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
		} else {
			super.paintComponent(g);
		}
	}

	public Color getEndBgColor() {
		return endBgColor;
	}

	public Color getStartBgColor() {
		return startBgColor;
	}

	public void setStartBgColor(Color startBgColor) {
		this.startBgColor = startBgColor;
	}

	public void setEndBgColor(Color endBgColor) {
		this.endBgColor = endBgColor;
	}
}
