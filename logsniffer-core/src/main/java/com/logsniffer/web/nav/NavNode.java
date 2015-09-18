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
package com.logsniffer.web.nav;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a navigation node.
 * 
 * @author mbok
 * 
 */
public final class NavNode {
	private String title;
	private String path;
	private final List<NavNode> subNodes = new ArrayList<NavNode>();
	private int order;
	private NavNode parent;
	private IPageContext pageContext;

	private static final Comparator<NavNode> NODE_COMPARATOR = new Comparator<NavNode>() {
		@Override
		public int compare(final NavNode o1, final NavNode o2) {
			return o1.getOrder() - o2.getOrder();
		}
	};

	/**
	 * Creates a new node with default order 0.
	 * 
	 * @param title
	 * @param path
	 */
	public NavNode(final String title, final String path) {
		this(title, path, 0);
	}

	/**
	 * @param title
	 * @param path
	 * @param order
	 */
	public NavNode(final String title, final String path, final int order) {
		super();
		this.title = title;
		this.path = path;
		this.order = order;
	}

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
	 * @return the path
	 */

	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(final String path) {
		this.path = path;
	}

	/**
	 * 
	 * @return the order value in relation to the parent node
	 */

	public int getOrder() {
		return order;
	}

	/**
	 * @param order
	 *            the order to set
	 */
	public void setOrder(final int order) {
		this.order = order;
	}

	/**
	 * 
	 * @return ordered sub nodes of this node or an empty list if this node is a
	 *         leaf
	 */

	public List<NavNode> getSubNodes() {
		return Collections.unmodifiableList(subNodes);
	}

	/**
	 * Adds a sub node to this node. Doing that the parent of subNode will be
	 * assigned to this node.
	 * 
	 * @param subNode
	 *            the sub node
	 */
	public void addSubNode(final NavNode subNode) {
		subNodes.add(subNode);
		subNode.setParent(this);
		Collections.sort(subNodes, NODE_COMPARATOR);
	}

	/**
	 * @return the parent
	 */

	public NavNode getParent() {
		return parent;
	}

	/**
	 * Sets the parent node.
	 * 
	 * @param parent
	 *            the parent
	 */
	public void setParent(final NavNode parent) {
		this.parent = parent;
	}

	/**
	 * @return the pageContext
	 */
	public IPageContext getPageContext() {
		return pageContext;
	}

	/**
	 * @param pageContext
	 *            the pageContext to set
	 */
	public void setPageContext(final IPageContext pageContext) {
		this.pageContext = pageContext;
	}

}
