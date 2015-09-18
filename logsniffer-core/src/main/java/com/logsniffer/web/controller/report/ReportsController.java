/*******************************************************************************
 * logsniffer, open source tool for viewing, monitoring and analysing log data.
 * Copyright (c) 2015 Scaleborn UG, www.scaleborn.com
 *
 * logsniffer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * logsniffer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.logsniffer.web.controller.report;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.quartz.SchedulerException;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.logsniffer.web.ViewController;
import com.logsniffer.web.controller.exception.ResourceNotFoundException;

@ViewController
public class ReportsController {

	@RequestMapping(value = "/reports", method = RequestMethod.GET)
	ModelAndView listReports() {
		ModelAndView mv = new ModelAndView("reports/list");
		ReportsDashboard report = new ReportsDashboard();
		report.setId(1);
		report.setCreatedAt(new Date());
		report.setName("Log processing throughput");
		mv.addObject("reports", Collections.singletonList(report));
		return mv;
	}

	ReportsDashboard getDashboard(final long id) throws IOException {
		ReportsDashboard report = new ReportsDashboard();
		report.setId(1);
		report.setCreatedAt(new Date());
		report.setName("Log processing throughput");

		// Widget 1
		ReportWidget w1 = new ReportWidget();
		w1.setData(IOUtils.toString(getClass().getResourceAsStream(
				"/dummy-report-widget1h-data.js")));
		w1.setLayout("{}");
		w1.setTitle("Overview chart");
		w1.setMode(WidgetMode.CHART);
		report.getWidgets().add(w1);

		ReportWidget w2 = new ReportWidget();
		w2.setRawHtml(IOUtils.toString(getClass().getResourceAsStream(
				"/dummy-report-widget2-data.html")));
		w2.setLayout("{}");
		w2.setTitle("Trend chart");
		w2.setMode(WidgetMode.RAW);
		report.getWidgets().add(w2);
		return report;
	}

	@RequestMapping(value = "/reports/{reportId}", method = RequestMethod.GET)
	ModelAndView report(final Model model,
			@PathVariable("reportId") final long reportId) throws IOException {
		ModelAndView mv = new ModelAndView("reports/dashboard");
		mv.addObject("report", getDashboard(reportId));
		return mv;
	}

	@RequestMapping(value = "/reports/{reportId}/reload", method = RequestMethod.POST)
	String reloadWidgets(@PathVariable("reportId") final long reportId,
			@ModelAttribute("report") final ReportsDashboard widgetsContainer,
			final BindingResult result, final Model model,
			final HttpServletResponse responce)
			throws ResourceNotFoundException, SchedulerException,
			ParseException, IOException {
		// For Chrome
		responce.setHeader("X-XSS-Protection", "0");
		ReportsDashboard mergedReport = getDashboard(reportId);
		mergedReport.setWidgets(getValidWidgets(widgetsContainer.getWidgets()));
		model.addAttribute("report", mergedReport);
		return "reports/dashboard";
	}

	protected List<ReportWidget> getValidWidgets(
			final List<ReportWidget> widgets) {
		ArrayList<ReportWidget> validWidgets = new ArrayList<ReportsController.ReportWidget>();
		for (ReportWidget w : widgets) {
			if (w.getMode() != null) {
				validWidgets.add(w);
			}
		}
		return validWidgets;
	}

	public static class ReportsDashboard {
		private long id;
		private String name;
		private Date createdAt;
		private Date updatedAt;

		@JsonRawValue
		private String widtgetLayout;
		private List<ReportWidget> widgets = new ArrayList<ReportsController.ReportWidget>();

		/**
		 * @return the id
		 */
		public long getId() {
			return id;
		}

		/**
		 * @param id
		 *            the id to set
		 */
		public void setId(final long id) {
			this.id = id;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name
		 *            the name to set
		 */
		public void setName(final String name) {
			this.name = name;
		}

		/**
		 * @return the createdAt
		 */
		public Date getCreatedAt() {
			return createdAt;
		}

		/**
		 * @param createdAt
		 *            the createdAt to set
		 */
		public void setCreatedAt(final Date createdAt) {
			this.createdAt = createdAt;
		}

		/**
		 * @return the updatedAt
		 */
		public Date getUpdatedAt() {
			return updatedAt;
		}

		/**
		 * @param updatedAt
		 *            the updatedAt to set
		 */
		public void setUpdatedAt(final Date updatedAt) {
			this.updatedAt = updatedAt;
		}

		/**
		 * @return the widtgetLayout
		 */
		public String getWidtgetLayout() {
			return widtgetLayout;
		}

		/**
		 * @param widtgetLayout
		 *            the widtgetLayout to set
		 */
		public void setWidtgetLayout(final String widtgetLayout) {
			this.widtgetLayout = widtgetLayout;
		}

		/**
		 * @return the widgets
		 */
		public List<ReportWidget> getWidgets() {
			return widgets;
		}

		/**
		 * @param widgets
		 *            the widgets to set
		 */
		public void setWidgets(final List<ReportWidget> widgets) {
			this.widgets = widgets;
		}

	}

	public enum WidgetMode {
		CHART, RAW
	}

	public static final class ReportWidget {
		private String title;

		@JsonRawValue
		private String layout = "{}";

		@JsonRawValue
		private String data = "{}";

		private String rawHtml;

		private WidgetMode mode;

		/**
		 * @return the title
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * @param title
		 *            the title to set
		 */
		public void setTitle(final String title) {
			this.title = title;
		}

		/**
		 * @return the layout
		 */
		public String getLayout() {
			return layout;
		}

		/**
		 * @param layout
		 *            the layout to set
		 */
		public void setLayout(final String layout) {
			this.layout = layout;
		}

		/**
		 * @return the data
		 */
		public String getData() {
			return data;
		}

		/**
		 * @param data
		 *            the data to set
		 */
		public void setData(final String data) {
			this.data = data;
		}

		/**
		 * @return the rawHtml
		 */
		public String getRawHtml() {
			return rawHtml;
		}

		/**
		 * @param rawHtml
		 *            the rawHtml to set
		 */
		public void setRawHtml(final String rawHtml) {
			this.rawHtml = rawHtml;
		}

		/**
		 * @return the mode
		 */
		public WidgetMode getMode() {
			return mode;
		}

		/**
		 * @param mode
		 *            the mode to set
		 */
		public void setMode(final WidgetMode mode) {
			this.mode = mode;
		}

	}
}
