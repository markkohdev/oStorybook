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

import storybook.toolkit.I18N;

public class ExportReport {

	private String jasperReportName;
	private String resourceKey;

	public ExportReport(String paramString1, String paramString2) {
		this.jasperReportName = paramString1;
		this.resourceKey = paramString2;
	}

	public ExportReport() {
	}

	public void setReportName(String paramString) {
		this.jasperReportName = paramString;
	}

	public void setResourceKey(String paramString) {
		this.resourceKey = paramString;
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getJasperReportName() {
		return this.jasperReportName;
	}

	public String getName() {
		return I18N.getMsg(this.resourceKey);
	}
}