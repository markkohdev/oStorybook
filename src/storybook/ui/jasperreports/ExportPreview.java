/*
 * SbApp: Open Source software for novelists and authors.
 * Original idea 2008 - 2012 Martin Mustun
 * Copyrigth (C) Favdb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package storybook.ui.jasperreports;

import java.awt.Dimension;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JRViewer;
import storybook.StorybookApp;

public class ExportPreview extends JRViewer {

	public ExportPreview(JasperPrint param) {
		super(param);
		super.setPreferredSize(new Dimension(420, 600));
		StorybookApp.trace("ExportPreview("+(param!=null?param.getName():"null")+")");
	}

	@Override
	public void loadReport(JasperPrint param) {
		super.loadReport(param);
		StorybookApp.trace("ExportPreview.loadReport("+(param!=null?param.getName():"null")+")");
		if (param == null) {
			return;
		}
		super.forceRefresh();
		super.setFitPageZoomRatio();
	}
}