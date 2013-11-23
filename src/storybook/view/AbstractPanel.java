package storybook.view;

import java.beans.PropertyChangeEvent;

import javax.swing.JPanel;

import storybook.view.interfaces.IPaintable;
import storybook.view.interfaces.IRefreshable;

@SuppressWarnings("serial")
public abstract class AbstractPanel extends JPanel implements IRefreshable,
		IPaintable {

	protected MainFrame mainFrame;

	public AbstractPanel() {
	}

	public AbstractPanel(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}
	public abstract void init();

	public abstract void initUi();

	public abstract void modelPropertyChange(PropertyChangeEvent evt);

	public void initAll() {
		init();
		initUi();
	}

	public void refresh() {
		removeAll();
		init();
		initUi();
		invalidate();
		validate();
		repaint();
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}
}
